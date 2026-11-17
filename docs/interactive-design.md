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

## Platform integration investigation

Before designing the UI or project integration, inspect the read-only IntelliJ 2026.3
checkout and current public API contracts. Prefer native editor, PSI, execution,
debugger, coroutine, and lifecycle mechanisms. New plugin implementation belongs in
the MARS repository, in Kotlin, with Jupiter coverage and public packaged APIs. There
is no plugin mechanism implemented by this book repository. A custom protocol or
adapter needs a concrete platform gap documented during that investigation.

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
