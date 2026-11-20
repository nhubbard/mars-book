package edu.missouristate.mars.book

import com.intellij.util.ui.JBUI
import java.awt.Rectangle
import java.awt.image.BufferedImage
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JToggleButton

/** Expected port masks; all observations use public Swing accessibility and painting. */
data class BookDigitalLabView(val left: Int = 0, val right: Int = 0) {
    fun validate() { require(left in 0..255 && right in 0..255) { "Digital Lab masks must be bytes" } }
}

internal fun isBookLitSegment(rgb: Int): Boolean {
    val red = (rgb ushr 16) and 255
    return red >= 150 && red > 2 * ((rgb ushr 8) and 255) && red > 2 * (rgb and 255)
}

internal data class BookLabSample(val x: Int, val y: Int, val lit: Boolean, val nativeRgb: Int)
internal data class BookObservedDigitalLab(val rows: List<BookUiRow>, val selectedExecution: String, val samples: List<BookLabSample>) {
    fun screenEvidence(image: BufferedImage, viewport: Rectangle): List<Map<String, Any>> = samples.map { sample ->
        val x = sample.x - viewport.x
        val y = sample.y - viewport.y
        require(x in 0 until image.width && y in 0 until image.height) { "Digital Lab sample is outside the photograph" }
        val rgb = image.getRGB(x, y) and 0xffffff
        check(isBookLitSegment(rgb) == sample.lit) { "Photographed segment differs from its expected mask" }
        mapOf("x" to x, "y" to y, "lit" to sample.lit, "nativeRgb" to sample.nativeRgb, "screenRgb" to rgb)
    }
}

/** The centers identify the eight visible segment shapes, including the decimal point. */
internal val bookLabSegmentCenters = listOf(21 to 7, 35 to 20, 35 to 46, 21 to 59, 7 to 46, 7 to 20, 21 to 33, 40 to 59)

internal fun observeBookLabDisplay(component: JComponent, name: String, mask: Int): List<BookLabSample>? {
    val description = "$name display (0x${mask.toString(16).padStart(2, '0')})"
    if (component.accessibleContext?.accessibleName != description || component.toolTipText?.contains(description) != true) return null
    if (component.width < JBUI.scale(46) || component.height < JBUI.scale(73)) return null
    val image = BufferedImage(component.width, component.height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()
    try { component.paint(graphics) } finally { graphics.dispose() }
    val screen = component.locationOnScreen
    return bookLabSegmentCenters.mapIndexed { bit, (x, y) ->
        val px = JBUI.scale(x); val py = JBUI.scale(y)
        val rgb = image.getRGB(px, py) and 0xffffff
        val lit = mask and (1 shl bit) != 0
        if (isBookLitSegment(rgb) != lit) return null
        BookLabSample(screen.x + px, screen.y + py, lit, rgb)
    }
}

internal fun observeBookDigitalLab(root: JComponent, view: BookDigitalLabView, execution: String): BookObservedDigitalLab? {
    val components = captureComponents(root).filter { it.isShowing }.toList()
    @Suppress("UNCHECKED_CAST")
    val selector = components.filterIsInstance<JComboBox<*>>().singleOrNull() as? JComboBox<Any> ?: return null
    fun label(index: Int): String {
        val rendered = selector.renderer.getListCellRendererComponent(JList<Any>(), selector.getItemAt(index), index, false, false)
        return captureComponents(rendered).filterIsInstance<JLabel>().joinToString(" ") { it.text.orEmpty() }
    }
    val owned = (0 until selector.itemCount).singleOrNull { label(it).startsWith("$execution (#") } ?: return null
    if (selector.selectedIndex != owned) { selector.selectedIndex = owned; return null }
    if (components.filterIsInstance<JButton>().none { it.text == "Disconnect" && it.isEnabled }) return null
    val displays = listOf("Left" to view.left, "Right" to view.right).map { (name, mask) ->
        val target = components.filterIsInstance<JComponent>().singleOrNull {
            it.accessibleContext?.accessibleName == "$name display (0x${mask.toString(16).padStart(2, '0')})"
        } ?: return null
        target to (observeBookLabDisplay(target, name, mask) ?: return null)
    }
    val timer = components.filterIsInstance<JEditorPane>().singleOrNull {
        it.document.getText(0, it.document.length).trim() == "Counter interrupt off"
    } ?: return null
    val keys = components.filterIsInstance<JToggleButton>()
    if (keys.size != 16 || keys.any { it.isSelected || !it.isEnabled } || keys.map { it.text }.toSet() != (0..15).map { it.toString(16).uppercase() }.toSet()) return null
    fun bounds(component: JComponent) = Rectangle(component.locationOnScreen, component.size)
    val keypad = keys.map(::bounds).reduce { a, b -> a.union(b) }
    val rows = displays.map { (component, _) -> BookUiRow(component.accessibleContext.accessibleName, bounds(component)) } +
        listOf(BookUiRow("Counter interrupt off", bounds(timer)), BookUiRow("No pressed key; 16 enabled keys", keypad))
    return BookObservedDigitalLab(rows, label(owned), displays.flatMap { it.second })
}

/** Diagnostic observations are retained only for an unaccepted authoring attempt. */
internal fun bookLabDiagnostics(root: JComponent): List<String> = listOf("Digital Lab diagnostics") +
    captureComponents(root).filterIsInstance<JComponent>().filter { it.isShowing }.mapNotNull { component ->
        val name = component.accessibleContext?.accessibleName
        when {
            component is JComboBox<*> -> "Selector: ${component.selectedItem}; items=${component.itemCount}"
            component is JEditorPane -> "Text: ${component.document.getText(0, component.document.length).trim()}"
            component is JToggleButton -> "Key: ${component.text}; enabled=${component.isEnabled}; pressed=${component.isSelected}"
            component is JButton && !component.text.isNullOrEmpty() -> "Button: ${component.text}; enabled=${component.isEnabled}"
            name?.startsWith("Left display") == true || name?.startsWith("Right display") == true -> {
                val image = BufferedImage(component.width, component.height, BufferedImage.TYPE_INT_RGB)
                val graphics = image.createGraphics()
                try { component.paint(graphics) } finally { graphics.dispose() }
                "$name; size=${component.width}x${component.height}; tooltip=${component.toolTipText}; colors=" +
                    bookLabSegmentCenters.joinToString(",") { (x, y) -> "%06x".format(image.getRGB(JBUI.scale(x), JBUI.scale(y)) and 0xffffff) }
            }
            component is JLabel && !component.text.isNullOrBlank() -> "Label: ${component.text}"
            else -> null
        }
    }.toList()
