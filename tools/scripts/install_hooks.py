"""
Cài đặt Git pre-commit hook cho dự án NRO_219.
Hook sẽ tự động chạy guardrails & compile_check trước mỗi lần commit.
"""
import os
import stat

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
HOOK_PATH = os.path.join(ROOT_DIR, ".git", "hooks", "pre-commit")

HOOK_SCRIPT = """#!/bin/sh
echo "[NRO Pre-Commit Gate] Running guardrails & compile verification..."

python tools/memory/guardrails.py --check-all
if [ $? -ne 0 ]; then
    echo "[BLOCKED] Guardrails check failed! Fix violations before committing."
    exit 1
fi

python tools/compile_check.py
if [ $? -ne 0 ]; then
    echo "[BLOCKED] Compile check failed! Fix build errors before committing."
    exit 1
fi

echo "[NRO Pre-Commit Gate] All checks passed!"
exit 0
"""


def main():
    hooks_dir = os.path.dirname(HOOK_PATH)
    if not os.path.exists(hooks_dir):
        print(f"[ERROR] .git/hooks not found at {hooks_dir}")
        return
    with open(HOOK_PATH, "w", encoding="utf-8", newline="\n") as f:
        f.write(HOOK_SCRIPT)
    try:
        st = os.stat(HOOK_PATH)
        os.chmod(HOOK_PATH, st.st_mode | stat.S_IEXEC)
    except Exception:
        pass
    print(f"[OK] Installed Git pre-commit hook at: {HOOK_PATH}")


if __name__ == "__main__":
    main()
