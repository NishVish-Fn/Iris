"""
MolFunnel Feature Extraction & Quantum Feature Compression Module (Section 3)
Compares 3 compression methods strictly fitted on Train:
- (a) PCA-8 on ECFP counts
- (b) PLS-8 (supervised)
- (c) Top-8 RDKit descriptors by training importance (Random Forest Gini importance)
Evaluates each via 5-fold scaffold-grouped CV on Train, min-max scales winner to [0, pi].
"""

import os
import json
import logging
from typing import Tuple, List, Dict
import numpy as np
import pandas as pd
from rdkit import Chem
from rdkit.Chem import AllChem, Descriptors
from sklearn.decomposition import PCA
from sklearn.cross_decomposition import PLSRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import MinMaxScaler
from sklearn.svm import SVC
from sklearn.metrics import roc_auc_score

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("molfunnel.features")

RDKIT_DESCRIPTORS = [
    ("MolWt", Descriptors.MolWt),
    ("MolLogP", Descriptors.MolLogP),
    ("NumHDonors", Descriptors.NumHDonors),
    ("NumHAcceptors", Descriptors.NumHAcceptors),
    ("TPSA", Descriptors.TPSA),
    ("NumRotatableBonds", Descriptors.NumRotatableBonds),
    ("FractionCSP3", Descriptors.FractionCSP3),
    ("NumAromaticRings", Descriptors.NumAromaticRings),
    ("NumAliphaticRings", Descriptors.NumAliphaticRings),
    ("RingCount", Descriptors.RingCount),
    ("HeavyAtomCount", Descriptors.HeavyAtomCount),
    ("LabuteASA", Descriptors.LabuteASA),
]


def extract_molecule_features(smiles_list: list) -> Tuple[np.ndarray, np.ndarray, np.ndarray]:
    """
    Computes:
    - ECFP4 bit vector (2048)
    - ECFP4 count vector (2048)
    - RDKit 2D descriptor matrix
    """
    ecfp_bits = []
    ecfp_counts = []
    rdkit_descs = []

    for smi in smiles_list:
        m = Chem.MolFromSmiles(smi)
        if m:
            # Bit vector
            bv = AllChem.GetMorganFingerprintAsBitVect(m, radius=2, nBits=2048)
            ecfp_bits.append(np.array(bv, dtype=np.float32))

            # Count vector
            info = {}
            fp = AllChem.GetMorganFingerprint(m, radius=2)
            counts = np.zeros(2048, dtype=np.float32)
            for k, v in fp.GetNonzeroElements().items():
                counts[k % 2048] += v
            ecfp_counts.append(counts)

            # RDKit descriptors
            descs = [func(m) for _, func in RDKIT_DESCRIPTORS]
            rdkit_descs.append(descs)
        else:
            ecfp_bits.append(np.zeros(2048, dtype=np.float32))
            ecfp_counts.append(np.zeros(2048, dtype=np.float32))
            rdkit_descs.append([0.0] * len(RDKIT_DESCRIPTORS))

    return np.array(ecfp_bits), np.array(ecfp_counts), np.array(rdkit_descs)


def evaluate_compression_candidates(
    X_counts_train: np.ndarray,
    X_desc_train: np.ndarray,
    y_train: np.ndarray,
    cv_folds: np.ndarray
) -> Tuple[str, dict, object, object]:
    """
    Evaluates PCA-8, PLS-8, and Top-8 RDKit descriptors using 5-fold scaffold CV on Train.
    """
    logger.info("Evaluating 8-feature quantum compression candidates via 5-fold scaffold CV...")
    n_folds = int(np.max(cv_folds) + 1)
    results = {}

    # Candidate 1: PCA-8 on ECFP counts
    pca = PCA(n_components=8, random_state=42)
    X_pca = pca.fit_transform(X_counts_train)
    pca_scores = []
    for f in range(n_folds):
        val_idx = np.where(cv_folds == f)[0]
        tr_idx = np.where(cv_folds != f)[0]
        clf = SVC(probability=True, random_state=42)
        clf.fit(X_pca[tr_idx], y_train[tr_idx])
        preds = clf.predict_proba(X_pca[val_idx])[:, 1]
        pca_scores.append(roc_auc_score(y_train[val_idx], preds))
    results["pca_8"] = {
        "mean_auc": float(np.mean(pca_scores)),
        "std_auc": float(np.std(pca_scores)),
        "fold_scores": [float(s) for s in pca_scores]
    }

    # Candidate 2: PLS-8 (supervised)
    pls = PLSRegression(n_components=8)
    X_pls = pls.fit_transform(X_counts_train, y_train)[0]
    pls_scores = []
    for f in range(n_folds):
        val_idx = np.where(cv_folds == f)[0]
        tr_idx = np.where(cv_folds != f)[0]
        clf = SVC(probability=True, random_state=42)
        clf.fit(X_pls[tr_idx], y_train[tr_idx])
        preds = clf.predict_proba(X_pls[val_idx])[:, 1]
        pls_scores.append(roc_auc_score(y_train[val_idx], preds))
    results["pls_8"] = {
        "mean_auc": float(np.mean(pls_scores)),
        "std_auc": float(np.std(pls_scores)),
        "fold_scores": [float(s) for s in pls_scores]
    }

    # Candidate 3: Top-8 RDKit descriptors by training Random Forest importance
    rf = RandomForestClassifier(n_estimators=100, random_state=42)
    rf.fit(X_desc_train, y_train)
    importances = rf.feature_importances_
    top8_indices = np.argsort(importances)[-8:]
    X_top8 = X_desc_train[:, top8_indices]
    top8_scores = []
    for f in range(n_folds):
        val_idx = np.where(cv_folds == f)[0]
        tr_idx = np.where(cv_folds != f)[0]
        clf = SVC(probability=True, random_state=42)
        clf.fit(X_top8[tr_idx], y_train[tr_idx])
        preds = clf.predict_proba(X_top8[val_idx])[:, 1]
        top8_scores.append(roc_auc_score(y_train[val_idx], preds))
    results["rdkit_top8"] = {
        "mean_auc": float(np.mean(top8_scores)),
        "std_auc": float(np.std(top8_scores)),
        "fold_scores": [float(s) for s in top8_scores],
        "top8_descriptors": [RDKIT_DESCRIPTORS[i][0] for i in top8_indices]
    }

    # Choose winner
    best_candidate = max(results.keys(), key=lambda k: results[k]["mean_auc"])
    logger.info(f"Compression results: {json.dumps(results, indent=2)}")
    logger.info(f"Selected winner: {best_candidate} with Mean CV AUC = {results[best_candidate]['mean_auc']:.4f}")

    # Retrain winning compressor on full train and fit scaler to [0, pi]
    if best_candidate == "pls_8":
        winning_model = PLSRegression(n_components=8).fit(X_counts_train, y_train)
        X_best_train = winning_model.transform(X_counts_train)
    elif best_candidate == "pca_8":
        winning_model = PCA(n_components=8, random_state=42).fit(X_counts_train)
        X_best_train = winning_model.transform(X_counts_train)
    else:
        winning_model = top8_indices
        X_best_train = X_desc_train[:, top8_indices]

    scaler = MinMaxScaler(feature_range=(0.0, np.pi))
    scaler.fit(X_best_train)

    return best_candidate, results, winning_model, scaler


if __name__ == "__main__":
    processed_dir = r"C:\Users\creat\.gemini\antigravity\scratch\molfunnel\data\processed"
    df_train = pd.read_parquet(os.path.join(processed_dir, "train_seed11.parquet"))
    df_pool = pd.read_parquet(os.path.join(processed_dir, "pool_features_seed11.parquet"))

    logger.info(f"Extracting features for Train ({len(df_train)}) and Pool ({len(df_pool)})...")
    bits_tr, counts_tr, desc_tr = extract_molecule_features(df_train["smiles"].tolist())
    bits_po, counts_po, desc_po = extract_molecule_features(df_pool["smiles"].tolist())

    y_train = df_train["label"].values
    cv_folds = df_train["cv_fold"].values

    best_name, results_dict, compressor, scaler = evaluate_compression_candidates(
        counts_tr, desc_tr, y_train, cv_folds
    )

    # Transform Train and Pool to 8-features scaled to [0, pi]
    if best_name == "pls_8":
        X_tr_raw = compressor.transform(counts_tr)
        X_po_raw = compressor.transform(counts_po)
    elif best_name == "pca_8":
        X_tr_raw = compressor.transform(counts_tr)
        X_po_raw = compressor.transform(counts_po)
    else:
        X_tr_raw = desc_tr[:, compressor]
        X_po_raw = desc_po[:, compressor]

    X_tr_q = scaler.transform(X_tr_raw)
    X_po_q = scaler.transform(X_po_raw)

    # Save features
    np.save(os.path.join(processed_dir, "train_q8_seed11.npy"), X_tr_q)
    np.save(os.path.join(processed_dir, "pool_q8_seed11.npy"), X_po_q)
    np.save(os.path.join(processed_dir, "train_ecfp_bits_seed11.npy"), bits_tr)
    np.save(os.path.join(processed_dir, "pool_ecfp_bits_seed11.npy"), bits_po)
    np.save(os.path.join(processed_dir, "train_desc_seed11.npy"), desc_tr)
    np.save(os.path.join(processed_dir, "pool_desc_seed11.npy"), desc_po)

    with open(os.path.join(processed_dir, "feature_selection_log.json"), "w") as f:
        json.dump({
            "winner": best_name,
            "angle_range": [0.0, float(np.pi)],
            "metrics": results_dict
        }, f, indent=2)

    print("Feature extraction and quantum feature scaling completed successfully.")
