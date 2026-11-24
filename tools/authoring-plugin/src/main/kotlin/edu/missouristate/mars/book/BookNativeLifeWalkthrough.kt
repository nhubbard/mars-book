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
internal suspend fun captureBookNativeLife(
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
        "publish-complete",
        "When may the destination become the current board?",
        "After all 64 next cells have been computed",
        "move",
        "After its first row has been computed",
    )
    predict("halo-size", "How many bytes belong to the halo?", "36 bytes", "lbu")
    predict(
        "first-interior",
        "Where is logical cell (0,0) relative to its board base?",
        "At byte offset 11",
        "daddiu",
    )
  }
  awaitText("Start a fresh Debug session for life-buffers.asm.")
  selected(entry)
  captureLifeDebug(project, entry, ::record, awaitText, capture, plan.resume)
  if (!plan.resume) {
    predict("allocation-role", "Which allocation is current after two updates?", "Board A", "beqc")
  }
  awaitText("Run life-buffers.asm using ordinary Run.")
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
    predict("changed-count", "How many cells change in one oscillator update?", "Four", "addu")
  }
  val fixture = plan.assessmentFixtures.single()
  val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(Path.of(fixture.path)) }
  check(captureSha256(bytes) == fixture.sha256) { "Changed private authoring fixture" }
  awaitText("Write life_changes in independent/life-changes.asm, replacing its break body.")
  val independent = directory.resolve(fixture.source)
  selected(independent)
  if (!plan.resume) {
    click("Check this program", false)
    capture("life-starter", "Public cases passed: 0 of 14.")
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
  capture("life-passed", "The current source passes all fourteen public Life cases.")
  record(
      "independent",
      mapOf(
          "source" to fixture.source,
          "solutionSha256" to fixture.sha256,
          "starterPassedPublicCases" to if (plan.resume) "not rerun" else "0 of 14",
          "completedPublicCases" to 14,
          "checkedUnsavedText" to !plan.resume,
      ),
  )
  withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
    FileDocumentManager.getInstance().saveAllDocuments()
  }
  awaitText(
      "Explain a transition using the old source, complete destination and publication boundary.",
  )
  while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
  capture(
      "life-unit-complete",
      "Explain a transition using the old source, complete destination and publication boundary.",
  )
  if (!plan.resume) {
    for ((path, note) in
        listOf(
            entry to "Recheck Life execution after this edit.",
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
    capture("life-source-invalidated", "Start a fresh Debug session for life-buffers.asm.")
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
                        "Actual inline predictions, eight snapshot/role/publication Debug boundaries, ordinary Run, direct function-memory checks and continuation. Bitmap inspection and human learner review are separate verification.",
                ),
            ),
        CREATE_NEW,
    )
  }
}

private data class LifePause(
    val label: String,
    val registers: Map<String, Long>,
    val nextText: String,
)

private suspend fun captureLifeDebug(
    project: Project,
    entry: Path,
    record: suspend (String, Map<String, Any>) -> Unit,
    awaitText: suspend (String) -> Unit,
    capture: suspend (String, String) -> Unit,
    resumed: Boolean,
) {
  val stops =
      listOf(
          LifePause(
              "generation_ready",
              mapOf(
                  "\$s0" to 268501320L,
                  "\$s1" to 268501424L,
                  "\$s2" to 0L,
                  "\$s3" to 2L,
                  "\$sp" to 2147479536L,
              ),
              "Resume to the first count_neighbors entry.",
          ),
          LifePause(
              "count_neighbors",
              mapOf(
                  "\$a0" to 268501331L,
                  "\$s0" to 268501331L,
                  "\$s1" to 268501435L,
                  "\$s2" to 8L,
                  "\$s3" to 8L,
                  "\$s4" to 0L,
                  "\$sp" to 2147479488L,
              ),
              "Resume to the first store_cell.",
          ),
          LifePause(
              "store_cell",
              mapOf(
                  "\$s0" to 268501331L,
                  "\$s1" to 268501435L,
                  "\$s2" to 8L,
                  "\$s3" to 8L,
                  "\$s4" to 0L,
                  "\$t1" to 0L,
                  "\$v0" to 0L,
                  "\$sp" to 2147479488L,
              ),
              "Resume to the first generation_built.",
          ),
          LifePause(
              "generation_built",
              mapOf(
                  "\$s0" to 268501320L,
                  "\$s1" to 268501424L,
                  "\$s2" to 0L,
                  "\$s3" to 2L,
                  "\$v0" to 3L,
                  "\$sp" to 2147479536L,
              ),
              "Resume to the next generation_ready.",
          ),
          LifePause(
              "generation_ready",
              mapOf(
                  "\$s0" to 268501424L,
                  "\$s1" to 268501320L,
                  "\$s2" to 1L,
                  "\$s3" to 2L,
                  "\$sp" to 2147479536L,
              ),
              "Resume to the second generation_built.",
          ),
          LifePause(
              "generation_built",
              mapOf(
                  "\$s0" to 268501424L,
                  "\$s1" to 268501320L,
                  "\$s2" to 1L,
                  "\$s3" to 2L,
                  "\$v0" to 3L,
                  "\$sp" to 2147479536L,
              ),
              "Resume to the next generation_ready.",
          ),
          LifePause(
              "generation_ready",
              mapOf(
                  "\$s0" to 268501320L,
                  "\$s1" to 268501424L,
                  "\$s2" to 2L,
                  "\$s3" to 2L,
                  "\$sp" to 2147479536L,
              ),
              "Resume to life_done before the exit selector is loaded.",
          ),
          LifePause(
              "life_done",
              mapOf(
                  "\$s0" to 268501320L,
                  "\$s1" to 268501424L,
                  "\$s2" to 2L,
                  "\$s3" to 2L,
                  "\$sp" to 2147479536L,
              ),
              if (resumed) "Run life-buffers.asm using ordinary Run."
              else "Which allocation is current after two updates?",
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
      check(launch.pauses.receive() == line(stop.label)) { "Wrong Life stop: ${stop.label}" }
      val registers = awaitBookDebugRegisters(project, handler, stop.registers)
      awaitText(stop.nextText)
      record(
          "debug-life-$index",
          mapOf(
              "source" to entry.toString(),
              "line" to line(stop.label) + 1,
              "renderedRegisters" to registers,
              "nativeNextStepObserved" to stop.nextText,
          ),
      )
      capture("debug-life-$index", stop.nextText)
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
