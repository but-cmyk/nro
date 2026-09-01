@echo off
chcp 65001 > nul
echo =======================================================
echo      STARTING NRO GAME SERVER (JAVA 21 / NETTY 4.X)
echo =======================================================

java -server -Xms2G -Xmx8G -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:+UseStringDeduplication "-Dfile.encoding=UTF-8" -cp "dist\NROK.jar;lib\*" server.ServerManager

if %errorlevel% neq 0 (
    echo.
    echo Server terminated with error code %errorlevel%
    pause
)