package edu.missouristate.mars.book

import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BookPackageImportPlanTest {
    private val archive = BookImportArchive("/tmp/student.zip", "a".repeat(64), "001-example", "1.0.0", "starter/main.asm", "assignment.md", "b".repeat(64))
    private val plan = BookPackageImportPlan("/tmp/import-project", "/tmp/import-evidence", listOf(archive))

    @Test
    fun `native imports bind exact archives and keep source-relative paths in the fixture`() {
        plan.validate()
        assertEquals(Path.of("/tmp/import-project/mars-labs/packages/001-example/1.0.0/" + "a".repeat(64)), archive.directory(Path.of(plan.projectRoot)))
        for (bad in listOf(archive.copy(path = "relative.zip"), archive.copy(sha256 = "bad"), archive.copy(entrySha256 = "bad"),
            archive.copy(id = "../bad"), archive.copy(version = "1"), archive.copy(entry = "../escape.asm"), archive.copy(assignment = "/outside.md"),
            archive.copy(entry = "a/../main.asm"), archive.copy(entry = ""))) {
            assertThrows<IllegalArgumentException> { plan.copy(archives = listOf(bad)).validate() }
        }
        assertThrows<IllegalArgumentException> { plan.copy(archives = emptyList()).validate() }
        assertThrows<IllegalArgumentException> { plan.copy(archives = listOf(archive, archive)).validate() }
        assertThrows<IllegalArgumentException> { plan.copy(outputDirectory = plan.projectRoot).validate() }
        assertThrows<IllegalArgumentException> { plan.copy(projectRoot = "relative").validate() }
    }

    @Test
    fun `course links retain a local exact archive binding for native capture`() {
        plan.copy(archives = listOf(archive.copy(source = "https://example.com/student.zip?edition=1"))).validate()
        for (source in listOf("http://example.com/a.zip", "relative.zip", "https:///a.zip",
            "https://user:secret@example.com/a.zip", "https://example.com/a.zip#step")) {
            assertThrows<IllegalArgumentException> { plan.copy(archives = listOf(archive.copy(source = source))).validate() }
        }
    }

    @Test
    fun `native prediction walkthrough binds one edition and retains resume source checks`() {
        plan.copy(nativePredictions = true).validate()
        plan.copy(nativePredictions = true, resume = true,
            expectedEntryDigests = mapOf(archive.sha256 to "c".repeat(64))).validate()
        assertThrows<IllegalArgumentException> {
            plan.copy(nativePredictions = true, archives = listOf(archive, archive.copy(sha256 = "c".repeat(64)))).validate()
        }
    }

    @Test
    fun `fresh-process captures require every retained source digest and do not inject source`() {
        val resume = plan.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "c".repeat(64)))
        resume.validate()
        assertThrows<IllegalArgumentException> { resume.copy(expectedEntryDigests = emptyMap()).validate() }
        assertThrows<IllegalArgumentException> { resume.copy(expectedEntryDigests = mapOf(archive.sha256 to "bad")).validate() }
        assertThrows<IllegalArgumentException> { resume.copy(resume = false).validate() }
        plan.copy(archives = listOf(archive, archive.copy(sha256 = "c".repeat(64), version = "1.1.0"))).validate()
    }
}
