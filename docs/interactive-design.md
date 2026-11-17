# Guided lessons in the MARS plugin

## Intended experience

A student opens a course catalog, downloads a versioned task, and chooses guided work
or free writing. Guided mode presents a short explanation, focuses the relevant source
region, asks for an instruction or observation, and offers a quiz and hints. Students
can run or debug their actual program. Free-writing mode presents the same contract
with less scaffolding and accepts equivalent solutions. The book and both modes consume
the same versioned package content.

Instructors can control required checks and quizzes, hint release, retry behavior,
skipping, and mode switching. Course policy is distinct from authored lesson content.
A teacher can change progression without forking every paragraph. Markdown readers
currently navigate freely; these controls are proposed behavior, not implemented features.

## Proposed local state and transitions

Keep progress outside the source package and bind it to course, package ID, package
version, and content digest. Record attempts, selected choices, revealed hints, and the
source revision checked. An edit invalidates successful execution checks that depend
on that source. It should not erase a student's written reasoning or previously read
content. Advancing a step should not modify unrelated source. A version update must be
explicit and preserve the original working copy until migration succeeds.

A useful transition model is available, editing, checking, needs-revision, complete.
Completion depends on instructor policy and the step contract. Assembly success alone
does not establish semantic correctness. Source text equality is inappropriate for
behavioral equivalence. State checks need defined pause boundaries and independent
sessions so they do not corrupt the student's active debugger.

## Separate authoring plugin

The trainer and capture tooling belong in a separate IntelliJ plugin that depends on
MARS. The published MARS simulator does not need a dependency on the book or trainer.
The authoring plugin can depend on the bundled IDE Features Trainer for lessons and
native highlighting. Its first purpose is reproducible IDE figure production; student
remote delivery and instructor policy remain later work.

The pinned source and public SDK investigation is recorded in
[intellij-tutor-investigation.md](intellij-tutor-investigation.md). It identifies public
course and language extension points, task action triggers, state checks, and UI
highlighting. Prefer that integration before inventing a separate lesson UI framework.
The New UI onboarding step API is internal and is excluded from the packaged design.

MARS-specific fixture selection, expected register and console states, figure receipts,
and capture output remain authoring concerns. Use native Run and Debug APIs and check
what is actually visible in the IDE. A core-only run cannot establish that a debugger
view has updated. Keep private solutions in the private repository and supply them to
the capture run through local configuration, rather than embedding them in the authoring
plugin JAR. The local source remains editable; the capture run uses a dedicated fixture.

Implementation must be Kotlin with imports and Jupiter tests. Inspect API contracts
before choosing entry points and avoid internal APIs in the packaged plugin. A public
MARS API may need a small extension if the authoring plugin lacks a necessary supported
operation; document that gap and test the upstream change. Do not reach into internal
run-service implementations by reflection.

## Remote delivery concept

Start with local versioned archives. Later a server catalog can publish package IDs,
versions, digests, compatibility requirements, and download locations. Verify content
before extraction, reject path traversal and symlinks, limit archive size, and retain
an offline copy. Pin the version for an active assignment. Authentication, hosting,
rosters, grade return, and learning-management-system integration remain open decisions.

Treat downloaded lessons as content, not permission to execute arbitrary code. The
current grading scripts are trusted instructor Kotlin and can execute JVM code; they
are not a sandbox for scripts received from an unknown server. A future student runner
needs an allowlisted declarative assertion format or an isolated trusted grading service.
Private grading and instructor keys stay off the public download endpoint. Checks sent
to a client are inspectable; secrecy must not be a client-side security assumption.

Do not call this feature JetBrains Code With Me or imply use of its collaboration
protocol. The desired interaction is guided instruction authoring; live collaborative
editing is a separate optional product decision.
