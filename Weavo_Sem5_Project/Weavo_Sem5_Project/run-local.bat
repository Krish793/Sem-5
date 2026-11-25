@echo off
title Weavo Artisan Marketplace - Sem 5 Integrated Project
echo ================================================================
echo           WEAVO - LOCAL ARTISAN MARKETPLACE
echo           From the loom to your living room.
echo ================================================================
echo.
echo [*] Starting Weavo Unified Python REST API + Modern Web App...
echo [*] Academic Test Mode: DPDP Act 2023 Compliant + Sandbox Payments
echo.

start "" http://localhost:8080

where python >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    python weavo-app\server.py
) else (
    "%LOCALAPPDATA%\Programs\Python\Python313\python.exe" weavo-app\server.py
)

pause
