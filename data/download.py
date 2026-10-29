"""
Data ingestion and SHA-256 integrity verification for LIT-PCBA ALDH1 (AID 1030).
"""

import hashlib
import json
import sys
from pathlib import Path

def compute_sha256(file_path: Path) -> str:
    h = hashlib.sha256()
    with open(file_path, "rb") as f:
        while chunk := f.read(65536):
            h.update(chunk)
    return h.hexdigest()

def verify_data_integrity(data_dir: Path = None) -> bool:
    if data_dir is None:
        data_dir = Path(__file__).resolve().parent
    
    manifest_path = data_dir / "MANIFEST.json"
    if not manifest_path.exists():
        print(f"Error: Manifest not found at {manifest_path}", file=sys.stderr)
        return False
        
    with open(manifest_path, "r", encoding="utf-8") as f:
        manifest = json.load(f)
        
    repo_root = data_dir.parent
    all_ok = True
    
    print("=" * 60)
    print("IRIS DATA INTEGRITY AUDIT (LIT-PCBA AID 1030)")
    print("=" * 60)
    
    for filename, meta in manifest.get("files", {}).items():
        file_path = repo_root / meta["path"]
        expected_sha = meta["sha256"]
        
        if not file_path.exists():
            print(f"[MISSING] {filename}: Not found at {file_path}")
            all_ok = False
            continue
            
        actual_sha = compute_sha256(file_path)
        actual_size = file_path.stat().st_size
        
        if actual_sha == expected_sha:
            print(f"[VERIFIED] {filename}")
            print(f"  Path:    {meta['path']}")
            print(f"  SHA-256: {actual_sha}")
            print(f"  Size:    {actual_size} bytes (matches manifest)")
        else:
            print(f"[FAILED] {filename}: Hash mismatch!")
            print(f"  Expected: {expected_sha}")
            print(f"  Actual:   {actual_sha}")
            all_ok = False
            
    print("=" * 60)
    if all_ok:
        print("ALL DATASETS AUTHENTICATED AND VERIFIED (PASS)")
    else:
        print("DATASET INTEGRITY FAILED (FAIL)", file=sys.stderr)
    print("=" * 60)
    return all_ok

if __name__ == "__main__":
    success = verify_data_integrity()
    sys.exit(0 if success else 1)
