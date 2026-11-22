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
    fun `loop walkthrough confines source variants and its single independent fixture`() {
        val loop = plan.copy(archives = listOf(archive.copy(id = "005-seven-segment-counter", entry = "range-counter.asm")),
            nativePredictions = true, loopUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/end-pointer.asm", "/tmp/end-pointer.asm", "c".repeat(64))))
        loop.validate()
        loop.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(loop.copy(nativePredictions = false), loop.copy(nativeExecution = true),
            loop.copy(nativeDebug = true), loop.copy(memoryUnit = true), loop.copy(assessmentFixtures = emptyList()),
            loop.copy(archives = listOf(archive)), loop.copy(resumeCompleted = true), loop.copy(loopUnit = false),
            loop.copy(activityNavigation = true), loop.copy(prerequisiteControls = true),
            loop.copy(assessmentFixtures = loop.assessmentFixtures + loop.assessmentFixtures),
            loop.copy(assessmentFixtures = listOf(loop.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `function walkthrough binds its exact unit and isolates its private assessment fixture`() {
        val functions = plan.copy(archives = listOf(archive.copy(id = "003-row-functions", entry = "reverse-rows.asm")),
            nativePredictions = true, functionUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/reverse-and-invert.asm", "/tmp/function.asm", "c".repeat(64))))
        functions.validate()
        functions.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(functions.copy(nativePredictions = false), functions.copy(nativeExecution = true),
            functions.copy(nativeDebug = true), functions.copy(memoryUnit = true), functions.copy(loopUnit = true),
            functions.copy(assessmentFixtures = emptyList()), functions.copy(archives = listOf(archive)),
            functions.copy(resumeCompleted = true), functions.copy(functionUnit = false),
            functions.copy(activityNavigation = true), functions.copy(prerequisiteControls = true),
            functions.copy(assessmentFixtures = functions.assessmentFixtures + functions.assessmentFixtures),
            functions.copy(assessmentFixtures = listOf(functions.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `sprite walkthrough isolates its exact source and single private fixture`() {
        val sprite = plan.copy(archives = listOf(archive.copy(id = "003-sprite-rows", entry = "sprite-rows.asm")),
            nativePredictions = true, spriteUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/vertical-sprite.asm", "/tmp/vertical.asm", "c".repeat(64))))
        sprite.validate()
        sprite.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(sprite.copy(nativePredictions = false), sprite.copy(nativeExecution = true),
            sprite.copy(nativeDebug = true), sprite.copy(memoryUnit = true), sprite.copy(loopUnit = true),
            sprite.copy(functionUnit = true), sprite.copy(assessmentFixtures = emptyList()),
            sprite.copy(archives = listOf(archive)), sprite.copy(resumeCompleted = true), sprite.copy(spriteUnit = false),
            sprite.copy(activityNavigation = true), sprite.copy(prerequisiteControls = true),
            sprite.copy(assessmentFixtures = sprite.assessmentFixtures + sprite.assessmentFixtures),
            sprite.copy(assessmentFixtures = listOf(sprite.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `raster walkthrough isolates its exact source and single private fixture`() {
        val raster = plan.copy(archives = listOf(archive.copy(id = "007-integer-raster", entry = "integer-line.asm")),
            nativePredictions = true, rasterUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/dashed-line.asm", "/tmp/dashed.asm", "c".repeat(64))))
        raster.validate()
        raster.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(raster.copy(nativePredictions = false), raster.copy(nativeExecution = true),
            raster.copy(nativeDebug = true), raster.copy(memoryUnit = true), raster.copy(loopUnit = true),
            raster.copy(functionUnit = true), raster.copy(spriteUnit = true), raster.copy(assessmentFixtures = emptyList()),
            raster.copy(archives = listOf(archive)), raster.copy(resumeCompleted = true), raster.copy(rasterUnit = false),
            raster.copy(activityNavigation = true), raster.copy(prerequisiteControls = true),
            raster.copy(assessmentFixtures = raster.assessmentFixtures + raster.assessmentFixtures),
            raster.copy(assessmentFixtures = listOf(raster.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `Life walkthrough isolates its exact source and single private fixture`() {
        val life = plan.copy(archives = listOf(archive.copy(id = "008-life-buffers", entry = "life-buffers.asm")),
            nativePredictions = true, lifeUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/life-changes.asm", "/tmp/life.asm", "c".repeat(64))))
        life.validate()
        life.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(life.copy(nativePredictions = false), life.copy(nativeExecution = true),
            life.copy(nativeDebug = true), life.copy(memoryUnit = true), life.copy(loopUnit = true),
            life.copy(functionUnit = true), life.copy(spriteUnit = true), life.copy(rasterUnit = true), life.copy(assessmentFixtures = emptyList()),
            life.copy(archives = listOf(archive)), life.copy(resumeCompleted = true), life.copy(lifeUnit = false),
            life.copy(activityNavigation = true), life.copy(prerequisiteControls = true),
            life.copy(assessmentFixtures = life.assessmentFixtures + life.assessmentFixtures),
            life.copy(assessmentFixtures = listOf(life.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `sorting walkthrough isolates its exact source and single private fixture`() {
        val sorting = plan.copy(archives = listOf(archive.copy(id = "009-sorting-costs", entry = "sorting-costs.asm")),
            nativePredictions = true, sortingUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/partition-negative.asm", "/tmp/sorting.asm", "c".repeat(64))))
        sorting.validate()
        sorting.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(sorting.copy(nativePredictions = false), sorting.copy(nativeExecution = true),
            sorting.copy(nativeDebug = true), sorting.copy(memoryUnit = true), sorting.copy(loopUnit = true),
            sorting.copy(functionUnit = true), sorting.copy(spriteUnit = true), sorting.copy(rasterUnit = true), sorting.copy(lifeUnit = true), sorting.copy(assessmentFixtures = emptyList()),
            sorting.copy(archives = listOf(archive)), sorting.copy(resumeCompleted = true), sorting.copy(sortingUnit = false),
            sorting.copy(activityNavigation = true), sorting.copy(prerequisiteControls = true),
            sorting.copy(assessmentFixtures = sorting.assessmentFixtures + sorting.assessmentFixtures),
            sorting.copy(assessmentFixtures = listOf(sorting.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `fixed walkthrough isolates its exact source and single private fixture`() {
        val fixed = plan.copy(archives = listOf(archive.copy(id = "012-fixed-point", entry = "fixed-point-ball.asm")),
            nativePredictions = true, fixedUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/fixed-interpolate.asm", "/tmp/fixed.asm", "c".repeat(64))))
        fixed.validate()
        fixed.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(fixed.copy(nativePredictions = false), fixed.copy(nativeExecution = true),
            fixed.copy(nativeDebug = true), fixed.copy(memoryUnit = true), fixed.copy(loopUnit = true),
            fixed.copy(functionUnit = true), fixed.copy(spriteUnit = true), fixed.copy(rasterUnit = true), fixed.copy(lifeUnit = true), fixed.copy(sortingUnit = true), fixed.copy(assessmentFixtures = emptyList()),
            fixed.copy(archives = listOf(archive)), fixed.copy(resumeCompleted = true), fixed.copy(fixedUnit = false),
            fixed.copy(activityNavigation = true), fixed.copy(prerequisiteControls = true),
            fixed.copy(assessmentFixtures = fixed.assessmentFixtures + fixed.assessmentFixtures),
            fixed.copy(assessmentFixtures = listOf(fixed.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `locality walkthrough isolates its exact source and single private fixture`() {
        val locality = plan.copy(archives = listOf(archive.copy(id = "011-memory-locality", entry = "matrix-locality.asm")),
            nativePredictions = true, localityUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/matrix-rotate.asm", "/tmp/locality.asm", "c".repeat(64))))
        locality.validate()
        locality.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(locality.copy(nativePredictions = false), locality.copy(nativeExecution = true),
            locality.copy(nativeDebug = true), locality.copy(memoryUnit = true), locality.copy(loopUnit = true),
            locality.copy(functionUnit = true), locality.copy(spriteUnit = true), locality.copy(rasterUnit = true), locality.copy(lifeUnit = true), locality.copy(sortingUnit = true), locality.copy(fixedUnit = true), locality.copy(assessmentFixtures = emptyList()),
            locality.copy(archives = listOf(archive)), locality.copy(resumeCompleted = true), locality.copy(localityUnit = false),
            locality.copy(activityNavigation = true), locality.copy(prerequisiteControls = true),
            locality.copy(assessmentFixtures = locality.assessmentFixtures + locality.assessmentFixtures),
            locality.copy(assessmentFixtures = listOf(locality.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `bounded walkthrough isolates its exact source and single private fixture`() {
        val bounded = plan.copy(archives = listOf(archive.copy(id = "013-bounded-interfaces", entry = "bounded-packet.asm")),
            nativePredictions = true, boundedUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/bounded-line.asm", "/tmp/bounded.asm", "c".repeat(64))))
        bounded.validate()
        bounded.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(bounded.copy(nativePredictions = false), bounded.copy(nativeExecution = true),
            bounded.copy(nativeDebug = true), bounded.copy(memoryUnit = true), bounded.copy(loopUnit = true),
            bounded.copy(functionUnit = true), bounded.copy(spriteUnit = true), bounded.copy(rasterUnit = true), bounded.copy(lifeUnit = true), bounded.copy(sortingUnit = true), bounded.copy(fixedUnit = true), bounded.copy(localityUnit = true), bounded.copy(assessmentFixtures = emptyList()),
            bounded.copy(archives = listOf(archive)), bounded.copy(resumeCompleted = true), bounded.copy(boundedUnit = false),
            bounded.copy(activityNavigation = true), bounded.copy(prerequisiteControls = true),
            bounded.copy(assessmentFixtures = bounded.assessmentFixtures + bounded.assessmentFixtures),
            bounded.copy(assessmentFixtures = listOf(bounded.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
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
