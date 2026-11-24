package edu.missouristate.mars.book

import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import javax.swing.JLabel
import javax.swing.JPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class BookCaptureUiTest {
  private fun label(text: String, showing: Boolean = true) =
      object : JLabel(text) {
        override fun isShowing() = showing
      }

  @Test
  fun `native visible analysis labels prevent capture after a completed daemon event`() =
      timeoutRunBlocking {
        withContext(Dispatchers.EDT) {
          val root =
              JPanel().apply {
                add(label("No errors"))
                add(
                    JPanel().apply {
                      add(label("Analyzing…"))
                      add(label("Indexing..."))
                    },
                )
                add(label("Analyzing...", showing = false))
              }
          assertEquals(listOf("Analyzing…", "Indexing..."), visibleAnalysisIndicators(root))
        }
      }

  @Test
  fun `a hidden progress label does not block a settled editor`() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      val root =
          JPanel().apply {
            add(label("Analysis complete"))
            add(label("Analyzing...", showing = false))
            add(label("2 warnings"))
          }
      assertTrue(visibleAnalysisIndicators(root).isEmpty())
    }
  }
}
