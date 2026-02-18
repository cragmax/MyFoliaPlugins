@echo off
:: ============================================================
:: start.bat [minMemory] [maxMemory]
:: Template file - tokens are substituted by Gradle on deploy.
:: Generated copy lives in server folder - do not edit that one.
::
:: Can also be run manually from the server folder:
::   start.bat          -> uses baked-in defaults
::   start.bat 2G 8G   -> overrides memory settings
:: ============================================================

set MIN_MEMORY=%1
set MAX_MEMORY=%2

if "%MIN_MEMORY%"=="" set MIN_MEMORY=@SERVER_MIN_MEMORY@
if "%MAX_MEMORY%"=="" set MAX_MEMORY=@SERVER_MAX_MEMORY@

java -Xms%MIN_MEMORY% -Xmx%MAX_MEMORY% -jar @SERVER_JAR@ --nogui
pause