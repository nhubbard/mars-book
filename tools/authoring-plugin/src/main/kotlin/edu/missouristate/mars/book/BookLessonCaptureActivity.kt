package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
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
import com.intellij.openapi.startup.ProjectActivity
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
) {
    fun validate() {
        require(Path.of(projectRoot).isAbsolute && Path.of(outputDirectory).isAbsolute)
        require(Path.of(projectRoot).normalize() != Path.of(outputDirectory).normalize())
        require(sourceSha256.matches(Regex("[a-f0-9]{64}")))
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
            withTimeout(180_000) { capture(project, plan, output) }
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
        val receipt = mapOf(
            "scope" to if (plan.captureDebug) "Native prediction, Run and Debug observations; diagnostic exercise and independent writing remain incomplete" else if (plan.captureRun) "Native prediction and Run segments; Debug and independent implementation remain separate work" else "Native prediction segment only",
            "nativePredictionPassesObserved" to predictionPasses.get(),
            "nativeRunPassedObserved" to plan.captureRun,
            "nativeDebugPassedObserved" to plan.captureDebug,
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

    private suspend fun captureRun(project: Project, output: Path, source: Path) {
        val passed = CompletableDeferred<Unit>()
        val listener = object : LessonListener {
            override fun lessonPassed(lesson: Lesson) { passed.complete(Unit) }
        }
        val lesson = withContext(Dispatchers.EDT) {
            CourseManager.instance.modules.single { it.id == "mars.console" }.lessons.single { it.id == "mars.console.run" }.also {
                it.addLessonListener(listener)
                CourseManager.instance.openLesson(project, it, LessonStartingWay.LEARN_TAB, forceStartLesson = true)
            }
        }
        val connection = project.messageBus.connect()
        var owned: ProcessHandler? = null
        try {
            awaitFrame(project, "Run first-program.asm using the normal MARS Run", source)
            captureFrame(project, output, "run-ready", "Run first-program.asm using the normal MARS Run")
            val process = CompletableDeferred<ProcessHandler>()
            val exit = CompletableDeferred<Int>()
            val transcript = StringBuilder()
            val name = "Book lesson Run observation"
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
                Files.writeString(output.resolve("run-transcript.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
                    "exitCode" to 0, "stdin" to "20\n", "stdout" to observed,
                    "scope" to "Actual Run only; native lesson completion must also be observed",
                )), CREATE_NEW)
            }
            passed.await()
            awaitFrame(project, "The console result agrees with the prediction", source)
            captureFrame(project, output, "run-complete", "The console result agrees with the prediction")
        } finally {
            connection.disconnect()
            withContext(NonCancellable + Dispatchers.EDT) {
                lesson.removeLessonListener(listener)
                owned?.takeUnless { it.isProcessTerminated }?.destroyProcess()
            }
        }
    }

    private suspend fun captureDebug(project: Project, output: Path, source: Path) {
        val passed = CompletableDeferred<Unit>()
        val listener = object : LessonListener {
            override fun lessonPassed(lesson: Lesson) { passed.complete(Unit) }
        }
        val lesson = withContext(Dispatchers.EDT) {
            CourseManager.instance.modules.single { it.id == "mars.console" }.lessons.single { it.id == "mars.console.debug" }.also {
                it.addLessonListener(listener)
                CourseManager.instance.openLesson(project, it, LessonStartingWay.LEARN_TAB, forceStartLesson = true)
            }
        }
        awaitFrame(project, "Set a breakpoint on addu", source)
        val file = withContext(Dispatchers.EDT) { checkNotNull(LocalFileSystem.getInstance().findFileByNioFile(source)) }
        val line = withContext(Dispatchers.IO) { Files.readString(source).lines().indexOfFirst { it.trim().startsWith("addu ") } }
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
            val name = "Book lesson Debug observation"
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
            for ((index, expected) in listOf(0L, 40L, 41L).withIndex()) {
                val sourceLine = pauses.receive()
                check(sourceLine >= line)
                val text = when (index) {
                    0 -> "Set a breakpoint on addu"
                    1 -> "The first addition produced 40"
                    else -> "You have observed the input"
                }
                if (index == 2) passed.await()
                awaitFrame(project, text, source)
                val observed = awaitDebugRegisters(project, expected)
                rows += mapOf("sourceLine" to sourceLine + 1, "input" to 20, "result" to expected, "renderedRows" to observed)
                captureFrame(project, output, "debug-" + listOf("before-add", "after-add", "after-one")[index], text)
                if (index < 2) withContext(Dispatchers.EDT) { session.stepOver(false) }
            }
            withContext(Dispatchers.IO) {
                Files.writeString(output.resolve("debug-observations.json"), GsonBuilder().setPrettyPrinting().create().toJson(rows), CREATE_NEW)
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

    private suspend fun awaitDebugRegisters(project: Project, result: Long): List<String> {
        val view = lessonDebugRegisterView
        while (true) {
            val rows = withContext(Dispatchers.EDT) {
                val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
                val trees = captureComponents(frame.rootPane).filterIsInstance<JTree>().filter { it.isShowing }.toList()
                val tree = selectDebuggerTree(trees, view)
                if (tree == null || !applyDebuggerTreeView(tree, view)) emptyList()
                else (0 until tree.rowCount).map { renderedDebuggerRow(tree, tree.getPathForRow(it)) }
            }
            if (lessonRegisterValue(rows, "\$v0") == 20L && lessonRegisterValue(rows, "\$t0") == result) return rows
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

    private suspend fun awaitDialog(failure: AtomicReference<Throwable>): JDialog {
        while (true) {
            failure.get()?.let { throw it }
            val dialog = onModalUi {
                Window.getWindows().filterIsInstance<JDialog>()
                    .singleOrNull { it.isShowing && it.title == "Predict the register state" }
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
