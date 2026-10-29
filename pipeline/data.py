"""
MolFunnel Data Preparation & Cleaning Module
Cleans ALDH1 assay data (AID 1030) from LIT-PCBA according to Section 2.1.
"""

import os
import json
import logging
from typing import Dict, Tuple, List
import pandas as pd
from rdkit import Chem
from rdkit.Chem.Scaffolds import MurckoScaffold
from rdkit.Chem import Descriptors

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("molfunnel.data")


def clean_molecule(smiles: str) -> Tuple[str, str, str]:
    """
    Cleans a SMILES string:
    - strips salts / keeps largest organic fragment
    - canonicalizes SMILES
    - computes InChIKey
    - computes Bemis-Murcko scaffold SMILES
    Returns (canonical_smiles, inchi_key, scaffold_smiles) or (None, None, None) on failure.
    """
    if not smiles or not isinstance(smiles, str):
        return None, None, None
    try:
        mol = Chem.MolFromSmiles(smiles)
        if mol is None:
            return None, None, None

        # Keep largest fragment
        frags = Chem.GetMolFrags(mol, asMols=True, sanitizeFrags=True)
        if not frags:
            return None, None, None
        largest_mol = max(frags, key=lambda m: m.GetNumHeavyAtoms())

        # Canonicalize
        canon_smiles = Chem.MolToSmiles(largest_mol, canonical=True, isomericSmiles=False)
        inchi_key = Chem.MolToInchiKey(largest_mol)

        # Scaffold
        try:
            scaffold = MurckoScaffold.GetScaffoldForMol(largest_mol)
            scaffold_smiles = Chem.MolToSmiles(scaffold, canonical=True, isomericSmiles=False)
            if not scaffold_smiles:
                scaffold_smiles = "NO_RING"
        except Exception:
            scaffold_smiles = "NO_RING"

        return canon_smiles, inchi_key, scaffold_smiles
    except Exception:
        return None, None, None


def load_raw_dataset(raw_dir: str) -> Tuple[pd.DataFrame, Dict]:
    """
    Loads raw actives.smi and inactives.smi, logs cleaning counts at each step.
    """
    actives_path = os.path.join(raw_dir, "ALDH1", "actives.smi")
    inactives_path = os.path.join(raw_dir, "ALDH1", "inactives.smi")

    log_stats = {
        "raw_actives": 0,
        "raw_inactives": 0,
        "failed_rdkit_actives": 0,
        "failed_rdkit_inactives": 0,
        "duplicate_inchikey_actives": 0,
        "duplicate_inchikey_inactives": 0,
        "conflicting_active_and_inactive": 0,
        "final_clean_actives": 0,
        "final_clean_inactives": 0,
        "total_unique_molecules": 0,
    }

    def parse_smi(path: str, label: int):
        rows = []
        with open(path, "r", encoding="utf-8", errors="ignore") as f:
            for idx, line in enumerate(f):
                line = line.strip()
                if not line:
                    continue
                parts = line.split()
                smiles = parts[0]
                sid = parts[1] if len(parts) > 1 else f"SID_{label}_{idx}"
                rows.append({"smiles_raw": smiles, "pubchem_sid": sid, "label": label})
        return rows

    active_rows = parse_smi(actives_path, 1)
    inactive_rows = parse_smi(inactives_path, 0)
    log_stats["raw_actives"] = len(active_rows)
    log_stats["raw_inactives"] = len(inactive_rows)

    logger.info(f"Loaded raw records: Actives={len(active_rows)}, Inactives={len(inactive_rows)}")

    # Process actives
    clean_actives = []
    seen_active_keys = set()
    for row in active_rows:
        smi, ikey, scaf = clean_molecule(row["smiles_raw"])
        if not smi:
            log_stats["failed_rdkit_actives"] += 1
            continue
        if ikey in seen_active_keys:
            log_stats["duplicate_inchikey_actives"] += 1
            continue
        seen_active_keys.add(ikey)
        clean_actives.append({
            "smiles": smi,
            "pubchem_sid": row["pubchem_sid"],
            "inchi_key": ikey,
            "scaffold": scaf,
            "label": 1
        })

    # Process inactives
    clean_inactives = []
    seen_inactive_keys = set()
    for row in inactive_rows:
        smi, ikey, scaf = clean_molecule(row["smiles_raw"])
        if not smi:
            log_stats["failed_rdkit_inactives"] += 1
            continue
        if ikey in seen_inactive_keys:
            log_stats["duplicate_inchikey_inactives"] += 1
            continue
        seen_inactive_keys.add(ikey)
        clean_inactives.append({
            "smiles": smi,
            "pubchem_sid": row["pubchem_sid"],
            "inchi_key": ikey,
            "scaffold": scaf,
            "label": 0
        })

    # Find conflicts (present as both active and inactive)
    conflicts = seen_active_keys.intersection(seen_inactive_keys)
    log_stats["conflicting_active_and_inactive"] = len(conflicts)
    logger.info(f"Found {len(conflicts)} molecules conflicting between active and inactive sets. Dropping them.")

    final_actives = [m for m in clean_actives if m["inchi_key"] not in conflicts]
    final_inactives = [m for m in clean_inactives if m["inchi_key"] not in conflicts]

    log_stats["final_clean_actives"] = len(final_actives)
    log_stats["final_clean_inactives"] = len(final_inactives)
    log_stats["total_unique_molecules"] = len(final_actives) + len(final_inactives)

    df_clean = pd.DataFrame(final_actives + final_inactives)
    logger.info(f"Dataset cleaning complete: Clean Actives={len(final_actives)}, Clean Inactives={len(final_inactives)}")
    return df_clean, log_stats


if __name__ == "__main__":
    raw_dir = r".\data\raw"
    df, stats = load_raw_dataset(raw_dir)
    out_dir = r".\data\processed"
    os.makedirs(out_dir, exist_ok=True)
    df.to_parquet(os.path.join(out_dir, "aldh1_clean.parquet"), index=False)
    with open(os.path.join(out_dir, "cleaning_log.json"), "w") as f:
        json.dump(stats, f, indent=2)
    print("Saved clean dataset to aldh1_clean.parquet and cleaning_log.json")
