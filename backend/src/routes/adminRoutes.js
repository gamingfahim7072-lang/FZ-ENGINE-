const express = require('express');
const crypto = require('node:crypto');
const { db, recordAudit } = require('../db');
const { requireAuth, requireRole } = require('../auth');

const router = express.Router();

/**
 * GET /api/admin/users
 */
router.get('/users', (req, res) => {
  const users = db.prepare('SELECT id, display_name, email, role, status, created_at, last_login FROM users ORDER BY created_at DESC').all();
  res.json({ success: true, data: users });
});

/**
 * GET /api/admin/sellers
 */
router.get('/sellers', (req, res) => {
  const sellers = db.prepare('SELECT * FROM sellers ORDER BY created_at DESC').all();
  res.json({ success: true, data: sellers });
});

/**
 * POST /api/admin/sellers
 * Create new seller (Owner test step)
 */
router.post('/sellers', (req, res) => {
  const {
    display_name,
    country,
    country_code = 'US',
    published_contact_method,
    territory = 'Global',
    email
  } = req.body;

  if (!display_name || !country) {
    return res.status(400).json({ success: false, error: { code: 'INVALID_INPUT', message: 'Name and country are required.' } });
  }

  const sellerId = 'sel_' + crypto.randomUUID().slice(0, 8);
  const now = Date.now();
  const avatarTag = display_name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase() || 'FZ';

  let userId = null;
  if (email) {
    userId = 'usr_' + crypto.randomUUID().slice(0, 8);
    const passHash = crypto.createHash('sha256').update('seller_pass_2026').digest('hex');
    db.prepare(`
      INSERT INTO users (id, display_name, email, password_hash, role, status, created_at, last_login)
      VALUES (?, ?, ?, ?, 'SELLER', 'ACTIVE', ?, ?)
    `).run(userId, display_name, email.toLowerCase(), passHash, now, now);
  }

  db.prepare(`
    INSERT INTO sellers (
      id, user_id, display_name, country, country_code, avatar_tag, verification_status,
      status, total_sales, amount_sold_usd, published_contact_method, rating, territory,
      wallet_balance_usd, created_at
    ) VALUES (?, ?, ?, ?, ?, ?, 1, 'VERIFIED', 0, 0.0, ?, 5.0, ?, 0.0, ?)
  `).run(
    sellerId, userId, display_name, country, country_code.toUpperCase(), avatarTag,
    published_contact_method || '@FZSupportDesk', territory, now
  );

  recordAudit(
    req.user ? req.user.id : 'ADMIN',
    req.user ? req.user.role : 'OWNER',
    'CREATE_SELLER',
    'SUCCESS',
    `Created verified seller ${display_name} (${sellerId})`
  );

  const created = db.prepare('SELECT * FROM sellers WHERE id = ?').get(sellerId);
  res.json({ success: true, data: created, message: `Seller ${display_name} created successfully.` });
});

/**
 * GET /api/admin/audit-logs
 */
router.get('/audit-logs', (req, res) => {
  const { limit = 100 } = req.query;
  const logs = db.prepare('SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ?').all(Number(limit));
  res.json({ success: true, data: logs });
});

/**
 * POST /api/admin/system-status
 * Toggle server status for testing and maintenance
 */
router.post('/system-status', (req, res) => {
  const { status, notice, latency_ms } = req.body;

  if (status) {
    const valid = ['ONLINE', 'DEGRADED', 'MAINTENANCE', 'OFFLINE'];
    if (!valid.includes(status.toUpperCase())) {
      return res.status(400).json({ success: false, error: { message: 'Invalid status. Must be ONLINE, DEGRADED, MAINTENANCE, or OFFLINE.' } });
    }
    const now = Date.now();
    db.prepare("INSERT OR REPLACE INTO system_config (key, value, updated_at) VALUES ('server_health', ?, ?)").run(status.toUpperCase(), now);

    if (notice) {
      db.prepare("INSERT OR REPLACE INTO system_config (key, value, updated_at) VALUES ('maintenance_notice', ?, ?)").run(notice, now);
    }
    if (latency_ms !== undefined) {
      db.prepare("INSERT OR REPLACE INTO system_config (key, value, updated_at) VALUES ('latency_ms', ?, ?)").run(String(latency_ms), now);
    }

    recordAudit('ADMIN', 'OWNER', 'CHANGE_SYSTEM_STATUS', 'SUCCESS', `System status set to ${status}`);
  }

  const current = db.prepare("SELECT value FROM system_config WHERE key = 'server_health'").get();
  res.json({ success: true, data: { status: current ? current.value : 'ONLINE' } });
});

/**
 * POST /api/admin/broadcast
 */
router.post('/broadcast', (req, res) => {
  const { title, message, category = 'ANNOUNCEMENT' } = req.body;
  if (!title || !message) {
    return res.status(400).json({ success: false, error: { message: 'Title and message required.' } });
  }

  const notifId = 'notif_' + crypto.randomUUID().slice(0, 6);
  const now = Date.now();

  db.prepare(`
    INSERT INTO notifications (id, user_id, role_target, title, message, category, read_status, created_at)
    VALUES (?, NULL, 'ALL', ?, ?, ?, 0, ?)
  `).run(notifId, title, message, category, now);

  recordAudit('ADMIN', 'OWNER', 'BROADCAST_NOTIFICATION', 'SUCCESS', `Broadcast: ${title}`);
  res.json({ success: true, message: 'Broadcast notification dispatched to all clients.' });
});

module.exports = router;
