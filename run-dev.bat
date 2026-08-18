@echo off
cd /d "%~dp0"
docker compose --env-file env\dev.env -p log-monitor-dev up --build -d
