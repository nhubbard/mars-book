package edu.missouristate.mars.book

import java.awt.Point
import java.awt.Rectangle
import java.nio.file.Path
import javax.accessibility.AccessibleAction
import javax.accessibility.AccessibleContext
import javax.accessibility.AccessibleState
import javax.accessibility.AccessibleStateSet
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.SwingUtilities
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookCacheCaptureTest {
    private val execution = "Book figure: cache"
    private val view = BookCacheView(accesses = 512, misses = 128)

    private class Disclosure : JLabel("Cache Settings") {
        var expanded = true
        var expandable = true
        var collapsed = true
        var actions = 1
        var calls = 0
        override fun isShowing() = true
        override fun getAccessibleContext(): AccessibleContext {
            if (accessibleContext == null) accessibleContext = object : AccessibleJLabel(), AccessibleAction {
                override fun getAccessibleStateSet(): AccessibleStateSet = super.getAccessibleStateSet().apply {
                    if (expandable) add(AccessibleState.EXPANDABLE)
                    if (expanded) add(AccessibleState.EXPANDED) else if (collapsed) add(AccessibleState.COLLAPSED)
                }
                override fun getAccessibleAction(): AccessibleAction = this
                override fun getAccessibleActionCount() = actions
                override fun getAccessibleActionDescription(index: Int) = "Expand or collapse"
                override fun doAccessibleAction(index: Int): Boolean { calls++; expanded = !expanded; return true }
            }
            return accessibleContext
        }
    }
    private class Label(text: String) : JLabel(text) {
        var shown = true
        override fun isShowing() = shown
        override fun getLocationOnScreen() = Point(20, 20)
        override fun getVisibleRect() = Rectangle(0, 0, width, height)
        init { setSize(160, 20) }
    }
    private class Combo(value: String) : JComboBox<String>(arrayOf(value, "Other")) {
        var clipped = false
        override fun isShowing() = true
        override fun getLocationOnScreen() = Point(20, 45)
        override fun getVisibleRect() = Rectangle(0, 0, width, if (clipped) 1 else height)
        init { setSize(180, 25) }
    }
    private class Pane(text: String) : JEditorPane() {
        override fun isShowing() = true
        override fun getLocationOnScreen() = Point(20, 80)
        override fun getVisibleRect() = Rectangle(0, 0, width, height)
        init { this.text = text; setSize(250, 20) }
    }
    private inner class Fixture(val expected: BookCacheView = view) {
        val root = JPanel()
        val title = Disclosure()
        val runs = Combo("Other (#1)").apply { addItem("$execution (#2)"); selectedIndex = 2 }
        val disconnect = object : JButton("Disconnect") { override fun isShowing() = true }
        val logging = JCheckBox("Record access log", expected.logging)
        val labels = expected.settings.mapValues { (key, _) -> Label(key) }
        val combos = expected.settings.mapValues { (key, value) -> Combo(value).also { labels.getValue(key).labelFor = it } }
        val summary = Pane("${expected.bytes} bytes   |   Accesses ${expected.accesses}")
        val traffic = Pane("Hits ${expected.hits}   |   Misses ${expected.misses}")
        val rate = object : JProgressBar(0, 100) {
            override fun isShowing() = true
            override fun getLocationOnScreen() = Point(200, 105)
            override fun getVisibleRect() = Rectangle(0, 0, width, height)
        }.apply { setSize(100, 20); value = expected.hitRate; accessibleContext.accessibleName = "Hit rate" }
        val rateLabel = Label("Hit rate").apply { labelFor = rate }
        val percentage = Label("${expected.hitRate}%")
        init {
            root.add(title); root.add(runs); root.add(disconnect); root.add(logging)
            labels.values.forEach(root::add); combos.values.forEach(root::add)
            root.add(summary); root.add(traffic); root.add(rate); root.add(rateLabel); root.add(percentage)
        }
        fun observe() = observeBookCache(root, expected, execution)
    }
    @Test
    fun `native geometry counters and rounded percentage form eight observed rows`() = SwingUtilities.invokeAndWait {
        for (expected in listOf(view, view.copy(placement = "direct", ways = 1, misses = 344),
                                view.copy(placement = "set", ways = 2, replacement = "random"),
                                view.copy(accesses = 0, misses = 0), view.copy(accesses = 30, misses = 8))) {
            val observed = checkNotNull(Fixture(expected).observe())
            assertEquals(8, observed.rows.size)
            assertEquals("$execution (#2)", observed.selectedExecution)
            assertEquals("${expected.bytes} bytes | Accesses ${expected.accesses}", observed.rows[5].text)
            assertEquals("Hits ${expected.hits} | Misses ${expected.misses}", observed.rows[6].text)
            assertEquals("Hit rate: ${expected.hitRate}%", observed.rows[7].text)
            assertEquals(true, observed.evidence()["logging"])
            assertEquals(observed.rows.map { it.text }, observed.evidence()["renderedRows"])
        }
    }
    @Test
    fun `disclosure uses its accessible action and waits for native layout`() = SwingUtilities.invokeAndWait {
        val f = Fixture(); f.title.expanded = false
        assertNull(f.observe()); assertTrue(f.title.expanded); assertEquals(1, f.title.calls)
        assertNotNull(f.observe()); assertEquals(1, f.title.calls)
        f.root.remove(f.title); assertFalse(expandBookCacheSettings(f.root)); f.root.add(f.title)
        f.title.expandable = false; assertFalse(expandBookCacheSettings(f.root)); f.title.expandable = true
        f.title.expanded = false; f.title.collapsed = false; assertFalse(expandBookCacheSettings(f.root))
        f.title.collapsed = true; f.title.actions = 2; assertFalse(expandBookCacheSettings(f.root))
        assertEquals(1, f.title.calls)
    }
    @Test
    fun `every geometry control must match be enabled and be fully visible`() = SwingUtilities.invokeAndWait {
        val f = Fixture()
        for ((key, combo) in f.combos) {
            combo.selectedIndex = 1; assertNull(f.observe()); combo.selectedIndex = 0
            combo.isEnabled = false; assertNull(f.observe()); combo.isEnabled = true
            combo.clipped = true; assertNull(f.observe()); combo.clipped = false
            val label = f.labels.getValue(key)
            label.shown = false; assertNull(f.observe()); label.shown = true
            label.labelFor = f.runs; assertNull(f.observe()); label.labelFor = combo
        }
        f.root.add(Label("Cache blocks")); assertNull(f.observe())
    }
    @Test
    fun `counter mismatches and duplicate rows cannot be accepted`() = SwingUtilities.invokeAndWait {
        val f = Fixture(); val summary = f.summary.text; val traffic = f.traffic.text
        f.summary.text = "128 bytes | Accesses 513"; assertNull(f.observe()); f.summary.text = summary
        f.traffic.text = "Hits 383 | Misses 129"; assertNull(f.observe()); f.traffic.text = traffic
        f.root.add(Pane(summary)); assertNull(f.observe())
    }
    @Test
    fun `progress value and printed percentage must agree`() = SwingUtilities.invokeAndWait {
        val f = Fixture()
        f.rate.value = 74; assertNull(f.observe()); f.rate.value = 75
        f.rate.maximum = 200; assertNull(f.observe()); f.rate.maximum = 100
        f.rate.minimum = 1; assertNull(f.observe()); f.rate.minimum = 0
        f.percentage.text = "74%"; assertNull(f.observe()); f.percentage.text = "75%"
        f.rateLabel.labelFor = f.runs; assertNull(f.observe()); f.rateLabel.labelFor = f.rate
        assertNotNull(f.observe())
    }
    @Test
    fun `capture requires owned connected execution and expected logging state`() = SwingUtilities.invokeAndWait {
        val f = Fixture(); f.runs.selectedIndex = 0
        assertNull(f.observe()); assertEquals(2, f.runs.selectedIndex); assertNotNull(f.observe())
        f.disconnect.isEnabled = false; assertNull(f.observe()); f.disconnect.isEnabled = true
        f.disconnect.text = "Connect"; assertNull(f.observe()); f.disconnect.text = "Disconnect"
        f.logging.isSelected = false; assertNull(f.observe()); f.logging.isSelected = true
        f.logging.isEnabled = false; assertNull(f.observe()); f.logging.isEnabled = true
        f.root.remove(f.logging); assertNull(f.observe())
        assertNotNull(Fixture(view.copy(logging = false)).observe())
    }
    @Test
    fun `cache contract enforces native sizes policies and counter boundaries`() {
        for (bad in listOf(view.copy(blocks = 0), view.copy(blocks = 3), view.copy(blocks = 4096),
                           view.copy(wordsPerBlock = -1), view.copy(wordsPerBlock = 3), view.copy(wordsPerBlock = 4096),
                           view.copy(ways = 0), view.copy(ways = 3), view.copy(ways = 16),
                           view.copy(placement = "unknown"), view.copy(placement = "direct"), view.copy(ways = 4),
                           view.copy(replacement = "unknown"), view.copy(accesses = -1), view.copy(misses = -1), view.copy(misses = 513))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
        view.validate(); view.copy(blocks = 2048, wordsPerBlock = 2048, ways = 2048).validate()
        assertEquals(73, view.copy(accesses = 30, misses = 8).hitRate)
        assertEquals(0, view.copy(accesses = 0, misses = 0).hitRate)
    }
    @Test
    fun `cache capture cannot mix other tools or presentation modes`() {
        val root = Path.of("/tmp/fixture")
        val f = BookCaptureFigure(id = "cache", source = "main.asm", sourceSha256 = "a".repeat(64), checkpoint = "done", cacheView = view)
        f.validate(root)
        for (bad in listOf(f.copy(expectedRows = listOf(BookExpectedRow("Value", listOf("42")))),
                           f.copy(memoryView = BookMemoryView("0x10010000")),
                           f.copy(debuggerView = BookDebuggerTreeView(expand = listOf(listOf("Integer registers")))),
                           f.copy(digitalLabView = BookDigitalLabView()), f.copy(marsBotView = BookMarsBotView()),
                           f.copy(mode = "editor", checkpoint = "", editorCallouts = listOf(BookTextCallout("Source", "text"))))) {
            assertThrows<IllegalArgumentException> { bad.validate(root) }
        }
    }
}
