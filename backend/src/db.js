const { DatabaseSync } = require('node:sqlite');
const crypto = require('node:crypto');
const path = require('node:path');
const fs = require('node:fs');

const DATA_DIR = path.join(__dirname, '..', 'data');
if (!fs.existsSync(DATA_DIR)) {
  fs.mkdirSync(DATA_DIR, { recursive: true });
}

const DB_PATH = process.env.DATABASE_FILE || path.join(DATA_DIR, 'fz_engine.db');
const db = new DatabaseSync(DB_PATH);

// Initialize Tables
db.exec(`
  PRAGMA journal_mode = WAL;

  CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    display_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'USER',
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at INTEGER NOT NULL,
    last_login INTEGER NOT NULL
  );

  CREATE TABLE IF NOT EXISTS sellers (
    id TEXT PRIMARY KEY,
    user_id TEXT,
    display_name TEXT NOT NULL,
    country TEXT NOT NULL,
    country_code TEXT NOT NULL,
    avatar_tag TEXT NOT NULL,
    verification_status INTEGER NOT NULL DEFAULT 1,
    status TEXT NOT NULL DEFAULT 'VERIFIED',
    total_sales INTEGER NOT NULL DEFAULT 0,
    amount_sold_usd REAL NOT NULL DEFAULT 0.0,
    published_contact_method TEXT NOT NULL,
    rating REAL NOT NULL DEFAULT 4.9,
    territory TEXT NOT NULL DEFAULT 'Global',
    wallet_balance_usd REAL NOT NULL DEFAULT 0.0,
    created_at INTEGER NOT NULL
  );

  CREATE TABLE IF NOT EXISTS licenses (
    id TEXT PRIMARY KEY,
    license_key TEXT,
    license_key_hash TEXT UNIQUE NOT NULL,
    masked_key TEXT NOT NULL,
    plan TEXT NOT NULL,
    duration TEXT NOT NULL,
    duration_days REAL NOT NULL,
    status TEXT NOT NULL DEFAULT 'UNUSED',
    seller_id TEXT NOT NULL,
    user_id TEXT,
    device_binding TEXT,
    created_at INTEGER NOT NULL,
    activated_at INTEGER,
    expires_at INTEGER,
    activation_count INTEGER NOT NULL DEFAULT 0,
    max_activations INTEGER NOT NULL DEFAULT 1,
    revoked_at INTEGER,
    prefix TEXT NOT NULL DEFAULT 'FZ-'
  );

  CREATE TABLE IF NOT EXISTS sessions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    license_id TEXT,
    device_name TEXT NOT NULL,
    device_identifier_hash TEXT NOT NULL,
    platform TEXT NOT NULL,
    app_version TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    last_seen INTEGER NOT NULL,
    expires_at INTEGER NOT NULL,
    is_current INTEGER NOT NULL DEFAULT 1,
    is_revoked INTEGER NOT NULL DEFAULT 0,
    revoked_at INTEGER
  );

  CREATE TABLE IF NOT EXISTS sales (
    id TEXT PRIMARY KEY,
    seller_id TEXT NOT NULL,
    license_id TEXT NOT NULL,
    plan TEXT NOT NULL,
    amount REAL NOT NULL,
    currency TEXT NOT NULL DEFAULT 'USD',
    country TEXT NOT NULL DEFAULT 'US',
    status TEXT NOT NULL DEFAULT 'COMPLETED',
    created_at INTEGER NOT NULL
  );

  CREATE TABLE IF NOT EXISTS notifications (
    id TEXT PRIMARY KEY,
    user_id TEXT,
    role_target TEXT NOT NULL DEFAULT 'ALL',
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    category TEXT NOT NULL,
    read_status INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL
  );

  CREATE TABLE IF NOT EXISTS audit_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    actor_id TEXT NOT NULL,
    actor_role TEXT NOT NULL,
    action TEXT NOT NULL,
    result TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    metadata TEXT
  );

  CREATE TABLE IF NOT EXISTS app_versions (
    version_code INTEGER PRIMARY KEY,
    version_name TEXT NOT NULL,
    min_supported_version TEXT NOT NULL,
    release_notes TEXT NOT NULL,
    download_url TEXT NOT NULL,
    is_required INTEGER NOT NULL DEFAULT 0,
    released_at INTEGER NOT NULL
  );

  CREATE TABLE IF NOT EXISTS system_config (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at INTEGER NOT NULL
  );
`);

function hashKey(key) {
  return crypto.createHash('sha256').update(key.trim().toUpperCase()).digest('hex');
}

function maskKey(key) {
  const clean = key.trim().toUpperCase();
  if (clean.length <= 8) return clean;
  return clean.slice(0, 4) + '••••••••' + clean.slice(-4);
}

function recordAudit(actorId, actorRole, action, result, metadata = '') {
  try {
    const stmt = db.prepare(`
      INSERT INTO audit_logs (actor_id, actor_role, action, result, timestamp, metadata)
      VALUES (?, ?, ?, ?, ?, ?)
    `);
    stmt.run(actorId, actorRole, action, result, Date.now(), typeof metadata === 'object' ? JSON.stringify(metadata) : String(metadata));
  } catch (err) {
    console.error('Audit log write error:', err);
  }
}

// Seed initial data if empty
function seedDatabase() {
  const userCount = db.prepare('SELECT COUNT(*) as count FROM users').get().count;
  if (userCount === 0) {
    const now = Date.now();

    // Default users
    const users = [
      { id: 'usr_owner_01', display_name: 'FZ Core Owner', email: 'owner@fzengine.app', role: 'OWNER', pass: 'fz_owner_2026' },
      { id: 'usr_dev_01', display_name: 'Lead Engine Dev', email: 'dev@fzengine.app', role: 'DEVELOPER', pass: 'fz_dev_2026' },
      { id: 'usr_apex_01', display_name: 'Apex Gaming Hub (Seller)', email: 'apex@sellers.fzengine.app', role: 'SELLER', pass: 'apex_seller_pass' },
      { id: 'usr_nova_02', display_name: 'Nova Velocity (Seller)', email: 'nova@sellers.fzengine.app', role: 'SELLER', pass: 'nova_seller_pass' },
      { id: 'usr_support_01', display_name: 'Global Support Desk', email: 'support@fzengine.app', role: 'SUPPORT', pass: 'support_fz_pass' },
      { id: 'usr_fz_7749', display_name: 'Verified Gamer Terminal', email: 'player7749@fzengine.app', role: 'USER', pass: 'player_pass' }
    ];

    const insertUser = db.prepare(`
      INSERT INTO users (id, display_name, email, password_hash, role, status, created_at, last_login)
      VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?, ?)
    `);

    for (const u of users) {
      const passHash = crypto.createHash('sha256').update(u.pass).digest('hex');
      insertUser.run(u.id, u.display_name, u.email, passHash, u.role, now, now);
    }

    // Default Sellers
    const sellers = [
      {
        id: 'sel_apex_01',
        user_id: 'usr_apex_01',
        display_name: 'Apex Gaming Hub',
        country: 'United States',
        country_code: 'US',
        avatar_tag: 'AP',
        verification_status: 1,
        status: 'VERIFIED',
        total_sales: 4820,
        amount_sold_usd: 43380.0,
        published_contact_method: '@ApexEngineHub (Telegram)',
        rating: 4.95,
        territory: 'North America',
        wallet_balance_usd: 8676.0,
        created_at: now - 86400000 * 180
      },
      {
        id: 'sel_pulse_02',
        user_id: 'usr_nova_02',
        display_name: 'Nova Velocity Core',
        country: 'India',
        country_code: 'IN',
        avatar_tag: 'NV',
        verification_status: 1,
        status: 'VERIFIED',
        total_sales: 3915,
        amount_sold_usd: 31320.0,
        published_contact_method: '+91 98210 44321 (WhatsApp)',
        rating: 4.91,
        territory: 'South Asia',
        wallet_balance_usd: 6264.0,
        created_at: now - 86400000 * 120
      },
      {
        id: 'sel_cyber_03',
        user_id: null,
        display_name: 'CyberGate License Desk',
        country: 'United Kingdom',
        country_code: 'GB',
        avatar_tag: 'CG',
        verification_status: 1,
        status: 'VERIFIED',
        total_sales: 2780,
        amount_sold_usd: 25020.0,
        published_contact_method: 'support@cybergate-licenses.com',
        rating: 4.88,
        territory: 'Europe',
        wallet_balance_usd: 5004.0,
        created_at: now - 86400000 * 90
      },
      {
        id: 'sel_brazil_04',
        user_id: null,
        display_name: 'Titanium Keys LatAm',
        country: 'Brazil',
        country_code: 'BR',
        avatar_tag: 'TK',
        verification_status: 1,
        status: 'VERIFIED',
        total_sales: 2150,
        amount_sold_usd: 17200.0,
        published_contact_method: '@TitaniumLatam (Telegram)',
        rating: 4.84,
        territory: 'Latin America',
        wallet_balance_usd: 3440.0,
        created_at: now - 86400000 * 75
      },
      {
        id: 'sel_indo_05',
        user_id: null,
        display_name: 'Garuda Digital Pass',
        country: 'Indonesia',
        country_code: 'ID',
        avatar_tag: 'GD',
        verification_status: 1,
        status: 'VERIFIED',
        total_sales: 1890,
        amount_sold_usd: 13230.0,
        published_contact_method: 't.me/garudapass_official',
        rating: 4.79,
        territory: 'Southeast Asia',
        wallet_balance_usd: 2646.0,
        created_at: now - 86400000 * 60
      },
      {
        id: 'sel_berlin_06',
        user_id: null,
        display_name: 'Krypton Systems EU',
        country: 'Germany',
        country_code: 'DE',
        avatar_tag: 'KS',
        verification_status: 1,
        status: 'VERIFIED',
        total_sales: 1420,
        amount_sold_usd: 12780.0,
        published_contact_method: 'desk@kryptonsystems.de',
        rating: 4.82,
        territory: 'Europe',
        wallet_balance_usd: 2556.0,
        created_at: now - 86400000 * 45
      }
    ];

    const insertSeller = db.prepare(`
      INSERT INTO sellers (id, user_id, display_name, country, country_code, avatar_tag, verification_status, status, total_sales, amount_sold_usd, published_contact_method, rating, territory, wallet_balance_usd, created_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);

    for (const s of sellers) {
      insertSeller.run(
        s.id, s.user_id, s.display_name, s.country, s.country_code, s.avatar_tag,
        s.verification_status, s.status, s.total_sales, s.amount_sold_usd,
        s.published_contact_method, s.rating, s.territory, s.wallet_balance_usd, s.created_at
      );
    }

    // Default Seed Licenses
    const seedLicenses = [
      {
        key: 'FZ-PRO30-APEX-8841',
        plan: 'Pro Engine Pass (30 Days)',
        duration: '30 Days',
        duration_days: 30,
        seller_id: 'sel_apex_01',
        status: 'UNUSED'
      },
      {
        key: 'FZ-PRO90-NOVA-9923',
        plan: 'Pro Engine Pass (Quarterly)',
        duration: '3 Months',
        duration_days: 90,
        seller_id: 'sel_pulse_02',
        status: 'UNUSED'
      },
      {
        key: 'FZ-YEAR-APEX-1102',
        plan: 'Pro Engine Pass (Annual)',
        duration: '1 Year',
        duration_days: 365,
        seller_id: 'sel_apex_01',
        status: 'UNUSED'
      },
      {
        key: 'FZ-7DAY-TRIAL-4412',
        plan: 'Trial Engine Pass (7 Days)',
        duration: '7 Days',
        duration_days: 7,
        seller_id: 'sel_cyber_03',
        status: 'UNUSED'
      },
      {
        key: 'FZ-ACTIVE-DEMO-2026',
        plan: 'Pro Engine Pass (30 Days)',
        duration: '30 Days',
        duration_days: 30,
        seller_id: 'sel_apex_01',
        status: 'ACTIVE',
        activated_at: now - 86400000 * 5,
        expires_at: now + 86400000 * 25,
        user_id: 'usr_fz_7749',
        device_binding: 'Samsung-SM-X710'
      },
      {
        key: 'FZ-EXPIRED-TEST-KEY',
        plan: 'Trial Engine Pass (7 Days)',
        duration: '7 Days',
        duration_days: 7,
        seller_id: 'sel_apex_01',
        status: 'EXPIRED',
        activated_at: now - 86400000 * 14,
        expires_at: now - 86400000 * 7,
        user_id: 'usr_fz_7749',
        device_binding: 'Google-Pixel-8'
      },
      {
        key: 'FZ-REVOKED-TEST-KEY',
        plan: 'Pro Engine Pass (30 Days)',
        duration: '30 Days',
        duration_days: 30,
        seller_id: 'sel_apex_01',
        status: 'REVOKED',
        revoked_at: now - 3600000 * 2,
        user_id: 'usr_fz_7749'
      },
      {
        key: 'FZ-BANNED-POLICY-KEY',
        plan: 'Pro Engine Pass (Annual)',
        duration: '1 Year',
        duration_days: 365,
        seller_id: 'sel_pulse_02',
        status: 'BANNED',
        revoked_at: now - 3600000 * 6,
        user_id: 'usr_fz_7749'
      }
    ];

    const insertLicense = db.prepare(`
      INSERT INTO licenses (
        id, license_key, license_key_hash, masked_key, plan, duration, duration_days,
        status, seller_id, user_id, device_binding, created_at, activated_at, expires_at,
        activation_count, max_activations, revoked_at, prefix
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);

    for (let i = 0; i < seedLicenses.length; i++) {
      const lic = seedLicenses[i];
      const licId = 'lic_' + crypto.randomUUID().slice(0, 8);
      const kHash = hashKey(lic.key);
      const mKey = maskKey(lic.key);
      insertLicense.run(
        licId,
        lic.key,
        kHash,
        mKey,
        lic.plan,
        lic.duration,
        lic.duration_days,
        lic.status,
        lic.seller_id,
        lic.user_id || null,
        lic.device_binding || null,
        now - 86400000 * 10,
        lic.activated_at || null,
        lic.expires_at || null,
        lic.status === 'ACTIVE' ? 1 : 0,
        1,
        lic.revoked_at || null,
        'FZ-'
      );
    }

    // Seed Recent Sales for Authoritative Rankings (Today, This Week, This Month, All Time)
    const insertSale = db.prepare(`
      INSERT INTO sales (id, seller_id, license_id, plan, amount, currency, country, status, created_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);

    // Apex Sales
    for (let i = 0; i < 28; i++) {
      insertSale.run(
        'sal_' + crypto.randomUUID().slice(0, 8),
        'sel_apex_01',
        'lic_seed_' + i,
        'Pro Engine Pass',
        14.99,
        'USD',
        'US',
        'COMPLETED',
        now - (i * 3600000 * 4) // distributed across today, this week, this month
      );
    }

    // Nova Sales
    for (let i = 0; i < 22; i++) {
      insertSale.run(
        'sal_' + crypto.randomUUID().slice(0, 8),
        'sel_pulse_02',
        'lic_seed_' + (i + 50),
        'Pro Engine Pass',
        12.99,
        'USD',
        'IN',
        'COMPLETED',
        now - (i * 3600000 * 6)
      );
    }

    // CyberGate Sales
    for (let i = 0; i < 15; i++) {
      insertSale.run(
        'sal_' + crypto.randomUUID().slice(0, 8),
        'sel_cyber_03',
        'lic_seed_' + (i + 100),
        'Pro Engine Pass',
        15.50,
        'USD',
        'GB',
        'COMPLETED',
        now - (i * 3600000 * 10)
      );
    }

    // Seed Initial App Version
    db.prepare(`
      INSERT INTO app_versions (version_code, version_name, min_supported_version, release_notes, download_url, is_required, released_at)
      VALUES (1001, '1.0.0', '1.0.0', 'Production Release v1.0.0. Unified FZ ENGINE ecosystem with live Partner Panel synchronization, cryptographically signed licenses, and zero-trust session checks.', 'https://fzengine.app/downloads/fz_engine_v1.0.0.apk', 0, ?)
    `).run(now);

    // Seed System Config
    db.prepare(`
      INSERT INTO system_config (key, value, updated_at) VALUES ('server_health', 'ONLINE', ?)
    `).run(now);
    db.prepare(`
      INSERT INTO system_config (key, value, updated_at) VALUES ('maintenance_notice', 'System operating normally at all global edge locations.', ?)
    `).run(now);
    db.prepare(`
      INSERT INTO system_config (key, value, updated_at) VALUES ('latency_ms', '42', ?)
    `).run(now);

    // Initial Notifications
    const insertNotif = db.prepare(`
      INSERT INTO notifications (id, user_id, role_target, title, message, category, read_status, created_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    `);

    insertNotif.run(
      'notif_sys_01',
      null,
      'ALL',
      'FZ Engine Ecosystem Live',
      'Central backend synchronization active for Android client and Partner Panel.',
      'ANNOUNCEMENT',
      0,
      now - 3600000 * 2
    );

    insertNotif.run(
      'notif_sys_02',
      'usr_apex_01',
      'SELLER',
      'Partner Tier Verified',
      'Your seller account has been elevated to Verified Tier-1 distributor status.',
      'SUPPORT_RESPONSE',
      0,
      now - 86400000
    );

    // Initial Audit Log
    recordAudit('SYSTEM', 'SYSTEM', 'SYSTEM_INITIALIZATION', 'SUCCESS', 'Database schema migrated and authoritative seed records populated');
  }
}

seedDatabase();

module.exports = {
  db,
  hashKey,
  maskKey,
  recordAudit
};
