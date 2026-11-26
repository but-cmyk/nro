"""
Script kiểm tra lỗi biên dịch (Compile Check) toàn diện cho Project NRO:
1. Client Unity C# (Sử dụng Roslyn C# 9.0 Compiler với Unity 2022.3 assemblies)
2. Server Java (Sử dụng javac với lib/*.jar và Java 21)
"""
import os
import sys
import glob
import subprocess
import tempfile

ROSLYN_CSC_PATHS = [
    r"C:\Program Files\dotnet\sdk\10.0.300\Roslyn\bincore\csc.dll",
    r"C:\Program Files\dotnet\sdk\9.0.306\Roslyn\bincore\csc.dll",
]

UNITY_BASE = r"C:\Program Files\Unity\Hub\Editor\2022.3.61f1\Editor\Data"

UNITY_MANAGED_DIRS = [
    os.path.join(UNITY_BASE, "Managed", "UnityEngine"),
    os.path.join(UNITY_BASE, "Managed"),
    os.path.join(UNITY_BASE, "UnityReferenceAssemblies", "unity-4.8-api"),
    os.path.join(UNITY_BASE, "UnityReferenceAssemblies", "unity-4.8-api", "Facades"),
]

CLIENT_SCRIPTS_DIR = os.path.join("Client", "Client", "Assets", "Scripts")
CLIENT_LIB_DIR = os.path.join("Client", "Client", "Library", "ScriptAssemblies")


def find_roslyn_csc():
    for p in ROSLYN_CSC_PATHS:
        if os.path.exists(p):
            return p
    # Search in dotnet sdk
    sdk_dir = r"C:\Program Files\dotnet\sdk"
    if os.path.exists(sdk_dir):
        for root, dirs, files in os.walk(sdk_dir):
            if "csc.dll" in files:
                return os.path.join(root, "csc.dll")
    return None


def check_client_compile(verbose=False):
    print("=" * 60)
    print("1. KIỂM TRA BIÊN DỊCH CLIENT UNITY C#")
    print("=" * 60)

    csc_dll = find_roslyn_csc()
    if not csc_dll:
        print("[FAIL] Không tìm thấy Roslyn csc.dll trên hệ thống!")
        return False

    if not os.path.exists(CLIENT_SCRIPTS_DIR):
        print(f"[SKIP] Không tìm thấy thư mục {CLIENT_SCRIPTS_DIR}")
        return True

    # Thu thập tất cả file .cs
    cs_files = []
    for root, dirs, files in os.walk(CLIENT_SCRIPTS_DIR):
        for f in files:
            if f.endswith(".cs"):
                cs_files.append(os.path.abspath(os.path.join(root, f)))

    print(f"-> Đã quét được {len(cs_files)} tệp mã nguồn C#.")

    # Thu thập các thư viện tham chiếu
    refs = set()

    for d in UNITY_MANAGED_DIRS:
        if os.path.exists(d):
            for f in os.listdir(d):
                if f.endswith(".dll"):
                    refs.add(os.path.abspath(os.path.join(d, f)))

    if os.path.exists(CLIENT_LIB_DIR):
        for f in os.listdir(CLIENT_LIB_DIR):
            if f.endswith(".dll") and f != "Assembly-CSharp.dll":
                refs.add(os.path.abspath(os.path.join(CLIENT_LIB_DIR, f)))

    print(f"-> Đã nạp {len(refs)} assemblies tham chiếu (UnityEngine, .NET 4.8 Facades & plugins).")

    out_dll = os.path.join(tempfile.gettempdir(), "test_assembly_csharp.dll")
    with tempfile.NamedTemporaryFile("w", encoding="utf-8", delete=False, suffix=".rsp") as rsp:
        rsp_path = rsp.name
        rsp.write("/target:library\n")
        rsp.write(f'/out:"{out_dll}"\n')
        rsp.write("/langversion:9.0\n")
        rsp.write("/nowarn:0169,0649,0414,0219\n")
        rsp.write("/define:UNITY_2022_3_OR_NEWER;UNITY_2022_3;UNITY_EDITOR\n")
        for r in sorted(refs):
            rsp.write(f'/r:"{r}"\n')
        for s in cs_files:
            rsp.write(f'"{s}"\n')

    try:
        cmd = ["dotnet", csc_dll, f"@{rsp_path}"]
        res = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")

        if os.path.exists(out_dll):
            try:
                os.remove(out_dll)
            except Exception:
                pass

        if res.returncode == 0:
            print("[PASS] ✅ Client C# biên dịch thành công! (0 lỗi biên dịch)")
            return True
        else:
            print("[FAIL] ❌ Phát hiện lỗi biên dịch Client C#:")
            errors = []
            for line in res.stdout.splitlines():
                line_str = line.strip()
                if "error CS" in line_str:
                    errors.append(line_str)
                    print(f"  🔴 {line_str}")
            if not errors:
                for line in res.stdout.splitlines()[:25]:
                    print(f"  {line}")
            return False
    finally:
        if os.path.exists(rsp_path):
            try:
                os.remove(rsp_path)
            except Exception:
                pass


def check_server_compile():
    print("\n" + "=" * 60)
    print("2. KIỂM TRA BIÊN DỊCH SERVER JAVA")
    print("=" * 60)

    src_dir = "src"
    lib_dir = "lib"
    if not os.path.exists(src_dir):
        print(f"[SKIP] Không tìm thấy thư mục {src_dir}")
        return True

    # Thu thập jar
    jars = []
    if os.path.exists(lib_dir):
        for f in os.listdir(lib_dir):
            if f.endswith(".jar"):
                jars.append(os.path.abspath(os.path.join(lib_dir, f)))
    cp = ";".join(jars) if jars else "."

    # Lấy danh sách .java (dùng forward slash để tránh lỗi escape trong javac response file)
    java_files = []
    for root, dirs, files in os.walk(src_dir):
        for f in files:
            if f.endswith(".java"):
                p = os.path.abspath(os.path.join(root, f)).replace("\\", "/")
                java_files.append(p)

    print(f"-> Đã quét được {len(java_files)} tệp mã nguồn Java.")

    with tempfile.NamedTemporaryFile("w", encoding="utf-8", delete=False, suffix=".txt") as tf:
        tf_path = tf.name
        for jf in java_files:
            tf.write(f'"{jf}"\n')

    temp_out = tempfile.mkdtemp(prefix="nro_build_test_")
    try:
        cmd = ["javac", "-encoding", "UTF-8", "-cp", cp, "-d", temp_out, f"@{tf_path}"]
        res = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")

        if res.returncode == 0:
            print("[PASS] ✅ Server Java biên dịch thành công! (0 lỗi biên dịch)")
            return True
        else:
            print("[FAIL] ❌ Phát hiện lỗi biên dịch Server Java:")
            for line in (res.stderr or res.stdout).splitlines()[:25]:
                print(f"  🔴 {line.strip()}")
            return False
    finally:
        if os.path.exists(tf_path):
            try:
                os.remove(tf_path)
            except Exception:
                pass


def main():
    check_client = True
    check_server = True
    if "--client" in sys.argv:
        check_server = False
    elif "--server" in sys.argv:
        check_client = False

    client_ok = check_client_compile() if check_client else True
    server_ok = check_server_compile() if check_server else True

    print("\n" + "=" * 60)
    print("TỔNG KẾT KIỂM TRA BIÊN DỊCH")
    print("=" * 60)
    if client_ok and server_ok:
        print("🎉 [ALL CLEAR] Tất cả module kiểm tra đều BIÊN DỊCH THÀNH CÔNG (0 LỖI)!")
        sys.exit(0)
    else:
        print("💥 [FAILED] Vui lòng sửa các lỗi biên dịch ở trên trước khi báo cáo hoàn thành!")
        sys.exit(1)


if __name__ == "__main__":
    main()
