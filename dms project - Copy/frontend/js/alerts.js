const BASE = '/disaster';

function openBroadcast()  { document.getElementById('broadcast-modal').classList.remove('hidden'); }
function closeBroadcast() { document.getElementById('broadcast-modal').classList.add('hidden'); }

const LEVEL_CLASS = { INFO:'info', WARNING:'warning', DANGER:'danger', EVACUATION:'evacuation' };

async function loadAlerts() {
  const grid   = document.getElementById('alerts-grid');
  const tbody  = document.getElementById('alert-history-body');

  try {
    const res  = await fetch(`${BASE}/AlertServlet`);
    const data = await res.json();
    renderActiveAlerts(grid, data.filter(a => a.status === 'ACTIVE'));
    renderAlertHistory(tbody, data);
  } catch (e) {
    const mock = [
      { id:1, title:'Cyclone Fengal Approaching', level:'EVACUATION', region:'Chennai Coastal', message:'Immediate evacuation ordered for zones 1-5. Proceed to nearest shelter.', status:'ACTIVE', sentAt:'2025-03-23 06:00', incidentId:3 },
      { id:2, title:'Flash Flood Warning', level:'DANGER', region:'Velachery & Adyar', message:'Heavy rainfall expected. Avoid low-lying areas and underpasses.', status:'ACTIVE', sentAt:'2025-03-23 08:30', incidentId:1 },
      { id:3, title:'Industrial Fire Advisory', level:'WARNING', region:'Guindy, Saidapet', message:'Stay indoors. Keep windows closed. Avoid Guindy Industrial Area.', status:'ACTIVE', sentAt:'2025-03-23 09:45', incidentId:2 },
      { id:4, title:'Relief Camp Open — T.Nagar', level:'INFO', region:'T.Nagar, Nungambakkam', message:'Relief camp operational at P.S. High School. Food and shelter available.', status:'RESOLVED', sentAt:'2025-03-22 14:00', incidentId:null },
    ];
    renderActiveAlerts(grid, mock.filter(a => a.status === 'ACTIVE'));
    renderAlertHistory(tbody, mock);
  }
}

function renderActiveAlerts(grid, alerts) {
  if (!alerts.length) {
    grid.innerHTML = `<div style="text-align:center;padding:2rem;opacity:.5;grid-column:1/-1">No active alerts.</div>`;
    return;
  }
  grid.innerHTML = alerts.map((a, i) => `
    <div class="alert-card ${LEVEL_CLASS[a.level]}" style="animation-delay:${i * .1}s">
      <div class="alert-card-level">${a.level} ALERT</div>
      <div class="alert-card-title">${a.title}</div>
      <div class="alert-card-msg">${a.message}</div>
      <div class="alert-card-meta">
        <span>📍 ${a.region}</span>
        <span>🕐 ${a.sentAt}</span>
        ${a.incidentId ? `<span>🔗 #${a.incidentId}</span>` : ''}
      </div>
    </div>`).join('');
}

function renderAlertHistory(tbody, alerts) {
  tbody.innerHTML = alerts.map(a => `
    <tr>
      <td class="mono">#${a.id}</td>
      <td>${a.title}</td>
      <td><span class="badge badge-${a.level.toLowerCase()}">${a.level}</span></td>
      <td>${a.region}</td>
      <td class="mono">${a.sentAt}</td>
      <td><span class="badge badge-${a.status.toLowerCase()}">${a.status}</span></td>
      <td>
        ${a.status === 'ACTIVE'
          ? `<button class="btn-xs" onclick="resolveAlert(${a.id})">✓ Resolve</button>`
          : '—'
        }
      </td>
    </tr>`).join('');
}

async function submitAlert() {
  const title     = document.getElementById('alert-title').value.trim();
  const level     = document.getElementById('alert-level').value;
  const region    = document.getElementById('alert-region').value.trim();
  const message   = document.getElementById('alert-message').value.trim();
  const incidentId= document.getElementById('alert-incident-id').value || null;
  const expiresAt = document.getElementById('alert-expires').value || null;

  if (!title || !region || !message) {
    alert('Please fill title, region, and message.');
    return;
  }

  try {
    await fetch(`${BASE}/AlertServlet`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, level, region, message, incidentId, expiresAt })
    });
  } catch (e) {}

  closeBroadcast();
  loadAlerts();
}

async function resolveAlert(id) {
  try {
    await fetch(`${BASE}/AlertServlet`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id, status: 'RESOLVED' })
    });
  } catch (e) {}
  loadAlerts();
}

loadAlerts();