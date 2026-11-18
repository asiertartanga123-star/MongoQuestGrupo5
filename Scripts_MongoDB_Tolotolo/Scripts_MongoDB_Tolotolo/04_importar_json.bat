@echo off
REM ============================================================================
REM 04_importar_json.bat
REM Importación rápida mediante mongoimport desde la carpeta json_iniciales
REM ============================================================================

mongoimport --db Tolotolo --collection Salas --file json_iniciales/Tolotolo.Salas.json --jsonArray --drop
mongoimport --db Tolotolo --collection Clases --file json_iniciales/Tolotolo.Clases.json --jsonArray --drop
mongoimport --db Tolotolo --collection Sesiones --file json_iniciales/Tolotolo.Sesiones.json --jsonArray --drop
mongoimport --db Tolotolo --collection Clientes --file json_iniciales/Tolotolo.Clientes.json --jsonArray --drop
mongoimport --db Tolotolo --collection Trabajadores --file json_iniciales/Tolotolo.Trabajadores.json --jsonArray --drop

echo Importacion completada.
pause
