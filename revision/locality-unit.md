# Revised memory access and locality unit

The eleventh main chapter is `main/memory-locality.tex`; the written supplement is
`labs/memory-locality.tex`. The current full lab supplement has 200 pages; this unit occupies pages 75–80.
Native LaTeX remains authoritative. The following original pilot evidence is
retained separately from the later native integration below.

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

Human learner and assistive-technology reviews remain open. Later native units, further companions and remaining instructional-system work
are unfinished. The proposed MARS-versus-hardware opening remains
deferred. This unit does not complete the curriculum goal.


## Native integration, 22 November 2026

The development lesson `011-memory-locality` 0.1.0 preserves the worked ordinary
source, adds a complete copy changing only METHOD to one, and retains the full
rotation contract. Six predictions lead through two four-stop Debug routes,
two ordinary Runs and independent transfer/caller and normal-exit checks.
The native JSON profiles and standalone script are separately stated public
corpora. See the restructuring plan for their scope and equivalent-solution
qualification; neither prescribes a private traversal or cache count.

Two actual light-theme IDE processes verify source switching and source pointers,
all eight register snapshots, native cache counters, ordinary Runs, failing
starter feedback, unsaved correct text and both assessments. Source edits clear
all six execution passes. Fresh-process comparison retains seven quiz attempts
and freshly earns all six checks. Both projects close normally, all eleven
installed JARs match and logs contain no ERROR or SEVERE entries. Capture and
fixture code remain in the separate authoring plugin.

All 473 book tests and 121 authoring Jupiter tests pass. The reused production
build's full 184 CLI / 1198 plugin regression and coverage are retained separately
from its subsequent focused exact-archive test. Both exact plugin ZIPs pass the
API verifier. The written lab now explains the two routes, pending caller store,
read/write evidence, revision credit and independent checking boundaries.
Pages 75–80 and the cache figure in grayscale were inspected. The corrected
200-page proof passes automated PDF/UA-2: 1728 rules / 938435 checks, zero
failures. Minion Pro, Noto Sans Math and JetBrains Mono remain embedded. Existing
actual photographs, native annotations and all 41 manual MARS edits are preserved.
Human learner and assistive-technology review remain pending; the full curriculum
goal remains active.
