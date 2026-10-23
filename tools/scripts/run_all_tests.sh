#!/usr/bin/env bash
set -euo pipefail

echo "======================================================="
echo "   CHAY TOAN BO TEST SUITES NRO (CI & REGRESSION)"
echo "======================================================="

# 1. Kiem tra Guardrails
echo -e "\n[1/6] Running Memory Guardrails & Invariant Checks..."
python3 tools/memory/guardrails.py --check-all

# 2. Chuan bi classpath va thu muc build
mkdir -p build/test-classes
SEP=":"
if [[ "${OSTYPE:-}" == "msys"* || "${OSTYPE:-}" == "win32"* || "${OSTYPE:-}" == "cygwin"* ]]; then
    SEP=";"
fi
CP="dist/NROK.jar${SEP}lib/*"

# 3. Bien dich test suites
echo -e "\n[2/6] Compiling Automated Test Suites..."
javac -encoding UTF-8 -cp "$CP" -d build/test-classes \
    test/security/TestSecurityFixes.java \
    tools/test/TestGroupBIntegrity.java \
    tools/test/TestAuditAndShutdown.java \
    tools/test/TestGroupD.java \
    tools/test/TestPacketValidationAndFuzz.java \
    tools/test/TestMetricsAndHealthEndpoint.java \
    tools/test/TestZoneSerialExecutor.java

# 4. Chay tung bo test
echo -e "\n[3/7] Running Auth & Password Security Tests..."
java -cp "build/test-classes${SEP}$CP" TestSecurityFixes

echo -e "\n[4/7] Running Group B (Trade, Item, Economy Cap) Tests..."
java -cp "build/test-classes${SEP}$CP" test.TestGroupBIntegrity

echo -e "\n[5/7] Running Group A5 & C1 (Admin Audit, Idempotent Shutdown) Tests..."
java -cp "build/test-classes${SEP}$CP" test.TestAuditAndShutdown

echo -e "\n[6/7] Running Group C2 (Zone Single-Writer SerialExecutor) Tests..."
java -cp "build/test-classes${SEP}$CP" test.TestZoneSerialExecutor

echo -e "\n[7/7] Running Group D, E1 & F (Bounded Cache, Decoder Fuzz, Health & Metrics) Tests..."
java -cp "build/test-classes${SEP}$CP" test.TestGroupD
java -cp "build/test-classes${SEP}$CP" test.TestPacketValidationAndFuzz
java -cp "build/test-classes${SEP}$CP" test.TestMetricsAndHealthEndpoint

echo -e "\n======================================================="
echo "   [SUCCESS] TAT CA TEST SUITES DA PASS 100% TREN CI!"
echo "======================================================="
