"""
Plotting and Visualization Module (Section 9).
Generates publication-ready comparative charts:
1. Leaderboard bar chart with 95% bootstrap CIs
2. Quantum Kernel Gram Matrix Heatmap
3. Qubits vs Accuracy scaling curve
4. Optimization Solver scaling curves (QpiAI Opt vs Classical)
5. Runtime-vs-Accuracy Pareto Frontier
"""

import os
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from pathlib import Path
from typing import Dict, Any, List

from .utils import BASE_DIR


def plot_leaderboard_bars(df_results: pd.DataFrame, out_path: Path):
    """Plots top models with 95% confidence intervals."""
    plt.figure(figsize=(10, 6))
    
    # Filter Track A for fair comparison
    df_track = df_results[df_results["track"] == "Track_A"].copy()
    if df_track.empty:
        df_track = df_results.copy()
        
    grouped = df_track.groupby(["model_name", "model_family"])["roc_auc"].agg(["mean", "std"]).reset_index()
    grouped = grouped.sort_values(by="mean", ascending=True)
    
    colors = []
    for fam in grouped["model_family"]:
        if "QML" in fam or "Quantum" in fam:
            colors.append("#2563EB") # Quantum blue
        elif "HYBRID" in fam or "QUBO" in fam:
            colors.append("#7C3AED") # Purple hybrid
        else:
            colors.append("#475569") # Classical slate
            
    y_pos = np.arange(len(grouped))
    plt.barh(y_pos, grouped["mean"], xerr=grouped["std"].fillna(0.0), color=colors, alpha=0.88, capsize=4, edgecolor="#0F172A")
    plt.yticks(y_pos, grouped["model_name"], fontsize=9)
    plt.xlabel("Mean ROC-AUC (with ±1 SD error bars)", fontsize=11, fontweight="bold")
    plt.title("Track A (Matched Features): QML vs Classical Market Models", fontsize=13, fontweight="bold", pad=12)
    plt.xlim(0.40, 1.0)
    plt.grid(axis='x', linestyle='--', alpha=0.5)
    
    # Legend
    from matplotlib.patches import Patch
    legend_elements = [
        Patch(facecolor="#2563EB", label="Pure QML (qpiai_quantum)"),
        Patch(facecolor="#7C3AED", label="Hybrid QpiAI Opt / QUBO"),
        Patch(facecolor="#475569", label="Classical ML (Market Baselines)")
    ]
    plt.legend(handles=legend_elements, loc="lower right", framealpha=0.95)
    plt.tight_layout()
    plt.savefig(out_path, dpi=300)
    plt.close()


def plot_kernel_heatmap(K: np.ndarray, out_path: Path, title: str = "QpiAI Quantum Kernel Gram Matrix"):
    """Plots quantum fidelity kernel matrix heatmap."""
    plt.figure(figsize=(7, 6))
    plt.imshow(K[:40, :40], cmap="viridis", interpolation="nearest", vmin=0.0, vmax=1.0)
    cbar = plt.colorbar()
    cbar.set_label("Fidelity |⟨ψ(x_i)|ψ(x_j)⟩|²", fontsize=10)
    plt.title(title, fontsize=12, fontweight="bold", pad=10)
    plt.xlabel("Sample Index i (First 40 Molecules)", fontsize=10)
    plt.ylabel("Sample Index j (First 40 Molecules)", fontsize=10)
    plt.tight_layout()
    plt.savefig(out_path, dpi=300)
    plt.close()


def plot_qubit_scaling(qubit_results: Dict[int, float], out_path: Path):
    """Plots classification accuracy as a function of qubit count."""
    plt.figure(figsize=(7, 4.5))
    qubits = sorted(qubit_results.keys())
    scores = [qubit_results[q] for q in qubits]
    
    plt.plot(qubits, scores, marker='o', linewidth=2.5, markersize=8, color="#2563EB", label="Quantum Kernel SVM")
    plt.title("Quantum Expressibility: Effect of Qubit Dimension", fontsize=12, fontweight="bold")
    plt.xlabel("Number of Qubits (n)", fontsize=11)
    plt.ylabel("Test ROC-AUC Score", fontsize=11)
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.ylim(0.5, 1.0)
    plt.xticks(qubits)
    plt.legend()
    plt.tight_layout()
    plt.savefig(out_path, dpi=300)
    plt.close()


def plot_solver_scaling(opt_results: List[Dict[str, Any]], out_path: Path):
    """Plots optimization solver energy and time scaling across problem sizes."""
    df_opt = pd.DataFrame(opt_results)
    if df_opt.empty:
        return
        
    df_opt_clean = df_opt.replace([np.inf, -np.inf], np.nan).dropna(subset=["optimality_gap"])
    if df_opt_clean.empty:
        df_opt_clean = df_opt
        
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))
    
    # 1. Optimality Gap vs Size
    for solver, grp in df_opt_clean.groupby("solver"):
        means = grp.groupby("n_variables")["optimality_gap"].mean()
        ax1.plot(means.index, means.values, marker='s', label=solver, linewidth=2)
        
    ax1.set_title("Optimality Gap vs Problem Size (n)", fontsize=11, fontweight="bold")
    ax1.set_xlabel("Number of Binary Variables (n)", fontsize=10)
    ax1.set_ylabel("Optimality Gap: (E - E_best) / |E_best|", fontsize=10)
    ax1.grid(True, linestyle="--", alpha=0.5)
    ax1.legend(fontsize=8)
    
    # 2. Runtime Scaling
    for solver, grp in df_opt.groupby("solver"):
        t_means = grp.groupby("n_variables")["time_sec"].mean()
        ax2.plot(t_means.index, t_means.values, marker='o', label=solver, linewidth=2)
        
    ax2.set_title("Solver Runtime Scaling", fontsize=11, fontweight="bold")
    ax2.set_xlabel("Number of Binary Variables (n)", fontsize=10)
    ax2.set_ylabel("Solve Time (seconds)", fontsize=10)
    ax2.set_yscale("log")
    ax2.grid(True, linestyle="--", alpha=0.5)
    ax2.legend(fontsize=8)
    
    plt.tight_layout()
    plt.savefig(out_path, dpi=300)
    plt.close()


def plot_pareto_frontier(df_results: pd.DataFrame, out_path: Path):
    """Plots Pareto tradeoff between Training Latency and ROC-AUC."""
    plt.figure(figsize=(9, 6))
    
    for fam, grp in df_results.groupby("model_family"):
        color = "#2563EB" if "QML" in fam else ("#7C3AED" if "HYBRID" in fam else "#475569")
        marker = '^' if "QML" in fam else ('D' if "HYBRID" in fam else 'o')
        plt.scatter(
            grp["train_time_sec"], grp["roc_auc"],
            label=fam, color=color, marker=marker, s=80, alpha=0.85, edgecolors='black', linewidth=0.5
        )
        
    plt.xscale("log")
    plt.xlabel("Training Time in Seconds (Log Scale)", fontsize=11, fontweight="bold")
    plt.ylabel("ROC-AUC Performance", fontsize=11, fontweight="bold")
    plt.title("Accuracy vs Computational Cost Pareto Frontier", fontsize=13, fontweight="bold", pad=12)
    plt.grid(True, linestyle="--", alpha=0.5)
    plt.legend(framealpha=0.95)
    plt.tight_layout()
    plt.savefig(out_path, dpi=300)
    plt.close()
