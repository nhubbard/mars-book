# First package verification

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
