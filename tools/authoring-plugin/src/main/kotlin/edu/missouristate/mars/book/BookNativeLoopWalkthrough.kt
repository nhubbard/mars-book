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
internal suspend fun captureBookNativeLoop(
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
    predict("port-address", "Which operand chooses the display port?", "t7", "sb", "t3")
    predict(
        "glyph-width",
        "After writing one glyph, how far should t0 advance?",
        "One byte",
        "daddiu",
    )
  }
  awaitText("Use range-counter.asm unchanged.")
  selected(entry)
  captureLoopDebug(project, entry, "full", ::record, awaitText, capture, plan.resume)
  if (!plan.resume) {
    predict(
        "empty-prediction",
        "What must happen at the first visit to loop?",
        "Branch to done before lbu",
        "beqzc",
    )
    awaitText("The lesson opens ranges/empty.asm")
    val empty = directory.resolve("ranges/empty.asm")
    selected(empty)
    captureLoopDebug(project, empty, "empty", ::record, awaitText, capture)
    predict(
        "tail-prediction",
        "Which sequence should be sent to the right-hand display port?",
        "0x79, then 0x71",
        "beqzc",
    )
    awaitText("The lesson opens ranges/tail.asm")
    val tail = directory.resolve("ranges/tail.asm")
    selected(tail)
    captureLoopDebug(project, tail, "tail", ::record, awaitText, capture)
  }
  awaitText("Run range-counter.asm using ordinary Run")
  selected(entry)
  // Running another file must not satisfy the full-range observation.
  if (!plan.resume) {
    val wrong = launchBookProgram(project, directory.resolve("ranges/empty.asm"), debug = false)
    try {
      wrong.started.await()
      check(wrong.exit.await() == 0)
    } finally {
      wrong.close()
    }
    capture("wrong-source-run", "Run range-counter.asm using ordinary Run")
    record(
        "wrong-source-run",
        mapOf("source" to "ranges/empty.asm", "fullRangeActivityStillPending" to true),
    )
  }
  val run = launchBookProgram(project, entry, debug = false)
  try {
    run.started.await()
    check(run.exit.await() == 0 && run.stdout.toString().isEmpty())
    record("ordinary-run", mapOf("source" to archive.entry, "exitCode" to 0, "stdout" to ""))
  } finally {
    run.close()
  }
  val fixture = plan.assessmentFixtures.single()
  val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(Path.of(fixture.path)) }
  check(captureSha256(bytes) == fixture.sha256) { "Changed private authoring fixture" }
  awaitText("Write the end-pointer version described in the written lab.")
  val independent = directory.resolve(fixture.source)
  selected(independent)
  if (!plan.resume) {
    click("Check this program", false)
    capture("end-pointer-starter", "Public cases passed: 3 of 10.")
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
  capture("end-pointer-passed", "The current source passes the public transaction")
  record(
      "independent",
      mapOf(
          "source" to fixture.source,
          "solutionSha256" to fixture.sha256,
          "starterPassedPublicCases" to if (plan.resume) "not rerun" else "3 of 10",
          "completedPublicCases" to 10,
          "checkedUnsavedText" to !plan.resume,
      ),
  )
  withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
    FileDocumentManager.getInstance().saveAllDocuments()
  }
  awaitText("Explain the loop using its entry state")
  while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
  capture("loop-unit-complete", "Explain the loop using its entry state")
  if (!plan.resume) {
    for ((path, note) in
        listOf(
            entry to "Recheck full-range execution after this edit.",
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
    capture("loop-source-invalidated", "Use range-counter.asm unchanged.")
  }
  val digests =
      withContext(Dispatchers.IO) {
        listOf(archive.entry, "ranges/empty.asm", "ranges/tail.asm", fixture.source).associateWith {
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
                        "Actual inline predictions, separately sourced Debug, ordinary Run, independent transaction/read checks and continuation. No human learner review.",
                ),
            ),
        CREATE_NEW,
    )
  }
}

private data class LoopPause(
    val label: String,
    val registers: Map<String, Long>,
    val nextText: String,
    val steps: Int = 0,
)

private suspend fun captureLoopDebug(
    project: Project,
    entry: Path,
    mode: String,
    record: suspend (String, Map<String, Any>) -> Unit,
    awaitText: suspend (String) -> Unit,
    capture: suspend (String, String) -> Unit,
    resumed: Boolean = false,
) {
  val stops =
      when (mode) {
        "full" ->
            listOf(
                LoopPause(
                    "loop",
                    mapOf("\$t0" to 0x10010000L, "\$t1" to 16L, "\$t2" to 0L),
                    "Resume to port_write.",
                ),
                LoopPause(
                    "port_write",
                    mapOf(
                        "\$t0" to 0x10010000L,
                        "\$t1" to 16L,
                        "\$t3" to 0x3fL,
                        "\$t7" to 0xffff0010L,
                    ),
                    "Step over sb to displayed.",
                ),
                LoopPause(
                    "displayed",
                    mapOf("\$t0" to 0x10010000L, "\$t1" to 16L, "\$t3" to 0x3fL),
                    "Step over the three update instructions",
                    1,
                ),
                LoopPause(
                    "iteration_done",
                    mapOf("\$t0" to 0x10010001L, "\$t1" to 15L, "\$t2" to 1L),
                    "Resume to the next visit to port_write.",
                    3,
                ),
                LoopPause(
                    "port_write",
                    mapOf("\$t0" to 0x10010001L, "\$t1" to 15L, "\$t2" to 1L, "\$t3" to 6L),
                    "What must happen at the first visit to loop?",
                ),
            )
        "empty" ->
            listOf(
                LoopPause(
                    "loop",
                    mapOf("\$t0" to 0x10010010L, "\$t1" to 0L, "\$t2" to 16L),
                    "Step over beqzc.",
                ),
                LoopPause(
                    "done",
                    mapOf("\$t0" to 0x10010010L, "\$t1" to 0L, "\$t2" to 16L),
                    "Which sequence should be sent",
                    1,
                ),
            )
        "tail" ->
            listOf(
                LoopPause(
                    "port_write",
                    mapOf("\$t0" to 0x1001000eL, "\$t1" to 2L, "\$t2" to 14L, "\$t3" to 0x79L),
                    "Resume to the next visit to port_write.",
                ),
                LoopPause(
                    "port_write",
                    mapOf("\$t0" to 0x1001000fL, "\$t1" to 1L, "\$t2" to 15L, "\$t3" to 0x71L),
                    "Set a breakpoint at done and resume there.",
                ),
                LoopPause(
                    "done",
                    mapOf("\$t0" to 0x10010010L, "\$t1" to 0L, "\$t2" to 16L),
                    "Run range-counter.asm using ordinary Run",
                ),
            )
        else -> error("Unsupported authored loop variant")
      }
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
  val labels =
      if (mode == "full") listOf("loop", "port_write")
      else if (mode == "empty") listOf("loop") else listOf("port_write", "done")
  val breakpoints = writeAction {
    labels
        .associateWith {
          XDebuggerManager.getInstance(project)
              .breakpointManager
              .addLineBreakpoint(type, file.url, line(it), null)
        }
        .toMutableMap()
  }
  val launch = launchBookProgram(project, entry, debug = true)
  try {
    val handler = launch.started.await()
    val session = checkNotNull(launch.session)
    for ((index, stop) in stops.withIndex()) {
      if (index > 0) {
        if (stop.steps == 0) bookModalUi { session.resume() }
        else
            repeat(stop.steps) {
              bookModalUi { session.stepOver(false) }
              if (it < stop.steps - 1) launch.pauses.receive()
            }
      }
      check(launch.pauses.receive() == line(stop.label)) { "Wrong $mode stop: ${stop.label}" }
      val registers = awaitBookDebugRegisters(project, handler, stop.registers)
      // On continuation the previously passed variants are skipped; the final
      // full-range pause advances directly to its invalidated Run activity.
      val next =
          if (mode == "full" && index == stops.lastIndex && resumed)
              "Run range-counter.asm using ordinary Run"
          else stop.nextText
      awaitText(next)
      record(
          "debug-$mode-$index",
          mapOf(
              "source" to entry.toString(),
              "line" to line(stop.label) + 1,
              "renderedRegisters" to registers,
              "nativeNextStepObserved" to next,
          ),
      )
      capture("debug-$mode-$index", next)
      if (mode == "full" && index == 0)
          writeAction {
            XDebuggerManager.getInstance(project)
                .breakpointManager
                .removeBreakpoint(checkNotNull(breakpoints.remove("loop")))
          }
    }
    writeAction {
      breakpoints.values.forEach {
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
        breakpoints.values.forEach {
          XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(it)
        }
      }
    }
    launch.close()
  }
}
