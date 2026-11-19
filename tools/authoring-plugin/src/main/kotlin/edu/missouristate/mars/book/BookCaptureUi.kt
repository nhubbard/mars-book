package edu.missouristate.mars.book

import com.intellij.ui.SimpleColoredComponent
import java.awt.Component
import java.awt.Container
import javax.swing.JLabel

internal fun captureComponents(root: Component): Sequence<Component> = sequence {
    yield(root)
    if (root is Container) root.components.forEach { yieldAll(captureComponents(it)) }
}

/** Read on the EDT. The native progress widget renders its status through ordinary Swing labels. */
internal fun visibleAnalysisIndicators(editor: Component): List<String> = captureComponents(editor)
    .filter { it.isShowing }
    .mapNotNull {
        when (it) {
            is JLabel -> it.text
            is SimpleColoredComponent -> it.getCharSequence(false).toString()
            else -> null
        }
    }
    .map(String::trim)
    .filter { it.startsWith("Analyzing") || it.startsWith("Indexing") }
    .toList()
