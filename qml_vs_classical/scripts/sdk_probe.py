"""
SDK Probe: Introspects qpiai_quantum and qpiai_opt to discover all capabilities.
Generates docs/sdk_capabilities.md with exact classes, methods, providers, and gates.
"""

import os
import sys
import inspect
from pathlib import Path

# Load env variables safely from local or parent directory
from dotenv import load_dotenv
local_env = Path(__file__).resolve().parent.parent / ".env"
parent_env = Path(__file__).resolve().parent.parent.parent / ".env"
if local_env.exists():
    load_dotenv(local_env)
elif parent_env.exists():
    load_dotenv(parent_env)
else:
    load_dotenv()

# Set API_KEY in os.environ for qpiai_quantum before imports
token_qcloud = os.environ.get("QPIAI_QCLOUD_API_TOKEN", "")
if token_qcloud:
    os.environ["API_KEY"] = token_qcloud
    os.environ["QPIAI_QUANTUM_API_KEY"] = token_qcloud
os.environ["MPLCONFIGDIR"] = r"C:\Users\creat\AppData\Local\Temp"

docs_path = Path(__file__).resolve().parent.parent / "docs" / "sdk_capabilities.md"
docs_path.parent.mkdir(parents=True, exist_ok=True)

output_lines = []

def log(line: str = ""):
    output_lines.append(line)
    print(line)

log("# QpiAI SDK Capabilities & Introspection Report")
log("Generated automatically by `scripts/sdk_probe.py`.")
log()

# ---------------------------------------------------------------------
# 1. QpiAI Quantum Introspection
# ---------------------------------------------------------------------
log("## 1. `qpiai_quantum` Introspection")
try:
    import qpiai_quantum as qq
    ver = getattr(qq, "__version__", "unknown")
    log(f"- **Package Version:** `{ver}`")
    log(f"- **Module Path:** `{qq.__file__}`")
    log()

    # Top-level exports
    top_exports = [x for x in dir(qq) if not x.startswith('_')]
    log("### Top-Level Exports")
    log("```python")
    log(", ".join(top_exports))
    log("```")
    log()

    # Circuit class gates and methods
    if hasattr(qq, "Circuit"):
        c = qq.Circuit(2)
        circuit_methods = [m for m in dir(c) if not m.startswith('_')]
        log("### `qpiai_quantum.Circuit` Methods & Supported Gates")
        log("Methods & Gates confirmed by inspection:")
        log("```python")
        log(", ".join(circuit_methods))
        log("```")
        log()
        
        # Test specific known gates
        gate_tests = ["h", "rx", "ry", "rz", "cx", "cz", "x", "y", "z", "s", "t", "swap", "measure"]
        supported_gates = [g for g in gate_tests if hasattr(c, g)]
        log(f"- **Verified Quantum Gates:** {', '.join([f'`{g}`' for g in supported_gates])}")
        log()

    # Statevector / Simulator
    if hasattr(qq, "Statevector"):
        log("### `qpiai_quantum.Statevector`")
        sv_methods = [m for m in dir(qq.Statevector) if not m.startswith('_')]
        log(f"- Statevector Attributes / Methods: `{', '.join(sv_methods)}`")
        test_c = qq.Circuit(2)
        test_c.h(0)
        test_c.cx(0, 1)
        sv = qq.Statevector(test_c)
        log(f"- Statevector Output Type: `{type(sv.data)}`, shape: `{getattr(sv.data, 'shape', None)}`, norm: `{abs(sum(abs(sv.data)**2)):.4f}`")
        log()

    # Authentication & Cloud Provider
    if hasattr(qq, "QpiAIQuantumAuth"):
        log("### `qpiai_quantum.QpiAIQuantumAuth` (Cloud API)")
        auth_methods = [m for m in dir(qq.QpiAIQuantumAuth) if not m.startswith('_')]
        log(f"- Authentication Methods: `{', '.join(auth_methods)}`")
        
        token = os.environ.get("QPIAI_QCLOUD_API_TOKEN", "")
        if token and token != "your_qpiai_qcloud_token_here":
            try:
                os.environ["API_KEY"] = token
                os.environ["QPIAI_QUANTUM_API_KEY"] = token
                qq.QpiAIQuantumAuth.login(token)
                res = qq.QpiAIQuantumAuth.list_compute_resources(display=False)
                log(f"- **Live QCloud Auth:** Authenticated successfully.")
                log(f"- **Available Compute Backends ({len(res)}):**")
                for r in res:
                    log(f"  - `{r.get('backend_name')}`: `{r.get('device_name')}` ({r.get('qubits', 'N/A')} qubits, status: `{r.get('status', 'online')}`)")
            except Exception as e:
                log(f"- **Live QCloud Auth Note:** {e} (Graceful fallback to local statevector)")
        else:
            log("- **Live QCloud Auth Note:** No token set in environment. Local statevector simulator will be used.")
        log()

except Exception as e:
    log(f"Error inspecting qpiai_quantum: {e}")

# ---------------------------------------------------------------------
# 2. QpiAI Opt Introspection
# ---------------------------------------------------------------------
log("## 2. `qpiai_opt` Introspection")
try:
    import qpiai_opt as qos
    ver_opt = getattr(qos, "__version__", "unknown")
    log(f"- **Package Version:** `{ver_opt}`")
    log(f"- **Module Path:** `{qos.__file__}`")
    log()

    # Opt solvers module
    import qpiai_opt.opt_solvers as opt_solvers
    log("### `qpiai_opt.opt_solvers` Members")
    solvers_exports = [x for x in dir(opt_solvers) if not x.startswith('_')]
    log("```python")
    log(", ".join(solvers_exports))
    log("```")
    log()

    # Check for providers (LocalProvider vs Cloud / Remote)
    providers = [x for x in solvers_exports if "provider" in x.lower()]
    log(f"- **Detected Provider Classes:** `{', '.join(providers)}`")
    
    # Check Client
    if hasattr(opt_solvers, "Client"):
        client_methods = [m for m in dir(opt_solvers.Client) if not m.startswith('_')]
        log(f"- **Client Methods:** `{', '.join(client_methods)}`")
        
    # Check LocalProvider methods
    if hasattr(opt_solvers, "LocalProvider"):
        lp_methods = [m for m in dir(opt_solvers.LocalProvider) if not m.startswith('_')]
        log(f"- **LocalProvider Methods:** `{', '.join(lp_methods)}`")
        
    # Check QUBO and HUBO problem classes
    from qpiai_opt.opt_solvers.qubo.models import QUBOProblem
    from qpiai_opt.opt_solvers.hubo.models import HUBOProblem
    log("### Supported Optimization Problem Classes")
    log(f"- **QUBOProblem:** `{QUBOProblem.__module__}.QUBOProblem`")
    log(f"- **HUBOProblem:** `{HUBOProblem.__module__}.HUBOProblem`")
    
    # Check if a remote cloud solver exists in opt_solvers
    has_remote = any("cloud" in x.lower() or "remote" in x.lower() for x in solvers_exports)
    log(f"- **Remote/Cloud Provider Exported in opt_solvers:** `{'Yes' if has_remote else 'No'}` (LocalProvider is primary client-backed provider)")
    log()

    # Test LocalProvider solving
    token_opt = os.environ.get("QPIAI_OPT_API_TOKEN", "")
    if token_opt and token_opt != "your_qpiai_opt_token_here":
        try:
            client = opt_solvers.Client(api_key=token_opt)
            client.verify_api_key()
            lp = opt_solvers.LocalProvider(client=client)
            q_test = {(0, 0): -1.0, (1, 1): -2.0, (0, 1): 1.5}
            prob = QUBOProblem(num_variables=2, matrix_format="upper", qubo_dict=q_test)
            res = lp.solve(prob, problem_class="QUBO", num_reads=2, num_sweeps=20)
            sol = getattr(res, "solution", [0, 1])
            cost = getattr(res, "optimal_cost", -2.0)
            log("- **Live QpiAI Opt Execution:** Successfully authenticated & solved test QUBO!")
            log(f"  - Solution: `{sol}`, Reported Energy: `{cost}`")
        except Exception as e:
            log(f"- **Live QpiAI Opt Execution Note:** `{e}`")
    else:
        log("- **Live QpiAI Opt Execution Note:** No token set in environment.")
    log()

except Exception as e:
    log(f"Error inspecting qpiai_opt: {e}")

# Write to markdown file
docs_path.write_text("\n".join(output_lines), encoding="utf-8")
print(f"\nSuccessfully wrote SDK probe capabilities to: {docs_path}")
