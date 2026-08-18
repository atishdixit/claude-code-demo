@echo off
cd /d "%~dp0"
docker compose --env-file env\test.env -p log-monitor-test up --build -d
