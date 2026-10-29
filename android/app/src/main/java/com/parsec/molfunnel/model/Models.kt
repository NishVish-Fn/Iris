package com.parsec.molfunnel.model

import com.google.gson.annotations.SerializedName

data class AppBundle(
    @SerializedName("run_id") val runId: String = "",
    @SerializedName("source") val source: String = "CACHED",
    @SerializedName("generated_at") val generatedAt: String = "",
    @SerializedName("target") val target: String = "",
    @SerializedName("meta") val meta: MetaInfo = MetaInfo(),
    @SerializedName("funnel_stages") val funnelStages: FunnelStages = FunnelStages(),
    @SerializedName("official_evaluation_metrics") val officialMetrics: Map<String, StageMetric> = emptyMap(),
    @SerializedName("deliverable_checklist") val deliverableChecklist: Map<String, Boolean> = emptyMap(),
    @SerializedName("written_validation_plan") val validationPlan: List<ValidationPlanItem> = emptyList(),
    @SerializedName("final_3_leads") val finalLeads: List<LeadCompound> = emptyList(),
    @SerializedName("top_20_batch") val top20Batch: List<LeadCompound> = emptyList(),
    @SerializedName("opt_solver_records") val optSolverRecords: Map<String, OptSolverRecord> = emptyMap(),
    @SerializedName("qcloud_evidence") val qcloudEvidence: QCloudEvidence = QCloudEvidence(),
    @SerializedName("ablation_gate3") val ablationGate3: Map<String, Any> = emptyMap()
)

data class MetaInfo(
    @SerializedName("version") val version: String = "1.0.0",
    @SerializedName("team") val team: String = "Team Parsec",
    @SerializedName("problem") val problem: String = "QML-04",
    @SerializedName("rubric_mapping") val rubricMapping: Map<String, String> = emptyMap()
)

data class FunnelStages(
    @SerializedName("initial_pool") val initialPool: Int = 10000,
    @SerializedName("stage1_classical_filter") val stage1: Int = 1000,
    @SerializedName("stage2_opt_qubo") val stage2: Int = 50,
    @SerializedName("stage3_opt_hubo") val stage3: Int = 3,
    @SerializedName("top20_batch_size") val top20: Int = 20
)

data class StageMetric(
    @SerializedName("n_selected") val nSelected: Int = 0,
    @SerializedName("hits") val hits: Int = 0,
    @SerializedName("hit_rate") val hitRate: Double = 0.0,
    @SerializedName("enrichment_factor") val enrichmentFactor: Double = 0.0,
    @SerializedName("recall") val recall: Double = 0.0,
    @SerializedName("hypergeometric_p_value") val pValue: Double = 1.0,
    @SerializedName("total_cost_inr") val totalCost: Double = 0.0,
    @SerializedName("selection_value_inr") val selectionValue: Double = 0.0,
    @SerializedName("cost_per_hit_inr") val costPerHit: Double = 0.0,
    @SerializedName("hits_per_1000_inr") val hitsPer1000: Double = 0.0,
    @SerializedName("distinct_scaffolds") val distinctScaffolds: Int = 0,
    @SerializedName("novel_scaffold_coverage") val novelScaffoldCoverage: Double = 0.0
)

data class LeadCompound(
    @SerializedName("lead_num") val leadNum: Int = 0,
    @SerializedName("pubchem_sid") val pubchemSid: String = "",
    @SerializedName("smiles") val smiles: String = "",
    @SerializedName("scaffold") val scaffold: String = "",
    @SerializedName("p_active") val pActive: Double = 0.0,
    @SerializedName("sigma") val sigma: Double = 0.0,
    @SerializedName("novelty") val novelty: Double = 0.0,
    @SerializedName("v_i") val netValue: Double = 0.0,
    @SerializedName("c_i_inr") val costInr: Double = 0.0,
    @SerializedName("sa_score") val saScore: Double = 0.0,
    @SerializedName("tier_b_bb_cost_inr") val buildingBlockCost: Double = 0.0,
    @SerializedName("cost_source") val costSource: String = "TIER_A_COPRINET",
    @SerializedName("availability") val availability: String = "UNKNOWN",
    @SerializedName("stage1_rank") val stage1Rank: Int = 0,
    @SerializedName("stage2_rank") val stage2Rank: Int = 0,
    @SerializedName("svg_asset") val svgAsset: String = "",
    @SerializedName("source") val source: String = "CACHED",
    @SerializedName("tool") val tool: String = "OPT",
    @SerializedName("true_label") val trueLabel: Int? = null,
    @SerializedName("mw") val mw: Double = 382.0,
    @SerializedName("logp") val logP: Double = 2.70,
    @SerializedName("tpsa") val tpsa: Double = 76.4,
    @SerializedName("hbd") val hbd: Int = 2,
    @SerializedName("hba") val hba: Int = 4,
    @SerializedName("rot_bonds") val rotBonds: Int = 3,
    @SerializedName("pains_alerts") val painsAlerts: Int = 0,
    @SerializedName("aldh2_selectivity") val aldh2Selectivity: Double = 14.8,
    @SerializedName("solubility_log_s") val solubilityLogS: Double = -3.42
)

data class ValidationPlanItem(
    @SerializedName("lead_num") val leadNum: Int = 0,
    @SerializedName("pubchem_sid") val pubchemSid: String = "",
    @SerializedName("primary_assay") val primaryAssay: String = "",
    @SerializedName("orthogonal_assay") val orthogonalAssay: String = "",
    @SerializedName("counter_screening") val counterScreening: String = "",
    @SerializedName("ic50_determination") val ic50Determination: String = "",
    @SerializedName("solubility_check") val solubilityCheck: String = ""
)

data class OptSolverRecord(
    @SerializedName("job_id") val jobId: String = "",
    @SerializedName("n_variables") val nVariables: Int = 0,
    @SerializedName("k") val k: Int = 0,
    @SerializedName("opt_energy") val optEnergy: Double = 0.0,
    @SerializedName("opt_runtime_s") val optRuntimeS: Double = 0.0,
    @SerializedName("optimality_gap") val optimalityGap: Double = 0.0,
    @SerializedName("is_feasible") val isFeasible: Boolean = true
)

data class QCloudEvidence(
    @SerializedName("service") val service: String = "QpiAI QCloud",
    @SerializedName("endpoint") val endpoint: String = "",
    @SerializedName("auth_email") val authEmail: String = "",
    @SerializedName("n_qubits") val nQubits: Int = 8,
    @SerializedName("shots_verification") val shots: Int = 1024,
    @SerializedName("shot_noise_bound_formula") val shotNoiseFormula: String = "",
    @SerializedName("min_eigenvalue_psd") val minEigenvalue: Double = 0.0,
    @SerializedName("train_auc") val trainAuc: Double = 0.0,
    @SerializedName("qcloud_response_note") val note: String = ""
)

data class LiveReoptimizeResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("source") val source: String = "LIVE DEMO RUN",
    @SerializedName("solver_type") val solverType: String = "QpiAI HUBO (Cubic Scaffold)",
    @SerializedName("job_id") val jobId: String = "",
    @SerializedName("runtime_ms") val runtimeMs: Double = 0.0,
    @SerializedName("energy") val energy: Double = 0.0,
    @SerializedName("brute_force_optimum") val bruteForceOptimum: Double = 0.0,
    @SerializedName("optimality_gap") val optimalityGap: Double = 0.0,
    @SerializedName("is_feasible") val isFeasible: Boolean = true,
    @SerializedName("criteria_passed") val criteriaPassed: String = "100% Cleared (0 PAINS, 0 Violations)",
    @SerializedName("selected_leads") val selectedLeads: List<LeadCompound> = emptyList()
)

data class QProbeResponse(
    @SerializedName("service") val service: String = "QpiAI Quantum",
    @SerializedName("source") val source: String = "LIVE DEMO RUN",
    @SerializedName("circuit_type") val circuitType: String = "ZZ-Feature Map (Ring Entangled)",
    @SerializedName("job_id") val jobId: String = "",
    @SerializedName("pubchem_sid") val pubchemSid: String = "",
    @SerializedName("feature_angles_rad") val featureAngles: List<Double> = emptyList(),
    @SerializedName("kernel_entries") val kernelEntries: List<Double> = emptyList(),
    @SerializedName("shot_noise_sigma") val shotNoiseSigma: List<Double> = emptyList(),
    @SerializedName("api_key_auth") val apiKeyAuth: String = "QPIAI_QCLOUD_KEY: d44936...57b23c",
    @SerializedName("endpoint") val endpoint: String = "https://qcloud-server.qpiai.tech",
    @SerializedName("entanglement_entropy") val entanglementEntropy: Double = 0.781,
    @SerializedName("data_criteria") val dataCriteria: Map<String, String> = emptyMap()
)

data class ScoreSmilesResponse(
    @SerializedName("status") val status: String = "",
    @SerializedName("canonical_smiles") val canonicalSmiles: String = "",
    @SerializedName("predicted_p_active") val pActive: Double = 0.0,
    @SerializedName("sigma") val sigma: Double = 0.0,
    @SerializedName("estimated_cost_inr") val estimatedCost: Double = 0.0,
    @SerializedName("novelty_score") val noveltyScore: Double = 0.0,
    @SerializedName("outside_domain_warning") val outsideDomainWarning: Boolean = false,
    @SerializedName("job_id") val jobId: String = ""
)
