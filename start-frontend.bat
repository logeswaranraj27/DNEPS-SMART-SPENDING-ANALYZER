@echo off
title SmartSpend - Frontend Web Application
echo ====================================================================
echo Serving SmartSpend Frontend on http://localhost:5000 ...
echo ====================================================================

cd /d "%~dp0frontend"

start "" "http://localhost:5000"
python -m http.server 5000
pause
