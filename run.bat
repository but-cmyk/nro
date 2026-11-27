@echo off
chcp 65001 > nul
set DOCKER_CONTEXT=default
title NRO GAME SERVER (2026)

echo ===================================================================
echo               NRO GAME SERVER LAUNCHER - AUTO DOCKER
echo ===================================================================

where docker >nul 2>&1
if %errorlevel% neq 0 goto RUN_NATIVE

docker info >nul 2>&1
if %errorlevel% neq 0 goto RUN_NATIVE

echo [OK] Da phat hien Docker dang hoat dong san sang!
echo.
echo Vui long chon che do khoi chay:
echo   [1] KHOI DONG SIEU TOC (Native Java + Auto Docker MySQL 3308) - [MAC DINH: 1.5 giay]
echo   [2] Chay toan bo cum trong Docker (MySQL + Server)
echo   [3] Dung (Down) toan bo cum Docker
echo.
echo Tu dong khoi dong Sieu Toc [1] sau 2 giay...
echo -------------------------------------------------------------------
choice /c 123 /t 2 /d 1 /n /m "Nhap lua chon [1, 2, 3] (Mac dinh: 1): "

if errorlevel 3 goto DOCKER_DOWN
if errorlevel 2 goto RUN_DOCKER_COMPOSE
if errorlevel 1 goto RUN_NATIVE_MYSQL

:RUN_NATIVE_MYSQL
echo.
echo [DOCKER] Dung container backend server (neu co) de tranh xung dot port 14445...
docker stop nro_backend nro_server >nul 2>&1
echo [DOCKER] Kich hoat nhanh MySQL (3308) trong Docker...
docker start nro_mysql >nul 2>&1 || docker compose up -d mysql >nul 2>&1
timeout /t 2 /nobreak >nul 2>&1
goto RUN_NATIVE

:RUN_DOCKER_COMPOSE
echo.
echo ===================================================================
echo [DOCKER] Dang khoi dong toan bo cum Server qua Docker Compose...
echo ===================================================================
docker compose up -d
if %errorlevel% neq 0 (
    echo [LOI] Khoi dong Docker Compose that bai! Chuyen ve Native Java...
    goto RUN_NATIVE
)
echo.
echo [THANH CONG] Toan bo cum Server da chay trong Docker!
echo Dang mo nhat ky hoat dong (Logs)...
echo (Nhan Ctrl+C de thoat xem logs, Server van tiep tuc chay ngam).
echo.
docker compose logs -f nro_server
goto END

:DOCKER_DOWN
echo.
echo [DOCKER] Dang dung toan bo cum containers Docker...
docker compose down
echo [OK] Da dung toan bo dich vu Docker.
pause
goto END

:RUN_NATIVE
echo.
echo ===================================================================
echo           KHOI DONG NRO GAME SERVER (NATIVE JAVA)
echo ===================================================================

echo [PORT] Kiem tra va giai phong port 14445 / 14446 truoc khi khoi dong...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":14445 :14446" ^| findstr LISTENING') do (
    echo [PORT] Phat hien tien trinh PID %%a dang chiem port. Dang giai phong...
    taskkill /f /pid %%a >nul 2>&1
)
timeout /t 1 /nobreak >nul 2>&1

java -server -Xms512M -Xmx1536M -XX:MaxDirectMemorySize=256M -XX:MaxMetaspaceSize=192M -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:+UseStringDeduplication --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED "-Dio.netty.tryReflectionSetAccessible=true" "-Dfile.encoding=UTF-8" -cp "build\classes;dist\NROK.jar;lib\*" server.ServerManager

if %errorlevel% neq 0 (
    echo.
    echo [LOI] Server dung voi ma loi %errorlevel%
    pause
)

:END
