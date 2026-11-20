package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.debugger.ui.DebuggerContentInfo.VARIABLES_CONTENT
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.RunManager
import com.intellij.execution.ui.RunContentManager
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.ide.plugins.PluginDetailsService
import com.intellij.ide.ui.LafManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationsManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.util.ActionCallback
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
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XDebuggerUtil
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import java.awt.Color
import java.awt.Component
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Window
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.Locale
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import javax.imageio.ImageIO
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JTree
import javax.swing.JTable
import javax.swing.JTextField
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
        if (figure.mode != "debug") {
            captureBookPresentation(project, plan, figure, output, descriptor)
            return
        }
        val expectedBitmap = withContext(Dispatchers.IO) { figure.bitmapView?.load(Path.of(plan.projectRoot)) }
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
            check(manager.debugSessions.none { !it.debugProcess.processHandler.isProcessTerminated }) { "Use an idle, dedicated capture project" }
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
        val psi = readAction { checkNotNull(PsiDocumentManager.getInstance(project).getPsiFile(document)) }
        withContext(Dispatchers.EDT) { DaemonCodeAnalyzer.getInstance(project).restart(psi, "Book capture observation") }
        val labelLine = figure.checkpointLine(String(bytes, Charsets.UTF_8))
        val line = readAction {
            (labelLine until document.lineCount).first { type.canPutAt(source, it, project) }
        }
        val stepLines = readAction {
            figure.stepFrom.map { label ->
                val labelStart = figure.checkpointLine(String(bytes, Charsets.UTF_8), label)
                (labelStart until document.lineCount).first { type.canPutAt(source, it, project) }
            }
        }
        val reverseLine = readAction {
            figure.backstepFrom?.let { label ->
                val labelStart = figure.checkpointLine(String(bytes, Charsets.UTF_8), label)
                (labelStart until document.lineCount).first { type.canPutAt(source, it, project) }
            }
        }
        val breakpoint = writeAction {
            addBreakpoint(project, type, source, reverseLine ?: stepLines.firstOrNull() ?: line).also { breakpoint ->
                figure.breakpointCondition?.let { breakpoint.conditionExpression = XDebuggerUtil.getInstance().createExpression(it, null, null, EvaluationMode.EXPRESSION) }
            }
        }
        var inputDelivered = figure.standardInput == null
        val observedSteps = mutableListOf<Map<String, Any>>()
        var backstepCallback: ActionCallback? = null
        var observedBackstep: Map<String, Any>? = null
        var ownedSessionName: String? = null
        var lastObservedRows = emptyList<String>()
        var memoryAddressRequested = false
        var sourceAnalysisVerified = false
        val originalFiles = withContext(Dispatchers.EDT) { FileEditorManager.getInstance(project).openFiles.toSet() }
        var ownedBitmapFile: VirtualFile? = null
        var lastObservedTabs = emptyList<String>()
        try {
            withContext(Dispatchers.EDT) {
                val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
                val screen = frame.graphicsConfiguration.bounds
                require(figure.width <= screen.width && figure.height <= screen.height) { "Requested viewport exceeds its screen" }
                frame.setBounds(screen.x + (screen.width - figure.width) / 2, screen.y + (screen.height - figure.height) / 2, figure.width, figure.height)
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
            var reportedRows: List<String>? = null
            var reportedTabs: List<String>? = null
            while (true) {
                if (!inputDelivered) {
                    val inputProcess = withContext(Dispatchers.EDT) {
                        manager.currentSession?.takeIf { it.sessionName == ownedSessionName }
                            ?.debugProcess?.processHandler?.takeIf { it.isStartNotified && !it.isProcessTerminated }
                    }
                    if (inputProcess != null) {
                        withContext(Dispatchers.IO) { deliverBookStandardInput(inputProcess, checkNotNull(figure.standardInput)) }
                        inputDelivered = true
                    }
                }
                val ready = withContext(Dispatchers.EDT) {
                    check(document.modificationStamp == sourceStamp) { "Fixture edited during capture" }
                    if (analysisReady.get()) sourceAnalysisVerified = true
                    if (!analysisReady.get() && !(expectedBitmap != null && sourceAnalysisVerified)) {
                        if (expectedBitmap != null && FileEditorManager.getInstance(project).selectedTextEditor?.document != document) {
                            FileEditorManager.getInstance(project).requestOpenFile(source, FileEditorOpenRequest.withFocus(false).withReuseOpen(true))
                        }
                        return@withContext null
                    }
                    val session = manager.currentSession ?: return@withContext null
                    if (session.sessionName != ownedSessionName || !session.isSuspended) return@withContext null
                    check(!session.debugProcess.processHandler.isProcessTerminated) { "Debugger terminated before capture" }
                    val position = session.currentPosition ?: return@withContext null
                    if (position.file != source) return@withContext null
                    if (reverseLine != null) {
                        if (backstepCallback == null) {
                            if (position.line != reverseLine) return@withContext null
                            val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
                            val candidates = captureComponents(frame).filterIsInstance<ActionToolbar>().flatMap { toolbar ->
                                (toolbar.actions + toolbar.actionGroup.getChildren(null)).filter { it.templatePresentation.text == "Backstep Instruction" }.map { toolbar to it }
                            }.distinctBy { it.second }.toList()
                            val (toolbar, action) = candidates.singleOrNull() ?: return@withContext null
                            backstepCallback = ActionManager.getInstance().tryToExecute(action, null, toolbar.component, "MarsBookCapture", false)
                            observedBackstep = mapOf("fromCheckpoint" to checkNotNull(figure.backstepFrom), "sourceLine" to position.line + 1, "actionText" to "Backstep Instruction")
                            return@withContext null
                        }
                        check(!backstepCallback.isRejected) { "Native Backstep Instruction action was rejected" }
                        if (!backstepCallback.isDone) return@withContext null
                    }
                    if (observedSteps.size < stepLines.size) {
                        val index = observedSteps.size
                        if (position.line == stepLines[index]) {
                            session.stepOver(true)
                            observedSteps.add(mapOf("fromCheckpoint" to figure.stepFrom[index], "sourceLine" to position.line + 1))
                        }
                        return@withContext null
                    }
                    if (position.line != line) return@withContext null
                    val editors = FileEditorManager.getInstance(project)
                    if (expectedBitmap == null) {
                        val editor = editors.selectedTextEditor
                        if (editor == null || editor.document != document) {
                            editors.requestOpenFile(source, FileEditorOpenRequest.withFocus(false).withReuseOpen(true))
                            return@withContext null
                        }
                        if (visibleAnalysisIndicators(editor.component).isNotEmpty()) return@withContext null
                        editor.caretModel.moveToLogicalPosition(LogicalPosition(line, 0))
                        editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
                    }
                    val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
                    if (!frame.isActive || Window.getWindows().any { it is JDialog && it.isModal && it.isShowing }) return@withContext null
                    val balloons = captureBalloons(project)
                    if (balloons.isNotEmpty()) {
                        balloons.forEach { it.hide() }
                        return@withContext null
                    }
                    if (expectedBitmap != null) {
                        val bitmapFile = ownedBitmapFile ?: editors.openFiles.singleOrNull {
                            it !in originalFiles && it.name == "Bitmap Display — " + source.name
                        }?.also { ownedBitmapFile = it } ?: return@withContext null
                        if (bitmapFile !in editors.selectedFiles) {
                            editors.requestOpenFile(bitmapFile, FileEditorOpenRequest.withFocus(false).withReuseOpen(true))
                            return@withContext null
                        }
                        val windows = ToolWindowManager.getInstance(project)
                        val visible = listOf("Project", "MARS Tools", "Debug").mapNotNull(windows::getToolWindow).filter { it.isVisible }
                        if (visible.isNotEmpty()) {
                            visible.forEach { it.hide() }
                            return@withContext null
                        }
                        val canvas = selectedBitmapCanvas(project) ?: return@withContext null
                        if (observeBookBitmap(canvas, expectedBitmap) == null) return@withContext null
                        return@withContext emptyList<BookUiRow>()
                    }
                    figure.marsBotView?.let { view ->
                        if (visibleAnalysisIndicators(frame).isNotEmpty()) return@withContext null
                        val tool = ToolWindowManager.getInstance(project).getToolWindow("MARS Tools") ?: return@withContext null
                        if (!tool.isVisible) { tool.show(); return@withContext null }
                        val content = tool.contentManager.contents.singleOrNull { it.displayName == "MarsBot" } ?: return@withContext null
                        if (tool.contentManager.selectedContent != content) { tool.contentManager.setSelectedContent(content, false); return@withContext null }
                        ToolWindowManager.getInstance(project).getToolWindow("Debug")?.takeIf { it.isVisible }?.let { it.hide(); return@withContext null }
                        lastObservedRows = bookBotDiagnostics(content.component)
                        return@withContext observeBookMarsBot(content.component, view, "Book figure: " + figure.id)?.rows
                    }
                    figure.digitalLabView?.let { view ->
                        if (visibleAnalysisIndicators(frame).isNotEmpty()) return@withContext null
                        val tool = ToolWindowManager.getInstance(project).getToolWindow("MARS Tools") ?: return@withContext null
                        if (!tool.isVisible) { tool.show(); return@withContext null }
                        val content = tool.contentManager.contents.singleOrNull { it.displayName == "Digital Lab Sim" } ?: return@withContext null
                        if (tool.contentManager.selectedContent != content) { tool.contentManager.setSelectedContent(content, false); return@withContext null }
                        ToolWindowManager.getInstance(project).getToolWindow("Debug")?.takeIf { it.isVisible }?.let { it.hide(); return@withContext null }
                        lastObservedRows = bookLabDiagnostics(content.component)
                        return@withContext observeBookDigitalLab(content.component, view, "Book figure: " + figure.id)?.rows
                    }
                    val descriptor = RunContentManager.getInstance(project).findContentDescriptor(
                        DefaultDebugExecutor.getDebugExecutorInstance(), session.debugProcess.processHandler,
                    ) ?: return@withContext null
                    val ui = descriptor.runnerLayoutUi ?: return@withContext null
                    lastObservedTabs = ui.contents.map { it.displayName }
                    val content = if (figure.memoryView != null) ui.findContent("MarsMipsMemory")
                        else ui.findContent(VARIABLES_CONTENT) ?: ui.contents.singleOrNull { isRegisterCaptureTab(it.displayName) }
                    if (content == null) return@withContext null
                    if (!content.component.isShowing) {
                        ui.selectAndFocus(content, false, true)
                        return@withContext null
                    }
                    figure.memoryView?.let { view ->
                        if (!configureBottomCaptureHeight(content.component, view.minimumHeight)) return@withContext null
                        if (!memoryAddressRequested) {
                            val address = content.preferredFocusableComponent as? JTextField
                                ?: error("The native memory tab does not expose its address field")
                            if (!address.isEnabled) return@withContext null
                            address.text = view.address
                            address.postActionEvent()
                            memoryAddressRequested = true
                            return@withContext null
                        }
                    }
                    lastObservedRows = visibleRows(frame).map { it.text }
                    if (!figure.debuggerView.isEmpty) {
                        if (!configureBottomCaptureHeight(content.component, 400)) return@withContext null
                        val tree = selectDebuggerTree(captureComponents(frame).filterIsInstance<JTree>().filter { it.isShowing }.toList(), figure.debuggerView)
                            ?: return@withContext null
                        if (!applyDebuggerTreeView(tree, figure.debuggerView)) return@withContext null
                    }
                    val rows = visibleRows(frame)
                    lastObservedRows = rows.map { it.text }
                    if (figure.expectedRows.any { expected -> rows.count { expected.matches(it.text) } != 1 }) return@withContext null
                    rows
                }
                if (reportedRows != lastObservedRows || reportedTabs != lastObservedTabs) {
                    val progress = mapOf("figure" to figure.id, "observedDebuggerRows" to lastObservedRows, "observedDebuggerTabs" to lastObservedTabs)
                    withContext(Dispatchers.IO) {
                        Files.writeString(output.resolve(figure.id + "-progress.json"), GsonBuilder().setPrettyPrinting().create().toJson(progress))
                    }
                    reportedRows = lastObservedRows.toList()
                    reportedTabs = lastObservedTabs.toList()
                }
                if (ready == null && ((figure.digitalLabView != null && lastObservedRows.firstOrNull() == "Digital Lab diagnostics") || (figure.marsBotView != null && lastObservedRows.firstOrNull() == "MarsBot diagnostics"))) {
                    val diagnostic = output.resolve(figure.id + "-unaccepted-diagnostic.png")
                    if (!withContext(Dispatchers.IO) { Files.exists(diagnostic) }) {
                        val bounds = withContext(Dispatchers.EDT) {
                            val root = checkNotNull(WindowManager.getInstance().getFrame(project)).rootPane
                            Rectangle(root.locationOnScreen, root.size)
                        }
                        withContext(Dispatchers.IO) { Files.newOutputStream(diagnostic, CREATE_NEW).use { check(ImageIO.write(Robot().createScreenCapture(bounds), "png", it)) } }
                    }
                }
                val texts = ready?.map { it.text }
                stable = if (texts != null && texts == previous) stable + 1 else 0
                previous = texts
                if (ready != null && stable >= 2) {
                    val nativeCondition = withContext(Dispatchers.EDT) { breakpoint.conditionExpression?.expression }
                    check(nativeCondition == figure.breakpointCondition) { "Native breakpoint condition changed" }
                    captureReady(project, plan, figure, output, ready, line, sourceStamp, source.url, expectedBitmap, observedSteps.toList(), nativeCondition, observedBackstep?.plus("toSourceLine" to (line + 1)))
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
                    val editors = FileEditorManager.getInstance(project)
                    editors.openFiles.filter { it !in originalFiles && it.name == "Bitmap Display — " + source.name }.forEach(editors::closeFile)
                    writeAction { manager.breakpointManager.removeBreakpoint(breakpoint) }
                }
            }
            if (!project.isDisposed) withContext(NonCancellable) {
                withTimeout(10_000) {
                    while (withContext(Dispatchers.EDT) {
                        !project.isDisposed && manager.debugSessions.any { it.sessionName == ownedSessionName && !it.debugProcess.processHandler.isProcessTerminated }
                    }) delay(50)
                }
            }
        }
    }

    private fun selectedBitmapCanvas(project: Project): JComponent? = FileEditorManager.getInstance(project).selectedEditors
        .mapNotNull { it.preferredFocusedComponent }
        .singleOrNull { it.isShowing && it.accessibleContext?.accessibleName == "Bitmap Display" }

    private fun <P : XBreakpointProperties<*>> addBreakpoint(project: Project, type: XLineBreakpointType<P>, file: VirtualFile, line: Int) =
        XDebuggerManager.getInstance(project).breakpointManager.addLineBreakpoint(type, file.url, line, type.createBreakpointProperties(file, line))

    private fun captureBalloons(project: Project) = NotificationsManager.getNotificationsManager().let { manager ->
        (manager.getNotificationsOfType(Notification::class.java, project).toList() +
         manager.getNotificationsOfType(Notification::class.java, null).toList())
            .mapNotNull { it.balloon }.filterNot { it.isDisposed }.distinct()
    }

    private fun visibleRows(root: Component): List<BookUiRow> = captureComponents(root).filterIsInstance<JTree>().filter { it.isShowing }.flatMap { tree ->
        (0 until tree.rowCount).mapNotNull { row ->
            val bounds = tree.getRowBounds(row) ?: return@mapNotNull null
            val visible = bounds.intersection(tree.visibleRect)
            if (visible.isEmpty) return@mapNotNull null
            val path = tree.getPathForRow(row)
            val text = renderedDebuggerRow(tree, path)
            val screen = tree.locationOnScreen
            BookUiRow(text, Rectangle(screen.x + visible.x, screen.y + visible.y, visible.width, visible.height))
        }
    }.toList() + captureComponents(root).filterIsInstance<JTable>().filter { it.isShowing }.flatMap { table ->
        val screen = table.locationOnScreen
        renderedMemoryRows(table).map { row ->
            BookUiRow(row.text, Rectangle(screen.x + row.bounds.x, screen.y + row.bounds.y, row.bounds.width, row.bounds.height),
                row.cells.mapValues { (_, cell) -> Rectangle(screen.x + cell.x, screen.y + cell.y, cell.width, cell.height) })
        }
    }.toList()

    private suspend fun captureReady(project: Project, plan: BookCapturePlan, figure: BookCaptureFigure, output: Path, rows: List<BookUiRow>, line: Int, sourceStamp: Long, sourceUrl: String, expectedBitmap: BookExpectedBitmap?, observedSteps: List<Map<String, Any>>, nativeCondition: String?, observedBackstep: Map<String, Any>?) {
        val snapshot = withContext(Dispatchers.EDT) {
            val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
            val root = frame.rootPane
            val viewport = Rectangle(root.locationOnScreen, root.size)
            requireVisibleCaptureViewport(viewport, frame.graphicsConfiguration.bounds)
            val theme = checkNotNull(LafManager.getInstance().currentUIThemeLookAndFeel)
            check(theme.name == "IntelliJ Light" && !theme.isDark)
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            if (expectedBitmap == null) {
                checkNotNull(editor)
                check(editor.colorsScheme.defaultBackground == Color.WHITE)
                check(visibleAnalysisIndicators(editor.component).isEmpty()) { "Visible analysis indicator returned" }
            }
            check(captureBalloons(project).isEmpty()) { "Notification appeared before screenshot" }
            val callouts = figure.expectedRows.map { expected ->
                val row = rows.single { expected.matches(it.text) }
                mapOf("label" to expected.label, "box" to captureBox(expected.column?.let { checkNotNull(row.cells[it]) } ?: row.bounds, viewport),
                    "observedText" to row.text, "column" to expected.column)
            }.toMutableList()
            figure.marsBotView?.let { view ->
                val content = checkNotNull(ToolWindowManager.getInstance(project).getToolWindow("MARS Tools")).contentManager.selectedContent
                val bot = checkNotNull(observeBookMarsBot(checkNotNull(content).component, view, "Book figure: " + figure.id))
                callouts += bot.rows.map { row -> mapOf("label" to row.text, "observedText" to row.text, "box" to captureBox(row.bounds, viewport)) }
            }
            figure.digitalLabView?.let { view ->
                val content = checkNotNull(ToolWindowManager.getInstance(project).getToolWindow("MARS Tools")).contentManager.selectedContent
                val lab = checkNotNull(observeBookDigitalLab(checkNotNull(content).component, view, "Book figure: " + figure.id))
                callouts += lab.rows.map { row -> mapOf("label" to row.text, "observedText" to row.text, "box" to captureBox(row.bounds, viewport)) }
            }
            LearningUiHighlightingManager.clearHighlights()
            if (expectedBitmap == null) {
                checkNotNull(editor)
                val point = editor.logicalPositionToXY(LogicalPosition(line, 0))
                SwingUtilities.convertPointToScreen(point, editor.contentComponent)
                val visible = Rectangle(editor.contentComponent.locationOnScreen, editor.contentComponent.size)
                val end = editor.offsetToXY(editor.document.getLineEndOffset(line))
                SwingUtilities.convertPointToScreen(end, editor.contentComponent)
                val code = Rectangle(point.x, point.y, (end.x - point.x).coerceAtLeast(editor.lineHeight), editor.lineHeight).intersection(visible).intersection(viewport)
                callouts += mapOf("label" to "Next source instruction", "box" to captureBox(code, viewport))
            } else {
                val canvas = checkNotNull(selectedBitmapCanvas(project))
                val bitmap = checkNotNull(observeBookBitmap(canvas, expectedBitmap)) { "Native bitmap changed before capture" }
                val bounds = Rectangle(bitmap.bounds).apply { translate(canvas.locationOnScreen.x, canvas.locationOnScreen.y) }
                callouts += mapOf("label" to expectedBitmap.view.label, "box" to captureBox(bounds, viewport))
            }
            root.paintImmediately(root.bounds)
            Triple(viewport, callouts, rows.map { it.text })
        }
        // The platform's remote-driver screenshot transport is a test-process API. The installed
        // authoring plugin uses the same JDK screen-capture facility, limited to its own IDE root.
        val screenImages = withContext(Dispatchers.IO) { Robot().createMultiResolutionScreenCapture(snapshot.first).resolutionVariants }
        val image = screenImages.first() as BufferedImage
        var screenEvidence: BookBitmapScreenEvidence? = null
        var labEvidence: Map<String, Any>? = null
        var botEvidence: Map<String, Any>? = null
        withContext(Dispatchers.EDT) {
            val session = checkNotNull(XDebuggerManager.getInstance(project).currentSession)
            check(session.isSuspended && session.sessionName == "Book figure: " + figure.id)
            check(session.currentPosition?.file?.url == sourceUrl && session.currentPosition?.line == line)
            val document = checkNotNull(FileDocumentManager.getInstance().getDocument(checkNotNull(session.currentPosition).file))
            check(document.modificationStamp == sourceStamp) { "Fixture edited during screenshot" }
            if (expectedBitmap == null) {
                val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
                check(editor.document == document)
                check(visibleAnalysisIndicators(editor.component).isEmpty()) { "Analysis indicator appeared during screenshot" }
            } else {
                val canvas = checkNotNull(selectedBitmapCanvas(project))
                val observed = checkNotNull(observeBookBitmap(canvas, expectedBitmap)) { "Native bitmap changed during capture" }
                val relative = Rectangle(observed.bounds).apply {
                    translate(canvas.locationOnScreen.x - snapshot.first.x, canvas.locationOnScreen.y - snapshot.first.y)
                }
                val photograph = observed.copy(bounds = relative)
                val evidence = photograph.screenEvidence(image, expectedBitmap)
                screenEvidence = evidence
                if (evidence.maximumChannelError > expectedBitmap.view.screenChannelTolerance) {
                    val differences = expectedBitmap.colors.indices.mapNotNull { index ->
                        val point = photograph.center(index)
                        val actual = image.getRGB(point.x, point.y) and 0xffffff
                        if (actual == expectedBitmap.colors[index]) null else mapOf("index" to index, "x" to point.x, "y" to point.y,
                            "expected" to "%06x".format(expectedBitmap.colors[index]), "actual" to "%06x".format(actual))
                    }
                    val diagnostic = mapOf("bounds" to relative, "differentPixels" to differences.size, "samples" to differences.take(32),
                        "colorSpace" to image.colorModel.colorSpace.toString(), "colorModel" to image.colorModel.toString(), "callouts" to snapshot.second)
                    withContext(Dispatchers.IO) {
                        Files.newOutputStream(output.resolve(figure.id + "-failed.png"), CREATE_NEW).use { check(ImageIO.write(image, "png", it)) }
                        val native = screenImages.last() as BufferedImage
                        Files.newOutputStream(output.resolve(figure.id + "-failed-native.png"), CREATE_NEW).use { check(ImageIO.write(native, "png", it)) }
                        Files.writeString(output.resolve(figure.id + "-pixel-failure.json"), GsonBuilder().setPrettyPrinting().create().toJson(diagnostic), CREATE_NEW)
                    }
                    error("Captured pixels differ from the independently checked frame")
                }
            }
            figure.marsBotView?.let { view ->
                val content = checkNotNull(ToolWindowManager.getInstance(project).getToolWindow("MARS Tools")).contentManager.selectedContent
                val bot = checkNotNull(observeBookMarsBot(checkNotNull(content).component, view, "Book figure: " + figure.id))
                check(bot.rows.map { it.text } == snapshot.third) { "MarsBot changed during capture" }
                bot.rows.forEachIndexed { index, row -> check(captureBox(row.bounds, snapshot.first) == snapshot.second[index]["box"]) { "MarsBot bounds changed during capture" } }
                botEvidence = bot.board.screenEvidence(image, snapshot.first) + mapOf("selectedExecution" to bot.selectedExecution)
            }
            figure.digitalLabView?.let { view ->
                val content = checkNotNull(ToolWindowManager.getInstance(project).getToolWindow("MARS Tools")).contentManager.selectedContent
                val lab = checkNotNull(observeBookDigitalLab(checkNotNull(content).component, view, "Book figure: " + figure.id))
                check(lab.rows.map { it.text } == snapshot.third) { "Digital Lab changed during capture" }
                lab.rows.forEachIndexed { index, row -> check(captureBox(row.bounds, snapshot.first) == snapshot.second[index]["box"]) { "Digital Lab bounds changed during capture" } }
                labEvidence = mapOf("left" to view.left, "right" to view.right, "selectedExecution" to lab.selectedExecution,
                    "timerRemaining" to 0, "pressedKeys" to emptyList<String>(), "samples" to lab.screenEvidence(image, snapshot.first))
            }
            check(captureBalloons(project).isEmpty()) { "Notification appeared during screenshot" }
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
                "visibleAnalysisIndicators" to emptyList<String>(),
                "notificationBalloonsVisible" to 0,
                "standardInput" to figure.standardInput, "standardInputDelivered" to (figure.standardInput?.let { true }),
                "observedSteps" to observedSteps, "backstepFrom" to figure.backstepFrom, "observedBackstep" to observedBackstep,
                "marsBotView" to figure.marsBotView, "observedMarsBot" to botEvidence,
                "digitalLabView" to figure.digitalLabView, "observedDigitalLab" to labEvidence, "breakpointCondition" to nativeCondition,
                "debuggerView" to figure.debuggerView, "memoryView" to figure.memoryView, "bitmapView" to figure.bitmapView,
                "observedBitmap" to expectedBitmap?.let { mapOf("columns" to it.view.columns, "rows" to it.view.rows,
                    "address" to it.view.address, "rgbSha256" to it.view.rgbSha256, "verifiedPixels" to it.colors.size,
                    "exactTooltipPixels" to it.colors.size, "exactRenderedPixels" to it.colors.size,
                    "screenChannelTolerance" to it.view.screenChannelTolerance, "screen" to screenEvidence) },
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

/** Uses the same public input stream as IntelliJ's console worker; never close a live process's stdin. */
internal fun deliverBookStandardInput(process: ProcessHandler, input: String) {
    validateBookStandardInput(input)
    check(process.isStartNotified && !process.isProcessTerminated) { "Capture process is not running" }
    val stream = checkNotNull(process.processInput) { "Capture process has no standard input" }
    stream.write(input.toByteArray(Charsets.US_ASCII))
    stream.flush()
}
