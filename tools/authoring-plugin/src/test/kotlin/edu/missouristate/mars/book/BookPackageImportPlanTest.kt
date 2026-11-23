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
    fun `measurement walkthrough isolates its exact source and single private fixture`() {
        val measurement = plan.copy(archives = listOf(archive.copy(id = "022-measuring-programs", entry = "matrix-measure.asm")),
            nativePredictions = true, measurementUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/matrix-vector.asm", "/tmp/measurement.asm", "c".repeat(64))))
        measurement.validate()
        measurement.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(measurement.copy(nativePredictions = false), measurement.copy(nativeExecution = true),
            measurement.copy(nativeDebug = true), measurement.copy(memoryUnit = true), measurement.copy(loopUnit = true),
            measurement.copy(functionUnit = true), measurement.copy(spriteUnit = true), measurement.copy(rasterUnit = true), measurement.copy(lifeUnit = true), measurement.copy(sortingUnit = true), measurement.copy(fixedUnit = true), measurement.copy(localityUnit = true), measurement.copy(boundedUnit = true), measurement.copy(assessmentFixtures = emptyList()),
            measurement.copy(archives = listOf(archive)), measurement.copy(resumeCompleted = true), measurement.copy(measurementUnit = false),
            measurement.copy(activityNavigation = true), measurement.copy(prerequisiteControls = true),
            measurement.copy(assessmentFixtures = measurement.assessmentFixtures + measurement.assessmentFixtures),
            measurement.copy(assessmentFixtures = listOf(measurement.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `msa walkthrough isolates its exact source and single private fixture`() {
        val msa = plan.copy(archives = listOf(archive.copy(id = "019-working-with-msa", entry = "rgb-formats.asm")),
            nativePredictions = true, msaUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/rgb555.asm", "/tmp/msa.asm", "c".repeat(64))))
        msa.validate()
        msa.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(msa.copy(nativePredictions = false), msa.copy(nativeExecution = true),
            msa.copy(nativeDebug = true), msa.copy(memoryUnit = true), msa.copy(loopUnit = true),
            msa.copy(functionUnit = true), msa.copy(spriteUnit = true), msa.copy(rasterUnit = true), msa.copy(lifeUnit = true), msa.copy(sortingUnit = true), msa.copy(fixedUnit = true), msa.copy(localityUnit = true), msa.copy(boundedUnit = true), msa.copy(measurementUnit = true), msa.copy(assessmentFixtures = emptyList()),
            msa.copy(archives = listOf(archive)), msa.copy(resumeCompleted = true), msa.copy(msaUnit = false),
            msa.copy(activityNavigation = true), msa.copy(prerequisiteControls = true),
            msa.copy(assessmentFixtures = msa.assessmentFixtures + msa.assessmentFixtures),
            msa.copy(assessmentFixtures = listOf(msa.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `pcm walkthrough isolates its exact source and single private fixture`() {
        val pcm = plan.copy(archives = listOf(archive.copy(id = "020-pcm-mixing", entry = "pcm-mix.asm")),
            nativePredictions = true, pcmUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/pcm-subtract.asm", "/tmp/pcm.asm", "c".repeat(64))))
        pcm.validate()
        pcm.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(pcm.copy(nativePredictions = false), pcm.copy(nativeExecution = true),
            pcm.copy(nativeDebug = true), pcm.copy(memoryUnit = true), pcm.copy(loopUnit = true),
            pcm.copy(functionUnit = true), pcm.copy(spriteUnit = true), pcm.copy(rasterUnit = true), pcm.copy(lifeUnit = true), pcm.copy(sortingUnit = true), pcm.copy(fixedUnit = true), pcm.copy(localityUnit = true), pcm.copy(boundedUnit = true), pcm.copy(measurementUnit = true), pcm.copy(msaUnit = true), pcm.copy(assessmentFixtures = emptyList()),
            pcm.copy(archives = listOf(archive)), pcm.copy(resumeCompleted = true), pcm.copy(pcmUnit = false),
            pcm.copy(activityNavigation = true), pcm.copy(prerequisiteControls = true),
            pcm.copy(assessmentFixtures = pcm.assessmentFixtures + pcm.assessmentFixtures),
            pcm.copy(assessmentFixtures = listOf(pcm.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `fir walkthrough isolates its exact source and single private fixture`() {
        val fir = plan.copy(archives = listOf(archive.copy(id = "021-fixed-point-fir", entry = "fir-filter.asm")),
            nativePredictions = true, firUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/fir-decimate.asm", "/tmp/fir.asm", "c".repeat(64))))
        fir.validate()
        fir.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(fir.copy(nativePredictions = false), fir.copy(nativeExecution = true),
            fir.copy(nativeDebug = true), fir.copy(memoryUnit = true), fir.copy(loopUnit = true),
            fir.copy(functionUnit = true), fir.copy(spriteUnit = true), fir.copy(rasterUnit = true), fir.copy(lifeUnit = true), fir.copy(sortingUnit = true), fir.copy(fixedUnit = true), fir.copy(localityUnit = true), fir.copy(boundedUnit = true), fir.copy(measurementUnit = true), fir.copy(msaUnit = true), fir.copy(pcmUnit = true), fir.copy(assessmentFixtures = emptyList()),
            fir.copy(archives = listOf(archive)), fir.copy(resumeCompleted = true), fir.copy(firUnit = false),
            fir.copy(activityNavigation = true), fir.copy(prerequisiteControls = true),
            fir.copy(assessmentFixtures = fir.assessmentFixtures + fir.assessmentFixtures),
            fir.copy(assessmentFixtures = listOf(fir.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `delta walkthrough isolates its exact source and single private fixture`() {
        val delta = plan.copy(archives = listOf(archive.copy(id = "023-delta-varint", entry = "delta-codec.asm")),
            nativePredictions = true, deltaUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/delta-scan.asm", "/tmp/delta.asm", "c".repeat(64))))
        delta.validate()
        delta.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(delta.copy(nativePredictions = false), delta.copy(nativeExecution = true),
            delta.copy(nativeDebug = true), delta.copy(memoryUnit = true), delta.copy(loopUnit = true),
            delta.copy(functionUnit = true), delta.copy(spriteUnit = true), delta.copy(rasterUnit = true), delta.copy(lifeUnit = true), delta.copy(sortingUnit = true), delta.copy(fixedUnit = true), delta.copy(localityUnit = true), delta.copy(boundedUnit = true), delta.copy(measurementUnit = true), delta.copy(msaUnit = true), delta.copy(pcmUnit = true), delta.copy(firUnit = true), delta.copy(assessmentFixtures = emptyList()),
            delta.copy(archives = listOf(archive)), delta.copy(resumeCompleted = true), delta.copy(deltaUnit = false),
            delta.copy(activityNavigation = true), delta.copy(prerequisiteControls = true),
            delta.copy(assessmentFixtures = delta.assessmentFixtures + delta.assessmentFixtures),
            delta.copy(assessmentFixtures = listOf(delta.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `base64 walkthrough isolates its exact source and single private fixture`() {
        val base64 = plan.copy(archives = listOf(archive.copy(id = "028-base64", entry = "base64-codec.asm")),
            nativePredictions = true, base64Unit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/base64url.asm", "/tmp/base64.asm", "c".repeat(64))))
        base64.validate()
        base64.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(base64.copy(nativePredictions = false), base64.copy(nativeExecution = true),
            base64.copy(nativeDebug = true), base64.copy(memoryUnit = true), base64.copy(loopUnit = true),
            base64.copy(functionUnit = true), base64.copy(spriteUnit = true), base64.copy(rasterUnit = true), base64.copy(lifeUnit = true), base64.copy(sortingUnit = true), base64.copy(fixedUnit = true), base64.copy(localityUnit = true), base64.copy(boundedUnit = true), base64.copy(measurementUnit = true), base64.copy(msaUnit = true), base64.copy(pcmUnit = true), base64.copy(firUnit = true), base64.copy(deltaUnit = true), base64.copy(ppmUnit = true), base64.copy(assessmentFixtures = emptyList()),
            base64.copy(archives = listOf(archive)), base64.copy(resumeCompleted = true), base64.copy(base64Unit = false),
            base64.copy(activityNavigation = true), base64.copy(prerequisiteControls = true),
            base64.copy(assessmentFixtures = base64.assessmentFixtures + base64.assessmentFixtures),
            base64.copy(assessmentFixtures = listOf(base64.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `PPM walkthrough isolates its exact source and single private fixture`() {
        val ppm = plan.copy(archives = listOf(archive.copy(id = "029-binary-ppm", entry = "ppm-codec.asm")),
            nativePredictions = true, ppmUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/ppm-negative.asm", "/tmp/ppm.asm", "c".repeat(64))))
        ppm.validate()
        ppm.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(ppm.copy(nativePredictions = false), ppm.copy(nativeExecution = true),
            ppm.copy(nativeDebug = true), ppm.copy(memoryUnit = true), ppm.copy(loopUnit = true),
            ppm.copy(functionUnit = true), ppm.copy(spriteUnit = true), ppm.copy(rasterUnit = true), ppm.copy(lifeUnit = true), ppm.copy(sortingUnit = true), ppm.copy(fixedUnit = true), ppm.copy(localityUnit = true), ppm.copy(boundedUnit = true), ppm.copy(measurementUnit = true), ppm.copy(msaUnit = true), ppm.copy(pcmUnit = true), ppm.copy(firUnit = true), ppm.copy(deltaUnit = true), ppm.copy(base64Unit = true), ppm.copy(assessmentFixtures = emptyList()),
            ppm.copy(archives = listOf(archive)), ppm.copy(resumeCompleted = true), ppm.copy(ppmUnit = false),
            ppm.copy(activityNavigation = true), ppm.copy(prerequisiteControls = true),
            ppm.copy(assessmentFixtures = ppm.assessmentFixtures + ppm.assessmentFixtures),
            ppm.copy(assessmentFixtures = listOf(ppm.assessmentFixtures.single().copy(source = "independent/plus-one.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `CRC walkthrough isolates its archive source fixture and native activity lifetimes`() {
        val crc = plan.copy(archives = listOf(archive.copy(id = "024-crc32", entry = "crc32.asm")),
            nativePredictions = true, crcUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/crc32-chunk.asm", "/tmp/chunk.asm", "c".repeat(64))))
        crc.validate()
        crc.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(crc.copy(nativePredictions = false), crc.copy(nativeExecution = true),
            crc.copy(nativeDebug = true), crc.copy(memoryUnit = true), crc.copy(loopUnit = true),
            crc.copy(functionUnit = true), crc.copy(spriteUnit = true), crc.copy(rasterUnit = true),
            crc.copy(lifeUnit = true), crc.copy(sortingUnit = true), crc.copy(fixedUnit = true),
            crc.copy(localityUnit = true), crc.copy(boundedUnit = true), crc.copy(measurementUnit = true),
            crc.copy(msaUnit = true), crc.copy(pcmUnit = true), crc.copy(firUnit = true),
            crc.copy(deltaUnit = true), crc.copy(base64Unit = true), crc.copy(ppmUnit = true),
            crc.copy(assessmentFixtures = emptyList()), crc.copy(archives = listOf(archive)),
            crc.copy(resumeCompleted = true), crc.copy(crcUnit = false),
            crc.copy(activityNavigation = true), crc.copy(prerequisiteControls = true),
            crc.copy(assessmentFixtures = crc.assessmentFixtures + crc.assessmentFixtures),
            crc.copy(assessmentFixtures = listOf(crc.assessmentFixtures.single().copy(source = "independent/ppm-negative.asm"))),
            crc.copy(archives = listOf(crc.archives.single().copy(entry = "other.asm"))),
            crc.copy(archives = listOf(crc.archives.single().copy(id = "029-binary-ppm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `QOI walkthrough keeps private reflection fixtures separate and isolates its content edition`() {
        val qoi = plan.copy(archives = listOf(archive.copy(id = "030-qoi", entry = "qoi-codec.asm")),
            nativePredictions = true, qoiUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/qoi-mirrored.asm", "/tmp/mirror.asm", "c".repeat(64))))
        qoi.validate()
        qoi.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(qoi.copy(nativePredictions = false), qoi.copy(nativeExecution = true),
            qoi.copy(nativeDebug = true), qoi.copy(resumeCompleted = true), qoi.copy(crcUnit = true),
            qoi.copy(ppmUnit = true), qoi.copy(memoryUnit = true), qoi.copy(qoiUnit = false),
            qoi.copy(activityNavigation = true), qoi.copy(prerequisiteControls = true),
            qoi.copy(assessmentFixtures = emptyList()),
            qoi.copy(assessmentFixtures = qoi.assessmentFixtures + qoi.assessmentFixtures),
            qoi.copy(assessmentFixtures = listOf(qoi.assessmentFixtures.single().copy(source = "independent/crc32-chunk.asm"))),
            qoi.copy(archives = listOf(qoi.archives.single().copy(id = "024-crc32"))),
            qoi.copy(archives = listOf(qoi.archives.single().copy(entry = "other.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `CBOR walkthrough isolates sequence answers and the correct native archive`() {
        val cbor = plan.copy(archives = listOf(archive.copy(id = "032-cbor-telemetry", entry = "cbor-telemetry.asm")),
            nativePredictions = true, cborUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/cbor-sequence.asm", "/tmp/sequence.asm", "c".repeat(64))))
        cbor.validate()
        cbor.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(cbor.copy(nativePredictions = false), cbor.copy(nativeExecution = true),
            cbor.copy(nativeDebug = true), cbor.copy(resumeCompleted = true), cbor.copy(qoiUnit = true),
            cbor.copy(crcUnit = true), cbor.copy(memoryUnit = true), cbor.copy(cborUnit = false),
            cbor.copy(activityNavigation = true), cbor.copy(prerequisiteControls = true),
            cbor.copy(assessmentFixtures = emptyList()),
            cbor.copy(assessmentFixtures = cbor.assessmentFixtures + cbor.assessmentFixtures),
            cbor.copy(assessmentFixtures = listOf(cbor.assessmentFixtures.single().copy(source = "independent/qoi-mirrored.asm"))),
            cbor.copy(archives = listOf(cbor.archives.single().copy(id = "030-qoi"))),
            cbor.copy(archives = listOf(cbor.archives.single().copy(entry = "other.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `ChaCha20 walkthrough isolates the complete interface answer and native archive`() {
        val chacha = plan.copy(archives = listOf(archive.copy(id = "026-chacha20", entry = "chacha20.asm")),
            nativePredictions = true, chachaUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/chacha20.asm", "/tmp/chacha.asm", "c".repeat(64))))
        chacha.validate()
        chacha.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(chacha.copy(nativePredictions = false), chacha.copy(nativeExecution = true),
            chacha.copy(nativeDebug = true), chacha.copy(resumeCompleted = true), chacha.copy(qoiUnit = true), chacha.copy(cborUnit = true),
            chacha.copy(crcUnit = true), chacha.copy(memoryUnit = true), chacha.copy(chachaUnit = false),
            chacha.copy(activityNavigation = true), chacha.copy(prerequisiteControls = true),
            chacha.copy(assessmentFixtures = emptyList()),
            chacha.copy(assessmentFixtures = chacha.assessmentFixtures + chacha.assessmentFixtures),
            chacha.copy(assessmentFixtures = listOf(chacha.assessmentFixtures.single().copy(source = "independent/qoi-mirrored.asm"))),
            chacha.copy(archives = listOf(chacha.archives.single().copy(id = "030-qoi"))),
            chacha.copy(archives = listOf(chacha.archives.single().copy(entry = "other.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `SHA corpus plans bind one archive and private fixture and retain fresh-process resume`() {
        val sha = plan.copy(archives = listOf(archive.copy(id = "025-sha256", version = "0.2.0", entry = "worked/schedule.asm")),
            nativePredictions = true, shaUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/sha256.asm", "/tmp/sha.asm", "c".repeat(64))))
        sha.validate()
        sha.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(sha.copy(nativePredictions = false), sha.copy(nativeExecution = true),
            sha.copy(nativeDebug = true), sha.copy(resumeCompleted = true), sha.copy(chachaUnit = true),
            sha.copy(cborUnit = true), sha.copy(qoiUnit = true), sha.copy(crcUnit = true), sha.copy(memoryUnit = true),
            sha.copy(shaUnit = false), sha.copy(activityNavigation = true), sha.copy(prerequisiteControls = true),
            sha.copy(assessmentFixtures = emptyList()),
            sha.copy(assessmentFixtures = sha.assessmentFixtures + sha.assessmentFixtures),
            sha.copy(assessmentFixtures = listOf(sha.assessmentFixtures.single().copy(source = "independent/chacha20.asm"))),
            sha.copy(archives = listOf(sha.archives.single().copy(id = "026-chacha20"))),
            sha.copy(archives = listOf(sha.archives.single().copy(entry = "other.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `RSA corpus plans bind one archive and private fixture and retain fresh-process resume`() {
        val rsa = plan.copy(archives = listOf(archive.copy(id = "027-rsa-arithmetic", version = "0.1.0", entry = "worked/remainders.asm")),
            nativePredictions = true, rsaUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/rsa-arithmetic.asm", "/tmp/rsa.asm", "c".repeat(64))))
        rsa.validate()
        rsa.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(rsa.copy(nativePredictions = false), rsa.copy(nativeExecution = true),
            rsa.copy(nativeDebug = true), rsa.copy(resumeCompleted = true), rsa.copy(chachaUnit = true),
            rsa.copy(shaUnit = true), rsa.copy(cborUnit = true), rsa.copy(qoiUnit = true), rsa.copy(crcUnit = true), rsa.copy(memoryUnit = true),
            rsa.copy(rsaUnit = false), rsa.copy(activityNavigation = true), rsa.copy(prerequisiteControls = true),
            rsa.copy(assessmentFixtures = emptyList()),
            rsa.copy(assessmentFixtures = rsa.assessmentFixtures + rsa.assessmentFixtures),
            rsa.copy(assessmentFixtures = listOf(rsa.assessmentFixtures.single().copy(source = "independent/chacha20.asm"))),
            rsa.copy(archives = listOf(rsa.archives.single().copy(id = "026-chacha20"))),
            rsa.copy(archives = listOf(rsa.archives.single().copy(entry = "other.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `WAV corpus plans bind one archive and private fixture and retain fresh-process resume`() {
        val wav = plan.copy(archives = listOf(archive.copy(id = "031-pcm-wav", version = "0.1.0", entry = "worked/chunk-offsets.asm")),
            nativePredictions = true, wavUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/wav-reverse-frames.asm", "/tmp/wav.asm", "c".repeat(64))))
        wav.validate()
        wav.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(wav.copy(nativePredictions = false), wav.copy(nativeExecution = true),
            wav.copy(nativeDebug = true), wav.copy(resumeCompleted = true), wav.copy(chachaUnit = true),
            wav.copy(rsaUnit = true), wav.copy(shaUnit = true), wav.copy(cborUnit = true), wav.copy(qoiUnit = true), wav.copy(crcUnit = true), wav.copy(memoryUnit = true),
            wav.copy(wavUnit = false), wav.copy(activityNavigation = true), wav.copy(prerequisiteControls = true),
            wav.copy(assessmentFixtures = emptyList()),
            wav.copy(assessmentFixtures = wav.assessmentFixtures + wav.assessmentFixtures),
            wav.copy(assessmentFixtures = listOf(wav.assessmentFixtures.single().copy(source = "independent/chacha20.asm"))),
            wav.copy(archives = listOf(wav.archives.single().copy(id = "026-chacha20"))),
            wav.copy(archives = listOf(wav.archives.single().copy(entry = "other.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `PNG corpus plans bind one archive and private fixture and retain fresh-process resume`() {
        val png = plan.copy(archives = listOf(archive.copy(id = "033-png", version = "0.1.0", entry = "worked/pipeline-boundaries.asm")),
            nativePredictions = true, pngUnit = true,
            assessmentFixtures = listOf(BookAssessmentFixture("independent/png-filter-scanline.asm", "/tmp/png.asm", "c".repeat(64))))
        png.validate()
        png.copy(resume = true, expectedEntryDigests = mapOf(archive.sha256 to "b".repeat(64))).validate()
        for (bad in listOf(png.copy(nativePredictions = false), png.copy(nativeExecution = true),
            png.copy(nativeDebug = true), png.copy(resumeCompleted = true), png.copy(chachaUnit = true),
            png.copy(wavUnit = true), png.copy(rsaUnit = true), png.copy(shaUnit = true), png.copy(cborUnit = true), png.copy(qoiUnit = true), png.copy(crcUnit = true), png.copy(memoryUnit = true),
            png.copy(pngUnit = false), png.copy(activityNavigation = true), png.copy(prerequisiteControls = true),
            png.copy(assessmentFixtures = emptyList()),
            png.copy(assessmentFixtures = png.assessmentFixtures + png.assessmentFixtures),
            png.copy(assessmentFixtures = listOf(png.assessmentFixtures.single().copy(source = "independent/chacha20.asm"))),
            png.copy(archives = listOf(png.archives.single().copy(id = "026-chacha20"))),
            png.copy(archives = listOf(png.archives.single().copy(entry = "other.asm"))))) {
            assertThrows<IllegalArgumentException> { bad.validate() }
        }
    }

    @Test
    fun `vector photograph focus names an observed register including packed w3 only stops`() {
        assertEquals("\$w3", bookCorpusVectorFocus(setOf("\$w3")))
        assertEquals("\$w4", bookCorpusVectorFocus(setOf("\$w0", "\$w3", "\$w4")))
        assertEquals("\$w7", bookCorpusVectorFocus(setOf("\$w7", "\$w5")))
        assertEquals("\$w5", bookCorpusVectorFocus(setOf("\$w5")))
        assertEquals("\$w1", bookCorpusVectorFocus(emptySet()))
        for (index in 0..31) {
            val register = "\$w$index"
            assertEquals(register, bookCorpusVectorFocus(setOf(register)))
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
