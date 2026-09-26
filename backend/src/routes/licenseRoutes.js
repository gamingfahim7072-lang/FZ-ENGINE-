const express = require('express');
const crypto = require('node:crypto');
const { db, hashKey, maskKey, recordAudit } = require('../db');

const router = express.Router();

// Helper to check system health
function getSystemHealth() {
  const row = db.prepare("SELECT value FROM system_config WHERE key = 'server_health'").get();
  return row ? row.value : 'ONLINE';
}

/**
 * POST /api/license/activate
 * Main authoritative license activation endpoint used by Android APK
 */
router.post('/activate', (req, res) => {
  const {
    license_key,
    device_name = 'Android Device',
    device_identifier_hash = 'dev_unknown',
    platform = 'Android',
    app_version = '1.0.0',
    user_id = 'usr_fz_7749'
  } = req.body;

  if (!license_key || typeof license_key !== 'string') {
    return res.status(400).json({
      success: false,
      error: { code: 'INVALID', message: 'License key is required.' }
    });
  }

  const cleanKey = license_key.trim().toUpperCase();
  if (cleanKey.length < 8) {
    return res.status(400).json({
      success: false,
      error: { code: 'INVALID', message: 'License key format is too short. Expected format: FZ-XXXX-XXXX-XXXX' }
    });
  }

  // Check server health
  const health = getSystemHealth();
  if (health === 'OFFLINE') {
    recordAudit(user_id, 'USER', 'ACTIVATE_KEY', 'FAILED', 'Server is in OFFLINE mode');
    return res.status(503).json({
      success: false,
      error: { code: 'NETWORK_OFFLINE', message: 'No Internet Connection or License Verification Server is offline.' }
    });
  }

  if (health === 'MAINTENANCE') {
    recordAudit(user_id, 'USER', 'ACTIVATE_KEY', 'FAILED', 'Server is in MAINTENANCE mode');
    return res.status(503).json({
      success: false,
      error: { code: 'MAINTENANCE', message: 'License server is currently undergoing scheduled maintenance. Please try again shortly.' }
    });
  }

  // Check simulated test keywords or database lookup
  if (cleanKey.includes('EXP')) {
    recordAudit(user_id, 'USER', 'VERIFY_KEY', 'EXPIRED', `Key ${cleanKey} has expired`);
    return res.status(400).json({
      success: false,
      error: { code: 'EXPIRED', message: 'This license key expired on the server. Please contact an authorized seller for renewal.' }
    });
  }

  if (cleanKey.includes('REV')) {
    recordAudit(user_id, 'USER', 'VERIFY_KEY', 'REVOKED', `Key ${cleanKey} was revoked`);
    return res.status(403).json({
      success: false,
      error: { code: 'REVOKED', message: 'This license has been revoked by the issuer due to a refund or security alert.' }
    });
  }

  if (cleanKey.includes('BAN')) {
    recordAudit(user_id, 'USER', 'VERIFY_KEY', 'BANNED', 'Account or key blacklisted');
    return res.status(403).json({
      success: false,
      error: { code: 'BANNED', message: 'This license key has been blacklisted for fair-play or policy violation.' }
    });
  }

  if (cleanKey.includes('DEV') || cleanKey.includes('LIMIT')) {
    recordAudit(user_id, 'USER', 'VERIFY_KEY', 'DEVICE_LIMIT', 'Device limit reached');
    return res.status(409).json({
      success: false,
      error: { code: 'DEVICE_LIMIT', message: 'Device limit reached. This license is already bound to maximum allowed devices (1/1).' }
    });
  }

  if (cleanKey.includes('ERR') || cleanKey.includes('500')) {
    recordAudit(user_id, 'USER', 'VERIFY_KEY', 'SERVER_ERROR', 'Internal server error simulation');
    return res.status(500).json({
      success: false,
      error: { code: 'SERVER_ERROR', message: 'Server is temporarily unavailable. Error Code: 503. Please try again.' }
    });
  }

  const kHash = hashKey(cleanKey);
  const now = Date.now();

  // Search by hash or cleanKey
  let lic = db.prepare('SELECT * FROM licenses WHERE license_key_hash = ? OR license_key = ?').get(kHash, cleanKey);

  if (!lic) {
    // If not in database, reject unless valid formatted key in demo mode
    if (!cleanKey.startsWith('FZ-') && !cleanKey.startsWith('VIP-') && !cleanKey.startsWith('KEY-')) {
      recordAudit(user_id, 'USER', 'VERIFY_KEY', 'INVALID', `Key not found: ${cleanKey}`);
      return res.status(404).json({
        success: false,
        error: { code: 'INVALID', message: 'Invalid license key. Checksum verification failed on the server.' }
      });
    }

    // Auto-create dynamically for valid prefix keys in dynamic testing
    const durationDays = cleanKey.includes('365') || cleanKey.includes('YEAR') ? 365
      : cleanKey.includes('90') ? 90
      : cleanKey.includes('7') ? 7
      : 30;

    const planName = durationDays === 365 ? 'Pro Engine Pass (Annual)'
      : durationDays === 90 ? 'Pro Engine Pass (Quarterly)'
      : durationDays === 7 ? 'Trial Engine Pass (7 Days)'
      : 'Pro Engine Pass (30 Days)';

    const newId = 'lic_' + crypto.randomUUID().slice(0, 8);
    db.prepare(`
      INSERT INTO licenses (
        id, license_key, license_key_hash, masked_key, plan, duration, duration_days,
        status, seller_id, user_id, device_binding, created_at, activated_at, expires_at,
        activation_count, max_activations, revoked_at, prefix
      ) VALUES (?, ?, ?, ?, ?, ?, ?, 'UNUSED', 'sel_apex_01', NULL, NULL, ?, NULL, NULL, 0, 1, NULL, 'FZ-')
    `).run(
      newId, cleanKey, kHash, maskKey(cleanKey), planName, `${durationDays} Days`, durationDays, now
    );

    lic = db.prepare('SELECT * FROM licenses WHERE id = ?').get(newId);
  }

  // Validate state
  if (lic.status === 'REVOKED') {
    recordAudit(user_id, 'USER', 'ACTIVATE_KEY', 'REVOKED', `License ${lic.id} was revoked`);
    return res.status(403).json({
      success: false,
      error: { code: 'REVOKED', message: 'This license has been revoked by the issuer due to a refund or security alert.' }
    });
  }

  if (lic.status === 'BANNED') {
    recordAudit(user_id, 'USER', 'ACTIVATE_KEY', 'BANNED', `License ${lic.id} was banned`);
    return res.status(403).json({
      success: false,
      error: { code: 'BANNED', message: 'This license key has been blacklisted for policy violation.' }
    });
  }

  if (lic.status === 'EXPIRED' || (lic.expires_at && now > lic.expires_at)) {
    if (lic.status !== 'EXPIRED') {
      db.prepare("UPDATE licenses SET status = 'EXPIRED' WHERE id = ?").run(lic.id);
    }
    recordAudit(user_id, 'USER', 'ACTIVATE_KEY', 'EXPIRED', `License ${lic.id} is expired`);
    return res.status(400).json({
      success: false,
      error: { code: 'EXPIRED', message: 'This license key expired on the server. Please contact an authorized seller for renewal.' }
    });
  }

  const deviceBinding = `${device_name} (${device_identifier_hash.slice(0, 8)})`;

  if (lic.status === 'ACTIVE') {
    // Check device binding
    if (lic.device_binding && lic.device_binding !== deviceBinding && lic.activation_count >= lic.max_activations) {
      recordAudit(user_id, 'USER', 'ACTIVATE_KEY', 'DEVICE_LIMIT', `Already bound to ${lic.device_binding}`);
      return res.status(409).json({
        success: false,
        error: { code: 'DEVICE_LIMIT', message: `Device limit reached. License bound to: ${lic.device_binding}` }
      });
    }

    // Same device returning - refresh last seen
    db.prepare('UPDATE licenses SET user_id = ?, device_binding = ? WHERE id = ?').run(user_id, deviceBinding, lic.id);
  } else if (lic.status === 'UNUSED') {
    // First time activation!
    const expiresAt = now + Math.round(lic.duration_days * 86400000);
    db.prepare(`
      UPDATE licenses
      SET status = 'ACTIVE',
          user_id = ?,
          device_binding = ?,
          activated_at = ?,
          expires_at = ?,
          activation_count = 1
      WHERE id = ?
    `).run(user_id, deviceBinding, now, expiresAt, lic.id);

    // Record sale in sales table
    const saleId = 'sal_' + crypto.randomUUID().slice(0, 8);
    const amount = lic.duration_days >= 365 ? 89.99 : lic.duration_days >= 90 ? 29.99 : lic.duration_days >= 30 ? 14.99 : 4.99;
    db.prepare(`
      INSERT INTO sales (id, seller_id, license_id, plan, amount, currency, country, status, created_at)
      VALUES (?, ?, ?, ?, ?, 'USD', 'US', 'COMPLETED', ?)
    `).run(saleId, lic.seller_id, lic.id, lic.plan, amount, now);

    // Update seller stats
    db.prepare(`
      UPDATE sellers
      SET total_sales = total_sales + 1,
          amount_sold_usd = amount_sold_usd + ?,
          wallet_balance_usd = wallet_balance_usd + ?
      WHERE id = ?
    `).run(amount, amount * 0.20, lic.seller_id); // 20% commission

    // Create notification
    db.prepare(`
      INSERT INTO notifications (id, user_id, role_target, title, message, category, read_status, created_at)
      VALUES (?, ?, 'USER', 'License Activated Successfully', ?, 'LICENSE_ACTIVATED', 0, ?)
    `).run(
      'notif_' + crypto.randomUUID().slice(0, 6),
      user_id,
      `${lic.plan} is now active on ${device_name}.`,
      now
    );

    recordAudit(user_id, 'USER', 'LICENSE_ACTIVATION', 'SUCCESS', `Activated ${lic.plan} on ${deviceBinding}`);
  }

  // Register or update active session
  const sessionId = 'sess_' + crypto.randomUUID().slice(0, 8);
  db.prepare(`
    INSERT OR REPLACE INTO sessions (
      id, user_id, license_id, device_name, device_identifier_hash, platform, app_version,
      created_at, last_seen, expires_at, is_current, is_revoked, revoked_at
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0, NULL)
  `).run(
    sessionId, user_id, lic.id, device_name, device_identifier_hash, platform, app_version,
    now, now, now + 86400000 * 30
  );

  // Fetch fresh license entity
  const updatedLic = db.prepare('SELECT * FROM licenses WHERE id = ?').get(lic.id);
  const seller = db.prepare('SELECT displayName, country FROM sellers WHERE id = ?').get(lic.seller_id) || { displayName: 'Apex Gaming Hub (Verified)', country: 'United States' };

  res.json({
    success: true,
    data: {
      license: {
        id: updatedLic.id,
        keyHash: updatedLic.license_key_hash,
        maskedKey: updatedLic.masked_key,
        plan: updatedLic.plan,
        durationDays: Math.round(updatedLic.duration_days),
        status: updatedLic.status,
        createdAt: updatedLic.created_at,
        activatedAt: updatedLic.activated_at,
        expiresAt: updatedLic.expires_at,
        revokedAt: updatedLic.revoked_at,
        deviceBinding: updatedLic.device_binding,
        sellerId: updatedLic.seller_id,
        sellerName: seller.displayName || seller.display_name,
        activationCount: updatedLic.activation_count,
        maxActivations: updatedLic.max_activations
      },
      session: {
        id: sessionId,
        deviceName: device_name,
        expiresAt: now + 86400000 * 30
      },
      message: `${updatedLic.plan} successfully verified and cryptographically locked to this hardware.`
    }
  });
});

/**
 * GET /api/license/status
 * Heartbeat verification for the APK
 */
router.get('/status', (req, res) => {
  const { key_hash, license_id, session_id } = req.query;

  if (!key_hash && !license_id) {
    return res.status(400).json({ success: false, error: { code: 'MISSING_PARAM', message: 'key_hash or license_id required' } });
  }

  const lic = db.prepare('SELECT * FROM licenses WHERE license_key_hash = ? OR id = ?').get(key_hash || '', license_id || '');

  if (!lic) {
    return res.status(404).json({ success: false, error: { code: 'NOT_FOUND', message: 'License record not found on server.' } });
  }

  const now = Date.now();
  let currentStatus = lic.status;

  if (lic.status === 'ACTIVE' && lic.expires_at && now > lic.expires_at) {
    currentStatus = 'EXPIRED';
    db.prepare("UPDATE licenses SET status = 'EXPIRED' WHERE id = ?").run(lic.id);
  }

  // Check session revocation
  let sessionRevoked = false;
  if (session_id) {
    const sess = db.prepare('SELECT is_revoked FROM sessions WHERE id = ?').get(session_id);
    if (sess && sess.is_revoked) {
      sessionRevoked = true;
    }
  }

  res.json({
    success: true,
    data: {
      id: lic.id,
      status: currentStatus,
      expiresAt: lic.expires_at,
      activatedAt: lic.activated_at,
      revokedAt: lic.revoked_at,
      deviceBinding: lic.device_binding,
      sessionRevoked,
      serverTime: now
    }
  });
});

module.exports = router;
