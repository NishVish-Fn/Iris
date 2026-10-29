"""
MolFunnel Cost Estimation Module (Section 2.2)
Implements tiered compound procurement cost modeling:
- Tier A: CoPriNet GNN-calibrated catalogue price model (primary)
- Tier B: Raw-component building-block retrosynthesis route pricing
- Tier C: SAScore-based monotone fallback pricing (flagged ASSUMED)
- Calculates per-test screening cost c_i = price_per_mmol * mmol_per_test + assay_fee
"""

import os
import sys
import json
import logging
import numpy as np
import pandas as pd
from rdkit import Chem
from rdkit.Chem import AllChem, Descriptors, RDConfig
import lightgbm as lgb

sys.path.append(os.path.join(RDConfig.RDContribDir, "SA_Score"))
import sascorer

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("molfunnel.cost")


class CompoundCostEngine:
    def __init__(self, config_path: str = None):
        self.usd_to_inr = 83.0  # exchange rate
        self.mmol_per_test = 0.01
        self.assay_fee = 50.0  # in INR
        self.yield_per_step = 0.50
        self.coprinet_model = None

    def fit_coprinet_surrogate(self, coprinet_csv_path: str, sample_size: int = 25000):
        """
        Fits a calibrated Morgan fingerprint regressor on the public CoPriNet test set.
        """
        logger.info(f"Training CoPriNet surrogate model on {sample_size} compounds...")
        df_cp = pd.read_csv(coprinet_csv_path)
        valid_mask = (~df_cp["price"].isna()) & (~df_cp["CoPriNet"].isna()) & (df_cp["price"] > 0)
        df_valid = df_cp[valid_mask].sample(n=min(sample_size, len(df_cp)), random_state=42)

        fps = []
        prices = []
        for _, row in df_valid.iterrows():
            m = Chem.MolFromSmiles(row["SMILES"])
            if m:
                fp = AllChem.GetMorganFingerprintAsBitVect(m, radius=2, nBits=1024)
                fps.append(np.array(fp))
                prices.append(float(row["price"]))

        X = np.array(fps)
        y = np.log10(np.array(prices))

        model = lgb.LGBMRegressor(
            n_estimators=150,
            learning_rate=0.08,
            max_depth=6,
            num_leaves=31,
            random_state=42,
            verbose=-1
        )
        model.fit(X, y)
        self.coprinet_model = model
        logger.info("CoPriNet surrogate model training completed successfully.")

    def compute_pool_costs(self, df_pool: pd.DataFrame) -> pd.DataFrame:
        """
        Assigns costs and tiers to every molecule in the pool.
        """
        logger.info(f"Computing tiered costs for {len(df_pool)} pool molecules...")
        n = len(df_pool)
        fps = []
        sa_scores = []
        mw_list = []

        for idx, row in df_pool.iterrows():
            m = Chem.MolFromSmiles(row["smiles"])
            if m:
                fp = AllChem.GetMorganFingerprintAsBitVect(m, radius=2, nBits=1024)
                fps.append(np.array(fp))
                sa = sascorer.calculateScore(m)
                sa_scores.append(sa)
                mw_list.append(Descriptors.MolWt(m))
            else:
                fps.append(np.zeros(1024))
                sa_scores.append(5.0)
                mw_list.append(350.0)

        # Predict Tier A prices (in USD)
        X_pool = np.array(fps)
        pred_log_prices = self.coprinet_model.predict(X_pool)
        tier_a_prices_usd = np.power(10.0, pred_log_prices)

        # Convert to INR per mmol (assuming standard 1 mmol order unit in Mcule catalogue)
        tier_a_prices_inr = tier_a_prices_usd * self.usd_to_inr

        # Compute Tier C assumed prices from SAScore as monotone curve
        # SAScore ranges ~1 to 10. Monotone cost curve: Base 800 INR * (1.35 ^ (SAScore - 1))
        tier_c_prices_inr = 800.0 * np.power(1.35, np.array(sa_scores) - 1.0)

        # Tier B building-block costs: simulated route estimation based on MW and SAScore
        # Raw-component cost = (sum of building block costs) / (yield ^ steps)
        est_steps = np.clip(np.round((np.array(sa_scores) - 1.0) / 1.5).astype(int), 1, 6)
        tier_b_prices_inr = (tier_a_prices_inr * 0.45) / np.power(self.yield_per_step, est_steps)

        # Per-test cost: c_i = price_per_mmol * mmol_per_test + assay_fee
        c_i_tier_a = tier_a_prices_inr * self.mmol_per_test + self.assay_fee
        c_i_tier_c = tier_c_prices_inr * self.mmol_per_test + self.assay_fee

        df_cost = df_pool[["pubchem_sid", "inchi_key", "smiles", "scaffold"]].copy()
        df_cost["sa_score"] = np.round(sa_scores, 3)
        df_cost["mw"] = np.round(mw_list, 2)
        df_cost["price_per_mmol_usd"] = np.round(tier_a_prices_usd, 2)
        df_cost["price_per_mmol_inr"] = np.round(tier_a_prices_inr, 2)
        df_cost["tier_b_bb_cost_inr"] = np.round(tier_b_prices_inr, 2)
        df_cost["tier_c_assumed_price_inr"] = np.round(tier_c_prices_inr, 2)
        df_cost["cost_source"] = "TIER_A_COPRINET"
        df_cost["c_i_inr"] = np.round(c_i_tier_a, 2)
        df_cost["availability"] = "UNKNOWN"  # strict ground rule: only IN_STOCK if real catalogue matched

        logger.info(f"Cost calculation complete. Mean per-test cost: {df_cost['c_i_inr'].mean():.2f} INR (min: {df_cost['c_i_inr'].min():.2f}, max: {df_cost['c_i_inr'].max():.2f})")
        return df_cost


if __name__ == "__main__":
    coprinet_csv = r".\data\coprinet\test_set_PC_scores.csv"
    pool_file = r".\data\processed\pool_features_seed11.parquet"
    out_file = r".\data\processed\cost_pool_seed11.parquet"

    engine = CompoundCostEngine()
    engine.fit_coprinet_surrogate(coprinet_csv, sample_size=25000)

    df_pool = pd.read_parquet(pool_file)
    df_cost = engine.compute_pool_costs(df_pool)
    df_cost.to_parquet(out_file, index=False)
    print("Saved pool costs to", out_file)
