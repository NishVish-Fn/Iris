# QpiAI SDK Capabilities & Introspection Report
Generated automatically by `scripts/sdk_probe.py`.

## 1. `qpiai_quantum` Introspection
- **Package Version:** `0.3.0`
- **Module Path:** `C:\Users\creat\AppData\Local\Programs\Python\Python310\lib\site-packages\qpiai_quantum\__init__.py`

### Top-Level Exports
```python
Backend, BaseError, BellStateGenerator, BernsteinVazirani, Circuit, ClassicalRegister, ClusterStateGenerator, DensityMatrix, DeutschJozsa, GHZStateGenerator, GroverSearch, JobManager, JobResult, PackageNotFoundError, QFT, QRNG, QpiAIQuantumAuth, QuantumRegister, ShorsAlgorithm, SimonAlgorithm, Statevector, VQESolver, WStateGenerator, algorithms, applications, authentication, circuit, config, exceptions, formalism, icr, jobmanager, phase_estimation, quantum_info, results, simulator, state_preparation, version
```

### `qpiai_quantum.Circuit` Methods & Supported Gates
Methods & Gates confirmed by inspection:
```python
, BARRIER, CCX, CH, CP, CRX, CRY, CRZ, CS, CSDG, CSWAP, CU, CUSTOMCONTROL, CX, CY, CZ, DCX, ECR, H, ID, ISWAP, ISWAPDG, MCX, MEASURE, P, RCCX, RX, RXX, RY, RYY, RZ, RZZ, S, SDG, SWAP, SX, SXDG, T, TDG, U, U2, X, Y, Z, add_operation, barrier, cancel_job, ccx, ch, check_job_status, compose, cp, crx, cry, crz, cs, csdg, cswap, cu, cx, cy, cz, dcx, delete_job, depth, ecr, get_current_job, get_job_history, get_job_results, get_job_status, h, icr, id, inverse, iswap, iswapdg, list_gates, list_jobs, mcx, measure, measure_all, name, num_clbits, num_qubits, p, rccx, remove_operation, run, rx, rxx, ry, ryy, rz, rzz, s, sdg, show, size, standard_gate_set, swap, sx, sxdg, t, tdg, to_circuit_operation, to_json, to_qasm, u, u2, x, y, z
```

- **Verified Quantum Gates:** `h`, `rx`, `ry`, `rz`, `cx`, `cz`, `x`, `y`, `z`, `s`, `t`, `swap`, `measure`

### `qpiai_quantum.Statevector`
- Statevector Attributes / Methods: `copy, evolve, from_circuit_object, from_label, is_valid, probabilities, probabilities_dict, purity, to_density_matrix, to_dict`
- Statevector Output Type: `<class 'numpy.ndarray'>`, shape: `(4,)`, norm: `1.0000`

### `qpiai_quantum.QpiAIQuantumAuth` (Cloud API)
- Authentication Methods: `list_compute_resources, login, logout, me, verify_api_key`
- **Live QCloud Auth:** Authenticated successfully.
- **Available Compute Backends (6):**
  - `QpiAI-QSV-Simulator`: `QpiAI-QSV-Simulator` (N/A qubits, status: `online`)
  - `QpiAI-QDM-Simulator`: `QpiAI-QDM-Simulator` (N/A qubits, status: `online`)
  - `QpiAI-QTN-Simulator`: `QpiAI-QTN-Simulator` (N/A qubits, status: `online`)
  - `QpiAI-QSV-Lite`: `QpiAI-QSV-Lite` (N/A qubits, status: `online`)
  - `QpiAI-QDM-Lite`: `QpiAI-QDM-Lite` (N/A qubits, status: `online`)
  - `QpiAI-Indus-1`: `QpiAI-Indus-1` (N/A qubits, status: `online`)

## 2. `qpiai_opt` Introspection
- **Package Version:** `unknown`
- **Module Path:** `C:\Users\creat\AppData\Local\Programs\Python\Python310\lib\site-packages\qpiai_opt\__init__.py`

### `qpiai_opt.opt_solvers` Members
```python
APIError, AuthenticationError, CapabilityError, Client, ComputeResource, CreditQuota, CreditResourceUsage, CreditUsage, CreditUsageByResource, CreditUsageJob, Experiment, Invitation, IsingSolveResult, Job, JobCancelledError, JobFailedError, JobStatus, LOCAL_LIMITS, LocalLimitExceeded, LocalProvider, Organization, PlatformError, Problem, Project, QPSolveResult, SSEEvent, SolveInfo, SolveResult, UserSummary, Workspace, automation, cancel, classify, client, convert, exceptions, feasibility, hubo, inspect, ising, local, milp, platform_models, provider_capabilities, providers, qubo, result, result_base, results, routing, schemas, solve, status, submit
```

- **Detected Provider Classes:** `LocalProvider, provider_capabilities, providers`
- **Client Methods:** `DEFAULT_EXPERIMENT_NAME, DEFAULT_URL, add_experiment_member, add_project_member, add_workspace_member, add_workspace_members, associate_compute_resource, can_invite_members, cancel_invitation, cancel_job, create_experiment, create_problem, create_project, create_workspace, default_problem_name, delete_experiment, delete_organization, delete_problem, delete_project, delete_storage, delete_workspace, download_result, download_result_by_id, extract_result, get_api_key, get_experiment, get_job, get_organization, get_organization_compute_resources, get_organization_credit_usage, get_organization_quotas, get_organization_statistics, get_problem, get_project, get_solver, get_user_credit_usage, get_user_quotas, get_workspace, get_workspace_credit_usage, get_workspace_quotas, headers, health, invite_member, invite_members, list_experiment_jobs, list_experiment_members, list_experiments, list_invitations, list_jobs, list_my_experiments, list_my_projects, list_my_workspaces, list_problem_jobs, list_problems, list_project_members, list_projects, list_solvers, list_workspace_experiments, list_workspace_members, list_workspaces, me, regenerate_api_key, remove_experiment_member, remove_project_member, remove_workspace_member, resolve_experiment, schedule_job, stream_job, submit_job, update_experiment, update_member_role, update_organization, update_problem, update_project, update_workspace, upload_file, validate_base_url, validate_boolean, validate_identifier, validate_pagination, validate_scheduled_at, validate_timeout, verify_api_key, wait_for_job`
- **LocalProvider Methods:** `capabilities, solve`
### Supported Optimization Problem Classes
- **QUBOProblem:** `qpiai_opt.opt_solvers.qubo.models.QUBOProblem`
- **HUBOProblem:** `qpiai_opt.opt_solvers.hubo.models.HUBOProblem`
- **Remote/Cloud Provider Exported in opt_solvers:** `No` (LocalProvider is primary client-backed provider)

- **Live QpiAI Opt Execution:** Successfully authenticated & solved test QUBO!
  - Solution: `[0, 1]`, Reported Energy: `-2.0`
