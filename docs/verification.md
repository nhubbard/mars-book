# Chapter verification

The initial package was checked with JDK 25.0.1 and the locally built MARS CLI
from the upstream checkout pinned in package.json. The private build records the
actual CLI SHA-256 and Java version, rather than assuming a JAR matches a branch name.

## Executed checks

- The illustrated reference passes three public and ten private input cases.
- An alternative using $t4 and computing (x + 1) + x passes the same thirteen cases.
- The illustrated register checkpoint passes for input 20: $v0 = 20, then $t0 = 40 and 41.
- Seventeen partial programs reconstructed from the published snippets assemble,
  produce their expected partial output, and exit normally.
- The reconstructed complete program passes all three public cases.
- A deliberate +2 error fails all three public cases.
- The original starter exits normally and fails all three assignment cases.
- All 22 quiz keys cover existing questions and valid choices.
- Twelve Python tooling tests check package validity, quiz consistency, path confinement,
  ordering, deterministic exports, insertion of author prose within a chapter, link
  rebasing, native diagram export, source preservation, and annotation validation.
- The Markdown book and student archive build successfully.

These cases establish the observed behavior for the supplied fixtures; the full bounded
input domain is specified by the task and explained algebraically in the text. They are
not an exhaustive execution of every possible input. The private verifier can be rerun
against any explicitly supplied compatible CLI build.

## Remaining publication work

The five IDE figures have recipes, captions, alt text, and annotation labels. Native
automated IDE capture and actual screenshots remain pending. The annotation renderer
is implemented and tested, but that is only the rendering stage of the capture process.
Interactive progression, downloaded lessons, and instructor policy enforcement are
design descriptions. The text needs author review and a student walkthrough. MacTeX
composition awaits the author-supplied template and frontmatter.

## Expanded chapter review

The 0.2.0 reading sequence places 22 connective prose sections and ten semantic
teaching fixtures around the original microsteps. Two diagrams have SVG draft
previews and native TikZ sources. The SVG previews were rendered and visually
inspected; their labels and arrows are readable without clipping. Native TikZ
compilation and PDF accessibility checks await MacTeX and the author's template.
The two diagrams explain concepts and are not IDE screenshots.

## Single-pixel chapter

The illustrated first word is 0x00336699; its little-endian bytes are 99 66 33 00.
After the byte store they are ff 66 33 00, and the final word is 0x003366ff.
The chapter's verifier checks the reference, an equivalent register allocation,
reference checkpoints, intermediate snippet programs, and deliberately incorrect
implementations. It also checks varied neighboring canaries. The new data-address
assertion is covered by packaged CLI tests for aligned, unaligned, and wide addresses,
as well as moved, missing, and incorrectly typed symbols.

The CLI checks memory state, not the visible Bitmap Display. The three pixel figures
remain planned. The chapter explains RGB numerically so its meaning does not depend on
recognizing a color in a screenshot.

## Eight-byte sprite chapter

The sprite reference and bitswap alternative pass the same coordinate-based oracle.
Public cases use the default image and walking bits. Private cases include all 256
possible byte patterns across 32 frames, plus empty, full, and mixed rows; this is not
an exhaustive test of every possible eight-byte image. The frame is poisoned before
each execution. Both canaries and the original source bytes must be preserved.

The illustrated checks observe the loaded row, completed reversal, first pixel pair,
and first completed row. Fifteen cumulative source segments are reconstructed from
the published steps and checked at their intermediate register or memory boundaries.
Deliberate early reversal termination, wrong stride, omitted black stores, a color
write in a delay slot, an overrun, and source modification must all be rejected.

The default workload completes 1,674 instructions in the explicit-loop reference and
1,210 in the bitswap alternative, including exit. The 464-instruction difference is
also explained by eight replacements of 59 reversal instructions with one bitswap.
The verifier requires warning-free assembly and exact counts for these fixtures.

The concept diagram's SVG preview was rendered and visually inspected. Its native
TikZ source awaits MacTeX and the template. All four IDE figures remain planned;
no screenshot or repaint assertion has yet been verified in an IDE capture.

## Bit-inspector chapter

The reference and wide-shift alternative use the same word-report and full-width-memory
contract. Public inputs include -1, zero, and the maximum signed word. Private inputs
add signed boundaries, walking-one and walking-zero patterns, and deterministic mixed
words. These are representative tests, not all 2^32 possible input words.

Each of the four result fields is checked across eight bytes. Poisoned high halves
expose narrow stores that a console-only or low-word-only test would miss. The source
and both varied guards must be preserved. Illustration checks observe both load
extensions and both addition widths for -1 and the positive signed-word boundary.

Eleven cumulative programs are reconstructed from the published code segments and
checked for intermediate registers, stored bytes, and their exact partial reports.
Deliberately wrong extension, arithmetic width, source interpretation, store width,
formatting, input writes,
and result overruns must be rejected. Both implementations must assemble without warnings.

The extension diagram preview was rendered and visually inspected. Its native TikZ
source and the other fixtures await the LaTeX template and compilation. All four
IDE figures remain planned; a console report alone is not evidence of the high halves.
