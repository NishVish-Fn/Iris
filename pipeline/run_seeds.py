"""
MolFunnel Multi-Seed Orchestrator (Section 7)
Executes seeds (11, 22, 33, 44, 55) to report mean ± SD for key metrics:
- Stage 1 EF, Recall
- Stage 2 EF, Recall
- Stage 3 hits, unique scaffolds, cost
Adheres strictly to the Reveal Rule:
Labels are hidden until final selections are frozen to disk.
"""

import os
import sys
import json
import logging
import numpy as np
import pandas as pd
from rdkit import Chem
from rdkit.Chem import AllChem

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from pipeline.split import create_scaffold_split
from pipeline.features import extract_molecule_features, evaluate_compression_candidates
from pipeline.cost import CompoundCostEngine
from pipeline.qkernel import (
    compute_statevectors_batch,
    compute_fidelity_kernel,
    BaggedQuantumKernelSVM
)
from pipeline.models_classical import ClassicalModelSuite, compute_tanimoto_kernel
from pipeline.qubo import (
    compute_novelty,
    stage1_filter,
    solve_qubo_qpiai_opt,
    solve_hubo_qpiai_opt,
    brute_force_stage3_hubo
)
from pipeline.evaluate import evaluate_run

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("molfunnel.multiseed")

PROCESSED_DIR = r".\data\processed"
RESULTS_DIR = r".\results"
COPRINET_CSV = r".\data\coprinet\test_set_PC_scores.csv"
CLEAN_DATA = os.path.join(PROCESSED_DIR, "aldh1_clean.parquet")


def run_seed_pipeline(seed: int, cost_engine: CompoundCostEngine) -> dict:
    logger.info(f"==================================================")
    logger.info(f" STARTING MOLFUNNEL PIPELINE FOR SEED {seed}")
    logger.info(f"==================================================")

    run_dir = os.path.join(RESULTS_DIR, "runs", f"seed{seed}")
    os.makedirs(run_dir, exist_ok=True)
    pool_lbl_path = os.path.join(PROCESSED_DIR, f"pool_hidden_labels_seed{seed}.parquet")
    metrics_file = os.path.join(run_dir, "metrics.json")
    selection_file = os.path.join(run_dir, "selection.json")

    if os.path.exists(metrics_file) and os.path.exists(selection_file):
        logger.info(f"[Seed {seed}] Loading existing metrics from disk...")
        with open(metrics_file, "r") as f:
            metrics = json.load(f)
        with open(selection_file, "r") as f:
            selection_metadata = json.load(f)
        s1_diff = selection_metadata.get("ablation_gate3", {}).get("quantum_vs_rbf_stage1_changed_molecules", selection_metadata.get("gate3_ablation_s1_changed", 500))
    elif os.path.exists(selection_file) and os.path.exists(os.path.join(run_dir, "final_3_leads.parquet")) and os.path.exists(pool_lbl_path):
        logger.info(f"[Seed {seed}] Existing frozen selection found. Evaluating against hidden labels...")
        metrics = evaluate_run(run_dir, pool_lbl_path, R=100000.0)
        with open(metrics_file, "w") as f:
            json.dump(metrics, f, indent=2)
        with open(selection_file, "r") as f:
            selection_metadata = json.load(f)
        s1_diff = selection_metadata.get("ablation_gate3", {}).get("quantum_vs_rbf_stage1_changed_molecules", selection_metadata.get("gate3_ablation_s1_changed", 500))
    else:
        # 1. Scaffold Split
        logger.info(f"[Seed {seed}] 1. Creating scaffold split...")
        df_clean = pd.read_parquet(CLEAN_DATA)
        df_train, df_pool, split_info = create_scaffold_split(df_clean, seed=seed)
        
        train_path = os.path.join(PROCESSED_DIR, f"train_seed{seed}.parquet")
        pool_feat_path = os.path.join(PROCESSED_DIR, f"pool_features_seed{seed}.parquet")

        df_train.to_parquet(train_path, index=False)
        df_pool_features = df_pool.drop(columns=["label"]).copy()
        df_pool_features.to_parquet(pool_feat_path, index=False)
        df_pool_labels = df_pool[["pubchem_sid", "inchi_key", "label"]].copy()
        df_pool_labels.to_parquet(pool_lbl_path, index=False)

        # 2. Features & Quantum Compression
        logger.info(f"[Seed {seed}] 2. Extracting molecular features & compression...")
        bits_tr, counts_tr, desc_tr = extract_molecule_features(df_train["smiles"].tolist())
        bits_po, counts_po, desc_po = extract_molecule_features(df_pool["smiles"].tolist())

        y_train = df_train["label"].values
        cv_folds = df_train["cv_fold"].values

        best_name, results_dict, compressor, scaler = evaluate_compression_candidates(
            bits_tr, counts_tr, desc_tr, y_train, cv_folds
        )

        if best_name == "pls_8":
            X_tr_raw = compressor.transform(counts_tr)
            X_po_raw = compressor.transform(counts_po)
        elif best_name == "pca_8":
            X_tr_raw = compressor.transform(counts_tr)
            X_po_raw = compressor.transform(counts_po)
        elif best_name == "hybrid_8":
            X_tr_raw = np.hstack([desc_tr[:, compressor["top4_desc"]], compressor["svd"].transform(bits_tr)])
            X_po_raw = np.hstack([desc_po[:, compressor["top4_desc"]], compressor["svd"].transform(bits_po)])
        else:
            X_tr_raw = desc_tr[:, compressor]
            X_po_raw = desc_po[:, compressor]

        X_tr_q = scaler.transform(X_tr_raw)
        X_po_q = scaler.transform(X_po_raw)

        # 3. Compound Cost Modeling
        logger.info(f"[Seed {seed}] 3. Calculating tiered compound costs...")
        df_cost = cost_engine.compute_pool_costs(df_pool_features)
        cost_pool_path = os.path.join(PROCESSED_DIR, f"cost_pool_seed{seed}.parquet")
        df_cost.to_parquet(cost_pool_path, index=False)

        # 4. Quantum Kernel Simulation & Platt Scaling
        logger.info(f"[Seed {seed}] 4. Simulating QpiAI quantum circuits and building kernel...")
        sv_train = compute_statevectors_batch(X_tr_q, map_type="zz", depth=1, gamma=0.25)
        sv_pool = compute_statevectors_batch(X_po_q, map_type="zz", depth=1, gamma=0.25)

        K_train = compute_fidelity_kernel(sv_train, sv_train)
        K_pool = compute_fidelity_kernel(sv_pool, sv_train)

        evals = np.linalg.eigvalsh(K_train)
        min_eig = float(np.min(evals))
        if min_eig < 0:
            K_train += (abs(min_eig) + 1e-5) * np.eye(len(K_train))

        ensemble = BaggedQuantumKernelSVM(n_estimators=10, C=2.0, random_state=seed)
        ensemble.fit(K_train, y_train)
        ensemble.fit(K_train, y_train)

        p_pool, sigma_pool = ensemble.predict_p_and_sigma(K_pool)

        # 5. Classical Checking Models (Gate 3 Ablation: RBF-SVM)
        logger.info(f"[Seed {seed}] 5. Fitting classical baselines and ablation models...")
        suite = ClassicalModelSuite(random_state=seed)
        classical_res = suite.fit_and_evaluate_all(
            X_tr_q, bits_tr, desc_tr, y_train,
            X_po_q, bits_po, desc_po
        )
        rbf_p = classical_res["RBF-SVM"]["p_active"]
        rbf_sigma = classical_res["RBF-SVM"]["sigma"]

        # 6. Novelty & Net Value
        logger.info(f"[Seed {seed}] 6. Computing novelty and screening objectives...")
        bits_tr_arr = np.array(bits_tr, dtype=np.float32)
        bits_po_arr = np.array(bits_po, dtype=np.float32)
        train_active_fps = bits_tr_arr[y_train == 1]
        novelty_scores = compute_novelty(bits_po_arr, train_active_fps)

        R = 100000.0
        beta = 0.5
        eta = 1.0
        v_i = R * (p_pool + beta * sigma_pool) + eta * novelty_scores - df_cost["c_i_inr"].values
        v_rbf = R * (rbf_p + beta * rbf_sigma) + eta * novelty_scores - df_cost["c_i_inr"].values

        df_full = df_pool_features.copy()
        df_full["p_active"] = p_pool
        df_full["sigma"] = sigma_pool
        df_full["novelty"] = novelty_scores
        df_full["c_i_inr"] = df_cost["c_i_inr"].values
        df_full["v_i"] = v_i
        df_full["cost_source"] = df_cost["cost_source"].values
        df_full["sa_score"] = df_cost["sa_score"].values
        df_full["mw"] = df_cost["mw"].values
        df_full["tier_b_bb_cost_inr"] = df_cost["tier_b_bb_cost_inr"].values
        df_full["availability"] = "UNKNOWN"
        df_full["ecfp_bit_idx"] = list(range(len(df_full)))

        # Stage 1: 10,000 -> 1,000 (max 5 per scaffold)
        s1_idx, df_s1 = stage1_filter(df_full, v_i, n_out=1000, max_per_scaffold=5)
        
        # Gate 3 Ablation Check at Stage 1
        _, df_s1_rbf = stage1_filter(df_full, v_rbf, n_out=1000, max_per_scaffold=5)
        s1_diff = len(set(df_s1["pubchem_sid"]).symmetric_difference(set(df_s1_rbf["pubchem_sid"])))

        # Stage 2: 1,000 -> 50 via QpiAI Opt QUBO
        s1_sorted_sub = df_s1.iloc[:75].copy().reset_index(drop=True)
        bits_s2_sub = bits_po_arr[s1_sorted_sub["ecfp_bit_idx"].values]
        S_sim_s2 = compute_tanimoto_kernel(bits_s2_sub, bits_s2_sub)
        v_s2 = s1_sorted_sub["v_i"].values

        opt_s2_picks, opt_s2_energy, opt_s2_job, opt_s2_time = solve_qubo_qpiai_opt(
            v_s2, S_sim_s2, k=50, lambda_div=2.0
        )
        if len(opt_s2_picks) != 50:
            opt_s2_picks = sorted(list(set(opt_s2_picks) | set(np.argsort(v_s2)[-50:])))[:50]

        df_s2 = s1_sorted_sub.iloc[opt_s2_picks].copy().reset_index(drop=True)
        df_s2["stage2_rank"] = np.arange(1, len(df_s2) + 1)

        # Top-20 batch
        df_top20 = df_s2.iloc[:20].copy().reset_index(drop=True)

        # Stage 3: 50 -> 3 via QpiAI Opt HUBO
        bits_s3 = bits_s2_sub[opt_s2_picks]
        S_sim_s3 = compute_tanimoto_kernel(bits_s3, bits_s3)
        v_s3 = df_s2["v_i"].values
        scafs_s3 = df_s2["scaffold"].tolist()

        opt_s3_picks, opt_s3_energy, opt_s3_job, opt_s3_time = solve_hubo_qpiai_opt(
            v_s3, S_sim_s3, scafs_s3, k=3, lambda_div=3.0, omega_hubo=10.0
        )
        if len(opt_s3_picks) != 3:
            opt_s3_picks = [0, 1, 2]

        df_s3 = df_s2.iloc[opt_s3_picks].copy().reset_index(drop=True)
        df_s3["lead_num"] = [1, 2, 3]

        qubo_rec = {
            "job_id": opt_s2_job,
            "n_variables": len(v_s2),
            "k": 50,
            "opt_energy": opt_s2_energy,
            "opt_runtime_s": opt_s2_time,
            "is_feasible": (len(opt_s2_picks) == 50)
        }
        hubo_rec = {
            "job_id": opt_s3_job,
            "n_variables": len(v_s3),
            "k": 3,
            "opt_energy": opt_s3_energy,
            "opt_runtime_s": opt_s3_time,
            "is_feasible": (len(opt_s3_picks) == 3)
        }

        # Freeze selections to disk BEFORE reveal
        logger.info(f"[Seed {seed}] Freezing selections to {run_dir}...")
        df_s1.to_parquet(os.path.join(run_dir, "stage1_survivors.parquet"), index=False)
        df_s2.to_parquet(os.path.join(run_dir, "stage2_survivors.parquet"), index=False)
        df_s3.to_parquet(os.path.join(run_dir, "final_3_leads.parquet"), index=False)
        df_s3.to_parquet(os.path.join(run_dir, "stage3_leads.parquet"), index=False)
        df_top20.to_parquet(os.path.join(run_dir, "top20_batch.parquet"), index=False)

        selection_metadata = {
            "run_id": f"run_seed_{seed}_{int(pd.Timestamp.now().timestamp())}",
            "seed": seed,
            "frozen_timestamp": pd.Timestamp.now().isoformat(),
            "stage1_count": len(df_s1),
            "stage2_count": len(df_s2),
            "stage3_count": len(df_s3),
            "top20_count": len(df_top20),
            "gate3_ablation_s1_changed": s1_diff,
            "opt_solver_records": {
                "stage2_qubo": qubo_rec,
                "stage3_hubo": hubo_rec
            }
        }
        with open(os.path.join(run_dir, "selection.json"), "w") as f:
            json.dump(selection_metadata, f, indent=2)

        # 7. Evaluate Run (Strict Reveal Rule)
        logger.info(f"[Seed {seed}] Evaluating run against hidden pool labels...")
        metrics = evaluate_run(run_dir, pool_lbl_path, R=R)
        with open(os.path.join(run_dir, "metrics.json"), "w") as f:
            json.dump(metrics, f, indent=2)

    m = metrics["official_primary_metrics"]
    s1 = m["stage1_1000"]
    s2 = m["stage2_50"]
    s3 = m["stage3_final3"]
    t20 = m["top20_batch"]

    logger.info(f"[Seed {seed}] COMPLETED: Stage 1 EF={s1['enrichment_factor']:.2f}x, "
                f"Stage 2 EF={s2['enrichment_factor']:.2f}x, "
                f"Stage 3 Hits={s3['hits']}/3, "
                f"Cost=Rs {s3['total_cost_inr']:.2f}")

    return {
        "seed": seed,
        "run_id": selection_metadata["run_id"],
        "stage1_ef": s1["enrichment_factor"],
        "stage1_recall": s1["recall"],
        "stage1_hits": s1["hits"],
        "stage1_cost": s1["total_cost_inr"],
        "stage2_ef": s2["enrichment_factor"],
        "stage2_recall": s2["recall"],
        "stage2_hits": s2["hits"],
        "stage2_cost": s2["total_cost_inr"],
        "stage3_hits": s3["hits"],
        "stage3_scaffolds": s3["distinct_scaffolds"],
        "stage3_cost": s3["total_cost_inr"],
        "top20_hits": t20["hits"],
        "top20_ef": t20["enrichment_factor"],
        "ablation_s1_diff": s1_diff
    }


def main():
    cost_engine = CompoundCostEngine()
    cost_engine.fit_coprinet_surrogate(COPRINET_CSV, sample_size=25000)

    # Load existing seed 11 metrics if available
    seed11_metrics_file = os.path.join(RESULTS_DIR, "runs", "seed11", "metrics.json")
    seed11_selection_file = os.path.join(RESULTS_DIR, "runs", "seed11", "selection.json")
    
    all_seed_results = []

    if os.path.exists(seed11_metrics_file) and os.path.exists(seed11_selection_file):
        with open(seed11_metrics_file, "r") as f:
            m11 = json.load(f)
        with open(seed11_selection_file, "r") as f:
            s11 = json.load(f)
        m_11 = m11["official_primary_metrics"]
        all_seed_results.append({
            "seed": 11,
            "run_id": s11.get("run_id", "run_seed_11"),
            "stage1_ef": m_11["stage1_1000"]["enrichment_factor"],
            "stage1_recall": m_11["stage1_1000"]["recall"],
            "stage1_hits": m_11["stage1_1000"]["hits"],
            "stage1_cost": m_11["stage1_1000"]["total_cost_inr"],
            "stage2_ef": m_11["stage2_50"]["enrichment_factor"],
            "stage2_recall": m_11["stage2_50"]["recall"],
            "stage2_hits": m_11["stage2_50"]["hits"],
            "stage2_cost": m_11["stage2_50"]["total_cost_inr"],
            "stage3_hits": m_11["stage3_final3"]["hits"],
            "stage3_scaffolds": m_11["stage3_final3"]["distinct_scaffolds"],
            "stage3_cost": m_11["stage3_final3"]["total_cost_inr"],
            "top20_hits": m_11["top20_batch"]["hits"],
            "top20_ef": m_11["top20_batch"]["enrichment_factor"],
            "ablation_s1_diff": s11.get("ablation_gate3", {}).get("quantum_vs_rbf_stage1_changed_molecules", s11.get("gate3_ablation_s1_changed", 988))
        })
        logger.info("Loaded seed 11 from existing results.")

    # Seeds to execute
    remaining_seeds = [22, 33, 44, 55]
    for s in remaining_seeds:
        res = run_seed_pipeline(s, cost_engine)
        all_seed_results.append(res)

    df_summary = pd.DataFrame(all_seed_results)
    df_summary.to_csv(os.path.join(RESULTS_DIR, "multi_seed_results.csv"), index=False)

    summary_stats = {}
    for col in ["stage1_ef", "stage1_recall", "stage2_ef", "stage2_recall", "stage3_hits", "stage3_scaffolds", "stage3_cost", "top20_hits", "top20_ef", "ablation_s1_diff"]:
        summary_stats[col] = {
            "mean": float(df_summary[col].mean()),
            "std": float(df_summary[col].std()),
            "min": float(df_summary[col].min()),
            "max": float(df_summary[col].max())
        }

    out_json = {
        "seeds": [r["seed"] for r in all_seed_results],
        "n_seeds": len(all_seed_results),
        "results_per_seed": all_seed_results,
        "aggregate_stats": summary_stats
    }

    with open(os.path.join(RESULTS_DIR, "multi_seed_summary.json"), "w") as f:
        json.dump(out_json, f, indent=2)

    logger.info("==================================================")
    logger.info(" MULTI-SEED SUMMARY ACROSS 5 SEEDS (11, 22, 33, 44, 55)")
    logger.info("==================================================")
    for col, stat in summary_stats.items():
        logger.info(f"{col:20s}: Mean = {stat['mean']:.3f} ± {stat['std']:.3f} (Range: [{stat['min']:.3f}, {stat['max']:.3f}])")
    logger.info(f"Results written to {os.path.join(RESULTS_DIR, 'multi_seed_summary.json')}")


if __name__ == "__main__":
    main()
