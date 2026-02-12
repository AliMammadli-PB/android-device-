const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;
const LOG_FILE = path.join(__dirname, 'logs.json');

app.use(cors());
app.use(express.json());

function loadLogs() {
  try {
    const data = fs.readFileSync(LOG_FILE, 'utf8');
    return JSON.parse(data);
  } catch {
    return [];
  }
}

function saveLogs(logs) {
  fs.writeFileSync(LOG_FILE, JSON.stringify(logs, null, 2), 'utf8');
}

// POST /api/logs — tətbiq buradan log göndərir
app.post('/api/logs', (req, res) => {
  const log = {
    id: Date.now(),
    timestamp: new Date().toISOString(),
    app_version: req.body.app_version || '—',
    android_id_old: req.body.android_id_old || '—',
    android_id_new: req.body.android_id_new || '—',
    serialno_old: req.body.serialno_old || '—',
    serialno_new: req.body.serialno_new || '—',
    ap_serial_old: req.body.ap_serial_old || '—',
    ap_serial_new: req.body.ap_serial_new || '—',
    bluetooth_old: req.body.bluetooth_old || '—',
    bluetooth_new: req.body.bluetooth_new || '—',
    ril_model_old: req.body.ril_model_old || '—',
    ril_model_new: req.body.ril_model_new || '—',
    imei: req.body.imei || '—',
  };
  const logs = loadLogs();
  logs.unshift(log);
  saveLogs(logs);
  console.log('Log qəbul edildi:', log.timestamp, log.app_version);
  res.json({ ok: true, id: log.id });
});

// GET /api/logs — bütün loglar (JSON)
app.get('/api/logs', (req, res) => {
  res.json(loadLogs());
});

// GET / — brauzerdə logları göstərmək üçün sadə səhifə
app.get('/', (req, res) => {
  const logs = loadLogs();
  const rows = logs.map(
    (l) => `
    <tr>
      <td>${new Date(l.timestamp).toLocaleString()}</td>
      <td>${l.app_version}</td>
      <td>${l.android_id_old}</td>
      <td>${l.android_id_new}</td>
      <td>${l.serialno_old}</td>
      <td>${l.serialno_new}</td>
      <td>${l.ap_serial_old}</td>
      <td>${l.ap_serial_new}</td>
      <td>${l.bluetooth_old}</td>
      <td>${l.bluetooth_new}</td>
      <td>${l.ril_model_old}</td>
      <td>${l.ril_model_new}</td>
      <td>${l.imei}</td>
    </tr>`
  ).join('');
  res.send(`
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>PND Loglar</title>
  <style>
    body { font-family: sans-serif; padding: 16px; background: #f5f5f5; }
    h1 { color: #00695C; }
    table { border-collapse: collapse; background: white; width: 100%; font-size: 12px; }
    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
    th { background: #00897B; color: white; }
    tr:nth-child(even) { background: #f9f9f9; }
  </style>
</head>
<body>
  <h1>PND log server — Device ID dəyişiklikləri</h1>
  <p>Ümumi: ${logs.length} qeyd</p>
  <div style="overflow-x: auto;">
    <table>
      <thead>
        <tr>
          <th>Tarix</th>
          <th>Versiya</th>
          <th>Android ID (köhnə)</th>
          <th>Android ID (yeni)</th>
          <th>Serial (köhnə)</th>
          <th>Serial (yeni)</th>
          <th>AP Serial (köhnə)</th>
          <th>AP Serial (yeni)</th>
          <th>Bluetooth (köhnə)</th>
          <th>Bluetooth (yeni)</th>
          <th>RIL Model (köhnə)</th>
          <th>RIL Model (yeni)</th>
          <th>IMEI</th>
        </tr>
      </thead>
      <tbody>${rows.length ? rows : '<tr><td colspan="13">Hələ log yoxdur</td></tr>'}
      </tbody>
    </table>
  </div>
</body>
</html>
  `);
});

app.listen(PORT, () => {
  console.log(`PND log server: http://localhost:${PORT}`);
});
