# Sources and verification

The exact conversion contract is defined locally. Native operations are checked against pinned MARS MSA implementation files and permutation tests; references.json also pins the independent QEMU instruction reference. The conversion oracle uses division and arithmetic, not the vector algorithm. Public checks exercise the function interface and full caller. Private checks cover broader bounded domains, reuse, intermediate construction observations and a scalar alternative. Actual IDE captures, author review and independent accessibility validation remain pending.
