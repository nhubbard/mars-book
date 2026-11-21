# Mixing samples without losing their meaning

Chapter 15 develops original project `020-pcm-mixing` after MSA lanes. It assumes
signed integer representation, byte order, full-width function interfaces, vector
lanes and group/tail bounds. The main chapter begins on page 92 of the 98-page
main proof; its written lab begins on page 86 of the 90-page lab proof. The
advanced review entry includes the same seven-page PCM chapter followed by the
five-page H.263 reconstruction excerpt (pages 8–12). Native LaTeX remains the
publication authority.

Five outcomes link the prose to executable work:

- Recover a signed sample from bytes and distinguish its exact sum from the
  halfword representation stored by saturation or wrapping.
- Distinguish an exact endpoint from a clipped sum, then derive an event count
  from a comparison mask and a population count whose unit is bits.
- Carry the processed-prefix and clipping-count invariants through complete groups,
  a scalar remainder, exact aliases and repeated calls.
- Find the first incorrect interpretation, counter update or access instead of
  diagnosing only a final waveform or endpoint value.
- Implement saturated subtraction independently, explaining operand order, clipping
  evidence, group bounds and the difference between identical inputs and disjoint
  inputs. Explain why clipping several additions at intermediate stages changes
  the result and prevents arbitrary regrouping.

`labs/files/pcm-mix.asm` is a complete worked mixer and wrapping comparison. It
returns the processed count and strict clipping count separately. The caller
publishes these results only after the named return stops. Its ordinary default
processes sixteen samples with six clips. The main listing is the exact group
instruction sequence, omitting observation labels and normalizing indentation;
a binding receipt retains that comparison. Its existing IntelliJ record matches
a fresh actual export exactly, so all 809 highlight records remain unchanged.
The literal fragment also passes 128 complete alias observations covering 1024
sample pairs, retained count accumulation and the entire guarded data image.

A native diagram explains why mask lanes 2,4,5,6 contribute 16 and 48 set bits in
two doublewords, then four samples after division by sixteen. Three actual
light-theme Debug captures show the mask, population counts and an in-place
nine-sample tail. The last selected sample changes to bytes `00 80`; its following
unselected neighbor remains `01 00`. Original PNGs, exact source variations,
component geometry and hash receipts are retained. LaTeX supplies all crops,
boxes, numbering and semantic descriptions.

The complete vector, scalar, alternate lane-reduction and wide-address mixer
variants each pass 962 public kernel, 65 caller, 7936 private and 15 repeated-call
cases. The private domain includes every exact two-input sum, each possible first
input paired with five fixed boundary values, every eight-lane clipping mask and
512 deterministic random trials. It is not every possible sample pair. A signed
halfword scalar variant additionally passes 962 public and 7936 private cases,
including odd byte addresses supported by the Release 6 target.

`labs/files/pcm-subtract.asm` is the independent failing starter. Its public
`labs/checks/pcm-subtract.kts` suite has 332 cases: all 65 counts under five alias
modes, six boundary cases and the nine-sample caller. Scalar and vector solutions
each pass these and 12704 expanded cases. The expanded domain covers all supported
counts under all alias modes with 32 correlated byte-offset seeds, every exact
difference from -65535 through 65535, and all 256 eight-lane clipping masks. It is
not every input pair or every independent combination of pointer offsets. A
wide-address vector and a signed-halfword scalar variant pass the expanded set.
The starter fails all 332 public cases. Equivalent solutions remain accepted;
behavioral checks do not by themselves establish vector strategy.

Sixteen distinct semantic faults are rejected: eleven mixer faults and five
subtraction faults. A separate written probe verifies the specific counterexample
where resetting the count at the scalar tail loses the first group's four events.
Eight positive written-example cases cover exact intermediate lanes and
zero/seven/eight/nine/63/64 group/tail observations, plus signed scalar recovery.
A separate ninth positive case verifies the in-place group/tail handoff and the
untouched suffix. An initial private subtraction adaptation mistakenly changed
count addition to subtraction; the public check rejected it, the instruction
match was corrected, and the failed evidence remains private. No check was weakened.

The caller audit found missing stack setup in the new PCM callers, the RGB callers
and the maze-neighbor experiment. Five ordinary startup checks now verify sixteen-
byte alignment; complete affected callers and behavioral suites pass again.
Function bodies and data are unchanged by that amendment. The PCM and RGB captures
were refreshed from aligned sources; earlier captures remain historical evidence.
The book's sixteen-byte call convention applies even to these leaf routines.

The separate authoring plugin uses the pinned packaged MARS dependency; installed
JARs are bound to both archives. Each IDE launch ran the real
`clean cleanSandbox runIde` tasks. Completed dedicated instances were terminated
with SIGTERM; this is not a native close/restart persistence test. No native Run
check is claimed for this unit. The authoring suite passes 104 Jupiter tests and
public tooling 391 tests. The existing compatible package validates and builds.

Final main/lab/pilot proofs receive automated PDF/UA-2 validation with exact PDF
hash bindings, plus page and representative grayscale review. Historical evidence
and the original manuscript are preserved, as are unrelated MARS edits. Human
author, learner and assistive-technology review remain open. The native guided
lesson for this unit, later chapters and production instructional-system work
remain unfinished. The next main chapter develops fixed-point filtering.
