# A frontier of unfinished work

Read the maze companion lab after the sorting chapter. This unit assumes byte
arrays, row-major addressing, loops and calls that preserve live state. It adds
an implicit graph, a first-in, first-out frontier and the argument that connects
that order to shortest distances. Use MIPS64 Release 6, Default memory and
little-endian byte order.

The questions appear inline in Learn. Read their explanations, predict the result
and use the feedback to reconsider an incorrect answer. Source pointers identify
instructions for discussion; they do not establish a paused debugger state.

## Separate an index from a move

`worked/neighbor.asm` is a complete experiment, rather than a search skeleton.
Cell ID 8y+x describes a row and column in an eight-column grid. The helper takes
a valid ID in a0 and a direction index in a1: zero north, one east, two south,
three west. It returns a neighboring ID in v0, or -1 if the candidate leaves the
grid. The helper does not read walls. A geometrically valid neighbor can still
be blocked, or already discovered by the search.

Debug this source with the breakpoints requested by Learn. Before `andi` at
`neighbor_id`, the caller has supplied cell seven and east. Before the first
`bgec`, the candidate column is eight. That coordinate fails the upper bound,
even though the mistaken flattened ID eight would fit the array. At
`neighbor_done`, the helper has returned -1 and the caller's store is still next.

Stop that session before debugging `worked/north.asm`. It changes only the call
to cell nine and north. Before the second `bltzc`, the row is zero and the signed
displacement in t3 is -1. At the caller's store, v0 contains ID one. Explain both
why `lb` is appropriate for the displacement and why the search still needs to
consult walls and discovery state.

Run `worked/neighbor.asm` normally after the debugger observations. It records
-1 in `answer` and exits with no console output. The empty transcript establishes
normal exit, rather than the neighbor's value. `worked/neighbor.kts` supplies the
original separate command-line checks: all 256 cell/direction combinations,
changing return continuations, caller-state preservation and the complete caller.
These are distinct from the native Run observation.

## Give unfinished work a precise meaning

Open `independent/maze.asm`. Its deliberate `break` bodies remain for you to
replace. Do not write a program that only paints the default path: the checks
supply different walls, endpoints and poisoned output state. Keep the public
allocations and labels described in `independent/README.md`.

Read the companion's small-room trace before coding the search. Track three
separate records: a cell's discovery state, its first parent, and its distance
in edges. The queue records discovery order. After head removes an entry, its
byte remains as history, while the half-open interval [head,tail) contains pending
work. Mark a cell waiting when discovering it, so two predecessors cannot append
the same unseen cell before either copy is processed.

A neighbor order is a choice. East before south can produce path 0,1,9; south
before east can produce 0,8,9. Both have two edges. The assessment follows the
submitted discovery order to check first parents, and independently checks
shortest distances. It does not require the reference's register allocation,
private helper labels or exact instruction count.

## Make the observation boundary trustworthy

You may implement `render` first. Its native assessment calls it independently;
main may still contain its unfinished search. All 64 pixels must be replaced
from walls, states and path. Check the color priority carefully. Restore saved
registers and sp before reaching `maze_ready`, so a debugger paused there sees
the complete frame and a valid published interface.

Then implement initialization, each complete expansion, and the terminal result.
Use `render` to publish phase zero, phase one after every dequeued cell, and phase
two after success or exhaustion. Dequeue the goal without expanding its neighbors.
Reconstruct one parent chain on success; length counts edges, so a zero-edge
success still marks its one endpoint. The written lab explains why FIFO order
processes increasing distance layers.

Use **Check this program** after edits. Distinguish a wrong discovery or parent
from corrupted queue history, a stale pixel, an input store or nontermination.
Each case uses an independent session and leaves your active debugger untouched.
The renderer has five public fixtures; the complete search has eight. The search
checks intermediate publications and the final state after normal exit.

After both assessments pass, run your independent program normally and inspect
Bitmap Display. The source directive selects an eight-by-eight logical grid.
Explain the final blue chain and its length using your own discovery order.
An empty console transcript establishes normal exit; it does not replace the
transition checks or prove a shortest route by itself.

**Choose Downloaded MARS Lab Activity...** can open an assessment directly when
instructor policy permits it. Guided and free-writing entry use the same source
and behavior checks. Hints are released in stages according to course policy.
Progress and attempts are saved; editing a source requires renewing its execution
observations. This is a development edition. Live first-use/resume verification
and representative learner review are recorded separately.
