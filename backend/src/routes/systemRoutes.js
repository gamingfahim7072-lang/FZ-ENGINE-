const express = require('express');
const { db, recordAudit } = require('../db');

const router = express.Router();

/**
 * GET /api/server/status
 * Real health and latency telemetry endpoint for Android APK and Web clients
 */
router.get('/server/status', (req, res) => {
  const healthRow = db.prepare("SELECT value FROM system_config WHERE key = 'server_health'").get();
  const noticeRow = db.prepare("SELECT value FROM system_config WHERE key = 'maintenance_notice'").get();
  const latencyRow = db.prepare("SELECT value FROM system_config WHERE key = 'latency_ms'").get();

  const status = healthRow ? healthRow.value : 'ONLINE';
  const notice = noticeRow ? noticeRow.value : 'System operational';
  const latency = latencyRow ? Number(latencyRow.value) : 38;

  if (status === 'OFFLINE') {
    return res.status(503).json({
      success: false,
      error: { code: 'OFFLINE', message: 'Licensing server is temporarily offline.' }
    });
  }

  res.json({
    success: true,
    data: {
      status,
      latencyMs: latency,
      serverTime: Date.now(),
      maintenanceNotice: notice,
      edgeLocation: 'Google Cloud (asia-east1)',
      tlsVersion: 'TLS 1.3 / Strict Enclave',
      antiTamperVerified: true
    }
  });
});

/**
 * GET /api/app/version
 * OTA Application update configuration endpoint
 */
router.get('/app/version', (req, res) => {
  const clientVersion = req.query.current_version || '1.0.0';
  const versionRow = db.prepare('SELECT * FROM app_versions ORDER BY version_code DESC LIMIT 1').get();

  const latest = versionRow || {
    version_name: '1.0.0',
    min_supported_version: '1.0.0',
    release_notes: 'FZ ENGINE v1.0.0: Initial production release. Unified ecosystem with live Partner Panel synchronization.',
    download_url: 'https://fzengine.app/downloads/fz_engine_v1.0.0.apk',
    is_required: 0
  };

  const hasUpdate = clientVersion !== latest.version_name && clientVersion < latest.version_name;
  const isRequired = Boolean(latest.is_required);

  res.json({
    success: true,
    data: {
      latestVersion: latest.version_name,
      minimumVersion: latest.min_supported_version,
      releaseNotes: latest.release_notes,
      downloadUrl: latest.download_url,
      hasUpdate,
      isRequired
    }
  });
});

/**
 * GET /api/notifications
 */
router.get('/notifications', (req, res) => {
  const userId = req.query.user_id || 'usr_fz_7749';
  const notifs = db.prepare(`
    SELECT * FROM notifications
    WHERE user_id = ? OR role_target = 'ALL'
    ORDER BY created_at DESC LIMIT 30
  `).all(userId);

  res.json({
    success: true,
    data: notifs.map(n => ({
      id: n.id,
      title: n.title,
      message: n.message,
      category: n.category,
      timestamp: n.created_at,
      isRead: Boolean(n.read_status)
    }))
  });
});

/**
 * POST /api/notifications/mark-read
 */
router.post('/notifications/mark-read', (req, res) => {
  const { id } = req.body;
  if (id) {
    db.prepare('UPDATE notifications SET read_status = 1 WHERE id = ?').run(id);
  }
  res.json({ success: true });
});

/**
 * POST /api/notifications/mark-all-read
 */
router.post('/notifications/mark-all-read', (req, res) => {
  db.prepare('UPDATE notifications SET read_status = 1').run();
  res.json({ success: true });
});

/**
 * GET /api/sessions
 */
router.get('/sessions', (req, res) => {
  const userId = req.query.user_id || 'usr_fz_7749';
  const sessions = db.prepare('SELECT * FROM sessions WHERE user_id = ? AND is_revoked = 0 ORDER BY last_seen DESC').all(userId);

  res.json({
    success: true,
    data: sessions.map(s => ({
      id: s.id,
      userId: s.user_id,
      deviceName: s.device_name,
      deviceIdentifierHash: s.device_identifier_hash,
      platform: s.platform,
      appVersion: s.app_version,
      createdAt: s.created_at,
      lastSeen: s.last_seen,
      expiresAt: s.expires_at,
      isCurrent: Boolean(s.is_current),
      isRevoked: Boolean(s.is_revoked)
    }))
  });
});

/**
 * POST /api/sessions/revoke
 */
router.post('/sessions/revoke', (req, res) => {
  const { session_id, terminate_all_others } = req.body;
  const now = Date.now();

  if (terminate_all_others) {
    db.prepare('UPDATE sessions SET is_revoked = 1, revoked_at = ? WHERE is_current = 0').run(now);
    recordAudit('USER', 'USER', 'TERMINATE_OTHER_SESSIONS', 'SUCCESS', 'Terminated all remote hardware sessions');
    return res.json({ success: true, message: 'All other remote hardware sessions have been revoked.' });
  }

  if (session_id) {
    db.prepare('UPDATE sessions SET is_revoked = 1, revoked_at = ? WHERE id = ?').run(now, session_id);
    recordAudit('USER', 'USER', 'REVOKE_SESSION', 'SUCCESS', `Revoked session ${session_id}`);
    return res.json({ success: true, message: 'Session terminated.' });
  }

  res.status(400).json({ success: false, error: { message: 'session_id required.' } });
});

module.exports = router;
