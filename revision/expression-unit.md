# Expression-evaluator companion

Section 21.6 revises original project `017-expression-evaluator` after the CBOR lab.
It occupies lab pages 128–133 in the 133-page proof. The main proof remains unchanged
at 139 pages. The directly edited native companion has five conceptual subsections,
three native concept figures and three actual annotated Debug observations.

## Prerequisites and outcomes

The reader needs bounded byte access, signed-word representation, function frames
and the nested-data chapter's retained parent state. The companion develops five
observable outcomes:

- Explain the grammar's precedence, left association and recursively nested unary signs.
  The worked expression and cursor trace distinguish completed values from pending work.
- Design cursor, error and frame ownership across nested calls without restoring away
  child progress. Separate the public saved-register convention from private shared state.
- Compute a wide candidate and prove signed-word representability. A complete independent
  arithmetic example makes that operation runnable before the student writes a parser.
- Preserve procedural first-failure identity and byte offsets, including late syntax,
  incomplete groups, literal overflow, intermediate overflow and zero division.
- Implement the complete evaluator independently and test bounds, caller preservation,
  equivalent parser organization and a fresh call after success or failure.

## Public work and qualified implementations

`labs/files/expression-evaluator.asm` is a deliberately unfinished independent skeleton:
only the public eval_expr entry needs implementing; no illustrated helper names or
frame layout are imposed. Its common caller and data labels match the original package
checks. It stops with break until implemented; it is not claimed to be an exiting solution.
Both supplied callers establish sixteen-byte stack alignment before their first call.

`labs/files/expression-arithmetic.asm` is a complete checked-arithmetic example, not a
complete parser. Its checked_binary entry assumes two signed-word operands and one of
ASCII +, -, * or /. It returns the same status/value-or-offset shape used by the language.
The caller computes 3*(-2), then 18 minus that result. The public
`labs/checks/expression-arithmetic.kts` checks eight boundary values crossed with eight
values and four operators, plus the actual two-call caller: 257 cases. Arbitrary precision
supplies arithmetic expectations; saved state and absence of caller-data access are checked.
Unknown operators and operands outside signed-word range are outside this small helper's
preconditions, rather than newly accepted expression-language inputs.

Private recursive-descent, precedence-climbing and above-32-bit-data implementations
each pass 540 public kernel cases, 20 caller cases, 10230 private grammar cases and
11 same-session sequence cases: 32403 positive grading-case executions. Each sequence
case contains eleven calls; case counts are not unique inputs or individual call counts.
An additional sixty caller cases verify the aligned copies used in the new illustrations.
The executable function bodies are unchanged by that caller-only alignment adjustment.

The private grammar domain includes all 7381 strings of length zero through four over
`01+-*/() `, 1024 byte substitutions, 400 boundary operand pairs, fourteen targeted
error-order inputs, 128 unary-depth and 64 group-depth inputs, 1200 independently reduced
operator chains, and nineteen in-bound whitespace expansions. These are finite domains,
not all 64-byte strings or all signed-word operands. Wide data is exercised; high text
and high stack addresses outside the pinned Default map are not positive coverage claims.
Full saved-register canaries and alternate mapped return continuations remain checked.

Nineteen assembled semantic faults fail the public kernel feedback, covering wrapped
arithmetic, unsigned division, skipped checks, wrong diagnostic offsets, precedence,
whitespace/NUL handling, writes/overreads, truncated saved state and pointers, and frame
balance. The unfinished skeleton's 540 failures are separate. Twelve written cases check
specific numerical results, diagnostic offsets and aligned caller boundaries. Full parser
implementations, alternative organization, expanded cases and faults remain private.

## Native figures and publication evidence

The separate authoring plugin captured three actual IntelliJ Light Debug observations
in one completed batch after actual clean, cleanSandbox and runIde tasks. The public
arithmetic example shows product -6, status zero and result 24. The two completed-parser
observations use the same caller/data interface as the student's skeleton: overflow at
offset ten for 2147483647+1?, and missing-close syntax at the six-byte input end for
1/(2-2. These are completed-solution boundary observations, not claims that the empty
student skeleton produces them. Students can reproduce them after implementation;
private helper layouts are never required. CLI probes independently check the exact rows.

Screenshot pixels are unchanged; titles, crops, outlines and semantic descriptions are
native LaTeX. One actual IntelliJ highlight export extends 824 unchanged records to 825.
The authoring plugin passes 104 Jupiter tests; public tooling passes 405 tests. The original
45-step package validates and exports under its unchanged contract. Final pages were
reviewed in color and representative code, table and screenshot pages in grayscale.
The exact final lab proof passes automated PDF/UA-2 validation. These checks do not
establish author, learner or assistive-technology usability review, all of which remain open.

The original manuscript and main/advanced review proofs remain unchanged. No MARS source
change was required. The native guided lesson remains unimplemented for this companion.
The next companion develops original project 018's bytecode interpreter; the next main
chapter remains word operations through ChaCha20. The full curriculum and production
instructional-system goal remains active. The opening orientation remains deferred.
