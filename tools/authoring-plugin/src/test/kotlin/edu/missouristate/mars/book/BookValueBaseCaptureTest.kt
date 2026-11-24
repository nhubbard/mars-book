package edu.missouristate.mars.book

import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@TestApplication
class BookValueBaseCaptureTest {
  private class Toggle(name: String = "Hexadecimal MARS Values") : ToggleAction(name) {
    var selected = true
    var changes = 0

    override fun isSelected(event: AnActionEvent) = selected

    override fun setSelected(event: AnActionEvent, state: Boolean) {
      selected = state
      changes++
    }
  }

  @Test
  fun `find native toggle through nested materialized groups without duplicating shared actions`() =
      timeoutRunBlocking {
        withContext(Dispatchers.EDT) {
          val toggle = Toggle()
          val gear = DefaultActionGroup(toggle)
          val more = DefaultActionGroup(gear)
          assertSame(toggle, findBookValueBaseAction(listOf(more, gear, toggle)))
          gear.add(more)
          assertSame(toggle, findBookValueBaseAction(listOf(more)))
        }
      }

  @Test
  fun `missing or ambiguous matching actions are rejected`() = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      assertNull(findBookValueBaseAction(emptyList()))
      assertNull(findBookValueBaseAction(listOf(Toggle("Other"))))
      assertNull(findBookValueBaseAction(listOf(DefaultActionGroup(Toggle(), Toggle()))))
    }
  }

  @Test
  fun `native toggle changes once then waits before accepting each requested base`() =
      timeoutRunBlocking {
        withContext(Dispatchers.EDT) {
          val toggle = Toggle()
          val event =
              AnActionEvent.createEvent(
                  toggle,
                  DataContext.EMPTY_CONTEXT,
                  null,
                  "MarsBookCapture",
                  ActionUiKind.TOOLBAR,
                  null,
              )
          assertTrue(configureBookValueBase(toggle, event, "hex"))
          assertEquals(0, toggle.changes)
          assertFalse(configureBookValueBase(toggle, event, "decimal"))
          assertFalse(toggle.selected)
          assertTrue(configureBookValueBase(toggle, event, "decimal"))
          assertEquals(1, toggle.changes)
          assertFalse(configureBookValueBase(toggle, event, "hex"))
          assertTrue(toggle.selected)
          assertTrue(configureBookValueBase(toggle, event, "hex"))
          assertEquals(2, toggle.changes)
          assertThrows<IllegalArgumentException> { configureBookValueBase(toggle, event, "octal") }
          assertEquals(2, toggle.changes)
        }
      }

  @Test
  fun `explicit display selection is restricted to supported register captures`() {
    val root = Path.of("/tmp/fixture")
    val figure =
        BookCaptureFigure(
            id = "overflow",
            source = "main.asm",
            sourceSha256 = "a".repeat(64),
            checkpoint = "ready",
            expectedRows = listOf(BookExpectedRow("Wrapped", listOf("-2147483392"))),
            valueBase = "decimal",
        )
    figure.validate(root)
    figure.copy(valueBase = "hex").validate(root)
    for (bad in
        listOf(
            figure.copy(valueBase = "octal"),
            figure.copy(memoryView = BookMemoryView("0x10010000")),
            figure.copy(expectedRows = emptyList()),
            figure.copy(
                mode = "editor",
                checkpoint = "",
                expectedRows = emptyList(),
                editorCallouts = listOf(BookTextCallout("Source", "text")),
            ),
            figure.copy(cacheView = BookCacheView()),
        )) {
      assertThrows<IllegalArgumentException> { bad.validate(root) }
    }
  }
}
