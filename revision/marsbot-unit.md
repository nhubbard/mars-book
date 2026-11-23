# MarsBot feedback companion

The revised independent square is section 4.7, on lab pages 16–21. Direct native
sources are `labs/companions/marsbot.tex` and `figures/marsbot`. The combined lab
proof has 76 pages; the main book is unchanged at 78. Later lab references in
README.md reflect the actual combined PDF. Original project 006 remains versioned
and unchanged; the revised work area is `labs/files/marsbot-square.asm`.

The companion distinguishes a completed command store from completed movement,
then explains feedback, headings, screen coordinates, polling, response latency,
paused execution, stopped-corner reasoning and final-state/trail diagnosis. It
requires the main counter chapter's loops and memory-mapped access, not later
functions or arrays. The optional command-table approach is explicitly deferred.
The complete `labs/files/marsbot-east.asm` experiment isolates one feedback loop;
its public check is `labs/checks/marsbot-east.kts`. The full square is independent.

| Learning outcome | Written investigation | Technical evidence |
| --- | --- | --- |
| Distinguish commands and observations | Trace heading, pen, start and coordinate loads | Protected RAM/feedback stores and actual device state |
| Explain feedback-controlled repetition | State a first-leg invariant and change the rate | All 63 side lengths at every integer rate 8–128 |
| Distinguish reached from stopped | Pause before stop, then step its store | Exact core states and native one-step UI receipt |
| Compose a complete route | Record each stopped corner and final pen command | Exact ordered strokes and written-corner probe |
| Diagnose misleading success | Remove ink, hard-code input, or calibrate a delay | Faults rejected; delay passes default and fails changed cases |

Three square implementations (explicit compact legs, command table, threshold
comparisons) each pass three original public cases and all 7,623 documented
side/rate pairs. The one-leg experiment passes sixteen public cases and the same
7,623 pairs. These are exact finite domains for this contract, not arbitrary
headings or routes. All checks use the actual MarsBot model and its full trail,
not a RAM description of intended corners. Ten semantic faults are rejected.
A further calibrated-delay variant passes the default 16/64 case but fails both
changed public cases, showing why a plausible default drawing is insufficient.
Two written one-leg cases verify reached/moving, stopped/pen-down and final pen-up
boundaries; the original guided-reference probe verifies every illustrated corner.

Three actual IntelliJ Light observations verify moving/stopped east endpoints
and the completed 48-pixel square. The second uses one native Debug step from
east_stop to east_stopped. The public native figure uses only the two one-leg
images, with source already published as the complete experiment. The full-square
image exposes private instructor code in its editor and remains private; it is
not embedded in the public PDF. Source defaults stay 16; the figure explicitly
uses 48 for readable trail strips. PNGs are unchanged; all crops, boxes, endpoint
labels and semantic descriptions are native LaTeX. Coordinate/source/image/archive
bindings are in `captures/marsbot/receipts.json`.

The actual clean cleanSandbox runIde launch completed the three-figure batch.
All eleven installed JARs match the previously API-verified archives, with no
ERROR or SEVERE entries in the completed IDEA log. Native/screen trail checks
found no unexpected ink; marker occlusion and two border pixels are excluded
from those visual checks. These are geometric screen observations, not exact-RGB
claims or a substitute for the core trail checks. No MARS or authoring-plugin
implementation change or new grading API was needed.

Public tooling passes 391 tests and the separate authoring plugin 104 Jupiter tests.
Original project 006 validates and builds. One fresh IntelliJ snippet preserves
all 804 prior records (805 total) and exactly matches the tested one-leg source.
The final lab PDF passes automated PDF/UA-2 with zero failures. New pages 16–21,
adjacent page22 and grayscale18/20 were reviewed. Established fonts, native LaTeX
authority, original main.tex and forty unrelated MARS files are preserved.

All original projects 001–016 now have revised written treatment across twelve
main chapters and five companion labs. Native lessons beyond the first unit,
advanced chapters, production delivery/policies and human learner/accessibility
review remain unfinished. The proposed MARS-versus-hardware opening stays deferred.
This written-foundation milestone does not complete the curriculum goal.

## Later native delivery

The historical written milestone above is retained. Current native lesson, live
first-use/resume verification and instructor/publication bindings are documented
in [marsbot-native-unit.md](marsbot-native-unit.md). Its completion does not relabel
the older figures or the earlier test/page counts as current evidence.
