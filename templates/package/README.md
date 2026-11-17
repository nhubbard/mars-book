# Author a project package

Copy the first package structure, assign a new stable ID, and remove its example-specific
text before drafting. Use this checklist as the package template until a second project
establishes which fields truly repeat.

1. Choose one concrete result from docs/roadmap.md and list prerequisite concepts.
2. Specify inputs, output or artifact, termination, side effects, and resource bounds.
   Define normal cases, boundaries, and invalid-input policy before implementation.
3. Write and verify a private reference implementation. Add an independently structured
   correct alternative or compare against a C/Kotlin reference for bounded kernels.
4. Make a minimal student project that opens without unnecessary infrastructure. Supply
   scaffolding only for concepts outside the lesson. Keep partial execution safe.
5. Divide the work into edits and observations small enough to explain a single state
   change. For each, write meaning, action, expected state, a quiz, and staged hints.
6. Keep syntax examples precise. State whether an operation is basic or a pseudo, the
   width of operands and results, and relevant traps or control-flow constraints.
7. Write public feedback cases and private cases for the same published contract.
   Separate illustration-specific checkpoints from equivalent-solution grading.
8. If a meaningful assertion is missing, add it to the MARS grader with tests and pin
   the minimum revision that provides it. Do not simulate the machine in book tooling.
9. Define figures as reproducible state recipes with light theme, captions, alt text,
   assertions, and semantic annotations. Generate figures from actual IDE captures.
10. Write connective explanations between edits. Add semantic fixtures and a reading
    sequence; permit author passages at any segment boundary. Record authorship and review.
11. Validate, run checks, generate the book and both distributions, and inspect archives.
12. Trial the exercise with a reader from the intended audience before finalizing it.

A small debugging fixture and a larger artifact-producing fixture should accompany
later projects where the full workload is too slow to step. Accuracy checks must
precede performance comparisons and use the same inputs and expected outputs.
