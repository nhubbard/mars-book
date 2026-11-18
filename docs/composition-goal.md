# Full chapter composition goal

Author the complete instructional text for the increasingly difficult projects in the
35-project roadmap. The reader understands basic programming and is learning assembly
and machine representation. A chapter must contain its explanations and worked reasoning;
a sequence of tasks or a table of proposed sections is not a completed composition.

## Chapter acceptance

A complete draft introduces the problem, explains the relevant machine model, develops
the program in teachable increments, and connects the increments with substantive prose.
It includes concrete traces, worked examples, semantic teaching figures or tables,
comprehension questions, practice variations, and a clear account of the tested contract.
A reader should be able to explain why the demonstrated instructions produce the result.

Supply an opening project, a verified reference and equivalent alternative, meaningful
public and private checks, and instructor explanations. Keep the reference and private
checks in the instructor repository. Separate checks for a particular illustration from
behavioral grading of equivalent submissions. Add tested grader operations upstream when
an essential contract cannot be checked. State what remains a human or UI assessment.

Screenshots must come from verified light-mode IDE captures performed by the separate
authoring plugin. Keep capture plans explicit while implementation is pending. Do not
call a chapter illustrated or print-ready because its capture recipes exist.

## Composition after MacTeX installation

MacTeX is installed and the first nine chapters have been migrated once to ordinary
LaTeX. Use input/include directly for chapters, sections, worked examples, and teaching
fixtures. main.tex and includes/ are the editable publication authority. The old
JSON/Markdown book assembler has been retired from the publication path. The final book must not depend on rendering a JSON-defined document
model or on round-tripping edited LaTeX through Markdown.

Machine-readable task metadata may still serve the trainer and grading distribution.
It must not constrain the order, layout, prose, or inclusion structure of the book.
Migrate source labels and authorship records without overwriting human contributions.
Use native LaTeX for diagrams, tables, math, code, captions, and screenshot annotations.
Validate accessibility in the actual compiled PDF with the selected template.

## Working order

Complete chapters in prerequisite order, beginning with console state, memory-backed
pixels, sprite rows, and integer representation. Preserve the full roadmap and document
any needed prerequisite lessons before changing its progression. Prefer completing one
substantive, verified composition to producing many skeletal chapter packages. Review
instruction semantics and the explanatory text together before committing each increment.

The console, single-pixel, eight-byte-sprite, bit-inspector, seven-segment-counter,
MarsBot-square, Bresenham-line, Life, sorting-visualizer, maze-explorer, matrix-transpose,
fixed-point ball, binary packet parser, ring-buffer logger, UTF-8 scanner, clipped sprite blitter, and expression evaluator chapters are composed text drafts with verified companion programs.
They still need author review and actual IDE figures. The remaining projects stay
planned until their text and companions are written and checked. The next uncomposed project is the Q15 FIR filter.

Matrix transpose and fixed-point ball are composed through ordinary includes in the
main manuscript. Their independent entries, `drafts/matrix-transpose.tex` and
`drafts/fixed-point-ball.tex`, support focused editorial and layout review. The ball
chapter has thirty microsteps, sixteen cumulative code increments, public animation
and kernel checks, independent private oracles, and four planned light-theme figures.
Student and instructor distributions pin the same public version and archive digest.

Binary packet parser is composed in native includes and has an independent entry,
`drafts/binary-packet-parser.tex`. Its 24 steps include 15 cumulative code increments,
explicit error priority, byte order and checksum reasoning, a native input-window
diagram, and measured instruction/read comparisons. Its grader adds native read-width
observations to detect logical overreads into mapped padding. Three actual IDE figures
remain planned. The full roadmap remains the goal; eighteen text drafts do not complete it.

Ring-buffer logger has a complete native text draft and an independent entry,
`drafts/ring-buffer-logger.tex`. Thirty steps and eighteen cumulative code increments
develop separate push/pop contracts, an explicit occupied-byte invariant, arbitrary
bounded capacities, independent cursor wraps, zero-length records, exact fits, and
short-destination retries. A native slot diagram, complete command trace, measured
copy comparison, public/private grading and separate distributions accompany it.
Three actual IDE captures remain planned.

UTF-8 scanner is a full native composition with an independent entry,
`drafts/utf8-scanner.tex`. Thirty-four steps and twenty-two cumulative increments
develop scalar counting, prefix bounds across chunks, explicit finality, separate
start/detection positions, and terminal-state preservation. The programs are checked
against all scalar encodings and all two-byte inputs, with additional transition,
split, wide-address, and equivalent implementation tests. Four actual IDE figures
remain planned. The complete 35-project composition and capture goal remains active.

Clipped sprite blitter has a full native draft at `drafts/clipped-sprite-blitter.tex`.
Twenty-five steps and sixteen cumulative increments develop half-open intersection,
signed coordinate width, independent row strides, full-word key equality, no-store
transparency and full caller preservation. Native clipping/stride/count fixtures,
measured preclipped/per-cell work, public checks and private geometry checks accompany
separate student/instructor archives. Four actual light-theme captures remain planned.

Expression evaluator is a full native text draft at `drafts/expression-evaluator.tex`.
Forty-five steps and thirty-four cumulative edits develop bounded lexical reads,
checked arithmetic, literals, recursive groups/signs and left-associated precedence
loops. All edits pass forty-six intermediate observations and reconstruct the tested
reference exactly. The public package supports guided and free-writing modes; private
checks accept a precedence-climbing alternative and submission-owned constant reads.
Repeated calls verify fresh parser state in one session. The 76 grading runs record
23,887 positive case executions, including repeated variants and observations.
Nineteen faulty variants and the unfinished starter are rejected. Four actual IDE
captures, human review and independent accessibility validation remain pending. The
full 35-project book and capture objective remains active.

Tiny bytecode interpreter is a full native text draft at
`drafts/tiny-bytecode-interpreter.tex`, included in main.tex as chapter eighteen.
Fifty-one steps and forty cumulative assembly edits develop thirteen variable-length
opcodes, whole-image and target-validation priority, checked arithmetic, stack bounds,
precise retirement and fresh-call behavior. Six provided frame/exit excerpts, a native
instruction-boundary diagram, complete countdown prefixes, measured composite
implementations and guided/free-writing packages accompany the prose. All forty partial
programs have fifty-eight executable observations. Four actual light-theme captures,
human review and independent accessibility validation remain pending. Eighteen text
packages do not complete the full 35-project and capture objective, which remains active.

RGB888/RGB565 is a full native text draft at `drafts/rgb-formats.tex`, included in
main.tex as chapter nineteen. Forty-nine steps and thirty-nine cumulative assembly
edits develop unaligned scalar conversion first, then bounded MSA groups and scalar
tails. Three native diagrams, exact quantization/expansion formulas, error bounds,
selector derivations and complete-call measurements accompany it. Forty-seven
intermediate observations check scalar values and vector lanes both in isolated calls
and through the student caller, using published reproducible input recipes. Final grading also
accepts an equivalent scalar implementation. Four actual light-theme captures,
author review and independent accessibility validation remain pending. Nineteen
text drafts do not complete the full 35-project and capture objective, which remains active.

PCM mixing is a full native text draft at `drafts/pcm-mixing.tex`, included in main.tex
as chapter twenty. Forty steps and thirty cumulative edits develop signed decoding,
exact sums, strict saturation bounds, byte stores, exact in-place aliases, eight-lane
saturation, clipping masks and doubleword populations. Forty-eight reproducible
observations are checked in unaligned calls and through the actual program caller;
the completed scalar stage and final MSA stage share the behavioral contract.
Public per-step construction checks, final checks, twelve listening files and a
reproduction renderer accompany separate student/instructor distributions. Three native
diagrams and four verified numerical capture models support the text. Actual IDE
captures, interactive progression controls, author review and independent accessibility
validation remain pending. Twenty text packages do not complete the full 35-project
composition and capture objective, which remains active.

Fixed-point FIR is in progress at `drafts/fixed-point-fir.tex`. Its native foundation
specifies eight oldest-first Q15 coefficients, seven supplied history samples, exact
64-bit accumulation, one nearest rounding with ties toward positive infinity, and
post-round saturation. It explains widening before dot products, asymmetric impulse
response, explicit chunk history and complete-call measurements. Six simulator-derived
listening files demonstrate exact cancellation of a supplied period-four component.
Both implementations and every chunk cut are checked. Full incremental edits, student
packaging and actual IDE captures remain unfinished. Chapter twenty-one is outside
main.tex and the twenty completed text packages; the full goal remains active.
