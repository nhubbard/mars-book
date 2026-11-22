package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.EDT
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
import javax.swing.JTable
import javax.swing.JTextField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import training.learn.lesson.LessonManager

/** Authoring-only driver. Uses actual UI controls and public debugger observations, never progress setters. */
internal suspend fun captureBookNativeMemory(
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
    suspend fun continueLesson() = bookModalUi {
        val action = checkNotNull(ActionManager.getInstance().getAction("Mars.Learning.ContinueDownloaded"))
        val event = AnActionEvent.createEvent(action, SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build(),
            null, ActionPlaces.MAIN_MENU, ActionUiKind.NONE, null)
        check(ActionUtil.updateAction(action, event).isPerformed && event.presentation.isEnabled)
        ActionUtil.performAction(action, event)
    }
    suspend fun editNote(path: Path, note: String) {
        val file = withContext(Dispatchers.IO) { checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)) }
        bookModalUi {
            val document = checkNotNull(FileDocumentManager.getInstance().getDocument(file))
            WriteCommandAction.runWriteCommandAction(project) { document.insertString(0, "# $note\n") }
            FileDocumentManager.getInstance().saveDocument(document)
        }
    }
    if (plan.resume) {
        continueLesson()
        awaitText("Start fresh Debug on memory-pixel.asm.")
        record("resumed-pending-debug", mapOf("entrySha256" to captureSha256(Files.readAllBytes(entry)),
            "predictionAnswersLoaded" to true, "newExecutionCreditInjected" to false))
    } else {
        val predictions = listOf(
            Triple("Immediately before sw, which register holds the destination address?", "t0", "sw"),
            Triple("Which bytes follow pixel in increasing address order?", "99 66 33 00", "sw"),
            Triple("Which decimal values should the two loads produce?", "153 and -103", "lbu"),
            Triple("After sb changes blue to 255, what does t2 retain?", "0x00336699", "sb"),
        )
        for ((index, prediction) in predictions.withIndex()) {
            awaitText(prediction.first)
            focus("memory-prediction-$index", prediction.third)
            capture("memory-prediction-$index", prediction.first)
            if (index == 0) {
                click("t1", true)
                capture("memory-address-feedback", "t1 holds 0x00336699, the color value.")
            }
            click(prediction.second, true)
            record("prediction-$index", mapOf("question" to prediction.first, "actualInlineChoiceClicked" to prediction.second))
        }
        awaitText("Start fresh Debug on memory-pixel.asm.")
    }
    val run = launchBookProgram(project, entry, debug = false)
    try {
        run.started.await()
        check(run.exit.await() == 0 && run.stdout.toString().isEmpty())
        record("ordinary-run", mapOf("exitCode" to 0, "stdout" to "", "memoryCorrectnessInferredFromConsole" to false))
    } finally { run.close() }
    captureMemoryDebug(project, entry, ::record, awaitText, capture)

    val fixtures = plan.assessmentFixtures.associateBy { it.source }
    for (source in listOf("independent/red-component.asm", "independent/independent-copy.asm")) {
        val fixture = fixtures.getValue(source)
        val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(Path.of(fixture.path)) }
        check(captureSha256(bytes) == fixture.sha256) { "Changed private authoring fixture" }
        if (plan.resume && source.endsWith("/independent-copy.asm")) continue // This unchanged source retains its actual first-process pass.
        awaitText("Open $source")
        val path = directory.resolve(source)
        if (!plan.resume) {
            click("Check this program", false)
            capture("${path.fileName}-starter-failed", "Public cases passed: 0 of 6.")
            bookModalUi {
                val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
                check(FileDocumentManager.getInstance().getFile(editor.document)?.toNioPath() == path)
                WriteCommandAction.runWriteCommandAction(project) { editor.document.setText(bytes.toString(Charsets.UTF_8)) }
                // Deliberately unsaved: checks must assemble the student's current document.
            }
        } else {
            val expected = "# Recheck this edited independent source.\n" + bytes.toString(Charsets.UTF_8)
            check(withContext(Dispatchers.IO) { Files.readString(path) } == expected)
        }
        click("Check this program", false)
        capture("${path.fileName}-passed", "The current program passed its six stated final-memory cases.")
        bookModalUi { FileDocumentManager.getInstance().saveAllDocuments() }
        record("checked-${path.fileName}", mapOf("source" to source, "solutionSha256" to fixture.sha256,
            "starterFailedThisProcess" to !plan.resume, "passedPublicCases" to 6, "checkedUnsavedText" to !plan.resume))
    }
    awaitText("You have compared old register copies with later memory contents")
    while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
    capture("memory-unit-complete", "You have compared old register copies with later memory contents")
    if (!plan.resume) {
        editNote(entry, "Recheck Debug after editing this worked source.")
        editNote(directory.resolve("independent/red-component.asm"), "Recheck this edited independent source.")
        continueLesson()
        capture("memory-source-invalidated", "Start fresh Debug on memory-pixel.asm.")
    }
    val digests = withContext(Dispatchers.IO) {
        (listOf(archive.entry) + fixtures.keys).associateWith { captureSha256(Files.readAllBytes(directory.resolve(it))) }
    }
    withContext(Dispatchers.IO) {
        Files.writeString(output.resolve("complete.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
            "resumed" to plan.resume, "entryDigests" to mapOf(archive.sha256 to digests.getValue(archive.entry)),
            "sourceDigests" to digests, "observations" to receipts,
            "lessonRunningAtCaptureEnd" to bookModalUi { LessonManager.instance.lessonIsRunning() },
            "marsPluginArchiveSha256" to captureSha256(Files.readAllBytes(Path.of(checkNotNull(System.getProperty("mars.book.mars.archive"))))),
            "scope" to "Actual inline predictions, ordinary Run, ordered Debug with rendered memory/registers, independent final-state checks and source-bound continuation. No human learner review.",
        )), CREATE_NEW)
    }
}

private suspend fun captureMemoryDebug(
    project: Project, entry: Path,
    record: suspend (String, Map<String, Any>) -> Unit,
    awaitText: suspend (String) -> Unit, capture: suspend (String, String) -> Unit,
) {
    val file = withContext(Dispatchers.IO) { checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(entry)) }
    val lines = withContext(Dispatchers.IO) { Files.readString(entry).lines() }
    fun line(instruction: String) = lines.indexOfFirst { it.trim() == instruction }.also { check(it >= 0) }
    val store = line("sw \$t1, 0(\$t0)")
    val blue = line("sb \$t5, 0(\$t0)")
    val reload = line("lw \$t6, 0(\$t0)")
    val exit = line("li \$v0, 10")
    val type = XBreakpointType.EXTENSION_POINT_NAME.extensionList.filterIsInstance<XLineBreakpointType<*>>()
        .single { it.id == "MarsMipsLine" }
    val breakpoints = writeAction {
        listOf(store, blue).map { XDebuggerManager.getInstance(project).breakpointManager.addLineBreakpoint(type, file.url, it, null) }
    }
    val launch = launchBookProgram(project, entry, debug = true)
    try {
        val handler = launch.started.await()
        val session = checkNotNull(launch.session)
        check(launch.pauses.receive() == store)
        val first = awaitBookDebugRegisters(project, handler, mapOf("\$t0" to 0x10010000L, "\$t1" to 0x00336699L))
        awaitText("Resume to sb at change_blue.")
        record("debug-before-store", mapOf("line" to store + 1, "renderedRegisters" to first))
        capture("memory-before-store", "Resume to sb at change_blue.")
        bookModalUi { session.resume() }
        check(launch.pauses.receive() == blue)
        val loaded = awaitBookDebugRegisters(project, handler, mapOf("\$t2" to 0x00336699L, "\$t3" to 153L, "\$t4" to -103L))
        awaitText("Step Over once to lw at inspect_final.")
        val initial = awaitMemoryRow(project, handler, "99 66 33 00 df 9b 57 13")
        record("debug-before-byte", mapOf("line" to blue + 1, "renderedRegisters" to loaded, "renderedMemory" to initial))
        capture("memory-before-byte", "Step Over once to lw at inspect_final.")
        bookModalUi { session.stepOver(false) }
        check(launch.pauses.receive() == reload)
        awaitText("Step Over the two lw instructions.")
        val changed = awaitMemoryRow(project, handler, "ff 66 33 00 df 9b 57 13")
        record("debug-after-byte", mapOf("line" to reload + 1, "renderedMemory" to changed))
        capture("memory-after-byte", "Step Over the two lw instructions.")
        bookModalUi { session.stepOver(false) }
        check(launch.pauses.receive() == reload + 1)
        bookModalUi { session.stepOver(false) }
        check(launch.pauses.receive() == exit)
        val final = awaitBookDebugRegisters(project, handler,
            mapOf("\$t2" to 0x00336699L, "\$t6" to 0x003366ffL, "\$t7" to 0x13579bdfL))
        awaitText("Open independent/red-component.asm")
        record("debug-after-reloads", mapOf("line" to exit + 1, "renderedRegisters" to final, "completedInOneSession" to true))
        capture("memory-after-reloads", "Open independent/red-component.asm")
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

/** Existing native memory tab and its cell renderer; no direct simulator-state access. */
private suspend fun awaitMemoryRow(project: Project, handler: ProcessHandler, expected: String): String {
    var requested = false
    while (true) {
        val row = bookModalUi {
            val descriptor = RunContentManager.getInstance(project).findContentDescriptor(DefaultDebugExecutor.getDebugExecutorInstance(), handler)
                ?: return@bookModalUi null
            val ui = descriptor.runnerLayoutUi ?: return@bookModalUi null
            val content = ui.findContent("MarsMipsMemory") ?: return@bookModalUi null
            if (!content.component.isShowing) { ui.selectAndFocus(content, false, true); return@bookModalUi null }
            if (!requested) {
                val address = content.preferredFocusableComponent as? JTextField ?: error("Native memory address field missing")
                if (!address.isEnabled) return@bookModalUi null
                address.text = "0x10010000"
                address.postActionEvent()
                requested = true
                return@bookModalUi null
            }
            captureComponents(content.component).filterIsInstance<JTable>().flatMap { renderedMemoryRows(it).asSequence() }
                .singleOrNull { it.text.startsWith("0x10010000 | $expected") }?.text
        }
        if (row != null) return row
        delay(100)
    }
}
