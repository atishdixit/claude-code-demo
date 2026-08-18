@echo off
cd /d "%~dp0"
docker compose --env-file env\stage.env -p log-monitor-stage up --build -d
