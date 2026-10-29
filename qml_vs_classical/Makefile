PYTHON = python

.PHONY: help probe data qml opt classical analyze report all test clean check-secrets

help:
	@echo "qml_vs_classical Benchmark Suite Makefile Targets:"
	@echo "  make probe         - Run scripts/sdk_probe.py and update docs/sdk_capabilities.md"
	@echo "  make check-secrets - Run scripts/check_secrets.py"
	@echo "  make smoke         - Run fast end-to-end smoke test (~1 min)"
	@echo "  make all           - Run full comprehensive benchmark across all datasets and models"
	@echo "  make test          - Run pytest test suite"

probe:
	$(PYTHON) scripts/sdk_probe.py

check-secrets:
	$(PYTHON) scripts/check_secrets.py

smoke:
	$(PYTHON) scripts/run_all.py --smoke

all:
	$(PYTHON) scripts/run_all.py --full

test:
	$(PYTHON) -m pytest tests/

clean:
	rm -rf results/cache/*.pkl
