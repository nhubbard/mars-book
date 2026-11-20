package edu.missouristate.mars.book

import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Graphics
import java.awt.Point
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.swing.JPanel
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JEditorPane
import javax.swing.JToggleButton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookDigitalLabCaptureTest {
    private fun display(mask: Int, name: String = "Right") = object : JPanel() {
        init {
            setSize(JBUI.scale(46), JBUI.scale(73))
            getAccessibleContext().accessibleName = "$name display (0x${mask.toString(16).padStart(2, '0')})"
            toolTipText = getAccessibleContext().accessibleName
        }
        override fun isShowing() = true
        override fun getLocationOnScreen() = Point(100, 100)
        override fun paintComponent(graphics: Graphics) {
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, width, height)
            bookLabSegmentCenters.forEachIndexed { bit, (x, y) ->
                graphics.color = if (mask and (1 shl bit) != 0) Color.RED else Color.WHITE
                graphics.fillRect(JBUI.scale(x), JBUI.scale(y), 1, 1)
            }
        }
    }

    @Test
    fun `native painted segment bits include the decimal point and reject false metadata`() {
        for (mask in listOf(0, 0x3f, 0x77, 0x71, 255)) {
            val component = display(mask)
            val observed = checkNotNull(observeBookLabDisplay(component, "Right", mask))
            assertEquals((0..7).map { mask and (1 shl it) != 0 }, observed.map { it.lit })
            assertNull(observeBookLabDisplay(component, "Right", mask xor 1))
            component.toolTipText = "Other display"
            assertNull(observeBookLabDisplay(component, "Right", mask))
        }
    }

    @Test
    fun `photograph must show each expected lit and unlit segment inside its bounds`() {
        val samples = checkNotNull(observeBookLabDisplay(display(0x77), "Right", 0x77))
        val observed = BookObservedDigitalLab(emptyList(), "Owned run", samples)
        val image = BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB)
        samples.forEach { image.setRGB(it.x, it.y, it.nativeRgb) }
        assertEquals(8, observed.screenEvidence(image, Rectangle(0, 0, 300, 300)).size)
        image.setRGB(samples[0].x, samples[0].y, Color.WHITE.rgb)
        assertThrows<IllegalStateException> { observed.screenEvidence(image, Rectangle(0, 0, 300, 300)) }
        assertThrows<IllegalArgumentException> { observed.screenEvidence(image, Rectangle(500, 500, 300, 300)) }
        val tooSmall = display(0x77).also { it.setSize(1, 1) }
        assertNull(observeBookLabDisplay(tooSmall, "Right", 0x77))
    }

    @Test
    fun `segment classification excludes unlit outlines and nonred colors`() {
        assertTrue(isBookLitSegment(Color.RED.rgb))
        for (color in listOf(Color.BLACK, Color.WHITE, Color.GRAY, Color.BLUE, Color.GREEN, Color.YELLOW, Color(149, 0, 0), Color(150, 75, 0))) {
            assertFalse(isBookLitSegment(color.rgb))
        }
    }

    @Test
    fun `whole native view selects its owned run and requires idle connected controls`() {
        val root = JPanel()
        val selector = object : JComboBox<String>(arrayOf("Other (#1)", "Book figure: lab (#2)")) { override fun isShowing() = true }
        val disconnect = object : JButton("Disconnect") { override fun isShowing() = true }
        val timer = object : JEditorPane() {
            override fun isShowing() = true
            override fun getLocationOnScreen() = Point(100, 200)
        }.also { it.text = "Counter interrupt off"; it.setSize(200, 20) }
        val keys = (0..15).map { index -> object : JToggleButton(index.toString(16).uppercase()) {
            override fun isShowing() = true
            override fun getLocationOnScreen() = Point(100 + index * 10, 230)
        }.also { it.setSize(10, 10) } }
        root.add(selector); root.add(disconnect); root.add(timer)
        root.add(display(0, "Left")); root.add(display(0x77))
        keys.forEach(root::add)
        val expected = BookDigitalLabView(right = 0x77)
        assertNull(observeBookDigitalLab(root, expected, "Book figure: lab"))
        val observed = checkNotNull(observeBookDigitalLab(root, expected, "Book figure: lab"))
        assertEquals(4, observed.rows.size)
        assertEquals(16, observed.samples.size)
        assertNull(observeBookDigitalLab(root, expected, "Unowned"))
        keys[3].isSelected = true
        assertNull(observeBookDigitalLab(root, expected, "Book figure: lab"))
        keys[3].isSelected = false; keys[3].isEnabled = false
        assertNull(observeBookDigitalLab(root, expected, "Book figure: lab"))
        keys[3].isEnabled = true; timer.text = "Counter interrupt in 10 instructions"
        assertNull(observeBookDigitalLab(root, expected, "Book figure: lab"))
        timer.text = "Counter interrupt off"; disconnect.text = "Connect"
        assertNull(observeBookDigitalLab(root, expected, "Book figure: lab"))
    }

    @Test
    fun `tool and conditional breakpoint contracts cannot mix unrelated capture modes`() {
        val figure = BookCaptureFigure(id = "lab", source = "main.asm", sourceSha256 = "a".repeat(64), checkpoint = "displayed", digitalLabView = BookDigitalLabView(right = 0x77), breakpointCondition = "\$s2 == 10")
        val root = Path.of("/tmp/fixture")
        figure.validate(root)
        for (bad in listOf(figure.copy(digitalLabView = BookDigitalLabView(left = -1)), figure.copy(digitalLabView = BookDigitalLabView(right = 256)),
                           figure.copy(memoryView = BookMemoryView("0x10010000")), figure.copy(expectedRows = listOf(BookExpectedRow("Index", listOf("10")))),
                           figure.copy(breakpointCondition = ""), figure.copy(breakpointCondition = "x\ny"), figure.copy(breakpointCondition = "1".repeat(257)),
                           figure.copy(stepFrom = listOf("start")), figure.copy(mode = "editor", checkpoint = "", editorCallouts = listOf(BookTextCallout("Source", "text"))))) {
            assertThrows<IllegalArgumentException> { bad.validate(root) }
        }
    }
}
