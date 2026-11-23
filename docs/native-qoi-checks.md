# Preserving QOI checks in native lessons

The QOI reflection exercise (`030-qoi`, revised Chapter 20) has an existing
205-case behavioral contract. The native lesson delivery must preserve that
contract before adding guided observations. The current work prepares grading;
it does not yet constitute a complete QOI guided lesson or a new publication proof.

`tools/qoi_mirror_lesson_cases.py` authors the finite public case data. It uses
the strict host decoder in `revision_qoi_format.py` to reconstruct expected RGBA,
then reflects pixels within each row and serializes native framebuffer words in
little-endian order. It does not execute assembly or recognize a solution's source.
Run it with an output directory to write the profiles, or add `--check` to compare
an existing generated directory. Publication prose remains directly edited LaTeX.

The original grid contains 197 memory cases, seven invalid-count cases and one
ordinary caller case. Of these, 182 retain a six-million-instruction ceiling and
23 retain a 100,000-instruction ceiling. The profiles are split into 188 batches
under the existing ten-million-instruction and one-MiB JSON limits per profile.
These are allowances, not exact instruction counts or performance targets.

The 197 memory cases retain complete poison-seeded caller arrays and workspace,
full-width preserved registers and the assembled workspace-end stack pointer.
Per-object access policies preserve the original permission test: a data event
that overlaps one of the 13 named caller objects must fit wholly within an
allowed read or write interval. A separate complete-history limit watches the
smallest enclosing assembled span, including intervening padding or workspace.
Watching that span does not grant or prohibit access there. This distinction
matters when equivalent programs arrange their data differently.

The original final comparison outside writable windows is covered by complete
store policies: a write there is rejected even if it stores the same value.
Successful result bytes are checked independently. The seven special invalid-count
cases retain their original invalid input/output pointers and register expectations;
they acquire no additional memory policy absent from the original checks.

The ordinary caller starts with the supplied assembly image's data. It must reach
its public continuation with the expected full-width result and reflected words,
then actually terminate and publish the expected status and packed dimensions.
Stopping at the function return alone does not establish caller correctness.

## Function case format 14

Formats 1–13 retain their existing field and instruction-budget rules. Format 14
inherits format 13 and adds these optional members:

- On a call, `accessHistory` contains required `regions` and optional `maxAccesses`
  (default 65,536). A region is `{"at":{"label":"source","offset":0},"length":20544}`.
  One through 64 positive regions may total at most one MiB. Their resolved
  enclosing span must also contain at most one MiB. The access capacity is one
  through one million notices, shared with the call's policies under the existing
  4,194,304-notice aggregate allowance. Each invocation has a fresh history;
  overflow fails the check rather than accepting truncated evidence.
- On a case, `exit` contains optional signed 32-bit `code` (default zero) and
  optional `memory` in the existing label-relative hexadecimal memory format.
  An empty object requests actual termination with code zero. The continuation
  consumes the same cumulative instruction budget as its calls. Function-specific
  observations finish before caller continuation; post-exit memory is checked
  after termination. Console output must remain empty, and additional file
  operations outside declared read calls are still rejected. Exit-memory bytes
  count toward the existing combined one-MiB setup/result bound.

Format 14 permits up to six million instructions per case. Formats 11–13 retain
three million; formats 1–10 retain one million. The ten-million budget per profile
and 4,096 calls per profile are unchanged. Unknown members and malformed numbers
are errors. The public typed grading API provides `FunctionAccessHistoryCheck`
and `FunctionExitCheck` for these same semantics.

## Native lesson format 10

Format 10 inherits format 9 and raises an assessment's `caseProfiles` maximum
from 32 to 256. Earlier formats retain their previous bounds. The existing
512-case and 4,096-call aggregate limits, duplicate-name rejection, and lesson
archive size limits still apply. This lets one coherent assessment retain a
large original grid without weakening its budgets or forcing students through
artificially split interface steps.

## Native lesson format 11

Format 11 inherits format 10 and permits an optional portable `source` path on
prediction activities. Omit it to use the package entry, as before. Formats 1–10
reject this new member. The referenced file must be present in the retained
archive; malformed, escaping and reserved private paths are rejected before import.
The native lesson uses the existing editor-opening and instruction-highlighting
path for the selected file. This lets a question point at its actual worked
variant rather than at an unrelated instruction in the entry program.

## Verification scope

The original Kotlin checker was instrumented to export its actual names, budgets,
register arguments, all seeded arrays, and resolved permission windows. All 205
case inputs and budgets match; all 197 watched layouts and 204 seeded states
match. The ordinary caller uses its original image data. Native entry staging
sets a return address and workspace-end stack pointer; `main` replaces both
before their first use.

The complete archive is also parsed by a Jupiter test through the production
reader. Separate grading tests cover history overflow, gaps, reordered/wide
objects, per-call reset, mutable-input capture, actual exit codes, post-exit
publication, cumulative budgets, unexpected output and undeclared file operations.
Native and original runtime qualification retains private implementations and
logs in the instructor repository. Guided quiz/Debug delivery, a new QOI desktop
walkthrough and refreshed written-lab figures remain to be completed.
