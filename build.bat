@echo off
chcp 65001 > nul
echo =======================================================
echo      BUILD NRO SERVER - JAVA 21 LTS ^& NETTY 4.X
echo =======================================================

if not exist "build\classes" mkdir "build\classes"
if not exist "dist" mkdir "dist"

echo [1/3] Gathering dependencies and Java source files...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$jars = (Get-ChildItem -Path lib\*.jar | ForEach-Object { 'lib/' + $_.Name }) -join ';'; $opts = @('-encoding', 'UTF-8', '-cp', ('build/classes;' + $jars), '-d', 'build/classes', '--release', '21'); [System.IO.File]::WriteAllLines('options.txt', $opts, [System.Text.UTF8Encoding]::new($false)); [System.IO.File]::WriteAllLines('sources.txt', (Get-ChildItem -Path src\*.java -Recurse | Select-Object -ExpandProperty FullName), [System.Text.UTF8Encoding]::new($false));"

echo [2/3] Compiling source code with javac...
javac @options.txt @sources.txt

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)

echo [3/3] Packaging dist\NROK.jar...
jar --create --file dist\NROK.jar --main-class server.ServerManager -C build\classes .

echo.
echo =======================================================
echo [SUCCESS] BUILD COMPLETED 100%%!
echo Artifact: dist\NROK.jar
echo =======================================================
