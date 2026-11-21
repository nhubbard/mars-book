# Bounded interfaces unit

Chapter 12, `main/bounded-interfaces.tex`, begins on main page 72; its written
supplement, `labs/bounded-interfaces.tex`, begins on lab page 55. The combined
proofs contain 78 and 59 pages. Native LaTeX remains authoritative.

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

Human learner/assistive-technology reviews and a native interactive version of
this lab remain open. The UTF-8 companion, clipped blitting and MarsBot companion,
later chapters and remaining instructional-system work are unfinished. Reaching
twelve main chapters does not complete the foundational phase or overall goal.
The proposed MARS-versus-hardware opening remains deferred. The original manuscript
and unrelated MARS edits are preserved.
