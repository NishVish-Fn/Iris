# Iris / MolFunnel (QML-04) — Hackathon Execution Plan

This master plan maps every required engineering, scientific, and verification task directly to the judging criteria:
1. **Common Criteria** (Innovation, Technical Execution, Presentation, Completeness, Reproducibility)
2. **QML Track Criteria** (Quantum Algorithm Design, Fair Baseline Comparison, Quantum Diagnostics & Noise)
3. **QpiAI 20-Point Prize Criteria** (QpiAI Quantum SDK, QpiAI Opt SDK, QCloud & Opt execution, Verifiable Evidence)
4. **QML-04 Challenge Deliverables** (10k -> 300 -> 50 -> 20 -> 3 Funnel, Cheminformatics & Medicinal Chemistry Quality, Wet-Lab Validation Protocol)

---

## Rubric to Task Mapping

| Section | Task Description | Target Rubric Line Earned | Priority & Artifacts |
|---|---|---|---|
| **0. Non-Negotiables** | Zero fabricated/typed-in numbers; trace every claim through `claims.yaml`; replace fake audit with computed `reports/self_check.md`; enforce honest backend provenance tags (`CACHED_RUN`, `LOCAL_STATEVECTOR`, `QCLOUD_JOB`, `OPT_LOCAL`, `OPT_CLOUD`, `CLASSICAL`); remove all hardcoded secrets & personal email; remove hardcoded paths (`pathlib`); delete one-off scripts. | **Common: Reproducibility & Integrity**, **Code Quality** | P0: `claims.yaml`, `tests/test_claims.py`, `scripts/check_secrets.py`, `reports/self_check.md` |
| **1. QpiAI QCloud** | Run `sdk_probe.py` for confirmed capabilities. Run 8-qubit feature maps (ZZ-ring, depth 1, $\gamma=0.25$) on QCloud simulator with shots {256, 1024, 8192}. Compute fidelity kernel from shots. Run subset on `QpiAI-Indus-1` if available. Persist SDK job IDs, backend, raw counts, timestamps to `evidence/qcloud/*.json`. Create `scripts/verify_evidence.py` to re-query SDK. Report shot-kernel vs exact statevector error vs $\sqrt{k(1-k)/M}$ bound. | **QpiAI 20-Point Prize: QCloud Utilization**, **QML Track: Realistic Hardware & Measurement Simulation** | P1: `evidence/qcloud/*.json`, `scripts/verify_evidence.py`, `results/shot_noise_analysis.json` |
| **2. QpiAI Opt** | Stage 2 `pre_trim: 300`. Formulate QUBO (Stage 2), HUBO (Stage 3 cubic scaffold penalty), and equivalent **Ising** form with tested QUBO $\leftrightarrow$ Ising mathematical conversion. Add real assay budget constraint $B$ (in INR). Shared `evaluate_energy`. Optimality gap $(E_{solver} - E_{best\_known})/|E_{best\_known}|$. Benchmark QpiAI Opt against Greedy, SA, and Tabu over $n \in \{20, 30, 50, 75, 100, 300\}$. Save to `evidence/opt/*.json`. | **QpiAI 20-Point Prize: QpiAI Opt SDK (QUBO/HUBO/Ising)**, **QML-04: Combinatorial Selection with Assay Budget** | P1: `pipeline/qubo.py`, `tests/test_qubo_ising.py`, `evidence/opt/*.json`, `results/opt_benchmark.json` |
| **3. Real ALDH1 Science** | Multi-seed evaluation ($\ge 5$ seeds: 11, 22, 33, 44, 55, + 5 more) with scaffold splits (0% scaffold overlap asserted). Run full funnel swapping Quantum Kernel SVM with RBF-SVM (matched 8 features), Tanimoto-SVM, RF, LightGBM, Stacked Ensemble, Max-Tanimoto, and Random. Bootstrap 95% CIs. Fix novel scaffold coverage denominator (total active scaffolds in pool). Paired Wilcoxon (Holm-corrected) & Friedman tests. Quantum diagnostics (kernel concentration, effective dimension, geometric difference, PSD check). Conformal prediction & Platt calibration on held-out scaffold fold. | **QML Track: Rigorous Scientific Benchmarking & No Data Leakage**, **Common: Technical Soundness** | P1: `pipeline/train_models.py`, `pipeline/evaluate.py`, `results/multi_seed_metrics.json`, `results/quantum_diagnostics.json` |
| **4. Deliverable Quality (QML-04)** | Output final 50-batch, top-20 priority list, and 3 lead candidates. Compute RDKit drug-likeness (Lipinski Ro5, Veber, PAINS, Brenk alerts, SA score). Remove fabricated ALDH2 selectivity & fake "quantum purity" claims; define ALDH2 as planned counter-screen. Formal 10-point dose-response validation protocol with positive/negative controls, Z' $\ge 0.5$ acceptance, and hit-calling criteria. Cost model accuracy evaluation & sensitivity analysis ($R, \beta, \eta, \lambda, B$). | **QML-04: Hit Picking, MedChem Filters, Wet-Lab Protocol**, **Common: Completeness** | P1: `results/final_leads.json`, `docs/VALIDATION_PROTOCOL.md`, `results/cost_model_sensitivity.json` |
| **5. Noise, Cost & Hardware Limits** | Study finite shots and depolarizing/readout noise models (`SIMULATED_NOISE`). Calculate QCloud runtime, credit expenditure per kernel entry, projected cost for 10,000 $\times$ 3,000 matrix, and asymptotic hardware scaling boundaries. | **QML Track: NISQ Feasibility Analysis**, **QpiAI 20-Point Prize** | P2: `results/noise_hardware_limits.json`, `docs/HARDWARE_SCALING.md` |
| **6. Engineering & Reproducibility** | One-command execution: `make all` and `make verify` runnable on clean clone across Windows/Linux/macOS. SHA256-verified data downloader (`data/download.py`). Comprehensive pytest suite for CI (scaffold disjointness, preprocessing isolation, kernel PSD, QUBO/Ising conversion, claims verification, secrets scan). | **Common: Reproducibility & Engineering Rigor** | P2: `Makefile`, `data/download.py`, `tests/`, `.github/workflows/ci.yml` |
| **7. Backend & Android App** | Zero secrets in app/backend. FastAPI backend (`backend/main.py`) serving live `/live/score_smiles`, `/live/qprobe`, `/live/demo` without mocks. App UI shows provenance badges (`CACHED`, `LOCAL_STATEVECTOR`, `QCLOUD`, `OPT_LOCAL`). Add new dedicated **Evidence Screen** displaying real job IDs, run hashes, reproducibility commands, and offline cache mode. Fix UI layout and text wrapping in comparison cards. | **Common: User Experience & Responsible Design**, **QML-04: Interactive Tool Delivery** | P2: `backend/main.py`, `android/app/src/main/java/...`, `app-debug.apk` |
| **8. Presentation & Synthesis** | Data-backed `README.md` reflecting only computed results. System architecture vector diagram `docs/ARCHITECTURE.svg`. 3-minute pitch walkthrough `docs/DEMO_SCRIPT.md`. Defense of all tough questions `docs/JUDGE_QA.md`. Detailed rubric mapping `docs/RUBRIC_MAP.md`. Explicit limitations section. | **Common: Presentation & Polish**, **Communication** | P3: `README.md`, `docs/ARCHITECTURE.svg`, `docs/DEMO_SCRIPT.md`, `docs/JUDGE_QA.md`, `docs/RUBRIC_MAP.md` |

---

## Detailed Execution Steps & Order

1. **Phase 0: Sanitization & Hygiene**
   - Delete fake audit files (`generate_audit_pdf_report.py`, `results/IRIS_Pipeline_Comprehensive_Audit_Report.pdf`).
   - Remove helper scripts in `pipeline/` (`check_*`, `list_*`, etc.).
   - Scrub hardcoded secrets and personal email `researcher@qpiai.tech` from all files (Android repository, Python modules).
   - Implement `scripts/check_secrets.py` and enforce in tests.
   - Replace any hardcoded paths with `pathlib.Path`.

2. **Phase 1: QpiAI Quantum & QCloud Evidence Generation**
   - Probe SDK capabilities with `scripts/sdk_probe.py`.
   - Execute circuits with shots {256, 1024, 8192} via QpiAI Quantum / QCloud simulator.
   - Log verified SDK responses into `evidence/qcloud/*.json`.
   - Write `scripts/verify_evidence.py` to cryptographically / API-verify recorded jobs.

3. **Phase 2: QpiAI Opt Enhancements & Benchmarking**
   - Update `pipeline/qubo.py` to handle 300 candidates (`pre_trim: 300`).
   - Implement mathematical QUBO $\leftrightarrow$ Ising conversion and HUBO formulation with unit tests.
   - Add the real INR assay budget penalty and shared `evaluate_energy`.
   - Benchmark QpiAI Opt against Greedy, SA, Tabu over sizes 20 to 300; save to `evidence/opt/*.json`.

4. **Phase 3: ALDH1 Multi-Seed Science & Rigorous Benchmarks**
   - Execute 5+ scaffold splits with zero overlap.
   - Run full funnel across all 8 scorers; compute bootstrap 95% CIs.
   - Fix novel scaffold coverage denominator to pool active scaffolds.
   - Run Wilcoxon and Friedman hypothesis tests.
   - Compute Platt/isotonic calibration, reliability diagrams, conformal prediction intervals, and applicability domain.

5. **Phase 4: Deliverable Quality & Wet-Lab Protocol**
   - Generate final 50 batch, top 20, and top 3 leads with genuine RDKit Lipinski, Veber, PAINS, Brenk, and SA scores.
   - Replace speculative ALDH2 data with a formal ALDH2 counter-screen protocol.
   - Author `docs/VALIDATION_PROTOCOL.md` with Z' factor, 10-point curves, and controls.

6. **Phase 5: Noise, Hardware Limits & Claims Traceability**
   - Execute simulated noise study (`SIMULATED_NOISE`).
   - Draft `claims.yaml` linking every single documented metric to its source JSON.
   - Implement `tests/test_claims.py`. Generate `reports/self_check.md`.

7. **Phase 6: Android App Polish & Backend Integration**
   - Update `backend/main.py` with real model inferences.
   - Fix Android UI text wrapping/truncation in model comparison cards.
   - Add the new Evidence Screen in Jetpack Compose showing job IDs, provenance tags, and offline status.
   - Build and verify debug APK.

8. **Phase 7: Presentation & Final Verification**
   - Update `README.md`, `docs/ARCHITECTURE.svg`, `docs/DEMO_SCRIPT.md`, `docs/JUDGE_QA.md`, and `docs/RUBRIC_MAP.md`.
   - Verify `make all` and `make verify`. Commit clean state.
