@echo off
setlocal

set "APP_DIR=%~dp0MongoQuest"

if not exist "%APP_DIR%\pom.xml" (
    echo No se encontro el proyecto Maven en "%APP_DIR%".
    pause
    exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven no esta instalado o no esta disponible en PATH.
    pause
    exit /b 1
)

pushd "%APP_DIR%" || (
    echo No se pudo acceder a "%APP_DIR%".
    pause
    exit /b 1
)

call mvn clean javafx:run
set "RESULT=%ERRORLEVEL%"
popd

if not "%RESULT%"=="0" (
    echo.
    echo La aplicacion termino con un error. Codigo: %RESULT%
)

pause
exit /b %RESULT%
