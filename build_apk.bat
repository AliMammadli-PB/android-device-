@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

echo.
echo  ====== PND APK Build (v2) ======
echo  Enter version for GitHub Release (e.g. v1.8.9 or 1.8.9)
echo.

set /p "INPUT=Version: "
if "!INPUT!"=="" (
    echo No version entered. Exiting.
    pause
    exit /b 1
)

if "!INPUT:~0,1!"=="v" (set "VER=!INPUT:~1!") else (set "VER=!INPUT!")
set "VER=!VER: =!"
if "!VER!"=="" (
    echo Invalid version.
    pause
    exit /b 1
)

for /f "tokens=1,2,3 delims=." %%a in ("!VER!") do (
    set "V1=%%a"
    set "V2=%%b"
    set "V3=%%c"
)
if not defined V2 set "V2=0"
if not defined V3 set "V3=0"
set "V1=!V1: =!"
set "V2=!V2: =!"
set "V3=!V3: =!"
set "CODE=!V1!!V2!!V3!"
if "!CODE!"=="" set "CODE=1"

echo.
echo  versionName: !VER!
echo  versionCode: !CODE!
echo.

set "CODE=!CODE!"
set "VER=!VER!"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0set_version.ps1" -Version "!VER!" -VersionCode "!CODE!"
if errorlevel 1 (
    echo Failed to update app\build.gradle.kts
    pause
    exit /b 1
)
echo app\build.gradle.kts updated: versionName=!VER! versionCode=!CODE!

rem Check JAVA_HOME via PowerShell to avoid "if exist ...\java.exe" period bug in CMD
set "JAVA_OK=0"
powershell -NoProfile -Command "if (Test-Path (Join-Path $env:JAVA_HOME 'bin\\java.exe')) { exit 0 } else { exit 1 }"
if errorlevel 1 set "JAVA_OK=1"
if "!JAVA_OK!"=="1" (
    set "JAVA_OK=0"
    powershell -NoProfile -Command "if (Test-Path 'C:\\Program Files\\Java\\jdk-17\\bin\\java.exe') { exit 0 } else { exit 1 }"
    if errorlevel 1 (
        powershell -NoProfile -Command "if (Test-Path 'C:\\Program Files\\Java\\jdk-21\\bin\\java.exe') { exit 0 } else { exit 1 }"
        if errorlevel 1 (
            set "JAVA_HOME=C:\Program Files\Java\jdk-17"
            echo WARNING: No Java found. Set JAVA_HOME to jdk-17.
        ) else (
            set "JAVA_HOME=C:\Program Files\Java\jdk-21"
            echo Using JAVA_HOME: jdk-21
        )
    ) else (
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
        echo Using JAVA_HOME: jdk-17
    )
)

if not defined ANDROID_HOME set "ANDROID_HOME=!LOCALAPPDATA!\Android\Sdk"

echo Stopping Gradle daemons (releases file locks)...
call gradlew.bat --stop 2>nul
echo Clean...
call gradlew.bat clean --no-daemon 2>nul
echo Building...
call gradlew.bat assembleRelease assembleDebug --no-daemon
if errorlevel 1 (
    echo Build failed.
    pause
    exit /b 1
)

if not exist "apk" mkdir apk
copy /Y "app\build\outputs\apk\release\app-release-unsigned.apk" "apk\PND-release-unsigned.apk" >nul
copy /Y "app\build\outputs\apk\debug\app-debug.apk" "apk\PND-debug.apk" >nul

echo.
echo  ====== Done ======
echo  Release: apk\PND-release-unsigned.apk
echo  Debug:   apk\PND-debug.apk
echo  Tag: v!VER!
echo.

set "DO_GH="
set /p "DO_GH=GitHub release ac + APK yukle + push? (y/n): "
if /i not "!DO_GH!"=="y" goto :skip_github

echo.
echo  Git add + commit...
git add app\build.gradle.kts 2>nul
git add set_version.ps1 build_apk.bat 2>nul
git status
git commit -m "Release v!VER!" 2>nul
if errorlevel 1 (
    echo  Commit atlandi veya hec bir degisiklik yoxdur.
) else (
    echo  Push...
    git push
)

echo.
where gh >nul 2>nul
if errorlevel 1 (
    echo  XETA: GitHub CLI ^(gh^) tapilmadi. Quraşdirmag: https://cli.github.com/
    echo  Alternativ: GitHub-da release-i elle yaradib APK yukleyin.
) else (
    echo  GitHub release: v!VER! ^(PND v!VER!^)...
    gh release create "v!VER!" "apk\PND-debug.apk" --title "PND v!VER!" --notes "Release v!VER!" 2>nul
    if errorlevel 1 (
        echo  XETA: gh release create ugursuz. gh auth login ile daxil olun.
    ) else (
        echo  GitHub release yaradildi: v!VER!
    )
)

:skip_github
echo.
pause
