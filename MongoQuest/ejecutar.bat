@echo off
setlocal
cd /d "%~dp0"

set "JAR=target\MongoQuest-1.0-SNAPSHOT-ejecutable.jar"

where java >nul 2>nul
if errorlevel 1 (
    echo No se encontro Java. Instala Java 11 o superior y agregalo al PATH.
    pause
    exit /b 1
)

if not exist "%JAR%" (
    echo No se encontro el JAR: %JAR%
    echo Genera el JAR primero ejecutando: mvn clean package
    pause
    exit /b 1
)

java -jar "%JAR%"
if errorlevel 1 (
    echo La aplicacion termino con un error.
    pause
    exit /b 1
)

endlocal
