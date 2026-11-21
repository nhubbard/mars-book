# Revised memory access and locality unit

The eleventh main chapter is `main/memory-locality.tex`; the written supplement is
`labs/memory-locality.tex`. They begin on main page 65 and lab page 50. Current
combined proofs contain 71 and 54 pages. Native LaTeX remains authoritative.

The chapter derives rectangular transpose addresses before introducing locality.
It explains active windows, relative-index versus pointer width, arbitrary word
patterns, source/destination recurrences, line hits, LRU, allocation on store miss,
clipped tiles, placement constraints and measurement boundaries. A native matrix
diagram shades the first clipped tile; two tables show actual access and cost
observations. Two printed fragments match the complete public source exactly and
use actual IntelliJ highlighting.

The worked program is `labs/files/matrix-locality.asm`, following original project
011's unchanged contract. Both methods copy every active word exactly once; ordinary
traversal follows source rows and tiled traversal completes clipped 4x4 tiles.
Main selects one method per launch. The original versioned package is preserved.

| Outcome | Written activity | Technical evidence |
| --- | --- | --- |
| Derive two row widths | Map two cells and the complete 3x5 matrix | All 289 dimension pairs in both functions, with interior windows |
| Explain access order | Trace source/destination recurrences and the first ten notices | Exact first-row read/write addresses and hit/miss sequence |
| Clip a tile correctly | Stop before the second tile and inspect twelve completed cells | Exact source IDs, output prefix and 24-access/seven-miss boundary |
| Separate locality from placement | Derive sets from row stride and actual bases | Cold full/direct/two-way measurements and real native cache views |
| Derive a new mapping independently | Rotate a rectangle clockwise | Source-order, destination-order and stack-using solutions accepted |

Literal, column-first outer tiles, stack-using, reversed-row-within-tile and wide-data
versions each pass 20 public and 578 private cases (all 289 shapes in both methods).
These vary documented windows and word patterns; they do not exhaust all input words
or possible pointers. Six semantic faults are rejected, including a correct-output
ordinary traversal substituted for the tiled objective. Valid traversal alternatives
remain accepted. Wide data is tested above 4 GiB.

`tools/measure_revision_transpose.py` checks twelve shape/method combinations and
retains cold full/direct/two-way counts, completed instructions, actual access logs
and source/CLI hashes. On 16x16, ordinary/tiled fully associative misses are 320/128
while instructions are 1,643/2,457; direct misses are 344 for both. Every cache has
eight four-word lines, LRU and equal 128-byte capacity. Measurement starts at
function entry and stops before the caller's count store. It includes auxiliary
traffic if present; these two leaf implementations have none. These are core counts,
not a host-time or hardware speedup claim. Printed table rows are checked against
the measured receipt.

The independent work area is `labs/files/matrix-rotate.asm`, with public checks in
`labs/checks/matrix-rotate.kts`. It maps source (i,j) to output (j,R-1-i), accepts
any traversal, and requires bounded one-read/one-write word traffic and preserved
caller state. Three implementations each pass ten public and 289 expanded shape
cases, with varying valid windows. A wide-data implementation also passes all 289.
Four faults are rejected; the unfinished starter fails all ten public cases. Full
solutions and expanded grading stay private. No grader API addition was needed.

Four actual light-theme Debug observations verify the first clipped tile and the
large ordinary/full-tiled/direct-tiled cache states. Two unchanged PNGs are published
with native LaTeX crops/boxes and semantic descriptions; coordinates and source/image
bindings are in `captures/locality`. The IDE used actual clean cleanSandbox runIde
tasks, and all eleven installed JARs match the previously API-verified archives.
The completed batch has no ERROR or SEVERE log entries. The transient small-tile
unaccepted diagnostic is retained privately, distinct from its accepted receipt.

The public tooling suite passes 391 tests; the separate authoring plugin passes
104 Jupiter tests. Two fresh IntelliJ exports preserve all 798 prior records
(800 total). Original project 011 validates and builds. All new proof pages and
representative grayscale figures were visually reviewed; both PDFs pass automated
PDF/UA-2 with the established fonts. The original manuscript and unrelated MARS
edits are preserved.

Human learner and assistive-technology reviews remain open. A native interactive
locality lesson, later chapters, further companions and remaining instructional-
system work are unfinished. The proposed MARS-versus-hardware opening remains
deferred. This unit does not complete the curriculum goal.
