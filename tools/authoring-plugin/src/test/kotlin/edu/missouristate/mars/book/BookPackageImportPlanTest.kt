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
        plan.copy(nativePredictions = true, nativeExecution = true).validate()
        plan.copy(nativePredictions = true, nativeExecution = true, nativeDebug = true).validate()
        assertThrows<IllegalArgumentException> { plan.copy(nativeDebug = true).validate() }
        assertThrows<IllegalArgumentException> { plan.copy(resumeCompleted = true).validate() }
        plan.copy(nativePredictions = true, nativeExecution = true, nativeDebug = true, resume = true, resumeCompleted = true,
            expectedEntryDigests = mapOf(archive.sha256 to "c".repeat(64))).validate()
        assertThrows<IllegalArgumentException> { plan.copy(nativeExecution = true).validate() }
        plan.copy(nativePredictions = true, resume = true,
            expectedEntryDigests = mapOf(archive.sha256 to "c".repeat(64))).validate()
        assertThrows<IllegalArgumentException> {
            plan.copy(nativePredictions = true, archives = listOf(archive, archive.copy(sha256 = "c".repeat(64)))).validate()
        }
    }

    @Test
    fun `assessment walkthrough requires both pinned local fixtures and the preceding native sequence`() {
        val plus = BookAssessmentFixture("independent/plus-one.asm", "/tmp/private-plus.asm", "c".repeat(64))
        val wrapped = BookAssessmentFixture("independent/wrapped-byte.asm", "/tmp/private-wrapped.asm", "d".repeat(64))
        val complete = plan.copy(nativePredictions = true, nativeExecution = true, nativeDebug = true,
            assessmentFixtures = listOf(plus, wrapped))
        complete.validate()
        assertThrows<IllegalArgumentException> { complete.copy(nativeDebug = false).validate() }
        assertThrows<IllegalArgumentException> { complete.copy(assessmentFixtures = listOf(plus)).validate() }
        assertThrows<IllegalArgumentException> { complete.copy(assessmentFixtures = listOf(plus, wrapped, plus)).validate() }
        for (bad in listOf(plus.copy(source = "../private.asm"), plus.copy(path = "relative.asm"), plus.copy(sha256 = "bad"))) {
            assertThrows<IllegalArgumentException> { complete.copy(assessmentFixtures = listOf(bad, wrapped)).validate() }
        }
    }

    @Test
    fun `activity navigation is confined to the completed independent fixture and its continuation`() {
        val fixtures = listOf(BookAssessmentFixture("independent/plus-one.asm", "/tmp/plus.asm", "c".repeat(64)),
            BookAssessmentFixture("independent/wrapped-byte.asm", "/tmp/wrapped.asm", "d".repeat(64)))
        val navigation = plan.copy(nativePredictions = true, nativeExecution = true, nativeDebug = true,
            resume = true, resumeCompleted = true, assessmentFixtures = fixtures,
            expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64)), activityNavigation = true)
        navigation.validate(); navigation.copy(resumeActivity = true).validate()
        assertThrows<IllegalArgumentException> { navigation.copy(resumeCompleted = false).validate() }
        assertThrows<IllegalArgumentException> { navigation.copy(assessmentFixtures = emptyList()).validate() }
        assertThrows<IllegalArgumentException> { navigation.copy(activityNavigation = false, resumeActivity = true).validate() }
    }

    @Test
    fun `instructor prerequisite captures require a completed exact independent unit and isolate their continuation`() {
        val fixtures=listOf(BookAssessmentFixture("independent/plus-one.asm","/tmp/plus.asm","c".repeat(64)),
            BookAssessmentFixture("independent/wrapped-byte.asm","/tmp/wrapped.asm","d".repeat(64)))
        val controls=plan.copy(nativePredictions=true,nativeExecution=true,nativeDebug=true,resume=true,resumeCompleted=true,
            assessmentFixtures=fixtures,expectedEntryDigests=mapOf(archive.sha256 to "b".repeat(64)),prerequisiteControls=true)
        controls.validate(); controls.copy(resumePrerequisites=true).validate()
        assertThrows<IllegalArgumentException> { controls.copy(resumeCompleted=false).validate() }
        assertThrows<IllegalArgumentException> { controls.copy(assessmentFixtures=emptyList()).validate() }
        assertThrows<IllegalArgumentException> { controls.copy(activityNavigation=true).validate() }
        assertThrows<IllegalArgumentException> { controls.copy(prerequisiteControls=false,resumePrerequisites=true).validate() }
    }

    @Test
    fun `memory walkthrough isolates its exact source interface and private fixtures`() {
        val memory = plan.copy(archives = listOf(archive.copy(id = "002-single-pixel", entry = "memory-pixel.asm")),
            nativePredictions = true, memoryUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/red-component.asm", "/tmp/red.asm", "c".repeat(64)),
                BookAssessmentFixture("independent/independent-copy.asm", "/tmp/copy.asm", "d".repeat(64))))
        memory.validate()
        memory.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(memory.copy(nativePredictions = false), memory.copy(nativeExecution = true),
            memory.copy(nativeDebug = true), memory.copy(assessmentFixtures = emptyList()),
            memory.copy(archives = listOf(archive)), memory.copy(resumeCompleted = true),
            memory.copy(memoryUnit = false))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
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
