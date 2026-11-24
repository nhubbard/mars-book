package edu.missouristate.mars.book

import com.intellij.openapi.application.EDT
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import training.learn.lesson.LessonManager

/** Use real lesson controls with explicitly pinned private fixtures, confined to authoring. */
internal suspend fun captureBookNativeAssessments(
    project: Project,
    directory: Path,
    fixtures: List<BookAssessmentFixture>,
    awaitText: suspend (String) -> Unit,
    capture: suspend (String, String) -> Unit,
    click: suspend (String, Boolean) -> Unit,
): List<Map<String, Any>> {
  val receipts = mutableListOf<Map<String, Any>>()
  for (fixture in fixtures.sortedBy { it.source }) {
    awaitText("Work in ${fixture.source}.")
    val entry = directory.resolve(fixture.source)
    val before = bookModalUi {
      val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
      check(FileDocumentManager.getInstance().getFile(editor.document)?.toNioPath() == entry)
      captureSha256(editor.document.text.toByteArray())
    }
    click("Check this program", false)
    capture("${entry.fileName}-starter-failed", "Public cases passed: 0 of 7.")
    val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(Path.of(fixture.path)) }
    check(captureSha256(bytes) == fixture.sha256) { "Changed private authoring fixture" }
    withContext(Dispatchers.EDT) {
      val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
      check(FileDocumentManager.getInstance().getFile(editor.document)?.toNioPath() == entry)
      WriteCommandAction.runWriteCommandAction(project) {
        editor.document.setText(bytes.toString(Charsets.UTF_8))
      }
      // Leave edits unsaved: the production check must assemble the current editor text.
    }
    click("Check this program", false)
    val success =
        if (fixture.source.endsWith("/plus-one.asm"))
            "The ordinary-sum program passed its seven public cases."
        else "The narrowed-sum program passed its seven public cases."
    capture("${entry.fileName}-passed", success)
    // Persist through the normal document manager for the fresh-process continuation test.
    withContext(Dispatchers.EDT) { FileDocumentManager.getInstance().saveAllDocuments() }
    receipts +=
        mapOf(
            "source" to fixture.source,
            "starterSha256" to before,
            "solutionSha256" to fixture.sha256,
            "starterCasesPassed" to 0,
            "solutionCasesPassed" to 7,
            "checkedUnsavedText" to true,
        )
  }
  awaitText("The guided runs, ordered Debug observations and two independent checks")
  while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
  capture(
      "assessments-complete",
      "The guided runs, ordered Debug observations and two independent checks",
  )
  return receipts
}
