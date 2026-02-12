# PND – Device ID Changer

Root (Magisk) ilə Android cihazın müxtəlif identifikatorlarını (Android ID, Serial, Bluetooth ünvanı və s.) dəyişən tətbiq. Azərbaycan dilində interfeys.

## Yeniləmə (Update) sistemi

Tətbiq açılanda **GitHub Releases**-dan yeni versiya yoxlanılır. Yeni versiya varsa ekranda bildiriş çıxır; **Endir və quraşdır** ilə APK endirilir və quraşdırma açılır.

### Yeni versiya buraxmaq üçün (GitHub-da)

1. **Versiyanı artır:** `app/build.gradle.kts`-də `versionCode` və `versionName` dəyişdirin (məs. `versionCode = 2`, `versionName = "1.1"`).

2. **APK yığın:**  
   `./gradlew assembleRelease` (və ya imzalı release üçün keystore ilə).

3. **GitHub Release yaradın:**
   - Repo: https://github.com/AliMammadli-PB/android-device-
   - **Releases** → **Create a new release**
   - **Tag:** `v1.1` (versionName ilə uyğun, `v` ilə başlamalıdır)
   - **Release title:** məs. `PND 1.1`
   - **Assets** bölməsində **Attach binaries** ilə `app/build/outputs/apk/release/app-release-unsigned.apk` (və ya imzaladığınız APK) faylını yükləyin.
   - **Publish release** düyməsinə basın.

4. Tətbiqdə yeni versiya görünəcək və istifadəçilər **Endir və quraşdır** ilə yeniləyə biləcək.

### Qeyd

- Release **tag** mütləq `v` + versionName formatında olsun (məs. `v1.0`, `v1.1`).
- Release-də **ən azı bir APK** asset olmalıdır (browser_download_url üçün).

## Proyekti GitHub-a push etmək

```bash
cd "c:\Users\canur\Desktop\testler\device id android"
git init
git add .
git commit -m "PND: initial commit with update system"
git branch -M main
git remote add origin https://github.com/AliMammadli-PB/android-device-.git
git push -u origin main
```

(İlk dəfə push edəndə GitHub istifadəçi adı/şifrə və ya token tələb edə bilər.)

## Build

- Debug: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`
- Release: `./gradlew assembleRelease` → `app/build/outputs/apk/release/app-release-unsigned.apk`

## Tələblər

- Android 7+ (minSdk 24)
- Root (Magisk) – identifikatorları dəyişmək üçün
