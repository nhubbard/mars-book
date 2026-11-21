# First-unit course policy

MARS stores course settings separately from each student's progress. In IntelliJ,
open **Settings → Tools → MARS Learning**. The first unit currently offers three
controls:

- **Released hints for the independent program** accepts a number from zero to
  four. Two releases the first two hints. Reducing the number prevents further
  reveals; it does not remove a hint the student has already read.
- **Require both prediction quizzes before independent work** requires correct
  latest answers to the doubling and negative-input questions in **Predicting
  register changes**. The independent exercise opens its assembly file and explains
  the prerequisite. Its assessment and hint actions become available after those
  answers satisfy the requirement. The questions use the current content edition.
- **Allow free-writing mode** controls access to independent assessment. When it is
  disabled, the mode chooser offers guided work, and direct entry to the independent
  lesson explains the restriction. Existing independent source and hint history stay
  intact. Releasing the mode later restores access without resetting that work.

The default is four released hints with no prerequisite and free writing available.
An instructor can start with no hints, release an initial question about value flow, then release a concrete
trace later. Reading help does not complete a source check. Meeting the prediction
requirement also does not establish that the student's independent program works;
the program still has to meet its execution contract.

Apply changes with the normal settings dialog. Reopen a hint dialog to refresh its
release display, and restart a lesson to refresh its task display. Requests consult
the current policy. If a newly required prerequisite is unsatisfied during an
execution check, that result cannot earn a successful check. Existing genuine
source evidence remains stored; the native task cannot complete while the current
prerequisite is unsatisfied. A source edit separately invalidates execution evidence.

IntelliJ saves the shared policy in `.idea/mars-learning.xml`. Personal progress,
quiz attempts and hint history stay in `.idea/workspace.xml`. For example, this
course policy releases the first two hints and requires the prediction questions:

```xml
<project version="4">
  <component name="MarsLearningPolicy">
    <option name="releasedHints" value="2" />
    <option name="requirePredictions" value="true" />
  </component>
</project>
```

Prefer the settings dialog when configuring a course project; IntelliJ owns the
file's serialization. Values equal to defaults may be omitted when it saves.
Share the policy file with the course project, and keep each student's workspace
personal. These client-side controls are inspectable and editable by the student.
Use private instructor grading for authoritative assessment; this file does not
supply authentication, a locked examination environment or private answers.

The public Features Trainer lesson properties do not provide course hint-release
or MARS prerequisite policies. The implementation uses a standard project settings
component, native settings controls, native lesson tasks and MARS-specific checks.
It does not replace the trainer UI. Screenshot automation remains in the separate
authoring plugin.

The native policy verification exercises Settings and real prediction dialogs,
then observes hint release at 0, 2, 0 and 4. It requires no source mutation, no
completion from hints, and rejection of the unfinished starter by all five public
cases. The implementation and exact verification scope are recorded upstream in
`docs/student-instruction-system.md` and privately under `native-policy-01`.
These first-unit controls do not yet implement per-step progression, retry limits,
remote course policy distribution or private grading.

## Choosing a mode

Tools → MARS Learning opens the chooser; Find Action can locate the same action.
Guided work starts with predictions and remembers the last guided segment opened.
Free writing opens the related independent contract. It permits your own instruction
sequence, labels and registers, subject to that contract's behavioral checks.
Both modes keep source editable. Switching changes the lesson and selected file,
while retaining each activity's work, attempts and hints. It does not grant a pass.
Cancel leaves the activity unchanged and creates no exercise source on first use.

The last mode and guided destination are personal workspace state. A verified fresh
IDE process restores them, retains both edited files and reopens the saved hint.
This is lesson-segment navigation; it does not serialize a live simulator or debugger
pause. Course prerequisites and source-dependent checks still apply. Reopen the
chooser after a policy change to refresh its available choices.
