@echo off
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
echo   [1] KHOI DONG SIEU TOC (Native Java + Auto Redis Docker) - [MUC DINH: 1.5 giay]
echo   [2] Chay toan bo cum trong Docker (MySQL + Redis + Server)
echo   [3] Chi bat Redis Cache trong Docker
echo   [4] Dung (Down) toan bo cum Docker
echo.
echo Tu dong khoi dong Sieu Toc [1] sau 2 giay...
echo -------------------------------------------------------------------
choice /c 1234 /t 2 /d 1 /n /m "Nhap lua chon [1, 2, 3, 4] (Mac dinh: 1): "

if errorlevel 4 goto DOCKER_DOWN
if errorlevel 3 goto RUN_REDIS_ONLY
if errorlevel 2 goto RUN_DOCKER_COMPOSE
if errorlevel 1 goto RUN_NATIVE_REDIS

:RUN_NATIVE_REDIS
echo.
echo [DOCKER] Kich hoat nhanh Redis Cache trong Docker...
docker start nro_redis >nul 2>&1 || docker compose up -d redis >nul 2>&1
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

:RUN_REDIS_ONLY
echo.
echo [DOCKER] Dang bat Redis Cache trong Docker...
docker compose up -d redis
echo [OK] Redis Cache da hoat dong tai localhost:6379!
pause
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
echo           KHOI DONG NRO GAME SERVER (NATIVE JAVA 22)
echo ===================================================================
java -server -Xms128M -Xmx1024M -XX:+UseG1GC --add-opens java.base/jdk.internal.misc=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true "-Dfile.encoding=UTF-8" -cp "build\classes;dist\NROK.jar;lib\*" server.ServerManager

if %errorlevel% neq 0 (
    echo.
    echo [LOI] Server dung voi ma loi %errorlevel%
    pause
)

:END