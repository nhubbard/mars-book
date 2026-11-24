package edu.missouristate.mars.book

import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JComponent
import kotlin.math.abs

/** Expected RGB888 pixels come from an independently checked fixture, never from the screen. */
data class BookBitmapView(
    val columns: Int = 0,
    val rows: Int = 0,
    val address: String = "",
    val expectedRgb: String = "",
    val rgbSha256: String = "",
    val label: String = "Decoded frame",
    // Exact native rendering is always required. This bound applies only to the screen photograph.
    val screenChannelTolerance: Int = 0,
) {
  fun validate(projectRoot: Path) {
    require(screenChannelTolerance in 0..8) { "Unsupported screen-color tolerance" }
    require(columns in 1..1920 && rows in 1..1080) { "Unsupported bitmap dimensions" }
    require(address.matches(Regex("0x[0-9a-fA-F]{1,16}"))) { "Use a hexadecimal bitmap address" }
    val start = address.substring(2).toLongOrNull(16)
    require(
        start != null &&
            start >= 0 &&
            start and 3L == 0L &&
            start <= Long.MAX_VALUE - columns.toLong() * rows * 4,
    ) {
      "Invalid bitmap address range"
    }
    require(
        expectedRgb.isNotBlank() &&
            !Path.of(expectedRgb).isAbsolute &&
            projectRoot.resolve(expectedRgb).normalize().startsWith(projectRoot.normalize()),
    ) {
      "RGB fixture escapes the project"
    }
    require(rgbSha256.matches(Regex("[a-f0-9]{64}")) && label.isNotBlank()) {
      "Missing bitmap assertion"
    }
  }

  internal fun load(projectRoot: Path): BookExpectedBitmap {
    validate(projectRoot)
    val path = projectRoot.resolve(expectedRgb).toRealPath()
    require(path.startsWith(projectRoot.toRealPath())) {
      "RGB fixture escapes the project through a link"
    }
    val bytes = Files.readAllBytes(path)
    require(bytes.size == columns * rows * 3 && captureSha256(bytes) == rgbSha256) {
      "RGB fixture changed"
    }
    val colors =
        IntArray(columns * rows) { index ->
          val offset = index * 3
          ((bytes[offset].toInt() and 255) shl 16) or
              ((bytes[offset + 1].toInt() and 255) shl 8) or
              (bytes[offset + 2].toInt() and 255)
        }
    return BookExpectedBitmap(this, colors)
  }
}

internal data class BookExpectedBitmap(val view: BookBitmapView, val colors: IntArray) {
  val address = view.address.substring(2).toLong(16)
}

/** Native canvas coordinates. Bounds are discovered through its public tooltip hit testing. */
internal data class BookObservedBitmap(val bounds: Rectangle, val columns: Int, val rows: Int) {
  fun center(index: Int): Point =
      Point(
          bounds.x + ((index % columns + 0.5) * bounds.width / columns).toInt(),
          bounds.y + ((index / columns + 0.5) * bounds.height / rows).toInt(),
      )

  fun screenEvidence(image: BufferedImage, expected: BookExpectedBitmap): BookBitmapScreenEvidence {
    require(Rectangle(0, 0, image.width, image.height).contains(bounds)) {
      "Bitmap is outside the screen image"
    }
    var exact = 0
    var maximum = 0
    val histogram = sortedMapOf<Int, Int>()
    val rgb = ByteArray(expected.colors.size * 3)
    for (index in expected.colors.indices) {
      val point = center(index)
      val actual = image.getRGB(point.x, point.y)
      var error = 0
      for (channel in 0..2) {
        val shift = (2 - channel) * 8
        val value = (actual ushr shift) and 255
        rgb[index * 3 + channel] = value.toByte()
        error = maxOf(error, abs(value - ((expected.colors[index] ushr shift) and 255)))
      }
      if (error == 0) exact++
      maximum = maxOf(maximum, error)
      histogram[error] = (histogram[error] ?: 0) + 1
    }
    return BookBitmapScreenEvidence(exact, maximum, histogram, captureSha256(rgb))
  }

  fun matches(image: BufferedImage, expected: BookExpectedBitmap): Boolean =
      Rectangle(0, 0, image.width, image.height).contains(bounds) &&
          expected.colors.indices.all { index ->
            val point = center(index)
            (image.getRGB(point.x, point.y) and 0xffffff) == expected.colors[index]
          }
}

internal data class BookBitmapScreenEvidence(
    val exactPixels: Int,
    val maximumChannelError: Int,
    val pixelsByMaximumChannelError: Map<Int, Int>,
    val sampledRgbSha256: String,
)

/**
 * Read only public Swing rendering/tooltip contracts; no MARS implementation classes or memory
 * reads.
 */
internal fun observeBookBitmap(
    canvas: JComponent,
    expected: BookExpectedBitmap,
    diagnostics: MutableList<String>? = null,
): BookObservedBitmap? {
  diagnostics?.clear()
  fun reject(reason: String): BookObservedBitmap? {
    diagnostics?.add(reason)
    return null
  }
  if (canvas.width <= 0 || canvas.height <= 0)
      return reject("Bitmap canvas has no size: ${canvas.width} by ${canvas.height}")
  fun tooltip(x: Int, y: Int) =
      canvas.getToolTipText(MouseEvent(canvas, MouseEvent.MOUSE_MOVED, 0, 0, x, y, 0, false))
  val horizontal = (0 until canvas.width).filter { tooltip(it, canvas.height / 2) != null }
  val vertical = (0 until canvas.height).filter { tooltip(canvas.width / 2, it) != null }
  if (horizontal.isEmpty() || vertical.isEmpty())
      return reject("No native bitmap tooltips in canvas ${canvas.width} by ${canvas.height}")
  val bounds =
      Rectangle(
          horizontal.first(),
          vertical.first(),
          horizontal.last() - horizontal.first() + 1,
          vertical.last() - vertical.first() + 1,
      )
  if (bounds.width < expected.view.columns * 4 || bounds.height < expected.view.rows * 4)
      return reject(
          "Native bitmap bounds $bounds are too small for ${expected.view.columns} by ${expected.view.rows} cells",
      )
  val observed = BookObservedBitmap(bounds, expected.view.columns, expected.view.rows)
  val pattern = Regex("""\((\d+), (\d+)\) · 0x([0-9a-fA-F]+) · #([0-9a-fA-F]{6})""")
  for (index in expected.colors.indices) {
    val point = observed.center(index)
    val actual = tooltip(point.x, point.y)
    val match =
        actual?.let(pattern::matchEntire)
            ?: return reject("Unrecognized native tooltip at expected cell $index: $actual")
    val (column, row, address, rgb) = match.destructured
    if (
        column.toInt() != index % expected.view.columns ||
            row.toInt() != index / expected.view.columns ||
            address.toLongOrNull(16) != expected.address + index * 4L ||
            rgb.toInt(16) != expected.colors[index]
    ) {
      return reject(
          "Native cell $index mismatch: $actual; expected (${index % expected.view.columns}, ${index / expected.view.columns}) at 0x${(expected.address + index * 4L).toString(16)} RGB #%06x"
              .format(expected.colors[index]),
      )
    }
  }
  val painted = BufferedImage(canvas.width, canvas.height, BufferedImage.TYPE_INT_RGB)
  val graphics = painted.createGraphics()
  try {
    canvas.paint(graphics)
  } finally {
    graphics.dispose()
  }
  if (!observed.matches(painted, expected))
      return reject("Native painted RGB differs from verified tooltip colors")
  diagnostics?.add("Verified ${expected.colors.size} native cells in $bounds")
  return observed
}
