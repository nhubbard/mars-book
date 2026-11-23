# Independent streaming scanner

Replace only the failing scan_utf8 body in stream.asm. The caller is complete. a0 selects a word-aligned 32-byte state, a1 points into the input arena, a2 is the supplied count (0–64) and a3 is the final flag. State layout, reachable-state assumptions, total bound and all error priorities are specified in the written companion.

The native assessments contain 426 stream, twelve interleaving and fifteen caller cases. Each call owns only its declared input interval. Read-ahead inside that interval is permitted; reading even one byte outside it is rejected. Zero-length and terminal calls permit no input load. Only the selected active state may be written. A terminal rejection permits no caller-data stores, including a write later restored. A private correctly restored stack frame is permitted.

The original public stream.kts and the shared-engine generated caller.kts also run with JDK25 from this directory:

    java -jar /path/to/cli.jar grade stream.kts stream.asm
    java -jar /path/to/cli.jar grade caller.kts stream.asm

The twelve additional interleaving cases run through the plugin's native assessment. They alternate both states in one session, change scratch registers and revisit terminal states. Matching the supplied example alone is insufficient. No particular private label, scratch-register use or instruction sequence is required. Fixed instruction budgets stop runaway executions; exact instruction counts are not graded.
