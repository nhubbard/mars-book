# Authoring rules

Treat the public repository as publishable student material. Keep instructor solutions,
answer keys, private grading cases, and instructor archives in mars-book-instructor.
Do not import or copy that repository into this Git history.

Assume basic programming knowledge. Explain assembly syntax, machine state, interfaces,
and instruction semantics precisely, without explaining ordinary computer use repeatedly.
Use the current MARS MIPS64 Release 6 environment and educational syscall ABI.
Check architecture claims against the pinned MARS source and architecture documentation.

Keep AI-authored lesson prose in packages/*/steps. Human interludes live separately
under book/interludes; edit those only when asked and record substantial AI edits in
book/editorial-log.json. Never regenerate human prose as a build side effect.
Use stable package, step, quiz, and figure IDs. Update consumers when changing contracts.

Validate packages, build the student distribution, and run executable checks after changes.
Use state and output contracts rather than reference-source matching. If an essential
check is missing in MARS, implement and test it upstream rather than weakening the check.
Read the upstream AGENTS.md before editing MARS; new plugin code must be Kotlin, use public
IntelliJ APIs, and use Jupiter tests. Commit each tested implementation step.

Use a dedicated IDE test instance and a light theme for captures. Before every upstream
sandbox launch, run the actual clean cleanSandbox runIde tasks. Never invent screenshots
or label a capture recipe as a completed capture. Follow docs/capture-workflow.md.
