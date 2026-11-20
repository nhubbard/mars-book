package edu.missouristate.mars.book

import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookPresentationPlanTest {
    private val root = Path.of("/tmp/fixture")
    private val source = BookTextCallout("Data region", "# BEGIN STUDENT DATA\n# END STUDENT DATA")
    private val editor = BookCaptureFigure(id = "starter", source = "starter.asm", sourceSha256 = "a".repeat(64), mode = "editor", editorCallouts = listOf(source))
    private val view = BookRunView("Prompt: ", "Prompt: Answer: 41\n", "Prompt: 20\nAnswer: 41\n", listOf(BookTextCallout("Answer", "Answer: 41")))
    private val run = editor.copy(id = "console", mode = "run", editorCallouts = emptyList(), standardInput = "20\n", runView = view)

    @Test
    fun `editor and completed Run have distinct explicit observation contracts`() {
        editor.validate(root)
        run.validate(root)
    }

    @Test
    fun `rejects mixed modes missing process evidence and invented source stops`() {
        for (bad in listOf(editor.copy(mode = "unknown"), editor.copy(checkpoint = "main"), editor.copy(editorCallouts = emptyList()),
                           editor.copy(standardInput = "20\n"), editor.copy(stepFrom = listOf("main")), editor.copy(runView = view),
                           editor.copy(expectedRows = listOf(BookExpectedRow("Value", listOf("41")))),
                           run.copy(runView = null), run.copy(standardInput = null), run.copy(editorCallouts = listOf(source)),
                           run.copy(mode = "debug", checkpoint = "main"))) {
            assertThrows<IllegalArgumentException> { bad.validate(root) }
        }
    }

    @Test
    fun `input-free Run has exact output without an invented prompt or echo`() {
        val report = BookRunView(stdout = "signed: -1\n", consoleText = "signed: -1\n", callouts = listOf(BookTextCallout("Signed value", "signed: -1")))
        val automatic = run.copy(standardInput = null, runView = report)
        automatic.validate(root)
        for (bad in listOf(automatic.copy(runView = report.copy(prompt = "signed: ")),
                           automatic.copy(runView = report.copy(consoleText = "signed: -1\nextra echo")),
                           automatic.copy(standardInput = ""),
                           run.copy(runView = view.copy(prompt = "")))) {
            assertThrows<IllegalArgumentException> { bad.validate(root) }
        }
    }

    @Test
    fun `exact text targets use UTF16 editor offsets and reject missing or ambiguous text`() {
        assertEquals(3..8, uniqueBookTextRange("😀\nanswer\n", "answer"))
        for (target in listOf("", " ", "missing", "same")) {
            assertThrows<IllegalArgumentException> { uniqueBookTextRange("same\nsame\n", target) }
        }
    }

    @Test
    fun `Run needs bounded stdout rendered text and at least one meaningful callout`() {
        for (bad in listOf(view.copy(stdout = ""), view.copy(stdout = "wrong prompt"), view.copy(consoleText = "wrong prompt"),
                           view.copy(stdout = "Prompt: " + "1".repeat(32768)), view.copy(consoleText = "Prompt: " + "1".repeat(32768)),
                           view.copy(callouts = emptyList()), view.copy(callouts = listOf(BookTextCallout("", "value"))),
                           view.copy(callouts = listOf(BookTextCallout("Value", ""))),
                           view.copy(callouts = listOf(BookTextCallout("Value", "1".repeat(8193)))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }
}
