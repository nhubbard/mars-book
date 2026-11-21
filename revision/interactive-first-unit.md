# First interactive unit implementation outline

The first complete unit combines the main chapter `main/first-program.tex`, the
written lab supplement `labs/first-program.tex` and a student lesson in the main
MARS plugin. The outline below records its learning contract and implementation boundaries.
The current native implementation is described under “Current implementation
evidence”; this document does not replace the written lab book.

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

The main MARS course exposes prediction, Run and Debug segments. Predictions now use
clickable answer choices in the Learn pane, with wrong-answer feedback and a native
highlight around the relevant assembly instruction. Students do not need Find Action
to answer a question. The optional legacy prediction actions remain available for
compatibility; they are not the guided lesson route. The independent exercise also
provides direct “Check my program” and “Open a hint” links. The Run segment uses
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

A fourth segment now connects the retained answer to output preparation. A
prediction asks for all three register roles, followed by source-bound Debug
observations before the copy, after it and before integer output. Its focused
native tests and full checks pass. The final fresh IDE batch observes native
completion, with selector 4 before the copy, argument 41 after it and selector 1
before integer output. Its log has no SEVERE entries. The initial attempts exposed
file-refresh and future-completion threading defects; both were corrected and
regression-tested before final capture.

Two further native segments provide wrong-output diagnosis and a separate repair
copy. The counterexample uses the service selector as an argument while retaining
correct arithmetic. Actual Debug pauses show the copied 4 and final selector 1;
a question distinguishes argument repair from changing arithmetic or services.
The repair checker observes exact current source and normal Run completion. Both
`move` and equivalent `or` preparations pass; the faulty program fails. In the
actual IDE, the `or` repair prints 41, survives restart and completes the native
lesson twice, while the counterexample remains unchanged.

The repair observation covers one Run for input 20. A seventh native segment now
checks independent writing of 3x+1 with inputs 20, 0, -3, -1000 and 1000. Its skeleton
supplies the exact text interface; the learner writes the input, computation and
output body. The existing grader evaluates a cached assembly image in separate
machines, using normal-exit and exact-output checks with a 2000-instruction bound.
Different labels, scratch registers and equivalent programs are accepted.

Actual IDE verification observes a failed starter, five passing cases for a private
equivalent fixture, invalidation after an edit, successful rechecking and retained
work on restart. The three native independent-pass callbacks correspond to the
first check, the recheck and restart. Public finite cases support reasoning for the
stated range; they are not authoritative private grading or a proof for every input.
At that milestone, the grader suite passed 131 tests and the plugin suite passed
962, including 67 learning tests. Later results are recorded upstream and in the
private verification history.

Graduated hints, project-level hint release and prediction requirements, and explicit guided/free-writing modes are now implemented and exercised. Three native LaTeX lab figures use four actual IDE originals and fourteen explanations; their current proof has automated geometry, pixel, font and PDF/UA-2 verification. Human review remains open.

A separate fresh-process batch reopens all six guided segments after native save/close. Each reports one native pass callback for unchanged source with zero process starts. After an offline comment edit to first-program.asm, Run, arithmetic Debug and output preparation require fresh execution evidence (zero pass callbacks); prediction history and the separate diagnosis and repair files retain completion. Sources, answers and progress are not injected by the restart driver. The independent segment has its own earlier reopen/edit evidence. These checks do not restore a running simulator, an unfinished multi-pause trace or every possible interrupted task.

Recoverable first-unit reset is now implemented and exercised across native save/close and a fresh IDE process. It preserves source, saves one edition-specific recovery copy and restores quiz/hint history while requiring fresh execution checks. Empty reopened records cannot replace the copy. Instructor-selected guided requirements and completed-assessment limits are now implemented. Remaining work includes general native content delivery, arbitrary per-step policies and independent review. The initial Learn-panel clipping is corrected with supported native dock resizing and concise navigation names. Captures at a 1280-pixel-wide IDE window now measure the lesson viewport and reject horizontal overflow of text or navigation. Wider layouts are preserved; vertical scrolling remains normal for long lessons. These are verification views, not publication plates, and do not establish every display scale or human usability. Author and representative learner review remain open.


The plugin also imports the existing version 1 student distributions through a
native file-selection and preview workflow. Each ID/version/archive digest has a
separate working copy, and an identical reimport keeps student edits. This is file
delivery for the public projects; it does not interpret their historical steps as
native trainer tasks or execute downloaded Kotlin grading scripts. The bundled
first unit retains its own guided and independent activities. See
[the import guide](../docs/lab-import.md) for the supported workflow and boundaries.


The instruction highlight is queried again by the trainer’s Swing repaint timer,
which does not inherit the lesson task’s read context. The MARS callback acquires
read access explicitly before reading PSI or editor geometry. A regression test
invokes it from a raw Swing timer without pre-existing read access. Native IDE
verification and its precise scope are retained privately with this implementation.

At the 21 November checkpoint, the new inline code passes the plugin tests and
packaged API verification. The corrected native capture run timed out before the
first question was observed. The new controls therefore still need a successful
end-to-end native walkthrough; earlier dialog captures do not validate them.


## Inline native verification after the checkpoint

The 21 November resumed batch completed all five questions through actual mouse
clicks on their native Learn-pane links. Wrong-answer feedback, corrected answers,
source focus and removal of completed choices were observed. Correct explanations
are deferred until their step instead of appearing as gray previews. The first
question and second-question screenshots explicitly check this distinction.

The same fresh IDE process completed normal Run, arithmetic Debug, output
preparation, diagnosis, repair and independent assessment. The starter failed its
public cases; an equivalent program passed five cases, an edit required a new
check, and source and hint history survived lesson restart. Direct check/hint links
were clicked. Prediction restart produced a second native pass without replacing
student edits. Native project save/close completed. This batch does not repeat
the earlier fresh-process policy/reset/recovery checks or substitute for human
learner review. The private evidence is `inline-prediction-02/desktop-final`.

The capture driver initially treated Swing's zero-width caret rectangles as
off-screen. The final log observes those zero widths and the corrected one-pixel
visibility boxes. A second driver wait used an obsolete diagnosis prompt; that
was corrected to the current visible text. These are separate from the MARS
read-access repair. The final log contains no SEVERE or ERROR entries.

Figure 1.1 in the revised written lab now shows the actual inline question and
source cue, with editable native LaTeX callouts and an accessible description.
The figure explains that editor focus is not a Debug pause.
