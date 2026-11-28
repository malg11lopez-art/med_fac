@echo off
setlocal EnableExtensions EnableDelayedExpansion
set ROOT=%~dp0..
set OUT=%TEMP%\mf-domain-core-classes
if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%"
set SOURCES="%ROOT%\src\main\java\com\medievalfactions\economy\MFCurrency.java" "%ROOT%\src\main\java\com\medievalfactions\production\MFProductionEngine.java" "%ROOT%\src\main\java\com\medievalfactions\simulation\MFOfflineSimulation.java" "%ROOT%\tools\DomainCoreSelfTest.java" "%ROOT%\tools\MFAdvancedSystemsSelfTest.java" "%ROOT%\tools\MFSimulationSystemsSelfTest.java"
for /r "%ROOT%\src\main\java\com\medievalfactions\domain" %%F in (*.java) do set SOURCES=!SOURCES! "%%F"
javac -encoding UTF-8 -d "%OUT%" !SOURCES!
if errorlevel 1 exit /b 1
java -ea -cp "%OUT%" com.medievalfactions.tools.DomainCoreSelfTest
if errorlevel 1 exit /b 1
java -ea -cp "%OUT%" com.medievalfactions.tools.MFAdvancedSystemsSelfTest
if errorlevel 1 exit /b 1
java -ea -cp "%OUT%" com.medievalfactions.tools.MFSimulationSystemsSelfTest
if errorlevel 1 exit /b 1
