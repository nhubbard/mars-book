package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.RunContentManager
import com.intellij.ide.DataManager
import com.intellij.ide.plugins.PluginDetailsService
import com.intellij.ide.ui.LafManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationsManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorOpenRequest
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.util.Key
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.xdebugger.XDebuggerManager
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Window
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import javax.swing.JDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Editor and completed-Run observations use native editor, execution and console contracts. */
internal suspend fun captureBookPresentation(project: Project, plan: BookCapturePlan, figure: BookCaptureFigure, output: Path, descriptor: PluginDescriptor) {
    val path = Path.of(plan.projectRoot).resolve(figure.source)
    val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(path) }
    check(captureSha256(bytes) == figure.sourceSha256)
    val analysis = AtomicBoolean(false)
    val process = AtomicReference<ProcessHandler>()
    val stdout = StringBuffer()
    val stderr = StringBuffer()
    val name = "Book figure: " + figure.id
    val connection = project.messageBus.connect(project)
    connection.subscribe(DaemonCodeAnalyzer.DAEMON_EVENT_TOPIC, object : DaemonCodeAnalyzer.DaemonListener {
        private fun matches(editors: Collection<FileEditor>) = editors.filterIsInstance<TextEditor>().any {
            captureFileMatches(FileDocumentManager.getInstance().getFile(it.editor.document), path)
        }
        override fun daemonStarting(fileEditors: Collection<FileEditor>) { if (matches(fileEditors)) analysis.set(false) }
        override fun daemonFinished(fileEditors: Collection<FileEditor>) { if (matches(fileEditors)) analysis.set(true) }
        override fun daemonCanceled(reason: String, fileEditors: Collection<FileEditor>) { if (matches(fileEditors)) analysis.set(false) }
    })
    connection.subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
        override fun processStarting(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
            if (executorId != DefaultRunExecutor.EXECUTOR_ID || env.runProfile.name != name) return
            check(process.compareAndSet(null, handler)) { "Duplicate owned Run process" }
            handler.addProcessListener(object : ProcessListener {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    if (outputType == ProcessOutputTypes.STDOUT) stdout.append(event.text)
                    if (outputType == ProcessOutputTypes.STDERR) stderr.append(event.text)
                    check(stdout.length + stderr.length <= 32768) { "Excessive capture output" }
                }
            })
        }
    })
    var inputSubmitted = false
    try {
        val sourceEditor = withContext(Dispatchers.EDT) {
            check(XDebuggerManager.getInstance(project).debugSessions.none { !it.debugProcess.processHandler.isProcessTerminated })
            val themes = LafManager.getInstance()
            themes.autodetect = false
            themes.setCurrentUIThemeLookAndFeel(themes.installedThemes.single { it.name == "IntelliJ Light" && !it.isDark })
            themes.updateUI()
            val colors = EditorColorsManager.getInstance()
            colors.setGlobalScheme(checkNotNull(colors.getScheme(EditorColorsManager.getDefaultSchemeName())))
            val file = checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path))
            val editors = FileEditorManager.getInstance(project)
            editors.openFile(file, FileEditorOpenRequest.defaults())
            PsiDocumentManager.getInstance(project).commitAllDocuments()
            val editor = checkNotNull(editors.selectedTextEditor)
            check(editor.document.text == String(bytes, Charsets.UTF_8))
            val windows = ToolWindowManager.getInstance(project)
            listOf("Project", "Debug", "Run", "MARS Tools").mapNotNull(windows::getToolWindow).forEach { it.hide() }
            editor.caretModel.moveToOffset(0)
            editor.scrollingModel.scrollVertically(0)
            val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
            val screen = frame.graphicsConfiguration.bounds
            require(figure.width <= screen.width && figure.height <= screen.height)
            frame.setBounds(screen.x + (screen.width - figure.width) / 2, screen.y + (screen.height - figure.height) / 2, figure.width, figure.height)
            frame.toFront()
            val psi = checkNotNull(PsiDocumentManager.getInstance(project).getPsiFile(editor.document))
            DaemonCodeAnalyzer.getInstance(project).restart(psi, "Book presentation capture")
            if (figure.mode == "run") {
                val runType = ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.single { it.id == "MarsMipsRun" }
                val settings = RunManager.getInstance(project).createConfiguration(name, runType.configurationFactories.single())
                configureBookCaptureLaunch(settings.configuration, path)
                ExecutionEnvironmentBuilder.create(DefaultRunExecutor.getRunExecutorInstance(), settings).buildAndExecute()
            }
            editor
        }
        val stamp = readAction { sourceEditor.document.modificationStamp }
        var stable = 0
        var previous: List<Map<String, Any>>? = null
        while (true) {
            val observed = withContext(Dispatchers.EDT) {
                check(sourceEditor.document.modificationStamp == stamp && sourceEditor.document.text == String(bytes, Charsets.UTF_8)) { "Fixture edited" }
                if (!analysis.get() || visibleAnalysisIndicators(sourceEditor.component).isNotEmpty()) return@withContext null
                val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
                if (!frame.isActive || Window.getWindows().any { it is JDialog && it.isModal && it.isShowing }) return@withContext null
                val balloons = bookPresentationBalloons(project)
                if (balloons.isNotEmpty()) { balloons.forEach { it.hide() }; return@withContext null }
                if (figure.mode == "editor") {
                    check(process.get() == null) { "An editor capture must not launch a process" }
                    if (FileEditorManager.getInstance(project).selectedTextEditor != sourceEditor) return@withContext null
                    figure.editorCallouts.map { callout -> bookEditorCallout(sourceEditor, callout) }
                } else {
                    val handler = process.get() ?: return@withContext null
                    val content = RunContentManager.getInstance(project).findContentDescriptor(DefaultRunExecutor.getRunExecutorInstance(), handler) ?: return@withContext null
                    val console = content.executionConsole as? ConsoleView ?: error("Run did not provide a native console")
                    if (!console.component.isShowing || console.hasDeferredOutput()) return@withContext null
                    if (!configureBottomCaptureHeight(console.component, 300)) return@withContext null
                    val context = DataManager.getInstance().getDataContext(console.preferredFocusableComponent ?: console.component)
                    val editor = CommonDataKeys.EDITOR.getData(context) ?: return@withContext null
                    val view = checkNotNull(figure.runView)
                    if (figure.standardInput != null && !inputSubmitted) {
                        check(!handler.isProcessTerminated) { "Run ended before input" }
                        if (stdout.toString() != view.prompt || !editor.document.text.endsWith(view.prompt)) return@withContext null
                        editor.caretModel.moveToOffset(editor.document.textLength)
                        for (character in checkNotNull(figure.standardInput)) {
                            if (character == '\n') EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_ENTER).execute(editor, null, context)
                            else TypedAction.getInstance().actionPerformed(editor, character, context)
                        }
                        inputSubmitted = true
                        return@withContext null
                    }
                    if (!handler.isProcessTerminated || console.hasDeferredOutput()) return@withContext null
                    check(handler.exitCode == 0 && stdout.toString() == view.stdout && stderr.isEmpty()) { "Run output or exit differs" }
                    if (!editor.document.text.startsWith(view.consoleText)) return@withContext null
                    view.callouts.map { callout -> bookEditorCallout(editor, callout) }
                }
            }
            stable = if (observed != null && observed == previous) stable + 1 else 0
            previous = observed
            if (observed != null && stable >= 2) {
                val viewport = withContext(Dispatchers.EDT) {
                    val root = checkNotNull(WindowManager.getInstance().getFrame(project)).rootPane
                    Rectangle(root.locationOnScreen, root.size)
                }
                val image = withContext(Dispatchers.IO) { Robot().createScreenCapture(viewport) }
                withContext(Dispatchers.EDT) {
                    val root = checkNotNull(WindowManager.getInstance().getFrame(project)).rootPane
                    check(Rectangle(root.locationOnScreen, root.size) == viewport && root.isShowing && checkNotNull(WindowManager.getInstance().getFrame(project)).isActive)
                    check(sourceEditor.document.modificationStamp == stamp && analysis.get() && bookPresentationBalloons(project).isEmpty())
                    val repeated = if (figure.mode == "editor") {
                        figure.editorCallouts.map { bookEditorCallout(sourceEditor, it) }
                    } else {
                        val handler = checkNotNull(process.get())
                        check(handler.isProcessTerminated && handler.exitCode == 0)
                        val console = checkNotNull(RunContentManager.getInstance(project).findContentDescriptor(DefaultRunExecutor.getRunExecutorInstance(), handler)).executionConsole as ConsoleView
                        check(!console.hasDeferredOutput() && console.component.isShowing)
                        val context = DataManager.getInstance().getDataContext(console.preferredFocusableComponent ?: console.component)
                        val editor = checkNotNull(CommonDataKeys.EDITOR.getData(context))
                        check(editor.document.text.startsWith(checkNotNull(figure.runView).consoleText))
                        figure.runView.callouts.map { bookEditorCallout(editor, it) }
                    }
                    check(repeated == observed) { "Observed text or native bounds changed during capture" }
                }
                val callouts = observed.map { row ->
                    @Suppress("UNCHECKED_CAST")
                    val bounds = row["screenBounds"] as Rectangle
                    row.filterKeys { it != "screenBounds" } + ("box" to captureBox(bounds, viewport))
                }
                withContext(Dispatchers.IO) {
                    val png = output.resolve(figure.id + ".png")
                    Files.newOutputStream(png, CREATE_NEW).use { check(ImageIO.write(image, "png", it)) }
                    val receipt = mapOf("figure" to figure.id, "mode" to figure.mode, "theme" to "IntelliJ Light", "assertionsPassed" to true,
                        "package" to plan.packageId, "packageVersion" to plan.packageVersion, "publicCommit" to plan.publicCommit,
                        "upstreamCommit" to plan.upstreamCommit, "upstreamDirtyFiles" to plan.upstreamDirtyFiles, "source" to figure.source,
                        "sourceSha256" to figure.sourceSha256, "pngSha256" to captureSha256(Files.readAllBytes(png)),
                        "viewport" to mapOf("width" to image.width, "height" to image.height), "callouts" to callouts,
                        "visibleAnalysisIndicators" to emptyList<String>(), "notificationBalloonsVisible" to 0,
                        "standardInput" to figure.standardInput, "inputMethod" to if (inputSubmitted) "Native console typing and Enter handlers" else null,
                        "stdout" to stdout.toString(), "stderr" to stderr.toString(), "exitCode" to process.get()?.exitCode,
                        "ideBuild" to ApplicationInfo.getInstance().build.asString(), "locale" to Locale.getDefault().toLanguageTag(), "scale" to 1,
                        "captureDriverSha256" to captureSha256(Files.readAllBytes(Path.of(descriptor.pluginPath.toString()).resolve("lib/mars-book-authoring-0.1.0.jar"))),
                        "marsPluginArchiveSha256" to captureSha256(Files.readAllBytes(Path.of(System.getProperty("mars.book.mars.archive")))),
                        "marsVersion" to PluginDetailsService.getInstance().findDetails(PluginId.getId("edu.missouristate.mars"))?.version)
                    Files.writeString(output.resolve(figure.id + ".json"), GsonBuilder().setPrettyPrinting().create().toJson(receipt), CREATE_NEW)
                }
                break
            }
            delay(200)
        }
    } catch (error: Exception) {
        withContext(NonCancellable + Dispatchers.IO) {
            Files.writeString(output.resolve(figure.id + "-failed.json"), GsonBuilder().create().toJson(mapOf("error" to error.stackTraceToString(), "stdout" to stdout.toString(), "stderr" to stderr.toString())), CREATE_NEW)
        }
        throw error
    } finally {
        withContext(NonCancellable + Dispatchers.EDT) {
            connection.disconnect()
            process.get()?.takeIf { !it.isProcessTerminated }?.destroyProcess()
        }
    }
}

private fun bookPresentationBalloons(project: Project) = NotificationsManager.getNotificationsManager().let { manager ->
    (manager.getNotificationsOfType(Notification::class.java, project).toList() + manager.getNotificationsOfType(Notification::class.java, null).toList())
        .mapNotNull { it.balloon }.filterNot { it.isDisposed }.distinct()
}

private fun bookEditorCallout(editor: Editor, callout: BookTextCallout): Map<String, Any> {
    val range = uniqueBookTextRange(editor.document.text, callout.text)
    val startLine = editor.document.getLineNumber(range.first)
    val endLine = editor.document.getLineNumber(range.last)
    val rectangles = (startLine..endLine).map { line ->
        val start = maxOf(range.first, editor.document.getLineStartOffset(line))
        val end = minOf(range.last + 1, editor.document.getLineEndOffset(line))
        val left = editor.offsetToXY(start)
        val right = editor.offsetToXY(end)
        Rectangle(left.x, left.y, maxOf(1, right.x - left.x), editor.lineHeight)
    }
    val bounds = rectangles.reduce { a, b -> a.union(b) }
    check(editor.contentComponent.visibleRect.contains(bounds)) { "Asserted text is clipped" }
    val screen = editor.contentComponent.locationOnScreen
    return mapOf("label" to callout.label, "observedText" to callout.text, "startOffset" to range.first, "endOffset" to range.last + 1,
        "screenBounds" to Rectangle(bounds.x + screen.x, bounds.y + screen.y, bounds.width, bounds.height))
}
