package edu.missouristate.mars.book

import java.awt.Rectangle
import javax.accessibility.AccessibleState
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JProgressBar
import kotlin.math.roundToInt

/** Expectations are derived outside the IDE; the observer reads native controls only. */
data class BookCacheView(
    val blocks: Int = 8,
    val wordsPerBlock: Int = 4,
    val placement: String = "full",
    val ways: Int = 8,
    val replacement: String = "lru",
    val accesses: Long = 0,
    val misses: Long = 0,
    val logging: Boolean = true,
) {
    fun validate() {
        fun validSize(value: Int) = value in 1..2048 && value and (value - 1) == 0
        require(validSize(blocks) && validSize(wordsPerBlock) && validSize(ways) && ways <= blocks) { "Invalid cache geometry" }
        require(placement in listOf("direct", "full", "set") && replacement in listOf("lru", "random")) { "Invalid cache policy" }
        require(placement != "direct" || ways == 1) { "Direct mapping has one block per set" }
        require(placement != "full" || ways == blocks) { "Full associativity has one set" }
        require(accesses >= 0 && misses in 0..accesses) { "Invalid cache counters" }
    }

    val hits get() = accesses - misses
    val bytes get() = blocks * wordsPerBlock * 4
    val hitRate get() = if (accesses == 0L) 0 else (100.0 * hits / accesses).roundToInt()
    internal val settings get() = linkedMapOf(
        "Placement" to when (placement) { "direct" -> "Direct mapping"; "full" -> "Fully associative"; else -> "Set associative" },
        "Cache blocks" to blocks.toString(), "Words per block" to wordsPerBlock.toString(), "Blocks per set" to ways.toString(),
        "Replacement" to if (replacement == "lru") "Least recently used" else "Random",
    )
}

internal data class BookObservedCache(val rows: List<BookUiRow>, val selectedExecution: String, val logging: Boolean) {
    fun evidence() = mapOf("selectedExecution" to selectedExecution, "logging" to logging, "renderedRows" to rows.map { it.text })
}

/** Expands only the native disclosure through Swing accessibility, without applying/resetting cache settings. */
internal fun expandBookCacheSettings(root: JComponent): Boolean {
    val title = captureComponents(root).filterIsInstance<JLabel>().singleOrNull { it.isShowing && it.text == "Cache Settings" } ?: return false
    val context = title.accessibleContext ?: return false
    val states = context.accessibleStateSet
    if (!states.contains(AccessibleState.EXPANDABLE)) return false
    if (states.contains(AccessibleState.EXPANDED)) return true
    if (!states.contains(AccessibleState.COLLAPSED)) return false
    val action = context.accessibleAction ?: return false
    if (action.accessibleActionCount != 1) return false
    action.doAccessibleAction(0)
    return false // Wait for the native layout before observing the expanded controls.
}

internal fun observeBookCache(root: JComponent, view: BookCacheView, execution: String): BookObservedCache? {
    val all = captureComponents(root).toList()
    val visible = all.filter { it.isShowing }
    val selected = selectBookToolExecution(visible, execution) ?: return null
    if (!expandBookCacheSettings(root)) return null
    if (visible.filterIsInstance<JButton>().none { it.text == "Disconnect" && it.isEnabled }) return null
    val log = all.filterIsInstance<JCheckBox>().singleOrNull { it.text == "Record access log" } ?: return null
    if (!log.isEnabled || log.isSelected != view.logging) return null
    fun bounds(component: JComponent): Rectangle? {
        if (component.width <= 0 || component.height <= 0 || !component.visibleRect.contains(Rectangle(0, 0, component.width, component.height))) return null
        return Rectangle(component.locationOnScreen, component.size)
    }
    val rows = view.settings.map { (name, expected) ->
        val label = visible.filterIsInstance<JLabel>().singleOrNull { it.text == name } ?: return null
        @Suppress("UNCHECKED_CAST")
        val combo = label.labelFor as? JComboBox<Any> ?: return null
        if (combo !in visible || !combo.isEnabled || combo.selectedIndex < 0 || bookComboItemText(combo, combo.selectedIndex) != expected) return null
        BookUiRow("$name: $expected", (bounds(label) ?: return null).union(bounds(combo) ?: return null))
    }.toMutableList()
    for (text in listOf("${view.bytes} bytes | Accesses ${view.accesses}", "Hits ${view.hits} | Misses ${view.misses}")) {
        val pane = visible.filterIsInstance<JEditorPane>().singleOrNull {
            it.document.getText(0, it.document.length).trim().replace(Regex("\\s+"), " ") == text
        } ?: return null
        rows += BookUiRow(text, bounds(pane) ?: return null)
    }
    val rate = visible.filterIsInstance<JProgressBar>().singleOrNull { it.accessibleContext?.accessibleName == "Hit rate" } ?: return null
    if (rate.minimum != 0 || rate.maximum != 100 || rate.value != view.hitRate) return null
    val label = visible.filterIsInstance<JLabel>().singleOrNull { it.text == "Hit rate" && it.labelFor === rate } ?: return null
    val percent = visible.filterIsInstance<JLabel>().singleOrNull { it.text == "${view.hitRate}%" } ?: return null
    rows += BookUiRow("Hit rate: ${view.hitRate}%", (bounds(label) ?: return null).union(bounds(percent) ?: return null).union(bounds(rate) ?: return null))
    return BookObservedCache(rows, selected, log.isSelected)
}

internal fun bookCacheDiagnostics(root: JComponent): List<String> = listOf("Cache diagnostics") +
    captureComponents(root).filterIsInstance<JComponent>().mapNotNull {
        when (it) {
            is JLabel -> it.text?.takeIf(String::isNotBlank)?.let { text -> "Label: $text; showing=${it.isShowing}" }
            is JComboBox<*> -> "Combo: ${it.selectedItem}; showing=${it.isShowing}; enabled=${it.isEnabled}"
            is JEditorPane -> "Text: ${it.document.getText(0, it.document.length).trim()}"
            is JProgressBar -> "Rate: ${it.value}"
            is JCheckBox -> "Log: ${it.isSelected}; enabled=${it.isEnabled}"
            else -> null
        }
    }.toList()
