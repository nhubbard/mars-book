# Bounded interfaces unit

Chapter 12 uses `main/bounded-interfaces.tex` and the written supplement
`labs/bounded-interfaces.tex`. The original chapter milestone began on main page
72 and lab page 55, in proofs of 78 and 59 pages. Those figures describe that
historical milestone, not the current full composition. Native LaTeX remains
authoritative. The original verification below is retained; the native interactive
integration is recorded afterward.

The chapter uses an invented binary packet format to explain supplied bounds,
three distinct lengths, explicit byte order, error priority, checksum arithmetic,
full-width addresses and delayed output stores. It follows a complete runnable
`labs/files/bounded-packet.asm`, preserving original project 013's interface.
Three printed fragments match its instructions exactly. A native byte diagram
and format/priority tables support the prose.

The lab asks learners to predict incomplete prefixes, observe tentative values
before publication, inspect the returned record, introduce decoding/checksum
faults and explain borrowed input storage. Four real IntelliJ Light captures
produce two native annotated comparisons: valid versus eight-byte availability,
and valid versus incorrect-checksum output bytes. PNGs are unchanged; original
coordinates, observed values and image/source/archive hashes are retained in
`captures/bounded/receipts.json`.

Five packet variants each pass 619 public parser, 5,016 private parser and 38
public program cases. They include prefetched headers, signed payload loads,
private stack use and data above 4 GiB. Seven deliberate packet faults fail.
Six additional written-example checks verify exact intermediate values, the four
success stores, incomplete-window reads and unchanged checksum under a balanced
payload exchange. Coverage is bounded and does not prove every arbitrary stream.

The independent exercise is `labs/files/bounded-line.asm`, with public checks
`labs/checks/bounded-line.kts`. It accepts one LF-delimited binary record with at
most 32 payload bytes, excluding LF from the sum. Other bytes, including zero,
are payload. The 33rd supplied byte distinguishes an exact-capacity delimiter
from a too-long record; 32 non-LF bytes without it still need more input.

One-pass, two-pass and stack-using implementations each pass 394 public, 924
expanded and 517 additional cases. A signed-byte sum variant passes 394 public
and 517 additional cases; wide data passes the 924 expanded cases. Five faults
are rejected, and the unfinished starter fails all 394 public cases. Grading
checks permitted reads and no-store failures, accepts equivalent implementations
and preserves caller state. Full solutions/expanded checks remain private.
No grader API change was needed. An initial private observation probe used an
unavailable snapshot method; the rejected probe is preserved separately from the
corrected passing checks, which use the existing access-event API.

The public tooling suite passes 391 tests and the separate authoring plugin 104
Jupiter tests. Three new IntelliJ exports preserve all 800 prior highlight records
(803 total). Original project 013 validates and builds. The dedicated IDE launch
used actual clean cleanSandbox runIde tasks; all eleven installed plugin JARs
match the previously API-verified archives. Its completed four-figure batch has
no ERROR or SEVERE entries. All new pages and representative grayscale figures
were reviewed, and both final PDFs pass automated PDF/UA-2. Established Minion
Pro, Noto Sans Math and IntelliJ-derived minted styling remain in use.

Human learner/assistive-technology reviews remain open. The native interactive
version was subsequently integrated as recorded below. The UTF-8 companion now follows in section 12.6; see
utf8-unit.md. The clipped-sprite companion is now section 6.6; see clipped-unit.md.
The MarsBot companion is now section 4.7; see marsbot-unit.md. Later chapters
and remaining instructional-system work are unfinished. Reaching
twelve main chapters does not complete the foundational phase or overall goal.
The proposed MARS-versus-hardware opening remains deferred. The original manuscript
and unrelated MARS edits are preserved.


## Native interactive integration, 22 November 2026

`lessons/013-bounded-interfaces` now supplies seven predictions, twelve Debug
boundaries, three ordinary Runs and three independent assessments. The accepted,
short-header and checksum sources are complete and separate. The short source
supplies seven bytes, failing before the first input read. The existing return
figure supplies eight bytes, passing the header bound before failing the complete
packet bound; the lab now explicitly distinguishes these examples. The lab also
explains the next-stop highlight, actual paused stack frame, tentative error
values and the caller's later metadata stores.

The independent exercise retains all original input families. Native profiles
cover 139 boundary/caller cases, 256 first-byte cases and normal program exit,
including 789 function returns across repeated continuations. They check full
returned and stored pointers, label-relative access bounds and forbidden execution
intervals. The new checker was implemented and tested upstream rather than
weakening the original script's requirements. Four correct algorithms, relocated
layouts, wide addresses and deliberate faults have separate private qualification
evidence. The historical statement that no grader change was needed applies only
to the earlier script route.

Two fresh-process IDE walkthroughs passed. Actual inline choices include an
incorrect answer followed by recovery. Debug, Run and independent-check controls
use the production student system. The unfinished starter fails; unsaved correct
source passes. Edits clear all nine execution passes. Reopening retains eight
quiz attempts and re-earns every execution pass without changing the four source
hashes. All eleven installed JARs match pinned archives and both IDE logs have
no ERROR or SEVERE entries. The separate authoring plugin passes 122 Jupiter tests;
all 477 book tests pass. The exact MARS and authoring archives pass API verification.
The reused MARS checker has 194 CLI / 1203 plugin regression results and required
coverage gates, followed by an exact archive import test. These are automated
results, not learner review. All 41 manual MARS edits remain preserved.

The written lab is integrated directly in native LaTeX. Final PDF rendering,
grayscale figure review, embedded-font inspection and renewed PDF/UA-2 evidence
are retained privately under `verification/restructuring/native-bounded-ide-01`.
The unchanged UTF-8 companion follows the main lab; its native interactive
integration remains separate work. The full curriculum goal remains active.
