package edu.missouristate.mars.book

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiFile
import java.awt.Color
import java.awt.Font
import java.security.MessageDigest

/** Captures language colors after IntelliJ has completed analysis of the editor document. */
class BookHighlightExporter {
  data class Appearance(
      val foreground: String,
      val background: String,
      val bold: Boolean,
      val italic: Boolean,
  )

  data class Run(val start: Int, val end: Int, val keys: List<String>, val appearance: Appearance)

  data class Export(val sha256: String, val source: String, val scheme: String, val runs: List<Run>)

  private data class Span(
      val start: Int,
      val end: Int,
      val keys: List<String>,
      val attributes: TextAttributes,
  )

  /**
   * Uses the named light scheme without changing application or editor settings. Offsets are
   * Unicode code points.
   */
  fun capture(file: PsiFile, editor: Editor): Export {
    ApplicationManager.getApplication().assertReadAccessAllowed()
    require(file.language.id == "MARS_MIPS") { "Expected a MARS MIPS source file" }
    val scheme =
        checkNotNull(
            EditorColorsManager.getInstance().getScheme(EditorColorsManager.getDefaultSchemeName()),
        )
    require(scheme.defaultBackground == Color.WHITE) {
      "The publication requires the default light scheme"
    }
    val text = editor.document.text
    require(file.text == text) { "Commit the editor document before exporting" }
    val lexer =
        EditorHighlighterFactory.getInstance()
            .createEditorHighlighter(file.fileType, scheme, file.project)
    lexer.setText(text)
    val spans = mutableListOf<Span>()
    val iterator = lexer.createIterator(0)
    while (!iterator.atEnd()) {
      spans +=
          Span(
              iterator.start,
              iterator.end,
              iterator.textAttributesKeys.map { it.externalName },
              iterator.textAttributes,
          )
      iterator.advance()
    }
    val models =
        listOfNotNull(
                editor.markupModel,
                DocumentMarkupModel.forDocument(editor.document, file.project, false),
            )
            .distinct()
    val semantic =
        models
            .flatMap { it.allHighlighters.toList() }
            .distinct()
            .filter {
              it.isValid &&
                  it.layer == HighlighterLayer.ADDITIONAL_SYNTAX &&
                  it.startOffset < it.endOffset
            }
            .mapNotNull {
              val key = it.textAttributesKey ?: return@mapNotNull null
              val attributes = it.getTextAttributes(scheme) ?: return@mapNotNull null
              Span(it.startOffset, it.endOffset, listOf(key.externalName), attributes)
            }
            .sortedWith(compareBy({ it.start }, { it.end }, { it.keys.joinToString() }))
    val boundaries =
        (listOf(0, text.length) + (spans + semantic).flatMap { listOf(it.start, it.end) })
            .distinct()
            .sorted()
    val runs =
        boundaries.zipWithNext().map { (start, end) ->
          val base = spans.firstOrNull { it.start <= start && end <= it.end }
          var attributes = base?.attributes ?: TextAttributes()
          val keys = base?.keys.orEmpty().toMutableList()
          for (overlay in semantic.filter { it.start <= start && end <= it.end }) {
            attributes = TextAttributes.merge(attributes, overlay.attributes)
            keys += overlay.keys
          }
          Run(
              text.codePointCount(0, start),
              text.codePointCount(0, end),
              keys.distinct(),
              appearance(attributes, scheme),
          )
        }
    return Export(sha256(text), text, scheme.name, runs)
  }

  private fun appearance(attributes: TextAttributes, scheme: EditorColorsScheme) =
      Appearance(
          hex(attributes.foregroundColor ?: scheme.defaultForeground),
          hex(attributes.backgroundColor ?: scheme.defaultBackground),
          attributes.fontType and Font.BOLD != 0,
          attributes.fontType and Font.ITALIC != 0,
      )

  private fun hex(color: Color) = "%06x".format(color.rgb and 0xffffff)

  private fun sha256(text: String) =
      MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8)).joinToString(
          "",
      ) {
        "%02x".format(it)
      }
}
