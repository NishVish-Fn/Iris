"""
Data Loader & Preprocessor for QML vs Classical ML Benchmark Suite
Target: ALDH1 Inhibition (LIT-PCBA / PubChem AID 1030)
Extracts:
1. 8-qubit normalized continuous angles in [0, pi] for quantum circuits
2. 2048-bit ECFP4 fingerprint vectors for fingerprint-based classical models
3. 12 RDKit physicochemical descriptors for tabular tree models
"""

import os
import sys
import numpy as np
import pandas as pd
from typing import Tuple, Dict
from rdkit import Chem
from rdkit.Chem import AllChem, Descriptors
from sklearn.cross_decomposition import PLSRegression
from sklearn.preprocessing import MinMaxScaler
from sklearn.model_selection import StratifiedKFold

# Set MPL config dir
import tempfile
os.environ["MPLCONFIGDIR"] = os.path.join(tempfile.gettempdir(), "matplotlib_loader")

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

def extract_raw_features(smiles_list: list) -> Tuple[np.ndarray, np.ndarray]:
    """Computes 2048-bit ECFP4 and 12 RDKit descriptors."""
    bits_list = []
    desc_list = []
    for smi in smiles_list:
        m = Chem.MolFromSmiles(smi)
        if m:
            fp = AllChem.GetMorganFingerprintAsBitVect(m, radius=2, nBits=2048)
            arr = np.zeros(2048, dtype=np.float32)
            for on_bit in fp.GetOnBits():
                arr[on_bit] = 1.0
            bits_list.append(arr)
            
            d_vals = [fn(m) for _, fn in RDKIT_DESCRIPTORS]
            desc_list.append(d_vals)
        else:
            bits_list.append(np.zeros(2048, dtype=np.float32))
            desc_list.append(np.zeros(len(RDKIT_DESCRIPTORS), dtype=np.float32))
            
    return np.array(bits_list, dtype=np.float32), np.array(desc_list, dtype=np.float32)


def get_benchmark_dataset(
    n_train: int = 400,
    n_test: int = 200,
    random_state: int = 42
) -> Dict[str, np.ndarray]:
    """
    Loads and pre-processes balanced dataset for the benchmark suite.
    Uses balanced subsample to enable rapid, exact quantum simulation across multiple ansatzes.
    """
    data_dir = r".\data\processed"
    df_train_raw = pd.read_parquet(os.path.join(data_dir, "train_seed11.parquet"))
    
    # Balanced sampling of actives and inactives
    actives = df_train_raw[df_train_raw["label"] == 1]
    inactives = df_train_raw[df_train_raw["label"] == 0]
    
    rng = np.random.RandomState(random_state)
    n_act_tr = min(len(actives) // 2, n_train // 2)
    n_inact_tr = n_train - n_act_tr
    
    idx_act = rng.permutation(len(actives))
    idx_inact = rng.permutation(len(inactives))
    
    tr_act = actives.iloc[idx_act[:n_act_tr]]
    te_act = actives.iloc[idx_act[n_act_tr:n_act_tr + (n_test // 2)]]
    
    tr_inact = inactives.iloc[idx_inact[:n_inact_tr]]
    te_inact = inactives.iloc[idx_inact[n_inact_tr:n_inact_tr + (n_test - len(te_act))]]
    
    df_train = pd.concat([tr_act, tr_inact]).sample(frac=1.0, random_state=random_state).reset_index(drop=True)
    df_test = pd.concat([te_act, te_inact]).sample(frac=1.0, random_state=random_state).reset_index(drop=True)
    
    y_train = df_train["label"].values.astype(int)
    y_test = df_test["label"].values.astype(int)
    
    # Extract fingerprints and descriptors
    bits_tr, desc_tr = extract_raw_features(df_train["smiles"].tolist())
    bits_te, desc_te = extract_raw_features(df_test["smiles"].tolist())
    
    # Clean descriptor NaNs
    desc_tr = np.nan_to_num(desc_tr, nan=0.0)
    desc_te = np.nan_to_num(desc_te, nan=0.0)
    
    # PLS-8 feature compression for 8-qubit quantum encoding
    pls = PLSRegression(n_components=8)
    pls.fit(bits_tr, y_train)
    q8_tr_raw = pls.transform(bits_tr)
    q8_te_raw = pls.transform(bits_te)
    
    scaler = MinMaxScaler(feature_range=(0.0, np.pi))
    q8_train = scaler.fit_transform(q8_tr_raw)
    q8_test = scaler.transform(q8_te_raw)
    
    return {
        "X_q8_train": q8_train,
        "X_q8_test": q8_test,
        "X_bits_train": bits_tr,
        "X_bits_test": bits_te,
        "X_desc_train": desc_tr,
        "X_desc_test": desc_te,
        "y_train": y_train,
        "y_test": y_test,
        "df_train": df_train,
        "df_test": df_test
    }

if __name__ == "__main__":
    data = get_benchmark_dataset(n_train=200, n_test=100)
    print("Dataset loaded successfully!")
    print(f"X_q8_train: {data['X_q8_train'].shape}, range: [{data['X_q8_train'].min():.2f}, {data['X_q8_train'].max():.2f}]")
    print(f"y_train positive rate: {data['y_train'].mean():.2%}")
    print(f"y_test positive rate: {data['y_test'].mean():.2%}")
