# H.263 prediction and reconstruction unit

Chapter 26 begins the two-chapter revision of original project `035-h263`.
Directly edited native authority is `main/predicting-pictures.tex` and
`labs/predicting-pictures.tex`, included in the revised publication entries.
The main proof is 180 pages, with this unit on 172–180; the lab is 180 pages,
with this unit on 176–180. The original manuscript and the twelve-page advanced
editorial pilot remain unchanged. The proposed general opening explanation about
MARS and hardware has not been added.

## Teaching sequence and independent work

The chapter derives planar QCIF offsets, active-row access and exact SAD before
searching motion. It separates a search policy from required motion units,
uses negative odd displacement to explain chroma origins, and exposes diagonal
double rounding. DCT basis, fixed-point scale, selected levels, reconstructed
coefficients and final signed addition have distinct meanings. A two-picture
constant-block trace distinguishes expected quantization loss from using the
wrong reference. The accompanying lab supplies predictions, counterexamples,
ownership checks and independent implementation work.

The complete `labs/files/h263-encoder.asm` retains all original callable interfaces
with compact branch/call spellings and observation nops. This unit renews the
numerical pipeline and inverse accuracy. The following unit must separately renew
syntax, picture/file behavior, whole-stream independent decoding and quality.
No new full-stream interoperability or encoder timing claim follows from the
numerical results here. The selected target remains actual restricted baseline
H.263, with Python-generated QCIF raw input, no more than thirty pictures/s and
ten seconds. The ordinary clip is eight pictures at 5000/1001 pictures/s.

The independent `h263-reconstruct8.asm` isolates prediction plus signed inverse
correction, final clipping and exact clipped-count publication. Three full-width
native strides admit separately padded eight-row windows. Metadata rejection
precedes sample access; an invalid correction retains output, including an exact
prediction alias. Validation/replay and private staging are both accepted. The
demo reconstructs rows (0,0,1,128,254,255,255,255) and returns 24 clipped sums.
Instructor implementations and expanded/fault cases remain private.

## Renewed numerical qualification

Reference, equivalent and high-code placement each pass 692 public and 3228
private cases: 11760 positive executions across 42 domain runs. The seven domains
are SAD, search, prediction, six-block coordination, transforms, quantization and
block reconstruction. Private package copies of public checks are byte-identical
to the current student checks; exact roots and supplied harnesses are archived.

The full target inverse-accuracy gate runs all six Annex A cohorts for every
variant, mode and byte order: 720000 blocks plus 72 exact-zero calls. All five
error measures pass for every cohort. Its independently generated data, generator,
pinned NumPy environment, scripts, sources, target logs and metrics are retained.
A deliberately wrong zero rounding bias is rejected by all six full MSA/little-
endian cohorts. Those rejected executions are excluded from the positive total.

The independent reconstruction variants pass 1950 cases: 138 public and 512
expanded cases each. Six ordinary-demo cases execute main, inspect every output,
continue to exit and verify published fields in both byte orders. Seven assembled
faults are rejected: unsigned inverse samples, byte wrapping, endpoint miscounting,
early publication, narrowed strides, row-padding reads and an alias shortcut.
The unfinished student body fails all 138 public cases as expected. An earlier
130-case suite precedes additional invalid-sample/alias cases and is retained only
as history. Initial demo API/entry setup diagnostics are not semantic failures.

The printed fixture command produces eight raw pictures, checks whole/per-picture
hashes and the repeated pictures indexed five and six, and refuses an existing output.
The four exact capture probes verify actual computed values and complete relevant
blocks before the graphical observations.

## Native observations and publication evidence

Four new actual IntelliJ Light Debug captures show SAD, fractional prediction,
the chained reconstruction and independent clipped count. The private observation
callers copy real returned fields into native records; captions explain each pair.
The chain's second call uses the first returned sample block as prediction. The
reconstruction photograph uses a completed private body for the public unfinished
demo. Screenshot automation remains in the separate plugin depending on MARS.
The actual clean cleanSandbox runIde tasks ran before launch, the batch completed,
and the owned IDE is closed. Raw PNGs/receipts and native editable crops, outlines,
diagrams, captions and semantic alternatives remain available.

The installed driver is byte-identical to the paired-verified lossless-unit driver;
ten installed MARS JARs match the pinned immutable archive. This unit changes no
plugin implementation or MARS core. All 105 authoring Jupiter and 419 public
book-tooling tests pass. One actual IntelliJ export extends the preserved 833-record
catalog to 834. Both student package validation and distribution build pass.

All new pages and representative grayscale code/figures are visually reviewed.
Automated PDF/UA-2 validation binds the exact final main and lab proofs with zero
failed checks. An earlier main layout and its matching accessibility report remain
archived. These checks do not establish author, learner or assistive-technology
review; those remain open. Chapter 27, advanced native lesson integration and the
remaining instructional-system requirements are still in progress. This unit does
not complete the full curriculum goal.

The subsequent [stream unit](h263-stream-unit.md) now supplies renewed stream/file
qualification and whole-clip independent decoding. This page retains the Chapter 26
numerical publication boundary and its milestone page counts.


## Native reconstruction assessment foundation, 23 November 2026

The continuing `h263_reconstruct8` task now has five native format-21 profiles
retaining all 138 original public cases, 69 per byte order and 146 calls. They
cover sample arithmetic, exact prediction aliases, metadata, invalid-sample
rejection and same-machine reuse. The original 100000-instruction case budgets
remain. Ten saved registers, including gp, and full-width stack/return state
remain checked. No assembly solution is added to the public tools or profiles.

The contract distinguishes metadata status one from invalid-sample status two.
Invalid metadata permits no sample access. All inverse corrections must lie in
[-256,255] before any output store, including a last invalid correction under
an exact prediction/output alias. Valid reconstruction adds unsigned prediction
to signed halfwords, clips only the final sum, and counts sums strictly outside
zero through 255. Sums exactly zero and 255 are not clipped events. Only eight
active bytes or sixteen inverse bytes per row are readable; padding is retained.

Instrumented original runs and the native model agree on canonical case order,
arguments, status/counts and every complete caller object before and after each
call for three implementations. Native execution then passes all 138 cases for
reference, equivalent staging, wide-address and private-scratch variants. Seven
existing assembled faults are rejected, and the unfinished public source fails
all 138 cases. Exact native qualification retains 1656 outcomes across twelve
processes; original/model comparison is separate. No inverse-DCT accuracy or
whole-stream interoperability claim is inferred from these reconstruction checks.

Four new public model tests independently derive signed sums and clipped bytes
from the selected raw input rows, check complete disjoint setup coverage,
endpoint counts, native width, wrapping pointers, publication policies and profile
limits. The full tooling suite passes 613 tests. Two Jupiter tests load the actual
profiles through the production parser, preserving row extents, both orders,
late-invalid aliases and a final assessment with all cases and original budgets.
Production plugin/simulator code is unchanged and all 41 manual edits are retained.

The native student lesson, bounded worked experiments, inline predictions,
first-use/resume IDE verification and native LaTeX integration remain next. The
accepted books and historical H.263 accuracy/conformance receipts are unchanged;
this foundation claims no new screenshot, PDF or accessibility validation.
Private native-h263-reconstruction-foundation-01 retains exact original/model,
qualification, parser and source-preservation receipts.


## Native numerical student-unit draft, 23 November 2026

The draft `revision/lessons/035-h263-prediction` contains six worked sources,
twelve inline predictions, eighteen Debug boundaries across six activities,
one silent Run and six assessment stages. Five experiments call unchanged
supplied numerical helpers and retain the exact transform basis. They cover
active-row SAD and widened lanes, motion-search geometry/ties, diagonal
prediction, the transform/reconstruction chain, and negative-motion six-block
coordination. The last source explicitly models signed final addition and
native stride width without providing the independent function.

The chain begins with a separate actual forward transform of residual two,
showing DC sixteen before the complete encoder's level one, coefficient 23,
inverse three and pixel 131. Its second block uses the reconstructed reference
and remains 131. Constant examples do not replace asymmetric or full inverse-
accuracy qualification. The six-block metadata rejection is identified as an
upfront validation failure; this run does not claim a failure after successful
children. Whole-stream interoperability remains the next unit's separate work.

Six source-bound core probes verify all eighteen stop register/vector snapshots
and subsequent normal silent exits. They additionally verify admitted SAD row
reads, every predicted byte and signed residual, complete forward coefficients,
all four regions of each 448-byte block output, all six coordinated records, and no
output store on rejected prediction/coordinator metadata. Original initial probes
remain separate from the final strengthened probes. No host file or clock is
required for any native numerical observation.

The continuing independent starter is byte-identical to the existing
`h263-reconstruct8.asm`. Five stages retain 60 sample, 24 alias, 36 metadata,
sixteen rejection and two reuse cases, followed by all 138 on the same source.
The final stage preserves both byte orders, 146 calls and every original budget.
Its exact exported form passes all 138 cases with three private instructor bodies,
including wide-address placement; the unfinished body fails all 138. These 552
qualification outcomes add no assembly solution to the public distribution.

Five public unit tests bind helper/basis identity, profile staging, complete
vectors and representation values, deterministic explicit seventeen-file export,
and the actual QCIF generator. The default generator produces the exact eight
raw frames, 304128 bytes, plane extents, whole-file hash and intended repeated
pictures, and refuses existing output. Its default rate and duration satisfy the
course limits. The full tooling suite passes 618 tests. Two Jupiter tests through
the actual importer and native PSI retain all 25 activities and resolve all
thirty prediction/Debug source anchors. Production plugin/simulator code is
unchanged; all 41 manual edits remain preserved.

Build the archive with:

```sh
python3 tools/native_lesson.py revision/lessons/035-h263-prediction \
  --output build/native-lessons/035-h263-prediction-0.1.0-student.zip
```

Actual IDE first use, unsaved checking, source invalidation, fresh-process resume,
public capture and native LaTeX integration remain next. No new screenshot,
publication PDF, accessibility validation, inverse-accuracy gate or stream
conformance run is claimed by this draft. Private native-h263-prediction-unit-01
retains exact probe, package and exported-starter receipts.


### Native numerical IDE and written integration, 23 November 2026

The numerical unit now passes actual first-use and fresh-process resume trials in
IntelliJ. Its twelve inline predictions, eighteen Debug stops, one Run and six
assessments are connected directly to the written lab. The accepted trials retain
thirteen quiz attempts and seven edited sources, renew thirteen execution checks,
and each pass 276 overlapping case placements. The continuing function remains
one editable file; no solution is added to the public archive. The display title
is now “Predict and reconstruct H.263 blocks” after the native layout guard found
that the longer title exceeded the Learn viewport. All fifteen other archived
files, including lesson activities and executable cases, are byte-identical to
the previously qualified draft.

A real public-source SAD stop supplies the new native annotated figure: actual
frame line 185, full w4 and eight halfword lanes of 8160. The caption distinguishes
the actual frame from the editor's earlier source pointer. The text explains
adjacent byte-pair widening across rows, function-specific return units and the
limited scope of the six-block metadata rejection. Raw QCIF generation is an
optional whole-picture extension after the bounded experiments.

The separate authoring plugin passes 143 Jupiter tests, public tooling 618, and
six publisher guards. Both pinned plugins pass compatibility verification against
IntelliJ 2026.3, with no internal API use reported. The accepted 226-page lab proof
was visually reviewed on pages 215–224, with figure 217 also in grayscale; after
a paragraph-flow correction, changed pages 218–219 were reviewed again and all
other reviewed pages are pixel-identical. Exact final PDF/UA-2 validation passes
1728 rules and 1,067,167 checks with zero failures. Private
`native-h263-prediction-ide-01` retains the rejected initial layout trial, both
accepted trials, exact pins, installed-JAR comparisons and saved progress.

This completes native numerical delivery and written integration. Native
restricted H.263 stream delivery and course-wide acceptance remain. No renewed
inverse-accuracy gate, stream conformance run, human learner review or
assistive-technology review is claimed by this milestone.
