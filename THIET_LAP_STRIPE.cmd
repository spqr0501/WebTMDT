@echo off
chcp 65001 >nul
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\Thiet-lap-Stripe-Sandbox.ps1"
if errorlevel 1 echo Thiet lap chua thanh cong. Xem thong bao ben tren.
pause
