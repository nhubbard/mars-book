package edu.missouristate.mars.book

import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextPane
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookLessonCaptureTest {
    private val valid = BookLessonCapturePlan("/tmp/lesson-project", "/tmp/lesson-evidence", "a".repeat(64))

    @Test
    fun `capture requires explicit roots and exact source provenance`() {
        valid.validate()
        assertFalse(valid.captureRun)
        valid.copy(captureRun = true).validate()
        assertThrows<IllegalArgumentException> { valid.copy(projectRoot = "relative").validate() }
        assertThrows<IllegalArgumentException> { valid.copy(outputDirectory = "relative").validate() }
        assertThrows<IllegalArgumentException> { valid.copy(outputDirectory = "/tmp/a/../lesson-project").validate() }
        assertThrows<IllegalArgumentException> { valid.copy(sourceSha256 = "unknown").validate() }
    }

    @Test
    fun `selectors reject hidden and ambiguous UI controls`() {
        val root = JPanel()
        root.add(JButton("Check prediction"))
        assertThrows<NoSuchElementException> { visibleLessonButton(root, "Check prediction") }
        val active = object : JButton("Check prediction") { override fun isShowing() = true }
        root.add(active)
        assertEquals(active, visibleLessonButton(root, "Check prediction"))
        root.add(object : JButton("Check prediction") { override fun isShowing() = true })
        assertThrows<IllegalArgumentException> { visibleLessonButton(root, "Check prediction") }
    }

    @Test
    fun `observations read actual Swing text and exclude hidden messages`() {
        val root = JPanel()
        root.add(JLabel("hidden text"))
        root.add(object : JLabel("visible question") { override fun isShowing() = true })
        root.add(object : JButton("Check prediction") { override fun isShowing() = true })
        root.add(object : JTextPane() { override fun isShowing() = true }.apply { text = "Wrong sources do not consume values" })
        val text = lessonVisibleText(root)
        assertTrue(text.contains("visible question"))
        assertTrue(text.contains("Check prediction"))
        assertTrue(text.contains("Wrong sources do not consume values"))
        assertFalse(text.contains("hidden text"))
    }
}
