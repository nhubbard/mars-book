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
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.WindowManager
import java.awt.Point
import java.awt.Robot
import java.awt.event.InputEvent
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import training.ui.LearningUiHighlightingManager
import training.learn.lesson.LessonManager

/** Actual student controls only: no private progress APIs, synthetic answers or host internals. */
internal suspend fun captureBookNativePredictions(project: Project, plan: BookPackageImportPlan, output: Path) {
    val root = bookModalUi { checkNotNull(WindowManager.getInstance().getFrame(project)).rootPane }
    suspend fun awaitText(text: String) {
        while (!bookModalUi { lessonVisibleText(root).contains(text) }) delay(100)
        delay(250)
    }
    suspend fun capture(id: String, text: String) {
        awaitText(text)
        val layout = bookModalUi { checkNotNull(lessonHorizontalLayout(root)) }
        check(layout.overflowPixels == 0) { "Lesson content is clipped: $layout" }
        captureBookComponent(root, output, id, text)
    }
    suspend fun click(label: String, choice: Boolean = false) {
        fun target(): Pair<JTextPane, Int>? = captureComponents(root).filterIsInstance<JTextPane>()
            .filter { it.isShowing }.mapNotNull { pane ->
                val text = pane.document.getText(0, pane.document.length)
                val index = if (choice) bookInlineChoiceOffset(text, label) else bookLessonActionOffset(text, label)
                index?.let { pane to it }
            }.singleOrNull()
        var point: Point? = null
        while (point == null) {
            point = bookModalUi {
                val (pane, index) = target() ?: return@bookModalUi null
                val bounds = bookLessonLinkBounds(pane, index) ?: return@bookModalUi null
                pane.scrollRectToVisible(bounds)
                if (!pane.visibleRect.intersects(bounds)) return@bookModalUi null
                Point(bounds.x + 2, bounds.y + bounds.height / 2).also { SwingUtilities.convertPointToScreen(it, pane) }
            }
            if (point == null) delay(100)
        }
        val location = point
        withContext(Dispatchers.IO) {
            Robot().apply {
                mouseMove(location.x, location.y)
                mousePress(InputEvent.BUTTON1_DOWN_MASK)
                mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            }
        }
        delay(250)
    }
    suspend fun focus(id: String, mnemonic: String) {
        val observation = bookModalUi {
            val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
            val line = editor.document.getLineNumber(editor.caretModel.offset)
            val instruction = editor.document.getText(TextRange(
                editor.document.getLineStartOffset(line), editor.document.getLineEndOffset(line))).trim()
            check(instruction.startsWith("$mnemonic ")) { "Wrong source pointer: $instruction" }
            val rectangle = checkNotNull(LearningUiHighlightingManager.getRectangle(editor.contentComponent))
            mapOf("instruction" to instruction, "line" to line + 1, "highlight" to rectangle)
        }
        withContext(Dispatchers.IO) {
            Files.writeString(output.resolve("$id-focus.json"), GsonBuilder().setPrettyPrinting().create().toJson(observation), CREATE_NEW)
        }
    }
    if (plan.memoryUnit) {
        captureBookNativeMemory(project, plan, output, ::awaitText, ::capture, ::click, ::focus)
        return
    }
    val archive = plan.archives.single()
    val entry = archive.directory(Path.of(plan.projectRoot)).resolve(archive.entry)
    val runReceipts = mutableListOf<Map<String, Any>>()
    val debugReceipts = mutableListOf<Map<String, Any>>()
    val assessmentReceipts = mutableListOf<Map<String, Any>>()
    val navigationReceipts = mutableListOf<Map<String, Any>>()
    val prerequisiteReceipts = mutableListOf<Map<String, Any>>()
    suspend fun continueLesson() {
        withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
            val action = checkNotNull(ActionManager.getInstance().getAction("Mars.Learning.ContinueDownloaded"))
            val event = AnActionEvent.createEvent(action, SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build(),
                null, ActionPlaces.MAIN_MENU, ActionUiKind.NONE, null)
            check(ActionUtil.updateAction(action, event).isPerformed && event.presentation.isEnabled)
            ActionUtil.performAction(action, event)
        }
    }
    suspend fun editComment(comment: String) {
        withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
            val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
            WriteCommandAction.runWriteCommandAction(project) { editor.document.insertString(0, "# $comment\n") }
            FileDocumentManager.getInstance().saveDocument(editor.document)
        }
    }
    suspend fun run(input: Int, id: String, editWhileWaiting: Boolean = false) {
        val launch = launchBookProgram(project, entry, debug = false)
        try {
            val handler = launch.started.await()
            while (!launch.stdout.toString().contains("Enter an integer from 0 to 255: ")) delay(50)
            if (editWhileWaiting) editComment("An in-flight result must not credit this new source revision.")
            withContext(Dispatchers.IO) { deliverBookStandardInput(handler, "$input\n") }
            check(launch.exit.await() == 0)
            val byte = input and 255
            val expected = "Enter an integer from 0 to 255: Unsigned byte: $byte\nSigned byte: ${if (byte < 128) byte else byte - 256}\n"
            check(launch.stdout.toString() == expected)
            runReceipts += mapOf("id" to id, "integerInput" to input, "stdout" to expected,
                "sourceChangedWhileWaiting" to editWhileWaiting, "exitCode" to 0)
        } finally { launch.close() }
    }
    if (plan.prerequisiteControls) {
        prerequisiteReceipts += captureBookNativePrerequisites(project, plan, entry, ::awaitText, ::capture, ::click)
    } else if (plan.activityNavigation) {
        navigationReceipts += captureBookNativeActivityNavigation(project, plan, entry, ::awaitText, ::capture, ::click)
    } else if (plan.resumeCompleted) {
        check(withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(entry)) } == plan.expectedEntryDigests.getValue(archive.sha256))
        continueLesson()
        val finalText = if (plan.assessmentFixtures.isEmpty()) "The five checked runs and the ordered Debug observations"
            else "The guided runs, ordered Debug observations and two independent checks"
        for (fixture in plan.assessmentFixtures) {
            val source = archive.directory(Path.of(plan.projectRoot)).resolve(fixture.source)
            check(withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(source)) } == fixture.sha256)
        }
        awaitText(finalText)
        while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
        capture("resumed-complete", finalText)
        check(!bookModalUi { lessonVisibleText(root).contains("Start a fresh Run of byte-inspector.asm. Enter 150") })
    } else if (plan.resume) {
        check(withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(entry)) } == plan.expectedEntryDigests.getValue(archive.sha256))
        continueLesson()
        awaitText("Which complete value will seb place in t1?")
        check(!bookModalUi { lessonVisibleText(root).contains("A. Eight") })
        capture("resumed-third-prediction", "Which complete value will seb place in t1?")
        focus("resumed-third", "seb")
        click("0x0000000000000096", true)
        capture("extended-feedback", "That is the zero-extended masked value in t0.")
        click("0xFFFFFFFFFFFFFF96", true)
        if (plan.nativeExecution) {
            awaitText("Start a fresh Run of byte-inspector.asm. Enter 150")
            click("Open a hint")
            capture("run-hint", "Use Run on this imported byte-inspector.asm file.")
            run(406, "wrong-input")
            capture("wrong-input-feedback", "This run did not consume the requested integer inputs.")
            run(150, "stale-source", editWhileWaiting = true)
            capture("stale-source-feedback", "The source or this check changed during execution.")
            run(150, "first-current-run")
            awaitText("Which two numbers should the program print for input 128?")
            editComment("Earlier Run credit must be rechecked after this edit.")
            continueLesson()
            capture("recheck-after-edit", "Start a fresh Run of byte-inspector.asm. Enter 150")
            check(!bookModalUi { bookInlineChoiceOffset(lessonVisibleText(root), "128 and -128") != null })
            run(150, "rechecked-current-run")
        }
        awaitText("Which two numbers should the program print for input 128?")
        focus("boundary", "move")
        click("128 and -128", true)
        if (plan.nativeExecution) {
            for ((input, instruction) in listOf(
                128 to "Start a fresh Run and enter 128.",
                0 to "Start a fresh Run and enter 0.",
                127 to "Start a fresh Run and enter 127.",
                255 to "Start a fresh Run and enter 255.",
            )) {
                awaitText(instruction)
                run(input, "boundary-$input")
            }
            if (plan.nativeDebug) {
                debugReceipts += captureBookNativeDebug(project, entry, ::awaitText, ::capture, ::focus,
                    if (plan.assessmentFixtures.isEmpty()) null else "Keep the ordinary sum. Work in independent/plus-one.asm.")
                if (plan.assessmentFixtures.isNotEmpty()) {
                    assessmentReceipts += captureBookNativeAssessments(project, archive.directory(Path.of(plan.projectRoot)),
                        plan.assessmentFixtures, ::awaitText, ::capture, ::click)
                }
            } else {
                while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
                capture("execution-complete", "The five checked runs cover the example and the byte boundaries")
            }
        } else capture("predictions-complete", "Run the program with 0, 127, 128 and 255.")
    } else {
        capture("first-prediction", "How many bit positions can remain nonzero after the mask?")
        focus("mask", "andi")
        click("Open a hint")
        capture("mask-hint", "Write 0xFF as binary and count the one bits.")
        click("Sixteen", true)
        capture("mask-feedback", "The immediate field can encode sixteen bits")
        click("Eight", true)
        awaitText("What unsigned value does that byte represent?")
        click("Open a hint")
        awaitText("Start with the contribution of bit seven")
        click("Open a hint")
        capture("second-hint", "Alternatively, calculate 9 times 16 plus 6.")
        click("96", true)
        capture("unsigned-feedback", "96 is the pair of hexadecimal digits read as decimal text.")
        click("150", true)
        capture("pause-third-prediction", "Which complete value will seb place in t1?")
        withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
            val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
            WriteCommandAction.runWriteCommandAction(project) { editor.document.insertString(0, "# Student notes survive continuing this edition.\n") }
            FileDocumentManager.getInstance().saveDocument(editor.document)
        }
    }
    val digest = withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(entry)) }
    if (plan.resume && !plan.nativeExecution) check(digest == plan.expectedEntryDigests.getValue(archive.sha256))
    withContext(Dispatchers.IO) {
        Files.writeString(output.resolve("complete.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
            "resumed" to plan.resume, "entryDigests" to mapOf(archive.sha256 to digest),
            "nativeQuizLinksClicked" to !plan.resumeCompleted, "sourcePointersVerified" to !plan.resumeCompleted, "runs" to runReceipts,
            "debug" to debugReceipts, "assessments" to assessmentReceipts, "navigation" to navigationReceipts, "prerequisites" to prerequisiteReceipts, "resumedCompleted" to (plan.resumeCompleted && !plan.prerequisiteControls),
            "startedFromCompletedUnit" to plan.resumeCompleted,
            "lessonRunningAtCaptureEnd" to bookModalUi { LessonManager.instance.lessonIsRunning() },
            "scope" to if (plan.prerequisiteControls) "Native instructor prerequisites, transitive current-source checks, saved policy and deliberate release"
                else if (plan.activityNavigation) "Native guided and independent selection, preserved check history and direct fresh-process continuation"
                else if (plan.assessmentFixtures.isNotEmpty()) "Predictions, actual Run, ordered Debug and two independent public-case assessments"
                else if (plan.nativeDebug) "Predictions, actual Run and ordered Debug observations; no independent-writing credit claimed"
                else if (plan.nativeExecution) "Predictions and actual Run observations; no Debug or independent-writing credit claimed"
                else "Conceptual predictions and hints; execution and independent-writing credit are not claimed",
        )), CREATE_NEW)
    }
}
