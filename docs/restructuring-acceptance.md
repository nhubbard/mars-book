# Restructuring acceptance ledger

Updated: 23 November 2026. The restructuring goal remains active. This ledger
tracks the agreed products, rather than treating chapter counts or successful
builds as completion. Historical unit notes retain the exact scope and editions
of their technical evidence. A file or receipt's existence does not establish
that its contents were rechecked by this audit.

## Publication and learning sequence

| Requirement | Current evidence | Remaining acceptance work |
| --- | --- | --- |
| Preserve the original draft and tested baseline | Original `main.tex`, `includes/`, original packages, baseline records and historical verification are retained; revised publications use separate native include trees | Preserve those identifiers as new editions are produced |
| All 35 original subjects in a coherent progression | `revision/project-locations.json` identifies 36 placements, 27 paired chapters and ten companions; `tools/audit_curriculum.py` checks actual include ownership | Author's complete independent walkthrough and prerequisite review remain open |
| Complete smaller programs in the main book | Console, pixel, loop, function and array examples have complete public sources; individual unit notes identify executable checks | Confirm during walkthrough that every partial listing's purpose and complete source are clear |
| Explicit outcomes and prerequisites | `docs/revised-learning-outcomes.md` maps three assessable outcomes per chapter to activities and expected entry concepts | Human review of the map and the explanations at first use |
| Connected explanations, worked reasoning and independent transfer | Native chapter/lab pairs and the reviewed first-program, PCM and H.263 pilots; author accepted the direction for expansion | Substantive editing is a separate author-directed goal; no claim of completed learner validation |
| Written lab supplement supports the interactive lab | Each main chapter has a corresponding written supplement; ten companions retain contracts, diagnosis, public checks and independent tasks | Finish native delivery for seven companions below, then review names, paths and observations across all editions |
| Contents, glossary and index in each book | New native front matter, shared glossary and separately referenced chapter-oriented subject indexes | Technical navigation checks pass: literal references resolve, changed pages are visually inspected, final PDFs pass automated PDF/UA-2. A topic entry points to the relevant chapter, not a claimed first-use paragraph |
| Native composition, fonts and highlighting | Directly edited TeX includes; Minion Pro, Noto Sans Math and IntelliJ-derived minted style | Current proofs compile with embedded Minion Pro, Noto Sans Math and JetBrains Mono; native composition checks pass. Preserve direct prose editing in future changes |
| Actual figures and accessible annotations | Unit receipts bind real light-theme IDE images, source/state and native TeX annotations | Recapture changed demonstrations; renew automated PDF/UA-2 on final bytes; human reading-order and assistive-technology review remain open |
| Appropriate physical volumes | Main learning book and separate written laboratory companion | Choose any further physical division from the finished sequence; no page ceiling |

## Native student instructional system

Implementation lives in MARS. Capture and publication automation lives in the
separate authoring plugin, which declares a MARS dependency. Exact API-verifier,
test, archive and IDE results belong to the private verification repository;
they are not student answer keys.

| Requirement | Existing verification to inspect in the final integration audit | Remaining acceptance work |
| --- | --- | --- |
| Inline predictions with editor context | `native-predictions-*`, `native-debug-*` and per-unit IDE evidence | Course-wide review of questions and pointers; retain the actual inline interaction instead of requiring Find Action |
| Source-bound Run/Debug observations | `native-execution-01`, `native-reopen-01` and per-unit IDE evidence | Confirm every current package against its tested archive and source revision |
| Resumable progress, edits, retries and invalidation | `native-lesson-runtime-01`, `native-reopen-01`, per-unit fresh-process resume receipts | Audit current-edition distribution bindings and retry semantics |
| Guided and independent entry; equivalent solutions | `native-modes-01`, per-unit references, alternatives and deliberately failing fixtures | Complete seven companions; retain interface/state checks instead of reference-source matching |
| Graduated hints and instructor control | `native-hints-01`, `native-policy-01`, `downloaded-prerequisites-01`, `functions-prerequisites-01`, `native-fir-call-policy-01`, `native-matvec-policy-01` | Verify current package policy bindings and explain permitted direct entry without weakening checks |
| Versioned local/HTTPS delivery | `lesson-import-01`, `lesson-download-01`, `downloaded-navigation-01` and `downloaded-prerequisites-01` | Final public archive and download guidance audit; no hosted grading or LMS claim |
| Kotlin, public IntelliJ APIs and meaningful Jupiter coverage | MARS source, test reports and pinned API-verifier receipts in individual implementation steps | Recheck after any new production change; preserve unrelated manual edits |
| Student/instructor separation | Explicit native export lists and exact student ZIP digests; private reference/check distributions | Renew distribution scan after remaining companions, bind instructor editions to current student digests |

These are evidence locations, not a declaration that all their requirements have
passed a renewed final audit. The final audit must record the inspected receipts,
commits, archive digests and limitations.

## Remaining native companion packages

These seven subjects already have written labs, public executable starters/checks
and private qualification evidence. Ordinary Run/Debug availability does not
complete the promised native guided delivery.

| Original subject | Existing written location | Required integration |
| --- | --- | --- |
| 006 MarsBot | `revision/labs/companions/marsbot.tex` | Observe actual movement and trail; grade the stated device contract, including forbidden coordinate-feedback writes |
| 010 Maze | `revision/labs/companions/maze.tex` | Guided state/invariant observations and independent path/search checks |
| 014 Ring buffer | `revision/labs/companions/ring-buffer.tex` | Wrap/full/empty reasoning and independent state-transition checks |
| 015 UTF-8 | `revision/labs/companions/utf8.tex` | Streaming boundaries, invalid sequences and independent decoder checks |
| 016 Clipped blitter | `revision/labs/companions/clipped-sprite.tex` | Clipped address geometry and independent bounded writes |
| 017 Expression evaluator | `revision/labs/companions/expression.tex` | Parsing/evaluation state and independent syntax/overflow/error checks |
| 018 Bytecode interpreter | `revision/labs/companions/bytecode.tex` | Instruction decoding, bounded machine state and independent fault checks |

Verify the actual source paths and contracts before authoring each package. If a
required observation cannot be expressed by the existing grader, add and test the
missing MARS checking function. Do not weaken a trail, access or failure contract
to an easier final-value check.

## Review ownership and limits

The implementation agent owns the unfinished native companions, technical
integration audit, distribution checks and publication verification. The author
owns the complete unaided walkthrough, acceptance of prose and physical volume
decisions. Representative learner review and human accessibility review require
available people; they remain explicitly open rather than being inferred from
automated checks. The author has separated substantive editing from this goal.

The fuller opening explanation comparing MARS with hardware remains deferred at
the author's request. A short explanation of how to use these books does not
constitute that introduction. Simulator performance work, unrelated conformance,
LMS/rosters and hosted grading are separate work.

## Navigation verification milestone

The current learning proof has 201 PDF pages, including 189 chapter pages; the
lab proof has 241, including 228 chapter/companion pages. Both retain 27 chapters.
The structural audit reports 35 original subjects, 36 placements and ten
companions, with no duplicate labels or unresolved literal references. Public
tooling passes 628 tests. The corrected H.263 student archive passes two native
plugin import/parsing tests; its assignment text is the only changed file among
31 exports. Existing source, lesson and grading files are byte-identical. Prior
IDE receipts retain their original archive hashes, rather than being relabeled
as tests of the corrected archive.

Final automated PDF/UA-2 passes 1,728 rules in each book, with 1,148,311 checks
for the main book and 1,154,721 for the lab book, with zero failures. These are
technical publication checks, not the open human reviews above. The seven native
companions and course-wide distribution/evidence audit remain implementation work.
