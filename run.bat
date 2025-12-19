@echo off
title Ngoc Rong Server - Optimized RAM
echo Starting Ngoc Rong Server...

:: Cấu hình RAM tối ưu
set XMS=1024m
set XMX=2048m

:: Đường dẫn tuyệt đối đến Java 22 (SỬA LẠI CHO ĐÚNG MÁY BẠN)
set JAVA_EXEC="C:\Program Files\Java\jdk-22\bin\java.exe"

:: Chạy file jar
%JAVA_EXEC% -Xms%XMS% -Xmx%XMX% -XX:+UseG1GC -Dfile.encoding=UTF-8 -jar dist\NROK.jar

pause