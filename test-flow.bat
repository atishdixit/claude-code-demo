@echo off
setlocal enabledelayedexpansion

set AUTH_URL=http://localhost:8081
set INGEST_URL=http://localhost:8082
set PROCESS_URL=http://localhost:8083
set ALERT_URL=http://localhost:8084

echo === Register user "alice" ===
curl -s -X POST %AUTH_URL%/auth/register -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"password123\",\"role\":\"ADMIN\"}"
echo.
echo (if alice already exists, logging in instead)

echo.
echo === Login ===
curl -s -X POST %AUTH_URL%/auth/login -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"password123\"}" > token_response.json
type token_response.json
echo.

for /f "delims=" %%t in ('powershell -NoProfile -Command "(Get-Content token_response.json | ConvertFrom-Json).token"') do set TOKEN=%%t
del token_response.json

echo.
echo === Publish sample logs via log-ingestion-service ===
curl -s -w "\nHTTP %%{http_code}\n" -X POST %INGEST_URL%/api/logs -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"serviceName\":\"order-service\",\"level\":\"INFO\",\"message\":\"Order created\"}"
curl -s -w "\nHTTP %%{http_code}\n" -X POST %INGEST_URL%/api/logs -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"serviceName\":\"payment-service\",\"level\":\"ERROR\",\"message\":\"Failed to charge card\"}"
curl -s -w "\nHTTP %%{http_code}\n" -X POST %INGEST_URL%/api/logs -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"serviceName\":\"payment-service\",\"level\":\"CRITICAL\",\"message\":\"Payment gateway unreachable\"}"
curl -s -w "\nHTTP %%{http_code}\n" -X POST %INGEST_URL%/api/logs -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"serviceName\":\"order-service\",\"level\":\"WARN\",\"message\":\"Retrying confirmation\"}"

echo.
echo Waiting for Kafka consumers to process...
timeout /t 3 /nobreak >nul

echo.
echo === GET /api/logs (log-processing-service, should include all levels) ===
curl -s -H "Authorization: Bearer %TOKEN%" %PROCESS_URL%/api/logs
echo.

echo.
echo === GET /api/alerts (alert-service, should include only ERROR/CRITICAL) ===
curl -s -H "Authorization: Bearer %TOKEN%" %ALERT_URL%/api/alerts
echo.

endlocal
