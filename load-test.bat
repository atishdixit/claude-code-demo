@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0load-test.ps1" %*
