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
| Written lab supplement supports the interactive lab | Each main chapter has a corresponding written supplement; ten companions retain contracts, diagnosis, public checks and independent tasks | Finish native delivery for two companions below, then review names, paths and observations across all editions |
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
| Guided and independent entry; equivalent solutions | `native-modes-01`, per-unit references, alternatives and deliberately failing fixtures | Complete two companions; retain interface/state checks instead of reference-source matching |
| Graduated hints and instructor control | `native-hints-01`, `native-policy-01`, `downloaded-prerequisites-01`, `functions-prerequisites-01`, `native-fir-call-policy-01`, `native-matvec-policy-01` | Verify current package policy bindings and explain permitted direct entry without weakening checks |
| Versioned local/HTTPS delivery | `lesson-import-01`, `lesson-download-01`, `downloaded-navigation-01` and `downloaded-prerequisites-01` | Final public archive and download guidance audit; no hosted grading or LMS claim |
| Kotlin, public IntelliJ APIs and meaningful Jupiter coverage | MARS source, test reports and pinned API-verifier receipts in individual implementation steps | Recheck after any new production change; preserve unrelated manual edits |
| Student/instructor separation | Explicit native export lists and exact student ZIP digests; private reference/check distributions | Renew distribution scan after remaining companions, bind instructor editions to current student digests |

These are evidence locations, not a declaration that all their requirements have
passed a renewed final audit. The final audit must record the inspected receipts,
commits, archive digests and limitations.

## Remaining native companion packages

These two subjects already have written labs, public executable starters/checks
and private qualification evidence. Ordinary Run/Debug availability does not
complete the promised native guided delivery.

| Original subject | Existing written location | Required integration |
| --- | --- | --- |
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
technical publication checks, not the open human reviews above. At that milestone, seven native
companions and the course-wide distribution/evidence audit remained implementation work.

## Earlier MarsBot assessment and native draft milestone

Console-case format 5 now exposes actual MarsBot state and complete ordered
trails without changing the simulator. Three equivalent private implementations
each pass all 7,623 documented side/rate pairs. Twelve negative processes fail
their distinguishing profiles, including restored forbidden writes and a delay
calibrated to the default request. Full implementation regression passes 298 CLI
and 1,336 plugin tests with full required line/branch and parser coverage; two
additional generated-profile tests pass separately.

The new `006-marsbot-feedback` draft adds a thirtieth downloadable native package.
Its seven predictions, two three-stop Debug sequences and two assessments pass
actual archive/import and all fourteen PSI source-pointer checks (two more plugin
tests). Public tooling passes 636 tests. At that milestone the draft was not yet a completed native
IDE delivery: first-use/resume interaction, instructor distribution binding and
written-lab integration remained. The later milestone below records their completion.

## MarsBot live delivery and publication milestone

The MarsBot companion now passes actual first-use/resume IDE interaction, exact
installed-archive checks, instructor-distribution binding and direct written
integration. First use records sixteen observations / 21 photographs; resume
records nine / ten. Eight quiz attempts and all three source edits are retained;
all five execution activities renew, with nineteen passing placements per process.
Both logs contain no ERROR/SEVERE entries. Capture code remains in the separate
plugin. The grader/fault qualification in the earlier milestone remains separate.

The matching private checker passes 7,623 cases for each of three equivalent
implementations; one extracted shipped alternative also passes all 7,623. Five
private packaging tests preserve exact edition and graded-byte bindings. The final
lab proof has 242 PDF pages / 229 chapter pages and passes automated PDF/UA-2 with
1,728 rules / 1,156,866 checks and zero failures. Human reviews remain open.

There are now thirty exercised downloadable native packages plus the built-in
first unit. Six companions remained implementation work at that milestone. Final
course-wide distribution and evidence acceptance is also still required. Historical
milestones above describe their then-current scope, rather than current completion.

## Maze assessment foundation and native draft

The maze checker now preserves the original intermediate-transition and renderer
contracts in data-only native assessments. Both equivalent implementations pass
all eight public searches,416 private searches and five renderer cases:858 positive
placements. Ten faulty profiles are rejected. Generated CLI checks add26 passing
placements through the same checker. Complete implementation regression passes
304 CLI /1342 plugin tests and all required line/branch/parser gates.

The new010-maze-frontier draft passes two actual native archive/import tests and
all fifteen PSI source pointers; public tooling passes641 tests. It contains seven
predictions, two three-stop Debug observations, two Runs and separate renderer and
search assessments. The unfinished independent source remains student-authored,
with equivalent neighbor orders accepted. See revision/maze-native-unit.md.

This draft does not change the thirty exercised downloadable-package count. Its
first-use/resume IDE interaction, instructor edition and written integration remain
open, as do the other five companions and final course-wide acceptance. Six subjects therefore remained implementation work at that draft milestone.


## Maze live delivery and publication milestone

The maze unit now passes first-use/resume IDE interaction, exact installed-archive
comparison, saved-source/quiz verification, instructor binding and written integration.
First use records seventeen observations /22 photographs; resume records ten /eleven.
Eight quiz attempts and all three edited source files persist. All six execution
activities renew; thirteen assessment cases pass per process. Both IDE logs have
no ERROR/SEVERE entries. The two test projects close natively before their owned
IDE processes are terminated; the Gradle wrappers then exit1 as expected.

The bound instructor edition qualifies both equivalent implementations against421
private search/renderer cases each. The checker and alternative extracted from the
shipped archive pass all421 again. Five packaging guards pass. The lab proof remains
242 physical /229 chapter pages, with the maze on printed64–70. Changed/adjacent
pages and the retained annotated figure are reviewed in color and grayscale. Exact
final PDF/UA-2 passes 1,728 rules /1157783 checks with zero failures; the existing
main-book proof is unchanged. Human reviews remain open.

At the maze milestone there were thirty-one exercised downloadable native packages
plus the built-in first unit. Five companions and final course-wide evidence and
distribution acceptance remained implementation work. The restructuring goal
remains active; substantive editing is a separate goal.


A final checker audit adds explicit four-byte alignment validation for the public
byte-array bases (MARS `0f56027c`). The complete CLI suite then passes 305 tests and
the two native archive tests pass again. Both private references and the extracted
shipped alternative pass 421 cases with the final CLI runtime. The first-use/resume
and paired API receipts retain their exact earlier binaries; this adjustment
changes rejection of malformed layouts, without changing the valid shipped sources
or any IDE mechanism.

## Ring delivery milestone

The thirty-second downloadable native unit, `014-ring-records`, now passes first-use and fresh-process resume in IntelliJ, alongside the separate built-in first unit. It retains all 520 kernel and twelve caller cases, split into independent push, pop and caller assessments. Sixteen activities include eight predictions and ten Debug boundaries, with native conditional breakpoints continuing the same caller. Saved progress preserves nine quiz attempts and three edited sources while eight execution activities renew. Twenty PSI pointers and both import tests pass; 649 public tools and 148 authoring tests pass. The final publication receipt and 37,489-case private edition are documented in revision/ring-native-unit.md and private native-ring-unit-01 evidence. Four native companions and the final course-wide acceptance audit remain implementation work. Human reviews and substantive editing retain the separate ownership above.

## UTF-8 delivery milestone

The thirty-third downloadable native unit, `015-utf8-streams`, now passes first use and fresh-process edited-source resume alongside the separate built-in first unit. Sixteen activities connect two complete byte-interval experiments to one independent scanner, including eight predictions, ten Debug boundaries, two Runs and three assessments. All 426 original stream cases and fifteen caller variants remain, with twelve additional interleaved-state cases. Both real processes pass 453 cases; nine quiz attempts and three edited sources persist while eight execution activities renew. Twenty PSI pointers and both archive/import tests pass. Public tooling 657 and authoring 150 tests pass; a separate five-test highlighter export renews all 837 source snippets while preserving all 836 historical color records exactly. Two private implementations and the alternative extracted from the instructor archive each pass 87,461 cases, including every scalar value and every byte pair. Publication and evidence bindings are in revision/utf8-native-unit.md and private native-utf8-unit-01. Three companion deliveries and the final course-wide audit remain implementation work; human reviews and separate substantive editing retain the ownership above.

## Clipped-sprite assessment foundation and unit draft

Console format 8 now observes ordered public labels and full register results without resetting execution. Full regression passes 317 CLI / 1,353 plugin tests, required coverage and parser gates; both case readers have full line/branch coverage. All 581 original clipped kernels and ten caller placements are native data, with 474 empty and 107 overlap cases. Five valid source/layout variants pass; distinguishing faults and restored wrong-register results fail. The new checker also requalifies 1,970 ring/UTF-8 native cases. Public tooling passes 665 tests. The sixteen-activity clipped draft passes two native archive/import tests and all twenty PSI pointers, with core-verified boundaries and explicit public exports, but actual first/resume IDE, private distribution and native publication integration remain outstanding. At that draft milestone this remained one of three unfinished companions, and the completed unit count stayed thirty-three plus the built-in first unit. See revision/clipped-native-foundation.md and revision/clipped-native-unit.md.

## Clipped-sprite delivery milestone

The thirty-fourth downloadable native unit, 016-clipped-regions, now passes first use and fresh-process edited-source resume alongside the separate built-in first unit. Both processes pass all 591 public cases, retaining nine quiz attempts and three edited sources while eight execution activities renew. The authoring route passes 151 Jupiter tests; public tooling 665 and five private distribution guards pass. Both private implementations and the extracted shipped alternative each pass 7,230 cases. Native TeX directly connects interval reasoning, safe rejection, overlap copying and actual caller boundaries, retaining the real annotated figures and original IntelliJ listing colors. The accepted 244-page lab proof is reviewed on physical pages 42–51, figure 48 also grayscale; exact PDF/UA-2 passes 1,728 rules / 1,172,867 checks with zero failures. Expressions and bytecode, then the final course-wide audit, remain implementation work. Detailed bindings and limits are in revision/clipped-native-unit.md and private native-clipped-unit-01. Human reviews and separate substantive editing retain the ownership above.

## Expression delivery milestone

The thirty-fifth downloadable native unit, 017-expression-order, now passes actual first use and fresh-process edited-source resume alongside the separate built-in first unit. Both processes pass 566 public cases; nine quiz attempts and three edited sources survive while nine execution activities renew. Seventeen activities connect complete arithmetic/overflow experiments to grammar, precise first-error order and independent parsing. All twenty PSI pointers and two archive tests pass. Public tooling passes 673 tests, authoring 152 Jupiter tests and private distribution five guards. Both private implementations and the extracted shipped alternative each pass 10,267 checks. The original eleven repeated-call rotations remain private; six distinct public recovery sequences are shipped. Direct native TeX integration retains actual annotated figures and highlighter colors. The final 244-page proof is reviewed on physical 163–172, figure169 also grayscale; exact PDF/UA-2 passes 1,728 rules / 1,175,558 checks with zero failures. Bytecode and the final course-wide acceptance audit remain implementation work. See revision/expression-native-unit.md and private native-expression-unit-01. Human reviews and separate substantive editing retain their previous ownership.

## Bytecode delivery milestone

The thirty-sixth downloadable native unit, 018-bytecode-machine, completes native delivery for all ten companions alongside the separate built-in first unit. Both actual first/resume IDE processes pass all 1,274 public cases, retain nine quiz attempts and three edited sources, and renew eleven execution activities. Nineteen activities connect complete record-classifier experiments to whole-image validation, bounded guest state, precise faults, retirement and fresh calls. All twenty PSI pointers and two archive tests pass. Public tooling 680, authoring 153 and private release guards 5 pass. Both private implementations and the extracted alternative each pass 12,757. The 245-page lab proof is reviewed on physical 170–180; figures 174–177 also grayscale. Exact PDF/UA-2 passes 1,728 rules and 1,180,214 checks with zero failures. The course-wide integration/distribution acceptance audit remains implementation work; human review and substantive editing retain separate ownership. See revision/bytecode-native-unit.md and private native-bytecode-unit-01.
