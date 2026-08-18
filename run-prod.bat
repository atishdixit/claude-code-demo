@echo off
cd /d "%~dp0"
docker compose --env-file env\prod.env -p log-monitor-prod up --build -d
