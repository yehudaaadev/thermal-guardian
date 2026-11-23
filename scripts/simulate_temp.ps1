# ThermalGuard Temperature Simulation Script for PowerShell
# Simulates temperature rise and cooldown scenarios via ADB dumpsys battery

Write-Host "=== ThermalGuard Temperature Simulation (PowerShell) ===" -ForegroundColor Cyan
Write-Host "Checking connected ADB devices..."
adb devices

Write-Host "`n[Step 1/6] Normal baseline temperature (32.0°C / temp 320)..." -ForegroundColor Green
adb shell dumpsys battery set temp 320
Start-Sleep -Seconds 5

Write-Host "[Step 2/6] Moderate load (36.5°C / temp 365)..." -ForegroundColor Green
adb shell dumpsys battery set temp 365
Start-Sleep -Seconds 5

Write-Host "[Step 3/6] Entering WARM threshold (38.5°C / temp 385)..." -ForegroundColor Yellow
adb shell dumpsys battery set temp 385
Start-Sleep -Seconds 8

Write-Host "[Step 4/6] Entering HOT threshold (41.5°C / temp 415)..." -ForegroundColor Red
adb shell dumpsys battery set temp 415
Start-Sleep -Seconds 8

Write-Host "[Step 5/6] Entering CRITICAL threshold (44.0°C / temp 440)..." -ForegroundColor Magenta
adb shell dumpsys battery set temp 440
Start-Sleep -Seconds 10

Write-Host "[Step 6/6] Cooling down (35.0°C / temp 350)..." -ForegroundColor Cyan
adb shell dumpsys battery set temp 350
Start-Sleep -Seconds 5

Write-Host "`nResetting battery dumpsys to physical device state..." -ForegroundColor Cyan
adb shell dumpsys battery reset
Write-Host "=== Simulation Complete! ===" -ForegroundColor Green

