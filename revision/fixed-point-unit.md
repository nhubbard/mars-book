# Revised fixed-point unit

The tenth main chapter is `main/fixed-point.tex` and its written supplement is
`labs/fixed-point.tex`. The current full learning book has 189 pages; the lab supplement has 199.
The fixed-point lab occupies pages 70–74. Composition remains native LaTeX.
The later paragraphs retain the original chapter-pilot verification separately
from the native lesson integration below.

The chapter uses a complete public bouncing-point program to explain raw/256,
units per update, representable versus accepted range, reflection of an overshoot,
exact contact direction, floor and nearest quantization, coherent publication and
word/doubleword arithmetic. A native reflection diagram and a nine-row trajectory
table support the explanation. Two printed code fragments match the complete
source exactly and use actual IntelliJ highlighting.

The written lab adds prediction, intermediate-call inspection, boundary and
quantization faults, a native overflow experiment and independent signed
interpolation. Its two native figures use three unchanged actual light-theme IDE
captures. They compare nearest-mode ticks seven/eight and wrapped/exact sum
registers. Crop geometry, semantic descriptions and annotations remain native
LaTeX; original PNGs and coordinate receipts are in `captures/fixed-point`.

| Outcome | Written activity | Technical evidence |
| --- | --- | --- |
| Keep scale and units consistent | Derive raw seed values and several updates | All nine table rows checked in both display modes |
| Preserve traveled distance at a wall | Compare reflection, clamping and exact contact | Independent folding oracle on 7,937 axis cases per implementation |
| Separate display from state | Compare nearest-mode ticks seven and eight | Exact native bitmap cells and coherent raw-state probes |
| Justify intermediate width | Compare wrapped word and exact doubleword, then investigate ADD | 400 private sum pairs per implementation and actual native overflow pause |
| Implement signed rounding independently | Weighted interpolation with endpoint/tie/range arguments | Three solution forms pass 3,368 public and 17,472 expanded input triples each |

The complete source is `labs/files/fixed-point-ball.asm`. Its compact-transfer
implementation follows original project 012's public ABI; the original package
and versioned contract remain unchanged. It and a different renderer/axis/probe
implementation pass 372 public kernel cases, 48 public animation cases, and the
private axis/render/probe/program suites. Private renderer coverage is 3,586 calls;
animation checks 240 complete runs. These are documented domains and samples,
not all possible motion trajectories or signed-word pairs. The revised renderer
also passes public kernel checks with static data above 4 GiB.

The independent starter is `labs/files/fixed-interpolate.asm`, with public checks
in `labs/checks/fixed-interpolate.kts`. It accepts full signed-word endpoints and
integer weights zero through 256, returning nearest raw results with half ties
upward. Complete weighted-sum, difference and stack-using implementations remain
private. The public checker reports two cases, one aggregating 3,368 calls; the
expanded checker likewise aggregates 17,472. No scratch-register convention,
private labels or exact instruction count is required. The unfinished starter
fails both cases. Five interpolation faults and six worked-program faults are
rejected. No grader API addition was needed.

The actual dedicated authoring IDE ran clean cleanSandbox runIde before launch.
All eleven installed JARs match the previously API-verified plugin archives; the
log has no ERROR or SEVERE entries. Native breakpoint conditions select ticks seven
and eight. All 128 native bitmap cells and photograph samples match with zero
color tolerance; native decimal register rows verify the overflow comparison.
Only the completed dedicated instance was stopped after its batch receipt.

The public tooling suite passes 391 tests and the separate authoring plugin passes
104 Jupiter tests. Fresh highlighting preserves all 796 earlier exports and appends
two (798 total). Original project 012 validates and builds. Every new proof page
and representative grayscale figures were visually reviewed; both final PDFs
pass automated PDF/UA-2 with Minion Pro, Noto Sans Math and JetBrains Mono. The
original manuscript and unrelated MARS edits remain preserved.

Human learner and assistive-technology reviews are pending. Later native units,
additional companions and remaining instructional-system features are open. The proposed opening explanation about
MARS versus hardware remains deferred. The curriculum goal is not complete.


## Native lesson integration, 22 November 2026

The development student package is `lessons/012-fixed-point`, version 0.1.0.
Five inline predictions and eight ordered Debug stops lead from raw/256 scaling
to a mixed register pair between calls, complete publication, two overshoots,
ordinary silent Run and independent signed interpolation. Guided observations
use floor mode throughout. The existing nearest-mode photographs remain labeled
as experiments in a separate copy; their pixel IDs are intentionally different.

The new public profile retains all 3368 original scalar triples in seven bounded
fresh-machine cases, then runs the complete caller through its answer store in
an eighth case. A separate console assessment checks silent normal exit. Both
assessments use the same source and bind credit to its current revision. Three
correct forms, including private stack use, pass both grading routes in ordinary
and relocated data layouts. Eight injected faults and the starter are rejected.
The additional caller-store and abnormal-exit faults establish why the two
caller observations are needed. These are finite tests, not exhaustive proof.

The grader now includes the failed call number, entry, continuation and supplied
scalar arguments in feedback. Three new Jupiter tests check captured arguments,
empty arguments and cancellation propagation. Full checks passed 171 CLI and
1195 plugin tests, with required coverage checks; the exact fixed-point archive
adds a focused importer test afterward. All 469 book tests and 120 separate
authoring Jupiter tests pass. Exact builds pass the IntelliJ API verifier.

Desktop and fresh-process verification are recorded in the restructuring plan.
The initial automated attempt stopped after all eight Debug states because its
Run-text expectation omitted “the unchanged”; this authoring-only mismatch was
corrected and retested. Retained evidence distinguishes that incomplete attempt
from completed walkthroughs. It is not student or representative learner review.

The updated native lab includes the full ordered observation narrative,
negative tie and range reasoning, both check boundaries and reproducible error
inputs. Pages 70–74 and the contact figure in grayscale were inspected. The
199-page PDF passes automated PDF/UA-2: 1728 rules and 932432 checks, no failures.
Minion Pro, Noto Sans Math and JetBrains Mono are embedded. Original figures,
manuscript history and all 41 manual MARS edits remain preserved. The deferred
opening explanation about MARS and hardware has not been added.
