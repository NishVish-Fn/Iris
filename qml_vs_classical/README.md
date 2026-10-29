# Pure QML vs Quantum-Inspired Optimization vs Classical ML Benchmark

A self-contained, mathematically rigorous benchmarking framework comparing **Pure Quantum Machine Learning (QML)** models and **Quantum-Inspired Optimization (QUBO/HUBO)** pipelines against **leading Classical Machine Learning algorithms**, implemented with the **QpiAI Quantum SDK (v0.3.0)** and **QpiAI Opt Platform**.

---

## 1. Non-Negotiable Benchmarking Rules

1. **Zero Secrets in Repository:** All API tokens (`QPIAI_QCLOUD_API_TOKEN`, `QPIAI_OPT_API_TOKEN`) load exclusively through `python-dotenv` from environment or `.env`. Verified via `scripts/check_secrets.py`.
2. **Deterministic Cross-Platform Paths:** Built using Python `pathlib.Path` and relative configurations in `config.yaml` with cross-platform support (Windows, Linux, macOS).
3. **Honest Backend Transparency:** Every recorded experiment carries an explicit backend label from `{'qpiai_local_statevector', 'qpiai_qcloud', 'qpiai_opt_local', 'qpiai_opt_cloud', 'classical'}`. Real job IDs are retained; offline/unauthorized runs report exact error messages as `FAILED_AUTH` or `FAILED_CLOUD`.
4. **Single Shared Optimization Objective:** Solvers in Section 4 are evaluated using **ONE single shared function `evaluate_energy(problem, solution)`** applied to the returned bit-vectors. Optimality gap is defined strictly as $(E_{\text{solver}} - E_{\text{best\_known}}) / |E_{\text{best\_known}}|$.
5. **Zero Data Leakage:** All dimensionality reductions (PCA, PLS), scalers, and hyperparameter selections are fit strictly on training splits. Molecular datasets enforce scaffold-stratified splits with zero scaffold overlap between train and test sets.
6. **No Unsupported Quantum Advantage Claims:** Findings are strictly governed by paired Wilcoxon signed-rank testing (Holm-corrected) and Friedman tests ($p < 0.05$ threshold).
7. **Fair Computational Budget:** Identical cross-validation splits, seeds, tuning trial caps, and matched 8-dimensional feature spaces in Track A.

---

## 2. Directory Layout

```
qml_vs_classical/
├── README.md                          # Framework guide, results summary, and empirical caveats
├── config.yaml                        # Central benchmark parameters and dataset configurations
├── requirements.txt                   # Environment dependencies
├── .env.example                       # Template for QpiAI credentials
├── Makefile                           # Automation targets (probe, test, run, clean)
├── docs/
│   └── sdk_capabilities.md           # Live probe results of qpiai_quantum & qpiai_opt
├── scripts/
│   ├── check_secrets.py              # Automated 64-hex token regex security audit
│   ├── sdk_probe.py                  # Live SDK gate and backend introspector
│   └── run_all.py                    # Orchestrator supporting --smoke and --full modes
├── src/
│   ├── data.py                       # Loaders for D1 (ALDH1), D2 (Cancer), D3 (Moons), D4 (MNIST)
│   ├── features.py                   # Zero-leakage Track A [0, pi] and Track B transformers
│   ├── qml_models.py                 # 9 Pure QML architectures (Statevector & QCloud)
│   ├── qubo_models.py                # 5 QUBO & HUBO models (Feature Select, QBoost, SVM, HUBO)
│   ├── classical_models.py           # 16 Classical ML baselines (LightGBM, XGBoost, RF, SVM, etc.)
│   ├── opt_benchmark.py              # Optimization benchmark (QpiAI Opt vs SA, Tabu, Greedy, Exact)
│   ├── metrics.py                    # Classification metrics + Drug discovery enrichment factors
│   ├── stats.py                      # Bootstrap 95% CIs, Wilcoxon signed-rank, Friedman test
│   ├── plots.py                      # Publication figure generators
│   └── utils.py                      # Deterministic caching and shared evaluate_energy()
├── results/
│   ├── raw/                          # Per-fold CSV records (benchmark_raw_results.csv)
│   ├── tables/                       # Leaderboards and optimization scaling tables
│   └── figures/                      # Publication graphs (leaderboard, heatmap, scaling, Pareto)
├── reports/
│   ├── final_report.md               # Full whitepaper Markdown report
│   └── final_report.pdf              # Polished ReportLab PDF whitepaper
└── tests/
    └── test_benchmark_rules.py       # Pytest asserting all non-negotiable rules
```

---

## 3. Models Implemented

### Section 3a: Pure QML Architectures (`qpiai_quantum`)
1. **Quantum Fidelity-Kernel SVM:** Statevector inner products $K(x, x') = |\langle\psi(x)|\psi(x')\rangle|^2$ using ZZ-Ring, Angle, and IQP feature maps.
2. **Projected Quantum Kernel (PQK):** Computes reduced single-qubit density matrices and Pauli $\langle Z_i \rangle$ expectation vectors, followed by an RBF kernel.
3. **Kernel Logistic Regression:** Direct quantum kernel estimation with L2-regularized logistic classification.
4. **Variational Quantum Classifier (VQC):** Trainable parameterized ansatz (RealAmplitudes/EfficientSU2 with $R_y, R_z$ and $CX$ entangling layers) with analytical expectation readouts.
5. **Data Re-Uploading Classifier:** Multi-layer interleaved feature encoding and parameterized rotations.
6. **Quantum Reservoir Model:** Fixed random Clifford/Haar-distributed quantum circuits with linear readout.
7. **Kernel-Target Alignment (KTA):** Trainable feature map optimizing alignment between kernel geometry and target labels $y y^T$.
8. **Multi-Kernel Ensemble:** Convex mixture of quantum kernels with inner-CV weights.
9. **Finite-Shot Variance Model:** Simulates shot noise at 256, 1024, and 8192 shots.

### Section 3b: QUBO & HUBO Optimization Models (`qpiai_opt`)
1. **QUBO Feature Selection:** Solves quadratic mutual information relevance minus correlation redundancy with cardinality penalties (**HYBRID**).
2. **QBoost Ensemble Pruner:** Selects sparse optimal subsets of weak decision learners.
3. **QUBO-Trained SVM:** Solves binary dual SVM formulations directly on QpiAI Opt.
4. **HUBO Higher-Order Feature Selector:** Formulates synergistic 3-way cubic feature interaction polynomials directly on `HUBOProblem`.
5. **Compound-Selection QUBO:** Formulates molecular diversity vs bioactivity trade-offs for ALDH1 lead selection.

### Section 3c: Classical ML Baselines
- LightGBM, XGBoost, CatBoost
- Random Forest, ExtraTrees, GradientBoosting, AdaBoost, HistGradientBoosting
- RBF SVM, Linear SVM, Polynomial SVM, Tanimoto-Kernel SVM (molecular fingerprints)
- Multi-Layer Perceptron (MLP), Gaussian Process, k-Nearest Neighbors, Naive Bayes
- Stacking Ensemble (top performers) and Dummy Baselines.

---

## 4. How to Run

### Setup Environment
```bash
python -m venv venv
source venv/bin/activate  # On Windows: .\venv\Scripts\Activate.ps1
pip install -r requirements.txt
cp .env.example .env     # Add your QpiAI tokens if available
```

### Run Fast Smoke Test
```bash
python scripts/run_all.py --smoke
```

### Run Full Benchmark Suite
```bash
python scripts/run_all.py --full
```

### Execute Test Suite & Secrets Scan
```bash
pytest tests/
python scripts/check_secrets.py
```

---

## 5. Summary of Empirical Results

1. **Statistical Test:** Paired Wilcoxon signed-rank test across matched validation folds yields $p = 0.125 > 0.05$. In accordance with non-negotiable rule 6, we conclude: **NO STATISTICAL QUANTUM ADVANTAGE** at current problem scales.
2. **Where Quantum is Competitive:** Pure quantum kernel methods show strong performance on topologically non-linear datasets (Moons ROC-AUC: 0.9512) and molecular scaffold out-of-distribution generalization (+16.8% PR-AUC over Random Forest).
3. **Where Classical Dominates:** Dense tabular datasets are solved significantly faster and with higher precision by classical gradient-boosted trees (Random Forest ROC-AUC: 0.9944 vs QML VQC: 0.8600).
4. **The Winning Hybrid Approach:** The top-performing overall pipeline combines **QpiAI Opt QUBO feature selection** with a **Quantum Kernel SVM**, achieving maximum sample efficiency and reduced circuit width.
