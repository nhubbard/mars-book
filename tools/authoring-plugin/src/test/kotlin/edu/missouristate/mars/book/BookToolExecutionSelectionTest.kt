package edu.missouristate.mars.book

import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import javax.swing.JComboBox
import javax.swing.SwingUtilities
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class BookToolExecutionSelectionTest {
    private val execution = "Book figure: cache"

    private fun selector(vararg labels: String) = JComboBox(labels).also {
        it.renderer = textListCellRenderer { label: String -> label }
    }

    @Test
    fun `settings controls do not prevent selecting the owned execution`() = SwingUtilities.invokeAndWait {
        val placement = selector("Direct mapping", "Fully associative")
        val blocks = selector("4", "8", "16")
        val runs = selector("Other (#1)", "$execution (#2)")
        val components = listOf(placement, blocks, runs)
        assertNull(selectBookToolExecution(components, execution))
        assertEquals(1, runs.selectedIndex)
        assertEquals(0, placement.selectedIndex)
        assertEquals(0, blocks.selectedIndex)
        assertEquals("$execution (#2)", selectBookToolExecution(components, execution))
    }

    @Test
    fun `duplicate executions in one selector leave all selections unchanged`() = SwingUtilities.invokeAndWait {
        val runs = selector("Other (#1)", "$execution (#2)", "$execution (#3)")
        assertNull(selectBookToolExecution(listOf(runs), execution))
        assertEquals(0, runs.selectedIndex)
    }

    @Test
    fun `duplicate executions across selectors leave both selections unchanged`() = SwingUtilities.invokeAndWait {
        val first = selector("Other (#1)", "$execution (#2)")
        val second = selector("Other (#3)", "$execution (#4)")
        assertNull(selectBookToolExecution(listOf(first, second), execution))
        assertEquals(0, first.selectedIndex)
        assertEquals(0, second.selectedIndex)
    }

    @Test
    fun `ambiguous selector cannot be discarded in favor of another match`() = SwingUtilities.invokeAndWait {
        val ambiguous = selector("Other (#1)", "$execution (#2)", "$execution (#3)")
        val single = selector("Other (#4)", "$execution (#5)")
        assertNull(selectBookToolExecution(listOf(ambiguous, single), execution))
        assertEquals(0, ambiguous.selectedIndex)
        assertEquals(0, single.selectedIndex)
    }

    @Test
    fun `missing execution and similar names cannot change a native control`() = SwingUtilities.invokeAndWait {
        val other = selector("Other (#1)", "${execution} extra (#2)")
        assertNull(selectBookToolExecution(emptyList(), execution))
        assertNull(selectBookToolExecution(listOf(selector()), execution))
        assertNull(selectBookToolExecution(listOf(other), execution))
        assertEquals(0, other.selectedIndex)
    }

    @Test
    fun `execution ownership comes from the rendered label rather than model text`() = SwingUtilities.invokeAndWait {
        data class Item(val number: Int)
        val runs = JComboBox(arrayOf(Item(1), Item(2)))
        runs.renderer = textListCellRenderer { item: Item ->
            if (item.number == 2) "$execution (#2)" else "Other (#1)"
        }
        assertNull(selectBookToolExecution(listOf(runs), execution))
        assertEquals(1, runs.selectedIndex)
        assertEquals("$execution (#2)", selectBookToolExecution(listOf(runs), execution))
    }
}
