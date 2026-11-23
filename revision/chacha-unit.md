# ChaCha20 main chapter and laboratory

Chapter 22, “Transforming words in parallel,” revises original project
`026-chacha20` as connected native LaTeX exposition. Its historical main-book proof places it on pages 140–145; the historical
written lab occupied 142–146. The current 215-page lab proof places the revised
core on 171–176, with the SHA-256/RSA companions continuing through 191. The original manuscript and earlier chapter sources remain
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

## Historical finite verification

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
The original 42-region construction package retains its CLI route. The revised
native unit below uses complete worked observations followed by independent
construction of the four interfaces. Its fixed observation registers do not
prescribe an independent solution’s allocation.
The broader curriculum goal remains active. SHA-256 and RSA companion treatments
follow before WAV, PNG and the retained video sequence. The deferred opening
explanation of MARS versus hardware has not been added.

## Native grading foundation, 23 November 2026

The existing 73 public cases now have a native data author preserving all original
budgets, argument layouts, complete caller snapshots and access windows. The
original checker exports agree in the worked, alternative and wide-address
layouts. Nine native profiles retain 84 original function observations; eleven
additional main-dispatch observations and actual caller termination/status stores
strengthen the supplied-caller checks. Three correct implementations pass; all
eight retained faults and the unfinished starter fail. Per-call memory setup was
added to the public grader so changing inputs across a shared session remains a
real check, rather than a fresh-session approximation. See
[the native contract](../docs/native-chacha-checks.md).

## Completed native instructional draft, 23 November 2026

`revision/lessons/026-chacha20` now exports a versioned native student archive:
14 inline predictions, ten Debug activities with 46 ordered checkpoints, one Run
and six independent groups. It includes nine caller/data variants, the unchanged
complete worked code, the original unfinished source, six optional CLI scripts
and all nine format-15 profiles. The 73 cases retain 84 original observations,
with eleven real-main dispatch observations and eleven caller exit/status checks.
Three correct designs pass; eight retained semantic faults and the unfinished
starter fail. A separate wrong-status-store counterexample is accepted by the
old observation-only checker and rejected by two native caller cases.

Ten actual simulator processes verify every scalar/vector checkpoint, complete
caller arenas and real termination. Native import and PSI tests resolve every
prediction and stop in the intended source. Adjacent assembly aliases use the
label that owns the executable instruction, without altering the worked code.
The 563 public tooling tests, two final archive/import/PSI Jupiter tests and 136
authoring-plugin Jupiter tests pass. The per-call setup implementation also passes
the full 263-CLI/1284-plugin regression and coverage/generated-parser gates; the
two archive tests were added afterward without further production changes.
Packaged API verification passes with 91 MARS and 20 authoring experimental usages
and no internal API dependency.

The final native archive passes actual first-use and fresh-process resume in
IntelliJ Light after `clean cleanSandbox runIde` on each launch. The runs pass 67
and 53 observations. Fifteen quiz attempts, eleven edited sources and all saved
assessment counts survive reopening. Edits invalidate seventeen execution checks,
and actual execution renews them; each assessment count increments exactly once.
All eleven installed JARs match the pinned archives in both phases, and verified
logs have no ERROR/SEVERE entries. These are automated walkthroughs, not learner
review. An earlier successful pair is retained before the vector-view wording
was corrected to name the actual expandable 32-bit integer-lane entries.

The native lab now connects the observations with dependency order, word wrapping,
selection provenance, feed-forward and whole-request publication. It adds a lane
provenance table and a genuine debugger crop showing the expanded word-lane row.
The separate capture plugin and private exporter produce the unchanged PNG and
SHA-bound coordinate/state receipt; native TeX supplies the crops, outline,
caption and semantic description. Four exporter checks guard the reviewed image,
geometry, observed values and edited annotations. The three earlier genuine
light-theme figures remain retained across the main chapter and lab.

The final native proof is reviewed in color, with representative lane/table and
failure-figure pages also reviewed in grayscale. Embedded Minion Pro, Noto Sans
Math and JetBrains Mono remain in use, and the exact proof receives renewed
PDF/UA-2 validation. Its exact digest and result are recorded privately in
`native-chacha-unit-01/publication/review.json`. Human author, representative
learner and assistive-technology review remain pending. No timing, hardware-cycle
or cryptographic-certification result is claimed.

Private evidence is in `native-chacha-foundation-01`, `native-chacha-unit-01` and
`native-chacha-ide-02`; the earlier desktop iteration is `native-chacha-ide-01`.
All 35 original projects and ten companion includes remain in the structural
audit. The SHA-256 and RSA native companions are next before WAV; the expression
and bytecode native companions also remain unfinished work. The full curriculum
goal is active, and the deferred opening orientation has not been added.
