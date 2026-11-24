package edu.missouristate.mars.book

import com.intellij.openapi.components.service
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import edu.missouristate.mars.api.SessionSyscallEnvironment
import edu.missouristate.mars.plugin.MarsSessionService
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir

@TestApplication
class BookFileFixtureTest {
  private val projectFixture = projectFixture(openAfterCreation = true)
  private val project
    get() = projectFixture.get()

  private val fixture =
      BookFileFixture(
          "fixture.bin",
          "313233343536373839",
          3,
          4,
          expectedOperations =
              listOf(
                  BookFileOperation("OPEN", 3, 0, 3),
                  BookFileOperation("READ", 3, 7, 3),
                  BookFileOperation("READ", 3, 7, 1),
                  BookFileOperation("READ", 3, 7, -1),
                  BookFileOperation("CLOSE", 3, 0, 0),
              ),
      )

  private inline fun <T> withDefault(
      action: (SessionSyscallEnvironment, ByteArrayOutputStream) -> T,
  ): T {
    val bytes = ByteArrayOutputStream()
    return PrintStream(bytes).use { output ->
      SessionSyscallEnvironment(
              project.service<MarsSessionService>().context.memory,
              ByteArrayInputStream("123\n".toByteArray()),
              output,
              output,
          )
          .use { io -> action(io, bytes) }
    }
  }

  @Test
  fun `four-byte failure preserves unused buffer bytes and requires guest close`(): Unit =
      withDefault { io, _ ->
        BookFixtureFileEnvironment(io, fixture).use { env ->
          val buffer = ByteArray(9) { 0x55 }
          assertEquals(3, env.openFile("fixture.bin", 0))
          assertEquals(3, env.readFromFile(3, buffer, 7))
          assertArrayEquals(byteArrayOf(49, 50, 51, 85, 85, 85, 85, 85, 85), buffer)
          assertEquals(1, env.readFromFile(3, buffer, 7))
          assertArrayEquals(byteArrayOf(52, 50, 51, 85, 85, 85, 85, 85, 85), buffer)
          assertEquals(-1, env.readFromFile(3, buffer, 7))
          assertThrows<IllegalStateException> { env.checkedSnapshot() }
          env.closeFile(3)
          val evidence = env.checkedSnapshot()
          assertEquals(fixture.expectedOperations, evidence.operations)
          assertTrue(evidence.openDescriptors.isEmpty())
          assertTrue(evidence.cleanupDescriptors.isEmpty())
          assertFalse(evidence.disposed)
          assertEquals(captureSha256("123456789".toByteArray()), evidence.contentsSha256)
        }
      }

  @Test
  fun `lifetime cleanup cannot masquerade as a guest close`(): Unit = withDefault { io, _ ->
    val env = BookFixtureFileEnvironment(io, fixture)
    env.openFile("fixture.bin", 0)
    env.close()
    env.close()
    assertTrue(env.snapshot().disposed)
    assertEquals(listOf(3), env.snapshot().cleanupDescriptors)
    assertEquals(listOf(BookFileOperation("OPEN", 3, 0, 3)), env.snapshot().operations)
    assertThrows<IllegalStateException> { env.checkedSnapshot() }
    assertThrows<IllegalStateException> { env.openFile("fixture.bin", 0) }
    assertThrows<IllegalStateException> { env.readFromFile(3, ByteArray(1), 1) }
    assertThrows<IllegalStateException> { env.writeToFile(3, ByteArray(1), 1) }
    assertThrows<IllegalStateException> { env.closeFile(3) }
    io.printString("default remains usable")
  }

  @Test
  fun `ordinary short reads continue to EOF and zero reads do not advance`(): Unit =
      withDefault { io, _ ->
        BookFixtureFileEnvironment(io, fixture.copy(failAfter = null)).use { env ->
          val buffer = ByteArray(10)
          env.openFile("fixture.bin", 0)
          assertEquals(0, env.readFromFile(3, buffer, 0))
          repeat(3) { index ->
            assertEquals(3, env.readFromFile(3, buffer, 10))
            assertEquals(49 + index * 3, buffer[0].toInt())
          }
          assertEquals(0, env.readFromFile(3, buffer, 10))
          env.closeFile(3)
          assertEquals(-1, env.readFromFile(3, buffer, 1))
          assertEquals(3, env.openFile("fixture.bin", 0))
          assertEquals(1, env.readFromFile(3, buffer, 1))
          assertEquals(49, buffer[0].toInt())
          env.closeFile(3)
        }
        BookFixtureFileEnvironment(io, fixture.copy(contentsHex = "", failAfter = null)).use { env
          ->
          env.openFile("fixture.bin", 0)
          assertEquals(0, env.readFromFile(3, ByteArray(1), 1))
          env.closeFile(3)
        }
      }

  @Test
  fun `missing paths and unexpected operations cannot reach the host filesystem`(): Unit =
      withDefault { io, _ ->
        val missing =
            fixture.copy(
                missing = true,
                failAfter = null,
                expectedOperations = listOf(BookFileOperation("OPEN", -1, 0, -1)),
            )
        BookFixtureFileEnvironment(io, missing).use { env ->
          assertEquals(-1, env.openFile("fixture.bin", 0))
          assertEquals(missing.expectedOperations, env.checkedSnapshot().operations)
        }
        BookFixtureFileEnvironment(io, fixture).use { env ->
          assertEquals(-1, env.openFile("another.bin", 0))
          assertEquals(-1, env.openFile(null, 0))
          assertEquals(-1, env.openFile("fixture.bin", 1))
          assertEquals(-1, env.openFile("fixture.bin", 9))
          assertEquals(-1, env.readFromFile(30, ByteArray(1), 1))
          assertEquals(-1, env.writeToFile(3, byteArrayOf(1), 1))
          env.closeFile(30)
          assertEquals(BookFileOperation("CLOSE", 30, 0, -1), env.snapshot().operations.last())
          assertEquals(3, env.openFile("fixture.bin", 0))
          assertEquals(-1, env.readFromFile(3, null, 1))
          assertEquals(-1, env.readFromFile(3, ByteArray(1), 2))
          assertEquals(-1, env.readFromFile(3, ByteArray(1), -1))
          assertEquals(1, env.readFromFile(3, ByteArray(1), 1))
          env.closeFile(3)
        }
      }

  @Test
  fun `descriptors have independent cursors and a bounded reusable table`(): Unit =
      withDefault { io, _ ->
        BookFixtureFileEnvironment(io, fixture).use { env ->
          for (descriptor in 3..31) assertEquals(descriptor, env.openFile("fixture.bin", 0))
          assertEquals(-1, env.openFile("fixture.bin", 0))
          val a = ByteArray(2)
          val b = ByteArray(2)
          assertEquals(2, env.readFromFile(3, a, 2))
          assertEquals(2, env.readFromFile(4, b, 2))
          assertArrayEquals(a, b)
          env.closeFile(3)
          assertEquals(3, env.openFile("fixture.bin", 0))
          for (descriptor in 3..31) env.closeFile(descriptor)
          assertTrue(env.snapshot().openDescriptors.isEmpty())
        }
      }

  @Test
  fun `console operations delegate and separate environments never share cursors`(): Unit =
      withDefault { io, output ->
        BookFixtureFileEnvironment(io, fixture).use { first ->
          BookFixtureFileEnvironment(io, fixture).use { second ->
            first.printInt(42)
            assertEquals(1, first.writeToFile(1, byteArrayOf(124), 1))
            assertEquals(1, first.writeToFile(2, byteArrayOf(33), 1))
            val input = ByteArray(1)
            assertEquals(1, first.readFromFile(0, input, 1))
            assertEquals(49, input[0].toInt())
            assertEquals("42|!", output.toString())
            first.closeFile(0)
            first.closeFile(1)
            first.closeFile(2)
            first.openFile("fixture.bin", 0)
            second.openFile("fixture.bin", 0)
            assertEquals(3, first.readFromFile(3, ByteArray(7), 7))
            assertEquals(1, second.readFromFile(3, input, 1))
            assertEquals(49, input[0].toInt())
            first.closeFile(3)
            second.closeFile(3)
          }
        }
      }

  @Test
  fun `rejects invalid or unobserved fixture declarations`() {
    val invalid =
        listOf(
            fixture.copy(guestPath = ""),
            fixture.copy(guestPath = "a\u0000b"),
            fixture.copy(contentsHex = "1"),
            fixture.copy(contentsHex = "gg"),
            fixture.copy(maximumRead = 0),
            fixture.copy(maximumRead = 1_048_577),
            fixture.copy(failAfter = -1),
            fixture.copy(failAfter = 10),
            fixture.copy(missing = true),
            fixture.copy(expectedOperations = emptyList()),
            fixture.copy(expectedOperations = listOf(BookFileOperation("RENAME"))),
            fixture.copy(expectedOperations = listOf(BookFileOperation("READ", requested = -1))),
        )
    invalid.forEach { assertThrows<IllegalArgumentException> { it.validate() } }
    fixture.validate()
    val figure =
        BookCaptureFigure(
            "failure",
            "main.asm",
            "a".repeat(64),
            "ready",
            listOf(BookExpectedRow("Status", listOf("v1", "2"))),
            fileFixture = fixture,
        )
    figure.validate(Path.of("/tmp/fixture"))
    assertThrows<IllegalArgumentException> {
      figure.copy(mode = "run", checkpoint = "").validate(Path.of("/tmp/fixture"))
    }
  }

  @Test
  fun `only registered exact sources are customized and native lifetime owns the environment`(
      @TempDir directory: Path,
  ): Unit = withDefault { io, _ ->
    val entry = Files.writeString(directory.resolve("main.asm"), "nop\n")
    val other = directory.resolve("other.asm")
    val lifetime = Disposer.newDisposable()
    try {
      val registry = project.service<BookFileFixtureRegistry>()
      val customizer = BookFileFixtureCustomizer()
      assertSame(io, customizer.customize(project, entry, io, lifetime))
      registry.prepare(entry, captureSha256(Files.readAllBytes(entry)), fixture).use { binding ->
        assertThrows<IllegalStateException> { registry.prepare(entry, "a".repeat(64), fixture) }
        assertSame(io, customizer.customize(project, other, io, lifetime))
        assertThrows<IllegalStateException> { binding.snapshot() }
        val env = customizer.customize(project, entry, io, lifetime)
        assertNotSame(io, env)
        assertThrows<IllegalStateException> { customizer.customize(project, entry, io, lifetime) }
        env.openFile("fixture.bin", 0)
        repeat(3) { env.readFromFile(3, ByteArray(7), 7) }
        env.closeFile(3)
        assertFalse(registry.snapshot(entry).disposed)
        Disposer.dispose(lifetime)
        assertTrue(binding.snapshot().disposed)
      }
      val anotherLifetime = Disposer.newDisposable()
      try {
        assertSame(io, customizer.customize(project, entry, io, anotherLifetime))
      } finally {
        Disposer.dispose(anotherLifetime)
      }
    } finally {
      Disposer.dispose(lifetime)
    }
  }

  @Test
  fun `two registered launches retain independent cursors and disposal`(
      @TempDir directory: Path,
  ): Unit = withDefault { io, _ ->
    val first = Files.writeString(directory.resolve("first.asm"), "nop\n")
    val second = Files.writeString(directory.resolve("second.asm"), "nop\n")
    val registry = BookFileFixtureRegistry()
    val lifetimeA = Disposer.newDisposable()
    val lifetimeB = Disposer.newDisposable()
    try {
      registry.prepare(first, captureSha256(Files.readAllBytes(first)), fixture).use {
        registry.prepare(second, captureSha256(Files.readAllBytes(second)), fixture).use {
          val a = registry.customize(first, io, lifetimeA)
          val b = registry.customize(second, io, lifetimeB)
          a.openFile("fixture.bin", 0)
          b.openFile("fixture.bin", 0)
          assertEquals(3, a.readFromFile(3, ByteArray(7), 7))
          val buffer = ByteArray(1)
          assertEquals(1, b.readFromFile(3, buffer, 1))
          assertEquals(49, buffer[0].toInt())
          Disposer.dispose(lifetimeA)
          assertEquals(1, b.readFromFile(3, buffer, 1))
          assertEquals(50, buffer[0].toInt())
          b.closeFile(3)
          Disposer.dispose(lifetimeB)
        }
      }
    } finally {
      Disposer.dispose(lifetimeA)
      Disposer.dispose(lifetimeB)
    }
  }

  @Test
  fun `edited source is refused before any custom environment is installed`(
      @TempDir directory: Path,
  ): Unit = withDefault { io, _ ->
    val entry = Files.writeString(directory.resolve("main.asm"), "nop\n")
    val lifetime = Disposer.newDisposable()
    try {
      val registry = BookFileFixtureRegistry()
      registry.prepare(entry, captureSha256(Files.readAllBytes(entry)), fixture).use {
        Files.writeString(entry, "break\n")
        assertThrows<IllegalStateException> { registry.customize(entry, io, lifetime) }
      }
    } finally {
      Disposer.dispose(lifetime)
    }
  }
}
