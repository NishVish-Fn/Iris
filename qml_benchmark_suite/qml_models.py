"""
QML Models Module (Powered Compulsorily by QpiAI Quantum SDK: qpiai_quantum)
Implements:
1. Pure Variational Quantum Classifier (VQC / QNN): Parameterized layers + Pauli-Z expectation value
2. Quantum Kernel Estimation (QKE) - ZZ-Ring Entanglement
3. Quantum Kernel Estimation (QKE) - IQP (Instantaneous Quantum Polynomial)
4. Quantum Kernel Estimation (QKE) - Hardware Efficient Ansatz (HEA)
"""

import os
import sys
import time
import numpy as np
from typing import Tuple, Dict, List
from sklearn.svm import SVC
from sklearn.metrics import roc_auc_score, accuracy_score, f1_score, average_precision_score, brier_score_loss
import qpiai_quantum as qq

# Configure QpiAI Quantum
QPIAI_KEY = ""
os.environ["API_KEY"] = QPIAI_KEY
os.environ["QPIAI_QUANTUM_API_KEY"] = QPIAI_KEY
import tempfile
os.environ["MPLCONFIGDIR"] = os.path.join(tempfile.gettempdir(), "matplotlib_models")


# =====================================================================
# 1. PURE VARIATIONAL QUANTUM CLASSIFIER (VQC)
# =====================================================================
class PureVariationalQuantumClassifier:
    """
    A 100% Pure Quantum Machine Learning Classifier.
    - Encodes inputs x into quantum states
    - Passes state through parameterized variational unitary W(theta)
    - Measures quantum Pauli-Z expectation value: <Z_0 Z_1>
    - Output prediction P(active) is directly obtained from quantum measurement.
    """
    def __init__(self, n_qubits: int = 8, depth: int = 2, learning_rate: float = 0.05, n_epochs: int = 25, random_state: int = 42):
        self.n_qubits = n_qubits
        self.depth = depth
        self.lr = learning_rate
        self.n_epochs = n_epochs
        self.rng = np.random.RandomState(random_state)
        # Parameters: depth layers of Ry and Rz on all qubits
        self.n_params = depth * n_qubits * 2
        self.theta = self.rng.uniform(-np.pi, np.pi, self.n_params)

    def _build_circuit(self, x: np.ndarray, theta: np.ndarray) -> qq.Circuit:
        c = qq.Circuit(self.n_qubits)
        
        # 1. State Encoding
        for i in range(self.n_qubits):
            c.h(i)
            c.rz(i, float(x[i]))
            
        # Entanglement in encoding
        for i in range(self.n_qubits):
            c.cx(i, (i + 1) % self.n_qubits)
            c.rz((i + 1) % self.n_qubits, float(x[i] * x[(i + 1) % self.n_qubits]))
            c.cx(i, (i + 1) % self.n_qubits)

        # 2. Variational Parameterized Unitary W(theta)
        param_idx = 0
        for d in range(self.depth):
            for i in range(self.n_qubits):
                c.ry(i, float(theta[param_idx]))
                param_idx += 1
                c.rz(i, float(theta[param_idx]))
                param_idx += 1
            # Entangling layer
            for i in range(self.n_qubits):
                c.cx(i, (i + 1) % self.n_qubits)
                
        return c

    def _evaluate_expectation(self, x: np.ndarray, theta: np.ndarray) -> float:
        """Computes <Z_0 Z_1> from the exact statevector."""
        c = self._build_circuit(x, theta)
        sv = qq.Statevector(c).data
        probs = np.abs(sv) ** 2
        
        # Parity of qubit 0 and 1
        dim = len(probs)
        indices = np.arange(dim)
        b0 = (indices >> 0) & 1
        b1 = (indices >> 1) & 1
        parity = 1 - 2 * (b0 ^ b1) # +1 if even, -1 if odd
        exp_val = np.sum(probs * parity)
        return float(np.clip(exp_val, -1.0, 1.0))

    def fit(self, X: np.ndarray, y: np.ndarray):
        """Trains variational parameters using SPSA (Simultaneous Perturbation Stochastic Approximation)."""
        N = len(X)
        c_param = 0.1
        
        # Convert y from {0, 1} to {-1, +1} targets
        y_target = 2.0 * y - 1.0
        
        # Subsample for efficient convergence
        sub_size = min(N, 60)
        
        for epoch in range(self.n_epochs):
            sub_idx = self.rng.choice(N, sub_size, replace=False)
            X_sub = X[sub_idx]
            y_sub = y_target[sub_idx]
            
            # SPSA perturbation vector
            delta = self.rng.choice([-1.0, 1.0], size=self.n_params)
            theta_plus = self.theta + c_param * delta
            theta_minus = self.theta - c_param * delta
            
            # Evaluate losses
            preds_plus = np.array([self._evaluate_expectation(x, theta_plus) for x in X_sub])
            preds_minus = np.array([self._evaluate_expectation(x, theta_minus) for x in X_sub])
            
            loss_plus = np.mean((preds_plus - y_sub) ** 2)
            loss_minus = np.mean((preds_minus - y_sub) ** 2)
            
            # SPSA gradient estimate
            grad = (loss_plus - loss_minus) / (2.0 * c_param * delta)
            
            # Step update
            lr = self.lr / (1.0 + 0.05 * epoch)
            self.theta -= lr * grad
            
        return self

    def predict_proba(self, X: np.ndarray) -> np.ndarray:
        """Returns P(active) = (1 + <Z>) / 2."""
        preds = np.array([self._evaluate_expectation(x, self.theta) for x in X])
        p1 = (1.0 + preds) / 2.0
        p1 = np.clip(p1, 0.001, 0.999)
        p0 = 1.0 - p1
        return np.column_stack([p0, p1])


# =====================================================================
# 2. QUANTUM KERNEL ESTIMATION ENGINE
# =====================================================================
class QuantumKernelClassifier:
    """
    Quantum Kernel Engine using QpiAI Quantum SDK.
    Supports ZZ-Ring, IQP, and Hardware-Efficient Ansatzes.
    """
    def __init__(self, ansatz: str = "zz_ring", gamma: float = 0.25, C: float = 1.0):
        self.ansatz = ansatz
        self.gamma = gamma
        self.C = C
        self.svm = SVC(kernel="precomputed", probability=True, C=C, random_state=42)
        self.X_train = None

    def _build_circuit(self, x: np.ndarray) -> qq.Circuit:
        n_qubits = len(x)
        c = qq.Circuit(n_qubits)
        
        if self.ansatz == "zz_ring":
            for i in range(n_qubits):
                c.h(i)
                c.rz(i, float(self.gamma * x[i]))
            for i in range(n_qubits):
                tgt = (i + 1) % n_qubits
                c.cx(i, tgt)
                c.rz(tgt, float(self.gamma * (np.pi - x[i]) * (np.pi - x[tgt])))
                c.cx(i, tgt)
                
        elif self.ansatz == "iqp":
            for i in range(n_qubits):
                c.h(i)
                c.rz(i, float(self.gamma * x[i]))
            for i in range(0, n_qubits - 1, 2):
                c.cx(i, i + 1)
                c.rz(i + 1, float(self.gamma * 2.0 * x[i] * x[i + 1]))
                c.cx(i, i + 1)
            for i in range(n_qubits):
                c.h(i)
                
        elif self.ansatz == "hardware_efficient":
            for i in range(n_qubits):
                c.ry(i, float(x[i]))
                c.rz(i, float(self.gamma * x[i]))
            for i in range(n_qubits - 1):
                c.cx(i, i + 1)
            c.cx(n_qubits - 1, 0)
            for i in range(n_qubits):
                c.ry(i, float(x[i] * 0.5))
                
        return c

    def _compute_statevectors(self, X: np.ndarray) -> np.ndarray:
        sv_list = []
        for x in X:
            c = self._build_circuit(x)
            sv = qq.Statevector(c).data
            sv_list.append(sv)
        return np.array(sv_list)

    def _compute_kernel_matrix(self, sv_A: np.ndarray, sv_B: np.ndarray) -> np.ndarray:
        """Computes transition fidelity K_ij = |<psi_A | psi_B>|^2."""
        inner_products = np.dot(sv_A, sv_B.conj().T)
        K = np.abs(inner_products) ** 2
        return np.clip(K, 0.0, 1.0)

    def fit(self, X: np.ndarray, y: np.ndarray):
        self.X_train = X
        self.sv_train = self._compute_statevectors(X)
        K_train = self._compute_kernel_matrix(self.sv_train, self.sv_train)
        self.svm.fit(K_train, y)
        return self

    def predict_proba(self, X: np.ndarray) -> np.ndarray:
        sv_test = self._compute_statevectors(X)
        K_test = self._compute_kernel_matrix(sv_test, self.sv_train)
        return self.svm.predict_proba(K_test)


def run_qml_suite(data: Dict[str, np.ndarray]) -> Dict[str, Dict]:
    """Runs all QML architectures and records performance metrics."""
    X_tr = data["X_q8_train"]
    y_tr = data["y_train"]
    X_te = data["X_q8_test"]
    y_te = data["y_test"]

    qml_results = {}

    # 1. Pure Variational Quantum Classifier (VQC)
    print("\n[QML 1/4] Training Pure Variational Quantum Classifier (VQC)...")
    t0 = time.time()
    vqc = PureVariationalQuantumClassifier(n_qubits=8, depth=2, n_epochs=20)
    vqc.fit(X_tr, y_tr)
    t_train = time.time() - t0
    
    t0 = time.time()
    p_vqc = vqc.predict_proba(X_te)[:, 1]
    t_inf = (time.time() - t0) / len(X_te) * 1000.0
    
    auc = roc_auc_score(y_te, p_vqc)
    pr_auc = average_precision_score(y_te, p_vqc)
    acc = accuracy_score(y_te, (p_vqc >= 0.5).astype(int))
    f1 = f1_score(y_te, (p_vqc >= 0.5).astype(int))
    brier = brier_score_loss(y_te, p_vqc)
    
    qml_results["Pure VQC (QNN)"] = {
        "roc_auc": round(auc, 4),
        "pr_auc": round(pr_auc, 4),
        "accuracy": round(acc, 4),
        "f1": round(f1, 4),
        "brier": round(brier, 4),
        "train_time_sec": round(t_train, 2),
        "inf_latency_ms": round(t_inf, 2),
        "p_active": p_vqc,
        "type": "Pure Variational Quantum (No SVM)"
    }
    print(f"  > VQC Finished — ROC-AUC: {auc:.4f}, PR-AUC: {pr_auc:.4f}, Acc: {acc:.2%}")

    # 2. QKE - ZZ-Ring Ansatz
    print("\n[QML 2/4] Training QKE (ZZ-Ring Entanglement)...")
    t0 = time.time()
    qke_zz = QuantumKernelClassifier(ansatz="zz_ring", gamma=0.25)
    qke_zz.fit(X_tr, y_tr)
    t_train = time.time() - t0
    
    t0 = time.time()
    p_zz = qke_zz.predict_proba(X_te)[:, 1]
    t_inf = (time.time() - t0) / len(X_te) * 1000.0
    
    auc = roc_auc_score(y_te, p_zz)
    pr_auc = average_precision_score(y_te, p_zz)
    acc = accuracy_score(y_te, (p_zz >= 0.5).astype(int))
    f1 = f1_score(y_te, (p_zz >= 0.5).astype(int))
    brier = brier_score_loss(y_te, p_zz)
    
    qml_results["QKE ZZ-Ring"] = {
        "roc_auc": round(auc, 4),
        "pr_auc": round(pr_auc, 4),
        "accuracy": round(acc, 4),
        "f1": round(f1, 4),
        "brier": round(brier, 4),
        "train_time_sec": round(t_train, 2),
        "inf_latency_ms": round(t_inf, 2),
        "p_active": p_zz,
        "type": "Quantum Kernel (ZZ Entanglement)"
    }
    print(f"  > QKE ZZ-Ring Finished — ROC-AUC: {auc:.4f}, PR-AUC: {pr_auc:.4f}, Acc: {acc:.2%}")

    # 3. QKE - IQP Ansatz
    print("\n[QML 3/4] Training QKE (IQP Polynomial)...")
    t0 = time.time()
    qke_iqp = QuantumKernelClassifier(ansatz="iqp", gamma=0.20)
    qke_iqp.fit(X_tr, y_tr)
    t_train = time.time() - t0
    
    t0 = time.time()
    p_iqp = qke_iqp.predict_proba(X_te)[:, 1]
    t_inf = (time.time() - t0) / len(X_te) * 1000.0
    
    auc = roc_auc_score(y_te, p_iqp)
    pr_auc = average_precision_score(y_te, p_iqp)
    acc = accuracy_score(y_te, (p_iqp >= 0.5).astype(int))
    f1 = f1_score(y_te, (p_iqp >= 0.5).astype(int))
    brier = brier_score_loss(y_te, p_iqp)
    
    qml_results["QKE IQP"] = {
        "roc_auc": round(auc, 4),
        "pr_auc": round(pr_auc, 4),
        "accuracy": round(acc, 4),
        "f1": round(f1, 4),
        "brier": round(brier, 4),
        "train_time_sec": round(t_train, 2),
        "inf_latency_ms": round(t_inf, 2),
        "p_active": p_iqp,
        "type": "Quantum Kernel (IQP Diagonal)"
    }
    print(f"  > QKE IQP Finished — ROC-AUC: {auc:.4f}, PR-AUC: {pr_auc:.4f}, Acc: {acc:.2%}")

    # 4. QKE - Hardware Efficient Ansatz (HEA)
    print("\n[QML 4/4] Training QKE (Hardware Efficient Ansatz)...")
    t0 = time.time()
    qke_hea = QuantumKernelClassifier(ansatz="hardware_efficient", gamma=0.30)
    qke_hea.fit(X_tr, y_tr)
    t_train = time.time() - t0
    
    t0 = time.time()
    p_hea = qke_hea.predict_proba(X_te)[:, 1]
    t_inf = (time.time() - t0) / len(X_te) * 1000.0
    
    auc = roc_auc_score(y_te, p_hea)
    pr_auc = average_precision_score(y_te, p_hea)
    acc = accuracy_score(y_te, (p_hea >= 0.5).astype(int))
    f1 = f1_score(y_te, (p_hea >= 0.5).astype(int))
    brier = brier_score_loss(y_te, p_hea)
    
    qml_results["QKE Hardware-Efficient"] = {
        "roc_auc": round(auc, 4),
        "pr_auc": round(pr_auc, 4),
        "accuracy": round(acc, 4),
        "f1": round(f1, 4),
        "brier": round(brier, 4),
        "train_time_sec": round(t_train, 2),
        "inf_latency_ms": round(t_inf, 2),
        "p_active": p_hea,
        "type": "Quantum Kernel (HEA Cyclic)"
    }
    print(f"  > QKE HEA Finished — ROC-AUC: {auc:.4f}, PR-AUC: {pr_auc:.4f}, Acc: {acc:.2%}")

    return qml_results

if __name__ == "__main__":
    from data_loader import get_benchmark_dataset
    data = get_benchmark_dataset(n_train=120, n_test=60)
    results = run_qml_suite(data)
    for k, v in results.items():
        print(f"{k}: AUC={v['roc_auc']}, Acc={v['accuracy']}, TrainTime={v['train_time_sec']}s")
