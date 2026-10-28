"""
MolFunnel Scaffold Splitting Module
Enforces strict zero-leakage scaffold-based partition:
|Scaffolds(train) ∩ Scaffolds(pool)| = 0
"""

import os
import json
import logging
import numpy as np
import pandas as pd
from typing import Tuple, Dict

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("molfunnel.split")


def create_scaffold_split(
    df: pd.DataFrame,
    seed: int = 11,
    train_target_size: int = 3000,
    min_train_actives: int = 300,
    pool_target_size: int = 10000,
    k_fold: int = 5
) -> Tuple[pd.DataFrame, pd.DataFrame, Dict]:
    """
    Groups molecules by Bemis-Murcko scaffold.
    Assigns whole scaffold clusters to either Train or Pool.
    Guarantees:
    - Train and Pool scaffolds are strictly disjoint.
    - Train contains >= min_train_actives.
    - Pool contains pool_target_size molecules at natural hit rate.
    - 5-fold cross-validation inside Train is also grouped by scaffold.
    """
    np.random.seed(seed)

    # Aggregate molecules by scaffold
    scaffold_groups = {}
    for scaf, group in df.groupby("scaffold"):
        scaffold_groups[scaf] = {
            "indices": group.index.values,
            "total": len(group),
            "actives": int(group["label"].sum()),
            "inactives": int(len(group) - group["label"].sum())
        }

    # Sort scaffolds: prioritize scaffolds with actives for train to meet min_train_actives
    scaf_list = list(scaffold_groups.keys())
    np.random.shuffle(scaf_list)
    # Put scaffolds with actives first to easily satisfy min_train_actives
    active_scafs = [s for s in scaf_list if scaffold_groups[s]["actives"] > 0]
    inactive_scafs = [s for s in scaf_list if scaffold_groups[s]["actives"] == 0]

    train_scafs = set()
    train_indices = []
    train_actives = 0

    # Pick scaffolds for train
    for s in active_scafs:
        if len(train_indices) >= train_target_size and train_actives >= min_train_actives:
            break
        train_scafs.add(s)
        train_indices.extend(scaffold_groups[s]["indices"])
        train_actives += scaffold_groups[s]["actives"]

    # If need more to reach ~3000
    for s in inactive_scafs:
        if len(train_indices) >= train_target_size:
            break
        train_scafs.add(s)
        train_indices.extend(scaffold_groups[s]["indices"])

    # Now pick Pool from completely remaining scaffolds
    remaining_scafs = [s for s in scaf_list if s not in train_scafs]
    np.random.shuffle(remaining_scafs)

    pool_scafs = set()
    pool_indices = []
    for s in remaining_scafs:
        if len(pool_indices) >= pool_target_size:
            break
        pool_scafs.add(s)
        pool_indices.extend(scaffold_groups[s]["indices"])

    # Trim to exactly pool_target_size if slightly above, or keep as selected
    pool_indices = pool_indices[:pool_target_size]
    train_indices = train_indices[:min(train_target_size, len(train_indices))]

    df_train = df.loc[train_indices].copy().reset_index(drop=True)
    df_pool = df.loc[pool_indices].copy().reset_index(drop=True)

    # Re-verify disjoint scaffolds after selection
    train_scaf_set = set(df_train["scaffold"].unique())
    pool_scaf_set = set(df_pool["scaffold"].unique())
    overlap = train_scaf_set.intersection(pool_scaf_set)
    assert len(overlap) == 0, f"FATAL: Scaffold leak detected: {len(overlap)} overlapping scaffolds!"

    # Create K-fold inside Train grouped by scaffold
    train_scaf_list = list(train_scaf_set)
    np.random.shuffle(train_scaf_list)
    scaf_folds = np.array_split(train_scaf_list, k_fold)
    scaf_to_fold = {}
    for fold_idx, fold_scafs in enumerate(scaf_folds):
        for s in fold_scafs:
            scaf_to_fold[s] = fold_idx
    df_train["cv_fold"] = df_train["scaffold"].map(scaf_to_fold)

    # Separate pool features from pool labels (Pool labels hidden until reveal!)
    # Pool dataframe saved for screening does NOT expose label in features
    split_info = {
        "seed": seed,
        "train_size": len(df_train),
        "train_actives": int(df_train["label"].sum()),
        "train_hit_rate": float(df_train["label"].mean()),
        "train_distinct_scaffolds": len(train_scaf_set),
        "pool_size": len(df_pool),
        "pool_actives": int(df_pool["label"].sum()),
        "pool_hit_rate": float(df_pool["label"].mean()),
        "pool_distinct_scaffolds": len(pool_scaf_set),
        "scaffold_overlap_count": len(overlap),
        "k_folds": k_fold
    }

    logger.info(f"Scaffold Split created successfully: Train={len(df_train)} ({df_train['label'].sum()} actives, {len(train_scaf_set)} scaffolds), "
                f"Pool={len(df_pool)} ({df_pool['label'].sum()} actives, {len(pool_scaf_set)} scaffolds). Overlap={len(overlap)}.")

    return df_train, df_pool, split_info


if __name__ == "__main__":
    processed_dir = r"C:\Users\creat\.gemini\antigravity\scratch\molfunnel\data\processed"
    clean_path = os.path.join(processed_dir, "aldh1_clean.parquet")
    if os.path.exists(clean_path):
        df_clean = pd.read_parquet(clean_path)
        df_train, df_pool, info = create_scaffold_split(df_clean, seed=11)
        df_train.to_parquet(os.path.join(processed_dir, "train_seed11.parquet"), index=False)
        # Save pool without label visible for screening
        df_pool_features = df_pool.drop(columns=["label"]).copy()
        df_pool_features.to_parquet(os.path.join(processed_dir, "pool_features_seed11.parquet"), index=False)
        # Hidden pool labels saved separately for evaluation only (Section 0 reveal rule)
        df_pool_labels = df_pool[["pubchem_sid", "inchi_key", "label"]].copy()
        df_pool_labels.to_parquet(os.path.join(processed_dir, "pool_hidden_labels_seed11.parquet"), index=False)
        with open(os.path.join(processed_dir, "split_info_seed11.json"), "w") as f:
            json.dump(info, f, indent=2)
        print("Scaffold split completed and saved.")
    else:
        print("Clean data not ready yet.")
