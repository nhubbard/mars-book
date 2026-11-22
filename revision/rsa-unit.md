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

## Remaining scope

Author, representative learner and assistive-technology reviews remain pending.
The advanced native guided lesson, delivered progression and enforced instructor
hint/policy support are not complete for this companion. The broader goal remains
active. WAV, PNG and the retained lossless/H.263 video sequence follow. The deferred
opening explanation of MARS versus hardware has not been added. All forty-one
pre-existing MARS edits remain preserved; this unit adds no MARS source changes.
