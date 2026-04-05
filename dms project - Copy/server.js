const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = 3000;
const DB_FILE = path.join(__dirname, 'db.json');

app.use(cors());
app.use(bodyParser.json());
app.use(express.static(path.join(__dirname, 'frontend')));

// Helper to read DB
const readDB = () => {
    const data = fs.readFileSync(DB_FILE);
    return JSON.parse(data);
};

// Helper to write DB
const writeDB = (data) => {
    fs.writeFileSync(DB_FILE, JSON.stringify(data, null, 2));
};

// --- Dashboard Routes ---
app.get('/disaster/DashboardServlet', (req, res) => {
    const db = readDB();
    const activeIncidents = db.incidents.filter(i => i.status !== 'RESOLVED' && i.status !== 'CLOSED').length;
    const resolvedToday = db.incidents.filter(i => i.status === 'RESOLVED').length; // Simplification
    const resourcesDeployed = db.resources.filter(r => r.status === 'DEPLOYED').length;
    const alertsSent = db.alerts.length;
    const activeAlerts = db.alerts.filter(a => a.status === 'ACTIVE').length;

    res.json({
        activeIncidents,
        resolvedToday,
        resourcesDeployed,
        alertsSent,
        activeAlerts
    });
});

// --- Incident Routes ---
app.get('/disaster/IncidentServlet', (req, res) => {
    const db = readDB();
    let list = db.incidents;
    
    if (req.query.status) {
        list = list.filter(i => i.status === req.query.status);
    }
    
    if (req.query.limit) {
        list = list.slice(0, parseInt(req.query.limit));
    }
    
    res.json(list);
});

app.post('/disaster/IncidentServlet', (req, res) => {
    const db = readDB();
    const newInc = {
        id: db.incidents.length > 0 ? Math.max(...db.incidents.map(i => i.id)) + 1 : 1,
        type: req.body.type || 'UNKNOWN',
        severity: req.body.severity || 'MEDIUM',
        location: req.body.location || 'Unknown',
        latitude: req.body.lat || 0,
        longitude: req.body.lng || 0,
        description: req.body.description || '',
        reported_by: req.body.reporter || 'Anonymous',
        contact_number: req.body.contact || '',
        dead_count: req.body.dead || 0,
        injured_count: req.body.injured || 0,
        missing_count: req.body.missing || 0,
        displaced_count: req.body.displaced || 0,
        status: 'ACTIVE',
        reported_at: new Date().toISOString().slice(0, 19).replace('T', ' ')
    };
    db.incidents.push(newInc);
    writeDB(db);
    res.status(201).json({ id: newInc.id, message: 'Incident created' });
});

app.put('/disaster/IncidentServlet', (req, res) => {
    const db = readDB();
    const index = db.incidents.findIndex(i => i.id === parseInt(req.body.id));
    if (index !== -1) {
        db.incidents[index].status = req.body.status;
        writeDB(db);
        res.json({ message: 'Updated' });
    } else {
        res.status(404).json({ error: 'Not found' });
    }
});

app.delete('/disaster/IncidentServlet', (req, res) => {
    const db = readDB();
    const id = parseInt(req.query.id);
    const initialLength = db.incidents.length;
    db.incidents = db.incidents.filter(i => i.id !== id);
    if (db.incidents.length < initialLength) {
        writeDB(db);
        res.json({ message: 'Deleted' });
    } else {
        res.status(404).json({ error: 'Not found' });
    }
});

// --- Alert Routes ---
app.get('/disaster/AlertServlet', (req, res) => {
    const db = readDB();
    res.json(db.alerts);
});

app.post('/disaster/AlertServlet', (req, res) => {
    const db = readDB();
    const newAlert = {
        id: db.alerts.length > 0 ? Math.max(...db.alerts.map(a => a.id)) + 1 : 1,
        title: req.body.title || 'New Alert',
        level: req.body.level || 'INFO',
        region: req.body.region || 'Global',
        message: req.body.message || '',
        status: 'ACTIVE',
        sent_at: new Date().toISOString().slice(0, 19).replace('T', ' ')
    };
    db.alerts.push(newAlert);
    writeDB(db);
    res.status(201).json(newAlert);
});

// --- Resource Routes ---
app.get('/disaster/ResourceServlet', (req, res) => {
    const db = readDB();
    res.json(db.resources);
});

app.post('/disaster/ResourceServlet', (req, res) => {
    const db = readDB();
    const newRes = {
        id: db.resources.length > 0 ? Math.max(...db.resources.map(r => r.id)) + 1 : 1,
        name: req.body.name,
        type: req.body.type,
        quantity: req.body.quantity || 1,
        location: req.body.location,
        status: 'AVAILABLE',
        incident_id: null
    };
    db.resources.push(newRes);
    writeDB(db);
    res.status(201).json(newRes);
});

app.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}`);
});
