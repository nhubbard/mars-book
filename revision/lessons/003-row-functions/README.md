# Functions and saved state: development edition

This fifth revised native unit extracts byte operations from original project
003. It does not replace that project's later sprite work. The complete
reverse-rows.asm matches the main chapter's worked program. The independent
starter adds a second ordinary caller so different incoming continuations can
be exercised without depending on a private label.

The archive uses native lesson format 6: five inline predictions, eight ordered
Debug observations, an ordinary silent Run and a function assessment. The call
boundaries and expected registers were observed through actual CLI execution;
result words, inputs and guard were also checked. MARS's function reader and
existing native assessment path passed the full plugin regression and required
coverage checks. Actual on-screen walkthrough and chapter-five publication
figures remain pending. The current LaTeX main chapter and written lab remain
the authority for the exposition; they are not generated from this JSON.

The public function profile and equivalent standalone grading script cover all
256 low bytes with three upper-bit patterns, different stack positions and
full-width saved-register seeds. Return sites alternate between after_invert
and after_repeat. Each case has 256 calls in one machine and a cumulative
100000-instruction budget; cases use separate machines. Both private saved-link
and tail-transfer implementations pass all 768 calls. The starter and six
assembled result/preservation/helper/continuation faults are rejected. Checks
compare caller bytes after returns rather than claiming a full write history.
Run tools/function_lesson_cases.py to regenerate or check this finite grid.

Private complete implementations and checking variations remain in the instructor
repository. This is an importable development package, not a claim of a completed
native IDE walkthrough or representative learner review. The main book and written
lab retain native LaTeX publication authority. The broader curriculum is unfinished.
