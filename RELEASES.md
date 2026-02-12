# Avtomatik Release (v1.5, v1.6, …)

## Workflow

1. **Kod dəyişikliyi** + **versiya artır**  
   `app/build.gradle.kts`-də:
   - `versionCode` — bir artır (məs. 6)
   - `versionName` — yeni versiya (məs. `"1.6"`)

2. **Commit və push**
   ```bash
   git add .
   git commit -m "v1.6 update"
   git push origin main
   ```

3. **Tag push** (bu workflow-u işə salır)
   ```bash
   git tag v1.6
   git push origin v1.6
   ```

4. **GitHub Actions** avtomatik:
   - APK-nı build edir
   - Həmin tag üçün **Release** yaradır
   - **app-release.apk** faylını Release-ə yükləyir

Tətbiqdə yeniləmə yoxlaması bu Release-dən APK endirəcək.
