package edu.missouristate.mars.book

import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import java.awt.Component
import java.awt.Dimension
import javax.swing.JTree
import javax.swing.JViewport
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
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
class BookDebuggerTreeViewTest {
  private fun tree(): JTree {
    val root = DefaultMutableTreeNode("Debugger")
    root.add(
        DefaultMutableTreeNode("Integer registers").apply { add(DefaultMutableTreeNode("v1")) },
    )
    root.add(
        DefaultMutableTreeNode("MSA vector registers").apply {
          add(DefaultMutableTreeNode("w20"))
          add(
              DefaultMutableTreeNode("w2").apply {
                add(DefaultMutableTreeNode("32-bit integer lanes"))
              },
          )
        },
    )
    return JTree(root).apply {
      isRootVisible = false
      setSize(2000, 2000)
    }
  }

  @Test
  fun `expands the exact native path and waits after layout changes`(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      val tree = tree()
      val view =
          BookDebuggerTreeView(
              collapse = listOf(listOf("Integer registers")),
              expand = listOf(listOf("MSA vector registers"), listOf("MSA vector registers", "w2")),
          )
      tree.expandPath(debuggerTreePath(tree, listOf("Integer registers")))
      assertFalse(applyDebuggerTreeView(tree, view))
      assertTrue(applyDebuggerTreeView(tree, view))
      assertFalse(tree.isExpanded(debuggerTreePath(tree, listOf("Integer registers"))))
      assertTrue(tree.isExpanded(debuggerTreePath(tree, listOf("MSA vector registers", "w2"))))
      assertEquals(
          "w2",
          debuggerTreePath(tree, listOf("MSA vector registers", "w2"))!!
              .lastPathComponent
              .toString(),
      )
      assertNull(debuggerTreePath(tree, listOf("MSA vector registers", "w")))
    }
  }

  @Test
  fun `waits for asynchronously populated children and rejects ambiguous trees`(): Unit =
      timeoutRunBlocking {
        withContext(Dispatchers.EDT) {
          val tree = tree()
          val view = BookDebuggerTreeView(expand = listOf(listOf("MSA vector registers", "late")))
          assertFalse(applyDebuggerTreeView(tree, view))
          val parent =
              debuggerTreePath(tree, listOf("MSA vector registers"))!!.lastPathComponent
                  as DefaultMutableTreeNode
          (tree.model as DefaultTreeModel).insertNodeInto(
              DefaultMutableTreeNode("late").apply { add(DefaultMutableTreeNode("value")) },
              parent,
              parent.childCount,
          )
          assertFalse(applyDebuggerTreeView(tree, view))
          assertTrue(applyDebuggerTreeView(tree, view))
          assertSame(tree, selectDebuggerTree(listOf(tree), view))
          assertNull(selectDebuggerTree(listOf(JTree(DefaultMutableTreeNode("Other"))), view))
          assertThrows<IllegalStateException> { selectDebuggerTree(listOf(tree, tree()), view) }
          (tree.model as DefaultTreeModel).insertNodeInto(
              DefaultMutableTreeNode("late"),
              parent,
              parent.childCount,
          )
          assertThrows<IllegalStateException> {
            debuggerTreePath(tree, listOf("MSA vector registers", "late"))
          }
        }
      }

  @Test
  fun `reveals an offscreen lane row and waits for its scroll layout`(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      val tree = tree()
      val parent =
          debuggerTreePath(tree, listOf("MSA vector registers", "w2"))!!.lastPathComponent
              as DefaultMutableTreeNode
      repeat(30) {
        (tree.model as DefaultTreeModel).insertNodeInto(
            DefaultMutableTreeNode("lane $it"),
            parent,
            0,
        )
      }
      val viewport =
          JViewport().apply {
            setSize(600, 100)
            view = tree
            extentSize = Dimension(600, 100)
            viewSize = Dimension(600, 1600)
          }
      val view =
          BookDebuggerTreeView(
              expand = listOf(listOf("MSA vector registers"), listOf("MSA vector registers", "w2")),
              reveal = listOf("MSA vector registers", "w2", "32-bit integer lanes"),
          )
      assertFalse(applyDebuggerTreeView(tree, view))
      assertTrue(applyDebuggerTreeView(tree, view))
      assertTrue(tree.visibleRect.contains(tree.getPathBounds(debuggerTreePath(tree, view.reveal))))
      assertTrue(viewport.viewPosition.y > 0)
    }
  }

  @Test
  fun `uses displayed names rather than opaque model debug strings`(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      val root = DefaultMutableTreeNode("root")
      val group = DefaultMutableTreeNode("opaque group")
      val vector = DefaultMutableTreeNode("opaque vector")
      root.add(group)
      group.add(vector)
      val tree =
          JTree(root).apply {
            cellRenderer =
                object : DefaultTreeCellRenderer() {
                  override fun getTreeCellRendererComponent(
                      tree: JTree?,
                      value: Any?,
                      selected: Boolean,
                      expanded: Boolean,
                      leaf: Boolean,
                      row: Int,
                      focus: Boolean,
                  ): Component {
                    super.getTreeCellRendererComponent(
                        tree,
                        value,
                        selected,
                        expanded,
                        leaf,
                        row,
                        focus,
                    )
                    text =
                        when (value) {
                          group -> "MSA vector registers"
                          vector -> "\$w2 (written) = 0x00000000000000000000008800000000"
                          else -> value.toString()
                        }
                    return this
                  }
                }
          }
      assertSame(
          vector,
          debuggerTreePath(tree, listOf("MSA vector registers", "\$w2"))!!.lastPathComponent,
      )
      assertNull(debuggerTreePath(tree, listOf("opaque group")))
    }
  }

  @Test
  fun `rejects empty duplicate and contradictory view paths`() {
    for (view in
        listOf(
            BookDebuggerTreeView(expand = listOf(emptyList())),
            BookDebuggerTreeView(expand = listOf(listOf(""))),
            BookDebuggerTreeView(expand = listOf(listOf("MSA"), listOf("MSA"))),
            BookDebuggerTreeView(
                collapse = listOf(listOf("MSA")),
                expand = listOf(listOf("MSA", "w2")),
            ),
            BookDebuggerTreeView(collapse = listOf(listOf("MSA")), reveal = listOf("MSA", "w2")),
        )) {
      assertThrows<IllegalArgumentException> { view.validate() }
    }
    BookDebuggerTreeView().validate()
  }
}
