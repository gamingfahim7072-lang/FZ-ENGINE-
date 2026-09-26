const express = require('express');
const crypto = require('node:crypto');
const { db, hashKey, maskKey, recordAudit } = require('../db');
const { requireAuth, requireRole } = require('../auth');

const router = express.Router();

// Helper to determine seller ID for request
function getSellerId(req) {
  if (req.user && req.user.role === 'SELLER') {
    const s = db.prepare('SELECT id FROM sellers WHERE user_id = ?').get(req.user.id);
    if (s) return s.id;
  }
  // Default partner demo seller or admin fallback
  return 'sel_apex_01';
}

/**
 * GET /api/partner/dashboard
 * Live metrics and statistics for the Partner Panel
 */
router.get('/dashboard', (req, res) => {
  const sellerId = getSellerId(req);

  // Stats for this seller (or all if OWNER/DEVELOPER)
  const isOwnerOrDev = req.user && (req.user.role === 'OWNER' || req.user.role === 'DEVELOPER');

  const licQuery = isOwnerOrDev
    ? 'SELECT status, count(*) as c FROM licenses GROUP BY status'
    : 'SELECT status, count(*) as c FROM licenses WHERE seller_id = ? GROUP BY status';
  const licParams = isOwnerOrDev ? [] : [sellerId];

  const statusRows = db.prepare(licQuery).all(...licParams);
  const statusCounts = {
    TOTAL: 0,
    ACTIVE: 0,
    UNUSED: 0,
    EXPIRED: 0,
    REVOKED: 0,
    BANNED: 0
  };

  for (const r of statusRows) {
    statusCounts[r.status] = r.c;
    statusCounts.TOTAL += r.c;
  }

  // Sales and revenue metrics
  const salesQuery = isOwnerOrDev
    ? 'SELECT count(*) as total_sales, sum(amount) as revenue FROM sales WHERE status = "COMPLETED"'
    : 'SELECT count(*) as total_sales, sum(amount) as revenue FROM sales WHERE seller_id = ? AND status = "COMPLETED"';
  const salesStat = db.prepare(salesQuery).get(...licParams) || { total_sales: 0, revenue: 0 };

  // Seller details & wallet
  const seller = db.prepare('SELECT * FROM sellers WHERE id = ?').get(sellerId) || {
    display_name: 'Apex Gaming Hub',
    wallet_balance_usd: 8676.0,
    rating: 4.95,
    country: 'United States'
  };

  // Recent generated/activated licenses
  const recentLicensesQuery = isOwnerOrDev
    ? 'SELECT * FROM licenses ORDER BY created_at DESC LIMIT 6'
    : 'SELECT * FROM licenses WHERE seller_id = ? ORDER BY created_at DESC LIMIT 6';
  const recentLicenses = db.prepare(recentLicensesQuery).all(...licParams);

  // Recent sales transactions
  const recentSalesQuery = isOwnerOrDev
    ? 'SELECT * FROM sales ORDER BY created_at DESC LIMIT 6'
    : 'SELECT * FROM sales WHERE seller_id = ? ORDER BY created_at DESC LIMIT 6';
  const recentSales = db.prepare(recentSalesQuery).all(...licParams);

  res.json({
    success: true,
    data: {
      seller: {
        id: seller.id,
        displayName: seller.display_name,
        walletBalanceUsd: seller.wallet_balance_usd || 0.0,
        rating: seller.rating,
        country: seller.country
      },
      stats: {
        totalKeys: statusCounts.TOTAL,
        activeKeys: statusCounts.ACTIVE,
        unusedKeys: statusCounts.UNUSED,
        expiredKeys: statusCounts.EXPIRED,
        revokedKeys: statusCounts.REVOKED,
        totalSales: salesStat.total_sales || 0,
        revenueUsd: salesStat.revenue || 0.0
      },
      recentLicenses: recentLicenses.map(l => ({
        id: l.id,
        key: l.license_key || l.masked_key,
        maskedKey: l.masked_key,
        plan: l.plan,
        duration: l.duration,
        status: l.status,
        deviceBinding: l.device_binding,
        createdAt: l.created_at,
        activatedAt: l.activated_at,
        expiresAt: l.expires_at
      })),
      recentSales: recentSales.map(s => ({
        id: s.id,
        plan: s.plan,
        amount: s.amount,
        currency: s.currency,
        country: s.country,
        createdAt: s.created_at
      }))
    }
  });
});

/**
 * GET /api/partner/licenses
 * List licenses with search, filter, pagination
 */
router.get('/licenses', (req, res) => {
  const sellerId = getSellerId(req);
  const isOwnerOrDev = req.user && (req.user.role === 'OWNER' || req.user.role === 'DEVELOPER');
  const { status, search, limit = 50, offset = 0 } = req.query;

  let query = isOwnerOrDev ? 'SELECT * FROM licenses WHERE 1=1' : 'SELECT * FROM licenses WHERE seller_id = ?';
  const params = isOwnerOrDev ? [] : [sellerId];

  if (status && status !== 'ALL') {
    query += ' AND status = ?';
    params.push(status.toUpperCase());
  }

  if (search) {
    query += ' AND (license_key LIKE ? OR masked_key LIKE ? OR plan LIKE ? OR device_binding LIKE ?)';
    params.push(`%${search}%`, `%${search}%`, `%${search}%`, `%${search}%`);
  }

  query += ' ORDER BY created_at DESC LIMIT ? OFFSET ?';
  params.push(Number(limit), Number(offset));

  const rows = db.prepare(query).all(...params);

  res.json({
    success: true,
    data: rows.map(l => ({
      id: l.id,
      licenseKey: l.license_key || l.masked_key,
      maskedKey: l.masked_key,
      plan: l.plan,
      duration: l.duration,
      durationDays: l.duration_days,
      status: l.status,
      sellerId: l.seller_id,
      deviceBinding: l.device_binding,
      createdAt: l.created_at,
      activatedAt: l.activated_at,
      expiresAt: l.expires_at,
      revokedAt: l.revoked_at,
      activationCount: l.activation_count
    }))
  });
});

/**
 * POST /api/partner/licenses/generate
 * Generates secure digital license keys with duration options
 */
router.post('/licenses/generate', (req, res) => {
  const sellerId = getSellerId(req);
  const {
    duration_option = '30 Days',
    custom_days,
    prefix = 'FZ-',
    quantity = 1
  } = req.body;

  const count = Math.min(Math.max(1, Number(quantity) || 1), 100);

  // Map duration options
  let durationDays = 30;
  let planName = 'Pro Engine Pass (30 Days)';
  let durationLabel = duration_option;

  switch (duration_option) {
    case '2 Hours':
      durationDays = 2 / 24; // 0.0833 days
      planName = 'Flash Pass (2 Hours)';
      break;
    case '1 Day':
      durationDays = 1;
      planName = 'Daily Engine Pass (24 Hours)';
      break;
    case '2 Days':
      durationDays = 2;
      planName = 'Weekend Engine Pass (2 Days)';
      break;
    case '3 Days':
      durationDays = 3;
      planName = '3-Day Engine Pass';
      break;
    case '7 Days':
      durationDays = 7;
      planName = 'Trial Engine Pass (7 Days)';
      break;
    case '15 Days':
      durationDays = 15;
      planName = 'Semi-Monthly Pass (15 Days)';
      break;
    case '30 Days':
      durationDays = 30;
      planName = 'Pro Engine Pass (30 Days)';
      break;
    case '3 Months':
      durationDays = 90;
      planName = 'Pro Engine Pass (Quarterly)';
      break;
    case 'Custom':
      durationDays = Math.max(0.1, Number(custom_days) || 30);
      planName = `Custom Engine Pass (${durationDays} Days)`;
      durationLabel = `${durationDays} Days`;
      break;
    default:
      durationDays = 30;
      planName = 'Pro Engine Pass (30 Days)';
      durationLabel = '30 Days';
      break;
  }

  const cleanPrefix = (prefix.trim().toUpperCase() || 'FZ-').replace(/[^A-Z0-9-]/g, '');
  const now = Date.now();
  const generatedKeys = [];

  const insertStmt = db.prepare(`
    INSERT INTO licenses (
      id, license_key, license_key_hash, masked_key, plan, duration, duration_days,
      status, seller_id, user_id, device_binding, created_at, activated_at, expires_at,
      activation_count, max_activations, revoked_at, prefix
    ) VALUES (?, ?, ?, ?, ?, ?, ?, 'UNUSED', ?, NULL, NULL, ?, NULL, NULL, 0, 1, NULL, ?)
  `);

  for (let i = 0; i < count; i++) {
    // Generate secure random key: e.g. FZ-ABCD-1234-EFGH
    const part1 = crypto.randomBytes(2).toString('hex').toUpperCase();
    const part2 = crypto.randomBytes(2).toString('hex').toUpperCase();
    const part3 = crypto.randomBytes(2).toString('hex').toUpperCase();
    const rawKey = `${cleanPrefix}${part1}-${part2}-${part3}`;
    const kHash = hashKey(rawKey);
    const mKey = maskKey(rawKey);
    const licId = 'lic_' + crypto.randomUUID().slice(0, 8);

    insertStmt.run(
      licId,
      rawKey,
      kHash,
      mKey,
      planName,
      durationLabel,
      durationDays,
      sellerId,
      now,
      cleanPrefix
    );

    generatedKeys.push({
      id: licId,
      licenseKey: rawKey,
      maskedKey: mKey,
      plan: planName,
      duration: durationLabel,
      durationDays,
      status: 'UNUSED',
      createdAt: now
    });
  }

  const actorRole = req.user ? req.user.role : 'SELLER';
  const actorId = req.user ? req.user.id : sellerId;
  recordAudit(actorId, actorRole, 'GENERATE_LICENSES', 'SUCCESS', `Generated ${count} licenses for ${planName}`);

  res.json({
    success: true,
    data: {
      generatedCount: count,
      plan: planName,
      duration: durationLabel,
      keys: generatedKeys
    }
  });
});

/**
 * POST /api/partner/licenses/revoke
 * Instantly revokes license and remote sessions
 */
router.post('/licenses/revoke', (req, res) => {
  const { license_id, reason = 'Administrative revocation' } = req.body;
  const sellerId = getSellerId(req);
  const isOwnerOrDev = req.user && (req.user.role === 'OWNER' || req.user.role === 'DEVELOPER');

  if (!license_id) {
    return res.status(400).json({ success: false, error: { code: 'INVALID_ID', message: 'license_id is required.' } });
  }

  const lic = db.prepare('SELECT * FROM licenses WHERE id = ?').get(license_id);
  if (!lic) {
    return res.status(404).json({ success: false, error: { code: 'NOT_FOUND', message: 'License not found.' } });
  }

  // Security check: sellers cannot revoke another seller's license
  if (!isOwnerOrDev && lic.seller_id !== sellerId) {
    return res.status(403).json({ success: false, error: { code: 'FORBIDDEN', message: 'Cannot revoke licenses belonging to another seller.' } });
  }

  const now = Date.now();
  db.prepare("UPDATE licenses SET status = 'REVOKED', revoked_at = ? WHERE id = ?").run(now, license_id);

  // Revoke active sessions for this license
  db.prepare('UPDATE sessions SET is_revoked = 1, revoked_at = ? WHERE license_id = ?').run(now, license_id);

  const actorId = req.user ? req.user.id : sellerId;
  const actorRole = req.user ? req.user.role : 'SELLER';
  recordAudit(actorId, actorRole, 'REVOKE_LICENSE', 'SUCCESS', `Revoked license ${license_id}. Reason: ${reason}`);

  res.json({
    success: true,
    message: `License ${lic.masked_key} has been revoked immediately. Hardware sessions disconnected.`
  });
});

/**
 * GET /api/partner/wallet
 */
router.get('/wallet', (req, res) => {
  const sellerId = getSellerId(req);
  const seller = db.prepare('SELECT * FROM sellers WHERE id = ?').get(sellerId);

  res.json({
    success: true,
    data: {
      sellerId,
      walletBalanceUsd: seller ? seller.wallet_balance_usd : 0.0,
      totalSales: seller ? seller.total_sales : 0,
      amountSoldUsd: seller ? seller.amount_sold_usd : 0.0,
      payoutMethods: [
        { type: 'USDT (TRC20)', address: 'TQ9x7...88jZ (Verified)', status: 'ACTIVE' },
        { type: 'Bank Wire', account: '••••4812 (Verified)', status: 'READY' }
      ]
    }
  });
});

module.exports = router;
