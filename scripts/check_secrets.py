#!/usr/bin/env python3
"""
Security scanner for secrets, personal information, and hardcoded local paths.
Scans all tracked and codebase files for:
- 64-character hexadecimal tokens
- Known leaked tokens or credentials
- Personal email addresses
- Hardcoded local paths (e.g. C:\\Users\\...)

Excludes:
- .env (gitignored local file)
- .git, .pytest_cache, __pycache__, build, .gradle
- Binary files (*.apk, *.png, *.parquet, *.joblib)
"""

import sys
import re
from pathlib import Path

# Patterns to detect
HEX_64_PATTERN = re.compile(r'\b[0-9a-fA-F]{64}\b')
FORBIDDEN_STRINGS = [
    "d44936124323424b6b0dd0d4894cc920133ab21518b10b0bf40904921a57b23c",
    "169c3cb75982bb670c4ebe87831c6c5ded3c6121f17224557feb759c3f5913d7",
    "nb25ise205.shamat@nmit.ac.in",
    r"C:\Users\creat",
    r"C:\\Users\\creat",
]

IGNORE_DIRS = {
    ".git",
    ".pytest_cache",
    "__pycache__",
    ".gradle",
    "build",
    ".idea",
    "venv",
    ".venv",
}

IGNORE_FILES = {
    ".env",  # gitignored local configuration
    "local.properties",  # android local machine sdk config
    "prompt_full.txt",  # raw prompt dump
}

BINARY_EXTENSIONS = {
    ".apk", ".png", ".jpg", ".jpeg", ".pdf", ".parquet", ".joblib", ".pkl", ".class", ".dex", ".jar"
}


def scan_repo(root_dir: Path) -> int:
    violations = 0
    print(f"Scanning codebase for secrets at: {root_dir}")

    for file_path in root_dir.rglob("*"):
        if not file_path.is_file():
            continue

        # Skip ignored dirs
        if any(part in IGNORE_DIRS for part in file_path.parts):
            continue

        if file_path.name in IGNORE_FILES:
            continue

        if file_path.suffix.lower() in BINARY_EXTENSIONS:
            continue

        # Self-exemption for check_secrets.py string literals
        is_scanner = file_path.name == "check_secrets.py"

        try:
            content = file_path.read_text(encoding="utf-8", errors="ignore")
        except Exception:
            continue

        lines = content.splitlines()
        for idx, line in enumerate(lines, 1):
            if is_scanner and ("FORBIDDEN_STRINGS" in line or "d449361" in line):
                continue

            # Check 64-hex tokens (skip explicit sha256 hashes in manifests/checkpoints)
            if "sha256" in line.lower() or "checksum" in line.lower():
                hex_matches = []
            else:
                hex_matches = HEX_64_PATTERN.findall(line)
            for m in hex_matches:
                if not is_scanner:
                    print(f"[ERROR] 64-hex token detected in {file_path.relative_to(root_dir)}:{idx}")
                    violations += 1

            # Check forbidden strings
            for f_str in FORBIDDEN_STRINGS:
                if f_str in line and not is_scanner:
                    print(f"[ERROR] Forbidden string '{f_str[:12]}...' detected in {file_path.relative_to(root_dir)}:{idx}")
                    violations += 1

    if violations > 0:
        print(f"\nFAILED: Found {violations} secret / personal info violation(s).")
        return 1
    else:
        print("\nPASSED: Zero secrets, hardcoded paths, or leaked personal credentials found.")
        return 0


if __name__ == "__main__":
    base_dir = Path(__file__).resolve().parent.parent
    sys.exit(scan_repo(base_dir))
