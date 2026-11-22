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
    val archive = plan.archives.single()
    val entry = archive.directory(Path.of(plan.projectRoot)).resolve(archive.entry)
    if (plan.resume) {
        withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
            val action = checkNotNull(ActionManager.getInstance().getAction("Mars.Learning.ContinueDownloaded"))
            val event = AnActionEvent.createEvent(action, SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build(),
                null, ActionPlaces.MAIN_MENU, ActionUiKind.NONE, null)
            check(ActionUtil.updateAction(action, event).isPerformed && event.presentation.isEnabled)
            ActionUtil.performAction(action, event)
        }
        awaitText("Which complete value will seb place in t1?")
        check(!bookModalUi { lessonVisibleText(root).contains("A. Eight") })
        capture("resumed-third-prediction", "Which complete value will seb place in t1?")
        focus("resumed-third", "seb")
        click("0x0000000000000096", true)
        capture("extended-feedback", "That is the zero-extended masked value in t0.")
        click("0xFFFFFFFFFFFFFF96", true)
        awaitText("Which two numbers should the program print for input 128?")
        focus("boundary", "move")
        click("128 and -128", true)
        capture("predictions-complete", "Run the program with 0, 127, 128 and 255.")
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
    if (plan.resume) check(digest == plan.expectedEntryDigests.getValue(archive.sha256))
    withContext(Dispatchers.IO) {
        Files.writeString(output.resolve("complete.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
            "resumed" to plan.resume, "entryDigests" to mapOf(archive.sha256 to digest),
            "nativeQuizLinksClicked" to true, "sourcePointersVerified" to true,
            "scope" to "Conceptual predictions and hints; execution and independent-writing credit are not claimed",
        )), CREATE_NEW)
    }
}
