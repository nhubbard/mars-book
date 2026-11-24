package edu.missouristate.mars.book

import java.awt.Dimension
import java.awt.geom.Point2D
import java.nio.file.Path
import java.util.Properties
import java.util.jar.JarInputStream
import java.util.zip.ZipFile
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.JViewport
import javax.swing.SwingUtilities
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookLessonCaptureTest {
  @Test
  fun `inline answer offsets select native choice paragraphs rather than surrounding prose`() {
    val text = "A question mentions -5\nA. 7\nB. -7\nC. -5\n"
    val offset = bookInlineChoiceOffset(text, "-5")!!
    assertEquals('5', text[offset])
    assertNull(bookInlineChoiceOffset(text, "5"))
    assertNull(bookInlineChoiceOffset(text, "missing"))
    val repeated = text + "Next question\nC. -5\r\n"
    assertTrue(bookInlineChoiceOffset(repeated, "-5")!! > text.length)
    assertTrue(bookInlineChoiceOffset("A. \$x", "\$x")!! >= 3)
  }

  @Test
  fun `native lesson action links have a stable click point in their visible caption`() {
    assertNull(bookLessonActionOffset("No controls", "Open a hint"))
    val text = "Use Check my program and then Open a hint."
    assertEquals(
        text.indexOf("Check my program") + 4,
        bookLessonActionOffset(text, "Check my program"),
    )
    assertEquals(0, bookLessonActionOffset("X", "X"))
    assertThrows<IllegalArgumentException> { bookLessonActionOffset(text, "") }
  }

  @Test
  fun `native lesson click uses a visible caret box and maps back to its answer`() {
    SwingUtilities.invokeAndWait {
      val pane =
          JTextPane().apply {
            text = "Question\nA. first\nB. second\nC. third"
            setSize(400, 200)
            doLayout()
          }
      val offset = bookInlineChoiceOffset(pane.text, "second")!!
      val raw = pane.modelToView2D(offset).bounds
      val bounds = bookLessonLinkBounds(pane, offset)!!
      assertEquals(raw.x, bounds.x)
      assertEquals(raw.y, bounds.y)
      assertTrue(bounds.width > 0)
      assertTrue(pane.visibleRect.intersects(bounds))
      val point = Point2D.Double(bounds.centerX, bounds.centerY)
      val actualOffset = pane.viewToModel2D(point)
      assertTrue(actualOffset in pane.text.indexOf("B. second") until pane.text.indexOf("\nC."))
    }
  }

  private val valid =
      BookLessonCapturePlan("/tmp/lesson-project", "/tmp/lesson-evidence", "a".repeat(64))

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
    valid
        .copy(
            captureIndependent = true,
            independentSolutionPath = "/tmp/private-solution.asm",
            independentSolutionSha256 = "b".repeat(64),
        )
        .validate()
    assertThrows<IllegalArgumentException> {
      valid
          .copy(
              captureIndependent = true,
              independentSolutionPath = "/tmp/private-solution.asm",
              independentSolutionSha256 = "bad",
          )
          .validate()
    }
    valid.copy(captureDiagnosis = true, captureRepair = true).validate()
    valid.copy(captureOutput = true).validate()
    valid.copy(captureRun = true).validate()
    assertThrows<IllegalArgumentException> { valid.copy(projectRoot = "relative").validate() }
    assertThrows<IllegalArgumentException> { valid.copy(outputDirectory = "relative").validate() }
    assertThrows<IllegalArgumentException> {
      valid.copy(outputDirectory = "/tmp/a/../lesson-project").validate()
    }
    assertThrows<IllegalArgumentException> { valid.copy(sourceSha256 = "unknown").validate() }
  }

  @Test
  fun `resume captures require existing source provenance and cannot replay the initial sequence`() {
    assertFalse(valid.resumeIndependent)
    assertFalse(valid.expectIndependentRecheck)
    assertFalse(valid.closeProjectAfterCapture)
    assertThrows<IllegalArgumentException> {
      valid.copy(expectIndependentRecheck = true).validate()
    }
    assertThrows<IllegalArgumentException> { valid.copy(resumeIndependent = true).validate() }
    val resume =
        valid.copy(
            resumeIndependent = true,
            captureIndependent = true,
            independentSourceSha256 = "b".repeat(64),
        )
    resume.validate() // No instructor solution path is needed or read when resuming.
    resume
        .copy(expectIndependentRecheck = true, captureHints = true, closeProjectAfterCapture = true)
        .validate()
    assertThrows<IllegalArgumentException> {
      resume.copy(independentSourceSha256 = "bad").validate()
    }
    assertThrows<IllegalArgumentException> { resume.copy(captureRun = true).validate() }
    assertThrows<IllegalArgumentException> { resume.copy(captureDebug = true).validate() }
    assertThrows<IllegalArgumentException> { resume.copy(captureOutput = true).validate() }
    assertThrows<IllegalArgumentException> { resume.copy(captureDiagnosis = true).validate() }
    assertThrows<IllegalArgumentException> { resume.copy(captureRepair = true).validate() }
  }

  @Test
  fun `policy captures use native settings without a solution or resume injection`() {
    assertFalse(valid.capturePolicy)
    assertThrows<IllegalArgumentException> { valid.copy(capturePolicy = true).validate() }
    val policy = valid.copy(capturePolicy = true, captureIndependent = true, captureHints = true)
    policy.validate()
    for (invalid in
        listOf(
            policy.copy(captureHints = false),
            policy.copy(resumeIndependent = true),
            policy.copy(captureRun = true),
            policy.copy(captureDebug = true),
            policy.copy(captureOutput = true),
            policy.copy(captureDiagnosis = true),
            policy.copy(captureRepair = true),
        )) {
      assertThrows<IllegalArgumentException> { invalid.validate() }
    }
  }

  @Test
  fun `mode captures route through production UI without private solutions or mixed capture plans`() {
    assertFalse(valid.captureModes)
    assertThrows<IllegalArgumentException> { valid.copy(captureModes = true).validate() }
    val modes = valid.copy(captureModes = true, captureIndependent = true, captureHints = true)
    modes.validate()
    for (invalid in
        listOf(
            modes.copy(captureHints = false),
            modes.copy(capturePolicy = true),
            modes.copy(resumeIndependent = true),
            modes.copy(captureRun = true),
            modes.copy(captureDebug = true),
            modes.copy(captureOutput = true),
            modes.copy(captureDiagnosis = true),
            modes.copy(captureRepair = true),
        )) {
      assertThrows<IllegalArgumentException> { invalid.validate() }
    }
  }

  @Test
  fun `fresh-process mode restoration requires existing source provenance`() {
    assertFalse(valid.resumeModes)
    assertThrows<IllegalArgumentException> { valid.copy(resumeModes = true).validate() }
    val resume =
        valid.copy(
            captureModes = true,
            captureIndependent = true,
            captureHints = true,
            resumeModes = true,
        )
    assertThrows<IllegalArgumentException> { resume.validate() }
    resume.copy(independentSourceSha256 = "b".repeat(64)).validate()
    assertThrows<IllegalArgumentException> {
      resume.copy(independentSourceSha256 = "bad").validate()
    }
  }

  @Test
  fun `guided restoration binds every existing source and forbids execution or answer injection`() {
    assertFalse(valid.resumeGuided)
    assertThrows<IllegalArgumentException> { valid.copy(expectGuidedRecheck = true).validate() }
    assertThrows<IllegalArgumentException> { valid.copy(resumeGuided = true).validate() }
    val hashes =
        mapOf(
            "first-program.asm" to "a".repeat(64),
            "wrong-output.asm" to "b".repeat(64),
            "repair-output.asm" to "c".repeat(64),
        )
    val resume = valid.copy(resumeGuided = true, guidedSourceSha256 = hashes)
    resume.validate()
    resume.copy(expectGuidedRecheck = true).validate()
    for (invalid in
        listOf(
            resume.copy(captureRun = true),
            resume.copy(captureDebug = true),
            resume.copy(captureOutput = true),
            resume.copy(captureDiagnosis = true),
            resume.copy(captureRepair = true),
            resume.copy(captureIndependent = true),
            resume.copy(captureHints = true),
            resume.copy(capturePolicy = true),
            resume.copy(captureModes = true),
            resume.copy(resumeModes = true),
            resume.copy(resumeIndependent = true),
            resume.copy(guidedSourceSha256 = hashes - "repair-output.asm"),
            resume.copy(guidedSourceSha256 = hashes + ("../source.asm" to "a".repeat(64))),
            resume.copy(guidedSourceSha256 = hashes + ("wrong-output.asm" to "bad")),
            resume.copy(sourceSha256 = "d".repeat(64)),
        )) {
      assertThrows<IllegalArgumentException> { invalid.validate() }
    }
  }

  @Test
  fun `reset capture requires completed guided observations and cold recovery requires all existing sources`() {
    assertFalse(valid.captureReset)
    assertFalse(valid.resumeReset)
    assertThrows<IllegalArgumentException> { valid.copy(captureReset = true).validate() }
    val initial =
        valid.copy(
            captureReset = true,
            captureRun = true,
            captureDebug = true,
            captureOutput = true,
            captureDiagnosis = true,
            captureRepair = true,
        )
    initial.validate()
    for (bad in
        listOf(
            initial.copy(captureRun = false),
            initial.copy(captureDebug = false),
            initial.copy(captureOutput = false),
            initial.copy(captureDiagnosis = false),
            initial.copy(captureRepair = false),
            initial.copy(captureIndependent = true),
            initial.copy(captureHints = true),
            initial.copy(captureModes = true),
            initial.copy(capturePolicy = true),
            initial.copy(resumeModes = true),
            initial.copy(resumeGuided = true),
            initial.copy(resumeIndependent = true),
            initial.copy(resumeReset = true),
        )) {
      assertThrows<IllegalArgumentException> { bad.validate() }
    }
    assertThrows<IllegalArgumentException> { valid.copy(resumeReset = true).validate() }
    val hashes =
        listOf("first-program.asm", "wrong-output.asm", "repair-output.asm", "independent.asm")
            .associateWith { "a".repeat(64) }
    val resume = valid.copy(resumeReset = true, guidedSourceSha256 = hashes)
    resume.validate()
    assertThrows<IllegalArgumentException> {
      resume.copy(guidedSourceSha256 = hashes - "independent.asm").validate()
    }
    assertThrows<IllegalArgumentException> {
      resume.copy(guidedSourceSha256 = hashes + ("independent.asm" to "bad")).validate()
    }
    assertThrows<IllegalArgumentException> { resume.copy(sourceSha256 = "b".repeat(64)).validate() }
    assertThrows<IllegalArgumentException> { resume.copy(captureRun = true).validate() }
  }

  @Test
  fun `guided requirement capture needs real observations and cold validation binds all existing sources`() {
    assertThrows<IllegalArgumentException> { valid.copy(captureRequirements = true).validate() }
    val initial =
        valid.copy(
            captureRequirements = true,
            captureRun = true,
            captureDebug = true,
            captureOutput = true,
            captureDiagnosis = true,
            captureRepair = true,
        )
    initial.validate()
    for (bad in
        listOf(
            initial.copy(captureRun = false),
            initial.copy(captureDebug = false),
            initial.copy(captureOutput = false),
            initial.copy(captureDiagnosis = false),
            initial.copy(captureRepair = false),
            initial.copy(captureIndependent = true),
            initial.copy(captureHints = true),
            initial.copy(captureModes = true),
            initial.copy(capturePolicy = true),
            initial.copy(resumeModes = true),
            initial.copy(resumeGuided = true),
            initial.copy(resumeIndependent = true),
            initial.copy(captureReset = true),
            initial.copy(resumeReset = true),
            initial.copy(resumeRequirements = true),
        )) {
      assertThrows<IllegalArgumentException> { bad.validate() }
    }
    assertThrows<IllegalArgumentException> { valid.copy(resumeRequirements = true).validate() }
    val hashes =
        listOf("first-program.asm", "wrong-output.asm", "repair-output.asm", "independent.asm")
            .associateWith { "a".repeat(64) }
    val resumed = valid.copy(resumeRequirements = true, guidedSourceSha256 = hashes)
    resumed.validate()
    assertThrows<IllegalArgumentException> {
      resumed.copy(guidedSourceSha256 = hashes - "independent.asm").validate()
    }
    assertThrows<IllegalArgumentException> {
      resumed.copy(guidedSourceSha256 = hashes + ("repair-output.asm" to "bad")).validate()
    }
    assertThrows<IllegalArgumentException> {
      resumed.copy(sourceSha256 = "b".repeat(64)).validate()
    }
    assertThrows<IllegalArgumentException> { resumed.copy(captureRun = true).validate() }
  }

  @Test
  fun `layout measurement detects clipped text and buttons rather than matching future text`() {
    val root = JPanel(null)
    assertNull(lessonHorizontalLayout(root))
    val panel =
        object : JPanel(null) {
              override fun isShowing() = true
            }
            .apply { name = "lessonPanel" }
    root.add(panel)
    assertNull(lessonHorizontalLayout(root))
    val viewport =
        JViewport().apply {
          extentSize = Dimension(500, 700)
          view = panel
        }
    root.add(viewport)
    panel.setSize(500, 1000)
    assertEquals(BookLessonHorizontalLayout(500, 500, 0), lessonHorizontalLayout(root))
    val text =
        JTextPane().apply {
          name = "lessonMessagePane"
          setBounds(18, 10, 464, 600)
        }
    panel.add(text)
    assertEquals(0, lessonHorizontalLayout(root)!!.overflowPixels)
    panel.setSize(560, 1000)
    assertEquals(60, lessonHorizontalLayout(root)!!.overflowPixels)
    panel.setSize(500, 1000)
    text.setBounds(18, 10, 510, 600)
    assertEquals(28, lessonHorizontalLayout(root)!!.overflowPixels)
    text.setBounds(-5, 10, 464, 600)
    assertEquals(5, lessonHorizontalLayout(root)!!.overflowPixels)
    text.setBounds(18, 10, 464, 600)
    panel.add(JButton("Next").apply { setBounds(100, 700, 420, 30) })
    assertEquals(20, lessonHorizontalLayout(root)!!.overflowPixels)
  }

  @Test
  fun `selectors reject hidden and ambiguous UI controls`() {
    val root = JPanel()
    root.add(JButton("Check prediction"))
    assertThrows<NoSuchElementException> { visibleLessonButton(root, "Check prediction") }
    val active =
        object : JButton("Check prediction") {
          override fun isShowing() = true
        }
    root.add(active)
    assertEquals(active, visibleLessonButton(root, "Check prediction"))
    root.add(
        object : JButton("Check prediction") {
          override fun isShowing() = true
        },
    )
    assertThrows<IllegalArgumentException> { visibleLessonButton(root, "Check prediction") }
  }

  @Test
  fun `observations read actual Swing text and exclude hidden messages`() {
    val root = JPanel()
    root.add(JLabel("hidden text"))
    root.add(
        object : JLabel("visible question") {
          override fun isShowing() = true
        },
    )
    root.add(
        object : JButton("Check prediction") {
          override fun isShowing() = true
        },
    )
    root.add(
        object : JTextPane() {
              override fun isShowing() = true
            }
            .apply { text = "Wrong sources do not consume values" },
    )
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
    assertThrows<IllegalStateException> {
      lessonRegisterValue(listOf("\$v0 = 20", "\$v0 = 21"), "\$v0")
    }
  }

  @Test
  fun `register view caption matches the actual packaged MARS debugger bundle`() {
    val archive = Path.of(checkNotNull(System.getProperty("marsPluginArchive")))
    val labels = Properties()
    ZipFile(archive.toFile()).use { zip ->
      JarInputStream(
              zip.getInputStream(checkNotNull(zip.getEntry("plugin/lib/plugin-1.0-SNAPSHOT.jar"))),
          )
          .use { jar ->
            checkNotNull(
                generateSequence { jar.nextJarEntry }
                    .firstOrNull { it.name == "messages/MarsBundle.properties" },
            )
            labels.load(jar)
          }
    }
    assertEquals(
        listOf(labels.getProperty("debug.registers.cpu")),
        lessonDebugRegisterView.expand.single(),
    )
    assertEquals(lessonDebugRegisterView.expand.single() + "\$t0", lessonDebugRegisterView.reveal)
  }

  @Test
  fun `assessment attempt captures cannot mix modes and resume requires source provenance`() {
    valid.copy(captureAttempts = true).validate()
    valid.copy(resumeAttempts = true, independentSourceSha256 = "b".repeat(64)).validate()
    assertThrows<IllegalArgumentException> { valid.copy(resumeAttempts = true).validate() }
    assertThrows<IllegalArgumentException> {
      valid.copy(captureAttempts = true, resumeAttempts = true).validate()
    }
    for (mixed in
        listOf(
            valid.copy(captureRequirements = true),
            valid.copy(captureReset = true),
            valid.copy(captureIndependent = true),
            valid.copy(captureRun = true),
            valid.copy(capturePolicy = true),
        )) {
      assertThrows<IllegalArgumentException> { mixed.copy(captureAttempts = true).validate() }
    }
  }
}
