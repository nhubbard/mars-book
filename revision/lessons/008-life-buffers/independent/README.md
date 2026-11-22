# Compute a new board and count changed cells

Replace life_changes's break body. a0 points to a 100-byte source board; a1 points
to a separate, nonoverlapping 100-byte destination. Each board is a ten-by-ten
array with a zero halo around its eight-by-eight interior. Logical (x,y) is at
base+10*(y+1)+(x+1), for x,y from zero through seven. The source interior is
binary. The destination interior may initially contain any bytes. Positions
outside the logical board are dead and do not wrap.

Compute the standard Life successor: three neighbors produce one; two retain the
old center; all other counts produce zero. Write every resulting interior byte.
Return in v0 the number of positions whose old and next values differ. The empty
board changes zero cells, a stable block changes zero and the default oscillator
changes four, even though it has three live cells in either orientation.

Preserve the entire source, both zero halos and the six supplied guards. These
regions must receive no execution stores, including same-value stores or writes
later undone. The complete source range and destination halo are watched from
after setup through both direct calls. Each case has 25 protected ranges covering
160 bytes. Return with s0–s7, fp, gp and sp preserved at full width and the 32
caller-owned stack bytes unchanged. Stack comparisons observe values at return,
not all access history. Produce no console output and return through the incoming
continuation. Private labels, scratch registers and stack layouts are your choice.
count_neighbors is supplied but not required. Equivalent update algorithms and
counting during construction or in a later pass are accepted.

Fourteen public cases use seven patterns in both physical allocation orders.
They include empty, full, stable, oscillating, edge and asymmetric boards. All
destination interiors begin with nonzero bytes, so skipping stores for dead
results cannot pass on an already-zero board. Saved-register values, stack
locations and guard contents vary. Label-relative pointers permit allocations
to move. A case uses a fresh machine and a cumulative 50000-instruction budget.

Each case calls life_changes twice with the SAME unchanged source and the same
destination. The second destination already contains the first result; it must
not become an input to the rule or to the changed-cell count. This tests repeat
calls, not two successive generations. The caller alone exchanges pointer roles.
The first direct call returns to generation_built and the second uses life_done
as a distinct supplied harness continuation, stopping before its exit selector.
These direct tests do not execute the ordinary caller, renderer or role exchange.
The ordinary starter still demonstrates the chapter's two-generation caller.

The native profile contains exact public bytes and results; the adjacent Kotlin
script expresses those cases through the CLI grader. Its word strings target
the book's little-endian environment. From this directory, use JDK 25 and a MARS
CLI with version-3 function protection support:

java -jar /path/to/cli.jar grade life-changes.kts life-changes.asm

Fourteen passing public cases establish 28 returned calls on those patterns and
layouts. They do not exhaust the 2^64 binary boards, arbitrary destination bytes
or all execution accesses. Read a failure as a particular violated state or
protected-store contract and explain it with your own trace. Instructor policies
may control attempts, prerequisites and graduated hints; private checks and
instructor solutions remain in a separate distribution.
