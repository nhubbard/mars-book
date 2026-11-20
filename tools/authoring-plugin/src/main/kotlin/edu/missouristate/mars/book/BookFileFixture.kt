package edu.missouristate.mars.book

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import edu.missouristate.mars.api.SyscallEnvironment
import edu.missouristate.mars.plugin.run.MarsSyscallEnvironmentCustomizer
import java.nio.file.Files
import java.nio.file.Path

/** Opt-in, read-only virtual file for a completed authoring checkpoint, never a student runtime policy. */
data class BookFileFixture(
    val guestPath: String = "",
    val contentsHex: String = "",
    val maximumRead: Int = 256,
    val failAfter: Int? = null,
    val missing: Boolean = false,
    val expectedOperations: List<BookFileOperation> = emptyList(),
) {
    fun validate() {
        require(guestPath.isNotBlank() && '\u0000' !in guestPath && guestPath.length <= 4096) { "Invalid fixture guest path" }
        require(contentsHex.length <= 2_097_152 && contentsHex.matches(Regex("(?:[a-f0-9]{2})*"))) { "Invalid fixture octets" }
        require(maximumRead in 1..1_048_576) { "Invalid fixture read limit" }
        failAfter?.let { require(it in 0..contentsHex.length / 2 && !missing) { "Invalid fixture failure offset" } }
        require(expectedOperations.isNotEmpty()) { "File fixture needs exact operation evidence" }
        require(expectedOperations.all { it.operation in listOf("OPEN", "READ", "WRITE", "CLOSE") && it.requested >= 0 }) { "Invalid expected file operation" }
    }

    fun bytes(): ByteArray = contentsHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}

data class BookFileOperation(val operation: String = "", val descriptor: Int = 0, val requested: Int = 0, val result: Int = 0)

data class BookFileEvidence(
    val guestPath: String,
    val contentsSha256: String,
    val maximumRead: Int,
    val failAfter: Int?,
    val missing: Boolean,
    val operations: List<BookFileOperation>,
    val openDescriptors: List<Int>,
    val disposed: Boolean,
    val cleanupDescriptors: List<Int>,
)

/** Fresh cursors and descriptors per launch; all non-file services retain their native behavior. */
internal class BookFixtureFileEnvironment(
    private val delegate: SyscallEnvironment, private val fixture: BookFileFixture,
) : SyscallEnvironment by delegate, AutoCloseable {
    private val contents = fixture.also { it.validate() }.bytes()
    private val cursors = mutableMapOf<Int, Int>()
    private val operations = mutableListOf<BookFileOperation>()
    private var disposed = false
    private var cleanupDescriptors = emptyList<Int>()


    @Synchronized
    override fun openFile(path: String?, flags: Int): Int {
        check(!disposed) { "Fixture environment is disposed" }
        val descriptor = if (flags != 0 || path != fixture.guestPath || fixture.missing) -1 else
            (3..31).firstOrNull { it !in cursors } ?: -1
        if (descriptor >= 0) cursors[descriptor] = 0
        operations.add(BookFileOperation("OPEN", descriptor, 0, descriptor))
        return descriptor
    }

    @Synchronized
    override fun readFromFile(fd: Int, buffer: ByteArray?, lengthRequested: Int): Int {
        check(!disposed) { "Fixture environment is disposed" }
        if (fd == 0) return delegate.readFromFile(fd, buffer, lengthRequested)
        val position = cursors[fd]
        val result = when {
            position == null || buffer == null || lengthRequested < 0 || lengthRequested > buffer.size -> -1
            lengthRequested == 0 -> 0
            fixture.failAfter != null && position >= fixture.failAfter -> -1
            else -> minOf(lengthRequested, fixture.maximumRead, contents.size - position, (fixture.failAfter ?: contents.size) - position)
        }
        if (result > 0) {
            contents.copyInto(checkNotNull(buffer), 0, checkNotNull(position), position + result)
            cursors[fd] = position + result
        }
        operations.add(BookFileOperation("READ", fd, lengthRequested, result))
        return result
    }

    @Synchronized
    override fun writeToFile(fd: Int, buffer: ByteArray?, lengthRequested: Int): Int {
        check(!disposed) { "Fixture environment is disposed" }
        if (fd == 1 || fd == 2) return delegate.writeToFile(fd, buffer, lengthRequested)
        operations.add(BookFileOperation("WRITE", fd, lengthRequested, -1))
        return -1
    }

    @Synchronized
    override fun closeFile(fd: Int) {
        check(!disposed) { "Fixture environment is disposed" }
        if (fd in 0..2) { delegate.closeFile(fd); return }
        operations.add(BookFileOperation("CLOSE", fd, 0, if (cursors.remove(fd) == null) -1 else 0))
    }

    @Synchronized
    fun snapshot() = BookFileEvidence(fixture.guestPath, captureSha256(contents), fixture.maximumRead,
        fixture.failAfter, fixture.missing, operations.toList(), cursors.keys.sorted(), disposed, cleanupDescriptors.toList())

    @Synchronized
    fun checkedSnapshot(): BookFileEvidence = snapshot().also {
        check(it.operations == fixture.expectedOperations) { "Native file operations differ: ${it.operations}" }
        check(it.openDescriptors.isEmpty() && it.cleanupDescriptors.isEmpty()) { "Guest did not close its fixture descriptors" }
    }

    @Synchronized
    override fun close() {
        if (disposed) return
        cleanupDescriptors = cursors.keys.sorted()
        cursors.clear()
        disposed = true
    }
}

/** The existing capture controller owns registrations; unrelated launches pass through unchanged. */
@Service(Service.Level.PROJECT)
internal class BookFileFixtureRegistry {
    private val bindings = mutableMapOf<Path, Binding>()

    @Synchronized
    fun prepare(entry: Path, sourceSha256: String, fixture: BookFileFixture): Binding {
        fixture.validate()
        val path = entry.toRealPath()
        check(path !in bindings) { "Another capture already owns this source's file fixture" }
        return Binding(path, sourceSha256, fixture).also { bindings[path] = it }
    }

    @Synchronized
    fun customize(entry: Path, io: SyscallEnvironment, lifetime: Disposable): SyscallEnvironment {
        if (bindings.isEmpty() || !Files.exists(entry)) return io
        val binding = bindings[entry.toRealPath()] ?: return io
        check(captureSha256(Files.readAllBytes(entry)) == binding.sourceSha256) { "Fixture source changed before launch" }
        check(binding.environment == null) { "Ambiguous simultaneous launch of the same capture fixture" }
        return BookFixtureFileEnvironment(io, binding.fixture).also { environment ->
            binding.environment = environment
            Disposer.register(lifetime) { environment.close() }
        }
    }

    @Synchronized
    fun snapshot(entry: Path): BookFileEvidence = checkNotNull(bindings[entry.toRealPath()]).snapshot()

    internal inner class Binding(
        val entry: Path, val sourceSha256: String, val fixture: BookFileFixture,
    ) : AutoCloseable {
        @Volatile var environment: BookFixtureFileEnvironment? = null
            internal set

        fun snapshot(): BookFileEvidence = checkNotNull(environment) { "Native launch did not install its file fixture" }.checkedSnapshot()

        override fun close() {
            synchronized(this@BookFileFixtureRegistry) { check(bindings.remove(entry) === this) { "Fixture binding was already removed" } }
        }
    }
}

class BookFileFixtureCustomizer : MarsSyscallEnvironmentCustomizer {
    override fun customize(project: Project, entry: Path, environment: SyscallEnvironment, lifetime: Disposable): SyscallEnvironment =
        project.service<BookFileFixtureRegistry>().customize(entry, environment, lifetime)
}
