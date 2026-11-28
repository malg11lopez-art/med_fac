@echo off
setlocal
cd /d "%~dp0"
python tools\validate_assets.py
if errorlevel 1 exit /b 1
tools\run_domain_tests.bat
if errorlevel 1 exit /b 1
tools\run_saveddata_stub_tests.bat
if errorlevel 1 exit /b 1
where gradle >nul 2>nul
if errorlevel 1 (echo Validaciones locales superadas. Gradle 8.12+ no esta instalado; no se pudo ejecutar clean test build.& exit /b 2)
gradle clean test build
if errorlevel 1 exit /b 1
