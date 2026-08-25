@echo off
setlocal

cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: "java" was not found on PATH. Install a Java 21+ JDK and make sure java.exe is on PATH.
    pause
    exit /b 2
)

set JAR=target\tiff-to-pdf-converter-1.0.0.jar

if not exist "%JAR%" (
    echo ERROR: %JAR% not found. Build it first with:
    echo     mvn clean package
    pause
    exit /b 2
)

echo Running tiff-to-pdf-converter...
echo   input  : %cd%\input
echo   output : %cd%\output
echo   logs   : %cd%\logs
echo.

java -jar "%JAR%"
set EXITCODE=%ERRORLEVEL%

echo.
echo Finished with exit code %EXITCODE% (0=all converted, 1=some files failed, 2=could not start).
echo See logs\health.log and logs\error.log for details.
pause

endlocal
exit /b %EXITCODE%
