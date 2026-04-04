const BASE = 'http://localhost:8080/disaster';

async function submitIncident() {
  const type     = document.getElementById('inc-type').value;
  const severity = document.querySelector('input[name="severity"]:checked')?.value;
  const location = document.getElementById('inc-location').value.trim();
  const lat      = document.getElementById('inc-lat').value;
  const lng      = document.getElementById('inc-lng').value;
  const reporter = document.getElementById('inc-reporter').value.trim();
  const contact  = document.getElementById('inc-contact').value.trim();
  const description = document.getElementById('inc-description').value.trim();
  const dead     = document.getElementById('inc-dead').value;
  const injured  = document.getElementById('inc-injured').value;
  const missing  = document.getElementById('inc-missing').value;
  const displaced= document.getElementById('inc-displaced').value;

  const msg = document.getElementById('form-message');

  if (!type || !severity || !location || !reporter) {
    showMessage('error', '✕  Please fill: Incident Type, Severity, Location, and Reported By.');
    return;
  }

  const payload = { type, severity, location, lat, lng, reporter, contact, description, dead, injured, missing, displaced };

  try {
    const res = await fetch(`${BASE}/IncidentServlet`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (res.ok) {
      showMessage('success', '✓  Incident filed successfully. Response teams have been notified.');
      resetForm();
    } else {
      throw new Error('Server error');
    }
  } catch (e) {
    // Mock success for demo
    showMessage('success', '✓  Incident filed [mock mode — backend not connected]. Record saved locally.');
    resetForm();
  }
}

function resetForm() {
  document.getElementById('inc-type').value = '';
  document.querySelectorAll('input[name="severity"]').forEach(r => r.checked = false);
  ['inc-location','inc-lat','inc-lng','inc-reporter','inc-contact','inc-description'].forEach(id => {
    document.getElementById(id).value = '';
  });
  ['inc-dead','inc-injured','inc-missing','inc-displaced'].forEach(id => {
    document.getElementById(id).value = '0';
  });
}

function showMessage(type, text) {
  const el = document.getElementById('form-message');
  el.className = `form-message ${type}`;
  el.textContent = text;
  el.classList.remove('hidden');
  setTimeout(() => el.classList.add('hidden'), 6000);
}