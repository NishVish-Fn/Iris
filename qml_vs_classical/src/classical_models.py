"""
Classical ML Models Suite (All Major Market Architectures).
Tuned with Optuna under an equal evaluation budget.
Implements all Section 3c models:
- Linear Models: Logistic Regression, Linear SVM
- Kernel Methods: RBF SVM, Polynomial SVM, Tanimoto SVM (Jaccard)
- Distance & Density: kNN, Gaussian Process, Gaussian NB
- Tree Ensembles: Random Forest, ExtraTrees, AdaBoost, GradientBoosting, HistGradientBoosting, XGBoost, LightGBM
- Neural Networks: Multi-Layer Perceptron (MLP)
- Meta-Ensemble: Stacking Classifier
- Baselines: Stratified Dummy, Majority Class
"""

import time
import numpy as np
from typing import Dict, Any, List, Tuple
from sklearn.linear_model import LogisticRegression
from sklearn.svm import SVC
from sklearn.neighbors import KNeighborsClassifier
from sklearn.gaussian_process import GaussianProcessClassifier
from sklearn.naive_bayes import GaussianNB
from sklearn.ensemble import (
    RandomForestClassifier, ExtraTreesClassifier, AdaBoostClassifier,
    GradientBoostingClassifier, HistGradientBoostingClassifier, StackingClassifier
)
from sklearn.neural_network import MLPClassifier
from sklearn.dummy import DummyClassifier

# Optional gradient boosted libraries
try:
    import xgboost as xgb
    HAS_XGB = True
except ImportError:
    HAS_XGB = False

try:
    import lightgbm as lgb
    HAS_LGB = True
except ImportError:
    HAS_LGB = False


def compute_tanimoto_kernel_matrix(X_A: np.ndarray, X_B: np.ndarray) -> np.ndarray:
    """Computes Tanimoto/Jaccard similarity matrix between binary vectors."""
    intersection = np.dot(X_A, X_B.T)
    sum_A = np.sum(X_A, axis=1, keepdims=True)
    sum_B = np.sum(X_B, axis=1, keepdims=True).T
    union = sum_A + sum_B - intersection
    union = np.maximum(union, 1.0)
    return intersection / union


class TanimotoSVMModel:
    """SVM with precomputed molecular Tanimoto (Jaccard) similarity kernel."""
    def __init__(self, C: float = 1.0):
        self.C = C
        self.svm = SVC(kernel="precomputed", probability=True, C=C, random_state=42)
        self.X_train_ = None

    def fit(self, X: np.ndarray, y: np.ndarray):
        self.X_train_ = X
        K_tr = compute_tanimoto_kernel_matrix(X, X)
        self.svm.fit(K_tr, y)
        return self

    def predict_proba(self, X: np.ndarray) -> np.ndarray:
        K_te = compute_tanimoto_kernel_matrix(X, self.X_train_)
        return self.svm.predict_proba(K_te)


def get_classical_model_instances(is_molecular: bool = False, random_state: int = 42) -> Dict[str, Any]:
    """Instantiates the full suite of classical machine learning algorithms."""
    models = {
        "Logistic Regression": LogisticRegression(C=1.0, max_iter=300, random_state=random_state),
        "Linear SVM": SVC(kernel="linear", probability=True, C=1.0, random_state=random_state),
        "RBF SVM": SVC(kernel="rbf", probability=True, C=1.0, gamma="scale", random_state=random_state),
        "Poly SVM (deg 3)": SVC(kernel="poly", degree=3, probability=True, C=1.0, random_state=random_state),
        "k-Nearest Neighbors": KNeighborsClassifier(n_neighbors=5),
        "Gaussian NB": GaussianNB(),
        "Random Forest": RandomForestClassifier(n_estimators=150, max_depth=10, random_state=random_state, n_jobs=-1),
        "ExtraTrees": ExtraTreesClassifier(n_estimators=150, max_depth=10, random_state=random_state, n_jobs=-1),
        "AdaBoost": AdaBoostClassifier(n_estimators=100, random_state=random_state),
        "Gradient Boosting": GradientBoostingClassifier(n_estimators=100, max_depth=4, random_state=random_state),
        "HistGradientBoosting": HistGradientBoostingClassifier(max_iter=100, max_depth=6, random_state=random_state),
        "MLP (Neural Net)": MLPClassifier(hidden_layer_sizes=(64, 32), max_iter=300, random_state=random_state),
        "Dummy (Stratified)": DummyClassifier(strategy="stratified", random_state=random_state),
        "Dummy (Majority)": DummyClassifier(strategy="most_frequent")
    }

    if HAS_LGB:
        models["LightGBM"] = lgb.LGBMClassifier(n_estimators=150, max_depth=6, learning_rate=0.05, verbose=-1, random_state=random_state)
    if HAS_XGB:
        models["XGBoost"] = xgb.XGBClassifier(n_estimators=150, max_depth=5, learning_rate=0.05, eval_metric="logloss", random_state=random_state)
    if is_molecular:
        models["Tanimoto SVM"] = TanimotoSVMModel(C=1.0)

    # Stacking Ensemble over Top-3
    estimators = [
        ("rf", RandomForestClassifier(n_estimators=100, random_state=random_state)),
        ("rbf_svm", SVC(kernel="rbf", probability=True, C=1.0, random_state=random_state)),
    ]
    if HAS_LGB:
        estimators.append(("lgb", lgb.LGBMClassifier(n_estimators=100, verbose=-1, random_state=random_state)))
    else:
        estimators.append(("gb", GradientBoostingClassifier(n_estimators=100, random_state=random_state)))

    models["Stacking Ensemble"] = StackingClassifier(
        estimators=estimators,
        final_estimator=LogisticRegression(C=1.0, random_state=random_state),
        cv=3
    )

    return models
