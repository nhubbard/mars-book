package edu.missouristate.mars.book

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookLessonPhotographTest {
  private fun image(background: Color) =
      BufferedImage(1280, 900, BufferedImage.TYPE_INT_RGB).apply {
        createGraphics().useGraphics {
          color = background
          fillRect(0, 0, width, height)
        }
      }

  @Test
  fun `screen recording denial and empty screens cannot attest a native lesson photograph`() {
    for (background in listOf(Color(44, 44, 46), Color.WHITE, Color.BLACK)) {
      assertThrows<IllegalStateException> { requireBookLightPhotograph(image(background)) }
    }
  }

  @Test
  fun `light window with actual foreground contrast passes the photograph guard`() {
    val photograph =
        image(Color.WHITE).apply {
          createGraphics().useGraphics {
            color = Color.BLACK
            for (row in 0..30) fillRect(50, 40 + row * 20, 400, 5)
          }
        }
    val result = requireBookLightPhotograph(photograph)
    assertTrue(result.getValue("lightSamples") > result.getValue("darkSamples"))
    assertTrue(result.getValue("darkSamples") > 0)
  }
}

private inline fun <T> Graphics2D.useGraphics(block: Graphics2D.() -> T): T =
    try {
      block()
    } finally {
      dispose()
    }
