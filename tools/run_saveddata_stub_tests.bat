@echo off
setlocal EnableExtensions EnableDelayedExpansion
set ROOT=%~dp0..
set OUT=%TEMP%\mf-saveddata-stub-classes
if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%"
set SOURCES=
for /r "%ROOT%\tools\test_support\src" %%F in (*.java) do set SOURCES=!SOURCES! "%%F"
for /r "%ROOT%\src\main\java\com\medievalfactions\domain" %%F in (*.java) do set SOURCES=!SOURCES! "%%F"
set SOURCES=!SOURCES! "%ROOT%\src\main\java\com\medievalfactions\economy\MFCurrency.java" "%ROOT%\src\main\java\com\medievalfactions\data\MFFactionSavedData.java" "%ROOT%\src\main\java\com\medievalfactions\data\MFWorldSavedData.java" "%ROOT%\tools\SavedDataPersistenceSelfTest.java"
javac -encoding UTF-8 -d "%OUT%" !SOURCES!
if errorlevel 1 exit /b 1
java -ea -cp "%OUT%" SavedDataPersistenceSelfTest
