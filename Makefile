PYTHON = python

.PHONY: all verify data split cost features qkernel classical funnel evaluate export test clean run-backend

all: verify data split cost features qkernel classical funnel evaluate export test
	@echo ==========================================================
	@echo IRIS PIPELINE COMPLETE (ALL MODELS EVALUATED & VERIFIED)
	@echo ==========================================================

verify:
	@echo ==========================================================
	@echo VERIFYING DATA INTEGRITY, CLAIMS, SECRETS, AND EVIDENCE
	@echo ==========================================================
	$(PYTHON) data/download.py
	$(PYTHON) scripts/check_secrets.py
	$(PYTHON) -m pytest -o cache_dir=.pytest_cache tests/test_claims.py
	$(PYTHON) -m pytest -o cache_dir=.pytest_cache tests/test_qubo_ising.py
	$(PYTHON) scripts/verify_evidence.py
	@echo ==========================================================
	@echo ALL CLAIMS, SECRETS, AND EVIDENCE 100% VERIFIED
	@echo ==========================================================

data:
	$(PYTHON) pipeline/data.py

split:
	$(PYTHON) pipeline/split.py

cost:
	$(PYTHON) pipeline/cost.py

features:
	$(PYTHON) pipeline/features.py

qkernel:
	$(PYTHON) pipeline/qkernel.py

classical:
	$(PYTHON) pipeline/models_classical.py

funnel:
	$(PYTHON) pipeline/funnel.py

evaluate:
	$(PYTHON) pipeline/evaluate.py

export:
	$(PYTHON) pipeline/export_app_assets.py

test:
	$(PYTHON) -m pytest -o cache_dir=.pytest_cache tests/

run-backend:
	uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
