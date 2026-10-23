#requires -Version 5.1
$ErrorActionPreference = 'Stop'
$repoRoot = (git rev-parse --show-toplevel).Trim()
Set-Location $repoRoot

if (-not (Test-Path 'build/classes')) {
    throw "build/classes not found. Run build.bat first."
}

$jars = (Get-ChildItem lib\*.jar | ForEach-Object { 'lib\' + $_.Name }) -join ';'
$out = 'build/security-test'
New-Item -ItemType Directory -Force $out | Out-Null

javac -encoding UTF-8 -cp "build/classes;$jars" -d $out test/security/TestSecurityFixes.java
if ($LASTEXITCODE -ne 0) { throw "Test compilation failed" }

java -cp "$out;build/classes;$jars" TestSecurityFixes
if ($LASTEXITCODE -ne 0) { throw "Security checks failed" }
