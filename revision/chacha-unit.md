# ChaCha20 main chapter and laboratory

Chapter 22, “Transforming words in parallel,” revises original project
`026-chacha20` as connected native LaTeX exposition. It occupies main-book pages
140–145 in the 145-page proof. Its written laboratory occupies pages 142–146 in
the 146-page lab proof. The original manuscript and earlier chapter sources remain
preserved. There is no page cap.

## Teaching sequence

The chapter derives word arithmetic and rotation, traces the four dependent
quarter-round groups, selects columns and diagonals, retains originals for
feed-forward and relates word displays to little-endian serialization. Only then
does it arrange four independent quarter-rounds in MSA lanes. It decodes row
selection immediates and explains inverse selections before the next round.
The byte wrapper follows with exact tails, load-before-store aliasing, immutable
caller state and full-request counter preflight. Restart versus continuation and
transformation versus authentication have explicit examples.

The written lab asks for predictions, first-divergence diagnosis, provenance
lane tables, matching scalar/vector boundaries, counted byte footprints and
error proofs. Its independent attempt uses the existing four-entry contract.
A continuation-interface design is an optional paper extension, not an untested
new supported function or graded interface.

`labs/files/chacha20.asm` is intentionally a complete public worked example,
including its caller and the scalar/MSA implementations. The separate original
package starter remains the construction source. Private alternatives, expanded
checks, deliberate faults and instructor evidence remain in the instructor repo.

## Finite verification

The worked source uses compact Release 6 branches and full-width addresses.
Reference, equivalent alternative and above-32-bit-data variants each pass 2,003
existing grading cases: 73 public and 1,930 private, for 6,009 positive case
executions. The alternative may implement its scalar-named entry using MSA;
these are functionally equivalent variants, not a claim of three independently
implemented scalar algorithms. Eight assembled semantic faults are rejected by
the same public suites that pass the unmodified worked source.

Four further checks verify the exact first quarter group before publication,
one scalar double-round, vector column selection and vector diagonal state before
inverse selection. A separate masked-integer arithmetic model verifies the
printed quarter trace, block bytes, shuffle inverses and counter examples.
Three CLI boundary probes match the actual native Debug capture fixtures.
The student package validates and its deterministic student distribution builds.

The separate authoring plugin passes 104 Jupiter cases. Its dedicated capture IDE
was launched through actual `clean cleanSandbox runIde` tasks with the pinned
MARS archive. Three completed IntelliJ Light observations are published with
byte-identical PNGs, receipts, native LaTeX outlines, captions and alternative
text. The cropped figures state which bytes and registers they do not picture.
No authoring code was added to MARS. The owned capture IDE was closed after the
successful batch.

Both code excerpts use existing exact records in the 825-record actual IntelliJ
highlighter corpus. The manifest remains unchanged; no fresh export is claimed.
The native template retains Minion Pro, Noto Sans Math and JetBrains Mono.
All new pages receive color layout review, and code/figure pages grayscale review.
Final automated PDF/UA-2 reports and exact proofs are archived privately.
These checks do not establish teaching effectiveness or assistive-technology usability.
No elapsed-time, hardware-cycle or cryptographic certification result is claimed.

## Remaining work

Author, representative learner and assistive-technology review remain pending.
This advanced package has CLI construction/checking support; its native guided
lesson, delivered progression and enforced instructor policies are not complete.
The broader curriculum goal remains active. SHA-256 and RSA companion treatments
follow before WAV, PNG and the retained video sequence. The deferred opening
explanation of MARS versus hardware has not been added.
