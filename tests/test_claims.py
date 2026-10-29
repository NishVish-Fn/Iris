#!/usr/bin/env python3
"""
Test Claims Traceability (Section 0 Rule 1)
Reads claims.yaml and ensures every documented number matches its source file in results/.
"""

import sys
import json
from pathlib import Path
import yaml
import pytest

ROOT = Path(__file__).resolve().parent.parent


def get_nested_val(data, path_str):
    parts = path_str.split(".")
    curr = data
    for p in parts:
        curr = curr[p]
    return curr


def test_all_claims_traceability():
    claims_file = ROOT / "claims.yaml"
    assert claims_file.exists(), f"claims.yaml missing at {claims_file}"

    with open(claims_file, "r", encoding="utf-8") as f:
        manifest = yaml.safe_load(f)

    claims = manifest.get("claims", [])
    assert len(claims) > 0, "No claims listed in claims.yaml"

    failures = []
    print(f"\nVerifying {len(claims)} documented claims against raw results...")

    for c in claims:
        cid = c["id"]
        expected_val = c["value"]
        rel_path = c["source_file"]
        json_path = c["json_path"]

        src_file = ROOT / rel_path
        if not src_file.exists():
            failures.append(f"Claim '{cid}': Source file {rel_path} does not exist.")
            continue

        with open(src_file, "r", encoding="utf-8") as f:
            data = json.load(f)

        try:
            actual_val = get_nested_val(data, json_path)
        except Exception as e:
            failures.append(f"Claim '{cid}': Path '{json_path}' not found in {rel_path}: {e}")
            continue

        # Check numeric or boolean equality
        if isinstance(expected_val, (int, float)) and isinstance(actual_val, (int, float)):
            match = abs(expected_val - actual_val) < 1e-4
        else:
            match = (expected_val == actual_val)

        if not match:
            failures.append(f"Claim '{cid}': Expected {expected_val}, but found {actual_val} in {rel_path} at '{json_path}'")
        else:
            print(f"  [PASS] {cid}: {actual_val} == {expected_val}")

    if failures:
        pytest.fail("\n".join(failures))


if __name__ == "__main__":
    test_all_claims_traceability()
    print("\nALL CLAIMS FULLY TRACED AND VERIFIED.")
