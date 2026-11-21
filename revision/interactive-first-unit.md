# First interactive unit implementation outline

The first complete unit combines the main chapter `main/first-program.tex`, the
written lab supplement `labs/first-program.tex` and a student lesson in the main
MARS plugin. The outline below specifies the implementation to build next; it is
not a completed interactive lesson or a replacement for the written lab book.

## Learning contract

A programming-competent assembly beginner follows the input 20 through integer
input, doubling, addition and integer output. They explain a register's role at a
particular execution boundary, distinguish a computation error from an output
argument error, and implement a related calculation without construction hints.
Use the complete public `labs/files/console.asm` as the worked example. Use a
separate student work file for the independent variation. Keep the MIPS64 target
and execution options explicit in the content identity and source revision.

## Native lesson segments

| Segment | Teaching and interaction | Evidence |
| --- | --- | --- |
| Recognize the program | Open the public example without replacing existing student edits; connect it to the book and lab supplement | Correct project/file selected; content edition recorded |
| Predict the doubling | Ask what changes when both source operands name the input register; diagnose a mistaken belief that a source is consumed | Recorded choice and useful feedback; allow another attempt |
| Run the complete program | Use normal MARS Run, enter 20 and interpret the console result | Actual matching run/session and source revision; a headless check alone does not satisfy this observation |
| Trace the arithmetic | Use normal Debug, a breakpoint before the first addition and source stepping | Actual pause in the selected file, then observed register values at the two boundaries; late observations cannot pass edited source |
| Follow output preparation | Ask which register now holds a service selector and which retains the answer | Prediction plus actual source/session state; explain the last writer of the value |
| Investigate the wrong argument | In a recoverable exercise copy, change the integer-output argument to use v0 and find the first incorrect state | Arithmetic remains correct, but output is wrong; corrected edited work is checked again |
| Apply the idea independently | Implement 3x+1, update its answer message and test positive, zero and negative inputs; permit another temporary register | Behavioral checks accept equivalent programs; completion is distinct from the guided observations |

Repeat or revisit segments without erasing quiz/hint history. A deliberate reset
must be recoverable and must not silently overwrite a student's source. A learner
can choose free writing when course policy permits it. Instructor policy controls
required observations/quizzes, hint release and mode switching; it does not turn
client-side checks into secret or authoritative private grading.

## Implementation boundaries

Use the public Features Trainer course and task APIs as the native lesson host.
Follow their current public overloads and verify the actual packaged plugin API.
Use standard Run, Debug, editor, document, project coroutine and persistence APIs;
keep custom code limited to MARS content and assessment semantics. The concrete
extra progress-state requirement is documented in the upstream
`docs/student-instruction-system.md`.

Before choosing a quiz presentation, inspect the host's public choice/feedback
facilities. If no supported control exists, document that gap and use an ordinary
public IntelliJ dialog or component for the MARS assessment rather than creating
a second lesson framework. Do not access internal lesson completion state.

Use the existing assembly cache and independent session factory for behavioral
checks. Do not mutate the student's active debug session for grading. The lesson
controller must bind each evaluation to all checked source and execution options,
observe edits and cancellation, and use the progress service's current request
capability before recording a result. Actual IDE observations have their own
session and source identity; running a separate core check cannot substitute for them.

Start with a bundled, versioned unit. Downloaded lesson content must not authorize
arbitrary Kotlin execution. Design content import, version compatibility and a
supported assertion contract before enabling remote catalog downloads. Keep
instructor-only checks and solutions in the private distribution. Student-side
feedback and public tests remain inspectable.

## First unit completion review

Exercise normal entry, prediction retries, Run, Debug, source edits while checking,
resume after closing/reopening the project, hint release, free-writing completion
and reset. Test both the demonstrated implementation and an equivalent program,
plus meaningful incorrect implementations. Verify multiple projects and concurrent
student sessions independently. Before every actual IDE launch run the real
`clean cleanSandbox runIde` tasks. Capture the resulting student experience through
the separate authoring plugin, with light-theme state receipts and native annotations.

Review the written and interactive unit together with the author before extending
the same method across the curriculum. Representative learner review remains open
until people with the intended prerequisites perform the independent task.

## Current implementation evidence

The main MARS course currently exposes prediction, Run and Debug segments. The prediction
dialogs support wrong-answer feedback and revised answers. The Run segment uses
the ordinary process lifecycle, exact captured source and options, document stamps
and content-specific progress capabilities. Source edits invalidate old evidence.
The actual IDE check observes the platform's lesson-pass callbacks and the expected
console transcript; a core-only check or visible future text is insufficient.
The original event-only Run check was corrected to the native timer facility after
the first graphical attempt exposed asynchronous completion.

The Debug observer follows a source-bound session through the two arithmetic
instructions. It uses copied stopped-state values, instruction words, PC boundaries
and fresh suspension identities. Edits invalidate the attempt, and simultaneous
sessions cannot advance each other. A fresh actual IDE batch observes input 20,
results 0, 40 and 41, and native Debug lesson completion. The separate authoring
plugin selects the owned register tab and reads the displayed rows; the production
lesson contains no screenshot automation.

This verifies only the three implemented segments. Output-argument diagnosis,
graduated hints, instructor policies, free-writing acceptance and complete-unit
resume/reopen behavior remain to be integrated and exercised. The current raw
captures are verification artifacts awaiting publication layout and annotations.
