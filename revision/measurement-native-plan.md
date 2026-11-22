# Native integration of Chapter 13: measuring programs

The authoritative prose is `main/measuring-programs.tex` and
`labs/measuring-programs.tex`. This is an implementation record for the native
lesson, not a replacement composition or completed IDE verification.

## Teaching sequence

Keep the complete ordinary and blocked matrix products as observation sources.
Use separate complete worked sources for method selection, the positive 2^31
example and the larger cache observation. Preserve the default source so repeated
checks remain understandable. The independent task is matrix-vector reduction;
do not turn the complete worked matrix product into a fill-in-the-blanks exercise.

Predictions should distinguish the default dot products, four-byte inputs versus
eight-byte outputs, zero output dimensions versus a zero reduction width, and
partial sums before the second reduction block. Measurement questions should
identify the caller store excluded by a return boundary, the two native word
notices of a doubleword store, and the distinction between fewer misses and a
claim of lower elapsed time. Explain retained modeled cache state as a second
call in the same session, separate from JVM warmup and new IDE launches.

Debug observations should follow the first ordinary dot product through its
register accumulator and first output store, the complete return before the
caller publishes completed, the blocked product's initial zeroing and the
second reduction block's [25,28,73,82] partial output. The clipped reduction end
is three for default K=3,T=2. Inspect the final return [58,64,139,154] separately
from the publication store. Bind register expectations to actual executed probes;
written values or source inspection alone are not verification.

Use the existing reproducible measurement tool for cold, retained-cache warm,
reset replay and launch scopes. The exact reference instruction and traffic
counts belong to the supplied schedule. Independent solutions must not reproduce
those counts to pass. The native cache view remains part of the student activity;
any automated reset/measurement step needs actual GUI verification, not an
inference from the earlier core-only reset experiment.

## Native grading requirements

Retain all eight original function input families and the complete default caller
from `labs/checks/matrix-vector.kts`. Independently export their actual Kotlin
construction when comparing the new data profiles: same shape, seed, active
windows, bytes, expected sums and preserved state. Repeated native calls must use
distinct public continuations. Preserve the original public script alongside
bounded native data. Add an explicit positive 2^31 example without claiming it
was one of the original eight function inputs.

The original checker records access history, not just final output. Each selected
output byte must be written during the current call before its first partial-sum
read. Native function-profile version 7 is being added upstream to express this
requirement using per-call byte bitmaps. Correct row, reverse reduction, private
stack and zero-then-accumulate schedules should pass. Read-then-repair and
incomplete high-word writes should fail even when old caller bytes hide the fault.

Audit the rest of the original access contract before declaring profile parity.
The original script watches the assembled caller layout from matrix_guard through
completed, including unnamed alignment padding, and permits only selected matrix,
vector, output and descriptor accesses. Existing label-relative individual access
windows and assembled store bounds must not silently omit a read from padding
when a submission changes alignment or object order. Extend the grader if this
requires another missing check. Preserve full-width saved registers, caller stack
bytes, inputs, shape, guards, inactive output, normal exit and both halves of each
signed sum. Independent traversal order and restored private stack frames remain
valid. Keep solutions and extra qualification inputs private.

## Evidence required before marking this unit integrated

Qualify correct alternatives and deliberate faults through both the script and
native routes, including relocated and above-four-GiB inputs. Verify the exact
student archive imports and binds all sources and profiles. Run the real student
controls in a light-theme dedicated IDE after actual clean cleanSandbox runIde;
then edit sources, reopen in a fresh process and re-earn execution credit while
retaining quiz history. Keep authoring automation in the separate plugin.

Integrate the successful student route directly into the native LaTeX lab, retain
unchanged real photographs and native annotations, and rebuild/render/revalidate
the resulting PDF. The older measurement-chapter-01 evidence remains historical;
it does not prove a new native lesson or GUI reset. Human learner and
assistive-technology review remain separate. The proposed MARS-versus-hardware
opening stays deferred.
