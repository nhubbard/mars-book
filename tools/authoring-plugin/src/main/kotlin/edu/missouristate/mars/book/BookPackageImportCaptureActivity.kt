package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import java.awt.Rectangle
import java.awt.Window
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JDialog
import javax.swing.text.JTextComponent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

internal data class BookImportArchive(
    val path: String = "", val sha256: String = "", val id: String = "", val version: String = "",
    val entry: String = "", val assignment: String = "", val entrySha256: String = "",
    val source: String? = null,
) {
    fun validate() {
        require(Path.of(path).isAbsolute)
        source?.let {
            val uri = URI.create(it)
            require(uri.scheme == "https" && uri.host != null && uri.rawUserInfo == null && uri.rawFragment == null)
        }
        require(sha256.matches(Regex("[a-f0-9]{64}")) && entrySha256.matches(Regex("[a-f0-9]{64}")))
        require(id.matches(Regex("[0-9]{3}-[a-z][a-z0-9-]*")))
        require(version.matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+")))
        for (relative in listOf(entry, assignment)) {
            require(relative.isNotBlank() && !Path.of(relative).isAbsolute && Path.of(relative).normalize() == Path.of(relative))
            require(!relative.startsWith(".."))
        }
    }
    fun directory(project: Path): Path = project.resolve("mars-labs/packages/$id/$version/$sha256")
}

/** Local authoring-only source fixture. Its contents never enter student distributions. */
internal data class BookAssessmentFixture(val source: String = "", val path: String = "", val sha256: String = "") {
    fun validate() {
        require(source in setOf("independent/plus-one.asm", "independent/wrapped-byte.asm"))
        require(Path.of(path).isAbsolute && sha256.matches(Regex("[a-f0-9]{64}")))
    }
}

internal data class BookPackageImportPlan(
    val projectRoot: String = "", val outputDirectory: String = "", val archives: List<BookImportArchive> = emptyList(),
    val resume: Boolean = false, val expectedEntryDigests: Map<String, String> = emptyMap(),
    val closeProjectAfterCapture: Boolean = false,
    val nativePredictions: Boolean = false,
    val nativeExecution: Boolean = false,
    val nativeDebug: Boolean = false,
    val resumeCompleted: Boolean = false,
    val assessmentFixtures: List<BookAssessmentFixture> = emptyList(),
) {
    fun validate() {
        require(Path.of(projectRoot).isAbsolute && Path.of(outputDirectory).isAbsolute)
        require(Path.of(projectRoot).normalize() != Path.of(outputDirectory).normalize())
        require(archives.size in 1..64 && archives.map { it.sha256 }.distinct().size == archives.size)
        archives.forEach(BookImportArchive::validate)
        require(!nativePredictions || archives.size == 1)
        require(!nativeExecution || nativePredictions)
        require(!nativeDebug || nativeExecution)
        if (assessmentFixtures.isNotEmpty()) {
            require(nativeDebug)
            require(assessmentFixtures.map { it.source }.toSet() == setOf("independent/plus-one.asm", "independent/wrapped-byte.asm"))
            require(assessmentFixtures.size == 2)
            assessmentFixtures.forEach(BookAssessmentFixture::validate)
        }
        require(!resumeCompleted || (resume && nativeDebug))
        if (resume) {
            require(expectedEntryDigests.keys == archives.map { it.sha256 }.toSet())
            require(expectedEntryDigests.values.all { it.matches(Regex("[a-f0-9]{64}")) })
        } else require(expectedEntryDigests.isEmpty())
    }
}

/** Drives production actions and normal Run/Debug only in the explicitly configured authoring project. */
class BookPackageImportCaptureActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val location = System.getProperty("mars.book.package.import.plan") ?: return
        val plan = withContext(Dispatchers.IO) {
            GsonBuilder().create().fromJson(Files.readString(Path.of(location)), BookPackageImportPlan::class.java)
        }
        plan.validate()
        if (Path.of(checkNotNull(project.basePath)).toRealPath() != Path.of(plan.projectRoot).toRealPath()) return
        val output = Path.of(plan.outputDirectory)
        withContext(Dispatchers.IO) { Files.createDirectory(output) }
        try {
            withTimeout(600_000) {
                withContext(Dispatchers.EDT) { configureBookPresentation(project) }
                val ready = CompletableDeferred<Unit>()
                withContext(Dispatchers.EDT) { ToolWindowManager.getInstance(project).invokeLater { ready.complete(Unit) } }
                ready.await()
                capture(project, plan, output)
            }
            if (plan.closeProjectAfterCapture) closeBookCaptureProject(project, output)
        } catch (error: Exception) {
            withContext(NonCancellable + Dispatchers.IO) { Files.writeString(output.resolve("failed.txt"), error.stackTraceToString(), CREATE_NEW) }
            throw error
        }
    }

    private suspend fun capture(project: Project, plan: BookPackageImportPlan, output: Path) {
        val root = Path.of(plan.projectRoot)
        for (archive in plan.archives) withContext(Dispatchers.IO) {
            check(captureSha256(Files.readAllBytes(Path.of(archive.path))) == archive.sha256)
        }
        val first = plan.archives.first()
        val firstEntry = first.directory(root).resolve(first.entry)
        if (plan.nativePredictions) {
            if (plan.resume) verifyEntries(plan, root, plan.expectedEntryDigests)
            else importArchive(project, first, output, "native", capture = true)
            captureBookNativePredictions(project, plan, output)
            return
        }
        if (plan.resume) {
            verifyEntries(plan, root, plan.expectedEntryDigests)
            importArchive(project, first, output, "reopened", capture = true)
            verifyEntries(plan, root, plan.expectedEntryDigests)
        } else {
            check(!withContext(Dispatchers.IO) { Files.exists(root.resolve("mars-labs/packages")) })
            importArchive(project, first, output, "cancel-selection", cancelSelection = true, capture = true)
            check(!withContext(Dispatchers.IO) { Files.exists(root.resolve("mars-labs/packages")) })
            importArchive(project, first, output, "cancel-preview", cancelPreview = true, capture = true)
            check(!withContext(Dispatchers.IO) { Files.exists(root.resolve("mars-labs/packages")) })
            for ((index, archive) in plan.archives.withIndex()) {
                importArchive(project, archive, output, "import-${index + 1}", capture = index == 0 || index == plan.archives.lastIndex)
                withContext(Dispatchers.IO) {
                    check(captureSha256(Files.readAllBytes(archive.directory(root).resolve(archive.entry))) == archive.entrySha256)
                }
            }
            // A normal student edit, not a completed solution or progress injection.
            val file = withContext(Dispatchers.IO) { checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(firstEntry)) }
            withContext(Dispatchers.EDT) {
                val document = checkNotNull(FileDocumentManager.getInstance().getDocument(file))
                WriteCommandAction.runWriteCommandAction(project) { document.insertString(0, "# Student notes survive importing the same archive.\n") }
                FileDocumentManager.getInstance().saveDocument(document)
            }
            val edited = withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(firstEntry)) }
            importArchive(project, first, output, "reimport", capture = true)
            check(withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(firstEntry)) } == edited)
            runAndDebug(project, firstEntry, output)
        }
        val digests = withContext(Dispatchers.IO) { plan.archives.associate { it.sha256 to captureSha256(Files.readAllBytes(it.directory(root).resolve(it.entry))) } }
        withContext(Dispatchers.IO) {
            Files.writeString(output.resolve("complete.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
                "archives" to plan.archives, "entryDigests" to digests, "resumed" to plan.resume,
                "archiveCount" to plan.archives.size, "uniqueProjectCount" to plan.archives.map { it.id }.distinct().size,
                "editsPreserved" to true, "cancelledImportsCreateNoWorkingCopy" to !plan.resume,
                "runAndDebugExercised" to !plan.resume,
                "marsPluginArchiveSha256" to captureSha256(Files.readAllBytes(Path.of(checkNotNull(System.getProperty("mars.book.mars.archive"))))),
                "scope" to "Actual native archive preview/import and edited-file preservation; public distribution files are not interpreted as native trainer steps and included grading scripts are not run",
            )), CREATE_NEW)
        }
    }

    private suspend fun verifyEntries(plan: BookPackageImportPlan, root: Path, digests: Map<String, String>) = withContext(Dispatchers.IO) {
        for (archive in plan.archives) check(captureSha256(Files.readAllBytes(archive.directory(root).resolve(archive.entry))) == digests.getValue(archive.sha256))
    }

    private suspend fun importArchive(project: Project, archive: BookImportArchive, output: Path, prefix: String,
                               cancelSelection: Boolean = false, cancelPreview: Boolean = false, capture: Boolean = false) {
        val finished = CompletableDeferred<Unit>()
        val failure = AtomicReference<Throwable>()
        withContext(Dispatchers.EDT) {
            val action = checkNotNull(ActionManager.getInstance().getAction("Mars.Learning.ImportPackage"))
            val event = AnActionEvent.createEvent(action, SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build(),
                null, ActionPlaces.MAIN_MENU, ActionUiKind.NONE, null)
            check(ActionUtil.updateAction(action, event).isPerformed && event.presentation.isEnabled)
            ApplicationManager.getApplication().invokeLater({
                try { ActionUtil.performAction(action, event); finished.complete(Unit) }
                catch (error: Throwable) { failure.set(error); finished.completeExceptionally(error) }
            }, ModalityState.nonModal())
        }
        val choose = dialog("Open a MARS lab package", failure)
        bookModalUi {
            val field = captureComponents(choose.rootPane).filterIsInstance<JTextComponent>()
                .single { it.isShowing && it.accessibleContext.accessibleName == "Lab link or ZIP archive:" }
            field.text = archive.source ?: archive.path
            field.caretPosition = 0
            choose.rootPane.paintImmediately(choose.rootPane.visibleRect)
        }
        delay(200) // Wait for the actual screen paint before the Robot capture.
        if (capture) captureBookComponent(choose.rootPane, output, "$prefix-choose", "Lab link or ZIP archive:")
        bookModalUi { visibleLessonButton(choose.rootPane, if (cancelSelection) "Cancel" else "Preview lab").doClick() }
        if (!cancelSelection) {
            val preview = dialog("Import this MARS lab?", failure)
            val text = bookModalUi { lessonVisibleText(preview.rootPane) }
            check(text.contains("Package: ${archive.id}") && text.contains("Version: ${archive.version}") && text.contains(archive.sha256))
            if (capture) captureBookComponent(preview.rootPane, output, "$prefix-preview", archive.sha256)
            bookModalUi { visibleLessonButton(preview.rootPane, if (cancelPreview) "Cancel" else "Import lab").doClick() }
        }
        finished.await()
        if (!cancelSelection && !cancelPreview) {
            val expected = archive.directory(Path.of(checkNotNull(project.basePath))).resolve(archive.entry)
            while (!bookModalUi {
                val manager = FileEditorManager.getInstance(project)
                manager.selectedTextEditor?.let { FileDocumentManager.getInstance().getFile(it.document)?.toNioPath() == expected } == true
            }) delay(100)
            withContext(Dispatchers.IO) {
                val directory = archive.directory(Path.of(checkNotNull(project.basePath)))
                check(Files.readString(directory.resolve(".mars-package.sha256")) == archive.sha256 + "\n")
                check(captureSha256(Files.readAllBytes(directory.resolve(".mars-package.zip"))) == archive.sha256)
                check(Files.isRegularFile(directory.resolve(archive.assignment)))
                Files.writeString(output.resolve("$prefix-import.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
                    "archive" to archive, "selectedEntry" to expected.toString(), "nativePreviewMatched" to true,
                )), CREATE_NEW)
            }
        }
    }

    private suspend fun dialog(title: String, failure: AtomicReference<Throwable>): JDialog {
        while (true) {
            failure.get()?.let { throw it }
            val found = bookModalUi { Window.getWindows().filterIsInstance<JDialog>().singleOrNull { it.isShowing && it.title == title } }
            if (found != null) {
                delay(500)
                val first = bookModalUi { Rectangle(found.locationOnScreen, found.size) }
                delay(200)
                if (bookModalUi { found.isShowing && Rectangle(found.locationOnScreen, found.size) == first }) return found
            }
            delay(100)
        }
    }

    private suspend fun runAndDebug(project: Project, source: Path, output: Path) {
        val run = launchBookProgram(project, source, debug = false)
        val runHandler = run.started.await()
        check(run.exit.await() == 0)
        check(run.stdout.toString().isEmpty())
        check(runHandler.isProcessTerminated)
        run.close()
        val file = withContext(Dispatchers.IO) { checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(source)) }
        val line = withContext(Dispatchers.IO) { Files.readString(source).lines().indexOfFirst { it.trim() == "li \$v0, 10" } }
        check(line >= 0)
        val type = XBreakpointType.EXTENSION_POINT_NAME.extensionList.filterIsInstance<XLineBreakpointType<*>>().single { it.id == "MarsMipsLine" }
        val breakpoint = writeAction { XDebuggerManager.getInstance(project).breakpointManager.addLineBreakpoint(type, file.url, line, null) }
        val debug = launchBookProgram(project, source, debug = true)
        try {
            val handler = debug.started.await()
            val session = withContext(Dispatchers.EDT) { XDebuggerManager.getInstance(project).debugSessions.single { it.debugProcess.processHandler === handler } }
            val observations = mutableListOf<Map<String, Any>>()
            for ((index, value) in listOf(0L, 10L).withIndex()) {
                check(debug.pauses.receive() == line + index)
                awaitBookDebugRegisters(project, handler, mapOf("\$v0" to value))
                delay(300) // Native variable-model updates can precede their Swing paint.
                val rows = awaitBookDebugRegisters(project, handler, mapOf("\$v0" to value))
                val frame = withContext(Dispatchers.EDT) {
                    checkNotNull(WindowManager.getInstance().getFrame(project)).rootPane.also {
                        it.paintImmediately(it.visibleRect)
                    }
                }
                delay(100)
                captureBookComponent(frame, output, "imported-debug-$index", "Native imported source; v0=$value") {
                    session.topFramePosition?.file?.toNioPath() == source && session.topFramePosition?.line == line + index
                }
                observations += mapOf("line" to line + index + 1, "v0" to value, "renderedRows" to rows)
                withContext(Dispatchers.EDT) { if (index == 0) session.stepOver(false) else session.resume() }
            }
            check(debug.exit.await() == 0)
            withContext(Dispatchers.IO) { Files.writeString(output.resolve("execution.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
                "source" to source.toString(), "runExit" to 0, "runStdout" to "", "debugExit" to 0, "debugObservations" to observations,
                "scope" to "Normal Run/Debug of the unfinished imported starter's exit sequence, not completed submission grading",
            )), CREATE_NEW) }
        } finally {
            debug.close()
            withContext(NonCancellable + Dispatchers.EDT) {
                debug.session?.stop()
                writeAction { XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(breakpoint) }
            }
        }
    }

}
