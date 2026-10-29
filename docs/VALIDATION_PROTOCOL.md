# Experimental Wet-Lab Validation Protocol for ALDH1 Lead Inhibitors

This document outlines the biochemical testing cascade and rigorous quality controls for validating the top lead compounds selected by the MolFunnel screening platform.

---

## 1. Assay Overview & Reagents

| Assay Component | Specification | Purpose |
|---|---|---|
| **Target Enzyme** | Human Recombinant ALDH1A1 (expressed in *E. coli*, >95% purity) | Primary therapeutic target |
| **Substrate** | Propionaldehyde or Acetaldehyde ($1.0\text{ mM}$) + $\text{NAD}^+$ ($1.0\text{ mM}$) | Dehydrogenase reaction substrate |
| **Detection Method** | Kinetic NADH fluorescence ($\lambda_{ex} = 340\text{ nm}$, $\lambda_{em} = 460\text{ nm}$) | Measures enzyme activity rate |
| **Positive Control** | DEAB (4-Diethylaminobenzaldehyde), $\text{IC}_{50} \approx 50\text{ nM}$ | Known standard ALDH1 inhibitor |
| **Negative Control** | 1.0% DMSO vehicle in assay buffer | 100% baseline enzyme activity |
| **Assay Buffer** | $50\text{ mM}$ Sodium Pyrophosphate, $\text{pH } 9.0$, $1\text{ mM}$ DTT, $0.01\%$ Triton X-100 | Optimal kinetic buffer conditions |

---

## 2. Plate Quality & Acceptance Criteria

To guarantee experimental validity in 384-well microplate format:
1. **Z'-Factor Acceptance**:
   Every screening plate must achieve:
   $$Z' = 1 - \frac{3(\sigma_{pos} + \sigma_{neg})}{|\mu_{pos} - \mu_{neg}|} \ge 0.50$$
   Plates with $Z' < 0.50$ are discarded and re-assayed.
2. **Signal-to-Background (S/B)**: Ratio $\ge 5.0$ between negative control and enzyme-free background.
3. **Replication**: All compound measurements are performed in technical duplicates across independent assay plates ($n=2 \times 2$).

---

## 3. 10-Point Dose-Response Curve & $\text{IC}_{50}$ Determination

1. **Serial Dilution Scheme**:
   - 10-point, 3-fold serial dilution in 100% DMSO.
   - Top test concentration: $50\text{ }\mu\text{M}$ down to $2.5\text{ nM}$ (final DMSO concentration fixed at 1.0% v/v).
2. **Curve Fitting**:
   - Percent inhibition normalized against positive and negative controls.
   - Non-linear 4-parameter logistic Hill equation:
     $$\% \text{Inhibition} = \text{Bottom} + \frac{\text{Top} - \text{Bottom}}{1 + \left(\frac{[\text{Compound}]}{\text{IC}_{50}}\right)^{n_H}}$$
   - Acceptance: Hill slope $n_H \in [0.7, 1.5]$ and $R^2 \ge 0.95$.

---

## 4. Orthogonal & Counter-Screening Cascade

To eliminate promiscuous aggregators, fluorescent artifacts, and non-selective molecules:
1. **Detergent Dependency Assay (Aggregation Check)**:
   - Re-test in buffer with $0.01\%$ vs $0.05\%$ Triton X-100.
   - An $\text{IC}_{50}$ shift $>3\text{-fold}$ indicates colloidal aggregation (False Positive Flag).
2. **Isozyme Counter-Screens**:
   - **ALDH2 Counter-Screen**: Assay against human ALDH2 (mitochondrial isozyme). Selectivity index requirement:
     $$\text{SI} = \frac{\text{IC}_{50}\text{ (ALDH2)}}{\text{IC}_{50}\text{ (ALDH1A1)}} \ge 10.0$$
   - **ALDH3A1 Counter-Screen**: Assayed to ensure non-pan-ALDH inhibition.
3. **Redox Cycling Interference Check**:
   - DTT-sparing assay with catalase ($10\text{ }\mu\text{g/mL}$) to exclude compounds generating $\text{H}_2\text{O}_2$.
4. **LC-MS Orthogonal Substrate Consumption**:
   - Measures direct disappearance of 4-nitrobenzaldehyde and appearance of 4-nitrobenzoic acid via MRM LC-MS/MS.

---

## 5. Hit-Calling & Go / No-Go Decision Criteria

| Metric | Confirmed Active (GO) | Indeterminate (FLAG) | Inactive / Discard (NO-GO) |
|---|---|---|---|
| **Primary $\text{IC}_{50}$** | $\text{IC}_{50} \le 10\text{ }\mu\text{M}$ | $10\text{ }\mu\text{M} < \text{IC}_{50} \le 50\text{ }\mu\text{M}$ | $\text{IC}_{50} > 50\text{ }\mu\text{M}$ |
| **Max Efficacy** | $\ge 80\%$ inhibition at $50\text{ }\mu\text{M}$ | $50 - 79\%$ inhibition | $< 50\%$ inhibition |
| **ALDH2 Selectivity** | $\text{Selectivity} \ge 10\text{-fold}$ | $3 - 9\text{-fold}$ selectivity | $< 3\text{-fold}$ selectivity |
| **MedChem Clearance** | 0 PAINS alerts, Brenk clear | $\le 1$ minor alert | $>1$ PAINS / reactive alerts |
| **Solubility** | $>25\text{ }\mu\text{M}$ in PBS buffer | $10 - 25\text{ }\mu\text{M}$ | $< 10\text{ }\mu\text{M}$ |
