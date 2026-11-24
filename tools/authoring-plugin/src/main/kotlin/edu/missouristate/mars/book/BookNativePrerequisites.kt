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
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.awt.Rectangle
import java.awt.Window
import java.nio.file.Path
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JDialog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import training.learn.lesson.LessonManager

/**
 * Drive real instructor controls and student feedback; never seed progress or call policy
 * internals.
 */
internal suspend fun captureBookNativePrerequisites(
    project: Project,
    plan: BookPackageImportPlan,
    entry: Path,
    awaitText: suspend (String) -> Unit,
    capture: suspend (String, String) -> Unit,
    click: suspend (String, Boolean) -> Unit,
): List<Map<String, Any>> {
  val receipts = mutableListOf<Map<String, Any>>()
  suspend fun action(id: String): CompletableDeferred<Unit> {
    val done = CompletableDeferred<Unit>()
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
                  done.complete(Unit)
                } catch (error: Throwable) {
                  done.completeExceptionally(error)
                }
              },
              ModalityState.nonModal(),
          )
    }
    return done
  }
  suspend fun modal(id: String, title: String, operate: suspend (JDialog) -> Unit) {
    val done = action(id)
    var dialog: JDialog? = null
    while (dialog == null) {
      if (done.isCompleted) {
        done.await()
        error("Expected native dialog: $title")
      }
      dialog = bookModalUi {
        Window.getWindows().filterIsInstance<JDialog>().singleOrNull {
          it.isShowing && it.title == title
        }
      }
      if (dialog == null) delay(100)
    }
    operate(dialog)
    done.await()
  }
  suspend fun choose(index: Int) {
    modal("Mars.Learning.ChooseDownloaded", "Choose a MARS lab activity") { dialog ->
      bookModalUi {
        captureComponents(dialog.rootPane)
            .filterIsInstance<JComboBox<*>>()
            .single { it.isShowing }
            .selectedIndex = index
        visibleLessonButton(dialog.rootPane, "Open activity").doClick()
      }
    }
  }
  suspend fun configure(cancel: Boolean, releaseWrapped: Boolean = false) {
    modal(
        "Mars.Learning.ConfigureDownloadedPrerequisites",
        "Configure downloaded lab prerequisites",
    ) { dialog ->
      fun boxes() =
          captureComponents(dialog.rootPane).filterIsInstance<JCheckBox>().filter { it.isShowing }
      fun set(id: String, selected: Boolean) {
        val box = boxes().single { it.text.endsWith(": $id") }
        check(box.width > 0 && box.height > 0)
        val target = Rectangle(0, 0, box.width, box.height)
        box.scrollRectToVisible(target)
        check(box.visibleRect.contains(target)) { "Prerequisite control is clipped: $id" }
        if (box.isSelected != selected) box.doClick()
      }
      bookModalUi {
        val box =
            captureComponents(dialog.rootPane).filterIsInstance<JComboBox<*>>().single {
              it.isShowing
            }
        box.selectedIndex = 0
      }
      delay(100)
      bookModalUi {
        if (plan.resumePrerequisites) {
          check(
              boxes().filter { it.isSelected }.map { it.text.substringAfter(": ") }.toSet() ==
                  setOf("mask-bits", "run-0", "debug-150"),
          )
        } else check(boxes().none { it.isSelected })
        set("mask-bits", true)
        set("run-0", true)
        set("debug-150", true)
        captureComponents(dialog.rootPane)
            .filterIsInstance<JComboBox<*>>()
            .single { it.isShowing }
            .selectedIndex = 1
      }
      delay(100)
      bookModalUi {
        if (plan.resumePrerequisites)
            check(boxes().single { it.text.endsWith(": independent-plus-one") }.isSelected)
        else check(boxes().none { it.isSelected })
        set("independent-plus-one", !releaseWrapped)
      }
      delay(250)
      captureBookComponent(
          dialog.rootPane,
          Path.of(plan.outputDirectory),
          if (cancel) "prerequisites-cancel"
          else if (releaseWrapped) "prerequisites-release" else "prerequisites-save",
          "Select activities that must remain complete",
      )
      bookModalUi {
        visibleLessonButton(dialog.rootPane, if (cancel) "Cancel" else "Save requirements")
            .doClick()
      }
    }
    receipts +=
        mapOf(
            "configuredNativeControls" to true,
            "cancelled" to cancel,
            "releasedWrappedOnly" to releaseWrapped,
        )
  }
  suspend fun complete(text: String) {
    awaitText(text)
    while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
  }
  action("Mars.Learning.ContinueDownloaded").await()
  if (plan.resumePrerequisites) {
    awaitText("Start a fresh Run of byte-inspector.asm. Enter 150")
    choose(2)
    awaitText("Check this program")
    click("Check this program", false)
    capture("persisted-transitive-block", "No assessment attempt was used.")
    configure(cancel = false, releaseWrapped = true)
    complete("The narrowed-sum program passed its seven public cases.")
    capture("released-existing-pass", "Choose another activity")
    choose(1)
    awaitText("Check this program")
    click("Check this program", false)
    capture("first-exercise-still-restricted", "Required earlier activities:")
    choose(0)
    awaitText("Start a fresh Run of byte-inspector.asm. Enter 150")
    capture("guided-still-needs-current-run", "Start a fresh Run of byte-inspector.asm. Enter 150")
    receipts +=
        mapOf(
            "savedRulesLoaded" to true,
            "wrappedReleasedWithoutAssessment" to true,
            "firstStillRestricted" to true,
            "guidedSourceEvidenceStillRequired" to true,
        )
  } else {
    complete("The guided runs, ordered Debug observations and two independent checks")
    configure(cancel = true)
    configure(cancel = false)
    choose(2)
    complete("The narrowed-sum program passed its seven public cases.")
    withContext(Dispatchers.EDT) {
      val file = checkNotNull(LocalFileSystem.getInstance().findFileByNioFile(entry))
      val document = checkNotNull(FileDocumentManager.getInstance().getDocument(file))
      WriteCommandAction.runWriteCommandAction(project) {
        document.insertString(
            0,
            "# Current prerequisite observations must follow this source revision.\n",
        )
      }
      FileDocumentManager.getInstance().saveDocument(document)
    }
    action("Mars.Learning.ContinueDownloaded").await()
    awaitText("Check this program")
    click("Check this program", false)
    capture("transitive-source-block", "No assessment attempt was used.")
    capture("transitive-required-list", "Required earlier activities:")
    // Return to guided work without executing it, so the next process verifies
    // both the persisted course rule and the source-dependent observation loss.
    choose(0)
    awaitText("Start a fresh Run of byte-inspector.asm. Enter 150")
    receipts +=
        mapOf(
            "transitiveSourceEditBlocked" to true,
            "nativeCheckClicked" to true,
            "assessmentAttemptExpected" to false,
        )
  }
  return receipts
}
