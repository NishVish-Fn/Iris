# Iris (MolFunnel) — 3-Minute Hackathon Demo Script
**Track**: QML-04 (Quant-A-Maze 3.0)  
**Goal**: Walk a judge through the problem, the 10,000-to-3 funnel, the QpiAI Quantum + QpiAI Opt implementation, and verifiable evidence of zero data fabrication.

---

## ⏱️ Minute 0:00 – 0:45: The Problem & The Multi-Tier Funnel
- **Opener**:  
  "Virtual screening for cancer stem-cell targets like ALDH1 fails either due to classical brute force screening costs or quantum qubit bottleneck limits. You cannot pass 10,000 molecules directly into a quantum simulator or QPU—it would cost millions of credits and take months.
  **Iris** is a tiered screening funnel that marries classical high-throughput filtering, QpiAI Opt combinatorial portfolio optimization, and pure QML fidelity kernel SVMs to narrow 10,000 compounds down to 3 optimal, chemically diverse leads under an assay budget."
- **Visual**:  
  *Open Home Screen / Architecture Diagram (`docs/ARCHITECTURE.svg`)*.  
  Show the 4 tiers:
  1. **Tier 1 (Classical)**: 10,000 → 1,000 (removes 90% in <150ms, EF = 1.48×)
  2. **Tier 2 (QpiAI Opt QUBO)**: 300 pre-trimmed → 50 (optimizes potency + Tanimoto diversity + cost, EF = 2.30×)
  3. **Tier 2.5 (Pure QML)**: 8-Qubit ZZ-Ring Fidelity Kernel SVM selects top 20 candidates
  4. **Tier 3 (QpiAI Opt HUBO)**: High-Order optimization with cubic scaffold penalty selects the final 3 diverse leads matching the global brute-force optimum (0.00% optimality gap).

---

## ⏱️ Minute 0:45 – 1:30: Interactive Android App & Rectangle Graph Benchmarks
- **Switch to App**:  
  *Open Results Tab (Bar Chart icon)*.
- **Address Prompt Requirements**:  
  - "Notice our new **Best vs Least Successful Models (Rectangle Graphs)** comparison section. Rather than dense prose, it directly compares the top hybrid and pure QML architectures against baselines (RealAmplitudes VQC, Angle Maps, Gaussian Naive Bayes) using visual rectangle bars across ROC-AUC, PR-AUC, and EF@1%."
  - "Below each model, a segmented 6-layer visual gauge verifies compliance across: Lipinski Rule of 5, PAINS clearance, Veber bioavailability, Synthetic Accessibility (SA < 4.0), ALDH1 isozyme specificity (>10× vs ALDH2), and Quantum state entanglement purity."
  - "Notice that long model names like `QpiAI Opt QUBO + ZZ-Ring Quantum Kernel SVM` display fully without truncation, and cards have dedicated bottom clearance so nothing overlaps with the navigation dock."

---

## ⏱️ Minute 1:30 – 2:15: Interactive Lab & Live API Re-optimization
- **Switch to Lab Tab (Sliders icon)**:  
  - "In the **Solver Lab**, users can dynamically adjust exploration bonuses ($\beta$), diversity penalties ($\lambda_{\text{div}}$), novelty weights ($\eta$), and synthetic assay budgets in INR (₹)."
  - "When the user taps **Solve Portfolio**, the app invokes `api.reoptimize()`. If the local FastAPI proxy is running, it executes live through `qpiai_opt.LocalProvider`. If offline, it runs our verified engine tagged honestly as `CACHED_RUN`."
  - "In the **Q-Probe Lab**, inspect the 8-qubit quantum circuit. View the exact Bloch rotation angles and simulated finite-shot noise across 256, 1024, and 8192 shots matching the theoretical $\sqrt{k(1-k)/M}$ bound."

---

## ⏱️ Minute 2:15 – 3:00: Verifiable Evidence & Zero-Fabrication Audit
- **Switch to Audit/Evidence Tab (Shield/Check icon)**:  
  - "Every single number presented in Iris—every AUC, every hit count, every cost value—is cryptographically bound to raw JSON outputs in `results/`."
  - "A judge can run `pytest tests/test_claims.py`, which validates all 10 claims in `claims.yaml`. It returns **10/10 PASS**."
  - "Run `python scripts/check_secrets.py`: **0 secrets leaked**, zero hardcoded tokens, zero personal emails, zero hardcoded Windows paths."
  - "Run `python tests/test_qubo_ising.py`: Mathematical proof of bidirectional equivalence between our QUBO and Ising formulations within $10^{-10}$ precision."
- **Closing Punchline**:  
  "We don't make exaggerated claims of quantum supremacy. On scaffold-separated out-of-distribution test sets, our Friedman test shows $p = 0.373$ between quantum and classical kernels. What Iris delivers is an honest, mathematically rigorous, and reproducible blueprint for practical hybrid drug discovery. You can clone and verify the entire project in one command:
  ```bash
  git clone https://github.com/NishVish-Fn/Iris.git && make all && make verify
  ```
  Thank you, and we welcome your questions!"
