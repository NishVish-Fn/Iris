# Iris (MolFunnel) — Rubric Compliance Matrix
**Hackathon**: Quant-A-Maze 3.0 | **Problem**: QML-04 (Hybrid Quantum Drug Discovery)

This document maps every component, script, test, and artifact of the **Iris** repository directly to the judging criteria.

---

## 1. Common Criteria & Reproducibility (40 Points)

| Rubric Requirement | Iris Implementation | Primary Artifact / Evidence |
| :--- | :--- | :--- |
| **Zero Fabrication / Verifiable Numbers** | Every metric, hit count, and cost cited in docs and UI traces to JSON in `results/`. | `claims.yaml`<br>`tests/test_claims.py` (10/10 PASS) |
| **No Secrets / Credential Security** | Zero tokens or passwords hardcoded. All tokens loaded via `.env` or system environment. | `scripts/check_secrets.py` (0 violations)<br>`.env.example` |
| **Honest Provenance Tagging** | Every run, API response, and UI element carries explicit provenance tags (`LOCAL_STATEVECTOR`, `OPT_LOCAL`, `CACHED_RUN`). Never claims live QPU when using simulators. | `evidence/qcloud/qcloud_execution_log.json`<br>`EvidenceScreen.kt` |
| **One-Command Reproducibility** | Complete pipeline execution and verification in a single make target. | `Makefile`<br>`make all && make verify` |
| **Cross-Platform Compatibility** | Uses `pathlib.Path` throughout; tested on Windows and Unix paths. Zero hardcoded paths. | `pipeline/*.py`, `tests/*.py` |

---

## 2. QML Track & Science Methodology (40 Points)

| Rubric Requirement | Iris Implementation | Primary Artifact / Evidence |
| :--- | :--- | :--- |
| **Rigorous Dataset & Scaffold Splits** | 10,000-compound ALDH1 library (LIT-PCBA AID 1030). Bemis-Murcko scaffold splits with **asserted 0% test scaffold overlap**. | `pipeline/split.py`<br>`data/MANIFEST.json` |
| **Multi-Seed Robustness** | Evaluated across 5 independent random scaffold seeds (11, 22, 33, 44, 55). | `results/multi_seed_metrics.json` |
| **Statistical Rigor & Advantage Honesty** | Friedman test ($\chi^2 = 7.55, p = 0.373$) and Holm-corrected Wilcoxon tests confirm comparable performance between quantum and classical kernels on out-of-distribution scaffolds. Negative/modest result reported honestly. | `results/multi_seed_metrics.json`<br>`docs/PLAN.md` |
| **Quantum Diagnostics** | Quantum Fisher Information, effective dimension capacity gain ($1.67\times$), geometric difference ($g = 1.45$), and positive semi-definite (PSD) verification. | `results/quantum_diagnostics.json` |
| **Uncertainty & Calibration** | Expected Calibration Error (ECE), Brier score, and 90% split-conformal prediction radius ($q = 0.142$). | `results/calibration_conformal.json` |
| **Medicinal Chemistry Validation** | Complete 6-layer qualification: Lipinski Rule-of-5, PAINS alerts (100% pass), Veber bioavailability, Synthetic Accessibility (SA < 4.0), and ALDH1 vs ALDH2 counter-screening. | `pipeline/enrich_medchem.py`<br>`docs/VALIDATION_PROTOCOL.md` |
| **Assay Cost Model** | Cost model verified against empirical screening catalog pricing ($R^2 = 0.9070$, Spearman $\rho = 0.9749$). | `results/cost_model_sensitivity.json` |

---

## 3. QpiAI 20-Point Bonus (SDK Mastery)

| Rubric Requirement | Iris Implementation | Primary Artifact / Evidence |
| :--- | :--- | :--- |
| **QpiAI Quantum SDK Exploration** | Automated capability discovery script probe listing verified gates (`H`, `RZ`, `RX`, `CX`), statevector simulation, and circuit APIs. | `qml_vs_classical/scripts/sdk_probe.py`<br>`docs/sdk_capabilities.md` |
| **QpiAI QCloud Authenticated Integration** | Authentic login via `QpiAIQuantumAuth.login()`, resource discovery, and remote job submission attempt. Transparent logging of subscription requirements. | `evidence/qcloud/qcloud_execution_log.json`<br>`scripts/verify_evidence.py` |
| **Finite-Shot Hardware Noise Study** | Finite-shot measurement simulation across 256, 1024, and 8192 shots compared against theoretical bound $\sqrt{k(1-k)/M}$ ($1.02\times, 0.97\times, 1.02\times$). | `results/shot_noise_analysis.json`<br>`evidence/qcloud/shot_noise_analysis.json` |
| **QpiAI Opt Combinatorial Formulation** | NP-hard QUBO (Stage 2) and cubic HUBO (Stage 3). Mathematical proof of bidirectional QUBO $\leftrightarrow$ Ising equivalence ($< 10^{-10}$ error). | `pipeline/qubo.py`<br>`tests/test_qubo_ising.py` |
| **Hierarchical Partitioning (300 Variables)** | 2-level QUBO partitioning solving full 300 pre-trim variables without exceeding SDK 250-variable local limit. | `pipeline/qubo.py` (`solve_qubo_qpiai_opt_hierarchical_300`) |
| **Solver Benchmarking & Optimality Gap** | QpiAI Opt benchmarked against Simulated Annealing, Tabu, and Greedy across $n \in \{20, 30, 50, 75, 100, 300\}$. QpiAI Opt achieves 0.00% gap vs brute-force global optimum. | `results/opt_benchmark.json`<br>`evidence/opt/opt_benchmark_raw.json` |

---

## 4. QML-04 Deliverables & Mobile Application

| Rubric Requirement | Iris Implementation | Primary Artifact / Evidence |
| :--- | :--- | :--- |
| **Interactive Android App** | Production-ready Jetpack Compose Android application with Apple liquid-glass UI. | `android/app`<br>`app-debug.apk` |
| **Best vs Least Rectangle Graphs** | Head-to-head rectangle comparison of top hybrid and pure QML models vs naive baselines across ROC-AUC, PR-AUC, and 6-layer checks. | `ResultsScreen.kt`<br>(Zero text overlapping) |
| **Dedicated Audit & Evidence Screen** | Interactive audit tab displaying SDK job IDs, cryptographic run hashes, test statuses, and one-command reproduction. | `EvidenceScreen.kt` |
| **FastAPI Backend Proxy** | Local server exposing live SMILES scoring, applicability domain checks, and portfolio re-optimization. | `backend/main.py` |
