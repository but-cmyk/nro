"""
Automated Test Runner cho dự án NRO_219 (Server Java & Client C#).
- Biên dịch & chạy các bài test Java (không cần MySQL/Redis thật) với timeout chống treo JVM.
- Biên dịch & chạy Client C# Protocol Contract Test (nếu có Roslyn + Unity assemblies trên Windows).
"""
import os
import sys
import glob
import shutil
import tempfile
import subprocess

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
TIMEOUT_SEC = 20

ROSLYN_CSC_PATHS = [
    r"C:\Program Files\dotnet\sdk\10.0.300\Roslyn\bincore\csc.dll",
    r"C:\Program Files\dotnet\sdk\9.0.306\Roslyn\bincore\csc.dll",
]
UNITY_BASE = r"C:\Program Files\Unity\Hub\Editor\2022.3.61f1\Editor\Data"


def find_roslyn_csc():
    for p in ROSLYN_CSC_PATHS:
        if os.path.exists(p):
            return p
    sdk_dir = r"C:\Program Files\dotnet\sdk"
    if os.path.exists(sdk_dir):
        for root, _, files in os.walk(sdk_dir):
            if "csc.dll" in files:
                return os.path.join(root, "csc.dll")
    return None


def run_java_tests():
    print("=" * 60)
    print("1. RUNNING JAVA SERVER TESTS (SECURITY & PROTOCOL CONTRACT)")
    print("=" * 60)

    jars = glob.glob(os.path.join(ROOT_DIR, "lib", "*.jar"))
    cp_sep = ";" if os.name == "nt" else ":"
    classpath_lib = cp_sep.join(jars)

    java_sources = []
    for root, _, files in os.walk(os.path.join(ROOT_DIR, "src")):
        for f in files:
            if f.endswith(".java"):
                java_sources.append(os.path.abspath(os.path.join(root, f)))

    test_files = [
        os.path.join(ROOT_DIR, "tools", "test", "TestP0SecurityFixes.java"),
        os.path.join(ROOT_DIR, "tools", "test", "TestProtocolContract.java"),
    ]
    java_sources.extend(test_files)

    out_dir = tempfile.mkdtemp(prefix="nro_java_test_")
    arg_file = os.path.join(out_dir, "sources.txt")
    try:
        with open(arg_file, "w", encoding="utf-8") as f:
            for src in java_sources:
                src_posix = src.replace("\\", "/")
                f.write(f'"{src_posix}"\n')

        compile_cmd = [
            "javac",
            "-encoding", "UTF-8",
            "--release", "21",
            "-cp", classpath_lib,
            "-d", out_dir,
            f"@{arg_file}",
        ]
        res = subprocess.run(compile_cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")
        if res.returncode != 0:
            print("[FAIL] Java test compilation failed:")
            print(res.stdout)
            print(res.stderr)
            return False

        run_cp = f"{out_dir}{cp_sep}{classpath_lib}"
        test_classes = ["test.TestP0SecurityFixes", "test.TestProtocolContract"]
        for cls in test_classes:
            print(f"\n-> Executing {cls} (timeout={TIMEOUT_SEC}s)...")
            try:
                t_res = subprocess.run(
                    ["java", "-cp", run_cp, cls],
                    cwd=ROOT_DIR,
                    capture_output=True,
                    text=True,
                    encoding="utf-8",
                    errors="replace",
                    timeout=TIMEOUT_SEC,
                )
            except subprocess.TimeoutExpired:
                print(f"[FAIL] {cls} timed out after {TIMEOUT_SEC}s (JVM hang detected)!")
                return False

            if t_res.stdout:
                print(t_res.stdout.rstrip())
            if t_res.returncode != 0:
                print(f"[FAIL] {cls} exited with code {t_res.returncode}")
                if t_res.stderr:
                    print(t_res.stderr.rstrip())
                return False

        return True
    finally:
        shutil.rmtree(out_dir, ignore_errors=True)


def run_client_tests():
    print("\n" + "=" * 60)
    print("2. RUNNING C# CLIENT PROTOCOL CONTRACT TESTS")
    print("=" * 60)

    if os.name != "nt":
        print("[SKIP] Client C# runtime test execution requires Windows .NET Framework.")
        return True

    csc_dll = find_roslyn_csc()
    if not csc_dll or not os.path.exists(UNITY_BASE):
        print("[SKIP] Roslyn csc.dll or Unity assemblies not found.")
        return True

    unity_dirs = [
        os.path.join(UNITY_BASE, "Managed", "UnityEngine"),
        os.path.join(UNITY_BASE, "Managed"),
        os.path.join(UNITY_BASE, "UnityReferenceAssemblies", "unity-4.8-api"),
        os.path.join(UNITY_BASE, "UnityReferenceAssemblies", "unity-4.8-api", "Facades"),
    ]
    refs = set()
    for d in unity_dirs:
        if os.path.exists(d):
            for f in os.listdir(d):
                if f.endswith(".dll"):
                    refs.add(os.path.abspath(os.path.join(d, f)))

    client_lib_dir = os.path.join(ROOT_DIR, "Client", "Client", "Library", "ScriptAssemblies")
    if os.path.exists(client_lib_dir):
        for f in os.listdir(client_lib_dir):
            if f.endswith(".dll") and f != "Assembly-CSharp.dll":
                refs.add(os.path.abspath(os.path.join(client_lib_dir, f)))

    # Compile minimal pure-C# protocol files needed by ClientProtocolTest.cs
    # (NetworkOpcodes, myWriter, myReader) OR full client library -> exe
    out_dir = tempfile.mkdtemp(prefix="nro_cs_test_")
    try:
        lib_dll = os.path.join(out_dir, "ClientTestLib.dll")
        rsp_lib = os.path.join(out_dir, "lib.rsp")
        cs_files = []
        scripts_dir = os.path.join(ROOT_DIR, "Client", "Client", "Assets", "Scripts")
        for root, _, files in os.walk(scripts_dir):
            for f in files:
                if f.endswith(".cs"):
                    cs_files.append(os.path.abspath(os.path.join(root, f)))

        with open(rsp_lib, "w", encoding="utf-8") as rsp:
            rsp.write("/target:library\n")
            rsp.write(f'/out:"{lib_dll}"\n')
            rsp.write("/langversion:9.0\n")
            rsp.write("/nowarn:0169,0649,0414,0219\n")
            rsp.write("/define:UNITY_2022_3_OR_NEWER;UNITY_2022_3;UNITY_EDITOR\n")
            for r in sorted(refs):
                rsp.write(f'/r:"{r}"\n')
            for s in cs_files:
                rsp.write(f'"{s}"\n')

        c1 = subprocess.run(["dotnet", csc_dll, f"@{rsp_lib}"], capture_output=True, text=True, encoding="utf-8", errors="replace")
        if c1.returncode != 0:
            print("[FAIL] Client library compile failed:")
            print(c1.stdout)
            return False

        exe_path = os.path.join(out_dir, "ClientProtocolTest.exe")
        rsp_exe = os.path.join(out_dir, "exe.rsp")
        test_cs = os.path.join(ROOT_DIR, "tools", "test", "ClientProtocolTest.cs")
        with open(rsp_exe, "w", encoding="utf-8") as rsp:
            rsp.write("/target:exe\n")
            rsp.write(f'/out:"{exe_path}"\n')
            rsp.write("/langversion:9.0\n")
            rsp.write(f'/r:"{lib_dll}"\n')
            for r in sorted(refs):
                rsp.write(f'/r:"{r}"\n')
            rsp.write(f'"{test_cs}"\n')

        c2 = subprocess.run(["dotnet", csc_dll, f"@{rsp_exe}"], capture_output=True, text=True, encoding="utf-8", errors="replace")
        if c2.returncode != 0:
            print("[FAIL] ClientProtocolTest.cs compile failed:")
            print(c2.stdout)
            return False

        t_res = subprocess.run([exe_path], capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=TIMEOUT_SEC)
        if t_res.stdout:
            print(t_res.stdout.rstrip())
        if t_res.returncode != 0:
            print(f"[FAIL] ClientProtocolTest.exe exited with code {t_res.returncode}")
            if t_res.stderr:
                print(t_res.stderr.rstrip())
            return False
        return True
    finally:
        shutil.rmtree(out_dir, ignore_errors=True)


def main():
    java_only = "--java" in sys.argv
    client_only = "--client" in sys.argv

    ok = True
    if not client_only:
        ok = run_java_tests() and ok
    if not java_only:
        ok = run_client_tests() and ok

    print("\n" + "=" * 60)
    if ok:
        print("[ALL TESTS PASSED] ✅")
        sys.exit(0)
    else:
        print("[TESTS FAILED] ❌")
        sys.exit(1)


if __name__ == "__main__":
    main()
