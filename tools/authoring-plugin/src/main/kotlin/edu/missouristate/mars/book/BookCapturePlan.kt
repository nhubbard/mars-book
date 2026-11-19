package edu.missouristate.mars.book

import com.intellij.openapi.vfs.VirtualFile
import java.awt.Rectangle
import java.nio.file.Path
import java.security.MessageDigest

/** Local authoring input. Completed fixture sources and expected values stay outside the public book. */
data class BookCapturePlan(
    val projectRoot: String = "",
    val outputDirectory: String = "",
    val figures: List<BookCaptureFigure> = emptyList(),
    val packageId: String = "",
    val packageVersion: String = "",
    val publicCommit: String = "",
    val upstreamCommit: String = "",
    val upstreamDirtyFiles: Map<String, String> = emptyMap(),
) {
    fun validate() {
        require(Path.of(projectRoot).isAbsolute && projectRoot.isNotBlank()) { "An absolute fixture project is required" }
        require(Path.of(outputDirectory).isAbsolute && outputDirectory.isNotBlank()) { "An absolute output directory is required" }
        require(packageId.matches(Regex("[0-9]{3}-[a-z0-9-]+")) && packageVersion.matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+"))) { "Package identity is required" }
        require(listOf(publicCommit, upstreamCommit).all { it.matches(Regex("[a-f0-9]{40}")) }) { "Source revisions are required" }
        require(upstreamDirtyFiles.values.all { it.matches(Regex("[a-f0-9]{64}")) }) { "Invalid dirty-source digest" }
        require(figures.isNotEmpty()) { "No capture figures" }
        require(figures.map { it.id }.distinct().size == figures.size) { "Duplicate figure IDs" }
        figures.forEach { it.validate(Path.of(projectRoot)) }
    }
}

data class BookCaptureFigure(
    val id: String = "",
    val source: String = "",
    val sourceSha256: String = "",
    val checkpoint: String = "",
    val expectedRows: List<BookExpectedRow> = emptyList(),
    val width: Int = 1280,
    val height: Int = 900,
    val debuggerView: BookDebuggerTreeView = BookDebuggerTreeView(),
    val memoryView: BookMemoryView? = null,
    val bitmapView: BookBitmapView? = null,
) {
    fun validate(projectRoot: Path) {
        require(id.matches(Regex("[a-z][a-z0-9-]*"))) { "Invalid figure ID" }
        require(sourceSha256.matches(Regex("[a-f0-9]{64}"))) { "A fixture digest is required" }
        require(checkpoint.matches(Regex("[A-Za-z_][A-Za-z_0-9]*"))) { "Invalid checkpoint label" }
        require(source.isNotBlank() && !Path.of(source).isAbsolute) { "Source must be relative to the fixture project" }
        require(projectRoot.resolve(source).normalize().startsWith(projectRoot.normalize())) { "Source escapes fixture project" }
        require(width in 800..2560 && height in 600..1600) { "Unsupported viewport" }
        require(expectedRows.isNotEmpty() || bitmapView != null) { "A figure needs an observed debugger value or bitmap" }
        debuggerView.validate()
        memoryView?.validate()
        bitmapView?.validate(projectRoot)
        require(bitmapView == null || (memoryView == null && debuggerView.isEmpty && expectedRows.isEmpty())) { "Capture the bitmap in a dedicated raw panel" }
        require(memoryView == null || memoryView.minimumHeight <= height - 200) { "Leave room for the source editor" }
        require(memoryView == null || debuggerView.isEmpty) { "Choose a memory view or a register-tree view for each raw capture" }
        expectedRows.forEach {
            require(it.column == null || (memoryView != null && it.column in listOf("Address", "Bytes", "ASCII", "Words"))) { "A cell callout requires a native memory column" }
            require(it.label.isNotBlank() && it.contains.isNotEmpty() && it.contains.all(String::isNotBlank)) { "Empty UI assertion" }
        }
    }

    fun checkpointLine(text: String): Int {
        val matches = text.lines().mapIndexedNotNull { index, line ->
            index.takeIf { Regex("^\\s*${Regex.escape(checkpoint)}\\s*:").containsMatchIn(line) }
        }
        require(matches.size == 1) { "Checkpoint must identify one source label: $checkpoint" }
        return matches.single()
    }
}

data class BookExpectedRow(val label: String = "", val contains: List<String> = emptyList(), val column: String? = null) {
    fun matches(text: String) = contains.all { it in text }
}

/** Converts native Swing coordinates to the captured PNG, rejecting clipped or absent targets. */
internal fun captureBox(target: Rectangle, viewport: Rectangle): Map<String, Int> {
    require(target.width > 0 && target.height > 0 && viewport.contains(target)) { "Callout is outside the capture" }
    return mapOf("x" to target.x - viewport.x, "y" to target.y - viewport.y,
                 "width" to target.width, "height" to target.height)
}

internal fun captureSha256(bytes: ByteArray) = MessageDigest.getInstance("SHA-256")
    .digest(bytes).joinToString("") { "%02x".format(it) }

/** Analysis notifications also contain in-memory editors that have no native filesystem path. */
internal fun captureFileMatches(file: VirtualFile?, path: Path) =
    file != null && file.isInLocalFileSystem && file.path == path.toString()

/** Native debugger tree names, relative to its hidden root; no simulator values are edited. */
data class BookDebuggerTreeView(
    val collapse: List<List<String>> = emptyList(),
    val expand: List<List<String>> = emptyList(),
    val reveal: List<String> = emptyList(),
) {
    fun validate() {
        require(collapse.size + expand.size <= 32) { "Too many debugger view paths" }
        val paths = collapse + expand + listOfNotNull(reveal.takeIf { it.isNotEmpty() })
        require(paths.all { it.size in 1..8 && it.all(String::isNotBlank) }) { "Empty or excessive debugger view path" }
        require(collapse.distinct().size == collapse.size && expand.distinct().size == expand.size) { "Duplicate debugger view path" }
        require(collapse.none { closed -> (expand + listOfNotNull(reveal.takeIf { it.isNotEmpty() })).any { it.take(closed.size) == closed } }) {
            "A collapsed ancestor cannot contain an expanded or revealed path"
        }
    }

    val isEmpty get() = collapse.isEmpty() && expand.isEmpty() && reveal.isEmpty()
}


/** A read-only native memory-tab navigation request, resolved before the launch. */
data class BookMemoryView(val address: String = "", val minimumHeight: Int = 400) {
    fun validate() {
        require(minimumHeight in 200..600) { "Unsupported memory-view height" }
        require(address.matches(Regex("0x[0-9a-fA-F]{1,16}"))) { "Use an unsigned hexadecimal memory address" }
    }
}
