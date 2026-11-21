# IntelliJ lesson and highlighting investigation

Scope update, 20 November 2026: the student instructional system now belongs in
the main MARS plugin. Screenshot and publication-authoring automation remain in
the separate dependent plugin. The earlier dependency recommendations below
record the original authoring-only scope; the public API investigation remains
useful, and actual packaged API compatibility must be checked for the student
integration. Follow `interactive-design.md` and `restructuring-plan.md` for the
current implementation scope.

## Features Trainer

IntelliJ IDEA 2026.3 includes the IDE Features Trainer, available through Help → Learn
IDE Features and the Learn tool window. JetBrains describes tutorials made of lessons
and tasks in its [user documentation](https://www.jetbrains.com/help/idea/feature-trainer.html).
The public SDK [extension-point list](https://plugins.jetbrains.com/docs/intellij/intellij-community-plugins-extension-point-list.html#ide-features-trainer-plugin)
lists training.ift.language.extension, training.ift.learning.course, and
training.ift.learning.commonCourse without Internal tags. These are a credible integration
path for the separate MARS authoring plugin. This investigation is source inspection,
not a completed integration or a compatibility-verifier result.

The examined checkout is pinned at c91871ae2864649d952825cff72569e3db0e7ac6. Its
[plugin descriptor](https://github.com/JetBrains/intellij-community/blob/c91871ae2864649d952825cff72569e3db0e7ac6/plugins/ide-features-trainer/res/META-INF/plugin.xml)
declares plugin ID training and the three extension points.
[LangSupport](https://github.com/JetBrains/intellij-community/blob/c91871ae2864649d952825cff72569e3db0e7ac6/plugins/ide-features-trainer/src/training/lang/LangSupport.kt)
provides language and learning-project integration.
[LearningCourseBase](https://github.com/JetBrains/intellij-community/blob/c91871ae2864649d952825cff72569e3db0e7ac6/plugins/ide-features-trainer/src/training/learn/course/LearningCourseBase.kt)
and [KLesson](https://github.com/JetBrains/intellij-community/blob/c91871ae2864649d952825cff72569e3db0e7ac6/plugins/ide-features-trainer/src/training/learn/course/KLesson.kt)
provide course and lesson structure.

[TaskContext](https://github.com/JetBrains/intellij-community/blob/c91871ae2864649d952825cff72569e3db0e7ac6/plugins/ide-features-trainer/src/training/dsl/TaskContext.kt)
provides action triggers, stateCheck, task tests, and triggerAndBorderHighlight /
triggerAndFullHighlight. [LessonContext](https://github.com/JetBrains/intellij-community/blob/c91871ae2864649d952825cff72569e3db0e7ac6/plugins/ide-features-trainer/src/training/dsl/LessonContext.kt)
can prepare runtime tasks and associate a task with an action. These facilities can
highlight an identified UI component and wait for an expected action or state.
A production implementation must select supported members and validate dependencies
against the actual packaged IDE. Some nearby methods and sample implementations use
internal types or deprecated helpers; do not copy those calls indiscriminately.

## Contextual tooltips

[GotItTooltip](https://github.com/JetBrains/intellij-community/blob/c91871ae2864649d952825cff72569e3db0e7ac6/platform/platform-impl/src/com/intellij/ui/GotItTooltip.kt)
has public text constructors and can attach to an action presentation. The lower-level
builder constructor is marked Internal. Its source contract limits tooltips to a direct
response to a deliberate user action. Short, skippable tours are suitable; a 22-step
assembly course should not become a chain of promotional balloons.
The [SDK guidance](https://plugins.jetbrains.com/docs/intellij/got-it-tooltip.html)
describes contextual tooltips and sequences. Prefer Features Trainer tasks for sustained
instruction; use a tooltip only where its activation and lifetime fit the contract.

## Internal onboarding API

[NewUiOnboardingStep](https://github.com/JetBrains/intellij-community/blob/c91871ae2864649d952825cff72569e3db0e7ac6/platform/new-ui-onboarding/src/com/intellij/platform/ide/newUiOnboarding/NewUiOnboardingStep.kt)
is explicitly annotated Internal. Its module descriptor also has internal visibility.
It is not an acceptable dependency for this packaged authoring plugin.

## Capture responsibilities

Features Trainer provides the lesson interaction and highlighting. MARS provides the
simulator, Run/Debug behavior, and visible register and memory tools. The authoring
plugin must connect a selected figure recipe to the correct project fixture, arrange
a light UI, verify the execution and visible state, resolve annotation bounds, and
capture an actual image. It must write a digest-bound receipt and fail a capture when
a state assertion is unsatisfied. The existing annotation renderer then creates the
print figure from that PNG and its callouts.

The separate plugin should depend on edu.missouristate.mars and training. Add it to the
capture instance through its own build; do not reverse the dependency by including the
trainer in the MARS distribution. Run actual clean cleanSandbox runIde tasks before each
IDE launch. Both the integration and end-to-end capture remain implementation work.
