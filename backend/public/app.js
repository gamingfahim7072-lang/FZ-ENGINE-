// FZ ENGINE PARTNER & SELLER APP
let currentRole = 'SELLER';
let authToken = '';
let currentRankingPeriod = 'TODAY';
let generatedKeysCache = [];

// Initialize
document.addEventListener('DOMContentLoaded', async () => {
  await loginAsRole(currentRole);
  await loadDashboardData();
  await checkServerHealth();
  startHealthPoller();
});

// Switch role perspective
async function changeRolePerspective(newRole) {
  currentRole = newRole;
  await loginAsRole(newRole);
  await loadDashboardData();
  
  // Show or hide admin tab depending on role
  const adminTab = document.getElementById('adminTabBtn');
  if (adminTab) {
    if (newRole === 'OWNER' || newRole === 'DEVELOPER') {
      adminTab.style.display = 'flex';
    } else {
      adminTab.style.display = 'none';
      if (document.getElementById('tab-admin').classList.contains('active')) {
        switchTab('dashboard');
      }
    }
  }
}

async function loginAsRole(role) {
  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ role_override: role })
    });
    const data = await res.json();
    if (data.success) {
      authToken = data.data.token;
      document.getElementById('partnerName').textContent = data.data.user.display_name;
    }
  } catch (err) {
    console.error('Role login failed:', err);
  }
}

function getAuthHeaders() {
  return {
    'Content-Type': 'application/json',
    ...(authToken ? { 'Authorization': `Bearer ${authToken}` } : {})
  };
}

// TAB SWITCHING
function switchTab(tabId) {
  document.querySelectorAll('.nav-tab').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));

  const tabBtn = document.querySelector(`.nav-tab[data-tab="${tabId}"]`);
  const tabPane = document.getElementById(`tab-${tabId}`);
  if (tabBtn) tabBtn.classList.add('active');
  if (tabPane) tabPane.classList.add('active');

  // Load appropriate data
  if (tabId === 'dashboard') loadDashboardData();
  if (tabId === 'licenses') loadLicensesTable();
  if (tabId === 'rankings') loadRankings();
  if (tabId === 'audit') loadAuditLogs();
}

// DASHBOARD
async function loadDashboardData() {
  try {
    const res = await fetch('/api/partner/dashboard', { headers: getAuthHeaders() });
    const json = await res.json();
    if (!json.success) return;

    const { stats, seller, recentLicenses, recentSales } = json.data;

    document.getElementById('statTotalKeys').textContent = stats.totalKeys;
    document.getElementById('statActiveKeys').textContent = stats.activeKeys;
    document.getElementById('statUnusedKeys').textContent = stats.unusedKeys;
    document.getElementById('statExpiredKeys').textContent = stats.expiredKeys + stats.revokedKeys;
    document.getElementById('statKeysSold').textContent = stats.totalSales;
    document.getElementById('statRevenue').textContent = `$${Number(stats.revenueUsd).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
    document.getElementById('walletAmount').textContent = `$${Number(seller.walletBalanceUsd).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;

    // Render recent licenses
    const licBody = document.getElementById('recentLicensesBody');
    if (recentLicenses.length === 0) {
      licBody.innerHTML = '<tr><td colspan="4" class="text-center">No licenses created yet.</td></tr>';
    } else {
      licBody.innerHTML = recentLicenses.map(l => `
        <tr>
          <td class="font-mono text-purple"><b>${l.maskedKey}</b></td>
          <td>${l.plan}</td>
          <td><span class="status-pill ${l.status.toLowerCase()}">${l.status}</span></td>
          <td class="text-secondary">${l.deviceBinding || 'Unbound'}</td>
        </tr>
      `).join('');
    }

    // Render recent sales
    const salesBody = document.getElementById('recentSalesBody');
    if (recentSales.length === 0) {
      salesBody.innerHTML = '<tr><td colspan="4" class="text-center">No sales recorded yet.</td></tr>';
    } else {
      salesBody.innerHTML = recentSales.map(s => `
        <tr>
          <td>${s.plan}</td>
          <td class="font-mono text-green">+$${Number(s.amount).toFixed(2)}</td>
          <td>${s.country}</td>
          <td class="text-secondary">${new Date(s.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</td>
        </tr>
      `).join('');
    }
  } catch (err) {
    console.error('Failed to load dashboard:', err);
  }
}

// DURATION SELECTOR (STEP 3)
function selectDuration(dur, el) {
  document.querySelectorAll('.duration-pill').forEach(p => p.classList.remove('active'));
  el.classList.add('active');
  document.getElementById('selectedDuration').value = dur;

  const customGroup = document.getElementById('customDaysGroup');
  if (dur === 'Custom') {
    customGroup.classList.remove('hidden');
  } else {
    customGroup.classList.add('hidden');
  }
}

// GENERATE KEYS
async function handleGenerateKeys(e) {
  e.preventDefault();
  const btn = document.getElementById('btnGenSubmit');
  btn.disabled = true;
  btn.textContent = 'Generating Secure Keys...';

  const duration = document.getElementById('selectedDuration').value;
  const customDays = document.getElementById('customDaysInput').value;
  const prefix = document.getElementById('keyPrefix').value;
  const quantity = document.getElementById('keyQuantity').value;

  try {
    const res = await fetch('/api/partner/licenses/generate', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({
        duration_option: duration,
        custom_days: customDays,
        prefix,
        quantity
      })
    });

    const json = await res.json();
    if (json.success) {
      generatedKeysCache = json.data.keys;
      displayGeneratedKeys(json.data.keys);
      loadDashboardData();
    } else {
      alert('Generation error: ' + (json.error ? json.error.message : 'Unknown error'));
    }
  } catch (err) {
    alert('Failed to connect to license generation server.');
  } finally {
    btn.disabled = false;
    btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon></svg> Generate Secure License Keys`;
  }
}

function displayGeneratedKeys(keys) {
  const container = document.getElementById('generationResultsCard');
  const list = document.getElementById('generatedKeysList');
  container.classList.remove('hidden');

  list.innerHTML = keys.map(k => `
    <div class="key-item">
      <span><b>${k.licenseKey}</b> &nbsp;<span class="text-secondary">(${k.plan})</span></span>
      <button class="btn btn-outline btn-sm" onclick="copyText('${k.licenseKey}')">Copy</button>
    </div>
  `).join('');
}

function copyText(txt) {
  navigator.clipboard.writeText(txt);
  alert('Copied to clipboard: ' + txt);
}

function copyAllGeneratedKeys() {
  if (generatedKeysCache.length === 0) return;
  const all = generatedKeysCache.map(k => k.licenseKey).join('\n');
  navigator.clipboard.writeText(all);
  alert(`Copied ${generatedKeysCache.length} keys to clipboard!`);
}

function downloadKeysTxt() {
  if (generatedKeysCache.length === 0) return;
  const all = generatedKeysCache.map(k => `${k.licenseKey} | ${k.plan} | Issued: ${new Date(k.createdAt).toISOString()}`).join('\n');
  const blob = new Blob([all], { type: 'text/plain' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = `FZ_ENGINE_KEYS_${Date.now()}.txt`;
  a.click();
}

// LICENSES TABLE (STEP 5 & 8)
async function loadLicensesTable() {
  const search = document.getElementById('licenseSearchInput').value;
  const status = document.getElementById('licenseStatusFilter').value;

  try {
    const res = await fetch(`/api/partner/licenses?search=${encodeURIComponent(search)}&status=${status}`, {
      headers: getAuthHeaders()
    });
    const json = await res.json();
    const tbody = document.getElementById('licensesTableBody');

    if (!json.success || json.data.length === 0) {
      tbody.innerHTML = '<tr><td colspan="8" class="text-center">No licenses match current filter.</td></tr>';
      return;
    }

    tbody.innerHTML = json.data.map(l => {
      const isRevocable = l.status === 'ACTIVE' || l.status === 'UNUSED';
      return `
        <tr>
          <td class="font-mono text-purple">
            <span title="Click to copy" style="cursor:pointer" onclick="copyText('${l.licenseKey}')">${l.maskedKey}</span>
          </td>
          <td>${l.plan}</td>
          <td>${l.duration}</td>
          <td><span class="status-pill ${l.status.toLowerCase()}">${l.status}</span></td>
          <td class="text-secondary">${l.deviceBinding || '—'}</td>
          <td class="text-secondary">${l.activatedAt ? new Date(l.activatedAt).toLocaleDateString() : '—'}</td>
          <td class="text-secondary">${l.expiresAt ? new Date(l.expiresAt).toLocaleDateString() : '—'}</td>
          <td>
            ${isRevocable ? `<button class="btn btn-danger btn-sm" onclick="revokeLicense('${l.id}', '${l.maskedKey}')">Revoke</button>` : '<span class="text-muted">—</span>'}
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    console.error('Failed to load licenses:', err);
  }
}

let searchDebounce = null;
function handleLicenseSearch() {
  clearTimeout(searchDebounce);
  searchDebounce = setTimeout(() => {
    loadLicensesTable();
  }, 300);
}

// REVOKE LICENSE (STEP 5 REQUIREMENT)
async function revokeLicense(id, maskedKey) {
  if (!confirm(`Are you sure you want to REVOKE license ${maskedKey}?\n\nThis will immediately disconnect any active Android device session.`)) {
    return;
  }

  try {
    const res = await fetch('/api/partner/licenses/revoke', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ license_id: id, reason: 'Revoked via Partner Portal' })
    });
    const json = await res.json();
    if (json.success) {
      alert(json.message);
      loadLicensesTable();
      loadDashboardData();
    } else {
      alert('Revocation error: ' + (json.error ? json.error.message : 'Unknown'));
    }
  } catch (err) {
    alert('Network error while revoking license.');
  }
}

// SELLER RANKINGS (STEP 6 & 7)
function changeRankingPeriod(period, el) {
  document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
  el.classList.add('active');
  currentRankingPeriod = period;
  loadRankings();
}

async function loadRankings() {
  const scope = document.getElementById('rankingScopeFilter').value;
  try {
    const res = await fetch(`/api/sellers/ranking?period=${currentRankingPeriod}&scope=${scope}`);
    const json = await res.json();
    const tbody = document.getElementById('rankingsTableBody');

    if (!json.success || json.data.rankings.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center">No rankings found.</td></tr>';
      return;
    }

    tbody.innerHTML = json.data.rankings.map(s => `
      <tr>
        <td class="font-mono text-purple"><b>#${s.rank}</b></td>
        <td>
          <b>${s.displayName}</b>
          ${s.verificationStatus ? '<span class="badge-sub" style="margin-left:6px">VERIFIED</span>' : ''}
        </td>
        <td>${s.country} (${s.countryCode})</td>
        <td class="font-mono text-green"><b>${s.periodSales}</b></td>
        <td class="font-mono text-cyan">$${Number(s.periodRevenueUsd).toFixed(2)}</td>
        <td class="font-mono text-purple">★ ${Number(s.rating).toFixed(2)}</td>
        <td class="text-secondary">${s.publishedContactMethod}</td>
      </tr>
    `).join('');
  } catch (err) {
    console.error('Failed to load rankings:', err);
  }
}

// AUDIT LOGS
async function loadAuditLogs() {
  try {
    const res = await fetch('/api/admin/audit-logs?limit=40', { headers: getAuthHeaders() });
    const json = await res.json();
    const tbody = document.getElementById('auditTableBody');

    if (!json.success || json.data.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-center">No audit logs found.</td></tr>';
      return;
    }

    tbody.innerHTML = json.data.map(l => `
      <tr>
        <td class="text-secondary">${new Date(l.timestamp).toLocaleTimeString()}</td>
        <td>${l.actor_id}</td>
        <td><span class="status-pill unused">${l.actor_role}</span></td>
        <td class="text-cyan">${l.action}</td>
        <td><span class="status-pill ${l.result === 'SUCCESS' ? 'active' : 'expired'}">${l.result}</span></td>
        <td class="text-secondary">${l.metadata || '—'}</td>
      </tr>
    `).join('');
  } catch (err) {
    console.error('Failed to load audit logs:', err);
  }
}

// SERVER HEALTH MONITOR
async function checkServerHealth() {
  try {
    const start = performance.now();
    const res = await fetch('/api/server/status');
    const latency = Math.round(performance.now() - start);
    const json = await res.json();

    const badge = document.getElementById('serverHealthBadge');
    const txt = document.getElementById('serverHealthText');

    if (json.success) {
      const status = json.data.status;
      badge.className = `health-pill ${status.toLowerCase()}`;
      txt.textContent = `${status} • ${json.data.latencyMs || latency}ms`;
    }
  } catch (err) {
    const badge = document.getElementById('serverHealthBadge');
    const txt = document.getElementById('serverHealthText');
    badge.className = 'health-pill offline';
    txt.textContent = 'OFFLINE';
  }
}

function startHealthPoller() {
  setInterval(checkServerHealth, 10000);
}

// ADMIN SYSTEM CONTROL
async function setSystemStatus(status) {
  try {
    const res = await fetch('/api/admin/system-status', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ status })
    });
    const json = await res.json();
    if (json.success) {
      alert(`System Status successfully updated to: ${status}`);
      checkServerHealth();
    }
  } catch (err) {
    alert('Failed to update system status.');
  }
}

// PROVISION NEW SELLER (STEP 21 TEST 1)
async function handleCreateSeller(e) {
  e.preventDefault();
  const name = document.getElementById('newSellerName').value;
  const country = document.getElementById('newSellerCountry').value;
  const code = document.getElementById('newSellerCode').value;
  const contact = document.getElementById('newSellerContact').value;

  try {
    const res = await fetch('/api/admin/sellers', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({
        display_name: name,
        country,
        country_code: code,
        published_contact_method: contact
      })
    });
    const json = await res.json();
    if (json.success) {
      alert(`Seller "${name}" authorized and provisioned onto the ecosystem!`);
      document.getElementById('newSellerName').value = '';
      document.getElementById('newSellerCountry').value = '';
      document.getElementById('newSellerCode').value = '';
      document.getElementById('newSellerContact').value = '';
      loadRankings();
    } else {
      alert('Error creating seller: ' + (json.error ? json.error.message : 'Unknown'));
    }
  } catch (err) {
    alert('Network error while provisioning seller.');
  }
}

// NOTIFICATIONS
let notifOpen = false;
async function toggleNotifications() {
  const modal = document.getElementById('notifModal');
  notifOpen = !notifOpen;
  if (notifOpen) {
    modal.classList.remove('hidden');
    loadNotificationsList();
  } else {
    modal.classList.add('hidden');
  }
}

async function loadNotificationsList() {
  try {
    const res = await fetch('/api/notifications');
    const json = await res.json();
    const list = document.getElementById('notifList');
    if (!json.success || json.data.length === 0) {
      list.innerHTML = '<p class="text-center text-secondary">No notifications.</p>';
      return;
    }
    list.innerHTML = json.data.map(n => `
      <div class="notif-item">
        <h4>${n.title}</h4>
        <p>${n.message}</p>
        <small class="text-muted">${new Date(n.timestamp).toLocaleTimeString()}</small>
      </div>
    `).join('');
  } catch (err) {
    console.error(err);
  }
}
