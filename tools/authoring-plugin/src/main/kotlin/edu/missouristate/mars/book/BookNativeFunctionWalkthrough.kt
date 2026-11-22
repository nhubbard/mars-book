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
internal suspend fun captureBookNativeFunctions(
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
    if (plan.resume) continueLesson()
    else {
        predict("compact-link", "What does balc place in ra?", "The address of the next machine instruction", "balc",
            "The destination function’s address")
        predict("frame-size", "Why does this frame reserve thirty-two bytes for twenty-four bytes of saved values?",
            "To keep sp aligned at the inner calls", "daddiu")
        predict("result-lifetime", "Where does the worked pair routine keep the first result while reversing the second byte?", "s1", "move")
    }
    awaitText("Start a fresh Debug session for reverse-rows.asm.")
    selected(entry)
    captureFunctionDebug(project, entry, ::record, awaitText, capture, plan.resume)
    if (!plan.resume) {
        predict("released-frame", "What does releasing this frame mean?", "The callee has finished using those slots", "jrc")
        predict("exit-selector", "Where should you inspect the first reversal after running to exit?", "The first results word", "li")
    }
    awaitText("Run reverse-rows.asm using ordinary Run.")
    selected(entry)
    val run = launchBookProgram(project, entry, debug = false)
    try {
        run.started.await()
        check(run.exit.await() == 0 && run.stdout.toString().isEmpty())
        record("ordinary-run", mapOf("source" to archive.entry, "exitCode" to 0, "stdout" to ""))
    } finally { run.close() }
    val fixture = plan.assessmentFixtures.single()
    val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(Path.of(fixture.path)) }
    check(captureSha256(bytes) == fixture.sha256) { "Changed private authoring fixture" }
    awaitText("Write reverse_and_invert in independent/reverse-and-invert.asm, replacing its break body.")
    val independent = directory.resolve(fixture.source)
    selected(independent)
    if (!plan.resume) {
        click("Check this program", false)
        capture("function-starter", "Public cases passed: 0 of 3.")
        withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
            val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
            check(FileDocumentManager.getInstance().getFile(editor.document)?.toNioPath() == independent)
            WriteCommandAction.runWriteCommandAction(project) { editor.document.setText(bytes.toString(Charsets.UTF_8)) }
            // Leave the edit unsaved: production checking must use the live document.
        }
    } else check(withContext(Dispatchers.IO) { Files.readString(independent) } == "# Recheck this edited independent source.\n" + bytes.toString(Charsets.UTF_8))
    click("Check this program", false)
    capture("function-passed", "The current source passes all three public function cases.")
    record("independent", mapOf("source" to fixture.source, "solutionSha256" to fixture.sha256,
        "starterPassedPublicCases" to if (plan.resume) "not rerun" else "0 of 3", "completedPublicCases" to 3,
        "checkedUnsavedText" to !plan.resume))
    withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) { FileDocumentManager.getInstance().saveAllDocuments() }
    awaitText("Use your trace to explain a call as control flow plus an incoming continuation.")
    while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
    capture("function-unit-complete", "Use your trace to explain a call as control flow plus an incoming continuation.")
    if (!plan.resume) {
        for ((path, note) in listOf(entry to "Recheck function execution after this edit.",
            independent to "Recheck this edited independent source.")) {
            val file = withContext(Dispatchers.IO) { checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)) }
            withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
                val document = checkNotNull(FileDocumentManager.getInstance().getDocument(file))
                WriteCommandAction.runWriteCommandAction(project) { document.insertString(0, "# $note\n") }
                FileDocumentManager.getInstance().saveDocument(document)
            }
        }
        continueLesson()
        capture("function-source-invalidated", "Start a fresh Debug session for reverse-rows.asm.")
    }
    val digests = withContext(Dispatchers.IO) {
        listOf(archive.entry, fixture.source).associateWith { captureSha256(Files.readAllBytes(directory.resolve(it))) }
    }
    val running = bookModalUi { LessonManager.instance.lessonIsRunning() }
    withContext(Dispatchers.IO) {
        Files.writeString(output.resolve("complete.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
            "resumed" to plan.resume, "entryDigests" to mapOf(archive.sha256 to digests.getValue(archive.entry)),
            "sourceDigests" to digests, "observations" to receipts, "lessonRunningAtCaptureEnd" to running,
            "marsPluginArchiveSha256" to captureSha256(Files.readAllBytes(Path.of(checkNotNull(System.getProperty("mars.book.mars.archive"))))),
            "scope" to "Actual inline predictions, eight call/return Debug boundaries, ordinary Run, function-contract checking and continuation. Stack-memory inspection and human learner review are separate verification.",
        )), CREATE_NEW)
    }
}

private data class FunctionPause(
    val label: String, val registers: Map<String, Long>, val nextText: String,
    val stepInto: Boolean = false, val stepsOver: Int = 0,
)

private suspend fun captureFunctionDebug(
    project: Project, entry: Path, record: suspend (String, Map<String, Any>) -> Unit,
    awaitText: suspend (String) -> Unit, capture: suspend (String, String) -> Unit, resumed: Boolean,
) {
    val stops = listOf(
        FunctionPause("main_call", mapOf("\$a0" to 22L, "\$a1" to 129L, "\$s0" to 0x10010008L, "\$sp" to 0x7fffeff0L),
            "Step Into balc to reverse_pair."),
        FunctionPause("reverse_pair", mapOf("\$ra" to 0x400064L, "\$s0" to 0x10010008L, "\$sp" to 0x7fffeff0L),
            "Resume to pair_frame_ready, before move.", stepInto = true),
        FunctionPause("pair_frame_ready", mapOf("\$sp" to 0x7fffefd0L, "\$s0" to 0x10010008L, "\$s1" to 0L, "\$ra" to 0x400064L),
            "Resume to pair_first_ready."),
        FunctionPause("pair_first_ready", mapOf("\$v0" to 104L, "\$s0" to 129L, "\$sp" to 0x7fffefd0L, "\$ra" to 0x40008cL),
            "Step over the two moves to pair_second_call."),
        FunctionPause("pair_second_call", mapOf("\$s1" to 104L, "\$a0" to 129L, "\$s0" to 129L, "\$v0" to 104L),
            "Resume to pair_second_ready, before either result move.", stepsOver = 2),
        FunctionPause("pair_second_ready", mapOf("\$v0" to 129L, "\$s1" to 104L, "\$ra" to 0x400098L, "\$sp" to 0x7fffefd0L),
            "Resume to pair_return before jrc."),
        FunctionPause("pair_return", mapOf("\$v0" to 104L, "\$v1" to 129L, "\$s0" to 0x10010008L,
            "\$s1" to 0L, "\$sp" to 0x7fffeff0L, "\$ra" to 0x400064L), "Step Into the return to after_pair."),
        FunctionPause("after_pair", mapOf("\$v0" to 104L, "\$v1" to 129L, "\$s0" to 0x10010008L, "\$sp" to 0x7fffeff0L),
            if (resumed) "Run reverse-rows.asm using ordinary Run." else "What does releasing this frame mean?", stepInto = true),
    )
    val lines = withContext(Dispatchers.IO) { Files.readAllLines(entry) }
    fun line(label: String) = lines.indexOfFirst { it.trim() == "$label:" }.also { check(it >= 0) } + 1
    val file = withContext(Dispatchers.IO) { checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(entry)) }
    val type = XBreakpointType.EXTENSION_POINT_NAME.extensionList.filterIsInstance<XLineBreakpointType<*>>()
        .single { it.id == "MarsMipsLine" }
    val breakpoints = writeAction {
        listOf("main_call", "pair_frame_ready", "pair_first_ready", "pair_second_ready", "pair_return").map {
            XDebuggerManager.getInstance(project).breakpointManager.addLineBreakpoint(type, file.url, line(it), null)
        }.toMutableList()
    }
    val launch = launchBookProgram(project, entry, debug = true)
    try {
        val handler = launch.started.await()
        val session = checkNotNull(launch.session)
        for ((index, stop) in stops.withIndex()) {
            if (index > 0) {
                when {
                    stop.stepInto -> bookModalUi { session.stepInto() }
                    stop.stepsOver > 0 -> repeat(stop.stepsOver) {
                        bookModalUi { session.stepOver(false) }
                        if (it < stop.stepsOver - 1) launch.pauses.receive()
                    }
                    else -> bookModalUi { session.resume() }
                }
            }
            check(launch.pauses.receive() == line(stop.label)) { "Wrong function stop: ${stop.label}" }
            val registers = awaitBookDebugRegisters(project, handler, stop.registers)
            awaitText(stop.nextText)
            record("debug-functions-$index", mapOf("source" to entry.toString(), "line" to line(stop.label) + 1,
                "renderedRegisters" to registers, "nativeNextStepObserved" to stop.nextText))
            capture("debug-functions-$index", stop.nextText)
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
