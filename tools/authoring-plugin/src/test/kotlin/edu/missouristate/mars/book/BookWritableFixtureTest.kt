package edu.missouristate.mars.book

import com.intellij.openapi.components.service
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import edu.missouristate.mars.api.SessionSyscallEnvironment
import edu.missouristate.mars.plugin.MarsSessionService
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@TestApplication
class BookWritableFixtureTest {
  private val project = projectFixture(openAfterCreation = true)
  private val policy =
      BookFileFixture(
          guestPath = "output.qoi",
          contentsHex = "3132333435",
          writable = true,
          maximumWrite = 3,
          expectedContentsHex = "",
          expectedOperations = listOf(BookFileOperation("OPEN", 3, 0, 3)),
      )

  private inline fun exercise(action: (SessionSyscallEnvironment) -> Unit) {
    PrintStream(ByteArrayOutputStream()).use { output ->
      SessionSyscallEnvironment(
              project.get().service<MarsSessionService>().context.memory,
              ByteArrayInputStream(byteArrayOf()),
              output,
              output,
          )
          .use(action)
    }
  }

  @Test
  fun `three accepted bytes and an explicitly live descriptor survive disposal as distinct evidence`() =
      exercise { io ->
        val expected =
            listOf(BookFileOperation("OPEN", 19, 0, 19), BookFileOperation("WRITE", 19, 5, 3))
        val env =
            BookFixtureFileEnvironment(
                io,
                policy.copy(
                    firstDescriptor = 19,
                    expectedContentsHex = "010203",
                    expectedOpenDescriptors = listOf(19),
                    expectedOperations = expected,
                ),
            )
        assertEquals(19, env.openFile("output.qoi", 1))
        val input = byteArrayOf(1, 2, 3, 4, 5)
        assertEquals(3, env.writeToFile(19, input, input.size))
        assertArrayEquals(byteArrayOf(1, 2, 3, 4, 5), input)
        val live = env.checkedSnapshot()
        assertEquals("010203", live.writtenContentsHex)
        assertEquals(captureSha256("12345".toByteArray()), live.contentsSha256)
        assertEquals(listOf(19), live.openDescriptors)
        assertTrue(live.cleanupDescriptors.isEmpty())
        env.close()
        env.close()
        val stopped = env.checkedSnapshot()
        assertTrue(stopped.disposed)
        assertTrue(stopped.openDescriptors.isEmpty())
        assertEquals(listOf(19), stopped.cleanupDescriptors)
        assertEquals(expected, stopped.operations)
        assertEquals("010203", stopped.writtenContentsHex)
      }

  @Test
  fun `partial accepted prefix stops at the injected failure and guest close remains explicit`() =
      exercise { io ->
        val expected =
            listOf(
                BookFileOperation("OPEN", 3, 0, 3),
                BookFileOperation("WRITE", 3, 5, 3),
                BookFileOperation("WRITE", 3, 2, 1),
                BookFileOperation("WRITE", 3, 1, -1),
                BookFileOperation("WRITE", 3, 0, 0),
                BookFileOperation("CLOSE", 3, 0, 0),
            )
        BookFixtureFileEnvironment(
                io,
                policy.copy(
                    failWriteAfter = 4,
                    expectedContentsHex = "31323334",
                    expectedOperations = expected,
                ),
            )
            .use { env ->
              env.openFile("output.qoi", 1)
              assertEquals(3, env.writeToFile(3, "12345".toByteArray(), 5))
              assertEquals(1, env.writeToFile(3, "45".toByteArray(), 2))
              assertEquals(-1, env.writeToFile(3, "5".toByteArray(), 1))
              assertEquals(0, env.writeToFile(3, byteArrayOf(), 0))
              env.closeFile(3)
              assertEquals("31323334", env.checkedSnapshot().writtenContentsHex)
              assertTrue(env.checkedSnapshot().cleanupDescriptors.isEmpty())
            }
        BookFixtureFileEnvironment(io, policy.copy(failWriteAfter = 0)).use { env ->
          env.openFile("output.qoi", 1)
          assertEquals(-1, env.writeToFile(3, byteArrayOf(1), 1))
          assertEquals("", env.snapshot().writtenContentsHex)
        }
      }

  @Test
  fun `invalid modes handles and buffers cannot mutate the virtual output`() = exercise { io ->
    BookFixtureFileEnvironment(io, policy).use { env ->
      assertEquals(-1, env.openFile("output.qoi", 9))
      assertEquals(-1, env.openFile("other.qoi", 1))
      assertEquals(3, env.openFile("output.qoi", 0))
      assertEquals(-1, env.writeToFile(3, byteArrayOf(1), 1))
      assertEquals(-1, env.writeToFile(30, byteArrayOf(1), 1))
      assertEquals(4, env.openFile("output.qoi", 1))
      assertEquals(-1, env.readFromFile(4, ByteArray(1), 1))
      assertEquals(-1, env.writeToFile(4, null, 1))
      assertEquals(-1, env.writeToFile(4, ByteArray(1), 2))
      assertEquals(-1, env.writeToFile(4, ByteArray(1), -1))
      assertEquals(0, env.writeToFile(4, byteArrayOf(), 0))
      assertEquals("", env.snapshot().writtenContentsHex)
      env.closeFile(4)
      assertEquals(-1, env.writeToFile(4, byteArrayOf(1), 1))
      env.closeFile(3)
    }
  }

  @Test
  fun `output has a finite maximum extent`() = exercise { io ->
    BookFixtureFileEnvironment(io, policy.copy(maximumWrite = 1_048_576)).use { env ->
      env.openFile("output.qoi", 1)
      val input = ByteArray(1_048_576) { 42 }
      assertEquals(input.size, env.writeToFile(3, input, input.size))
      assertEquals(-1, env.writeToFile(3, byteArrayOf(1), 1))
      assertEquals(0, env.writeToFile(3, byteArrayOf(), 0))
      assertEquals("2a".repeat(input.size), env.snapshot().writtenContentsHex)
      env.closeFile(3)
    }
  }

  @Test
  fun `truncation preserves read cursor EOF and launches never share file bytes`() =
      exercise { io ->
        BookFixtureFileEnvironment(io, policy).use { a ->
          BookFixtureFileEnvironment(io, policy).use { b ->
            a.openFile("output.qoi", 0)
            assertEquals(3, a.readFromFile(3, ByteArray(3), 3))
            a.openFile("output.qoi", 1)
            assertEquals(0, a.readFromFile(3, ByteArray(3), 3))
            assertEquals(2, a.writeToFile(4, "AB".toByteArray(), 2))
            b.openFile("output.qoi", 0)
            val original = ByteArray(3)
            assertEquals(3, b.readFromFile(3, original, 3))
            assertArrayEquals("123".toByteArray(), original)
            assertEquals("4142", a.snapshot().writtenContentsHex)
            a.closeFile(4)
            assertEquals(4, a.openFile("output.qoi", 1))
            assertEquals("", a.snapshot().writtenContentsHex)
            a.closeFile(4)
            a.closeFile(3)
            b.closeFile(3)
          }
        }
      }

  @Test
  fun `checkpoint byte and ownership contracts cannot be bypassed by disposal`() = exercise { io ->
    BookFixtureFileEnvironment(
            io,
            policy.copy(expectedContentsHex = "01", expectedOpenDescriptors = listOf(3)),
        )
        .use { env ->
          env.openFile("output.qoi", 1)
          assertThrows<IllegalStateException> { env.checkedSnapshot() }
        }
    BookFixtureFileEnvironment(io, policy).use { env ->
      env.openFile("output.qoi", 1)
      assertThrows<IllegalStateException> { env.checkedSnapshot() }
      env.close()
      assertThrows<IllegalStateException> { env.checkedSnapshot() }
    }
  }

  @Test
  fun `invalid output declarations are rejected before installation`() {
    val invalid =
        listOf(
            policy.copy(maximumWrite = 0),
            policy.copy(maximumWrite = 1_048_577),
            policy.copy(firstDescriptor = 2),
            policy.copy(firstDescriptor = 32),
            policy.copy(failWriteAfter = -1),
            policy.copy(failWriteAfter = 1_048_577),
            policy.copy(writable = false, failWriteAfter = 1),
            policy.copy(missing = true),
            policy.copy(expectedContentsHex = null),
            policy.copy(expectedContentsHex = "a"),
            policy.copy(expectedContentsHex = "gg"),
            policy.copy(expectedContentsHex = "00".repeat(1_048_577)),
            policy.copy(writable = false),
            policy.copy(expectedOpenDescriptors = listOf(3, 3)),
            policy.copy(expectedOpenDescriptors = listOf(4, 3)),
            policy.copy(expectedOpenDescriptors = listOf(2)),
            policy.copy(firstDescriptor = 19, expectedOpenDescriptors = listOf(3)),
            policy.copy(expectedOpenDescriptors = listOf(32)),
        )
    invalid.forEach { assertThrows<IllegalArgumentException> { it.validate() } }
    policy.validate()
  }
}
