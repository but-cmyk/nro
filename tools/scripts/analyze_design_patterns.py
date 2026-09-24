import os
import sys
import re

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

def analyze_java_file(filepath):
    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
        lines = f.readlines()
    
    total_lines = len(lines)
    method_count = 0
    max_switch_cases = 0
    current_switch_cases = 0
    in_switch = False
    brace_depth = 0
    max_nesting = 0
    current_nesting = 0
    public_static_fields = 0

    method_pattern = re.compile(r'^\s*(?:public|protected|private|static|\s)+[\w\<\>\[\]]+\s+(\w+)\s*\([^)]*\)\s*(?:throws\s+[\w,\s]+)?\s*\{?')
    field_pattern = re.compile(r'^\s*public\s+static\s+(?!final\b)[\w\<\>\[\]]+\s+\w+')

    for line in lines:
        stripped = line.strip()
        
        # Check public static non-final field
        if field_pattern.match(stripped) and not '(' in stripped:
            public_static_fields += 1

        # Check method declaration
        if method_pattern.match(stripped) and not stripped.startswith("if") and not stripped.startswith("while") and not stripped.startswith("for") and not stripped.startswith("switch"):
            if not stripped.endswith(";"):
                method_count += 1

        # Nesting depth calculation
        opens = line.count('{')
        closes = line.count('}')
        current_nesting += (opens - closes)
        if current_nesting > max_nesting:
            max_nesting = current_nesting

        # Switch cases detection
        if "switch (" in line or "switch(" in line:
            current_switch_cases = 0
            in_switch = True
        elif in_switch:
            if stripped.startswith("case ") or stripped.startswith("default:"):
                current_switch_cases += 1
                if current_switch_cases > max_switch_cases:
                    max_switch_cases = current_switch_cases
            if current_nesting <= 1:
                in_switch = False

    # Calculate Code Smell Score:
    # LOC * 0.1 + methods * 2 + max_switch_cases * 3 + public_static * 4 + max_nesting * 5
    smell_score = (total_lines * 0.1) + (method_count * 2) + (max_switch_cases * 3) + (public_static_fields * 4) + (max_nesting * 5)

    return {
        "path": filepath,
        "filename": os.path.basename(filepath),
        "loc": total_lines,
        "methods": method_count,
        "max_switch": max_switch_cases,
        "public_static": public_static_fields,
        "max_nesting": max_nesting,
        "score": round(smell_score, 1)
    }

def main():
    print("==================================================================")
    print("      NRO CODEBASE DESIGN PATTERN & CODE SMELL ANALYZER           ")
    print("==================================================================")
    
    java_results = []
    for root, dirs, files in os.walk("src"):
        for f in files:
            if f.endswith(".java"):
                p = os.path.join(root, f)
                try:
                    res = analyze_java_file(p)
                    java_results.append(res)
                except Exception as e:
                    pass

    java_results.sort(key=lambda x: x["score"], reverse=True)

    print(f"\n[SERVER JAVA] Scanned {len(java_results)} Java files.")
    print("TOP 10 GOD CLASSES & GIANT SWITCH-CASES CẦN TÁI CẤU TRÚC (PRIORITY RANKING):")
    print("-" * 95)
    print(f"{'No':<4}{'Filename':<28}{'LOC':<8}{'Methods':<10}{'MaxCases':<10}{'PubStatic':<11}{'Nesting':<9}{'SmellScore'}")
    print("-" * 95)
    
    for i, r in enumerate(java_results[:10], 1):
        print(f"{i:<4}{r['filename']:<28}{r['loc']:<8}{r['methods']:<10}{r['max_switch']:<10}{r['public_static']:<11}{r['max_nesting']:<9}{r['score']}")

    print("-" * 95)

    # 2. SCAN CLIENT UNITY C#
    cs_path = r"Client/Client/Assets/Scripts"
    if os.path.exists(cs_path):
        cs_results = []
        for root, dirs, files in os.walk(cs_path):
            for f in files:
                if f.endswith(".cs"):
                    p = os.path.join(root, f)
                    try:
                        res = analyze_java_file(p) # cấu trúc C# tương đương Java
                        cs_results.append(res)
                    except Exception as e:
                        pass
        cs_results.sort(key=lambda x: x["score"], reverse=True)
        print(f"\n[CLIENT UNITY C#] Scanned {len(cs_results)} C# files.")
        print("TOP 10 CLIENT GOD CLASSES (PRIORITY RANKING):")
        print("-" * 95)
        print(f"{'No':<4}{'Filename':<28}{'LOC':<8}{'Methods':<10}{'MaxCases':<10}{'PubStatic':<11}{'Nesting':<9}{'SmellScore'}")
        print("-" * 95)
        for i, r in enumerate(cs_results[:10], 1):
            print(f"{i:<4}{r['filename']:<28}{r['loc']:<8}{r['methods']:<10}{r['max_switch']:<10}{r['public_static']:<11}{r['max_nesting']:<9}{r['score']}")
        print("-" * 95)

if __name__ == '__main__':
    main()
