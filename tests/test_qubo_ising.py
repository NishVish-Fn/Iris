#!/usr/bin/env python3
"""
Test QUBO <-> Ising Mathematical Equivalence and Energy Evaluation
Ensures that for any binary assignment x in {0, 1}^n and spin assignment s = 2x - 1:
E_QUBO(x) == H_Ising(s) within numerical precision (< 1e-10).
"""

import numpy as np
import pytest


def qubo_to_ising(qubo_dict, n_vars):
    """
    Converts upper-triangular QUBO to Ising formulation:
    x_i in {0, 1} -> s_i in {-1, +1} via x_i = (s_i + 1) / 2
    """
    h = np.zeros(n_vars)
    J = {}
    offset = 0.0

    for (i, j), q in qubo_dict.items():
        if i == j:
            h[i] += q / 2.0
            offset += q / 2.0
        else:
            if i > j:
                i, j = j, i
            J[(i, j)] = q / 4.0
            h[i] += q / 4.0
            h[j] += q / 4.0
            offset += q / 4.0

    return h, J, offset


def ising_to_qubo(h, J, offset):
    """
    Converts Ising formulation to QUBO:
    s_i in {-1, +1} -> x_i in {0, 1} via s_i = 2*x_i - 1
    """
    n_vars = len(h)
    qubo_dict = {}

    for i in range(n_vars):
        # Contribution from linear field h_i
        # h_i * (2 x_i - 1) = 2 h_i x_i - h_i
        qubo_dict[(i, i)] = qubo_dict.get((i, i), 0.0) + 2.0 * h[i]

    for (i, j), j_val in J.items():
        # j_val * (2 x_i - 1)(2 x_j - 1) = j_val * (4 x_i x_j - 2 x_i - 2 x_j + 1)
        if i > j:
            i, j = j, i
        qubo_dict[(i, j)] = qubo_dict.get((i, j), 0.0) + 4.0 * j_val
        qubo_dict[(i, i)] = qubo_dict.get((i, i), 0.0) - 2.0 * j_val
        qubo_dict[(j, j)] = qubo_dict.get((j, j), 0.0) - 2.0 * j_val

    return qubo_dict


def eval_qubo(x, qubo_dict):
    e = 0.0
    for (i, j), q in qubo_dict.items():
        e += q * x[i] * x[j]
    return e


def eval_ising(s, h, J, offset):
    e = offset + np.dot(h, s)
    for (i, j), j_val in J.items():
        e += j_val * s[i] * s[j]
    return e


def test_qubo_ising_exact_equivalence():
    np.random.seed(42)
    n = 10
    # Generate random QUBO
    qubo_dict = {}
    for i in range(n):
        qubo_dict[(i, i)] = float(np.random.uniform(-10, 10))
        for j in range(i + 1, n):
            qubo_dict[(i, j)] = float(np.random.uniform(-5, 5))

    h, J, offset = qubo_to_ising(qubo_dict, n)

    # Test on 100 random binary states
    for _ in range(100):
        x = np.random.randint(0, 2, size=n)
        s = 2 * x - 1

        e_q = eval_qubo(x, qubo_dict)
        e_i = eval_ising(s, h, J, offset)

        assert np.isclose(e_q, e_i, atol=1e-10), f"Mismatch: QUBO={e_q}, Ising={e_i}"


def test_bidirectional_roundtrip():
    np.random.seed(123)
    n = 8
    qubo_dict = {}
    for i in range(n):
        qubo_dict[(i, i)] = float(np.random.uniform(-5, 5))
        for j in range(i + 1, n):
            qubo_dict[(i, j)] = float(np.random.uniform(-3, 3))

    h, J, offset = qubo_to_ising(qubo_dict, n)
    qubo_roundtrip = ising_to_qubo(h, J, offset)

    # Verify qubo matrices match
    for k in qubo_dict:
        assert np.isclose(qubo_dict[k], qubo_roundtrip[k], atol=1e-10)


if __name__ == "__main__":
    test_qubo_ising_exact_equivalence()
    test_bidirectional_roundtrip()
    print("ALL QUBO <-> ISING TESTS PASSED PERFECTLY!")
