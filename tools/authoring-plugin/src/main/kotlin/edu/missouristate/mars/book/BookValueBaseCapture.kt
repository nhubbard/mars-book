package edu.missouristate.mars.book

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import java.awt.Component
import java.util.Collections
import java.util.IdentityHashMap

private const val BOOK_HEX_VALUES_ACTION = "Hexadecimal MARS Values"

/**
 * Only public, materialized DefaultActionGroup children are inspected; dynamic groups stay opaque.
 */
internal fun findBookValueBaseAction(roots: List<AnAction>): ToggleAction? {
  val seen = Collections.newSetFromMap(IdentityHashMap<AnAction, Boolean>())
  val matches = mutableListOf<ToggleAction>()
  fun visit(action: AnAction) {
    if (!seen.add(action)) return
    if (action is ToggleAction && action.templatePresentation.text == BOOK_HEX_VALUES_ACTION)
        matches += action
    if (action is DefaultActionGroup) action.childActionsOrStubs.forEach(::visit)
  }
  roots.forEach(::visit)
  return matches.singleOrNull()
}

private fun bookValueBaseAction(root: Component): Pair<ToggleAction, AnActionEvent>? {
  val actions =
      captureComponents(root)
          .filterIsInstance<ActionToolbar>()
          .flatMap { it.actions.asSequence() }
          .toList()
  val action = findBookValueBaseAction(actions) ?: return null
  val event =
      AnActionEvent.createEvent(
          action,
          DataManager.getInstance().getDataContext(root),
          null,
          "MarsBookCapture",
          ActionUiKind.TOOLBAR,
          null,
      )
  return action to event
}

/** Changing the normal display toggle requires another observation after native views rebuild. */
internal fun configureBookValueBase(
    action: ToggleAction,
    event: AnActionEvent,
    base: String,
): Boolean {
  require(base in listOf("hex", "decimal"))
  val selected = base == "hex"
  if (action.isSelected(event) == selected) return true
  action.setSelected(event, selected)
  return false
}

internal fun configureBookValueBase(root: Component, base: String): Boolean {
  val (action, event) = bookValueBaseAction(root) ?: return false
  return configureBookValueBase(action, event, base)
}

/** A post-photograph observation rejects drift without changing the action or simulator state. */
internal fun observeBookValueBase(root: Component, base: String): Map<String, Any> {
  val (action, event) =
      checkNotNull(bookValueBaseAction(root)) { "Native MARS value display toggle disappeared" }
  val selected = action.isSelected(event)
  check(selected == (base == "hex")) { "Native MARS value display base changed" }
  return mapOf(
      "actionText" to BOOK_HEX_VALUES_ACTION,
      "valueBase" to base,
      "hexadecimalSelected" to selected,
  )
}
