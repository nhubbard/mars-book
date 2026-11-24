package edu.missouristate.mars.book

import java.awt.Rectangle
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookCapturePlanTest {
  private val figure =
      BookCaptureFigure(
          "video-vector-count",
          "kernel.asm",
          "a".repeat(64),
          "video_group_counted",
          listOf(BookExpectedRow("Changed words", listOf("v1", "0x0000000000000002"))),
      )
  private val plan =
      BookCapturePlan(
          "/tmp/fixture",
          "/tmp/capture",
          listOf(figure),
          "034-lossless-video",
          "0.1.0",
          "b".repeat(40),
          "c".repeat(40),
      )

  @Test
  fun `validates an explicit local fixture and exact-value UI assertion`() {
    plan.validate()
    assertTrue(figure.expectedRows.single().matches("v1 = 0x0000000000000002"))
    assertFalse(figure.expectedRows.single().matches("v1 = 0x0000000000000020"))
    assertFalse(figure.expectedRows.single().matches("v0 = 0x0000000000000002"))
    assertEquals(
        2,
        figure.checkpointLine("# video_group_counted:\n.text\n  video_group_counted: nop\n"),
    )
  }

  @Test
  fun `rejects missing or duplicate labels rather than selecting a plausible stop`() {
    for (text in
        listOf(
            "# video_group_counted:\n",
            "video_group_counted: nop\nvideo_group_counted: nop\n",
        )) {
      assertThrows<IllegalArgumentException> { figure.checkpointLine(text) }
    }
  }

  @Test
  fun `rejects unchecked figures and unsafe fixture paths`() {
    val invalid =
        listOf(
            figure.copy(id = "../figure"),
            figure.copy(source = "../elsewhere.asm"),
            figure.copy(source = "/tmp/elsewhere.asm"),
            figure.copy(source = ""),
            figure.copy(sourceSha256 = "unknown"),
            figure.copy(checkpoint = "label; action"),
            figure.copy(expectedRows = emptyList()),
            figure.copy(width = 799),
            figure.copy(height = 599),
            figure.copy(expectedRows = listOf(BookExpectedRow("", listOf("value")))),
            figure.copy(expectedRows = listOf(BookExpectedRow("value", emptyList()))),
            figure.copy(expectedRows = listOf(BookExpectedRow("value", listOf("")))),
        )
    invalid.forEach {
      assertThrows<IllegalArgumentException> { it.validate(Path.of(plan.projectRoot)) }
    }
    for (invalidPlan in
        listOf(
            plan.copy(projectRoot = "relative"),
            plan.copy(outputDirectory = "relative"),
            plan.copy(packageId = ""),
            plan.copy(packageVersion = "latest"),
            plan.copy(publicCommit = "main"),
            plan.copy(upstreamCommit = "HEAD"),
            plan.copy(upstreamDirtyFiles = mapOf("source.kt" to "unknown")),
            plan.copy(figures = emptyList()),
            plan.copy(figures = listOf(figure, figure)),
        )) {
      assertThrows<IllegalArgumentException> { invalidPlan.validate() }
    }
  }

  @Test
  fun `translates real screen rectangles without accepting clipped or empty targets`() {
    val viewport = Rectangle(100, 200, 1280, 800)
    assertEquals(
        mapOf("x" to 15, "y" to 20, "width" to 160, "height" to 24),
        captureBox(Rectangle(115, 220, 160, 24), viewport),
    )
    for (invalid in
        listOf(
            Rectangle(99, 220, 160, 24),
            Rectangle(115, 199, 160, 24),
            Rectangle(1300, 220, 160, 24),
            Rectangle(115, 990, 160, 24),
            Rectangle(115, 220, 0, 24),
            Rectangle(115, 220, 160, 0),
        )) {
      assertThrows<IllegalArgumentException> { captureBox(invalid, viewport) }
    }
    assertEquals(
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        captureSha256(byteArrayOf()),
    )
  }
}
