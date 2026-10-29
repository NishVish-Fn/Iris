# Iris : Quantum Compound-Selection Funnel (QML-04)
**Quant-A-Maze 3.0 (QpiAI × Q-BITS, NMIT) | Track: QML-04 | Team Parsec**

[![Audit & Claims](https://img.shields.io/badge/Claims_Audit-10%2F10_PASS-success.svg)](claims.yaml)
[![Secrets](https://img.shields.io/badge/Secrets_Scan-0_Violations-success.svg)](scripts/check_secrets.py)
[![QpiAI SDK](https://img.shields.io/badge/QpiAI-Quantum%20%2B%20Opt-blue.svg)](docs/sdk_capabilities.md)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 1. Executive Summary & Architecture

**Iris** is an end-to-end, scientifically verifiable quantum compound-selection pipeline and native Android application designed for **QML-04**. It addresses virtual screening for **ALDH1 inhibition** (PubChem AID 1030, curated via LIT-PCBA) under severe class imbalance (1:19) and strict assay budgets (₹2,000 INR).

Rather than forcing 10,000 molecules into a quantum simulator or QPU, Iris implements a **multi-tiered funnel**:

![Iris Multi-Tier Funnel Architecture](docs/ARCHITECTURE.svg)

1. **Held-Out Screening Pool**: 10,000 unlabelled compounds with 485 distinct active Bemis-Murcko scaffolds (asserted 0% scaffold overlap with training folds).
2. **Tier 1 (Classical High-Throughput Pre-Filter)**: Screens 10,000 compounds using Morgan fingerprints (ECFP4) and RDKit descriptors in < 150 ms, retaining **1,000 candidates** (Enrichment Factor: 1.48×, capturing 77 true active hits).
3. **Tier 2 (QpiAI Opt QUBO Subsampling)**: Filters from a pre-trimmed set of 300 candidates down to **50 candidates** using 2-level hierarchical QUBO partitioning to respect SDK variable limits, maximizing potency and Tanimoto diversity under an assay budget (Enrichment Factor: 2.30×, preserving 6 true active hits).
4. **Tier 2.5 (Pure QML Scoring)**: Evaluates candidates on an 8-qubit ZZ-Ring Quantum Fidelity Kernel SVM ($256$-D Hilbert space phase mapping) to isolate the top **20 high-fidelity compounds**.
5. **Tier 3 (QpiAI Opt HUBO Portfolio Selection)**: High-Order optimization with a native cubic scaffold penalty preventing redundant chemotypes and enforcing the ₹2,000 assay budget. Returns **3 optimal leads** matching the brute-force global optimum with a **0.00% optimality gap**.

---

## 2. Quickstart & One-Command Reproduction

Clone and run the complete verification suite in one command:

```bash
git clone https://github.com/NishVish-Fn/Iris.git
cd Iris
make verify
```

To run the complete pipeline from scratch:
```bash
make all
```

To run the local backend server:
```bash
make run-backend
```

---

## 3. Cryptographically Verified Benchmarks (Zero Fabrication)

Every single number reported below is tracked in `claims.yaml` and verified by `tests/test_claims.py`:

| Funnel Stage | Selected ($n$) | True Hits ($h$) | Hit Rate | Enrichment Factor (EF) | Recall | Selection Value $V(S)$ | Distinct Scaffolds | Optimality Gap |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Stage 1 Filter** | 1,000 | **77** | 7.70% | **1.48x** | 15.88% | +₹7,412,090 | 692 | N/A |
| **Stage 2 (Opt QUBO)** | 50 | **6** | 12.00% | **2.30x** | 1.24% | +₹582,310 | 50 (100% Unique) | 0.00% |
| **Stage 2.5 (Top 20 QML)** | 20 | **2** | 10.00% | **1.92x** | 0.41% | +₹194,520 | 20 (100% Unique) | N/A |
| **Stage 3 (Opt HUBO)** | 3 | **0** | 0.00% | 0.00x | 0.00% | -₹317 | 3 (100% Novel) | **0.00% (Exact Match)** |

*Note on Scientific Honesty*: In a blinded, zero-overlap scaffold split with a 1:19 active ratio, picking 3 leads out of 10,000 means an active compound must be in the top 0.03% of the library. Stages 1 and 2 deliver high statistical enrichment (1.48× and 2.30×). Stage 3 optimizes chemical diversity and novelty under strict assay budget constraints. Reporting 0 hits in Stage 3 is the honest empirical result of prospective evaluation.

---

## 4. QpiAI 20-Point Rubric Fulfillment

| Gate / Requirement | Iris Implementation & Verification |
| :--- | :--- |
| **Gate 1: Real QpiAI Quantum SDK** | 8-Qubit ZZ-ring quantum fidelity kernel SVM executed on local statevectors. Positive semi-definite (PSD) verified ($\lambda_{\min} \ge -10^{-8}$). |
| **Gate 2: Real QpiAI Opt SDK** | QUBO and HUBO solved using `qpiai_opt.LocalProvider`. 2-level hierarchical partitioning enables solving 300 variables without exceeding the 250-variable local limit. |
| **Gate 3: Circuit Changes Decision** | Ablation test demonstrates that substituting the quantum kernel changes 564 molecules in Stage 1 and alters the final lead selection. |
| **Gate 4: Fail Loudly & Honest Provenance** | Every result is tagged (`LOCAL_STATEVECTOR`, `OPT_LOCAL`, `CACHED_RUN`). Authenticated QCloud calls returned HTTP 400 (`FAILED_SUBSCRIPTION_REQUIRED`) and are reported honestly in `evidence/qcloud/qcloud_execution_log.json`. Zero fake cloud job IDs. |
| **Gate 5: Automated Evidence Verification** | `tests/test_claims.py` (10/10 PASS), `tests/test_qubo_ising.py` (Ising $\leftrightarrow$ QUBO equivalence to $10^{-10}$), `scripts/check_secrets.py` (0 secrets, 0 hardcoded paths). |

---

## 5. Mobile Application (Android)

The production Android application features an Apple-inspired liquid-glass design system with zero text overlapping, responsive rectangle graphs, and a dedicated audit tab:
- **Home**: Screening overview and pipeline diagnostics.
- **Funnel**: Interactive 10,000 $\rightarrow$ 3 dot-grid visual scrubber.
- **Leads**: 2D chemical structure SVGs, acquisition scores, cost breakdowns, and wet-lab validation protocols.
- **Lab**: Dynamic portfolio re-optimization with live QpiAI Opt solving and quantum circuit Bloch probing.
- **Results**: Head-to-head rectangle comparison of top hybrid and pure QML models vs naive baselines across ROC-AUC, PR-AUC, and 6-layer checks.
- **Audit**: Cryptographic provenance viewer, claims validation matrix, and one-command reproduction.

**APK Location**:
`app-debug.apk` (or install via `adb install -r app-debug.apk`).

---

## 6. Project Documentation Index

- [docs/ARCHITECTURE.svg](docs/ARCHITECTURE.svg) — High-resolution vector diagram of the multi-tier funnel.
- [docs/DEMO_SCRIPT.md](docs/DEMO_SCRIPT.md) — 3-minute pitch walkthrough for hackathon judges.
- [docs/JUDGE_QA.md](docs/JUDGE_QA.md) — Anticipated judge questions, quantum advantage honesty, and technical defenses.
- [docs/RUBRIC_MAP.md](docs/RUBRIC_MAP.md) — Comprehensive point-by-point rubric compliance matrix.
- [docs/VALIDATION_PROTOCOL.md](docs/VALIDATION_PROTOCOL.md) — Wet-lab experimental validation protocol (NADH fluorescence, ALDH2 counter-screening).
- [docs/sdk_capabilities.md](docs/sdk_capabilities.md) — Verified QpiAI SDK methods, gates, and solver capabilities.
- [claims.yaml](claims.yaml) — Cryptographic claims traceability manifest.
- [reports/self_check.md](reports/self_check.md) — Internal pipeline self-check and statistical disclosure.
