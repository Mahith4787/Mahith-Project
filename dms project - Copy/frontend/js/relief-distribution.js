const BASE = '/disaster';
let map;
let markers = [];

// Initialize Map
function initMap() {
    map = L.map('map').setView([13.0827, 80.2707], 11);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
}

// Load Relief Data
async function loadReliefData() {
    const tableBody = document.getElementById('distribution-table-body');
    const donationsBody = document.getElementById('donations-table-body');
    const centerSelect = document.getElementById('donate-center-select');

    try {
        const res = await fetch(`${BASE}/ReliefServlet`);
        const { centers, donations } = await res.json();
        
        requestAnimationFrame(() => {
            renderCenters(tableBody, centers);
            renderDonations(donationsBody, donations, centers);
            updateMapMarkers(centers);
            populateCenterSelect(centerSelect, centers);
        });
    } catch (e) {
        tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:var(--accent-red)">Connection error.</td></tr>`;
    }
}

function renderCenters(tbody, centers) {
    if (!centers || !centers.length) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;opacity:.5">No supply centers registered.</td></tr>`;
        return;
    }

    tbody.innerHTML = centers.map(c => `
        <tr>
            <td style="font-weight:600">${c.name}</td>
            <td>${c.location}</td>
            <td>
                <div class="resource-list">
                    <li><span>Food Packets:</span> <span class="resource-val">${c.items.food_packets}</span></li>
                    <li><span>Water Cases:</span> <span class="resource-val">${c.items.water_liters}</span></li>
                    <li><span>Clothes:</span> <span class="resource-val">${c.items.clothing_kits}</span></li>
                </div>
            </td>
            <td><span class="badge ${c.status === 'LOW_STOCK' ? 'badge-high' : 'badge-available'}">${c.status}</span></td>
            <td><button class="btn-xs" onclick="window.location='tel:100'">📞 CONTACT HUB</button></td>
        </tr>
    `).join('');
}

function renderDonations(tbody, donations, centers) {
    if (!donations || !donations.length) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:1.5rem;opacity:.3">No recent activity.</td></tr>`;
        return;
    }

    tbody.innerHTML = donations.map(d => {
        const center = centers.find(c => c.id === d.center_id)?.name || 'Unknown Hub';
        const date = new Date(d.at).toLocaleString();
        return `
            <tr>
                <td style="font-weight:600">${d.item}</td>
                <td class="mono">${d.quantity}</td>
                <td>${d.donor} <br><small style="opacity:0.5">${date}</small></td>
                <td>${center}</td>
                <td><span class="badge ${d.status === 'PENDING' ? 'badge-info' : 'badge-available'}">${d.status}</span></td>
            </tr>
        `;
    }).join('');
}

function updateMapMarkers(centers) {
    markers.forEach(m => map.removeLayer(m));
    markers = [];

    centers.forEach(c => {
        if (c.latitude && c.longitude) {
            const marker = L.marker([c.latitude, c.longitude]).addTo(map);
            marker.bindPopup(`
                <div style="font-family: 'IBM Plex Sans', sans-serif; min-width: 160px;">
                    <b style="color: #2979ff; font-size: 14px;">${c.name}</b><br>
                    <small>${c.location}</small><hr style="border:0; border-top:1px solid #333; margin:8px 0">
                    <b>Stock Status:</b><br>
                    Food Packets: ${c.items.food_packets}<br>
                    Water (L): ${c.items.water_liters}<br>
                    Clothing: ${c.items.clothing_kits}<br><br>
                    <span style="color: ${c.status === 'LOW_STOCK' ? '#ff2d2d' : '#00e676'}">${c.status}</span>
                </div>
            `);
            markers.push(marker);
        }
    });

    if (markers.length > 0) {
        setTimeout(() => map.fitBounds(new L.featureGroup(markers).getBounds().pad(0.2)), 100);
    }
}

function populateCenterSelect(select, centers) {
    select.innerHTML = centers.map(c => `<option value="${c.id}">${c.name} (${c.location})</option>`).join('');
}

// Modal Handlers
function showDonateModal() {
    document.getElementById('donateModal').classList.remove('hidden');
    setTimeout(() => map.invalidateSize(), 150);
}

function hideDonateModal() {
    document.getElementById('donateModal').classList.add('hidden');
}

// Donate Form
document.getElementById('donate-form').onsubmit = async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button[type="submit"]');
    const originalText = btn.innerHTML;
    
    btn.innerHTML = 'SUBMITTING...';
    btn.disabled = true;

    const formData = new FormData(e.target);
    const payload = Object.fromEntries(formData.entries());

    try {
        const res = await fetch(`${BASE}/ReliefServlet/Donate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            hideDonateModal();
            e.target.reset();
            await loadReliefData();
        } else {
            alert('Submission failed.');
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
loadReliefData();
