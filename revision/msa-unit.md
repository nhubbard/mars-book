# Working with MSA lanes

Chapter 14 develops original project `019-rgb-formats` after the scalar measurement
chapter. It assumes scalar loads/stores, unsigned bytes, bit fields, full-width
pointers, functions and loop bounds. The new chapter begins on main page 86; its
written supplement begins on lab page 82. The combined review proofs contain 91
main pages and 85 lab pages. Sources are directly edited native LaTeX in
`main/working-with-msa.tex`, `labs/working-with-msa.tex` and `figures/vector/`.

Five outcomes link explanation to observable work:

- Compute packed fields and explain lost input bits; derive numerical words and
  byte order before comparing the preview.
- Interpret one 128-bit register as byte and halfword lanes; predict and inspect
  the same gathered bits through both native debugger rows.
- Derive shuffle controls from the actual overlapping windows, including the
  rightmost source's selector range and controls overwritten by their results.
- Prove complete-group and scalar-tail bounds together; investigate count zero,
  seven, eight, nine and seventeen and mapped bytes outside a selected slice.
- Implement RGB555 independently, establishing a scalar baseline, a vector layout
  and a tail, then explain strategy separately from behavioral acceptance.

The complete public `labs/files/rgb-formats.asm` has compact branches, working
pack/expand routines and a two-row preview. The main text develops the packing
kernel; expansion supplies the runnable comparison and remains a later reading
exercise. Four short literal fragments match the complete source. The first pixel
(123,45,67) packs to 0x7968, stores 68 79 and expands to (123,44,66).
Native figures explain field positions and all channel selectors. Two actual
light-theme Debug captures show the gathered and placed halfwords in w4, including
its simultaneous byte view. Native LaTeX provides crops, boxes and semantic
alternatives; unchanged PNGs and component geometry/hash receipts are retained.

The independent `labs/files/rgb555.asm` has a deliberately failing body. Its
`labs/checks/rgb555.kts` suite checks every supported count and a default caller,
34 public cases in total. Scalar and vector implementations pass these cases and
2080 expanded cases each: all count/offset pairs for one correlated source/output
offset mapping, plus a representative for every RGB555 output bucket. A
wide-address vector variant passes the expanded suite. This is not exhaustive over
all RGB888 triples or all independently chosen source/destination offset pairs.
A final review corrected the initial offset formula, which had exercised only
even output addresses. The expanded sweep now visits all 32 output byte offsets
for each count; a destination-rounding counterexample is rejected. That routine passed the old
sweep, confirming the gap. Unaligned halfword loads and stores are valid on this
Release 6 target: two additional halfword variants pass the final public and
expanded suites. The new chapter and earlier PCM pilot wording have been corrected. The
prior formula, results and counterexample observations remain private.
The first worked pixel becomes 0x3ca8; white becomes 0x7fff with a zero top bit.

The worked vector, scalar alternative and wide-address implementation each pass
619 original public kernel, 33 caller, 12 reuse and 6656 private cases. The private
set expands every RGB565 encoding, covers all red/green pairs with a coupled blue,
selects a representative from every packed bucket, and adds deterministic random
cases. It does not cover every RGB888 triple. Equivalent wider red field shifts
and an unused red high-byte variation pass the private set as well. Their initial
fault classification was corrected; the original observations remain private.
The checks were not weakened to make them pass. The text explicitly distinguishes
sensible worked intermediates from conditions required of every valid schedule.

Nine RGB semantic faults and five RGB555 faults are rejected. The additional
controls-only-once counterexample has a correct first group and an incorrect
second gather, verified in separate staged cases. Six positive written-example
cases verify the exact loaded bytes, gathered halfwords, placed fields, stored
bytes, retained controls and group/tail pointer/count observations. Intermediate
state probes apply to this worked schedule, not arbitrary free-writing solutions.

The authoring suite passes 104 Jupiter tests and public tooling 391 tests. The
existing package validates and builds its student archive. All four fragments have
actual IntelliJ exports; three add new records and one exactly matches an existing
record, leaving all previous 806 unchanged and 809 total. Captures use the pinned
packaged MARS and separate authoring plugin, with eleven installed JAR bindings.
The launch used actual `clean cleanSandbox runIde`; the dedicated completed IDE
was terminated with SIGTERM. That cleanup is not a native close/restart test.

The final proofs receive page/grayscale review and renewed automated PDF/UA-2
validation, with hashes bound to the archived PDF bytes. Superseded layout proofs
and their validator receipts are retained separately. Human author, learner and
assistive-technology reviews remain open. This unit uses ordinary Run and Debug;
its native interactive lesson is pending. No MSA wall-time speedup is claimed.
No MARS implementation was changed, and unrelated edits and the original manuscript
are preserved. The overall curriculum and production instructional-system goal
remains active; PCM mixing is the next main chapter.
