# Usage: .\set_version.ps1 -Version "1.1.1" -VersionCode "111"
param(
    [Parameter(Mandatory=$true)][string]$Version,
    [Parameter(Mandatory=$true)][string]$VersionCode
)
$ktsPath = Join-Path $PSScriptRoot "app\build.gradle.kts"
if (-not (Test-Path $ktsPath)) {
    Write-Error "Tapilmadi: $ktsPath"
    exit 1
}
$content = Get-Content $ktsPath -Raw -Encoding UTF8
$content = $content -replace 'versionCode = \d+', "versionCode = $VersionCode"
$content = $content -replace 'versionName = "[^"]*"', "versionName = `"$Version`""
Set-Content $ktsPath $content -Encoding UTF8
Write-Host "Yazildi: versionName = $Version, versionCode = $VersionCode"
