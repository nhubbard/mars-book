package edu.missouristate.mars.book

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
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import java.awt.Window
import java.nio.file.Path
import javax.swing.JComboBox
import javax.swing.JDialog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import training.learn.lesson.LessonManager

/** Select actual native destinations without importing progress or calling private lesson APIs. */
internal suspend fun captureBookNativeActivityNavigation(
    project: Project,
    plan: BookPackageImportPlan,
    entry: Path,
    awaitText: suspend (String) -> Unit,
    capture: suspend (String, String) -> Unit,
    click: suspend (String, Boolean) -> Unit,
): List<Map<String, Any>> {
  val receipts = mutableListOf<Map<String, Any>>()
  suspend fun action(id: String): CompletableDeferred<Unit> {
    val finished = CompletableDeferred<Unit>()
    withContext(Dispatchers.EDT) {
      val native = checkNotNull(ActionManager.getInstance().getAction(id))
      val event =
          AnActionEvent.createEvent(
              native,
              SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build(),
              null,
              ActionPlaces.MAIN_MENU,
              ActionUiKind.NONE,
              null,
          )
      check(ActionUtil.updateAction(native, event).isPerformed && event.presentation.isEnabled)
      ApplicationManager.getApplication()
          .invokeLater(
              {
                try {
                  ActionUtil.performAction(native, event)
                  finished.complete(Unit)
                } catch (error: Throwable) {
                  finished.completeExceptionally(error)
                }
              },
              ModalityState.nonModal(),
          )
    }
    return finished
  }
  suspend fun selected(source: String) {
    val expected =
        if (source == "guided") entry else entry.parent.resolve("independent/$source.asm")
    while (
        !bookModalUi {
          val document = FileEditorManager.getInstance(project).selectedTextEditor?.document
          document != null &&
              FileDocumentManager.getInstance().getFile(document)?.toNioPath() == expected
        }
    ) delay(100)
  }
  suspend fun choose(index: Int, id: String, cancel: Boolean = false) {
    val finished = action("Mars.Learning.ChooseDownloaded")
    var dialog: JDialog? = null
    while (dialog == null) {
      if (finished.isCompleted) {
        finished.await()
        error("Activity dialog did not open")
      }
      dialog = bookModalUi {
        Window.getWindows().filterIsInstance<JDialog>().singleOrNull {
          it.isShowing && it.title == "Choose a MARS lab activity"
        }
      }
      if (dialog == null) delay(100)
    }
    val root = dialog.rootPane
    val label = bookModalUi {
      val box = captureComponents(root).filterIsInstance<JComboBox<*>>().single { it.isShowing }
      check(box.itemCount == 3)
      box.selectedIndex = index
      box.selectedItem.toString()
    }
    delay(250)
    captureBookComponent(root, Path.of(plan.outputDirectory), id, label) {
      captureComponents(root)
          .filterIsInstance<JComboBox<*>>()
          .single { it.isShowing }
          .selectedItem
          .toString() == label
    }
    bookModalUi { visibleLessonButton(root, if (cancel) "Cancel" else "Open activity").doClick() }
    finished.await()
    receipts += mapOf("choice" to label, "cancelled" to cancel)
  }
  suspend fun completed(text: String) {
    awaitText(text)
    while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
  }
  action("Mars.Learning.ContinueDownloaded").await()
  if (plan.resumeActivity) {
    selected("wrapped-byte")
    completed("The narrowed-sum program passed its seven public cases.")
    capture("independent-fresh-process", "Choose another activity")
    receipts += mapOf("resumedIndependent" to "wrapped-byte", "repeatedAssessment" to false)
    choose(0, "choose-guided")
    completed("The guided runs, ordered Debug observations and two independent checks")
    capture(
        "guided-current-evidence",
        "The guided runs, ordered Debug observations and two independent checks",
    )
  } else {
    completed("The guided runs, ordered Debug observations and two independent checks")
    choose(1, "choose-first-independent")
    selected("plus-one")
    completed("The ordinary-sum program passed its seven public cases.")
    capture("independent-existing-pass", "Choose another activity")
    withContext(Dispatchers.EDT) {
      val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
      WriteCommandAction.runWriteCommandAction(project) {
        editor.document.insertString(0, "# Independent mode keeps the same source-bound check.\n")
      }
      FileDocumentManager.getInstance().saveDocument(editor.document)
    }
    action("Mars.Learning.ContinueDownloaded").await()
    selected("plus-one")
    awaitText("Check this program")
    capture("independent-edited-source", "Check this program")
    click("Check this program", false)
    completed("The ordinary-sum program passed its seven public cases.")
    receipts += mapOf("editedIndependent" to "plus-one", "recheckedThroughNativeControl" to true)
    choose(2, "choose-second-independent")
    selected("wrapped-byte")
    completed("The narrowed-sum program passed its seven public cases.")
    choose(0, "cancel-guided-choice", cancel = true)
    selected("wrapped-byte")
    capture("independent-after-cancel", "Choose another activity")
  }
  return receipts
}
