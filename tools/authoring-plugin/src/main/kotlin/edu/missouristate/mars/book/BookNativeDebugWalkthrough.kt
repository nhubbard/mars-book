package edu.missouristate.mars.book

import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import training.learn.lesson.LessonManager

/** Capture the student Debug experience through native actions and rendered registers only. */
internal suspend fun captureBookNativeDebug(
    project: Project, entry: Path,
    awaitText: suspend (String) -> Unit, capture: suspend (String, String) -> Unit,
    focus: suspend (String, String) -> Unit, nextActivity: String? = null,
): List<Map<String, Any>> {
    awaitText("Stop any earlier Debug session.")
    val file = bookModalUi { checkNotNull(LocalFileSystem.getInstance().findFileByNioFile(entry)) }
    val lines = withContext(Dispatchers.IO) { Files.readString(entry).lines() }
    val mask = lines.indexOfFirst { it.trim().startsWith("andi ") }
    val extension = lines.indexOfFirst { it.trim().startsWith("seb ") }
    val report = lines.indexOfFirst { it.trim() == "report:" } + 1
    check(mask >= 0 && extension > mask && report > extension)
    val type = XBreakpointType.EXTENSION_POINT_NAME.extensionList.filterIsInstance<XLineBreakpointType<*>>()
        .single { it.id == "MarsMipsLine" }
    val receipts = mutableListOf<Map<String, Any>>()
    for (wrongBoundary in listOf(true, false)) {
        val breakpoint = writeAction {
            XDebuggerManager.getInstance(project).breakpointManager.addLineBreakpoint(type, file.url,
                if (wrongBoundary) extension else mask, null)
        }
        val launch = launchBookProgram(project, entry, debug = true)
        try {
            val handler = launch.started.await()
            while (!launch.stdout.toString().contains("Enter an integer from 0 to 255: ")) delay(50)
            withContext(Dispatchers.IO) { deliverBookStandardInput(handler, "150\n") }
            val session = checkNotNull(launch.session)
            val first = launch.pauses.receive()
            check(first == if (wrongBoundary) extension else mask)
            if (wrongBoundary) {
                val rows = awaitBookDebugRegisters(project, handler, mapOf("\$v0" to 150L, "\$t0" to 150L))
                capture("debug-wrong-boundary", "This pause is not before the requested source operation.")
                receipts += mapOf("id" to "wrong-boundary", "line" to first + 1, "input" to 150,
                    "renderedRegisters" to rows, "accepted" to false)
                withContext(Dispatchers.EDT) { session.stop() }
                launch.exit.await()
            } else {
                val before = awaitBookDebugRegisters(project, handler, mapOf("\$v0" to 150L, "\$t0" to 0L))
                awaitText("Use Step Over once.")
                focus("debug-next-mask", "seb")
                capture("debug-before-mask", "At this boundary v0 contains the input.")
                receipts += mapOf("id" to "before-mask", "line" to first + 1, "input" to 150, "renderedRegisters" to before)
                withContext(Dispatchers.EDT) { session.stepOver(false) }
                check(launch.pauses.receive() == extension)
                val masked = awaitBookDebugRegisters(project, handler, mapOf("\$v0" to 150L, "\$t0" to 150L))
                awaitText("Use Step Over once more.")
                focus("debug-next-extension", "la")
                capture("debug-after-mask", "The mask retained the low eight bits")
                receipts += mapOf("id" to "after-mask", "line" to extension + 1, "renderedRegisters" to masked)
                withContext(Dispatchers.EDT) { session.stepOver(false) }
                check(launch.pauses.receive() == report)
                val extended = awaitBookDebugRegisters(project, handler, mapOf("\$t0" to 150L, "\$t1" to -106L))
                val next = nextActivity ?: "The five checked runs and the ordered Debug observations"
                awaitText(next)
                if (nextActivity == null) while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
                capture("debug-complete", next)
                receipts += mapOf("id" to "after-extension", "line" to report + 1, "renderedRegisters" to extended,
                    "completedInOneSession" to true)
                withContext(Dispatchers.EDT) { session.resume() }
                check(launch.exit.await() == 0)
                check(launch.stdout.toString() == "Enter an integer from 0 to 255: Unsigned byte: 150\nSigned byte: -106\n")
            }
        } finally {
            withContext(Dispatchers.EDT) { launch.session?.stop() }
            launch.close()
            writeAction { XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(breakpoint) }
        }
    }
    return receipts
}
