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

app.post('/api/logs', (req, res) => {
  const log = {
    id: Date.now(),
    timestamp: new Date().toISOString(),
    event: req.body.event || 'id_change',
    app_version: req.body.app_version || '—',
    ...req.body,
  };
  delete log.id;
  delete log.timestamp;
  log.id = Date.now();
  log.timestamp = new Date().toISOString();
  const logs = loadLogs();
  logs.unshift(log);
  saveLogs(logs);
  console.log('Log:', log.event, log.timestamp);
  res.json({ ok: true, id: log.id });
});

app.get('/api/logs', (req, res) => {
  res.json(loadLogs());
});

function rowForLog(l) {
  const ts = new Date(l.timestamp).toLocaleString();
  if (l.event === 'app_open') {
    const d = (x) => x || '—';
    return `
    <tr class="event-open">
      <td>${ts}</td>
      <td><strong>app_open</strong><br><small>(cari dəyərlər)</small></td>
      <td>${l.app_version}</td>
      <td>${d(l.android_id)}</td>
      <td>—</td>
      <td>${d(l.serialno)}</td>
      <td>—</td>
      <td>${d(l.ap_serial)}</td>
      <td>—</td>
      <td>${d(l.bluetooth)}</td>
      <td>—</td>
      <td>${d(l.ril_model)}</td>
      <td>—</td>
      <td>${d(l.imei)}</td>
    </tr>`;
  }
  if (l.event === 'post_reboot_verification') {
    const ok = (x) => (x ? '<span class="ok">✓ qaldı</span>' : '<span class="fail">✗ keçmiş qaldı</span>');
    return `
    <tr class="event-reboot">
      <td>${ts}</td>
      <td><strong>post_reboot_verification</strong></td>
      <td>${l.app_version}</td>
      <td>${l.expected_android_id || '—'}</td>
      <td>${l.actual_android_id || '—'} ${l.android_id_persisted !== undefined ? ok(l.android_id_persisted) : ''}</td>
      <td>${l.expected_serialno || '—'}</td>
      <td>${l.actual_serialno || '—'} ${l.serialno_persisted !== undefined ? ok(l.serialno_persisted) : ''}</td>
      <td>${l.expected_ap_serial || '—'}</td>
      <td>${l.actual_ap_serial || '—'} ${l.ap_serial_persisted !== undefined ? ok(l.ap_serial_persisted) : ''}</td>
      <td>${l.expected_bluetooth || '—'}</td>
      <td>${l.actual_bluetooth || '—'} ${l.bluetooth_persisted !== undefined ? ok(l.bluetooth_persisted) : ''}</td>
      <td>${l.expected_ril_model || '—'}</td>
      <td>${l.actual_ril_model || '—'} ${l.ril_model_persisted !== undefined ? ok(l.ril_model_persisted) : ''}</td>
      <td>—</td>
    </tr>`;
  }
  return `
    <tr class="event-change">
      <td>${ts}</td>
      <td><strong>id_change</strong></td>
      <td>${l.app_version}</td>
      <td>${l.android_id_old || '—'}</td>
      <td>${l.android_id_new || '—'}</td>
      <td>${l.serialno_old || '—'}</td>
      <td>${l.serialno_new || '—'}</td>
      <td>${l.ap_serial_old || '—'}</td>
      <td>${l.ap_serial_new || '—'}</td>
      <td>${l.bluetooth_old || '—'}</td>
      <td>${l.bluetooth_new || '—'}</td>
      <td>${l.ril_model_old || '—'}</td>
      <td>${l.ril_model_new || '—'}</td>
      <td>${l.imei || '—'}</td>
    </tr>`;
}

app.get('/', (req, res) => {
  const logs = loadLogs();
  const rows = logs.map(rowForLog).join('');
  res.send(`
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>PND Loglar 1.3</title>
  <style>
    * { box-sizing: border-box; }
    body { font-family: 'Segoe UI', sans-serif; padding: 20px; background: #1a1a2e; color: #eee; margin: 0; }
    h1 { color: #e94560; font-size: 1.5rem; }
    .summary { background: #16213e; padding: 12px; border-radius: 8px; margin-bottom: 16px; }
    table { border-collapse: collapse; background: #16213e; width: 100%; font-size: 11px; border-radius: 8px; overflow: hidden; }
    th, td { border: 1px solid #0f3460; padding: 6px 8px; text-align: left; }
    th { background: #e94560; color: #fff; }
    tr:nth-child(even) { background: #1a1a2e; }
    tr.event-open { background: #1b4332; }
    tr.event-reboot { background: #2d1b4e; }
    tr.event-change { background: #1a1a2e; }
    .ok { color: #80ed99; }
    .fail { color: #ff6b6b; }
    .wrap { overflow-x: auto; }
  </style>
</head>
<body>
  <h1>PND Log server — Tam detallı (v1.3)</h1>
  <div class="summary">Ümumi: ${logs.length} qeyd | app_open: girişdə cari ID-lər | id_change: dəyişiklik | post_reboot_verification: reboot sonrası yeni/keçmiş yoxlama</div>
  <div class="wrap">
    <table>
      <thead>
        <tr>
          <th>Tarix</th>
          <th>Hadisə</th>
          <th>Ver.</th>
          <th>Android ID (köhnə/əvvəl)</th>
          <th>Android ID (yeni/sonra)</th>
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
      <tbody>${rows.length ? rows : '<tr><td colspan="14">Hələ log yoxdur</td></tr>'}
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
