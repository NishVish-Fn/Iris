PYTHON = python

.PHONY: all data split cost features qkernel classical funnel evaluate export test clean run-backend

all: data split cost features qkernel classical funnel evaluate export test
	@echo "=========================================================="
	@echo "MolFunnel Pipeline Execution Successfully Completed!"
	@echo "All models trained, selections frozen, and assets bundled."
	@echo "=========================================================="

data:
	$(PYTHON) pipeline/data.py

split:
	$(PYTHON) pipeline/split.py

cost:
	$(PYTHON) pipeline/validate_coprinet.py
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
