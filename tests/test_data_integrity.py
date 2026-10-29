"""
Test Suite: Zero Scaffold Overlap & Label Leak Prevention
Enforces Section 0, 0A, and Section 2.1 non-negotiable rules.
"""

import os
import pandas as pd
import pytest


def test_zero_scaffold_overlap():
    processed_dir = r".\data\processed"
    train_path = os.path.join(processed_dir, "train_seed11.parquet")
    pool_path = os.path.join(processed_dir, "pool_features_seed11.parquet")

    assert os.path.exists(train_path), "Train dataset missing"
    assert os.path.exists(pool_path), "Pool dataset missing"

    df_train = pd.read_parquet(train_path)
    df_pool = pd.read_parquet(pool_path)

    train_scafs = set(df_train["scaffold"].unique())
    pool_scafs = set(df_pool["scaffold"].unique())

    overlap = train_scafs.intersection(pool_scafs)
    assert len(overlap) == 0, f"Violation: Found {len(overlap)} scaffolds overlapping between train and pool!"


def test_no_label_leak_in_pool():
    processed_dir = r".\data\processed"
    pool_path = os.path.join(processed_dir, "pool_features_seed11.parquet")

    df_pool = pd.read_parquet(pool_path)
    # The pool features file must NEVER expose ground-truth activity labels
    assert "label" not in df_pool.columns, "Security violation: Ground truth 'label' found in pool features!"
    assert "active" not in df_pool.columns, "Security violation: Ground truth 'active' found in pool features!"
    assert "is_hit" not in df_pool.columns, "Security violation: Ground truth 'is_hit' found in pool features!"


if __name__ == "__main__":
    test_zero_scaffold_overlap()
    test_no_label_leak_in_pool()
    print("All scaffold and label leak tests passed with 0 violations!")
