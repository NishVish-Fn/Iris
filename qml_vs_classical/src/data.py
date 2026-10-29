"""
Dataset Loader & Splitter for benchmark suite.
Covers D1 (ALDH1), D2 (Breast Cancer), D3 (Synthetic Moons), D4 (MNIST Binary).
Enforces zero data leakage:
- Scaffold-grouped splits for D1 with verified 0 scaffold overlap.
- Subsampled to statevector simulator scale.
"""

import os
import numpy as np
import pandas as pd
from pathlib import Path
from typing import Dict, Any, Tuple
from sklearn.datasets import load_breast_cancer, make_moons, load_digits
from sklearn.model_selection import StratifiedKFold
from rdkit import Chem
from rdkit.Chem import AllChem, Descriptors

from .utils import BASE_DIR, load_config

RDKIT_DESCRIPTORS = [
    ("MolWt", Descriptors.MolWt),
    ("MolLogP", Descriptors.MolLogP),
    ("NumHDonors", Descriptors.NumHDonors),
    ("NumHAcceptors", Descriptors.NumHAcceptors),
    ("TPSA", Descriptors.TPSA),
    ("NumRotatableBonds", Descriptors.NumRotatableBonds),
    ("FractionCSP3", Descriptors.FractionCSP3),
    ("NumAromaticRings", Descriptors.NumAromaticRings),
    ("RingCount", Descriptors.RingCount),
    ("HeavyAtomCount", Descriptors.HeavyAtomCount),
]

def load_d1_aldh1(config: Dict[str, Any], seed: int = 42) -> Dict[str, Any]:
    """Loads ALDH1 dataset with zero-leakage scaffold split."""
    cfg = config["datasets"]["d1_aldh1"]
    data_dir = BASE_DIR.parent / "data" / "processed"
    
    # Try reading train and pool
    tr_path = data_dir / "train_seed11.parquet"
    if tr_path.exists():
        df_all = pd.read_parquet(tr_path)
    else:
        # Fallback to general clean file
        df_all = pd.read_parquet(data_dir / "aldh1_clean.parquet").head(1000)

    # Scaffold-grouped split: Group by scaffold and assert zero overlap
    scaffolds = df_all["scaffold"].unique()
    rng = np.random.RandomState(seed)
    perm_scafs = rng.permutation(scaffolds)
    
    split_point = int(len(perm_scafs) * 0.70)
    train_scafs = set(perm_scafs[:split_point])
    test_scafs = set(perm_scafs[split_point:])
    
    # Strict assertion: zero scaffold overlap
    overlap = train_scafs.intersection(test_scafs)
    assert len(overlap) == 0, f"FATAL: Scaffold leakage detected! Overlap: {overlap}"
    
    df_train = df_all[df_all["scaffold"].isin(train_scafs)].copy()
    df_test = df_all[df_all["scaffold"].isin(test_scafs)].copy()
    
    # Subsample if necessary
    max_tr = cfg.get("subsample_train", 400)
    max_te = cfg.get("subsample_test", 200)
    
    if len(df_train) > max_tr:
        df_train = df_train.sample(n=max_tr, random_state=seed).reset_index(drop=True)
    if len(df_test) > max_te:
        df_test = df_test.sample(n=max_te, random_state=seed).reset_index(drop=True)
        
    y_train = df_train["label"].values.astype(int)
    y_test = df_test["label"].values.astype(int)
    
    # Extract fingerprints and descriptors
    def _extract_mol(smiles_list):
        bits = []
        descs = []
        for s in smiles_list:
            m = Chem.MolFromSmiles(s)
            if m:
                fp = AllChem.GetMorganFingerprintAsBitVect(m, radius=2, nBits=1024)
                arr = np.zeros(1024, dtype=np.float32)
                for b in fp.GetOnBits():
                    arr[b] = 1.0
                bits.append(arr)
                d_vals = [fn(m) for _, fn in RDKIT_DESCRIPTORS]
                descs.append(d_vals)
            else:
                bits.append(np.zeros(1024, dtype=np.float32))
                descs.append(np.zeros(len(RDKIT_DESCRIPTORS), dtype=np.float32))
        return np.array(bits, dtype=np.float32), np.array(descs, dtype=np.float32)

    bits_tr, desc_tr = _extract_mol(df_train["smiles"].tolist())
    bits_te, desc_te = _extract_mol(df_test["smiles"].tolist())
    
    # Clean NaNs
    desc_tr = np.nan_to_num(desc_tr)
    desc_te = np.nan_to_num(desc_te)
    
    return {
        "name": "D1_ALDH1",
        "X_train_raw": np.hstack([bits_tr, desc_tr]),
        "X_test_raw": np.hstack([bits_te, desc_te]),
        "y_train": y_train,
        "y_test": y_test,
        "scaffolds_train": df_train["scaffold"].values,
        "scaffolds_test": df_test["scaffold"].values,
        "is_molecular": True
    }


def load_d2_breast_cancer(config: Dict[str, Any], seed: int = 42) -> Dict[str, Any]:
    """Loads Breast Cancer Wisconsin dataset."""
    data = load_breast_cancer()
    X = data.data.astype(np.float32)
    y = data.target.astype(int)
    
    skf = StratifiedKFold(n_splits=5, shuffle=True, random_state=seed)
    train_idx, test_idx = next(skf.split(X, y))
    
    return {
        "name": "D2_Breast_Cancer",
        "X_train_raw": X[train_idx],
        "X_test_raw": X[test_idx],
        "y_train": y[train_idx],
        "y_test": y[test_idx],
        "is_molecular": False
    }


def load_d3_synthetic_moons(config: Dict[str, Any], seed: int = 42) -> Dict[str, Any]:
    """Loads synthetic two moons sanity check dataset."""
    cfg = config["datasets"]["d3_synthetic_moons"]
    n_samples = cfg.get("n_samples", 300)
    noise = cfg.get("noise", 0.2)
    
    X, y = make_moons(n_samples=n_samples, noise=noise, random_state=seed)
    X = X.astype(np.float32)
    
    skf = StratifiedKFold(n_splits=3, shuffle=True, random_state=seed)
    train_idx, test_idx = next(skf.split(X, y))
    
    return {
        "name": "D3_Synthetic_Moons",
        "X_train_raw": X[train_idx],
        "X_test_raw": X[test_idx],
        "y_train": y[train_idx],
        "y_test": y[test_idx],
        "is_molecular": False
    }


def load_d4_mnist_binary(config: Dict[str, Any], seed: int = 42) -> Dict[str, Any]:
    """Loads binary MNIST (0 vs 1 digits)."""
    digits = load_digits()
    mask = np.isin(digits.target, [0, 1])
    X = digits.data[mask].astype(np.float32)
    y = digits.target[mask].astype(int)
    
    skf = StratifiedKFold(n_splits=3, shuffle=True, random_state=seed)
    train_idx, test_idx = next(skf.split(X, y))
    
    return {
        "name": "D4_MNIST_Binary",
        "X_train_raw": X[train_idx],
        "X_test_raw": X[test_idx],
        "y_train": y[train_idx],
        "y_test": y[test_idx],
        "is_molecular": False
    }


def load_dataset_by_key(dataset_key: str, config: Dict[str, Any], seed: int = 42) -> Dict[str, Any]:
    loaders = {
        "d1_aldh1": load_d1_aldh1,
        "d2_breast_cancer": load_d2_breast_cancer,
        "d3_synthetic_moons": load_d3_synthetic_moons,
        "d4_mnist_binary": load_d4_mnist_binary
    }
    if dataset_key not in loaders:
        raise KeyError(f"Unknown dataset key: {dataset_key}. Available: {list(loaders.keys())}")
    return loaders[dataset_key](config, seed)
