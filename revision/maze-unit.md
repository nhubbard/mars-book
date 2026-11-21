# Revised maze written companion

`labs/companions/maze.tex` is section 9.6 of the lab book's “Sorting and search”
chapter. It begins on page 39 and ends on page 45 of the current 45-page lab proof.
The main proof remains 58 pages. Native LaTeX remains the composition authority.

This companion develops graph geometry, cell IDs versus addresses, discovery-time
marking, a monotone FIFO frontier, the shortest-layer argument, parent reconstruction,
zero-edge success, exhaustion and coherent observation. A four-cell room provides a
native diagram and exact queue-history table. A separate figure compares two actual
light-theme Debug observations with native cell numbers and route outlines. It is
readable without relying on the path/processed colors.

The public independent work area is `labs/files/maze-search.asm`. Its allocation
layout and fixture bytes match original project 010, with the long input declaration
wrapped for reading. Its two deliberately unfinished bodies stop with break; students
choose their private decomposition. The archived package and its versioned contract
remain unchanged. Both original public checks apply to the revised work area.
Complete maze implementations stay in the private instructor repository.

The complete `labs/files/maze-neighbor.asm` experiment isolates coordinate bounds.
It returns an orthogonal neighbor ID or minus one for every valid cell/direction
pair. The printed function is an exact suffix of that source. Public checks in
`labs/checks/maze-neighbor.kts` cover all 256 valid pairs and the complete caller.
An unsigned-range implementation and a relocated-data implementation also pass.
Unsigned displacement loads and a missing column bound are rejected. Invalid
input IDs/directions are outside the stated helper contract.

| Outcome | Written activity | Technical evidence |
| --- | --- | --- |
| Distinguish adjacency from array bounds | Reject east of cell seven and diagonal-only contact | Exhaustive valid helper domain and disconnected maze cases |
| Make one discovery per cell | Trace nine's first parent and unique append | Every published transition checked across two neighbor orders |
| Explain shortest distance | Relate FIFO removal to increasing layers | Independent shortest-distance oracle and predecessor-chain checks |
| Reconstruct a route correctly | Compare edge count, mask count and zero-edge success | Both routes accepted; missing start and cell-count faults rejected |
| Debug state before a final picture | Inspect phase, head/tail, history and parents | Exact printed five-boundary trace and independent renderer calls |

Each compact-transfer private maze implementation passes eight public full-search
cases, 416 private cases and five independent renderer cases. The private suite
covers all eligible wall masks in a three-by-three room in both endpoint directions,
96 seeded full grids and 64 isolated start-equals-goal cases. It is not exhaustive
for eight-by-eight mazes. Six complete-search faults are rejected, and the revised
unfinished starter fails all eight public search cases. No grading API change was
needed; valid alternate parent choices remain acceptable.

Both actual native IDE captures verify all 64 canvas cells and tooltip values
exactly. The accepted photograph check allows one color level of macOS ICC
conversion; a zero-tolerance rejected capture is retained privately. Original PNGs
are published unchanged. The native publication crops the open room, labels IDs
and outlines route cells. No private code is visible in either image or distributed
with the public materials. Coordinate and image receipts are in `captures/maze`.
The separate authoring plugin ran actual clean cleanSandbox runIde tasks before
each launch and byte-matched the previously API-verified plugin archives. The
accepted batch has no ERROR or SEVERE log entries.

The public tooling suite passes 391 tests; the separate authoring plugin passes
104 Jupiter tests. All 795 prior highlighting records are preserved and one actual
IntelliJ export is appended (796 total). Original project 010 validates and builds.
All changed proof pages and representative grayscale figures were visually reviewed;
the final lab proof passes automated PDF/UA-2 validation with the established fonts.
The original manuscript, main proof and unrelated MARS edits are preserved.

Human learner and assistive-technology reviews remain pending. A native interactive
maze lesson, other companion revisions, later chapters and remaining instructional
system features are open. The opening explanation about MARS versus hardware remains
deferred. This companion does not complete the curriculum goal.

A later caller amendment during Chapter 15 aligns the stack before the first
call, including leaf routines. Relevant caller and behavior checks pass again;
RGB captures were refreshed from the aligned source. The maze-neighbor experiment
has no figure tied to its caller setup. Earlier milestone evidence is preserved;
see [pcm-unit.md](pcm-unit.md) for the amendment and current proof scope.
