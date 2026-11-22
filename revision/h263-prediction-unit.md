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
