const BASE = '/disaster';

// Live clock
function updateClock() {
  const el = document.getElementById('live-time');
  if (el) el.textContent = new Date().toLocaleString('en-IN', {
    weekday: 'short', year: 'numeric', month: 'short',
    day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit'
  });
}
setInterval(updateClock, 1000);
updateClock();

// Stat cards
async function loadStats() {
  try {
    const res = await fetch(`${BASE}/DashboardServlet`);
    const data = await res.json();
    document.getElementById('stat-incidents').textContent = data.activeIncidents;
    document.getElementById('stat-resources').textContent = data.resourcesDeployed;
    document.getElementById('stat-alerts').textContent = data.alertsSent;
    document.getElementById('stat-resolved').textContent = data.resolvedToday;
    document.getElementById('alert-count').textContent = data.activeAlerts;
  } catch (e) {
    // mock
    document.getElementById('stat-incidents').textContent = '14';
    document.getElementById('stat-resources').textContent = '38';
    document.getElementById('stat-alerts').textContent = '27';
    document.getElementById('stat-resolved').textContent = '6';
  }
}

// Incident Timeline Chart
function renderIncidentChart() {
  const ctx = document.getElementById('incidentChart').getContext('2d');
  new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
      datasets: [
        {
          label: 'Reported',
          data: [4, 7, 3, 9, 5, 12, 6],
          backgroundColor: 'rgba(255, 45, 45, 0.7)',
          borderColor: '#ff2d2d',
          borderWidth: 1,
        },
        {
          label: 'Resolved',
          data: [2, 5, 3, 6, 4, 8, 4],
          backgroundColor: 'rgba(0, 230, 118, 0.5)',
          borderColor: '#00e676',
          borderWidth: 1,
        }
      ]
    },
    options: {
      responsive: true,
      plugins: { legend: { labels: { color: '#7b8299', font: { family: 'IBM Plex Mono', size: 11 } } } },
      scales: {
        x: { ticks: { color: '#7b8299', font: { family: 'IBM Plex Mono' } }, grid: { color: '#1f2635' } },
        y: { ticks: { color: '#7b8299', font: { family: 'IBM Plex Mono' } }, grid: { color: '#1f2635' } }
      }
    }
  });
}

// Disaster Type Donut Chart
function renderTypeChart() {
  const ctx = document.getElementById('typeChart').getContext('2d');
  new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['Flood', 'Fire', 'Earthquake', 'Cyclone', 'Landslide', 'Other'],
      datasets: [{
        data: [32, 21, 15, 18, 8, 6],
        backgroundColor: ['#2979ff','#ff2d2d','#ffd426','#ff7a1a','#00e676','#7b8299'],
        borderColor: '#111318',
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: {
          position: 'bottom',
          labels: { color: '#7b8299', font: { family: 'IBM Plex Mono', size: 10 }, padding: 8 }
        }
      }
    }
  });
}

// Incident table
async function loadIncidents() {
  const tbody = document.getElementById('incident-table-body');
  try {
    const res = await fetch(`${BASE}/IncidentServlet?limit=10`);
    const data = await res.json();
    renderIncidentRows(tbody, data);
  } catch (e) {
    const mock = [
      { id: 1, type: 'FLOOD', location: 'Velachery, Chennai', severity: 'HIGH', status: 'INVESTIGATING', reportedAt: '2025-03-23 08:14' },
      { id: 2, type: 'FIRE', location: 'Guindy Industrial Estate', severity: 'CRITICAL', status: 'ACTIVE', reportedAt: '2025-03-23 09:32' },
      { id: 3, type: 'CYCLONE', location: 'Ennore Port', severity: 'MEDIUM', status: 'ACTIVE', reportedAt: '2025-03-23 11:05' },
      { id: 4, type: 'EARTHQUAKE', location: 'Tambaram', severity: 'LOW', status: 'RESOLVED', reportedAt: '2025-03-22 16:48' },
      { id: 5, type: 'LANDSLIDE', location: 'Nilgiris', severity: 'HIGH', status: 'ACTIVE', reportedAt: '2025-03-23 07:22' },
    ];
    renderIncidentRows(tbody, mock);
  }
}

function renderIncidentRows(tbody, data) {
  if (!data.length) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:2rem;opacity:.5">No incidents found.</td></tr>`;
    return;
  }
  tbody.innerHTML = data.map(r => `
    <tr>
      <td class="mono">#${r.id}</td>
      <td>${r.type}</td>
      <td>${r.location}</td>
      <td><span class="badge badge-${r.severity.toLowerCase()}">${r.severity}</span></td>
      <td><span class="badge badge-${r.status.toLowerCase()}">${r.status}</span></td>
      <td class="mono">${r.reportedAt}</td>
      <td>
        <button class="btn-xs" onclick="updateStatus(${r.id}, 'RESOLVED')">✓ Resolve</button>
      </td>
    </tr>
  `).join('');
}

async function updateStatus(id, status) {
  try {
    await fetch(`${BASE}/IncidentServlet`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id, status })
    });
  } catch (e) {}
  loadIncidents();
}

// Init
loadStats();
renderIncidentChart();
renderTypeChart();
loadIncidents();