# Count the observed data sequence

Use a cold 256-byte cache at function entry and stop before caller storage. Count descriptor and stack accesses. Aligned doubleword loads/stores emit two word notices in this core. The baseline notice formula is 2*M*N*K+2*M*N+40 for these references; blocking adds 4*M*N*ceil(K/T).

The full directly edited composition is in `native/cache.tex`.
