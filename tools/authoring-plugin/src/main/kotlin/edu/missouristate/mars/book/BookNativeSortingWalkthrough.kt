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
internal suspend fun captureBookNativeSorting(
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
        predict("signed-order", "Which key moves left in the first exchange?", "The key -2", "bgec", "The key 2")
        predict("half-exchange", "Where does the algorithm retain the displaced key after the first store?", "Register t2", "sw")
        predict("sorted-prefix", "At outer index two, which words form the sorted prefix?", "The first two words", "bgec")
    }
    awaitText("Start a fresh Debug session for sorting-costs.asm.")
    selected(entry)
    captureSortingDebug(project, entry, ::record, awaitText, capture, plan.resume)
    if (!plan.resume) {
        predict("publication-count", "How many complete frames accompany seventeen exchanges?", "Nineteen frames", "balc")
    }
    awaitText("Run sorting-costs.asm using ordinary Run.")
    selected(entry)
    val run = launchBookProgram(project, entry, debug = false)
    try {
        run.started.await()
        check(run.exit.await() == 0 && run.stdout.toString().isEmpty())
        record("ordinary-run", mapOf("source" to archive.entry, "exitCode" to 0, "stdout" to ""))
    } finally { run.close() }
    capture("after-ordinary-run", "")
    if (!plan.resume) {
        predict("stable-groups", "Which negative prefix meets the partition contract?", "[-2, -4, -3, -1]", "bgec")
    }
    val fixture = plan.assessmentFixtures.single()
    val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(Path.of(fixture.path)) }
    check(captureSha256(bytes) == fixture.sha256) { "Changed private authoring fixture" }
    awaitText("Write partition_negative in independent/partition-negative.asm, replacing its break body.")
    val independent = directory.resolve(fixture.source)
    selected(independent)
    if (!plan.resume) {
        click("Check this program", false)
        capture("sorting-starter", "Public cases passed: 0 of 56.")
        withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
            val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
            check(FileDocumentManager.getInstance().getFile(editor.document)?.toNioPath() == independent)
            WriteCommandAction.runWriteCommandAction(project) { editor.document.setText(bytes.toString(Charsets.UTF_8)) }
            // Leave the edit unsaved: production checking must use the live document.
        }
    } else check(withContext(Dispatchers.IO) { Files.readString(independent) } == "# Recheck this edited independent source.\n" + bytes.toString(Charsets.UTF_8))
    click("Check this program", false)
    capture("sorting-passed", "The current source passes all fifty-six public partition cases.")
    record("independent", mapOf("source" to fixture.source, "solutionSha256" to fixture.sha256,
        "starterPassedPublicCases" to if (plan.resume) "not rerun" else "0 of 56", "completedPublicCases" to 56,
        "checkedUnsavedText" to !plan.resume))
    withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) { FileDocumentManager.getInstance().saveAllDocuments() }
    awaitText("Explain a result using its invariant, complete state and observation boundary.")
    while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
    capture("sorting-unit-complete", "Explain a result using its invariant, complete state and observation boundary.")
    if (!plan.resume) {
        for ((path, note) in listOf(entry to "Recheck sorting execution after this edit.",
            independent to "Recheck this edited independent source.")) {
            val file = withContext(Dispatchers.IO) { checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)) }
            withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
                val document = checkNotNull(FileDocumentManager.getInstance().getDocument(file))
                WriteCommandAction.runWriteCommandAction(project) { document.insertString(0, "# $note\n") }
                FileDocumentManager.getInstance().saveDocument(document)
            }
        }
        continueLesson()
        capture("sorting-source-invalidated", "Start a fresh Debug session for sorting-costs.asm.")
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
            "scope" to "Actual inline predictions, eight exchange/prefix/publication Debug boundaries, ordinary Run, direct function-memory checks and continuation. Bitmap inspection and human learner review are separate verification.",
        )), CREATE_NEW)
    }
}

private data class SortingPause(val label: String, val registers: Map<String, Long>, val nextText: String, val lineOffset: Int = 1)

private suspend fun captureSortingDebug(
    project: Project, entry: Path, record: suspend (String, Map<String, Any>) -> Unit,
    awaitText: suspend (String) -> Unit, capture: suspend (String, String) -> Unit, resumed: Boolean,
) {
    val stops = listOf(
        SortingPause("sort_ready", mapOf("\$s0" to 268501252L, "\$s1" to 8L, "\$sp" to 2147479536L),
            "Resume to the first insertion_compare."),
        SortingPause("insertion_compare", mapOf("\$t0" to 268501256L, "\$t1" to 0xfffffffffffffffeuL.toLong(), "\$t2" to 2L, "\$s0" to 268501252L, "\$s1" to 8L, "\$s2" to 1L, "\$s3" to 1L, "\$sp" to 2147479488L),
            "Resume to the second sw under insertion_compare_end, before it executes."),
        SortingPause("insertion_compare_end", mapOf("\$t0" to 268501256L, "\$t1" to 0xfffffffffffffffeuL.toLong(), "\$t2" to 2L, "\$s0" to 268501252L, "\$s1" to 8L, "\$s2" to 1L, "\$s3" to 1L, "\$sp" to 2147479488L),
            "Resume to insertion_exchanged.", lineOffset = 3),
        SortingPause("insertion_exchanged", mapOf("\$s0" to 268501252L, "\$s1" to 8L, "\$s2" to 1L, "\$s3" to 1L, "\$sp" to 2147479488L),
            "Resume to the next sort_ready."),
        SortingPause("sort_ready", mapOf("\$s0" to 268501252L, "\$s1" to 8L, "\$s2" to 1L, "\$s3" to 1L, "\$sp" to 2147479472L),
            "Resume to the next insertion_outer."),
        SortingPause("insertion_outer", mapOf("\$s0" to 268501252L, "\$s1" to 8L, "\$s2" to 2L, "\$s3" to 0L, "\$sp" to 2147479488L),
            "Resume to sort_end."),
        SortingPause("sort_end", mapOf("\$s0" to 268501252L, "\$s1" to 8L, "\$sp" to 2147479536L),
            "Resume to done before the exit selector is loaded."),
        SortingPause("done", mapOf("\$s0" to 268501252L, "\$s1" to 8L, "\$sp" to 2147479536L),
            if (resumed) "Run sorting-costs.asm using ordinary Run." else "How many complete frames accompany seventeen exchanges?"),
    )
    val lines = withContext(Dispatchers.IO) { Files.readAllLines(entry) }
    // Fixed line offsets bind this exact source fixture; production anchors still use PSI.
    fun line(stop: SortingPause) = lines.indexOfFirst { it.trim() == "${stop.label}:" }
        .also { check(it >= 0) } + stop.lineOffset
    check(lines[line(stops[2])].trim() == "sw \$t2, 0(\$t0)") { "Changed half-exchange fixture" }
    val file = withContext(Dispatchers.IO) { checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(entry)) }
    val type = XBreakpointType.EXTENSION_POINT_NAME.extensionList.filterIsInstance<XLineBreakpointType<*>>()
        .single { it.id == "MarsMipsLine" }
    val breakpoints = writeAction {
        mutableListOf(XDebuggerManager.getInstance(project).breakpointManager.addLineBreakpoint(type, file.url, line(stops.first()), null))
    }
    val launch = launchBookProgram(project, entry, debug = true)
    try {
        val handler = launch.started.await()
        val session = checkNotNull(launch.session)
        for ((index, stop) in stops.withIndex()) {
            if (index > 0) {
                // One temporary breakpoint at a time distinguishes repeated calls and loop visits.
                writeAction {
                    breakpoints.forEach { XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(it) }
                    breakpoints.clear()
                    breakpoints += XDebuggerManager.getInstance(project).breakpointManager.addLineBreakpoint(type, file.url, line(stop), null)
                }
                bookModalUi { session.resume() }
            }
            check(launch.pauses.receive() == line(stop)) { "Wrong sorting stop: ${stop.label}" }
            val registers = awaitBookDebugRegisters(project, handler, stop.registers)
            awaitText(stop.nextText)
            record("debug-sorting-$index", mapOf("source" to entry.toString(), "line" to line(stop) + 1,
                "renderedRegisters" to registers, "nativeNextStepObserved" to stop.nextText))
            capture("debug-sorting-$index", stop.nextText)
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
