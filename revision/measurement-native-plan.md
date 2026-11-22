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
read. Native function-profile version 7 now expresses this requirement upstream using
per-call byte bitmaps (MARS commit 11ecad96). Correct row, reverse reduction, private
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


## Output-initialization checker milestone, 22 November 2026

The new checker passes 206 CLI and 1208 plugin tests and the required plugin
coverage gates. The function-profile parser has 132/132 lines and 145/145 branches
covered. Twelve CLI tests and four parser tests exercise the new assertion and
its versioned data. Five complete matrix-vector implementations pass separate
initialization/product qualification, including reverse reduction, a private
stack frame, output partial sums and addresses above four GiB. Each qualifies
the default product and positive 2^31 across two continuations. Read-then-repair
and incomplete high-word-store faults fail. These seven actual JVM executions
verify this assertion, not the full independent exercise contract or native
lesson. Exact reports and private sources are retained under
`verification/restructuring/native-output-init-01` in the instructor repository.
All 41 unrelated manual MARS edits are preserved. No new screenshot, IDE
walkthrough or PDF validation is claimed for this checker-only milestone.


## Native measurement foundation, 22 November 2026

Function-profile version 8 now represents assembled caller read/write policies
(MARS b07a4af0), including unnamed padding and full crossing notices. It uses the
existing bounded access trace and preserves private stack use outside the watched
caller span. The full checker run passes 217 CLI / 1212 plugin tests and both
required coverage gates; the parser covers 144/144 lines and 150/150 branches.
The exact student archive import test follows that frozen full regression.

The original Kotlin construction independently exports all eight function inputs;
shapes, offsets, matrix/vector words and signed products match the new generator.
The native profile adds direct default input and positive 2^31 and retains the
complete default caller. Ten function cases call twice, followed by one caller
check and a separate normal-exit assessment. The original nine-check script is
unchanged. Four correct algorithms in ordinary/relocated layouts and a wide
variant pass both routes (18 JVM processes). Nine faults and the starter fail
both routes (20 processes), including padding read, restored padding store,
read-then-repair, incomplete high-word store and wrong continuation. These finite
checks do not prove every arbitrary input.

The draft `lessons/022-measuring-programs` supplies nine predictions, nineteen
CLI-measured Debug stops, five complete worked sources/Runs and the independent
exercise. Five actual source probes verify sums, partial output, caller metadata,
full-width positive 2^31 and exact cache launch intervals. Every declared register
binding matches its executed observation and source hash. All 481 book tests pass.
This is not an IDE walkthrough: separate authoring automation, actual IDE/resume,
GUI reset verification, written lab integration and renewed publication checks
remain open. Existing publication evidence is preserved as historical. Human
learner/accessibility review and the deferred opening remain separate.
