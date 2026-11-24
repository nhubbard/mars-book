package edu.missouristate.mars.book

import com.intellij.openapi.application.EDT
import com.intellij.openapi.ui.ThreeComponentsSplitter
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import java.awt.Component
import java.awt.Dimension
import java.awt.Rectangle
import java.nio.file.Path
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.JViewport
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@TestApplication
class BookMemoryCaptureTest {
  private fun table() =
      JTable(
              DefaultTableModel(
                  arrayOf(
                      arrayOf("0x10010000", "18 00 00 00", "....", "0x00000018"),
                      arrayOf("0x10010010", "44 30 00 00", "D0..", "0x00003044"),
                  ),
                  arrayOf("Address", "Bytes", "ASCII", "Words"),
              ),
          )
          .apply {
            autoResizeMode = JTable.AUTO_RESIZE_OFF
            rowHeight = 20
            repeat(4) { columnModel.getColumn(it).width = 100 }
            setSize(400, 100)
          }

  @Test
  fun `reads complete native rows without editing model values`(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      val table = table()
      val rows = renderedMemoryRows(table)
      assertEquals(2, rows.size)
      assertEquals("0x10010000 | 18 00 00 00 | .... | 0x00000018", rows[0].text)
      assertEquals(400, rows[0].bounds.width)
      assertEquals(100, rows[0].cells.getValue("Words").width)
      assertTrue(rows[0].cells.values.all(rows[0].bounds::contains))
      assertEquals("0x00000018", table.getValueAt(0, 3))
    }
  }

  @Test
  fun `uses rendered cells rather than opaque model diagnostics`(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      val table = table()
      table.setValueAt("opaque", 0, 0)
      table.setDefaultRenderer(
          Any::class.java,
          object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(
                table: JTable?,
                value: Any?,
                selected: Boolean,
                focus: Boolean,
                row: Int,
                column: Int,
            ): Component {
              super.getTableCellRendererComponent(table, value, selected, focus, row, column)
              if (value == "opaque") text = "0x10010000"
              return this
            }
          },
      )
      assertTrue(renderedMemoryRows(table).first().text.startsWith("0x10010000 |"))
      assertEquals("opaque", table.getValueAt(0, 0))
    }
  }

  @Test
  fun `does not attest to horizontally or vertically clipped rows`(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      val table = table()
      val viewport =
          JViewport().apply {
            view = table
            viewSize = Dimension(400, 100)
            extentSize = Dimension(300, 100)
            setSize(300, 100)
          }
      assertTrue(renderedMemoryRows(table).isEmpty())
      viewport.setSize(400, 10)
      viewport.extentSize = Dimension(400, 10)
      assertTrue(renderedMemoryRows(table).isEmpty())
    }
  }

  @Test
  fun `ignores tables with unrelated columns`(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      val table = table()
      table.model = DefaultTableModel(arrayOf(arrayOf("x")), arrayOf("Other"))
      assertTrue(renderedMemoryRows(table).isEmpty())
    }
  }

  @Test
  fun `matches the displayed tab title with native mnemonic escaping`() {
    assertTrue(isRegisterCaptureTab("Threads & Variables"))
    assertTrue(isRegisterCaptureTab("Threads \\& Variables"))
    assertFalse(isRegisterCaptureTab("Console"))
    assertFalse(isRegisterCaptureTab("Threads Variables"))
  }

  @Test
  fun `rejects a capture outside the actual screen`() {
    val screen = Rectangle(40, 20, 1280, 900)
    requireVisibleCaptureViewport(Rectangle(40, 20, 1280, 900), screen)
    for (viewport in
        listOf(
            Rectangle(0, 20, 1280, 900),
            Rectangle(40, 20, 1600, 900),
            Rectangle(40, 20, 0, 900),
        )) {
      assertThrows<IllegalArgumentException> { requireVisibleCaptureViewport(viewport, screen) }
    }
  }

  @Test
  fun `resizes only the native bottom splitter and waits for its layout`(): Unit =
      timeoutRunBlocking {
        withContext(Dispatchers.EDT) {
          val bottom = JPanel()
          val splitter =
              ThreeComponentsSplitter(true).apply {
                innerComponent = JPanel()
                lastComponent = bottom
                setSize(1000, 800)
                lastSize = 200
                doLayout()
              }
          assertFalse(configureBottomCaptureHeight(bottom, 400))
          assertTrue(configureBottomCaptureHeight(bottom, 400))
          assertTrue(bottom.height >= 400)
          assertTrue(splitter.innerComponent!!.height > 0)
          assertThrows<IllegalStateException> { configureBottomCaptureHeight(JPanel(), 400) }
        }
      }

  @Test
  fun `validates absolute addresses and separates memory from register captures`() {
    for (address in listOf("0x0", "0xffffffffffffffff")) BookMemoryView(address).validate()
    for (address in listOf("", "-1", "record_context", "0x10000000000000000", "0x")) {
      assertThrows<IllegalArgumentException> { BookMemoryView(address).validate() }
    }
    val figure =
        BookCaptureFigure(
            id = "memory",
            source = "source.asm",
            sourceSha256 = "0".repeat(64),
            checkpoint = "stopped",
            expectedRows = listOf(BookExpectedRow("Directory", listOf("0x00000018"))),
            memoryView = BookMemoryView("0x10010000"),
        )
    figure.validate(Path.of("/tmp"))
    figure
        .copy(expectedRows = listOf(BookExpectedRow("Words", listOf("0x18"), "Words")))
        .validate(Path.of("/tmp"))
    assertThrows<IllegalArgumentException> {
      figure
          .copy(expectedRows = listOf(BookExpectedRow("Unknown", listOf("x"), "Other")))
          .validate(Path.of("/tmp"))
    }
    assertThrows<IllegalArgumentException> {
      figure
          .copy(
              memoryView = null,
              expectedRows = listOf(BookExpectedRow("Words", listOf("x"), "Words")),
          )
          .validate(Path.of("/tmp"))
    }
    assertThrows<IllegalArgumentException> {
      figure
          .copy(debuggerView = BookDebuggerTreeView(expand = listOf(listOf("Integer registers"))))
          .validate(Path.of("/tmp"))
    }
  }
}
