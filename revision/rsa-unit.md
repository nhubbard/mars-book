# RSA-arithmetic companion to word transformations

Section 22.8 revises original project `027-rsa-arithmetic`, on lab pages 154–161
in the 161-page proof. The main-book proof remains unchanged at 145 pages.
The directly edited native companion has eight conceptual subsections, three
native arithmetic tables, two short instruction excerpts and four actual annotated
Debug observations. Original manuscript sources and earlier proofs are preserved.

## Teaching sequence and independent work

The explanation derives the small inverse-exponent pair through Euclid and states
why Euler's theorem recovers the example message. It then traces least-significant-
bit-first modular power before extending arithmetic to unsigned limbs. The radix
has its own symbol, beta, distinct from the second operand. The schoolbook product
has an explicit doubleword bound; modular addition retains a 129th carry; reduction
uses a bit-prefix invariant; and modular product and power compose those helpers.
Full exponent shifts, nested-call ownership, exact aliases and error precedence
are explained before the final independent test plan.

`labs/files/rsa-arithmetic.asm` supplies only the caller, arenas, private stack area
and seven intentional break entries. The learner implements every arithmetic
interface. Complete implementations, alternatives and private checks stay in the
instructor repository. Final acceptance does not require private labels, frames,
working registers, exact instruction counts or one exponent scan direction.
The original package retains its separate cumulative 42-step construction route.

The existing preparation command is executed against the independent skeleton:
it selects the declared observation while preserving instruction text, refuses an
existing output and leaves both source and existing output unchanged on refusal.
The prose names the required default mode and distinguishes the scalar caller's
right operand from the multiword caller's exponent object. Both reverse-power
caller arrangements are independently executed and checked.

## Public feedback improvement

A targeted fault initially passed the old 21-case word suite: only the accumulator
remainder used signed arithmetic, while base squaring retained unsigned arithmetic.
The new public case uses base 4294907291, exponent three and modulus 4294967291.
It expects 2494998972, while that fault stores 2494998952. A modulus of 2^32 minus
one can hide a signed-remainder difference after a low-word store, so the final
case uses 2^32 minus five. Exact before/after runs retain the same faulty candidate:
21 old cases pass, and the extended suite rejects it in the new twenty-second case.
Reference, equivalent alternative and above-32-bit-data variants pass all 22 cases.
The change uses the existing BigInteger result and ownership checks; no missing
MARS checker API or new core code was needed.

## Finite qualification

Each complete variant passes eight gcd, 22 word-power, 13 product, 22 addition,
17 reduction, 14 modular-product, 11 multiword-power, twelve caller, four reuse
and 312 private cases: 435 per variant, or 1305 positive grading-case executions.
These are executions, not distinct programs or function-call counts. The independent
skeleton fails all eight gcd cases as intended.

Fifteen assembled semantic faults are rejected by the completed public suites,
with 107 matching positive reference cases at wide data addresses. They cover
signed remainders, sign-extended limbs, narrow products, omitted carries or borrows,
old product contents, lost exponent/multiplier bits, narrowed pointers, damaged
preserved high bits, invalid output access, the modulus-one identity and wrong
reduction traversal. A separate wide-code alternative passes eight gcd, twelve
caller and four reuse cases with both code and data above 4 GiB. Its deliberately
narrowed return address fails all eight matching gcd cases. Thus sixteen distinct
assembled faults are rejected; the return-address fault has its own eight-case
positive baseline rather than borrowing a low-code observation.

Ten exact written observations check the gcd return convention, unsigned scalar
product, reduced carry result, prefix reduction, modular product, cross-half
exponent shift, top exponent bit, both reverse-power callers and modulus-one
identity. Four further CLI probes bind the native capture boundaries. Python's
arbitrary-precision arithmetic checks every printed table, the inverse certificate,
Euler identity, full products, carry example and high-exponent answers.

## Publication evidence

The separate authoring plugin passes 104 Jupiter cases, including the actual
highlighter export. Two new exports extend the prior 825 records to 827 without
changing those earlier records. `book/highlighting/rsa-excerpts.json` retains the
exact new batch and its current IDE/plugin provenance; the combined manifest
retains its historical root metadata. All 419 public tooling tests pass.

Four actual IntelliJ Light Debug captures are produced after actual
`clean cleanSandbox runIde` tasks. Published PNG bytes match their source receipts.
Native LaTeX supplies crops, outlines, captions and alternative text. The carry
figure explicitly shows a provisional sum; its separately checked high carry is
not falsely described as visible. Likewise, failure status and access ownership
are distinguished from unchanged pictured bytes. The dedicated IDE is closed after
the completed batch. Authoring automation stays outside MARS.

All eight new pages receive color review; pages 156–160 receive grayscale review.
The final proof retains embedded Minion Pro, Noto Sans Math and JetBrains Mono,
with exact automated PDF/UA-2 reports archived privately. Earlier validated proof
and report remain paired. These checks do not establish author approval, learner
independence, assistive-technology usability, production cryptography or hardware
performance. No elapsed-time, cycle or constant-time result is claimed.

## Native assessment foundation (23 November 2026)

The native host model in `tools/rsa_lesson_cases.py` retains all 123 original
public cases. Eighteen profiles contain 142 native invocations: 130 observed
arithmetic calls plus twelve caller-dispatch segments. Results use Python's
arbitrary-precision integers; the profiles contain no assembly solutions.

The original assertions were executed with observation logging against the
reference, alternative and wide-address implementations. Every native fixture,
selected read/write interval, result, preserved value and instruction budget
matches those runs. Power cases retain their 20-million-instruction allowance;
the three-call repeated-power case retains one 40-million-instruction budget.
No repeated-call case is split into fresh sessions.

A new generic caller-object check represents the original ownership rule with
one bounded enclosing history. Caller objects may be reordered or relocated;
private gaps and workspace remain available. Forbidden caller accesses are
rejected even when a store restores the previous value. This foundation is
assessment infrastructure, not a completed interactive RSA teaching sequence.
The private verification directory is `native-rsa-foundation-01`.

## Native guided package draft (23 November 2026)

`revision/lessons/027-rsa-arithmetic` now contains a 40-file native student
package. Its 35 activities comprise sixteen inline predictions, eight Debug
activities with 26 stopped observations, nine staged assessment groups, one Run
and a final complete assessment. The staged groups retain every original case;
the final group repeats them on the completed independent source. These are 246
case placements declaring 284 invocations, including caller-dispatch segments.

Eight public worked programs isolate unsigned remainders, product width, one
addition column, a carry decision, four prefix reductions, two multiplier-bit
contributions, a cross-half exponent shift and modulus validation. They supply
no complete `rsa_*` implementation. All required observations use unchanged
worked sources. Implementation work continues in one independent file, avoiding
mode edits that would invalidate its checks merely to complete a required Debug
activity.

Every declared stopped register value and normal termination is checked by actual
core execution. The validation probe also observes exactly four modulus reads
before the helper returns, with the output still untouched. The production package
importer accepts the archive, and Jupiter tests resolve every prediction and Debug
anchor through native IntelliJ PSI. All 581 public tooling tests pass. This is
headless execution and import evidence, not an IDE walkthrough or learner review.
The separate authoring plugin still needs an RSA first/resume route and actual
IDE trials before publication integration.

## Actual IDE and publication integration (23 November 2026)

The separate authoring plugin now drives the complete native RSA package through
production MARS controls. Two fresh IntelliJ processes exercise inline questions,
eight Debug activities, staged checks, normal Run and the final complete recheck.
The first records 53 observations and 58 photographs; resume records 37 observations
and 38 photographs. All eleven installed JARs match their pinned archives in both
processes. There are no ERROR or SEVERE entries in either accepted IDE log.

Seventeen quiz attempts, including one deliberate wrong answer, survive unchanged.
Edits to nine source files stale all nineteen execution checks; the second process
re-earns them on the saved edited sources. Each assessment's attempt count increases
once on resume, and source digests agree across the process boundary. Both projects
save, close and dispose before the scoped authoring helper terminates the owned IDE;
the resulting runIde child exit 143 is intentional, not a naturally successful
Gradle completion.

The native companion now connects its reasoning to the eight public worked
experiments, continuing independent source, inline predictions and final recheck.
Standalone preparation and CLI checks are explicitly optional. One new light-theme
photograph shows the first Euclidean remainder, selected frame and visible registers.
Native LaTeX supplies callouts, crops, alternative text and the explanation of the
current pause versus next lesson target. The four older arithmetic figures remain.
The new capture precedes private fixture injection, and six publication-guard tests
reject changed source, state, layout, geometry and post-injection provenance.

The accepted 219-page lab proof retains embedded Minion Pro, Noto Sans Math and
JetBrains Mono. Pages 185–196 receive visual review, including the transition into
WAV; the new figure receives grayscale review. The exact accepted PDF passes
PDF/UA-2: 1,728 rules, 1,034,219 checks and zero failures. The previous proof/report
remain paired privately. All 581 public tooling tests and 138 separate authoring
Jupiter tests pass. Exact plugin API reports retain the expected experimental
public API uses and no compatibility or internal-API release problem. Evidence is
private in `native-rsa-ide-01`.

## Remaining scope

Author, representative learner and assistive-technology reviews remain pending.
The RSA student sequence now has actual first/resume IDE evidence and native
publication integration. RSA-specific instructor-policy UI trials are not claimed
by these default-policy walkthroughs. The shared instructional system retains its
separately verified progression, hint and policy facilities. The broader goal remains
active. WAV, PNG and the retained lossless/H.263 video sequence follow. The deferred
opening explanation of MARS versus hardware has not been added. All forty-one
pre-existing MARS edits remain preserved. The historical written companion added
no MARS source changes; the native assessment foundation extends the shared grader.
