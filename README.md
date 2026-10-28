# MolFunnel (Android app): QML-04 Quantum Compound-Selection Funnel
**Quant-A-Maze 3.0 (QpiAI × Q-BITS, NMIT) | Team Parsec**

---

## 1. Project Overview & Architecture
MolFunnel is an end-to-end quantum-classical compound screening pipeline and native Android application designed for **QML-04**. It identifies and selects a high-value experimental batch of small molecules for **ALDH1 inhibition** (PubChem AID 1030, curated via LIT-PCBA), optimizing for predicted activity, chemical diversity, scaffold novelty, synthesis/procurement costs, and target availability.

### Screening Funnel Stages:
1. **10,000 Held-Out Pool Molecules**: Disjoint Bemis-Murcko scaffolds with zero training overlap ($|\text{Scaffolds}(\text{train}) \cap \text{Scaffolds}(\text{pool})| = 0$).
2. **Stage 1 (1,000 Candidates)**: Ranked by net value $v_i = R(P_i + \beta \sigma_i) + \eta \nu_i - c_i$ with a maximum of 5 molecules per scaffold cluster (classical filter).
3. **Stage 2 (50 Candidates)**: Selected via **QpiAI Opt QUBO** using Tanimoto diversity penalties and exact cardinality constraints.
4. **Stage 3 (Final 3 Leads)**: Combinatorially selected via **QpiAI Opt HUBO** with a native cubic term $\omega \sum x_i x_j x_l$ preventing more than 2 molecules from the same scaffold. Verified against ground-truth brute force evaluation of all 19,600 combinations (0.0000 optimality gap).

---

## 2. QpiAI Integration & Compulsory Gates (20-Point Rubric)

### Gate 1 & 2: Real QpiAI Quantum & Opt SDK Usage
- **Quantum Kernel**: Built on 8 qubits with the official `qpiai_quantum` SDK using exact simulator statevector overlaps:
  $$k(x, x') = |\langle 0| U^\dagger(x') U(x) |0\rangle|^2 = |\langle \psi(x')|\psi(x)\rangle|^2$$
- **Combinatorial Optimization**: Stage 2 (QUBO) and Stage 3 (HUBO) solved with `qpiai_opt` using authenticated `LocalProvider` client.

### Gate 3: The Circuit Changes the Decision (Ablation Proof)
- In the Gate 3 ablation experiment (`selection.json`), replacing the Quantum Kernel with an RBF-SVM baseline changed **564 molecules at Stage 1**, directly proving that the quantum circuit alters the final selection decisions rather than acting as a decorative wrapper.

### Gate 4: Fail Loudly
- Missing API keys, network disruptions, or solver infeasibility cause immediate explicit termination. Classical fallbacks are never labeled as QpiAI runs.

### Gate 5: Automated Verification Evidence
- Automated test `tests/test_qpiai_invoked.py` asserts that every cached full run records valid job IDs, timestamps, parameters, and outputs.

---

## 3. Official Evaluation Metrics (Held-Out Reveal)

| Funnel Stage | Selected ($n$) | True Hits ($h$) | Hit Rate | Enrichment Factor (EF) | Recall | Hypergeometric $p$ | Selection Value $V(S)$ | Distinct Scaffolds |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Stage 1** | 1,000 | 54 | 5.40% | **1.04x** | 10.36% | $p = 0.41$ | +₹5,295,112 | 692 |
| **Stage 2 (Opt QUBO)** | 50 | 3 | 6.00% | **1.15x** | 0.58% | $p = 0.49$ | +₹294,884 | 41 |
| **Top-20 Batch** | 20 | 1 | 5.00% | **0.96x** | 0.19% | $p = 0.66$ | +₹98,020 | 13 |
| **Stage 3 (Opt HUBO)**| 3 | 0 | 0.00% | 0.00x | 0.00% | $p = 1.00$ | -₹317 | 3 (100% Novel) |

*Note: Per Section 7 instructions, with $k=3$ final molecules, Stages 1 and 2 serve as the primary statistical evidence.*

### 3.1 Multi-Seed Validation Suite (5 Independent Seeds: 11, 22, 33, 44, 55)

All 5 seeds were executed through the identical strict pipeline with disjoint scaffold splits, PLS-8 feature extraction, quantum kernel training, QpiAI Opt QUBO/HUBO solving, and retrospective evaluation against hidden pool labels:

| Metric | Seed 11 | Seed 22 | Seed 33 | Seed 44 | Seed 55 | **Mean ± SD** | Range |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Stage 1 Hits** | 54 | 51 | 83 | 85 | 71 | **68.8 ± 16.2** | [51, 85] |
| **Stage 1 EF** | 1.04x | 1.08x | 1.69x | 1.87x | 1.50x | **1.44 ± 0.37x** | [1.04x, 1.87x] |
| **Stage 1 Recall** | 10.36% | 10.78% | 16.94% | 18.72% | 14.98% | **14.36 ± 3.68%** | [10.36%, 18.72%] |
| **Stage 2 Hits** | 3 | 3 | 4 | 3 | 3 | **3.2 ± 0.4** | [3, 4] |
| **Stage 2 EF** | 1.15x | 1.27x | 1.63x | 1.32x | 1.27x | **1.33 ± 0.18x** | [1.15x, 1.63x] |
| **Top-20 Hits** | 1 | 1 | 1 | 2 | 0 | **1.0 ± 0.7** | [0, 2] |
| **Top-20 EF** | 0.96x | 1.06x | 1.02x | 2.20x | 0.00x | **1.05 ± 0.78x** | [0.00x, 2.20x] |
| **Stage 3 Distinct Scaffolds**| 3 | 3 | 3 | 3 | 3 | **3.0 ± 0.0** | [3, 3] (100% Novel) |
| **Stage 3 Cost (INR)** | ₹317.09 | ₹307.08 | ₹318.53 | ₹309.87 | ₹304.23 | **₹311.36 ± ₹6.24** | [₹304.23, ₹318.53] |
| **Gate 3 Ablation Diff (mols)**| 564 | 304 | 652 | 360 | 344 | **444.8 ± 153.6** | [304, 652] |

---

## 4. Final 3 Selected Leads

### Lead #1
- **PubChem SID**: `24793056`
- **Predicted $P(\text{active})$**: $0.9994 \pm 0.0017$
- **Compound Cost**: ₹97.84 INR (Tier A CoPriNet)
- **Bemis-Murcko Scaffold**: `c1ccc(COc2ccc3ccccc3c2)cc1` (NOVEL SCAFFOLD)
- **Complexity**: 2.37 / 10 (SAScore)

### Lead #2
- **PubChem SID**: `4256629`
- **Predicted $P(\text{active})$**: $0.9996 \pm 0.0011$
- **Compound Cost**: ₹102.73 INR (Tier A CoPriNet)
- **Bemis-Murcko Scaffold**: `c1ccc(CCn2cccc2)cc1` (NOVEL SCAFFOLD)
- **Complexity**: 2.11 / 10 (SAScore)

### Lead #3
- **PubChem SID**: `26751339`
- **Predicted $P(\text{active})$**: $0.9995 \pm 0.0016$
- **Compound Cost**: ₹116.52 INR (Tier A CoPriNet)
- **Bemis-Murcko Scaffold**: `N=C1CCCC2C1CCC1C(=O)N(C3CCCCC3)C(=O)C12` (NOVEL SCAFFOLD)
- **Complexity**: 3.51 / 10 (SAScore)

---

## 5. 3-Minute Judge Demo Script

### Minute 1: The Guided Walkthrough & Funnel
1. Launch MolFunnel on Android.
2. Tap **"START WALKTHROUGH"** on the Home screen to step through the 12 editorial cards (data curation, zero-overlap scaffold split, 8-qubit feature map, Opt stages, and validation plan).
3. Switch to the **Funnel** tab: drag the interactive funnel scrubber to watch the dot-grid canvas transition from 10,000 to 1,000 to 50 to 3.

### Minute 2: Inspecting the Final 3 Leads
1. Tap the **Leads** tab.
2. View the 2D molecular structure panels (vector SVGs rendered with RDKit), monospace PubChem SIDs, acquisition values, cost tiers, and novel scaffold chips.
3. Tap **"WRITTEN VALIDATION PLAN"** to expand the primary NADH fluorescence protocol, orthogonal LC-MS confirmation, ALDH2 selectivity counter-screen, and IC50 dilution schedule.
4. Toggle the **"REVEALED"** badge to inspect binary assay hit labels.

### Minute 3: Interactive Lab & Live QpiAI Tools
1. Navigate to the **Lab** tab:
   - **Re-solve with Opt**: Move the exploration ($\beta$) or diversity ($\lambda$) slider and tap **"RE-SOLVE WITH OPT"** to trigger a live HUBO optimization. View the runtime, job ID, energy, and brute-force optimum.
   - **Quantum Probe**: Select a lead and tap **"RUN PROBE ON QCLOUD"** to view its 8 circuit feature angles, anchor overlaps, and finite-shot noise bound $\pm \sqrt{k(1-k)/M}$.
   - **Be the Screener**: Play the benchmark challenge: select 3 compounds from 12 candidates and tap **"REVEAL TRUTH"** to compare your picks against QpiAI Opt and classical baselines.
2. Check the **Evidence** tab to audit rubric lines A1, A2, and A3, copy the active `run_id`, or copy the `make all` command.

---

## 6. Known Limitations & Quantum Honesty
1. **Classical vs Quantum Advantage**: In tabular and fingerprint space, gradient-boosted trees (LightGBM) and Tanimoto-kernel SVMs remain highly competitive and fast. We do **not** claim quantum advantage; the quantum kernel provides an expressive non-linear Hilbert feature space complementary to tree-based models.
2. **Quota / Subscription Constraints**: Cloud submission of large batch jobs on QCloud requires an active hardware tier subscription. The pipeline logs subscription status per Gate 4 and computes exact statevectors via the QpiAI Quantum local simulator.
3. **Combinatorial HUBO Capacity**: Local HUBO solving is capped at 1,500 polynomial terms (100 variables). Higher-order interactions beyond 1,500 terms must be trimmed or federated to cloud solvers.

---

## 7. Reproduction & Verification

To reproduce the entire pipeline from raw data to bundled assets:
```bash
make all
```

To run individual pipeline stages:
```bash
python pipeline/data.py             # Download & clean ALDH1 assay data
python pipeline/split.py            # Generate zero-leakage scaffold train/pool split
python pipeline/cost.py             # Train CoPriNet cost surrogate and generate cost table
python pipeline/features.py         # Extract & select 8 quantum features
python pipeline/qkernel.py          # Run quantum kernel pipeline & feature map sweep
python pipeline/models_classical.py # Train 7 classical baseline models
python pipeline/funnel.py           # Run funnel & QpiAI Opt QUBO/HUBO
python pipeline/evaluate.py         # Reveal labels & compute primary metrics
python pipeline/export_app_assets.py# Render SVGs and export app JSON
pytest tests/                       # Run automated validation test suite
```

To run the FastAPI live backend server:
```bash
uvicorn backend.main:app --host 0.0.0.0 --port 8000
```
