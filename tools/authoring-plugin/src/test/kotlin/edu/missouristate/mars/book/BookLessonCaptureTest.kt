package edu.missouristate.mars.book

import java.nio.file.Path
import java.util.Properties
import java.util.jar.JarInputStream
import java.util.zip.ZipFile
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextPane
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookLessonCaptureTest {
    private val valid = BookLessonCapturePlan("/tmp/lesson-project", "/tmp/lesson-evidence", "a".repeat(64))

    @Test
    fun `capture requires explicit roots and exact source provenance`() {
        valid.validate()
        assertFalse(valid.captureRun)
        assertFalse(valid.captureDebug)
        assertFalse(valid.captureOutput)
        assertFalse(valid.captureDiagnosis)
        assertFalse(valid.captureRepair)
        assertFalse(valid.captureIndependent)
        assertFalse(valid.captureHints)
        assertThrows<IllegalArgumentException> { valid.copy(captureHints = true).validate() }
        assertThrows<IllegalArgumentException> { valid.copy(captureIndependent = true).validate() }
        valid.copy(captureIndependent = true, independentSolutionPath = "/tmp/private-solution.asm", independentSolutionSha256 = "b".repeat(64)).validate()
        assertThrows<IllegalArgumentException> { valid.copy(captureIndependent = true, independentSolutionPath = "/tmp/private-solution.asm", independentSolutionSha256 = "bad").validate() }
        valid.copy(captureDiagnosis = true, captureRepair = true).validate()
        valid.copy(captureOutput = true).validate()
        valid.copy(captureRun = true).validate()
        assertThrows<IllegalArgumentException> { valid.copy(projectRoot = "relative").validate() }
        assertThrows<IllegalArgumentException> { valid.copy(outputDirectory = "relative").validate() }
        assertThrows<IllegalArgumentException> { valid.copy(outputDirectory = "/tmp/a/../lesson-project").validate() }
        assertThrows<IllegalArgumentException> { valid.copy(sourceSha256 = "unknown").validate() }
    }

    @Test
    fun `resume captures require existing source provenance and cannot replay the initial sequence`() {
        assertFalse(valid.resumeIndependent)
        assertFalse(valid.expectIndependentRecheck)
        assertFalse(valid.closeProjectAfterCapture)
        assertThrows<IllegalArgumentException> { valid.copy(expectIndependentRecheck = true).validate() }
        assertThrows<IllegalArgumentException> { valid.copy(resumeIndependent = true).validate() }
        val resume = valid.copy(resumeIndependent = true, captureIndependent = true, independentSourceSha256 = "b".repeat(64))
        resume.validate() // No instructor solution path is needed or read when resuming.
        resume.copy(expectIndependentRecheck = true, captureHints = true, closeProjectAfterCapture = true).validate()
        assertThrows<IllegalArgumentException> { resume.copy(independentSourceSha256 = "bad").validate() }
        assertThrows<IllegalArgumentException> { resume.copy(captureRun = true).validate() }
        assertThrows<IllegalArgumentException> { resume.copy(captureDebug = true).validate() }
        assertThrows<IllegalArgumentException> { resume.copy(captureOutput = true).validate() }
        assertThrows<IllegalArgumentException> { resume.copy(captureDiagnosis = true).validate() }
        assertThrows<IllegalArgumentException> { resume.copy(captureRepair = true).validate() }
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
    @Test
    fun `register observations use rendered values and reject ambiguous rows`() {
        assertEquals(20L, lessonRegisterValue(listOf("\$v0 = 20"), "\$v0"))
        assertEquals(40L, lessonRegisterValue(listOf("\$t0 (written) = 0x0000000000000028"), "\$t0"))
        assertEquals(-1L, lessonRegisterValue(listOf("\$t0 = 0xffffffffffffffff"), "\$t0"))
        assertNull(lessonRegisterValue(emptyList(), "\$v0"))
        assertNull(lessonRegisterValue(listOf("\$v0 = loading"), "\$v0"))
        assertNull(lessonRegisterValue(listOf("\$v0 = 0xloading"), "\$v0"))
        assertThrows<IllegalStateException> { lessonRegisterValue(listOf("\$v0 = 20", "\$v0 = 21"), "\$v0") }
    }

    @Test
    fun `register view caption matches the actual packaged MARS debugger bundle`() {
        val archive = Path.of(checkNotNull(System.getProperty("marsPluginArchive")))
        val labels = Properties()
        ZipFile(archive.toFile()).use { zip ->
            JarInputStream(zip.getInputStream(checkNotNull(zip.getEntry("plugin/lib/plugin-1.0-SNAPSHOT.jar")))).use { jar ->
                checkNotNull(generateSequence { jar.nextJarEntry }.firstOrNull { it.name == "messages/MarsBundle.properties" })
                labels.load(jar)
            }
        }
        assertEquals(listOf(labels.getProperty("debug.registers.cpu")), lessonDebugRegisterView.expand.single())
        assertEquals(lessonDebugRegisterView.expand.single() + "\$t0", lessonDebugRegisterView.reveal)
    }

}
