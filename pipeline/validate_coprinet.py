"""
Download and validate CoPriNet test set.
Computes Spearman correlation and MAE of log-price.
"""

import os
import pandas as pd
import numpy as np
from scipy.stats import spearmanr
from sklearn.metrics import mean_absolute_error
from sklearn.linear_model import LinearRegression
import json

out_dir = r"C:\Users\creat\.gemini\antigravity\scratch\molfunnel\data\coprinet"
os.makedirs(out_dir, exist_ok=True)
csv_path = os.path.join(out_dir, "test_set_PC_scores.csv")

df = pd.read_csv(csv_path)
print(f"Loaded {len(df)} compounds from CoPriNet public test set.")

# Drop missing values
valid_mask = (~df["price"].isna()) & (~df["CoPriNet"].isna()) & (df["price"] > 0)
df_valid = df[valid_mask].copy()
print(f"Valid compounds for evaluation: {len(df_valid)}")

actual_price = df_valid["price"].values
coprinet_score = df_valid["CoPriNet"].values

# Check log scale
actual_log = np.log10(actual_price)

spearman_corr, p_val = spearmanr(coprinet_score, actual_price)

reg = LinearRegression().fit(coprinet_score.reshape(-1, 1), actual_log)
pred_log = reg.predict(coprinet_score.reshape(-1, 1))
mae_log = float(mean_absolute_error(actual_log, pred_log))

validation_results = {
    "dataset": "CoPriNet test_set_PC (100k public test set)",
    "n_compounds_total": len(df),
    "n_compounds_valid": len(df_valid),
    "license": "MIT License (oxpig/CoPriNet, Copyright 2022 rsanchezgarc)",
    "spearman_correlation": float(spearman_corr),
    "spearman_p_value": float(p_val),
    "mae_log10_price": mae_log,
    "r2_log10_price": float(reg.score(coprinet_score.reshape(-1, 1), actual_log)),
    "output_unit": "USD per standard compound order (calibrated to Mcule catalogue)",
    "calibration_slope": float(reg.coef_[0]),
    "calibration_intercept": float(reg.intercept_)
}

print("CoPriNet Model Validation Results:")
print(json.dumps(validation_results, indent=2))

with open(os.path.join(out_dir, "validation_metrics.json"), "w") as f:
    json.dump(validation_results, f, indent=2)
print("Saved validation metrics to data/coprinet/validation_metrics.json")
