#requires -Version 5.1
$ErrorActionPreference = 'Stop'
$repoRoot = (git rev-parse --show-toplevel).Trim()
Set-Location $repoRoot

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "   CHAY TOAN BO TEST SUITES NRO (WINDOWS / LOCAL)" -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

# 1. Kiem tra Guardrails
Write-Host "`n[1/6] Running Memory Guardrails & Invariant Checks..." -ForegroundColor Yellow
python tools/memory/guardrails.py --check-all
if ($LASTEXITCODE -ne 0) { throw "Guardrails check failed" }

# 2. Chuan bi classpath va thu muc build
$out = 'build/test-classes'
New-Item -ItemType Directory -Force $out | Out-Null
$cp = "dist/NROK.jar;lib/*"

# 3. Bien dich test suites
Write-Host "`n[2/6] Compiling Automated Test Suites..." -ForegroundColor Yellow
javac -encoding UTF-8 -cp $cp -d $out `
    test/security/TestSecurityFixes.java `
    tools/test/TestGroupBIntegrity.java `
    tools/test/TestAuditAndShutdown.java `
    tools/test/TestGroupD.java `
    tools/test/TestPacketValidationAndFuzz.java `
    tools/test/TestHandshakeFraming.java `
    tools/test/TestMetricsAndHealthEndpoint.java `
    tools/test/TestZoneSerialExecutor.java
if ($LASTEXITCODE -ne 0) { throw "Test compilation failed" }

# 4. Chay tung bo test
Write-Host "`n[3/7] Running Auth & Password Security Tests..." -ForegroundColor Yellow
java -cp "$out;$cp" TestSecurityFixes
if ($LASTEXITCODE -ne 0) { throw "Security tests failed" }

Write-Host "`n[4/7] Running Group B (Trade, Item, Economy Cap) Tests..." -ForegroundColor Yellow
java -cp "$out;$cp" test.TestGroupBIntegrity
if ($LASTEXITCODE -ne 0) { throw "Group B tests failed" }

Write-Host "`n[5/7] Running Group A5 & C1 (Admin Audit, Idempotent Shutdown) Tests..." -ForegroundColor Yellow
java -cp "$out;$cp" test.TestAuditAndShutdown
if ($LASTEXITCODE -ne 0) { throw "Audit and Shutdown tests failed" }

Write-Host "`n[6/7] Running Group C2 (Zone Single-Writer SerialExecutor) Tests..." -ForegroundColor Yellow
java -cp "$out;$cp" test.TestZoneSerialExecutor
if ($LASTEXITCODE -ne 0) { throw "Zone SerialExecutor tests failed" }

Write-Host "`n[7/7] Running Group D, E1 & F (Bounded Cache, Decoder Fuzz, Health & Metrics) Tests..." -ForegroundColor Yellow
java -cp "$out;$cp" test.TestGroupD
if ($LASTEXITCODE -ne 0) { throw "Group D tests failed" }

java -cp "$out;$cp" test.TestPacketValidationAndFuzz
if ($LASTEXITCODE -ne 0) { throw "Packet Validation and Fuzz tests failed" }

Write-Host "`n[*] Running Handshake Framing Regression Test..." -ForegroundColor Yellow
java -cp "$out;$cp" test.TestHandshakeFraming
if ($LASTEXITCODE -ne 0) { throw "Handshake framing regression test failed" }

java -cp "$out;$cp" test.TestMetricsAndHealthEndpoint
if ($LASTEXITCODE -ne 0) { throw "Group F Health & Metrics tests failed" }

Write-Host "`n=======================================================" -ForegroundColor Green
Write-Host "   [SUCCESS] TAT CA TEST SUITES DA PASS 100%!" -ForegroundColor Green
Write-Host "=======================================================" -ForegroundColor Green
