package edu.missouristate.mars.book

import com.intellij.ui.SimpleColoredComponent
import java.awt.Component
import java.awt.Container
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.TreePath

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


/** Uses only the Swing model and expansion/scroll APIs. Missing asynchronous children are retried. */
internal fun debuggerTreePath(tree: JTree, names: List<String>): TreePath? {
    var path = TreePath(tree.model.root)
    for (name in names) {
        val parent = path.lastPathComponent
        val matches = (0 until tree.model.getChildCount(parent)).map { tree.model.getChild(parent, it) }.filter {
            renderedDebuggerRow(tree, path.pathByAddingChild(it)).substringBefore(" = ").removeSuffix(" (written)") == name
        }
        check(matches.size <= 1) { "Ambiguous native debugger path: $names" }
        val child = matches.singleOrNull() ?: return null
        path = path.pathByAddingChild(child)
    }
    return path
}

/** Returns false after a layout change or while children are loading; capture waits for stable rows. */
internal fun applyDebuggerTreeView(tree: JTree, view: BookDebuggerTreeView): Boolean {
    var changed = false
    for (names in view.collapse) {
        val path = debuggerTreePath(tree, names) ?: return false
        if (tree.isExpanded(path)) { tree.collapsePath(path); changed = true }
    }
    for (names in view.expand) {
        val path = debuggerTreePath(tree, names) ?: return false
        if (!tree.isExpanded(path)) { tree.expandPath(path); changed = true }
    }
    if (view.reveal.isNotEmpty()) {
        val path = debuggerTreePath(tree, view.reveal) ?: return false
        val bounds = tree.getPathBounds(path) ?: return false
        if (!tree.visibleRect.contains(bounds)) { tree.scrollPathToVisible(path); changed = true }
    }
    return !changed
}

internal fun selectDebuggerTree(trees: List<JTree>, view: BookDebuggerTreeView): JTree? {
    val anchor = (view.expand.firstOrNull() ?: view.collapse.firstOrNull() ?: view.reveal).take(1)
    require(anchor.isNotEmpty())
    val matches = trees.filter { debuggerTreePath(it, anchor) != null }
    check(matches.size <= 1) { "More than one visible tree matches the debugger view" }
    return matches.singleOrNull()
}


internal fun renderedDebuggerRow(tree: JTree, path: TreePath): String {
    val row = tree.getRowForPath(path)
    val value = path.lastPathComponent
    val renderer = tree.cellRenderer.getTreeCellRendererComponent(tree, value, tree.isPathSelected(path), tree.isExpanded(path), tree.model.isLeaf(value), row, false)
    return when (renderer) {
        is SimpleColoredComponent -> renderer.getCharSequence(false).toString()
        is JLabel -> renderer.text
        else -> renderer.accessibleContext?.accessibleName.orEmpty()
    }
}
