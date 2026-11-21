package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.intellij.CommonBundle
import com.intellij.debugger.ui.DebuggerContentInfo.VARIABLES_CONTENT
import com.intellij.execution.ui.RunContentManager
import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.ide.DataManager
import com.intellij.ide.ui.LafManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebugSessionListener
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Window
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO
import javax.swing.AbstractButton
import javax.swing.JComponent
import javax.swing.JCheckBox
import javax.swing.JSpinner
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.text.JTextComponent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import training.lang.LangManager
import training.learn.CourseManager
import training.statistic.LessonStartingWay
import training.learn.course.Lesson
import training.learn.lesson.LessonListener
import org.jdom.Element

internal data class BookLessonCapturePlan(
    val projectRoot: String = "",
    val outputDirectory: String = "",
    val sourceSha256: String = "",
    val captureRun: Boolean = false,
    val captureDebug: Boolean = false,
    val captureOutput: Boolean = false,
    val captureDiagnosis: Boolean = false,
    val captureRepair: Boolean = false,
    val captureIndependent: Boolean = false,
    val captureHints: Boolean = false,
    val capturePolicy: Boolean = false,
    val resumeIndependent: Boolean = false,
    val expectIndependentRecheck: Boolean = false,
    val independentSourceSha256: String = "",
    val closeProjectAfterCapture: Boolean = false,
    val independentSolutionPath: String = "",
    val independentSolutionSha256: String = "",
) {
    fun validate() {
        require(Path.of(projectRoot).isAbsolute && Path.of(outputDirectory).isAbsolute)
        require(Path.of(projectRoot).normalize() != Path.of(outputDirectory).normalize())
        require(sourceSha256.matches(Regex("[a-f0-9]{64}")))
        require(!captureHints || captureIndependent) { "Hint capture requires the independent lesson" }
        require(!expectIndependentRecheck || resumeIndependent)
        if (capturePolicy) {
            require(captureIndependent && captureHints && !resumeIndependent && !captureRun && !captureDebug && !captureOutput && !captureDiagnosis && !captureRepair)
        }
        if (resumeIndependent) {
            require(captureIndependent && !captureRun && !captureDebug && !captureOutput && !captureDiagnosis && !captureRepair)
            require(independentSourceSha256.matches(Regex("[a-f0-9]{64}")))
        }
        if (captureIndependent && !resumeIndependent && !capturePolicy) {
            require(Path.of(independentSolutionPath).isAbsolute)
            require(independentSolutionSha256.matches(Regex("[a-f0-9]{64}")))
        }
    }
}

/** Opt-in authoring driver only. No screenshot or tutor automation is packaged in MARS. */
class BookLessonCaptureActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val location = System.getProperty("mars.book.lesson.capture.plan") ?: return
        val plan = withContext(Dispatchers.IO) {
            GsonBuilder().create().fromJson(Files.readString(Path.of(location)), BookLessonCapturePlan::class.java)
        }
        plan.validate()
        if (Path.of(checkNotNull(project.basePath)).toRealPath() != Path.of(plan.projectRoot).toRealPath()) return
        val output = Path.of(plan.outputDirectory)
        withContext(Dispatchers.IO) { Files.createDirectory(output) }
        try {
            withTimeout(180_000) {
                // Public queue waits for native toolwindow/layout initialization on reopened projects.
                val toolWindowsReady = CompletableDeferred<Unit>()
                withContext(Dispatchers.EDT) {
                    ToolWindowManager.getInstance(project).invokeLater { toolWindowsReady.complete(Unit) }
                }
                toolWindowsReady.await()
                when {
                    plan.capturePolicy -> capturePolicy(project, plan, output)
                    plan.resumeIndependent -> captureResume(project, plan, output)
                    else -> capture(project, plan, output)
                }
            }
            if (plan.closeProjectAfterCapture) closeProject(project, output)
        } catch (error: Exception) {
            withContext(NonCancellable + Dispatchers.IO) {
                Files.writeString(output.resolve("failed.txt"), error.stackTraceToString(), CREATE_NEW)
            }
            throw error
        }
    }

    private suspend fun capture(project: Project, plan: BookLessonCapturePlan, output: Path) {
        val source = Path.of(plan.projectRoot).resolve("mars-labs/console/first-program.asm")
        val predictionPasses = AtomicInteger()
        val predictionListener = object : LessonListener {
            override fun lessonPassed(lesson: Lesson) { predictionPasses.incrementAndGet() }
        }
        val lesson = withContext(Dispatchers.EDT) {
            configurePresentation(project)
            val manager = CourseManager.instance
            manager.modules.single { it.id == "mars.console" }.lessons.single { it.id == "mars.console.predictions" }.also {
                it.addLessonListener(predictionListener)
                Disposer.register(project) { it.removeLessonListener(predictionListener) }
                manager.openLesson(project, it, LessonStartingWay.LEARN_TAB, forceStartLesson = true)
            }
        }
        awaitFrame(project, "With 20 in", source)
        check(withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(source)) } == plan.sourceSha256)
        captureFrame(project, output, "prediction-start", "With 20 in")
        answer(project, output, "doubling", "Mars.Learning.PredictDoubling",
            "\$t0 is 20; reading \$v0 twice consumes the input.",
            "Reading a register does not consume its value.", "\$t0 is 40; \$v0 is still 20.")
        awaitFrame(project, "Now consider input -3", source)
        captureFrame(project, output, "after-doubling", "Now consider input -3")
        answer(project, output, "negative", "Mars.Learning.PredictNegative", "7",
            "The u does not turn -3 into a positive magnitude.", "-5")
        awaitFrame(project, "The destination tells you where to look", source)
        awaitUi { predictionPasses.get() >= 1 }
        captureFrame(project, output, "predictions-complete", "The destination tells you where to look")
        val edited = withContext(Dispatchers.EDT) {
            val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.insertString(editor.document.textLength, "\n# Student work retained after lesson restart.\n")
            }
            FileDocumentManager.getInstance().saveDocument(editor.document)
            editor.document.text
        }
        withContext(Dispatchers.EDT) {
            CourseManager.instance.openLesson(project, lesson, LessonStartingWay.RESTART_BUTTON, forceStartLesson = true)
        }
        awaitFrame(project, "The destination tells you where to look", source)
        awaitUi { predictionPasses.get() >= 2 }
        withContext(Dispatchers.EDT) { lesson.removeLessonListener(predictionListener) }
        val retained = withContext(Dispatchers.EDT) {
            checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor).document.text == edited
        }
        check(retained) { "Lesson restart replaced student source" }
        if (plan.captureRun) captureRun(project, output, source)
        if (plan.captureDebug) captureDebug(project, output, source)
        if (plan.captureOutput) captureDebug(project, output, source, outputPreparation = true)
        if (plan.captureDiagnosis) captureDebug(project, output, source.resolveSibling("wrong-output.asm"), diagnosis = true)
        if (plan.captureRepair) captureRun(project, output, source.resolveSibling("repair-output.asm"), repair = true)
        if (plan.captureIndependent) captureIndependent(project, output, source.resolveSibling("independent.asm"), plan)
        val receipt = mapOf(
            "scope" to "Only the native segments marked observed below; instructor policies and full-unit integration remain incomplete",
            "nativePredictionPassesObserved" to predictionPasses.get(),
            "nativeRunPassedObserved" to plan.captureRun,
            "nativeDebugPassedObserved" to plan.captureDebug,
            "nativeOutputPassedObserved" to plan.captureOutput,
            "nativeDiagnosisPassedObserved" to plan.captureDiagnosis,
            "nativeRepairPassedObserved" to plan.captureRepair,
            "nativeIndependentPassedObserved" to plan.captureIndependent,
            "nativeHintsObserved" to plan.captureHints,
            "platformBuild" to ApplicationInfo.getInstance().build.asString(),
            "sourceSha256" to plan.sourceSha256,
            "retainedSourceSha256" to captureSha256(edited.toByteArray(Charsets.UTF_8)),
            "studentEditsRetainedAfterRestart" to retained,
            "marsPluginArchiveSha256" to withContext(Dispatchers.IO) {
                captureSha256(Files.readAllBytes(Path.of(checkNotNull(System.getProperty("mars.book.mars.archive")))))
            },
        )
        withContext(Dispatchers.IO) {
            Files.writeString(output.resolve("complete.json"), GsonBuilder().setPrettyPrinting().create().toJson(receipt), CREATE_NEW)
        }
    }

    /** Uses only native settings, lesson actions and rendered dialogs; never injects policy/progress state. */
    private suspend fun capturePolicy(project: Project, plan: BookLessonCapturePlan, output: Path) {
        withContext(Dispatchers.EDT) { configurePresentation(project) }
        setPolicy(project, output, "policy-restricted", released = 0)
        val source = Path.of(plan.projectRoot).resolve("mars-labs/console/independent.asm")
        val passes = AtomicInteger()
        val listener = object : LessonListener {
            override fun lessonPassed(lesson: Lesson) { passes.incrementAndGet() }
        }
        val independent = withContext(Dispatchers.EDT) {
            CourseManager.instance.modules.single { it.id == "mars.console" }.lessons.single { it.id == "mars.console.independent" }.also {
                it.addLessonListener(listener)
                CourseManager.instance.openLesson(project, it, LessonStartingWay.LEARN_TAB, forceStartLesson = true)
            }
        }
        try {
            val required = "Your course requires both prediction quizzes"
            awaitFrame(project, required, source)
            delay(600)
            check(passes.get() == 0)
            captureFrame(project, output, "policy-prerequisite-required", required)
            withContext(Dispatchers.EDT) {
                val learn = checkNotNull(ToolWindowManager.getInstance(project).getToolWindow("Learn"))
                val data = DataManager.getInstance().getDataContext(learn.component)
                for (id in listOf("Mars.Learning.CheckIndependent", "Mars.Learning.IndependentHint")) {
                    val action = checkNotNull(ActionManager.getInstance().getAction(id))
                    val event = AnActionEvent.createEvent(action, data, null, ActionPlaces.LEARN_TOOLWINDOW, ActionUiKind.NONE, null)
                    check(ActionUtil.updateAction(action, event).isPerformed) { "Native action update did not complete" }
                    check(!event.presentation.isEnabled) { "Blocked lesson exposed $id" }
                }
                val predictions = CourseManager.instance.modules.single { it.id == "mars.console" }.lessons.single { it.id == "mars.console.predictions" }
                CourseManager.instance.openLesson(project, predictions, LessonStartingWay.LEARN_TAB, forceStartLesson = true)
            }
            val worked = source.resolveSibling("first-program.asm")
            awaitFrame(project, "With 20 in", worked)
            check(withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(worked)) } == plan.sourceSha256)
            answer(project, output, "policy-doubling", "Mars.Learning.PredictDoubling",
                "\$t0 is 20; reading \$v0 twice consumes the input.",
                "Reading a register does not consume its value.", "\$t0 is 40; \$v0 is still 20.")
            awaitFrame(project, "Now consider input -3", worked)
            answer(project, output, "policy-negative", "Mars.Learning.PredictNegative", "7",
                "The u does not turn -3 into a positive magnitude.", "-5")
            awaitFrame(project, "The destination tells you where to look", worked)
            withContext(Dispatchers.EDT) {
                CourseManager.instance.openLesson(project, independent, LessonStartingWay.LEARN_TAB, forceStartLesson = true)
            }
            awaitFrame(project, "implement 3x+1", source)
            val before = withContext(Dispatchers.IO) { Files.readAllBytes(source) }
            captureHints(project, output, "policy-hints-zero", existing = 0, revealTo = 0, released = 0)
            setPolicy(project, output, "policy-release-two", released = 2)
            captureHints(project, output, "policy-hints-two", existing = 0, revealTo = 2, released = 2)
            setPolicy(project, output, "policy-release-zero-again", released = 0)
            captureHints(project, output, "policy-hints-retained", existing = 2, revealTo = 2, released = 0)
            setPolicy(project, output, "policy-release-four", released = 4)
            captureHints(project, output, "policy-hints-four", existing = 2, revealTo = 4)
            check(passes.get() == 0)
            checkIndependent(project, output, "policy-starter-feedback", passed = false)
            check(passes.get() == 0)
            withContext(Dispatchers.IO) {
                check(Files.readAllBytes(source).contentEquals(before))
                Files.writeString(output.resolve("complete.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
                    "platformBuild" to ApplicationInfo.getInstance().build.asString(),
                    "nativeSettingsApplied" to true, "prerequisiteBlockedIndependent" to true,
                    "nativePredictionAnswersUnlockedIndependent" to true,
                    "hintReleaseObserved" to listOf(0, 2, 0, 4), "revealedHintsRetainedAfterRestriction" to true,
                    "independentPassesObserved" to passes.get(), "starterRejectedByFiveCases" to true,
                    "sourceUnchangedByPolicyAndHints" to true, "independentSourceSha256" to captureSha256(before),
                    "marsPluginArchiveSha256" to captureSha256(Files.readAllBytes(Path.of(checkNotNull(System.getProperty("mars.book.mars.archive"))))),
                    "scope" to "Native project policy settings and first-unit prediction prerequisite; other progression policies and mode controls remain open",
                )), CREATE_NEW)
            }
        } finally {
            withContext(NonCancellable + Dispatchers.EDT) { independent.removeLessonListener(listener) }
        }
    }

    private suspend fun setPolicy(project: Project, output: Path, prefix: String, released: Int) {
        val failure = AtomicReference<Throwable>()
        withContext(Dispatchers.EDT) {
            ApplicationManager.getApplication().invokeLater({
                try { ShowSettingsUtil.getInstance().showSettingsDialog(project, "MARS Learning") }
                catch (error: Throwable) { failure.set(error) }
            }, ModalityState.nonModal())
        }
        val label = "Require both prediction quizzes before independent work"
        var settings: JDialog? = null
        while (settings == null) {
            failure.get()?.let { throw it }
            settings = onModalUi {
                Window.getWindows().filterIsInstance<JDialog>().singleOrNull { it.isShowing && lessonVisibleText(it.rootPane).contains(label) }
            }
            if (settings == null) delay(100)
        }
        val dialog = settings
        awaitUi {
            captureComponents(dialog.rootPane).filterIsInstance<JLabel>()
                .none { it.isShowing && it.text == CommonBundle.getLoadingTreeNodeText() }
        }
        onModalUi {
            val widgets = captureComponents(dialog.rootPane)
            widgets.filterIsInstance<JSpinner>().single { it.isShowing }.value = released
            widgets.filterIsInstance<JCheckBox>().single { it.isShowing && it.text == label }.isSelected = true
        }
        awaitUi {
            val widgets = captureComponents(dialog.rootPane)
            widgets.filterIsInstance<JSpinner>().single { it.isShowing }.value == released &&
                widgets.filterIsInstance<JCheckBox>().single { it.isShowing && it.text == label }.isSelected
        }
        captureComponent(dialog.rootPane, output, prefix, label)
        onModalUi { visibleLessonButton(dialog.rootPane, "OK").doClick() }
        awaitUi { !dialog.isShowing }
        failure.get()?.let { throw it }
    }

    /** Only the dedicated authoring fixture opts into closing; the native API saves before disposal. */
    private fun closeProject(project: Project, output: Path) {
        val application = ApplicationManager.getApplication()
        application.invokeLater({
            val closed = ProjectManager.getInstance().closeAndDispose(project)
            val disposed = project.isDisposed
            application.executeOnPooledThread {
                Files.writeString(output.resolve("project-closed.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
                    "closed" to closed, "disposed" to disposed,
                    "scope" to "Native save, close and dispose completed; a later IDE process must verify loaded progress",
                )), CREATE_NEW)
            }
        }, ModalityState.nonModal())
    }

    private fun configurePresentation(project: Project) {
        val themes = LafManager.getInstance()
        themes.autodetect = false
        themes.setCurrentUIThemeLookAndFeel(themes.installedThemes.single { it.name == "IntelliJ Light" && !it.isDark })
        themes.updateUI()
        val colors = EditorColorsManager.getInstance()
        colors.setGlobalScheme(checkNotNull(colors.getScheme(EditorColorsManager.getDefaultSchemeName())))
        val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
        val screen = frame.graphicsConfiguration.bounds
        frame.setBounds(screen.x + 20, screen.y + 40, minOf(1280, screen.width - 40), minOf(900, screen.height - 80))
        frame.toFront()
        LangManager.getInstance().updateLangSupport("MARS_MIPS")
    }

    /** Reopen existing work without seeding progress or installing a solution. */
    private suspend fun captureResume(project: Project, plan: BookLessonCapturePlan, output: Path) {
        val directory = Path.of(plan.projectRoot).resolve("mars-labs/console")
        val source = directory.resolve("independent.asm")
        withContext(Dispatchers.IO) {
            check(captureSha256(Files.readAllBytes(directory.resolve("first-program.asm"))) == plan.sourceSha256)
            check(captureSha256(Files.readAllBytes(source)) == plan.independentSourceSha256)
        }
        val passes = AtomicInteger()
        val listener = object : LessonListener {
            override fun lessonPassed(lesson: Lesson) { passes.incrementAndGet() }
        }
        val lesson = withContext(Dispatchers.EDT) {
            configurePresentation(project)
            CourseManager.instance.modules.single { it.id == "mars.console" }.lessons.single { it.id == "mars.console.independent" }.also {
                it.addLessonListener(listener)
                CourseManager.instance.openLesson(project, it, LessonStartingWay.LEARN_TAB, forceStartLesson = true)
            }
        }
        try {
            awaitFrame(project, "", source)
            val learnShown = CompletableDeferred<Unit>()
            withContext(Dispatchers.EDT) {
                checkNotNull(ToolWindowManager.getInstance(project).getToolWindow("Learn")).show { learnShown.complete(Unit) }
            }
            learnShown.await()
            captureFrame(project, output, "reopened-start", "")
            if (plan.expectIndependentRecheck) {
                awaitFrame(project, "implement 3x+1", source)
                delay(600) // Several native timer checks must reject the old source evidence.
                check(passes.get() == 0) { "Offline edit reused an earlier source check" }
                captureFrame(project, output, "reopened-recheck-required", "implement 3x+1")
                if (plan.captureHints) captureHints(project, output, "reopened-hints", existing = 4, revealTo = 4)
                checkIndependent(project, output, "reopened-rechecked-feedback", passed = true)
            }
            awaitUi { passes.get() == 1 }
            awaitFrame(project, "Your current source passed all five", source)
            captureFrame(project, output, "reopened-complete", "Your current source passed all five")
            withContext(Dispatchers.IO) {
                check(captureSha256(Files.readAllBytes(source)) == plan.independentSourceSha256)
                Files.writeString(output.resolve("complete.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
                    "platformBuild" to ApplicationInfo.getInstance().build.asString(),
                    "resumedIndependent" to true, "nativeIndependentPassesObserved" to passes.get(),
                    "checkActionInvoked" to plan.expectIndependentRecheck,
                    "offlineEditRequiredRecheck" to plan.expectIndependentRecheck,
                    "hintHistoryObserved" to (plan.expectIndependentRecheck && plan.captureHints),
                    "sourceUnchangedByResume" to true, "independentSourceSha256" to plan.independentSourceSha256,
                    "marsPluginArchiveSha256" to captureSha256(Files.readAllBytes(Path.of(checkNotNull(System.getProperty("mars.book.mars.archive"))))),
                    "scope" to "Existing project in a fresh IDE process; no progress injection or solution installation",
                )), CREATE_NEW)
            }
        } finally {
            val observation = withContext(NonCancellable + Dispatchers.EDT) {
                lesson.removeLessonListener(listener)
                mapOf(
                    "nativeIndependentPassesObserved" to passes.get(),
                    "selectedSource" to FileEditorManager.getInstance(project).selectedTextEditor?.let {
                        FileDocumentManager.getInstance().getFile(it.document)?.path
                    },
                    "nativeVisibleText" to WindowManager.getInstance().getFrame(project)?.let { lessonVisibleText(it.rootPane) },
                )
            }
            withContext(NonCancellable + Dispatchers.IO) {
                Files.writeString(output.resolve("resume-state.json"), GsonBuilder().setPrettyPrinting().create().toJson(observation), CREATE_NEW)
            }
        }
    }

    private suspend fun captureIndependent(project: Project, output: Path, source: Path, plan: BookLessonCapturePlan) {
        val solution = withContext(Dispatchers.IO) {
            Files.readString(Path.of(plan.independentSolutionPath)).also {
                check(captureSha256(it.toByteArray(Charsets.UTF_8)) == plan.independentSolutionSha256)
            }
        }
        val passes = AtomicInteger()
        val listener = object : LessonListener {
            override fun lessonPassed(lesson: Lesson) { passes.incrementAndGet() }
        }
        val lesson = withContext(Dispatchers.EDT) {
            CourseManager.instance.modules.single { it.id == "mars.console" }.lessons.single { it.id == "mars.console.independent" }.also {
                it.addLessonListener(listener)
                CourseManager.instance.openLesson(project, it, LessonStartingWay.LEARN_TAB, forceStartLesson = true)
            }
        }
        try {
            awaitFrame(project, "implement 3x+1", source)
            captureFrame(project, output, "independent-ready", "implement 3x+1")
            checkIndependent(project, output, "independent-starter-feedback", passed = false)
            check(passes.get() == 0)
            if (plan.captureHints) {
                val beforeHints = withContext(Dispatchers.IO) { Files.readString(source) }
                captureHints(project, output, "hints-initial", existing = 0, revealTo = 2)
                captureHints(project, output, "hints-reopened", existing = 2, revealTo = 4)
                check(withContext(Dispatchers.IO) { Files.readString(source) } == beforeHints)
                check(passes.get() == 0)
            }
            withContext(Dispatchers.EDT) {
                val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
                WriteCommandAction.runWriteCommandAction(project) { editor.document.setText(solution) }
                FileDocumentManager.getInstance().saveDocument(editor.document)
            }
            checkIndependent(project, output, "independent-passed-feedback", passed = true)
            awaitUi { passes.get() == 1 }
            awaitFrame(project, "Your current source passed all five", source)
            captureFrame(project, output, "independent-complete", "Your current source passed all five")
            var expectedPasses = 1
            withContext(Dispatchers.EDT) {
                val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
                WriteCommandAction.runWriteCommandAction(project) { editor.document.insertString(0, "# Source edit requires a fresh check.\n") }
                FileDocumentManager.getInstance().saveDocument(editor.document)
                CourseManager.instance.openLesson(project, lesson, LessonStartingWay.RESTART_BUTTON, forceStartLesson = true)
            }
            awaitFrame(project, "implement 3x+1", source)
            delay(600) // Allow several native timer checks to reject the invalidated earlier pass.
            check(passes.get() == expectedPasses)
            if (plan.captureHints) captureHints(project, output, "hints-after-edit", existing = 4, revealTo = 4)
            captureFrame(project, output, "independent-edited", "implement 3x+1")
            checkIndependent(project, output, "independent-rechecked-feedback", passed = true)
            expectedPasses++
            awaitUi { passes.get() == expectedPasses }
            val retained = withContext(Dispatchers.IO) { Files.readString(source) }
            withContext(Dispatchers.EDT) {
                CourseManager.instance.openLesson(project, lesson, LessonStartingWay.RESTART_BUTTON, forceStartLesson = true)
            }
            expectedPasses++
            awaitUi { passes.get() == expectedPasses }
            withContext(Dispatchers.IO) {
                check(Files.readString(source) == retained)
                Files.writeString(output.resolve("independent-observations.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
                    "starterRejected" to true, "publicCasesObservedPerCheck" to 5,
                    "nativeIndependentPassesObserved" to passes.get(), "sourceEditRequiredRecheck" to true,
                    "workRetainedAfterRestart" to true, "hintHistoryObserved" to plan.captureHints,
                    "solutionSha256" to plan.independentSolutionSha256,
                    "retainedSourceSha256" to captureSha256(retained.toByteArray(Charsets.UTF_8)),
                    "scope" to "Public formative checks and lesson restart; project reopen and human review remain separate",
                )), CREATE_NEW)
            }
        } finally {
            withContext(NonCancellable + Dispatchers.EDT) { lesson.removeLessonListener(listener) }
        }
    }

    private suspend fun captureHints(project: Project, output: Path, prefix: String, existing: Int, revealTo: Int, released: Int = 4) {
        val titles = listOf("Plan the value flow", "Keep the original input available", "Combine the values deliberately", "Trace one possible calculation")
        val failure = AtomicReference<Throwable>()
        withContext(Dispatchers.EDT) {
            val action = checkNotNull(ActionManager.getInstance().getAction("Mars.Learning.IndependentHint"))
            val learn = checkNotNull(ToolWindowManager.getInstance(project).getToolWindow("Learn"))
            // Exercise the native Learn pane context, without injecting editor/file keys.
            val data = DataManager.getInstance().getDataContext(learn.component)
            val event = AnActionEvent.createEvent(action, data, null, ActionPlaces.LEARN_TOOLWINDOW, ActionUiKind.NONE, null)
            check(ActionUtil.updateAction(action, event).isPerformed) { "Native action update did not complete" }
            check(event.presentation.isEnabled) { "Hints must be available from the native Learn pane" }
            ApplicationManager.getApplication().invokeLater({
                try { ActionUtil.performAction(action, event) } catch (error: Throwable) { failure.set(error) }
            }, ModalityState.nonModal())
        }
        val dialog = awaitDialog(failure, "Independent program hints")
        suspend fun assertRevealed(count: Int) {
            awaitUi {
                val text = lessonVisibleText(dialog.rootPane)
                titles.take(count).all(text::contains) && titles.drop(count).none(text::contains)
            }
        }
        assertRevealed(existing)
        check(onModalUi { lessonVisibleText(dialog.rootPane).contains("releases $released of 4") })
        captureComponent(dialog.rootPane, output, "$prefix-open", "Reveal as much help as you need")
        for (index in existing until revealTo) {
            onModalUi { visibleLessonButton(dialog.rootPane, "Reveal next hint").doClick() }
            assertRevealed(index + 1)
            captureComponent(dialog.rootPane, output, "$prefix-${index + 1}", titles[index])
        }
        if (revealTo >= released) check(onModalUi { !visibleLessonButton(dialog.rootPane, "Reveal next hint").isEnabled })
        onModalUi { visibleLessonButton(dialog.rootPane, "Return to editor").doClick() }
        awaitUi { !dialog.isShowing }
    }

    private suspend fun checkIndependent(project: Project, output: Path, id: String, passed: Boolean) {
        val failure = AtomicReference<Throwable>()
        withContext(Dispatchers.EDT) {
            val action = checkNotNull(ActionManager.getInstance().getAction("Mars.Learning.CheckIndependent"))
            val learn = checkNotNull(ToolWindowManager.getInstance(project).getToolWindow("Learn"))
            val data = DataManager.getInstance().getDataContext(learn.component)
            val event = AnActionEvent.createEvent(action, data, null, ActionPlaces.LEARN_TOOLWINDOW, ActionUiKind.NONE, null)
            check(ActionUtil.updateAction(action, event).isPerformed) { "Native action update did not complete" }
            check(event.presentation.isEnabled) { "Checking must be available from the native Learn pane for its selected source" }
            ApplicationManager.getApplication().invokeLater({
                try { ActionUtil.performAction(action, event) } catch (error: Throwable) { failure.set(error) }
            }, ModalityState.nonModal())
        }
        val dialog = awaitDialog(failure, "Independent program results")
        val expected = if (passed) "PASS input 20" else "FAIL input 20"
        awaitUi { lessonVisibleText(dialog.rootPane).contains(expected) }
        val report = onModalUi { lessonVisibleText(dialog.rootPane) }
        check(report.lines().count { it.startsWith(if (passed) "PASS input " else "FAIL input ") } == 5)
        captureComponent(dialog.rootPane, output, id, expected)
        onModalUi { visibleLessonButton(dialog.rootPane, "Return to editor").doClick() }
        awaitUi { !dialog.isShowing }
    }

    private suspend fun captureRun(project: Project, output: Path, source: Path, repair: Boolean = false) {
        val id = if (repair) "mars.console.repair" else "mars.console.run"
        val prefix = if (repair) "repair" else "run"
        val readyText = if (repair) "repair-output.asm is your working copy" else "Run first-program.asm using the normal MARS Run"
        val completeText = if (repair) "The repaired source printed 41" else "The console result agrees with the prediction"
        val passes = AtomicInteger()
        val passed = CompletableDeferred<Unit>()
        val listener = object : LessonListener {
            override fun lessonPassed(lesson: Lesson) { passes.incrementAndGet(); passed.complete(Unit) }
        }
        val lesson = withContext(Dispatchers.EDT) {
            CourseManager.instance.modules.single { it.id == "mars.console" }.lessons.single { it.id == id }.also {
                it.addLessonListener(listener)
                CourseManager.instance.openLesson(project, it, LessonStartingWay.LEARN_TAB, forceStartLesson = true)
            }
        }
        val connection = project.messageBus.connect()
        var owned: ProcessHandler? = null
        try {
            awaitFrame(project, readyText, source)
            captureFrame(project, output, "$prefix-ready", readyText)
            if (repair) {
                withContext(Dispatchers.EDT) {
                    val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
                    val faulty = "move \$a0, \$v0"
                    val text = editor.document.text
                    check(text.split(faulty).size == 2)
                    WriteCommandAction.runWriteCommandAction(project) {
                        editor.document.setText(text.replace(faulty, "or \$a0, \$t0, \$zero"))
                    }
                    FileDocumentManager.getInstance().saveDocument(editor.document)
                }
            }
            val process = CompletableDeferred<ProcessHandler>()
            val exit = CompletableDeferred<Int>()
            val transcript = StringBuilder()
            val name = "Book lesson $prefix observation"
            connection.subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
                override fun processStarting(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
                    if (executorId == DefaultRunExecutor.EXECUTOR_ID && env.runProfile.name == name) {
                        handler.addProcessListener(object : ProcessListener {
                            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                                if (outputType == ProcessOutputTypes.STDOUT) synchronized(transcript) { transcript.append(event.text) }
                            }
                            override fun processTerminated(event: ProcessEvent) { exit.complete(event.exitCode) }
                        })
                        process.complete(handler)
                    }
                }
            })
            withContext(Dispatchers.EDT) {
                val type = ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.single { it.id == "MarsMipsRun" }
                val settings = RunManager.getInstance(project).createConfiguration(name, type.configurationFactories.single())
                val state = Element("configuration").setAttribute("marsExecutionDefaults", "1")
                state.addContent(Element("option").setAttribute("name", "sourcePath").setAttribute("value", source.toString()))
                settings.configuration.readExternal(state)
                settings.configuration.checkConfiguration()
                ExecutionEnvironmentBuilder.create(DefaultRunExecutor.getRunExecutorInstance(), settings).buildAndExecute()
            }
            owned = process.await()
            while (!owned.isStartNotified) delay(50)
            withContext(Dispatchers.IO) { deliverBookStandardInput(owned, "20\n") }
            check(exit.await() == 0)
            val observed = synchronized(transcript) { transcript.toString() }
            check(observed == "Enter a small integer: Twice your number plus one is: 41\n")
            withContext(Dispatchers.IO) {
                Files.writeString(output.resolve("$prefix-transcript.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
                    "exitCode" to 0, "stdin" to "20\n", "stdout" to observed,
                    "scope" to "Actual Run only; native lesson completion must also be observed",
                )), CREATE_NEW)
            }
            passed.await()
            awaitFrame(project, completeText, source)
            captureFrame(project, output, "$prefix-complete", completeText)
            if (repair) {
                val retained = withContext(Dispatchers.IO) { Files.readString(source) }
                withContext(Dispatchers.EDT) {
                    CourseManager.instance.openLesson(project, lesson, LessonStartingWay.RESTART_BUTTON, forceStartLesson = true)
                }
                awaitFrame(project, completeText, source)
                awaitUi { passes.get() >= 2 }
                withContext(Dispatchers.IO) {
                    check(Files.readString(source) == retained)
                    val counterexample = Files.readString(source.resolveSibling("wrong-output.asm"))
                    check(counterexample.contains("move \$a0, \$v0"))
                    Files.writeString(output.resolve("repair-preservation.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
                        "nativeRepairPassesObserved" to passes.get(), "repairRetainedAfterRestart" to true,
                        "repairSha256" to captureSha256(retained.toByteArray(Charsets.UTF_8)),
                        "counterexampleSha256" to captureSha256(counterexample.toByteArray(Charsets.UTF_8)),
                        "alternativePreparation" to "or \$a0, \$t0, \$zero",
                    )), CREATE_NEW)
                }
            }
        } finally {
            connection.disconnect()
            withContext(NonCancellable + Dispatchers.EDT) {
                lesson.removeLessonListener(listener)
                owned?.takeUnless { it.isProcessTerminated }?.destroyProcess()
            }
        }
    }

    private suspend fun captureDebug(project: Project, output: Path, source: Path, outputPreparation: Boolean = false, diagnosis: Boolean = false) {
        val outputStyle = outputPreparation || diagnosis
        val id = if (diagnosis) "mars.console.diagnosis" else if (outputPreparation) "mars.console.output" else "mars.console.debug"
        val prefix = if (diagnosis) "diagnosis" else if (outputPreparation) "output" else "debug"
        val passed = CompletableDeferred<Unit>()
        val listener = object : LessonListener {
            override fun lessonPassed(lesson: Lesson) { passed.complete(Unit) }
        }
        val lesson = withContext(Dispatchers.EDT) {
            CourseManager.instance.modules.single { it.id == "mars.console" }.lessons.single { it.id == id }.also {
                it.addLessonListener(listener)
                CourseManager.instance.openLesson(project, it, LessonStartingWay.LEARN_TAB, forceStartLesson = true)
            }
        }
        if (outputPreparation) {
            awaitFrame(project, "Track the most recent writer", source)
            answer(project, output, "output", "Mars.Learning.PredictOutput",
                "\$v0 is 20; \$a0 is 41; \$t0 is 0.", "\$v0 no longer holds the input:",
                "\$v0 is 1; \$a0 is 41; \$t0 is 41.")
        }
        if (diagnosis) {
            awaitFrame(project, "Find the last writer", source)
            answer(project, output, "wrong-output", "Mars.Learning.PredictWrongOutput", "20",
                "The input call returned 20", "4")
        }
        awaitFrame(project, if (diagnosis) "Stop other Debug sessions" else if (outputPreparation) "Stop the preceding Debug session" else "Set a breakpoint on addu", source)
        val file = withContext(Dispatchers.EDT) { checkNotNull(LocalFileSystem.getInstance().findFileByNioFile(source)) }
        val line = withContext(Dispatchers.IO) { Files.readString(source).lines().indexOfFirst { it.trim().startsWith(if (outputStyle) "move " else "addu ") } }
        check(line >= 0)
        val type = XBreakpointType.EXTENSION_POINT_NAME.extensionList.filterIsInstance<XLineBreakpointType<*>>()
            .single { it.id == "MarsMipsLine" }
        val breakpoint = writeAction { XDebuggerManager.getInstance(project).breakpointManager.addLineBreakpoint(type, file.url, line, null) }
        val started = CompletableDeferred<XDebugSession>()
        val pauses = Channel<Int>(Channel.UNLIMITED)
        val connection = project.messageBus.connect()
        var owned: XDebugSession? = null
        val rows = mutableListOf<Map<String, Any>>()
        try {
            val name = "Book lesson $prefix observation"
            connection.subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
                override fun processStarting(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
                    if (executorId == DefaultDebugExecutor.EXECUTOR_ID && env.runProfile.name == name) {
                        val session = XDebuggerManager.getInstance(project).debugSessions.single { it.debugProcess.processHandler === handler }
                        session.addSessionListener(object : XDebugSessionListener {
                            override fun sessionPaused() {
                                val position = checkNotNull(session.topFramePosition)
                                check(position.file.toNioPath() == source)
                                pauses.trySend(position.line)
                            }
                        })
                        started.complete(session)
                    }
                }
            })
            withContext(Dispatchers.EDT) {
                val runType = ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.single { it.id == "MarsMipsRun" }
                val settings = RunManager.getInstance(project).createConfiguration(name, runType.configurationFactories.single())
                val state = Element("configuration").setAttribute("marsExecutionDefaults", "1")
                state.addContent(Element("option").setAttribute("name", "sourcePath").setAttribute("value", source.toString()))
                settings.configuration.readExternal(state)
                settings.configuration.checkConfiguration()
                ExecutionEnvironmentBuilder.create(DefaultDebugExecutor.getDebugExecutorInstance(), settings).buildAndExecute()
            }
            val session = started.await()
            owned = session
            val handler = session.debugProcess.processHandler
            while (!handler.isStartNotified) delay(50)
            withContext(Dispatchers.IO) { deliverBookStandardInput(handler, "20\n") }
            for ((index, expected) in (if (outputStyle) listOf(41L, 41L, 41L) else listOf(0L, 40L, 41L)).withIndex()) {
                val sourceLine = pauses.receive()
                check(sourceLine >= line)
                val text = if (diagnosis) when (index) {
                    0 -> "Stop other Debug sessions"
                    1 -> "Step Over li"
                    else -> "Use the observed state"
                } else if (outputPreparation) when (index) {
                    0 -> "Stop the preceding Debug session"
                    1 -> "Step Over li"
                    else -> "You observed the answer survive"
                } else when (index) {
                    0 -> "Set a breakpoint on addu"
                    1 -> "The first addition produced 40"
                    else -> "You have observed the input"
                }
                if (index == 2 && !diagnosis) passed.await()
                awaitFrame(project, text, source)
                val registers = buildMap {
                    put("\$v0", if (outputStyle) (if (index == 2) 1L else 4L) else 20L)
                    put("\$t0", expected)
                    if (outputStyle && index > 0) put("\$a0", if (diagnosis) 4L else 41L)
                }
                val observed = awaitDebugRegisters(project, handler, registers)
                rows += mapOf("sourceLine" to sourceLine + 1, "standardInput" to 20, "expectedRegisters" to registers, "renderedRows" to observed)
                captureFrame(project, output, "$prefix-" + (if (outputStyle) listOf("before-copy", "after-copy", "before-syscall") else listOf("before-add", "after-add", "after-one"))[index], text)
                if (index < 2) withContext(Dispatchers.EDT) { session.stepOver(false) }
            }
            if (diagnosis) {
                answer(project, output, "diagnosis", "Mars.Learning.DiagnoseOutput",
                    "Change the arithmetic because the printed answer is wrong.", "The stopped arithmetic result is already 41",
                    "Prepare the integer-output argument from the retained answer instead of the service selector.")
                passed.await()
                awaitFrame(project, "You located an argument error", source)
                captureFrame(project, output, "diagnosis-complete", "You located an argument error")
            }
            withContext(Dispatchers.IO) {
                Files.writeString(output.resolve("$prefix-observations.json"), GsonBuilder().setPrettyPrinting().create().toJson(rows), CREATE_NEW)
            }
        } finally {
            connection.disconnect()
            pauses.close()
            withContext(NonCancellable + Dispatchers.EDT) {
                lesson.removeLessonListener(listener)
                owned?.stop()
            }
            withContext(NonCancellable) { writeAction { XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(breakpoint) } }
        }
    }

    private suspend fun awaitDebugRegisters(project: Project, handler: ProcessHandler, expected: Map<String, Long>): List<String> {
        val view = lessonDebugRegisterView
        while (true) {
            val rows = withContext(Dispatchers.EDT) {
                val descriptor = RunContentManager.getInstance(project).findContentDescriptor(
                    DefaultDebugExecutor.getDebugExecutorInstance(), handler,
                ) ?: return@withContext emptyList()
                val ui = descriptor.runnerLayoutUi ?: return@withContext emptyList()
                val content = ui.findContent(VARIABLES_CONTENT) ?: ui.contents.singleOrNull { isRegisterCaptureTab(it.displayName) }
                    ?: return@withContext emptyList()
                if (!content.component.isShowing) {
                    ui.selectAndFocus(content, false, true)
                    return@withContext emptyList()
                }
                val trees = captureComponents(content.component).filterIsInstance<JTree>().filter { it.isShowing }.toList()
                val tree = selectDebuggerTree(trees, view)
                if (tree == null || !applyDebuggerTreeView(tree, view)) emptyList()
                else (0 until tree.rowCount).map { renderedDebuggerRow(tree, tree.getPathForRow(it)) }
            }
            if (expected.all { (register, value) -> lessonRegisterValue(rows, register) == value }) return rows
            delay(100)
        }
    }

    private suspend fun answer(
        project: Project, output: Path, id: String, actionId: String,
        wrong: String, feedback: String, correct: String,
    ) {
        val failure = AtomicReference<Throwable>()
        withContext(Dispatchers.EDT) {
            val action = checkNotNull(ActionManager.getInstance().getAction(actionId))
            val context = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build()
            val event = AnActionEvent.createEvent(action, context, null, ActionPlaces.MAIN_MENU, ActionUiKind.NONE, null)
            ApplicationManager.getApplication().invokeLater({
                try { ActionUtil.performAction(action, event) } catch (error: Throwable) { failure.set(error) }
            }, ModalityState.nonModal())
        }
        val dialog = awaitDialog(failure)
        onModalUi {
            visibleLessonButton(dialog.rootPane, wrong).doClick()
            visibleLessonButton(dialog.rootPane, "Check prediction").doClick()
        }
        awaitUi { lessonVisibleText(dialog.rootPane).contains(feedback) }
        captureComponent(dialog.rootPane, output, "$id-feedback", feedback)
        onModalUi {
            visibleLessonButton(dialog.rootPane, correct).doClick()
            visibleLessonButton(dialog.rootPane, "Check prediction").doClick()
        }
        awaitUi { !dialog.isShowing }
        failure.get()?.let { throw it }
    }

    private suspend fun awaitDialog(failure: AtomicReference<Throwable>, title: String = "Predict the register state"): JDialog {
        while (true) {
            failure.get()?.let { throw it }
            val dialog = onModalUi {
                Window.getWindows().filterIsInstance<JDialog>()
                    .singleOrNull { it.isShowing && it.title == title }
            }
            if (dialog != null) return dialog
            delay(100)
        }
    }

    private suspend fun awaitFrame(project: Project, text: String, source: Path) {
        while (true) {
            val ready = withContext(Dispatchers.EDT) {
                val frame = WindowManager.getInstance().getFrame(project)
                val editor = FileEditorManager.getInstance(project).selectedTextEditor
                val file = editor?.let { FileDocumentManager.getInstance().getFile(it.document) }
                frame?.isShowing == true && file?.toNioPath() == source && lessonVisibleText(frame.rootPane).contains(text)
            }
            if (ready) { delay(300); return }
            delay(100)
        }
    }

    private suspend fun awaitUi(predicate: () -> Boolean) {
        while (!onModalUi(predicate)) delay(100)
        delay(200)
    }

    private suspend fun captureFrame(project: Project, output: Path, id: String, text: String) {
        val root = withContext(Dispatchers.EDT) { checkNotNull(WindowManager.getInstance().getFrame(project)).rootPane }
        captureComponent(root, output, id, text)
    }

    private suspend fun captureComponent(root: JComponent, output: Path, id: String, text: String) {
        val bounds = onModalUi {
            check(lessonVisibleText(root).contains(text)) { "Expected lesson text is not visible: $text" }
            check(LafManager.getInstance().currentUIThemeLookAndFeel?.name == "IntelliJ Light")
            Rectangle(root.locationOnScreen, root.size).also {
                requireVisibleCaptureViewport(it, root.graphicsConfiguration.bounds)
            }
        }
        withContext(Dispatchers.IO) {
            val image = Robot().createScreenCapture(bounds)
            val png = output.resolve("$id.png")
            Files.newOutputStream(png, CREATE_NEW).use { check(ImageIO.write(image, "png", it)) }
            Files.writeString(output.resolve("$id.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
                "theme" to "IntelliJ Light", "assertionsPassed" to true, "observedText" to text,
                "width" to image.width, "height" to image.height, "pngSha256" to captureSha256(Files.readAllBytes(png)),
            )), CREATE_NEW)
        }
    }

    private suspend fun <T> onModalUi(action: () -> T): T =
        withContext(Dispatchers.EDT + ModalityState.any().asContextElement()) { action() }
}

internal fun lessonVisibleText(root: JComponent): String = captureComponents(root)
    .filter { it.isShowing }
    .mapNotNull {
        when (it) {
            is JTextComponent -> it.document.getText(0, it.document.length)
            is JLabel -> it.text
            is AbstractButton -> it.text
            else -> null
        }
    }.joinToString("\n")

internal fun visibleLessonButton(root: JComponent, text: String): AbstractButton =
    captureComponents(root).filterIsInstance<AbstractButton>().single { it.isShowing && it.text == text }

/** Read actual scalar register rendering; do not accept duplicate or ambiguous native rows. */
internal fun lessonRegisterValue(rows: List<String>, register: String): Long? {
    val matches = rows.filter { it.substringBefore(" = ").removeSuffix(" (written)") == register }
    check(matches.size <= 1) { "Ambiguous debugger register: $register" }
    val value = matches.singleOrNull()?.substringAfter(" = ")?.substringBefore(' ') ?: return null
    return if (value.startsWith("0x")) value.drop(2).toULongOrNull(16)?.toLong() else value.toLongOrNull()
}

internal val lessonDebugRegisterView = BookDebuggerTreeView(
    expand = listOf(listOf("Integer registers")), reveal = listOf("Integer registers", "\$t0"),
)
