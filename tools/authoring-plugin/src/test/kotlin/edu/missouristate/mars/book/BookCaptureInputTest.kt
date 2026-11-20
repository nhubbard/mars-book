package edu.missouristate.mars.book

import com.intellij.execution.process.ProcessHandler
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookCaptureInputTest {
    private class Process(private val stream: OutputStream?) : ProcessHandler() {
        override fun destroyProcessImpl() = notifyProcessTerminated(143)
        override fun detachProcessImpl() = notifyProcessDetached()
        override fun detachIsDefault() = false
        override fun getProcessInput() = stream
    }

    @Test
    fun `delivers exact input once per request without closing the process stream`() {
        var flushed = false
        var closed = false
        val bytes = object : ByteArrayOutputStream() {
            override fun flush() { flushed = true }
            override fun close() { closed = true }
        }
        val process = Process(bytes).apply { startNotify() }
        deliverBookStandardInput(process, "20\n")
        assertEquals("20\n", bytes.toString(Charsets.US_ASCII))
        assertEquals(true, flushed)
        assertFalse(closed)
    }

    @Test
    fun `rejects malformed input and processes without a running input stream`() {
        val process = Process(ByteArrayOutputStream())
        assertThrows<IllegalStateException> { deliverBookStandardInput(process, "20\n") }
        process.startNotify()
        for (input in listOf("", "20", "é\n", "\r\n", "\u0000\n", "1".repeat(1024) + "\n")) {
            assertThrows<IllegalArgumentException> { deliverBookStandardInput(process, input) }
        }
        assertThrows<IllegalStateException> { deliverBookStandardInput(Process(null).apply { startNotify() }, "20\n") }
        process.destroyProcess()
        assertThrows<IllegalStateException> { deliverBookStandardInput(process, "20\n") }
    }

    @Test
    fun `propagates broken input rather than reporting a supplied fixture`() {
        val broken = object : OutputStream() {
            override fun write(value: Int) { throw IOException("closed fixture pipe") }
        }
        assertThrows<IOException> { deliverBookStandardInput(Process(broken).apply { startNotify() }, "20\n") }
    }

    @Test
    fun `validates each distinct step boundary and resolves the named instruction label`() {
        val figure = BookCaptureFigure("console-after-one", "main.asm", "a".repeat(64), "report",
            listOf(BookExpectedRow("Answer", listOf("t0", "29"))), standardInput = "20\n", stepFrom = listOf("calculate", "after_addu"))
        figure.validate(Path.of("/tmp/fixture"))
        assertEquals(1, figure.checkpointLine(".text\ncalculate:\n addu\nafter_addu:\n addiu\nreport:\n", "calculate"))
        for (steps in listOf(listOf("report"), listOf("calculate", "calculate"), listOf("bad label"), (0..32).map { "step$it" })) {
            assertThrows<IllegalArgumentException> { figure.copy(stepFrom = steps).validate(Path.of("/tmp/fixture")) }
        }
        assertThrows<IllegalArgumentException> { figure.copy(standardInput = "20").validate(Path.of("/tmp/fixture")) }
    }
}
