package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@TestApplication
class BookHighlightExporterTest {
  private val projectFixture = codeInsightProjectFixture()
  private val fixture by codeInsightFixture(projectFixture)

  @Test
  fun `capture analysis ignores unrelated in-memory editors`() {
    assertFalse(captureFileMatches(null, Path.of("/tmp/kernel.asm")))
    assertFalse(
        captureFileMatches(
            LightVirtualFile("AirPrompt.md", "# In-memory document"),
            Path.of("/tmp/kernel.asm"),
        ),
    )
  }

  @Test
  fun `capture analysis matches only its native fixture file`() = timeoutRunBlocking {
    val path = Files.createTempFile("book-capture-file-", ".asm")
    try {
      withContext(Dispatchers.EDT) {
        val file = checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path))
        readAction {
          assertTrue(captureFileMatches(file, path))
          assertFalse(captureFileMatches(file, path.resolveSibling("different.asm")))
        }
      }
    } finally {
      Files.delete(path)
    }
  }

  @Test
  fun `captures actual lexer and semantic colors without changing the active scheme`() =
      timeoutRunBlocking {
        val source =
            $$"""
            .eqv COUNT 3
            .data
            message: .asciiz "Hello # world"
            .text
            start: li $t0, COUNT  # comment 😀
            addiu $t0, $t0, -1
            bnez $t0, start
            """
                .trimIndent() + "\n"
        val manager = EditorColorsManager.getInstance()
        val original = manager.globalScheme
        val export =
            withContext(Dispatchers.EDT) {
              val file = fixture.configureByText("colors.asm", source)
              fixture.doHighlighting()
              readAction { BookHighlightExporter().capture(file, fixture.editor) }
            }
        assertEquals(original, manager.globalScheme)
        assertEquals(source, export.source)
        assertEquals(source.codePointCount(0, source.length), export.runs.last().end)
        assertEquals(0, export.runs.first().start)
        assertTrue(export.runs.zipWithNext().all { (a, b) -> a.end == b.start })
        for (key in
            listOf(
                "MARS_MIPS_INSTRUCTION",
                "MARS_MIPS_LABEL",
                "MARS_MIPS_REGISTER",
                "MARS_MIPS_STRING",
                "MARS_MIPS_COMMENT",
                "MARS_MIPS_EQUIVALENCE",
            )) {
          assertTrue(export.runs.any { key in it.keys }, "Missing actual highlighter key $key")
        }
        assertTrue(export.runs.all { it.appearance.background == "ffffff" })
      }

  @Test
  fun `exports an empty assembly file and rejects another language`(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      val empty = fixture.configureByText("empty.asm", "")
      fixture.doHighlighting()
      val export = readAction { BookHighlightExporter().capture(empty, fixture.editor) }
      assertEquals("", export.source)
      assertTrue(export.runs.isEmpty())
      val other = fixture.configureByText("other.txt", "plain text")
      assertThrows<IllegalArgumentException> {
        BookHighlightExporter().capture(other, fixture.editor)
      }
    }
  }

  @Test
  // The full publication corpus highlights hundreds of distinct editor files.
  fun `exports requested publication snippets using the installed MARS plugin`() =
      timeoutRunBlocking(timeout = 60.seconds) {
        val input = System.getProperty("highlightInput") ?: return@timeoutRunBlocking
        val output = Path.of(checkNotNull(System.getProperty("highlightOutput")))
        val exports = mutableListOf<BookHighlightExporter.Export>()
        Files.list(Path.of(input)).use { paths ->
          for (path in paths.filter { it.toString().endsWith(".asm") }.sorted().toList()) {
            val source = Files.readString(path)
            val export =
                withContext(Dispatchers.EDT) {
                  val file = fixture.configureByText(path.fileName.toString(), source)
                  fixture.doHighlighting()
                  readAction { BookHighlightExporter().capture(file, fixture.editor) }
                }
            assertEquals(source, export.source)
            exports += export
          }
        }
        Files.createDirectories(output.parent)
        val archive =
            Files.readAllBytes(Path.of(checkNotNull(System.getProperty("marsPluginArchive"))))
        val sha =
            MessageDigest.getInstance("SHA-256").digest(archive).joinToString("") {
              "%02x".format(it)
            }
        val manifest =
            mapOf(
                "formatVersion" to 1,
                "platformBuild" to ApplicationInfo.getInstance().build.asString(),
                "marsPluginVersion" to
                    checkNotNull(
                            PluginManagerCore.getPlugin(PluginId.getId("edu.missouristate.mars")),
                        )
                        .version,
                "marsPluginArchiveSha256" to sha,
                "exports" to exports,
            )
        Files.writeString(
            output,
            GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(manifest),
        )
        assertTrue(exports.isNotEmpty())
      }
}
