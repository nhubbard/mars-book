package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.ide.plugins.PluginDetailsService
import com.intellij.ide.ui.LafManager
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.extensions.PluginAware
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorOpenRequest
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.SimpleColoredComponent
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Window
import java.util.concurrent.atomic.AtomicBoolean
import java.util.Locale
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import javax.imageio.ImageIO
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.SwingUtilities
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.jdom.Element
import training.ui.LearningUiHighlightingManager

/** Opt-in batch driver for a dedicated authoring IDE. No action runs in an ordinary student launch. */
class BookCaptureActivity : ProjectActivity, PluginAware {
    private lateinit var descriptor: PluginDescriptor

    override fun setPluginDescriptor(pluginDescriptor: PluginDescriptor) {
        descriptor = pluginDescriptor
    }

    override suspend fun execute(project: Project) {
        val location = System.getProperty("mars.book.capture.plan") ?: return
        val plan = withContext(Dispatchers.IO) {
            GsonBuilder().create().fromJson(Files.readString(Path.of(location)), BookCapturePlan::class.java)
        }
        plan.validate()
        if (Path.of(checkNotNull(project.basePath)).toRealPath() != Path.of(plan.projectRoot).toRealPath()) return
        val output = Path.of(plan.outputDirectory)
        withContext(Dispatchers.IO) {
            Files.createDirectories(output)
            Files.list(output).use { check(it.findAny().isEmpty) { "Capture output directory must be empty" } }
        }
        try {
            withTimeout(600_000) {
                for (figure in plan.figures) withTimeout(180_000) { capture(project, plan, figure, output) }
            }
            withContext(Dispatchers.IO) { Files.writeString(output.resolve("batch-complete.json"), "{\"figures\":${plan.figures.size}}\n", CREATE_NEW) }
        } catch (error: Exception) {
            withContext(Dispatchers.IO) {
                Files.writeString(output.resolve("batch-failed.txt"), error.stackTraceToString(), CREATE_NEW)
            }
            throw error
        }
    }

    private suspend fun capture(project: Project, plan: BookCapturePlan, figure: BookCaptureFigure, output: Path) {
        val path = Path.of(plan.projectRoot).resolve(figure.source).normalize()
        val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(path) }
        check(captureSha256(bytes) == figure.sourceSha256) { "Fixture changed: ${figure.id}" }
        check(!Files.exists(output.resolve(figure.id + ".png")) && !Files.exists(output.resolve(figure.id + ".json"))) { "Existing capture refused" }
        val manager = XDebuggerManager.getInstance(project)
        val analysisReady = AtomicBoolean(false)
        val analysisConnection = project.messageBus.connect(project)
        analysisConnection.subscribe(DaemonCodeAnalyzer.DAEMON_EVENT_TOPIC, object : DaemonCodeAnalyzer.DaemonListener {
            private fun containsSource(editors: Collection<FileEditor>) = editors.filterIsInstance<TextEditor>().any {
                captureFileMatches(FileDocumentManager.getInstance().getFile(it.editor.document), path)
            }
            override fun daemonStarting(fileEditors: Collection<FileEditor>) {
                if (containsSource(fileEditors)) analysisReady.set(false)
            }
            override fun daemonFinished(fileEditors: Collection<FileEditor>) {
                if (containsSource(fileEditors)) analysisReady.set(true)
            }
            override fun daemonCanceled(reason: String, fileEditors: Collection<FileEditor>) {
                if (containsSource(fileEditors)) analysisReady.set(false)
            }
        })
        val source = withContext(Dispatchers.EDT) {
            check(manager.debugSessions.isEmpty()) { "Use an idle, dedicated capture project" }
            val themes = LafManager.getInstance()
            val light = themes.installedThemes.single { it.name == "IntelliJ Light" && !it.isDark }
            themes.autodetect = false
            themes.setCurrentUIThemeLookAndFeel(light)
            themes.updateUI()
            val colors = EditorColorsManager.getInstance()
            colors.setGlobalScheme(checkNotNull(colors.getScheme(EditorColorsManager.getDefaultSchemeName())))
            val file = checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path))
            FileEditorManager.getInstance(project).openFile(file, FileEditorOpenRequest.defaults())
            PsiDocumentManager.getInstance(project).commitAllDocuments()
            ToolWindowManager.getInstance(project).getToolWindow("Project")?.hide()
            file
        }
        val type = XBreakpointType.EXTENSION_POINT_NAME.extensionList.filterIsInstance<XLineBreakpointType<*>>()
            .single { it.id == "MarsMipsLine" }
        val document = readAction { checkNotNull(FileDocumentManager.getInstance().getDocument(source)) }
        val sourceStamp = readAction {
            check(document.text == String(bytes, Charsets.UTF_8)) { "The editor differs from the fixture" }
            document.modificationStamp
        }
        val labelLine = figure.checkpointLine(String(bytes, Charsets.UTF_8))
        val line = readAction {
            (labelLine until document.lineCount).first { type.canPutAt(source, it, project) }
        }
        val breakpoint = writeAction { addBreakpoint(project, type, source, line) }
        var ownedSessionName: String? = null
        var lastObservedRows = emptyList<String>()
        try {
            withContext(Dispatchers.EDT) {
                val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
                frame.setSize(figure.width, figure.height)
                frame.toFront()
                val runManager = RunManager.getInstance(project)
                val runType = ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.single { it.id == "MarsMipsRun" }
                val name = "Book figure: " + figure.id
                val settings = runManager.createConfiguration(name, runType.configurationFactories.single())
                val configuration = settings.configuration
                val state = Element("configuration").setAttribute("marsExecutionDefaults", "1")
                state.addContent(Element("option").setAttribute("name", "sourcePath").setAttribute("value", path.toString()))
                state.addContent(Element("option").setAttribute("name", "workingDirectory").setAttribute("value", path.parent.toString()))
                configuration.readExternal(state)
                configuration.checkConfiguration()
                ownedSessionName = name
                ExecutionEnvironmentBuilder.create(DefaultDebugExecutor.getDebugExecutorInstance(), settings).buildAndExecute()
            }
            var previous: List<String>? = null
            var stable = 0
            while (true) {
                val ready = withContext(Dispatchers.EDT) {
                    check(document.modificationStamp == sourceStamp) { "Fixture edited during capture" }
                    if (!analysisReady.get()) return@withContext null
                    val session = manager.currentSession ?: return@withContext null
                    if (session.sessionName != ownedSessionName || !session.isSuspended) return@withContext null
                    check(!session.debugProcess.processHandler.isProcessTerminated) { "Debugger terminated before capture" }
                    val position = session.currentPosition ?: return@withContext null
                    if (position.file != source || position.line != line) return@withContext null
                    val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@withContext null
                    if (editor.document != document) return@withContext null
                    editor.caretModel.moveToLogicalPosition(LogicalPosition(line, 0))
                    editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
                    val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
                    if (!frame.isActive || Window.getWindows().any { it is JDialog && it.isModal && it.isShowing }) return@withContext null
                    val rows = visibleRows(frame)
                    lastObservedRows = rows.map { it.text }
                    if (figure.expectedRows.any { expected -> rows.count { expected.matches(it.text) } != 1 }) return@withContext null
                    rows
                }
                val texts = ready?.map { it.text }
                stable = if (texts != null && texts == previous) stable + 1 else 0
                previous = texts
                if (ready != null && stable >= 2) {
                    captureReady(project, plan, figure, output, ready, line, sourceStamp, source.url)
                    break
                }
                delay(200)
            }
        } catch (error: Exception) {
            withContext(NonCancellable + Dispatchers.IO) {
                val diagnostic = mapOf("figure" to figure.id, "assertionsPassed" to false,
                                       "lastObservedDebuggerRows" to lastObservedRows, "error" to error.stackTraceToString())
                Files.writeString(output.resolve(figure.id + "-failed.json"), GsonBuilder().setPrettyPrinting().create().toJson(diagnostic), CREATE_NEW)
            }
            throw error
        } finally {
            withContext(NonCancellable + Dispatchers.EDT) {
                analysisConnection.disconnect()
                if (!project.isDisposed) {
                    manager.debugSessions.filter { it.sessionName == ownedSessionName }.forEach { it.stop() }
                    LearningUiHighlightingManager.clearHighlights()
                    writeAction { manager.breakpointManager.removeBreakpoint(breakpoint) }
                }
            }
        }
    }

    private fun <P : XBreakpointProperties<*>> addBreakpoint(project: Project, type: XLineBreakpointType<P>, file: VirtualFile, line: Int) =
        XDebuggerManager.getInstance(project).breakpointManager.addLineBreakpoint(type, file.url, line, type.createBreakpointProperties(file, line))

    private data class UiRow(val text: String, val bounds: Rectangle, val tree: JTree)

    private fun visibleRows(root: Component): List<UiRow> = components(root).filterIsInstance<JTree>().filter { it.isShowing }.flatMap { tree ->
        (0 until tree.rowCount).mapNotNull { row ->
            val bounds = tree.getRowBounds(row) ?: return@mapNotNull null
            val visible = bounds.intersection(tree.visibleRect)
            if (visible.isEmpty) return@mapNotNull null
            val path = tree.getPathForRow(row)
            val renderer = tree.cellRenderer.getTreeCellRendererComponent(tree, path.lastPathComponent, tree.isRowSelected(row), tree.isExpanded(row), tree.model.isLeaf(path.lastPathComponent), row, false)
            val text = when (renderer) {
                is SimpleColoredComponent -> renderer.getCharSequence(false).toString()
                is JLabel -> renderer.text
                else -> renderer.accessibleContext?.accessibleName.orEmpty()
            }
            val screen = tree.locationOnScreen
            UiRow(text, Rectangle(screen.x + visible.x, screen.y + visible.y, visible.width, visible.height), tree)
        }
    }.toList()

    private fun components(root: Component): Sequence<Component> = sequence {
        yield(root)
        if (root is Container) root.components.forEach { yieldAll(components(it)) }
    }

    private suspend fun captureReady(project: Project, plan: BookCapturePlan, figure: BookCaptureFigure, output: Path, rows: List<UiRow>, line: Int, sourceStamp: Long, sourceUrl: String) {
        val snapshot = withContext(Dispatchers.EDT) {
            val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
            val root = frame.rootPane
            val viewport = Rectangle(root.locationOnScreen, root.size)
            val theme = checkNotNull(LafManager.getInstance().currentUIThemeLookAndFeel)
            check(theme.name == "IntelliJ Light" && !theme.isDark)
            val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
            check(editor.colorsScheme.defaultBackground == Color.WHITE)
            val callouts = figure.expectedRows.map { expected ->
                val row = rows.single { expected.matches(it.text) }
                LearningUiHighlightingManager.highlightComponent(row.tree, LearningUiHighlightingManager.HighlightingOptions(usePulsation = false))
                mapOf("label" to expected.label, "box" to captureBox(row.bounds, viewport), "observedText" to row.text)
            }.toMutableList()
            LearningUiHighlightingManager.clearHighlights()
            val point = editor.logicalPositionToXY(LogicalPosition(line, 0))
            SwingUtilities.convertPointToScreen(point, editor.contentComponent)
            val visible = Rectangle(editor.contentComponent.locationOnScreen, editor.contentComponent.size)
            val code = Rectangle(visible.x, point.y, visible.width, editor.lineHeight).intersection(viewport)
            callouts += mapOf("label" to "Next source instruction", "box" to captureBox(code, viewport))
            root.paintImmediately(root.bounds)
            Triple(viewport, callouts, rows.map { it.text })
        }
        // The platform's remote-driver screenshot transport is a test-process API. The installed
        // authoring plugin uses the same JDK screen-capture facility, limited to its own IDE root.
        val image = withContext(Dispatchers.IO) { Robot().createScreenCapture(snapshot.first) }
        withContext(Dispatchers.EDT) {
            val session = checkNotNull(XDebuggerManager.getInstance(project).currentSession)
            check(session.isSuspended && session.sessionName == "Book figure: " + figure.id)
            check(session.currentPosition?.file?.url == sourceUrl && session.currentPosition?.line == line)
            val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
            check(editor.document.modificationStamp == sourceStamp) { "Fixture edited during screenshot" }
            val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
            check(frame.isActive && Rectangle(frame.rootPane.locationOnScreen, frame.rootPane.size) == snapshot.first) { "Capture window changed" }
        }
        val png = output.resolve(figure.id + ".png")
        withContext(Dispatchers.IO) {
            Files.newOutputStream(png, CREATE_NEW).use { check(ImageIO.write(image, "png", it)) }
            val receipt = mapOf(
                "figure" to figure.id, "theme" to "IntelliJ Light", "assertionsPassed" to true,
                "package" to plan.packageId, "packageVersion" to plan.packageVersion,
                "publicCommit" to plan.publicCommit, "upstreamCommit" to plan.upstreamCommit,
                "upstreamDirtyFiles" to plan.upstreamDirtyFiles,
                "pngSha256" to captureSha256(Files.readAllBytes(png)),
                "viewport" to mapOf("width" to image.width, "height" to image.height),
                "callouts" to snapshot.second, "observedDebuggerRows" to snapshot.third,
                "sourceSha256" to figure.sourceSha256, "source" to figure.source,
                "checkpoint" to figure.checkpoint, "sourceLine" to line + 1,
                "ideBuild" to ApplicationInfo.getInstance().build.asString(),
                "marsVersion" to PluginDetailsService.getInstance().findDetails(PluginId.getId("edu.missouristate.mars"))?.version,
                "captureDriverVersion" to "0.1.0",
                "captureDriverSha256" to Files.list(descriptor.pluginPath.resolve("lib")).use { paths ->
                    captureSha256(Files.readAllBytes(paths.filter { it.fileName.toString().startsWith("mars-book-authoring-") && it.fileName.toString().endsWith(".jar") }.toList().single()))
                },
                "marsPluginArchiveSha256" to captureSha256(Files.readAllBytes(Path.of(checkNotNull(System.getProperty("mars.book.mars.archive"))))),
                "locale" to Locale.getDefault().toLanguageTag(), "scale" to 1,
            )
            Files.writeString(output.resolve(figure.id + ".json"), GsonBuilder().setPrettyPrinting().create().toJson(receipt) + "\n", CREATE_NEW)
        }
    }
}
