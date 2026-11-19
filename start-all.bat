@echo off
title SmartSpend - All-In-One Launcher
echo ====================================================================
echo Starting SmartSpend Full-Stack System (Backend + Frontend)...
echo ====================================================================

start "SmartSpend Backend" "%~dp0start-backend.bat"
timeout /t 3 /nobreak >nul
start "SmartSpend Frontend" "%~dp0start-frontend.bat"

echo.
echo Both servers have been launched in separate windows!
echo Backend:  http://localhost:8080/api
echo Frontend: http://localhost:5000
echo ====================================================================
