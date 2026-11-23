#!/usr/bin/env bash
# ThermalGuard Temperature Simulation Script
# Simulates temperature rise and cooldown scenarios via ADB dumpsys battery

set -e

echo "=== ThermalGuard Temperature Simulation ==="
echo "Ensuring ADB device is connected..."
adb devices

echo ""
echo "[Step 1/6] Normal baseline temperature (32.0°C / temp 320)..."
adb shell dumpsys battery set temp 320
sleep 5

echo "[Step 2/6] Moderate load (36.5°C / temp 365)..."
adb shell dumpsys battery set temp 365
sleep 5

echo "[Step 3/6] Entering WARM threshold (38.5°C / temp 385)..."
adb shell dumpsys battery set temp 385
sleep 8

echo "[Step 4/6] Entering HOT threshold (41.5°C / temp 415)..."
adb shell dumpsys battery set temp 415
sleep 8

echo "[Step 5/6] Entering CRITICAL threshold (44.0°C / temp 440)..."
adb shell dumpsys battery set temp 440
sleep 10

echo "[Step 6/6] Cooling down (35.0°C / temp 350)..."
adb shell dumpsys battery set temp 350
sleep 5

echo ""
echo "Resetting battery dumpsys to physical device state..."
adb shell dumpsys battery reset
echo "=== Simulation Complete! ==="

