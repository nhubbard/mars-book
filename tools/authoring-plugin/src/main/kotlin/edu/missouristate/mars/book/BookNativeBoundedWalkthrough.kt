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
internal suspend fun captureBookNativeBounded(
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
  val short = directory.resolve("worked/packet-short.asm")
  val checksum = directory.resolve("worked/packet-checksum.asm")
  if (plan.resume) continueLesson()
  else {
    predict(
        "header-first",
        "With seven available bytes and a wrong first magic byte, which status is required?",
        "Need more, minus one",
        "bltuc",
        "Wrong magic, minus two",
    )
    predict(
        "wire-and-native",
        "Which four bytes represent that output word in increasing address order?",
        "34 12 00 00",
        "lbu",
    )
    predict("three-lengths", "How many bytes does this call consume on success?", "Twelve", "addiu")
  }
  awaitText("Start a fresh Debug session for bounded-packet.asm.")
  selected(entry)
  captureBoundedDebug(
      project,
      entry,
      ::record,
      awaitText,
      capture,
      "accepted",
      acceptedBoundedStops(
          if (plan.resume) "Start a fresh Debug session for worked/packet-short.asm."
          else "Why did minus six at packet_commit not mean the returned call failed?",
      ),
  )
  if (!plan.resume) {
    predict(
        "tentative-status",
        "Why did minus six at packet_commit not mean the returned call failed?",
        "The success assignments had not executed yet",
        "move",
    )
  }
  awaitText("Start a fresh Debug session for worked/packet-short.asm.")
  selected(short)
  captureBoundedDebug(
      project,
      short,
      ::record,
      awaitText,
      capture,
      "short",
      shortBoundedStops(
          if (plan.resume) "Start a fresh Debug session for worked/packet-checksum.asm."
          else "How many available bytes are needed to accept an empty payload packet?",
      ),
  )
  if (!plan.resume) {
    predict(
        "empty-payload-trailer",
        "How many available bytes are needed to accept an empty payload packet?",
        "Nine",
        "bltuc",
    )
  }
  awaitText("Start a fresh Debug session for worked/packet-checksum.asm.")
  selected(checksum)
  captureBoundedDebug(
      project,
      checksum,
      ::record,
      awaitText,
      capture,
      "checksum",
      checksumBoundedStops(
          if (plan.resume) "Run bounded-packet.asm using ordinary Run."
          else "Which observation distinguishes that forbidden write-and-restore path?",
      ),
  )
  if (!plan.resume) {
    predict(
        "store-history",
        "Which observation distinguishes that forbidden write-and-restore path?",
        "The execution store history",
        "sw",
    )
  }
  for ((id, relative) in
      listOf(
          "accepted" to archive.entry,
          "short" to "worked/packet-short.asm",
          "checksum" to "worked/packet-checksum.asm",
      )) {
    val path = directory.resolve(relative)
    awaitText("Run $relative using ordinary Run.")
    selected(path)
    val run = launchBookProgram(project, path, debug = false)
    try {
      run.started.await()
      check(run.exit.await() == 0 && run.stdout.toString().isEmpty())
      record("$id-run", mapOf("source" to relative, "exitCode" to 0, "stdout" to ""))
    } finally {
      run.close()
    }
    capture("after-$id-run", "")
  }
  if (!plan.resume) {
    predict(
        "delimiter-at-capacity",
        "What result is required for 32 non-LF bytes followed by LF?",
        "Success consuming thirty-three bytes",
        "bltuc",
    )
  }
  val fixture = plan.assessmentFixtures.single()
  val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(Path.of(fixture.path)) }
  check(captureSha256(bytes) == fixture.sha256) { "Changed private authoring fixture" }
  awaitText("Implement read_line in the independent source, replacing break.")
  val independent = directory.resolve(fixture.source)
  selected(independent)
  if (!plan.resume) {
    click("Check this program", false)
    capture("bounded-starter", "Public cases passed: 0 of 139.")
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
  capture("bounded-passed", "The current source passes all 139 public cases in this profile.")
  record(
      "independent",
      mapOf(
          "source" to fixture.source,
          "solutionSha256" to fixture.sha256,
          "starterPassedPublicCases" to if (plan.resume) "not rerun" else "0 of 139",
          "completedPublicCases" to 139,
          "checkedUnsavedText" to !plan.resume,
      ),
  )
  awaitText("Check the same independent source against all 256 possible first-byte values")
  selected(independent)
  click("Check this program", false)
  capture("bounded-bytes-passed", "The current source passes all 256 public cases in this profile.")
  record("all-bytes", mapOf("source" to fixture.source, "completedPublicCases" to 256))
  awaitText("Check the same independent source as a complete program.")
  selected(independent)
  click("Check this program", false)
  capture(
      "bounded-exit-passed",
      "The independent parser passes both public function profiles and complete silent execution.",
  )
  record("caller-exit", mapOf("source" to fixture.source, "completedPublicCases" to 1))
  withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
    FileDocumentManager.getInstance().saveAllDocuments()
  }
  awaitText(
      "Explain a parser using its input window, ordered decisions, accepted output and borrowed-storage lifetime.",
  )
  while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
  capture(
      "bounded-unit-complete",
      "Explain a parser using its input window, ordered decisions, accepted output and borrowed-storage lifetime.",
  )
  if (!plan.resume) {
    for ((path, note) in
        listOf(
            entry to "Recheck accepted execution after this edit.",
            short to "Recheck short-header execution after this edit.",
            checksum to "Recheck checksum-failure execution after this edit.",
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
    capture("bounded-source-invalidated", "Start a fresh Debug session for bounded-packet.asm.")
  }
  val digests =
      withContext(Dispatchers.IO) {
        listOf(
                archive.entry,
                "worked/packet-short.asm",
                "worked/packet-checksum.asm",
                fixture.source,
            )
            .associateWith { captureSha256(Files.readAllBytes(directory.resolve(it))) }
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
                        "Actual inline predictions, twelve packet Debug boundaries, three ordinary Runs, binary delimiter and complete caller checks and continuation. Human learner review is separate verification.",
                ),
            ),
        CREATE_NEW,
    )
  }
}

private data class BoundedPause(
    val label: String,
    val registers: Map<String, Long>,
    val nextText: String,
)

private fun acceptedBoundedStops(next: String) =
    listOf(
        BoundedPause(
            "parse_packet",
            mapOf(
                "\$sp" to 2147479536L,
                "\$a0" to 268500996L,
                "\$a1" to 12L,
                "\$a2" to 268501256L,
                "\$v0" to 0L,
                "\$v1" to 0L,
            ),
            "Resume to packet_length.",
        ),
        BoundedPause(
            "packet_length",
            mapOf("\$sp" to 2147479536L, "\$a1" to 12L, "\$t1" to 1L, "\$v0" to -4L, "\$v1" to 0L),
            "Resume to packet_fields.",
        ),
        BoundedPause(
            "packet_fields",
            mapOf(
                "\$sp" to 2147479536L,
                "\$t0" to 3L,
                "\$t1" to 1L,
                "\$t8" to 12L,
                "\$t9" to 64L,
                "\$v0" to -1L,
                "\$v1" to 0L,
            ),
            "Resume to the first payload_sum.",
        ),
        BoundedPause(
            "payload_sum",
            mapOf(
                "\$sp" to 2147479536L,
                "\$t0" to 3L,
                "\$t2" to 4660L,
                "\$t3" to 232L,
                "\$t4" to 0L,
                "\$t5" to 268501004L,
                "\$t6" to 3L,
                "\$v0" to -1L,
                "\$v1" to 0L,
            ),
            "Resume to the next payload_sum.",
        ),
        BoundedPause(
            "payload_sum",
            mapOf(
                "\$sp" to 2147479536L,
                "\$t2" to 4660L,
                "\$t3" to 359L,
                "\$t4" to 127L,
                "\$t5" to 268501005L,
                "\$t6" to 2L,
                "\$v0" to -1L,
                "\$v1" to 0L,
            ),
            "Resume to trailer_sum.",
        ),
        BoundedPause(
            "trailer_sum",
            mapOf(
                "\$sp" to 2147479536L,
                "\$t0" to 3L,
                "\$t3" to 742L,
                "\$t4" to 510L,
                "\$t5" to 268501007L,
                "\$t6" to 0L,
                "\$v0" to -1L,
                "\$v1" to 0L,
            ),
            "Resume to packet_commit before its first instruction.",
        ),
        BoundedPause(
            "packet_commit",
            mapOf(
                "\$sp" to 2147479536L,
                "\$t0" to 3L,
                "\$t1" to 1L,
                "\$t2" to 4660L,
                "\$t3" to 0L,
                "\$t4" to 510L,
                "\$t8" to 12L,
                "\$v0" to -6L,
                "\$v1" to 0L,
            ),
            "Resume to parse_ready before its nop.",
        ),
        BoundedPause(
            "parse_ready",
            mapOf(
                "\$sp" to 2147479536L,
                "\$t0" to 3L,
                "\$t2" to 4660L,
                "\$t3" to 0L,
                "\$t4" to 254L,
                "\$v0" to 12L,
                "\$v1" to 268501004L,
            ),
            next,
        ),
    )

private fun shortBoundedStops(next: String) =
    listOf(
        BoundedPause(
            "parse_packet",
            mapOf(
                "\$sp" to 2147479536L,
                "\$a0" to 268500996L,
                "\$a1" to 7L,
                "\$a2" to 268501256L,
                "\$v0" to 0L,
                "\$v1" to 0L,
            ),
            "Resume to parse_ready.",
        ),
        BoundedPause(
            "parse_ready",
            mapOf("\$sp" to 2147479536L, "\$a1" to 7L, "\$t9" to 8L, "\$v0" to -1L, "\$v1" to 0L),
            next,
        ),
    )

private fun checksumBoundedStops(next: String) =
    listOf(
        BoundedPause(
            "trailer_sum",
            mapOf(
                "\$sp" to 2147479536L,
                "\$t0" to 3L,
                "\$t2" to 4660L,
                "\$t3" to 742L,
                "\$t4" to 510L,
                "\$t5" to 268501007L,
                "\$t6" to 0L,
                "\$v0" to -1L,
                "\$v1" to 0L,
            ),
            "Resume to parse_ready.",
        ),
        BoundedPause(
            "parse_ready",
            mapOf(
                "\$sp" to 2147479536L,
                "\$t0" to 3L,
                "\$t3" to 1L,
                "\$t4" to 510L,
                "\$v0" to -6L,
                "\$v1" to 0L,
            ),
            next,
        ),
    )

private suspend fun captureBoundedDebug(
    project: Project,
    entry: Path,
    record: suspend (String, Map<String, Any>) -> Unit,
    awaitText: suspend (String) -> Unit,
    capture: suspend (String, String) -> Unit,
    prefix: String,
    stops: List<BoundedPause>,
) {
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
  val launch = launchBookProgram(project, entry, debug = true, name = "Book bounded $prefix Debug")
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
      check(launch.pauses.receive() == line(stop.label)) { "Wrong Bounded stop: ${stop.label}" }
      val registers = awaitBookDebugRegisters(project, handler, stop.registers)
      awaitText(stop.nextText)
      record(
          "debug-$prefix-$index",
          mapOf(
              "source" to entry.toString(),
              "line" to line(stop.label) + 1,
              "renderedRegisters" to registers,
              "nativeNextStepObserved" to stop.nextText,
          ),
      )
      capture("debug-$prefix-$index", stop.nextText)
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
