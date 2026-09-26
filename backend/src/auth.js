const crypto = require('node:crypto');
const { db } = require('./db');

const JWT_SECRET = process.env.JWT_SECRET || 'fz_engine_super_secure_jwt_secret_2026_x89';

// Simple, fast HMAC-SHA256 based JWT implementation (zero external dependency)
function base64UrlEncode(str) {
  return Buffer.from(str).toString('base64url');
}

function base64UrlDecode(str) {
  return Buffer.from(str, 'base64url').toString('utf-8');
}

function generateToken(payload, expiresInMs = 7 * 86400000) {
  const header = { alg: 'HS256', typ: 'JWT' };
  const exp = Date.now() + expiresInMs;
  const fullPayload = { ...payload, exp };
  
  const encodedHeader = base64UrlEncode(JSON.stringify(header));
  const encodedPayload = base64UrlEncode(JSON.stringify(fullPayload));
  
  const signature = crypto
    .createHmac('sha256', JWT_SECRET)
    .update(`${encodedHeader}.${encodedPayload}`)
    .digest('base64url');
    
  return `${encodedHeader}.${encodedPayload}.${signature}`;
}

function verifyToken(token) {
  if (!token || typeof token !== 'string') return null;
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  
  const [header, payload, signature] = parts;
  const expectedSignature = crypto
    .createHmac('sha256', JWT_SECRET)
    .update(`${header}.${payload}`)
    .digest('base64url');
    
  if (signature !== expectedSignature) return null;
  
  try {
    const data = JSON.parse(base64UrlDecode(payload));
    if (data.exp && Date.now() > data.exp) {
      return null; // Expired
    }
    return data;
  } catch {
    return null;
  }
}

function authMiddleware(req, res, next) {
  const authHeader = req.headers['authorization'];
  if (!authHeader) {
    req.user = null;
    return next();
  }
  
  const token = authHeader.startsWith('Bearer ') ? authHeader.slice(7) : authHeader;
  const decoded = verifyToken(token);
  if (decoded) {
    req.user = decoded;
  } else {
    req.user = null;
  }
  next();
}

function requireAuth(req, res, next) {
  if (!req.user) {
    return res.status(401).json({
      success: false,
      error: { code: 'UNAUTHORIZED', message: 'Authentication required. Please sign in.' }
    });
  }
  next();
}

function requireRole(...allowedRoles) {
  return (req, res, next) => {
    if (!req.user) {
      return res.status(401).json({
        success: false,
        error: { code: 'UNAUTHORIZED', message: 'Authentication required.' }
      });
    }
    if (!allowedRoles.includes(req.user.role)) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN',
          message: `Access denied. Role ${req.user.role} does not have required permissions.`
        }
      });
    }
    next();
  };
}

module.exports = {
  generateToken,
  verifyToken,
  authMiddleware,
  requireAuth,
  requireRole
};
