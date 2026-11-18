# Matrix transpose starter

The caller, data capacity, guard labels, and return stubs are supplied. Append guided
fragments in the named regions, or implement the same function contract in free-writing
mode. The unmodified starter assembles but intentionally fails the completed-task checks.

Use ROWS=3, COLS=5, METHOD=0 through step 12; set METHOD=1 for the tiled walkthrough.
For the cache comparison use ROWS=16 and COLS=16 and run each method from a fresh cold
state. Stop at transpose_done before the caller stores the count. Input values are the
original indices 0..255. Assembly constants are changed before a new assembly/run.

Do not treat the supplied nop delay slots as unfinished work. Preserve the declared
argument, output, and saved-register contract. The grader can supply interior pointers
and relocate the allocations. See assignment.md and the public Kotlin checks.
