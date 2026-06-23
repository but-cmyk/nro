@echo off
title Ngoc Rong Server - Optimized RAM
chcp 65001 > nul
echo.
echo ============================================
echo      NGOC RONG SERVER - STARTING...
echo ============================================
echo.

:: =============================================
:: CẤU HÌNH RAM
:: Xms thấp = JVM chỉ xin RAM khi thực sự cần
:: (giống cách NetBeans chạy → RAM ban đầu thấp)
:: Xmx là giới hạn tối đa, không đổi
:: =============================================
set XMS=64m
set XMX=2048m

:: =============================================
:: TỰ ĐỘNG TÌM JAVA
:: =============================================
set JAVA_EXEC=""

where java >nul 2>&1
if %errorlevel% == 0 (
    set JAVA_EXEC=java
    goto :found_java
)

for /d %%i in (
    "C:\Program Files\Java\jdk*"
    "C:\Program Files\Java\jre*"
    "C:\Program Files\Eclipse Adoptium\jdk*"
    "C:\Program Files\Microsoft\jdk*"
    "C:\Program Files\Amazon Corretto\*"
) do (
    if exist "%%i\bin\java.exe" (
        set JAVA_EXEC="%%i\bin\java.exe"
        goto :found_java
    )
)

echo [LOI] Khong tim thay Java!
echo Download: https://adoptium.net
echo.
pause
exit /b 1

:found_java
echo [OK] Java: %JAVA_EXEC%

:: =============================================
:: KIỂM TRA FILE JAR
:: =============================================
if not exist "dist\NROK.jar" (
    echo [LOI] Khong tim thay dist\NROK.jar
    pause
    exit /b 1
)

echo [OK] dist\NROK.jar
echo [OK] RAM: %XMS% (khoi dau) ~ %XMX% (toi da)
echo [OK] Thread stack: 256k (giam tu 512k mac dinh)
echo.
echo Server dang khoi dong...
echo ============================================
echo.

:: =============================================
:: CHẠY SERVER
:: Ghi chú từng flag:
::   -Xms64m                     → khởi đầu 64MB như NetBeans, tăng dần khi cần
::   -Xmx2048m                   → giới hạn tối đa 2GB
::   -Xss256k                    → giảm stack mỗi thread: 512k→256k (~18MB tiết kiệm với 75 threads)
::   -XX:+UseG1GC                 → GC tốt cho game server
::   -XX:MaxGCPauseMillis=200     → GC không giữ server quá 200ms
::   -XX:+ParallelRefProcEnabled  → GC song song
::   -XX:+DisableExplicitGC       → chặn gọi System.gc() từ code
::   -XX:+UseStringDeduplication  → gộp String trùng → tiết kiệm RAM đáng kể
::   -XX:G1HeapRegionSize=16m     → vùng heap lớn hơn, ít phân mảnh
::   -XX:InitiatingHeapOccupancyPercent=35 → GC chạy sớm, giải phóng RAM thường xuyên
::   -XX:+UnlockExperimentalVMOptions      → mở khóa tính năng thực nghiệm
::   -XX:G1NewSizePercent=20      → 20% heap cho vùng new gen
::   -XX:G1MaxNewSizePercent=40   → tối đa 40% heap cho new gen
:: =============================================
%JAVA_EXEC% ^
    -Xms%XMS% ^
    -Xmx%XMX% ^
    -Xss256k ^
    -XX:+UseG1GC ^
    -XX:MaxGCPauseMillis=200 ^
    -XX:+ParallelRefProcEnabled ^
    -XX:+DisableExplicitGC ^
    -XX:+UseStringDeduplication ^
    -XX:G1HeapRegionSize=16m ^
    -XX:InitiatingHeapOccupancyPercent=35 ^
    -XX:+UnlockExperimentalVMOptions ^
    -XX:G1NewSizePercent=20 ^
    -XX:G1MaxNewSizePercent=40 ^
    -Dfile.encoding=UTF-8 ^
    -jar dist\NROK.jar

:: =============================================
:: KHI SERVER TẮT
:: =============================================
echo.
if %errorlevel% == 0 (
    echo [OK] Server da tat binh thuong.
) else (
    echo [LOI] Server tat voi ma loi: %errorlevel%
)
echo.
pause