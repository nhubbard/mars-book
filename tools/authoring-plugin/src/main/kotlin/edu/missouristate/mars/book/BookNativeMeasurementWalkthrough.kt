package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
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
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import java.awt.Point
import java.awt.Robot
import java.awt.event.InputEvent
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import javax.swing.JButton
import javax.swing.JEditorPane
import javax.swing.SwingUtilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import training.learn.lesson.LessonManager

/** Separate publication automation: read the pinned lesson and operate real student controls. */
internal suspend fun captureBookNativeMeasurement(
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
  // Production MARS has already validated/imported this exact archive and every activity.
  // This trusted authoring reader follows those activities; it does not validate student content.
  val lesson =
      withContext(Dispatchers.IO) {
        JsonParser.parseString(Files.readString(directory.resolve("lesson.json"))).asJsonObject
      }
  val steps = lesson.getAsJsonArray("steps").map { it.asJsonObject }
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
  fun visible(step: JsonObject): String =
      step
          .get(if (step.get("kind").asString == "prediction") "question" else "explanation")
          .asString
  fun nextText(index: Int) =
      steps
          .drop(index + 1)
          .firstOrNull { !plan.resume || it.get("kind").asString != "prediction" }
          ?.let(::visible) ?: lesson.get("conclusion").asString
  if (plan.resume) continueLesson()
  val fixture = plan.assessmentFixtures.single()
  val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(Path.of(fixture.path)) }
  check(captureSha256(bytes) == fixture.sha256) { "Changed private authoring fixture" }
  var firstPrediction = true
  for ((index, step) in steps.withIndex()) {
    val id = step.get("id").asString
    val kind = step.get("kind").asString
    val relative = step.get("source")?.asString ?: archive.entry
    val path = directory.resolve(relative)
    when (kind) {
      "prediction" ->
          if (!plan.resume) {
            val question = step.get("question").asString
            awaitText(question)
            selected(entry)
            focus(id, step.getAsJsonObject("anchor").get("mnemonic").asString)
            capture(id, question)
            val choices = step.getAsJsonArray("choices").map { it.asJsonObject }
            val correct =
                choices
                    .single { it.get("id").asString == step.get("correctChoice").asString }
                    .get("text")
                    .asString
            val wrong =
                if (firstPrediction)
                    choices.first { it.get("text").asString != correct }.get("text").asString
                else null
            if (wrong != null) click(wrong, true)
            click(correct, true)
            record(
                id,
                mapOf(
                    "actualInlineChoiceClicked" to correct,
                    "wrongChoiceFirst" to (wrong ?: "none"),
                ),
            )
            firstPrediction = false
          }
      "debug" -> {
        awaitText(visible(step))
        selected(path)
        val rawStops = step.getAsJsonArray("stops").map { it.asJsonObject }
        val stops = rawStops.mapIndexed { stopIndex, stop ->
          MeasurementPause(
              stop.getAsJsonObject("anchor").get("label").asString,
              stop.getAsJsonObject("registers").entrySet().associate { (register, value) ->
                register to value.asString.removePrefix("0x").toULong(16).toLong()
              },
              rawStops.getOrNull(stopIndex + 1)?.get("explanation")?.asString ?: nextText(index),
          )
        }
        val cache = id.removePrefix("trace-").takeIf { it.startsWith("cache-") }
        captureMeasurementDebug(project, path, ::record, awaitText, capture, id, stops, cache)
      }
      "run" -> {
        awaitText(visible(step))
        selected(path)
        val run = launchBookProgram(project, path, debug = false)
        try {
          run.started.await()
          check(run.exit.await() == 0 && run.stdout.toString().isEmpty())
          record(id, mapOf("source" to relative, "exitCode" to 0, "stdout" to ""))
        } finally {
          run.close()
        }
        capture("after-$id", "")
      }
      "assessment" -> {
        awaitText(visible(step))
        selected(path)
        if (id == "matrix-vector") {
          if (!plan.resume) {
            click("Check this program", false)
            capture("measurement-starter", "Public cases passed: 0 of 11.")
            withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
              val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
              check(FileDocumentManager.getInstance().getFile(editor.document)?.toNioPath() == path)
              WriteCommandAction.runWriteCommandAction(project) {
                editor.document.setText(bytes.toString(Charsets.UTF_8))
              }
            }
          } else
              check(
                  withContext(Dispatchers.IO) { Files.readString(path) } ==
                      "# Recheck this edited independent source.\n" +
                          bytes.toString(Charsets.UTF_8),
              )
          click("Check this program", false)
          capture("measurement-passed", step.get("success").asString)
          record(
              id,
              mapOf(
                  "source" to relative,
                  "solutionSha256" to fixture.sha256,
                  "completedPublicCases" to 11,
                  "starterPassedPublicCases" to if (plan.resume) "not rerun" else "0 of 11",
                  "checkedUnsavedText" to !plan.resume,
              ),
          )
        } else {
          click("Check this program", false)
          capture("measurement-exit-passed", step.get("success").asString)
          record(id, mapOf("source" to relative, "completedPublicCases" to 1))
        }
      }
      else -> error("Unexpected measurement activity: $kind")
    }
  }
  withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
    FileDocumentManager.getInstance().saveAllDocuments()
  }
  val conclusion = lesson.get("conclusion").asString
  awaitText(conclusion)
  while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
  capture("measurement-unit-complete", conclusion)
  // Separate optional experiment: click the native Reset button while paused before the call.
  // Never replace the cold-launch counters recorded by the actual guided activities above.
  for (cache in listOf("cache-ordinary", "cache-tiled")) {
    val source = directory.resolve("worked/$cache.asm")
    val step = steps.single { it.get("id").asString == "trace-$cache" }
    val stops =
        step
            .getAsJsonArray("stops")
            .take(2)
            .map { it.asJsonObject }
            .map { stop ->
              MeasurementPause(
                  stop.getAsJsonObject("anchor").get("label").asString,
                  stop.getAsJsonObject("registers").entrySet().associate { (register, value) ->
                    register to value.asString.removePrefix("0x").toULong(16).toLong()
                  },
                  conclusion,
              )
            }
    captureMeasurementDebug(
        project,
        source,
        ::record,
        awaitText,
        capture,
        "reset-$cache",
        stops,
        cache,
        resetAtCall = true,
    )
  }
  val sources =
      listOf(
          archive.entry,
          "worked/matrix-tiled.asm",
          "worked/matrix-wide.asm",
          "worked/cache-ordinary.asm",
          "worked/cache-tiled.asm",
          fixture.source,
      )
  if (!plan.resume) {
    for (relative in sources) {
      val path = directory.resolve(relative)
      val note =
          if (relative == fixture.source) "Recheck this edited independent source."
          else "Recheck this edited worked source."
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
    capture("measurement-source-invalidated", "Start a fresh Debug session for matrix-measure.asm.")
  }
  val digests =
      withContext(Dispatchers.IO) {
        sources.associateWith { captureSha256(Files.readAllBytes(directory.resolve(it))) }
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
                        "Actual inline predictions, nineteen matrix Debug boundaries, five ordinary Runs, independent function/caller and exit checks, and visible stopped cache Reset. Human learner review remains separate.",
                ),
            ),
        CREATE_NEW,
    )
  }
}

private data class MeasurementPause(
    val label: String,
    val registers: Map<String, Long>,
    val nextText: String,
)

private suspend fun captureMeasurementDebug(
    project: Project,
    entry: Path,
    record: suspend (String, Map<String, Any>) -> Unit,
    awaitText: suspend (String) -> Unit,
    capture: suspend (String, String) -> Unit,
    prefix: String,
    stops: List<MeasurementPause>,
    cache: String?,
    resetAtCall: Boolean = false,
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
  val manager = XDebuggerManager.getInstance(project).breakpointManager
  val breakpoints = writeAction {
    mutableListOf(manager.addLineBreakpoint(type, file.url, line(stops.first().label), null))
  }
  val name = "Book measurement $prefix Debug"
  val launch = launchBookProgram(project, entry, debug = true, name = name)
  try {
    val handler = launch.started.await()
    val session = checkNotNull(launch.session)
    for ((index, stop) in stops.withIndex()) {
      if (index > 0) {
        writeAction {
          breakpoints.forEach(manager::removeBreakpoint)
          breakpoints.clear()
          breakpoints += manager.addLineBreakpoint(type, file.url, line(stop.label), null)
        }
        bookModalUi { session.resume() }
      }
      check(launch.pauses.receive() == line(stop.label)) { "Wrong matrix stop: ${stop.label}" }
      val registers = awaitBookDebugRegisters(project, handler, stop.registers)
      awaitText(stop.nextText)
      val facts =
          mutableMapOf<String, Any>(
              "source" to entry.toString(),
              "line" to line(stop.label) + 1,
              "renderedRegisters" to registers,
              "nativeNextStepObserved" to stop.nextText,
          )
      if (cache != null) {
        val tiled = cache == "cache-tiled"
        val accesses =
            when (index) {
              0 -> 1L
              1 -> (if (tiled) 12841L else 8745L) - if (resetAtCall) 1 else 0
              else -> if (tiled) 12842L else 8746L
            }
        val misses =
            if (index == 0) 1L else (if (tiled) 1068L else 5388L) + if (index == 2) 1 else 0
        facts["nativeCacheRows"] = awaitMeasurementCache(project, name, accesses, misses)
        if (resetAtCall && index == 0) {
          clickMeasurementReset(project)
          facts["afterVisibleReset"] = awaitMeasurementCache(project, name, 0, 0)
        }
      }
      record("debug-$prefix-$index", facts)
      capture("debug-$prefix-$index", stop.nextText)
    }
    writeAction {
      breakpoints.forEach(manager::removeBreakpoint)
      breakpoints.clear()
    }
    bookModalUi { session.resume() }
    check(launch.exit.await() == 0 && launch.stdout.toString().isEmpty())
  } finally {
    withContext(NonCancellable + Dispatchers.EDT) {
      launch.session?.stop()
      writeAction { breakpoints.forEach(manager::removeBreakpoint) }
    }
    launch.close()
  }
}

private suspend fun awaitMeasurementCache(
    project: Project,
    execution: String,
    accesses: Long,
    misses: Long,
): List<String> {
  while (true) {
    val rows = bookModalUi {
      val tool =
          ToolWindowManager.getInstance(project).getToolWindow("MARS Tools")
              ?: return@bookModalUi null
      if (!tool.isVisible) {
        tool.show()
        return@bookModalUi null
      }
      val content =
          tool.contentManager.contents.singleOrNull { it.displayName == "Data Cache Simulator" }
              ?: return@bookModalUi null
      if (tool.contentManager.selectedContent != content) {
        tool.contentManager.setSelectedContent(content, false)
        return@bookModalUi null
      }
      val components = captureComponents(content.component).toList()
      if (selectBookToolExecution(components, execution) == null) return@bookModalUi null
      val text =
          components.filterIsInstance<JEditorPane>().map {
            it.document.getText(0, it.document.length).trim().replace(Regex("\\s+"), " ")
          }
      val expected =
          listOf("256 bytes | Accesses $accesses", "Hits ${accesses - misses} | Misses $misses")
      if (text.containsAll(expected)) expected else null
    }
    if (rows != null) return rows
    delay(100)
  }
}

private suspend fun clickMeasurementReset(project: Project) {
  val location = bookModalUi {
    val tool = checkNotNull(ToolWindowManager.getInstance(project).getToolWindow("MARS Tools"))
    val content = tool.contentManager.contents.single { it.displayName == "Data Cache Simulator" }
    val button =
        captureComponents(content.component).filterIsInstance<JButton>().single {
          it.isShowing && it.isEnabled && it.text == "Reset"
        }
    Point(button.width / 2, button.height / 2).also {
      SwingUtilities.convertPointToScreen(it, button)
    }
  }
  withContext(Dispatchers.IO) {
    Robot().apply {
      mouseMove(location.x, location.y)
      mousePress(InputEvent.BUTTON1_DOWN_MASK)
      mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    }
  }
}
