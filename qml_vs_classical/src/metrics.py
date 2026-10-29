"""
Comprehensive Evaluation Metrics Module (Section 5).
Computes:
- Standard Classification: Accuracy, Balanced Accuracy, ROC-AUC, PR-AUC, F1, MCC, Brier Score, ECE.
- Drug Discovery Enrichment (D1): EF@1%, EF@5%, Recall@Top-K, Hit Rate.
"""

import numpy as np
from typing import Dict, Any, List, Tuple
from sklearn.metrics import (
    accuracy_score, balanced_accuracy_score, roc_auc_score,
    average_precision_score, f1_score, matthews_corrcoef, brier_score_loss
)

def compute_expected_calibration_error(y_true: np.ndarray, y_prob: np.ndarray, n_bins: int = 10) -> float:
    """Computes Expected Calibration Error (ECE)."""
    bins = np.linspace(0.0, 1.0, n_bins + 1)
    ece = 0.0
    N = len(y_true)
    for i in range(n_bins):
        b_min, b_max = bins[i], bins[i + 1]
        mask = (y_prob >= b_min) & (y_prob < b_max if i < n_bins - 1 else y_prob <= b_max)
        n_b = np.sum(mask)
        if n_b > 0:
            acc_b = np.mean(y_true[mask])
            conf_b = np.mean(y_prob[mask])
            ece += (n_b / float(N)) * abs(acc_b - conf_b)
    return float(ece)


def compute_enrichment_factors(y_true: np.ndarray, y_prob: np.ndarray) -> Dict[str, float]:
    """Computes EF@1%, EF@5%, and Recall@Top-20 for virtual screening."""
    N = len(y_true)
    total_actives = max(1, int(np.sum(y_true)))
    base_rate = total_actives / float(N)
    
    sorted_idx = np.argsort(y_prob)[::-1]
    y_sorted = y_true[sorted_idx]
    
    # EF@1%
    k1 = max(1, int(0.01 * N))
    hits1 = np.sum(y_sorted[:k1])
    ef1 = (hits1 / float(k1)) / base_rate
    
    # EF@5%
    k5 = max(1, int(0.05 * N))
    hits5 = np.sum(y_sorted[:k5])
    ef5 = (hits5 / float(k5)) / base_rate
    
    # Recall@Top-20
    k20 = min(20, N)
    hits20 = np.sum(y_sorted[:k20])
    rec20 = hits20 / float(total_actives)
    
    return {
        "ef_1pct": round(float(ef1), 3),
        "ef_5pct": round(float(ef5), 3),
        "recall_top20": round(float(rec20), 4),
        "hits_top20": int(hits20)
    }


def evaluate_predictions(
    y_true: np.ndarray,
    y_prob: np.ndarray,
    is_molecular: bool = False
) -> Dict[str, float]:
    """Computes full evaluation battery for predictions."""
    y_pred = (y_prob >= 0.5).astype(int)
    
    # Basic metrics
    acc = accuracy_score(y_true, y_pred)
    bal_acc = balanced_accuracy_score(y_true, y_pred)
    f1 = f1_score(y_true, y_pred, zero_division=0)
    mcc = matthews_corrcoef(y_true, y_pred)
    brier = brier_score_loss(y_true, y_prob)
    ece = compute_expected_calibration_error(y_true, y_prob)
    
    # Probabilistic metrics
    try:
        roc_auc = roc_auc_score(y_true, y_prob)
    except Exception:
        roc_auc = 0.5
    try:
        pr_auc = average_precision_score(y_true, y_prob)
    except Exception:
        pr_auc = np.mean(y_true)
        
    res = {
        "accuracy": round(float(acc), 4),
        "balanced_accuracy": round(float(bal_acc), 4),
        "roc_auc": round(float(roc_auc), 4),
        "pr_auc": round(float(pr_auc), 4),
        "f1": round(float(f1), 4),
        "mcc": round(float(mcc), 4),
        "brier_score": round(float(brier), 4),
        "ece": round(float(ece), 4)
    }
    
    if is_molecular:
        res.update(compute_enrichment_factors(y_true, y_prob))
        
    return res
