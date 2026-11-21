# Authoring rules

Treat the public repository as publishable student material. Keep instructor solutions,
answer keys, private grading cases, and instructor archives in mars-book-instructor.
Do not import or copy that repository into this Git history.

Assume basic programming knowledge. Explain assembly syntax, machine state, interfaces,
and instruction semantics precisely, without explaining ordinary computer use repeatedly.
Use the current MARS MIPS64 Release 6 environment and educational syscall ABI.
Check architecture claims against the pinned MARS source and architecture documentation.

Edit the publication directly in main.tex and includes/**/*.tex. Compose chapters and
human passages with ordinary input/include. Never regenerate edited TeX from JSON or
Markdown. Keep structured exercise/trainer drafts in packages; they do not control book
composition. Follow docs/composition-goal.md and prioritize full chapter prose. Human
passages may live anywhere in the native include sequence. Edit human prose only when
asked and record substantial AI edits in book/editorial-log.json. Never overwrite prose
as a build side effect.
Follow docs/writing-style.md, including the linked Wikipedia editorial guidance.
Final publication must be native LaTeX, including diagrams and screenshot callouts.
Keep screenshot PNGs as assets and retain semantic descriptions and coordinate receipts.
SVGs are draft previews. Verify tagged-PDF accessibility after template integration.
Use stable package, step, quiz, and figure IDs. Update consumers when changing contracts.

Validate packages, build the student distribution, and run executable checks after changes.
Use state and output contracts rather than reference-source matching. If an essential
check is missing in MARS, implement and test it upstream rather than weakening the check.
Read the upstream AGENTS.md before editing MARS; new plugin code must be Kotlin, use public
IntelliJ APIs, and use Jupiter tests. Commit each tested implementation step.

The student instructional system belongs in the main MARS plugin, following the
author's revised scope of 20 November 2026. Keep screenshot and publication-authoring
automation in the separate authoring plugin that depends on MARS. Prefer public
Features Trainer extension points; verify packaged APIs before implementation.
The curriculum has a main learning book and a native LaTeX lab book that explains
and supplements the interactive labs. Follow docs/restructuring-plan.md for revision;
preserve the initial-draft evidence and human passages. There is no page cap.

Use a dedicated IDE test instance and a light theme for captures. Before every upstream
sandbox launch, run the actual clean cleanSandbox runIde tasks. Never invent screenshots
or label a capture recipe as a completed capture. Follow docs/capture-workflow.md.
