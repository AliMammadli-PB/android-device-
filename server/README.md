# PND Log Server (v1.2)

Tətbiq "Hamısını dəyişdir" edəndə loglar bu servisə POST olunur (serial, android_id, bluetooth və s. — köhnə və yeni dəyərlər).

## Quraşdırma

```bash
cd server
npm install
npm start
```

Server `http://localhost:3000` ünvanında işləyir.

## API

- **POST /api/logs** — tətbiq JSON göndərir (android_id_old, android_id_new, serialno_old, …)
- **GET /api/logs** — bütün loglar JSON
- **GET /** — brauzerdə cədvəl şəklində loglar

## Telefonda URL

Telefon və kompüter eyni Wi-Fi-də olsun. Kompüterin IP-ni tapın (məs. `ipconfig`), sonra `gradle.properties`-də:

```
LOG_SERVER_URL=http://192.168.1.XXX:3000
```

Yenidən build edin. Tətbiq dəyişiklik edəndə loglar bu ünvana gedəcək.
