const BASE = '/disaster';
let map;
let markers = [];

// Initialize Map
function initMap() {
    map = L.map('map').setView([13.0827, 80.2707], 11); // Center on Chennai

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);
}

// Load Medical Camps
async function loadCamps() {
    const tbody = document.getElementById('camps-table-body');
    try {
        const res = await fetch(`${BASE}/MedicalCampServlet`);
        if (!res.ok) throw new Error('Data fetch failed');
        const data = await res.json();
        
        // Use requestAnimationFrame to avoid blocking the main thread
        requestAnimationFrame(() => {
            renderCamps(data);
            updateMapMarkers(data);
        });
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:2rem;color:var(--accent-red)">Error loading data.</td></tr>`;
    }
}

function renderCamps(data) {
    const tbody = document.getElementById('camps-table-body');
    if (!data || !data.length) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:2rem;opacity:.5">No medical camps registered.</td></tr>`;
        return;
    }

    const rows = data.map(camp => {
        const occPercent = (camp.occupancy || 0) / (camp.capacity || 1);
        const occupancyStatus = occPercent >= 1 ? 'badge-high' : 'badge-available';
        
        return `
            <tr>
                <td style="font-weight:600">${camp.name}</td>
                <td>${camp.location}</td>
                <td style="font-size: 11px; color: var(--text-secondary)">${camp.doctors}</td>
                <td style="font-size: 11px; color: var(--text-secondary)">${camp.medicines}</td>
                <td class="mono">${camp.occupancy || 0} / ${camp.capacity}</td>
                <td>
                    <span class="badge ${occupancyStatus}">
                        ${occPercent >= 1 ? 'FULL' : 'ACTIVE'}
                    </span>
                </td>
            </tr>
        `;
    }).join('');
    
    tbody.innerHTML = rows;
}

function updateMapMarkers(data) {
    // Clear existing markers efficiently
    markers.forEach(m => map.removeLayer(m));
    markers = [];

    data.forEach(camp => {
        if (camp.latitude && camp.longitude) {
            const marker = L.marker([camp.latitude, camp.longitude]).addTo(map);
            marker.bindPopup(`
                <div style="font-family: 'IBM Plex Sans', sans-serif; min-width: 150px;">
                    <b style="color: #ff2d2d; font-size: 14px;">${camp.name}</b><br>
                    <small>${camp.location}</small><hr style="border: 0; border-top: 1px solid #333; margin: 8px 0;">
                    <b>Doctors:</b> ${camp.doctors}<br>
                    <b>Medicines:</b> ${camp.medicines}<br>
                    <b>Status:</b> ${camp.occupancy || 0} / ${camp.capacity} Registered
                </div>
            `);
            markers.push(marker);
        }
    });

    // Fit bounds with a timeout to ensure everything is rendered
    if (markers.length > 0) {
        setTimeout(() => {
            const group = new L.featureGroup(markers);
            map.fitBounds(group.getBounds().pad(0.2));
        }, 100);
    }
}

// Modal Handlers
function showAddModal() {
    const modal = document.getElementById('addModal');
    modal.classList.remove('hidden');
    // Important: Leaflet sometimes needs a resize check if parent changed
    setTimeout(() => map.invalidateSize(), 150);
}

function hideAddModal() {
    document.getElementById('addModal').classList.add('hidden');
}

// Form Submission
document.getElementById('add-camp-form').onsubmit = async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button[type="submit"]');
    const originalText = btn.innerHTML;
    
    // Add loading state
    btn.innerHTML = 'REGISTERING...';
    btn.disabled = true;
    btn.style.opacity = '0.7';

    const formData = new FormData(e.target);
    const payload = Object.fromEntries(formData.entries());

    try {
        const res = await fetch(`${BASE}/MedicalCampServlet`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            hideAddModal();
            e.target.reset();
            await loadCamps(); // Wait for data to reload
        } else {
            alert('Failed to register camp.');
        }
    } catch (err) {
        console.error(err);
        alert('Error connecting to server.');
    } finally {
        // Restore button state
        btn.innerHTML = originalText;
        btn.disabled = false;
        btn.style.opacity = '1';
    }
};

// Auto-populate lat/lng on map click (Optional polish)
function setupMapClick() {
    map.on('click', function(e) {
        const { lat, lng } = e.latlng;
        document.getElementsByName('latitude')[0].value = lat.toFixed(6);
        document.getElementsByName('longitude')[0].value = lng.toFixed(6);
        
        // Show modal if not open? Maybe just toast.
        // For now, let's just populate if modal is open.
    });
}

// Init
initMap();
loadCamps();
setupMapClick();
