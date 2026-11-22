package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindowManager
import javax.swing.JEditorPane
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import training.learn.lesson.LessonManager

/** Publication automation only: real student controls and execution, never progress setters. */
internal suspend fun captureBookNativeLocality(
    project: Project, plan: BookPackageImportPlan, output: Path,
    awaitText: suspend (String) -> Unit, capture: suspend (String, String) -> Unit,
    click: suspend (String, Boolean) -> Unit, focus: suspend (String, String) -> Unit,
) {
    val archive = plan.archives.single()
    val directory = archive.directory(Path.of(plan.projectRoot))
    val entry = directory.resolve(archive.entry)
    val receipts = mutableListOf<Map<String, Any>>()
    suspend fun record(id: String, facts: Map<String, Any>) {
        receipts += mapOf("id" to id) + facts
        withContext(Dispatchers.IO) {
            Files.writeString(output.resolve("$id-observation.json"), GsonBuilder().setPrettyPrinting().create().toJson(facts), CREATE_NEW)
        }
    }
    suspend fun continueLesson() = withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
        val action = checkNotNull(ActionManager.getInstance().getAction("Mars.Learning.ContinueDownloaded"))
        val event = AnActionEvent.createEvent(action, SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build(),
            null, ActionPlaces.MAIN_MENU, ActionUiKind.NONE, null)
        check(ActionUtil.updateAction(action, event).isPerformed && event.presentation.isEnabled)
        ActionUtil.performAction(action, event)
    }
    suspend fun selected(path: Path) {
        while (!bookModalUi {
            FileEditorManager.getInstance(project).selectedTextEditor?.document?.let {
                FileDocumentManager.getInstance().getFile(it)?.toNioPath() == path
            } == true
        }) delay(100)
    }
    suspend fun predict(id: String, question: String, answer: String, mnemonic: String, wrong: String? = null) {
        awaitText(question)
        selected(entry)
        focus(id, mnemonic)
        capture(id, question)
        if (wrong != null) click(wrong, true)
        click(answer, true)
        record(id, mapOf("actualInlineChoiceClicked" to answer, "wrongChoiceFirst" to (wrong ?: "none")))
    }
    val tiled = directory.resolve("worked/matrix-tiled.asm")
    if (plan.resume) continueLesson()
    else {
        predict("destination-width", "Where does source (0,1), containing one, go?", "Output index three", "sw", "Output index one")
        predict("destination-stride", "For three source rows, how far does the output cursor advance?", "Twelve bytes", "daddu")
        predict("cold-lines", "What happens on those first two data accesses?", "Both miss", "lw")
    }
    awaitText("Start a fresh Debug session for matrix-locality.asm.")
    selected(entry)
    captureLocalityDebug(project, entry, ::record, awaitText, capture, "ordinary",
        ordinaryLocalityStops(if (plan.resume) "Start a fresh Debug session for worked/matrix-tiled.asm." else "What are the first tile’s exclusive row and column ends?"))
    if (!plan.resume) {
        predict("clipped-ends", "What are the first tile’s exclusive row and column ends?", "Three and four", "addiu")
    }
    awaitText("Start a fresh Debug session for worked/matrix-tiled.asm.")
    selected(tiled)
    captureLocalityDebug(project, tiled, ::record, awaitText, capture, "tiled",
        tiledLocalityStops(if (plan.resume) "Run matrix-locality.asm using ordinary Run." else "What conclusion do these two measurements support?"))
    if (!plan.resume) {
        predict("measurement-scope", "What conclusion do these two measurements support?", "Tiling uses more instructions and fewer misses in this stated model", "addiu")
    }
    for ((id, path) in listOf("ordinary" to entry, "tiled" to tiled)) {
        val relative = if (id == "ordinary") archive.entry else "worked/matrix-tiled.asm"
        awaitText("Run $relative using ordinary Run.")
        selected(path)
        val run = launchBookProgram(project, path, debug = false)
        try {
            run.started.await()
            check(run.exit.await() == 0 && run.stdout.toString().isEmpty())
            record("$id-run", mapOf("source" to relative, "exitCode" to 0, "stdout" to ""))
        } finally { run.close() }
        capture("after-$id-run", "")
    }
    if (!plan.resume) {
        predict("rotation-direction", "Which output is the clockwise quarter-turn?", "Rows [3,0], [4,1], [5,2]", "sw")
    }
    val fixture = plan.assessmentFixtures.single()
    val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(Path.of(fixture.path)) }
    check(captureSha256(bytes) == fixture.sha256) { "Changed private authoring fixture" }
    awaitText("Implement rotate_clockwise in the independent source, replacing break.")
    val independent = directory.resolve(fixture.source)
    selected(independent)
    if (!plan.resume) {
        click("Check this program", false)
        capture("locality-starter", "Public cases passed: 0 of 22.")
        withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
            val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
            check(FileDocumentManager.getInstance().getFile(editor.document)?.toNioPath() == independent)
            WriteCommandAction.runWriteCommandAction(project) { editor.document.setText(bytes.toString(Charsets.UTF_8)) }
            // Leave the edit unsaved: production checking must use the live document.
        }
    } else check(withContext(Dispatchers.IO) { Files.readString(independent) } == "# Recheck this edited independent source.\n" + bytes.toString(Charsets.UTF_8))
    click("Check this program", false)
    capture("locality-passed", "The current source passes all twenty-two public rotation cases.")
    record("independent", mapOf("source" to fixture.source, "solutionSha256" to fixture.sha256,
        "starterPassedPublicCases" to if (plan.resume) "not rerun" else "0 of 22", "completedPublicCases" to 22,
        "checkedUnsavedText" to !plan.resume))
    awaitText("Check the same independent source as a complete program.")
    selected(independent)
    click("Check this program", false)
    capture("locality-exit-passed", "The independent source passes both the transfer/caller contract and complete silent execution.")
    record("caller-exit", mapOf("source" to fixture.source, "completedPublicCases" to 1))
    withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) { FileDocumentManager.getInstance().saveAllDocuments() }
    awaitText("Explain correctness using a coordinate mapping and bounded access trace.")
    while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
    capture("locality-unit-complete", "Explain correctness using a coordinate mapping and bounded access trace.")
    if (!plan.resume) {
        for ((path, note) in listOf(entry to "Recheck ordinary execution after this edit.",
            tiled to "Recheck tiled execution after this edit.",
            independent to "Recheck this edited independent source.")) {
            val file = withContext(Dispatchers.IO) { checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)) }
            withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
                val document = checkNotNull(FileDocumentManager.getInstance().getDocument(file))
                WriteCommandAction.runWriteCommandAction(project) { document.insertString(0, "# $note\n") }
                FileDocumentManager.getInstance().saveDocument(document)
            }
        }
        continueLesson()
        capture("locality-source-invalidated", "Start a fresh Debug session for matrix-locality.asm.")
    }
    val digests = withContext(Dispatchers.IO) {
        listOf(archive.entry, "worked/matrix-tiled.asm", fixture.source).associateWith { captureSha256(Files.readAllBytes(directory.resolve(it))) }
    }
    val running = bookModalUi { LessonManager.instance.lessonIsRunning() }
    withContext(Dispatchers.IO) {
        Files.writeString(output.resolve("complete.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
            "resumed" to plan.resume, "entryDigests" to mapOf(archive.sha256 to digests.getValue(archive.entry)),
            "sourceDigests" to digests, "observations" to receipts, "lessonRunningAtCaptureEnd" to running,
            "marsPluginArchiveSha256" to captureSha256(Files.readAllBytes(Path.of(checkNotNull(System.getProperty("mars.book.mars.archive"))))),
            "scope" to "Actual inline predictions, two four-stop address/tile Debug routes, cache controls, two ordinary Runs, mapped rotation and complete caller checks and continuation. Human learner review is separate verification.",
        )), CREATE_NEW)
    }
}

private data class LocalityPause(val label: String, val registers: Map<String, Long>, val nextText: String,
    val accesses: Long, val misses: Long)

private fun ordinaryLocalityStops(next: String) = listOf(
    LocalityPause("transpose_begin", mapOf("\$sp" to 2147479536L, "\$a0" to 268500992L, "\$a1" to 268502080L, "\$a2" to 3L, "\$a3" to 5L),
        "Resume to the first naive_cell, before its load.", 0L, 0L),
    LocalityPause("naive_cell", mapOf("\$sp" to 2147479536L, "\$t0" to 0L, "\$t1" to 0L, "\$t2" to 268500992L, "\$t3" to 268502080L, "\$t4" to 268502080L, "\$v0" to 15L, "\$v1" to 12L),
        "Resume to the next naive_cell.", 0L, 0L),
    LocalityPause("naive_cell", mapOf("\$sp" to 2147479536L, "\$t0" to 0L, "\$t1" to 1L, "\$t2" to 268500996L, "\$t3" to 268502080L, "\$t4" to 268502092L, "\$v0" to 15L, "\$v1" to 12L),
        "Resume to transpose_done before the caller stores copied.", 2L, 2L),
    LocalityPause("transpose_done", mapOf("\$sp" to 2147479536L, "\$v0" to 15L, "\$v1" to 12L, "\$t0" to 3L, "\$t1" to 5L),
        next, 30L, 8L),
)

private fun tiledLocalityStops(next: String) = listOf(
    LocalityPause("tile_begin", mapOf("\$sp" to 2147479536L, "\$a0" to 268500992L, "\$a1" to 268502080L, "\$a2" to 3L, "\$a3" to 5L, "\$t0" to 0L, "\$t1" to 0L, "\$v0" to 15L, "\$v1" to 12L),
        "Resume to tile_columns_ready.", 0L, 0L),
    LocalityPause("tile_columns_ready", mapOf("\$sp" to 2147479536L, "\$t0" to 0L, "\$t1" to 0L, "\$t2" to 3L, "\$t3" to 4L, "\$v0" to 15L, "\$v1" to 12L),
        "Resume to the next tile_begin.", 0L, 0L),
    LocalityPause("tile_begin", mapOf("\$sp" to 2147479536L, "\$t0" to 0L, "\$t1" to 4L, "\$t2" to 3L, "\$t3" to 4L, "\$t4" to 3L, "\$t5" to 4L, "\$v0" to 15L, "\$v1" to 12L),
        "Resume to transpose_done before the caller store.", 24L, 7L),
    LocalityPause("transpose_done", mapOf("\$sp" to 2147479536L, "\$t0" to 4L, "\$t1" to 8L, "\$t2" to 3L, "\$t3" to 5L, "\$t4" to 3L, "\$t5" to 5L, "\$v0" to 15L, "\$v1" to 12L),
        next, 30L, 8L),
)

private suspend fun captureLocalityDebug(
    project: Project, entry: Path, record: suspend (String, Map<String, Any>) -> Unit,
    awaitText: suspend (String) -> Unit, capture: suspend (String, String) -> Unit,
    prefix: String, stops: List<LocalityPause>,
) {
    val lines = withContext(Dispatchers.IO) { Files.readAllLines(entry) }
    fun line(label: String) = lines.indexOfFirst { it.trim() == "$label:" }.also { check(it >= 0) } + 1
    val file = withContext(Dispatchers.IO) { checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(entry)) }
    val type = XBreakpointType.EXTENSION_POINT_NAME.extensionList.filterIsInstance<XLineBreakpointType<*>>()
        .single { it.id == "MarsMipsLine" }
    val breakpoints = writeAction {
        mutableListOf(XDebuggerManager.getInstance(project).breakpointManager.addLineBreakpoint(type, file.url, line(stops.first().label), null))
    }
    val launch = launchBookProgram(project, entry, debug = true, name = "Book locality $prefix Debug")
    try {
        val handler = launch.started.await()
        val session = checkNotNull(launch.session)
        for ((index, stop) in stops.withIndex()) {
            if (index > 0) {
                // One temporary breakpoint at a time distinguishes repeated calls and loop visits.
                writeAction {
                    breakpoints.forEach { XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(it) }
                    breakpoints.clear()
                    breakpoints += XDebuggerManager.getInstance(project).breakpointManager.addLineBreakpoint(type, file.url, line(stop.label), null)
                }
                bookModalUi { session.resume() }
            }
            check(launch.pauses.receive() == line(stop.label)) { "Wrong Locality stop: ${stop.label}" }
            val registers = awaitBookDebugRegisters(project, handler, stop.registers)
            val cache = awaitLocalityCache(project, "Book locality $prefix Debug", stop.accesses, stop.misses)
            awaitText(stop.nextText)
            record("debug-$prefix-$index", mapOf("source" to entry.toString(), "line" to line(stop.label) + 1,
                "renderedRegisters" to registers, "cache" to cache, "nativeNextStepObserved" to stop.nextText))
            capture("debug-$prefix-$index", stop.nextText)
        }
        writeAction {
            breakpoints.forEach { XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(it) }
            breakpoints.clear()
        }
        bookModalUi { session.resume() }
        check(launch.exit.await() == 0 && launch.stdout.toString().isEmpty())
    } finally {
        withContext(NonCancellable + Dispatchers.EDT) {
            launch.session?.stop()
            writeAction { breakpoints.forEach { XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(it) } }
        }
        launch.close()
    }
}

private suspend fun awaitLocalityCache(project: Project, execution: String, accesses: Long, misses: Long): List<String> {
    while (true) {
        val rows = bookModalUi {
            val tool = ToolWindowManager.getInstance(project).getToolWindow("MARS Tools") ?: return@bookModalUi null
            if (!tool.isVisible) { tool.show(); return@bookModalUi null }
            val content = tool.contentManager.contents.singleOrNull { it.displayName == "Data Cache Simulator" } ?: return@bookModalUi null
            if (tool.contentManager.selectedContent != content) { tool.contentManager.setSelectedContent(content, false); return@bookModalUi null }
            val components = captureComponents(content.component).toList()
            if (selectBookToolExecution(components, execution) == null) return@bookModalUi null
            val text = components.filterIsInstance<JEditorPane>().map {
                it.document.getText(0, it.document.length).trim().replace(Regex("\\s+"), " ")
            }
            val expected = listOf("128 bytes | Accesses $accesses", "Hits ${accesses - misses} | Misses $misses")
            if (text.containsAll(expected)) expected else null
        }
        if (rows != null) return rows
        delay(100)
    }
}
