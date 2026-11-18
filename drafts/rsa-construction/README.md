The current student distribution is `packages/027-rsa-arithmetic`; this draft remains
construction provenance. Use its packaged starter and helper for current student work.

# RSA arithmetic cumulative construction

This draft supports the directly edited native chapter at drafts/rsa-arithmetic.tex.
LaTeX includes remain the publication authority; metadata never regenerates edited prose.
The42-step sequence has35 authored regions,42 quizzes,84 hints and43 exact recipes.
It progresses from Euclid and scalar power through full128-bit limb arithmetic. Complete
implementations and quiz keys remain in the separate private instructor repository.

Replace only named BEGIN/END bodies in main.asm. The unedited source selects the gcd
caller and reports development status three. Successful gcd returns numerical v0 and
v1 zero; its caller records v1 as status. Other entries return status in v0. Framed
development paths restore their full saved state. A nested nonzero status must reach
the caller rather than publish temporary arithmetic as a completed result.

Prepare a recipe with python3 prepare.py s13-1 --source /path/to/your/main.asm
--output /path/to/a-new-observation.asm. Use the cumulative source through that edit.
Preparation changes only the selected data arenas and mode; it never edits authored
regions and never overwrites an existing output. Numerical values are canonical decimal
strings covering the unsigned128-bit range, with narrower scalar entry constraints.
Stop at the recipe's named checkpoint before continuing to its temporary or normal return.
The function_return recipe means the caller's corresponding return label when using
main rather than a direct function call. The completed interface may use another valid
algorithm; strict intermediate state belongs only to this guided construction.

The four capture plans are numerical specifications, not screenshots. Actual automated
light-theme IDE capture, separate student/instructor exports and remote progression
remain pending. Instructors will select progression and hint policy through the shared
exercise protocol; this draft does not implement a remote enforcement service. The
substantial author-requested prose rewrite remains deferred until all35 drafts exist.
