const BASE = '/disaster';
let map;
let markers = [];

// Initialize Map
function initMap() {
    map = L.map('map').setView([13.0827, 80.2707], 11);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);
}

// Load Shelters
async function loadShelters() {
    const tbody = document.getElementById('shelters-table-body');
    try {
        const res = await fetch(`${BASE}/ShelterServlet`);
        const data = await res.json();
        
        requestAnimationFrame(() => {
            renderShelters(tbody, data);
            updateMapMarkers(data);
        });
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:var(--accent-red)">Error connecting to server.</td></tr>`;
    }
}

function renderShelters(tbody, data) {
    if (!data || !data.length) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;opacity:.5">No shelters registered.</td></tr>`;
        return;
    }

    tbody.innerHTML = data.map(s => {
        const occupancyRate = (s.occupancy / s.capacity) * 100;
        let badgeClass = 'badge-available';
        let statusText = 'ACTIVE';

        if (occupancyRate >= 95) {
            badgeClass = 'badge-high';
            statusText = 'FULL';
        } else if (occupancyRate >= 75) {
            badgeClass = 'badge-warning';
            statusText = 'NEARLY FULL';
        }

        return `
            <tr>
                <td style="font-weight:600">${s.name}</td>
                <td>${s.location}</td>
                <td class="mono">${s.capacity}</td>
                <td class="mono">${s.occupancy} (${Math.round(occupancyRate)}%)</td>
                <td><span class="badge ${badgeClass}">${statusText}</span></td>
            </tr>
        `;
    }).join('');
}

function updateMapMarkers(data) {
    markers.forEach(m => map.removeLayer(m));
    markers = [];

    data.forEach(s => {
        if (s.latitude && s.longitude) {
            const marker = L.marker([s.latitude, s.longitude]).addTo(map);
            const occupancyRate = Math.round((s.occupancy / s.capacity) * 100);
            
            marker.bindPopup(`
                <div style="font-family: 'IBM Plex Sans', sans-serif; min-width: 140px;">
                    <b style="color: #00e676; font-size: 14px;">${s.name}</b><br>
                    <small>${s.location}</small><hr style="border:0; border-top:1px solid #333; margin:8px 0">
                    <b>Capacity:</b> ${s.capacity}<br>
                    <b>Occupancy:</b> ${s.occupancy} (${occupancyRate}%)
                </div>
            `);
            markers.push(marker);
        }
    });

    if (markers.length > 0) {
        setTimeout(() => {
            const group = new L.featureGroup(markers);
            map.fitBounds(group.getBounds().pad(0.2));
        }, 100);
    }
}

// Modal
function showAddModal() {
    document.getElementById('addModal').classList.remove('hidden');
    setTimeout(() => map.invalidateSize(), 150);
}

function hideAddModal() {
    document.getElementById('addModal').classList.add('hidden');
}

// Form
document.getElementById('add-shelter-form').onsubmit = async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button[type="submit"]');
    const originalText = btn.innerHTML;
    
    btn.innerHTML = 'SAVING...';
    btn.disabled = true;

    const formData = new FormData(e.target);
    const payload = Object.fromEntries(formData.entries());

    try {
        const res = await fetch(`${BASE}/ShelterServlet`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            hideAddModal();
            e.target.reset();
            await loadShelters();
        } else {
            alert('Failed to save shelter.');
        }
    } catch (err) {
        alert('Connection error.');
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
};

// Init
initMap();
loadShelters();
