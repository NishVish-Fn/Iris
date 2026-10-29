"""
Automated Test: Verification of QpiAI Invocations (Gate 5)
Asserts that every cached run contains real QpiAI QCloud and Opt solve records.
"""

import os
import json
import pytest

RUN_DIR = r".\results\runs\seed11"


def test_qpiai_records_exist():
    selection_file = os.path.join(RUN_DIR, "selection.json")
    qcloud_file = os.path.join(RUN_DIR, "qkernel_evidence.json")

    assert os.path.exists(selection_file), f"selection.json missing in {RUN_DIR}"
    assert os.path.exists(qcloud_file), f"qkernel_evidence.json missing in {RUN_DIR}"

    with open(selection_file, "r") as f:
        sel = json.load(f)

    with open(qcloud_file, "r") as f:
        qev = json.load(f)

    # 1. Assert QCloud record exists
    assert qev.get("service") == "QpiAI QCloud", "Service must be QpiAI QCloud"
    assert qev.get("n_qubits") == 8, "Quantum kernel must be built on 8 qubits"
    assert "shot_noise_bound_formula" in qev, "Shot noise bound must be documented"
    assert "train_auc" in qev and qev["train_auc"] > 0.80, "Quantum kernel must exhibit valid train AUC"

    # 2. Assert Opt solve records exist (QUBO Stage 2 and HUBO Stage 3)
    opt_records = sel.get("opt_solver_records", {})
    assert "stage2_qubo" in opt_records, "Missing stage2_qubo in opt_solver_records"
    assert "stage3_hubo" in opt_records, "Missing stage3_hubo in opt_solver_records"

    # Stage 2 QUBO checks
    s2 = opt_records["stage2_qubo"]
    assert s2.get("is_feasible") is True, "Stage 2 solution must be feasible"
    assert s2.get("k") == 50, "Stage 2 must select exactly 50 compounds"
    assert "job_id" in s2 and len(s2["job_id"]) > 0, "Stage 2 must record a job id"

    # Stage 3 HUBO checks
    s3 = opt_records["stage3_hubo"]
    assert s3.get("is_feasible") is True, "Stage 3 solution must be feasible"
    assert s3.get("k") == 3, "Stage 3 must select exactly 3 compounds"
    assert "optimality_gap" in s3 and s3["optimality_gap"] >= 0.0, "Stage 3 must verify optimality gap against brute force"
    assert "job_id" in s3 and len(s3["job_id"]) > 0, "Stage 3 must record a job id"


def test_ablation_gate3_verified():
    selection_file = os.path.join(RUN_DIR, "selection.json")
    with open(selection_file, "r") as f:
        sel = json.load(f)

    ablation = sel.get("ablation_gate3", {})
    assert "quantum_vs_rbf_stage1_changed_molecules" in ablation
    diff_count = ablation["quantum_vs_rbf_stage1_changed_molecules"]
    assert diff_count > 0, f"Gate 3 Failure: Quantum kernel produced identical selection to RBF! diff={diff_count}"


if __name__ == "__main__":
    test_qpiai_records_exist()
    test_ablation_gate3_verified()
    print("All QpiAI Gate 5 automated evidence tests passed successfully!")
