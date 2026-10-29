"""
Feature Extraction and Dimensionality Reduction Module.
Strict adherence to Rule 5:
- Scalers, PCA, and PLS are fit on training data only.
- Track A: Matched 4, 8, 10 features scaled to [0, pi] for quantum mapping.
- Track B: Full natural feature space for classical baselines.
"""

import numpy as np
from typing import Dict, Any, Tuple
from sklearn.decomposition import PCA
from sklearn.cross_decomposition import PLSRegression
from sklearn.preprocessing import MinMaxScaler, StandardScaler

def prepare_feature_tracks(
    dataset_dict: Dict[str, Any],
    n_qubits: int = 8,
    use_pls: bool = True
) -> Dict[str, Any]:
    """
    Transforms raw features into Track A (matched quantum features in [0, pi])
    and Track B (full natural features for classical baselines).
    All transformers are fit strictly on X_train.
    """
    X_tr_raw = dataset_dict["X_train_raw"]
    X_te_raw = dataset_dict["X_test_raw"]
    y_tr = dataset_dict["y_train"]

    # -------------------------------------------------------------
    # 1. TRACK B: Best-effort natural features
    # -------------------------------------------------------------
    # Standardize continuous features for classical baselines (fit on train only)
    scaler_std = StandardScaler()
    X_tr_track_b = scaler_std.fit_transform(X_tr_raw)
    X_te_track_b = scaler_std.transform(X_te_raw)

    # -------------------------------------------------------------
    # 2. TRACK A: Matched n_qubits features in [0, pi]
    # -------------------------------------------------------------
    n_feat = min(n_qubits, X_tr_raw.shape[1])
    
    if X_tr_raw.shape[1] <= n_feat:
        # If raw dimension is already small (e.g. D3 moons has 2 features), pad or scale
        if X_tr_raw.shape[1] < n_feat:
            pad_tr = np.zeros((len(X_tr_raw), n_feat - X_tr_raw.shape[1]), dtype=np.float32)
            pad_te = np.zeros((len(X_te_raw), n_feat - X_te_raw.shape[1]), dtype=np.float32)
            X_tr_red = np.hstack([X_tr_raw, pad_tr])
            X_te_red = np.hstack([X_te_raw, pad_te])
        else:
            X_tr_red = X_tr_raw.copy()
            X_te_red = X_te_raw.copy()
    else:
        # Dimensionality reduction (fit on train only)
        if use_pls and len(np.unique(y_tr)) > 1:
            reducer = PLSRegression(n_components=n_feat)
            reducer.fit(X_tr_raw, y_tr)
            X_tr_red = reducer.transform(X_tr_raw)
            X_te_red = reducer.transform(X_te_raw)
        else:
            reducer = PCA(n_components=n_feat, random_state=42)
            X_tr_red = reducer.fit_transform(X_tr_raw)
            X_te_red = reducer.transform(X_te_raw)

    # Scale strictly to [0, pi] (fit on train only)
    minmax = MinMaxScaler(feature_range=(0.0, np.pi))
    X_tr_track_a = minmax.fit_transform(X_tr_red)
    X_te_track_a = np.clip(minmax.transform(X_te_red), 0.0, np.pi)

    return {
        "X_train_track_a": X_tr_track_a,
        "X_test_track_a": X_te_track_a,
        "X_train_track_b": X_tr_track_b,
        "X_test_track_b": X_te_track_b,
        "y_train": y_tr,
        "y_test": dataset_dict["y_test"],
        "n_qubits": n_qubits
    }
