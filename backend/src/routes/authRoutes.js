const express = require('express');
const crypto = require('node:crypto');
const { db, recordAudit } = require('../db');
const { generateToken, requireAuth } = require('../auth');

const router = express.Router();

router.post('/login', (req, res) => {
  const { email, password, role_override } = req.body;

  // Convenient development / showcase login by role if specified
  if (role_override) {
    const user = db.prepare('SELECT * FROM users WHERE role = ? LIMIT 1').get(role_override.toUpperCase());
    if (user) {
      const token = generateToken({
        id: user.id,
        email: user.email,
        display_name: user.display_name,
        role: user.role
      });
      recordAudit(user.id, user.role, 'LOGIN_ROLE_PERSPECTIVE', 'SUCCESS', `Logged in as ${user.role}`);
      return res.json({
        success: true,
        data: {
          token,
          user: {
            id: user.id,
            display_name: user.display_name,
            email: user.email,
            role: user.role,
            status: user.status
          }
        }
      });
    }
  }

  if (!email || !password) {
    return res.status(400).json({
      success: false,
      error: { code: 'INVALID_CREDENTIALS', message: 'Email and password are required.' }
    });
  }

  const passHash = crypto.createHash('sha256').update(password).digest('hex');
  const user = db.prepare('SELECT * FROM users WHERE email = ?').get(email.trim().toLowerCase());

  if (!user || user.password_hash !== passHash) {
    recordAudit(email, 'UNKNOWN', 'LOGIN_FAILED', 'INVALID_CREDENTIALS', 'Failed login attempt');
    return res.status(401).json({
      success: false,
      error: { code: 'INVALID_CREDENTIALS', message: 'Invalid email or password.' }
    });
  }

  if (user.status !== 'ACTIVE') {
    recordAudit(user.id, user.role, 'LOGIN_FAILED', 'ACCOUNT_SUSPENDED', 'Account suspended or banned');
    return res.status(403).json({
      success: false,
      error: { code: 'ACCOUNT_SUSPENDED', message: 'Your account has been suspended or deactivated.' }
    });
  }

  db.prepare('UPDATE users SET last_login = ? WHERE id = ?').run(Date.now(), user.id);
  const token = generateToken({
    id: user.id,
    email: user.email,
    display_name: user.display_name,
    role: user.role
  });

  recordAudit(user.id, user.role, 'LOGIN_SUCCESS', 'SUCCESS', 'Authenticated via credentials');

  res.json({
    success: true,
    data: {
      token,
      user: {
        id: user.id,
        display_name: user.display_name,
        email: user.email,
        role: user.role,
        status: user.status
      }
    }
  });
});

router.post('/logout', requireAuth, (req, res) => {
  recordAudit(req.user.id, req.user.role, 'LOGOUT', 'SUCCESS', 'User logged out');
  res.json({ success: true, message: 'Logged out successfully.' });
});

router.get('/session', (req, res) => {
  if (!req.user) {
    return res.json({
      success: true,
      data: {
        authenticated: false,
        user: null
      }
    });
  }

  const user = db.prepare('SELECT id, display_name, email, role, status, created_at, last_login FROM users WHERE id = ?').get(req.user.id);
  if (!user) {
    return res.status(401).json({ success: false, error: { code: 'USER_NOT_FOUND', message: 'Session user no longer exists.' } });
  }

  // If seller, attach seller details
  let sellerInfo = null;
  if (user.role === 'SELLER') {
    sellerInfo = db.prepare('SELECT * FROM sellers WHERE user_id = ? OR id = ?').get(user.id, 'sel_apex_01');
  }

  res.json({
    success: true,
    data: {
      authenticated: true,
      user,
      seller: sellerInfo
    }
  });
});

module.exports = router;
