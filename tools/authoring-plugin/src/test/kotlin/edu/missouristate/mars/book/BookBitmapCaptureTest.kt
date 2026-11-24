package edu.missouristate.mars.book

import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JComponent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

class BookBitmapCaptureTest {
  @TempDir lateinit var project: Path
  private val bytes =
      byteArrayOf(0x12, 0x34, 0x56, 0x65, 0x43, 0x21, 0x11, 0x22, 0x33, 0x33, 0x22, 0x11)
  private val view
    get() = BookBitmapView(2, 2, "0x10010004", "frame.rgb", captureSha256(bytes))

  private fun expected(): BookExpectedBitmap {
    Files.write(project.resolve("frame.rgb"), bytes)
    return view.load(project)
  }

  private class Canvas(val expected: BookExpectedBitmap) : JComponent() {
    var wrongAddress = false
    var wrongTooltipColor = false
    var wrongPaintColor = false
    var noTooltip = false
    val viewport = Rectangle(10, 8, 20, 20)

    init {
      setSize(40, 36)
    }

    override fun getToolTipText(event: MouseEvent): String? {
      if (noTooltip || !viewport.contains(event.point)) return null
      val column = (event.x - viewport.x) / 10
      val row = (event.y - viewport.y) / 10
      val index = row * 2 + column
      return "($column, $row) · 0x${(expected.address + index * 4L + if (wrongAddress) 4 else 0).toString(16)} · #" +
          "%06x".format(expected.colors[index] xor if (wrongTooltipColor) 1 else 0)
    }

    override fun paintComponent(g: Graphics) {
      g.color = Color.BLACK
      g.fillRect(0, 0, width, height)
      for (index in expected.colors.indices) {
        g.color = Color(expected.colors[index] xor if (wrongPaintColor) 1 else 0)
        g.fillRect(viewport.x + index % 2 * 10, viewport.y + index / 2 * 10, 10, 10)
      }
    }
  }

  @Test
  fun `loads only the pinned independent RGB fixture`() {
    val expected = expected()
    assertEquals(listOf(0x123456, 0x654321, 0x112233, 0x332211), expected.colors.toList())
    assertEquals(0x10010004L, expected.address)
    Files.write(project.resolve("frame.rgb"), bytes.copyOf(3))
    assertThrows<IllegalArgumentException> { view.load(project) }
    Files.write(project.resolve("frame.rgb"), bytes.reversedArray())
    assertThrows<IllegalArgumentException> { view.load(project) }
  }

  @Test
  fun `discovers the real letterbox and checks every tooltip and painted cell`() {
    val expected = expected()
    val canvas = Canvas(expected)
    val observed = checkNotNull(observeBookBitmap(canvas, expected))
    assertEquals(canvas.viewport, observed.bounds)
    assertEquals(15, observed.center(0).x)
    assertEquals(23, observed.center(2).y)
    canvas.wrongAddress = true
    assertNull(observeBookBitmap(canvas, expected))
    canvas.wrongAddress = false
    canvas.wrongTooltipColor = true
    assertNull(observeBookBitmap(canvas, expected))
    canvas.wrongTooltipColor = false
    canvas.wrongPaintColor = true
    assertNull(observeBookBitmap(canvas, expected))
    canvas.wrongPaintColor = false
    canvas.noTooltip = true
    assertNull(observeBookBitmap(canvas, expected))
    canvas.setSize(0, 0)
    assertNull(observeBookBitmap(canvas, expected))
  }

  @Test
  fun `validates the captured photograph rather than trusting tooltip assertions`() {
    val expected = expected()
    val canvas = Canvas(expected)
    val observed = checkNotNull(observeBookBitmap(canvas, expected))
    val image = BufferedImage(canvas.width, canvas.height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()
    try {
      canvas.paint(graphics)
    } finally {
      graphics.dispose()
    }
    assertTrue(observed.matches(image, expected))
    val exact = observed.screenEvidence(image, expected)
    assertEquals(4, exact.exactPixels)
    assertEquals(0, exact.maximumChannelError)
    assertEquals(captureSha256(bytes), exact.sampledRgbSha256)
    val point = observed.center(3)
    image.setRGB(point.x, point.y, expected.colors[3] xor 1)
    assertFalse(observed.matches(image, expected))
    assertEquals(1, observed.screenEvidence(image, expected).maximumChannelError)
    image.setRGB(point.x, point.y, expected.colors[3] + 3)
    val bounded = observed.screenEvidence(image, expected)
    assertEquals(3, bounded.exactPixels)
    assertEquals(3, bounded.maximumChannelError)
    assertEquals(mapOf(0 to 3, 3 to 1), bounded.pixelsByMaximumChannelError)
    image.setRGB(point.x, point.y, expected.colors[3] + 4)
    assertEquals(4, observed.screenEvidence(image, expected).maximumChannelError)
    assertFalse(observed.copy(bounds = Rectangle(-1, 0, 20, 20)).matches(image, expected))
  }

  @Test
  fun `rejects unsafe or ambiguous bitmap plans`() {
    val figure =
        BookCaptureFigure("bitmap-frame", "main.asm", "a".repeat(64), "ready", bitmapView = view)
    figure.validate(project)
    view.copy(screenChannelTolerance = 3).validate(project)
    view.copy(screenChannelTolerance = 6).validate(project)
    view.copy(screenChannelTolerance = 8).validate(project)
    for (invalid in
        listOf(
            view.copy(screenChannelTolerance = -1),
            view.copy(screenChannelTolerance = 9),
            view.copy(columns = 0),
            view.copy(rows = 1081),
            view.copy(address = "0x10010001"),
            view.copy(address = "0xffffffffffffffff"),
            view.copy(address = "10010004"),
            view.copy(expectedRgb = "../frame.rgb"),
            view.copy(expectedRgb = "/tmp/frame.rgb"),
            view.copy(expectedRgb = ""),
            view.copy(rgbSha256 = ""),
            view.copy(label = ""),
        )) {
      assertThrows<IllegalArgumentException> { invalid.validate(project) }
    }
    assertThrows<IllegalArgumentException> {
      figure.copy(memoryView = BookMemoryView("0x10010004")).validate(project)
    }
    assertThrows<IllegalArgumentException> {
      figure.copy(expectedRows = listOf(BookExpectedRow("value", listOf("v0")))).validate(project)
    }
  }

  @Test
  fun `reports native bitmap mismatches without accepting them`() {
    val expected = expected()
    val canvas = Canvas(expected)
    val diagnostics = mutableListOf<String>()
    assertTrue(observeBookBitmap(canvas, expected, diagnostics) != null)
    assertTrue(diagnostics.single().startsWith("Verified 4 native cells"))
    canvas.wrongTooltipColor = true
    assertNull(observeBookBitmap(canvas, expected, diagnostics))
    assertTrue(diagnostics.single().startsWith("Native cell 0 mismatch:"))
    canvas.wrongTooltipColor = false
    canvas.wrongPaintColor = true
    assertNull(observeBookBitmap(canvas, expected, diagnostics))
    assertEquals("Native painted RGB differs from verified tooltip colors", diagnostics.single())
    canvas.noTooltip = true
    assertNull(observeBookBitmap(canvas, expected, diagnostics))
    assertTrue(diagnostics.single().startsWith("No native bitmap tooltips"))
    canvas.setSize(0, 0)
    assertNull(observeBookBitmap(canvas, expected, diagnostics))
    assertTrue(diagnostics.single().startsWith("Bitmap canvas has no size"))
  }
}
