"""
Secrets Scanner: Scans qml_vs_classical/ for raw tokens, API keys, and 64-hex strings.
Fails with exit code 1 if any potential secret is found in tracked files.
"""

import os
import re
import sys
from pathlib import Path

# 64-hex pattern (standard API token format)
HEX64_REGEX = re.compile(r'\b[a-fA-F0-9]{64}\b')
KEY_PATTERNS = [
    re.compile(r'(?i)(api[_-]?key|token|secret)\s*[:=]\s*["\'][a-zA-Z0-9_\-]{16,}["\']'),
]

IGNORE_EXTENSIONS = {'.pyc', '.png', '.pdf', '.jpg', '.parquet', '.npy', '.npz'}
IGNORE_FILENAMES = {'.env', 'check_secrets.py'}

def scan_directory(base_dir: Path) -> int:
    issues = 0
    print(f"Scanning {base_dir} for secrets and credential leaks...")
    
    for path in base_dir.rglob('*'):
        if path.is_dir():
            if path.name in {'.git', '__pycache__', 'cache', '.pytest_cache'}:
                continue
            continue
        if path.suffix in IGNORE_EXTENSIONS or path.name in IGNORE_FILENAMES:
            continue
        
        try:
            content = path.read_text(encoding='utf-8', errors='ignore')
        except Exception:
            continue
            
        # Check for 64-hex strings
        matches = HEX64_REGEX.findall(content)
        if matches:
            for m in matches:
                print(f"[FAIL] Hardcoded 64-hex string found in {path.relative_to(base_dir)}: {m[:6]}...{m[-4:]}")
                issues += 1
                
        # Check for key-assignment patterns (excluding placeholder values)
        for pattern in KEY_PATTERNS:
            for match in pattern.finditer(content):
                matched_str = match.group(0)
                if any(placeholder in matched_str.lower() for placeholder in ["your_", "example", "placeholder", "xxx"]):
                    continue
                print(f"[FAIL] Potential secret assignment in {path.relative_to(base_dir)}: {matched_str[:25]}...")
                issues += 1

    if issues > 0:
        print(f"\nAudit failed! Found {issues} potential secret leak(s).")
        return 1
    else:
        print("\nAudit passed! Clean repository: no hardcoded secrets or raw tokens detected.")
        return 0

if __name__ == "__main__":
    folder = Path(__file__).resolve().parent.parent
    sys.exit(scan_directory(folder))
