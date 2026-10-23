@echo off
chcp 65001 > nul
echo =======================================================
echo      BUILD NRO SERVER - JAVA 21 LTS ^& NETTY 4.X
echo =======================================================

if not exist "build\classes" mkdir "build\classes"
if not exist "dist" mkdir "dist"

echo [1/3] Gathering dependencies and Java source files...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$jars = (Get-ChildItem -Path lib\*.jar | ForEach-Object { 'lib/' + $_.Name }) -join ';'; $opts = @('-encoding', 'UTF-8', '-cp', ('build/classes;' + $jars), '-d', 'build/classes', '--release', '21'); [System.IO.File]::WriteAllLines('build/options.txt', $opts, [System.Text.UTF8Encoding]::new($false)); [System.IO.File]::WriteAllLines('build/sources.txt', (Get-ChildItem -Path src\*.java -Recurse | Select-Object -ExpandProperty FullName), [System.Text.UTF8Encoding]::new($false));"

echo [2/3] Compiling source code with javac...
javac @build/options.txt @build/sources.txt

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)

echo [3/3] Packaging dist\NROK.jar...
if exist "dist\NROK.jar" del /f /q "dist\NROK.jar" > nul 2>&1
jar --create --file dist\NROK.jar --main-class server.ServerManager -C build\classes .

if exist "build\options.txt" del /f /q "build\options.txt" > nul 2>&1
if exist "build\sources.txt" del /f /q "build\sources.txt" > nul 2>&1

echo.
echo =======================================================
echo [SUCCESS] BUILD COMPLETED 100%%!
echo Artifact: dist\NROK.jar
echo =======================================================
