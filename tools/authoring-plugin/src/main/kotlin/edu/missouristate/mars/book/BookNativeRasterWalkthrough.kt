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
internal suspend fun captureBookNativeRaster(
    project: Project,
    plan: BookPackageImportPlan,
    output: Path,
    awaitText: suspend (String) -> Unit,
    capture: suspend (String, String) -> Unit,
    click: suspend (String, Boolean) -> Unit,
    focus: suspend (String, String) -> Unit,
) {
  val archive = plan.archives.single()
  val directory = archive.directory(Path.of(plan.projectRoot))
  val entry = directory.resolve(archive.entry)
  val receipts = mutableListOf<Map<String, Any>>()
  suspend fun record(id: String, facts: Map<String, Any>) {
    receipts += mapOf("id" to id) + facts
    withContext(Dispatchers.IO) {
      Files.writeString(
          output.resolve("$id-observation.json"),
          GsonBuilder().setPrettyPrinting().create().toJson(facts),
          CREATE_NEW,
      )
    }
  }
  suspend fun continueLesson() =
      withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
        val action =
            checkNotNull(ActionManager.getInstance().getAction("Mars.Learning.ContinueDownloaded"))
        val event =
            AnActionEvent.createEvent(
                action,
                SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build(),
                null,
                ActionPlaces.MAIN_MENU,
                ActionUiKind.NONE,
                null,
            )
        check(ActionUtil.updateAction(action, event).isPerformed && event.presentation.isEnabled)
        ActionUtil.performAction(action, event)
      }
  suspend fun selected(path: Path) {
    while (
        !bookModalUi {
          FileEditorManager.getInstance(project).selectedTextEditor?.document?.let {
            FileDocumentManager.getInstance().getFile(it)?.toNioPath() == path
          } == true
        }
    ) delay(100)
  }
  suspend fun predict(
      id: String,
      question: String,
      answer: String,
      mnemonic: String,
      wrong: String? = null,
  ) {
    awaitText(question)
    selected(entry)
    focus(id, mnemonic)
    capture(id, question)
    if (wrong != null) click(wrong, true)
    click(answer, true)
    record(
        id,
        mapOf("actualInlineChoiceClicked" to answer, "wrongChoiceFirst" to (wrong ?: "none")),
    )
  }
  if (plan.resume) continueLesson()
  else {
    predict(
        "inclusive-endpoints",
        "How many cells does this solid raster paint?",
        "Six",
        "sw",
        "Five",
    )
    predict(
        "two-decisions",
        "Which coordinates advance after the first cell?",
        "Both x and y",
        "sll",
    )
    predict("canonical-tie", "Which cell is selected at x=2 for this halfway case?", "(2,1)", "lw")
  }
  awaitText("Start a fresh Debug session for integer-line.asm.")
  selected(entry)
  captureRasterDebug(project, entry, ::record, awaitText, capture, plan.resume)
  if (!plan.resume) {
    predict(
        "caller-count",
        "Where does the completed program retain the painted-cell count?",
        "The line_count word",
        "li",
    )
  }
  awaitText("Run integer-line.asm using ordinary Run.")
  selected(entry)
  val run = launchBookProgram(project, entry, debug = false)
  try {
    run.started.await()
    check(run.exit.await() == 0 && run.stdout.toString().isEmpty())
    record("ordinary-run", mapOf("source" to archive.entry, "exitCode" to 0, "stdout" to ""))
  } finally {
    run.close()
  }
  capture("after-ordinary-run", "")
  if (!plan.resume) {
    predict(
        "dash-state",
        "Which state should select alternating painted and skipped cells?",
        "The position in the canonical visit sequence",
        "addiu",
    )
  }
  val fixture = plan.assessmentFixtures.single()
  val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(Path.of(fixture.path)) }
  check(captureSha256(bytes) == fixture.sha256) { "Changed private authoring fixture" }
  awaitText("Write draw_dashed in independent/dashed-line.asm, replacing its break body.")
  val independent = directory.resolve(fixture.source)
  selected(independent)
  if (!plan.resume) {
    click("Check this program", false)
    capture("raster-starter", "Public cases passed: 0 of 16.")
    withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
      val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
      check(FileDocumentManager.getInstance().getFile(editor.document)?.toNioPath() == independent)
      WriteCommandAction.runWriteCommandAction(project) {
        editor.document.setText(bytes.toString(Charsets.UTF_8))
      }
      // Leave the edit unsaved: production checking must use the live document.
    }
  } else
      check(
          withContext(Dispatchers.IO) { Files.readString(independent) } ==
              "# Recheck this edited independent source.\n" + bytes.toString(Charsets.UTF_8),
      )
  click("Check this program", false)
  capture("raster-passed", "The current source passes all sixteen public raster cases.")
  record(
      "independent",
      mapOf(
          "source" to fixture.source,
          "solutionSha256" to fixture.sha256,
          "starterPassedPublicCases" to if (plan.resume) "not rerun" else "0 of 16",
          "completedPublicCases" to 16,
          "checkedUnsavedText" to !plan.resume,
      ),
  )
  withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
    FileDocumentManager.getInstance().saveAllDocuments()
  }
  awaitText("Use a counterexample to explain a wrong cell, count or store.")
  while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
  capture("raster-unit-complete", "Use a counterexample to explain a wrong cell, count or store.")
  if (!plan.resume) {
    for ((path, note) in
        listOf(
            entry to "Recheck raster execution after this edit.",
            independent to "Recheck this edited independent source.",
        )) {
      val file =
          withContext(Dispatchers.IO) {
            checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path))
          }
      withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
        val document = checkNotNull(FileDocumentManager.getInstance().getDocument(file))
        WriteCommandAction.runWriteCommandAction(project) { document.insertString(0, "# $note\n") }
        FileDocumentManager.getInstance().saveDocument(document)
      }
    }
    continueLesson()
    capture("raster-source-invalidated", "Start a fresh Debug session for integer-line.asm.")
  }
  val digests =
      withContext(Dispatchers.IO) {
        listOf(archive.entry, fixture.source).associateWith {
          captureSha256(Files.readAllBytes(directory.resolve(it)))
        }
      }
  val running = bookModalUi { LessonManager.instance.lessonIsRunning() }
  withContext(Dispatchers.IO) {
    Files.writeString(
        output.resolve("complete.json"),
        GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(
                mapOf(
                    "resumed" to plan.resume,
                    "entryDigests" to mapOf(archive.sha256 to digests.getValue(archive.entry)),
                    "sourceDigests" to digests,
                    "observations" to receipts,
                    "lessonRunningAtCaptureEnd" to running,
                    "marsPluginArchiveSha256" to
                        captureSha256(
                            Files.readAllBytes(
                                Path.of(checkNotNull(System.getProperty("mars.book.mars.archive"))),
                            ),
                        ),
                    "scope" to
                        "Actual inline predictions, eight cursor/decision/return Debug boundaries, ordinary Run, direct function-memory checks and continuation. Bitmap inspection and human learner review are separate verification.",
                ),
            ),
        CREATE_NEW,
    )
  }
}

private data class RasterPause(
    val label: String,
    val registers: Map<String, Long>,
    val nextText: String,
)

private suspend fun captureRasterDebug(
    project: Project,
    entry: Path,
    record: suspend (String, Map<String, Any>) -> Unit,
    awaitText: suspend (String) -> Unit,
    capture: suspend (String, String) -> Unit,
    resumed: Boolean,
) {
  val stops =
      listOf(
          RasterPause(
              "line_pixel",
              mapOf(
                  "\$t0" to 1L,
                  "\$t1" to 1L,
                  "\$t2" to 6L,
                  "\$t3" to 4L,
                  "\$t4" to 5L,
                  "\$t5" to 0xfffffffffffffffduL.toLong(),
                  "\$t6" to 1L,
                  "\$t7" to 2L,
                  "\$v0" to 0L,
              ),
              "Resume to the first line_decide.",
          ),
          RasterPause(
              "line_decide",
              mapOf("\$t0" to 1L, "\$t1" to 1L, "\$t7" to 2L, "\$v0" to 1L),
              "Resume to the next line_pixel.",
          ),
          RasterPause(
              "line_pixel",
              mapOf("\$t0" to 2L, "\$t1" to 2L, "\$t7" to 4L, "\$v0" to 1L),
              "Resume to the second line_decide.",
          ),
          RasterPause(
              "line_decide",
              mapOf("\$t0" to 2L, "\$t1" to 2L, "\$t7" to 4L, "\$v0" to 2L),
              "Resume to skip_x on this second transition, before its vertical comparison.",
          ),
          RasterPause(
              "skip_x",
              mapOf(
                  "\$t0" to 3L,
                  "\$t1" to 2L,
                  "\$t7" to 1L,
                  "\$t8" to 8L,
                  "\$t4" to 5L,
                  "\$v0" to 2L,
              ),
              "Resume to the next line_pixel.",
          ),
          RasterPause(
              "line_pixel",
              mapOf("\$t0" to 3L, "\$t1" to 2L, "\$t7" to 1L, "\$v0" to 2L),
              "Resume to line_return.",
          ),
          RasterPause(
              "line_return",
              mapOf(
                  "\$t0" to 6L,
                  "\$t1" to 4L,
                  "\$t7" to 2L,
                  "\$v0" to 6L,
                  "\$v1" to 268501144L,
                  "\$sp" to 2147479536L,
              ),
              "Resume to line_complete.",
          ),
          RasterPause(
              "line_complete",
              mapOf("\$v0" to 6L, "\$sp" to 2147479536L),
              if (resumed) "Run integer-line.asm using ordinary Run."
              else "Where does the completed program retain the painted-cell count?",
          ),
      )
  val lines = withContext(Dispatchers.IO) { Files.readAllLines(entry) }
  fun line(label: String) =
      lines.indexOfFirst { it.trim() == "$label:" }.also { check(it >= 0) } + 1
  val file =
      withContext(Dispatchers.IO) {
        checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(entry))
      }
  val type =
      XBreakpointType.EXTENSION_POINT_NAME.extensionList
          .filterIsInstance<XLineBreakpointType<*>>()
          .single { it.id == "MarsMipsLine" }
  val breakpoints = writeAction {
    mutableListOf(
        XDebuggerManager.getInstance(project)
            .breakpointManager
            .addLineBreakpoint(type, file.url, line(stops.first().label), null),
    )
  }
  val launch = launchBookProgram(project, entry, debug = true)
  try {
    val handler = launch.started.await()
    val session = checkNotNull(launch.session)
    for ((index, stop) in stops.withIndex()) {
      if (index > 0) {
        // One temporary breakpoint at a time distinguishes repeated calls and loop visits.
        writeAction {
          breakpoints.forEach {
            XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(it)
          }
          breakpoints.clear()
          breakpoints +=
              XDebuggerManager.getInstance(project)
                  .breakpointManager
                  .addLineBreakpoint(type, file.url, line(stop.label), null)
        }
        bookModalUi { session.resume() }
      }
      check(launch.pauses.receive() == line(stop.label)) { "Wrong raster stop: ${stop.label}" }
      val registers = awaitBookDebugRegisters(project, handler, stop.registers)
      awaitText(stop.nextText)
      record(
          "debug-raster-$index",
          mapOf(
              "source" to entry.toString(),
              "line" to line(stop.label) + 1,
              "renderedRegisters" to registers,
              "nativeNextStepObserved" to stop.nextText,
          ),
      )
      capture("debug-raster-$index", stop.nextText)
    }
    writeAction {
      breakpoints.forEach {
        XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(it)
      }
      breakpoints.clear()
    }
    bookModalUi { session.resume() }
    check(launch.exit.await() == 0 && launch.stdout.toString().isEmpty())
  } finally {
    withContext(NonCancellable + Dispatchers.EDT) {
      launch.session?.stop()
      writeAction {
        breakpoints.forEach {
          XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(it)
        }
      }
    }
    launch.close()
  }
}
