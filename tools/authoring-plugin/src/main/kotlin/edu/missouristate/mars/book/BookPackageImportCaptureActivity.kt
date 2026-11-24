package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import java.awt.Rectangle
import java.awt.Window
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JDialog
import javax.swing.text.JTextComponent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

internal data class BookImportArchive(
    val path: String = "",
    val sha256: String = "",
    val id: String = "",
    val version: String = "",
    val entry: String = "",
    val assignment: String = "",
    val entrySha256: String = "",
    val source: String? = null,
) {
  fun validate() {
    require(Path.of(path).isAbsolute)
    source?.let {
      val uri = URI.create(it)
      require(
          uri.scheme == "https" &&
              uri.host != null &&
              uri.rawUserInfo == null &&
              uri.rawFragment == null,
      )
    }
    require(sha256.matches(Regex("[a-f0-9]{64}")) && entrySha256.matches(Regex("[a-f0-9]{64}")))
    require(id.matches(Regex("[0-9]{3}-[a-z][a-z0-9-]*")))
    require(version.matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+")))
    for (relative in listOf(entry, assignment)) {
      require(
          relative.isNotBlank() &&
              !Path.of(relative).isAbsolute &&
              Path.of(relative).normalize() == Path.of(relative),
      )
      require(!relative.startsWith(".."))
    }
  }

  fun directory(project: Path): Path = project.resolve("mars-labs/packages/$id/$version/$sha256")
}

/** Local authoring-only source fixture. Its contents never enter student distributions. */
internal data class BookAssessmentFixture(
    val source: String = "",
    val path: String = "",
    val sha256: String = "",
) {
  fun validate() {
    require(
        source in
            setOf(
                "independent/plus-one.asm",
                "independent/wrapped-byte.asm",
                "independent/red-component.asm",
                "independent/independent-copy.asm",
                "independent/end-pointer.asm",
                "independent/vm.asm",
                "independent/expression.asm",
                "independent/blit.asm",
                "independent/stream.asm",
                "independent/ring.asm",
                "independent/maze.asm",
                "independent/square.asm",
                "independent/reverse-and-invert.asm",
                "independent/vertical-sprite.asm",
                "independent/dashed-line.asm",
                "independent/life-changes.asm",
                "independent/partition-negative.asm",
                "independent/fixed-interpolate.asm",
                "independent/matrix-rotate.asm",
                "independent/bounded-line.asm",
                "independent/matrix-vector.asm",
                "independent/rgb555.asm",
                "independent/pcm-subtract.asm",
                "independent/fir-decimate.asm",
                "independent/delta-scan.asm",
                "independent/base64url.asm",
                "independent/ppm-negative.asm",
                "independent/crc32-chunk.asm",
                "independent/qoi-mirrored.asm",
                "independent/cbor-sequence.asm",
                "independent/chacha20.asm",
                "independent/sha256.asm",
                "independent/rsa-arithmetic.asm",
                "independent/wav-reverse-frames.asm",
                "independent/png-filter-scanline.asm",
                "independent/video-change-bounds.asm",
                "independent/h263-reconstruct8.asm",
                "independent/h263-read-bits.asm",
            ),
    )
    require(Path.of(path).isAbsolute && sha256.matches(Regex("[a-f0-9]{64}")))
  }
}

internal data class BookPackageImportPlan(
    val projectRoot: String = "",
    val outputDirectory: String = "",
    val archives: List<BookImportArchive> = emptyList(),
    val resume: Boolean = false,
    val expectedEntryDigests: Map<String, String> = emptyMap(),
    val closeProjectAfterCapture: Boolean = false,
    val nativePredictions: Boolean = false,
    val nativeExecution: Boolean = false,
    val nativeDebug: Boolean = false,
    val resumeCompleted: Boolean = false,
    val assessmentFixtures: List<BookAssessmentFixture> = emptyList(),
    val activityNavigation: Boolean = false,
    val resumeActivity: Boolean = false,
    val prerequisiteControls: Boolean = false,
    val resumePrerequisites: Boolean = false,
    val memoryUnit: Boolean = false,
    val loopUnit: Boolean = false,
    val functionUnit: Boolean = false,
    val spriteUnit: Boolean = false,
    val rasterUnit: Boolean = false,
    val lifeUnit: Boolean = false,
    val sortingUnit: Boolean = false,
    val fixedUnit: Boolean = false,
    val localityUnit: Boolean = false,
    val boundedUnit: Boolean = false,
    val measurementUnit: Boolean = false,
    val msaUnit: Boolean = false,
    val pcmUnit: Boolean = false,
    val firUnit: Boolean = false,
    val deltaUnit: Boolean = false,
    val base64Unit: Boolean = false,
    val ppmUnit: Boolean = false,
    val crcUnit: Boolean = false,
    val qoiUnit: Boolean = false,
    val cborUnit: Boolean = false,
    val chachaUnit: Boolean = false,
    val shaUnit: Boolean = false,
    val rsaUnit: Boolean = false,
    val wavUnit: Boolean = false,
    val pngUnit: Boolean = false,
    val losslessUnit: Boolean = false,
    val h263PredictionUnit: Boolean = false,
    val h263StreamUnit: Boolean = false,
    val marsBotUnit: Boolean = false,
    val mazeUnit: Boolean = false,
    val ringUnit: Boolean = false,
    val utf8Unit: Boolean = false,
    val clippedUnit: Boolean = false,
    val expressionUnit: Boolean = false,
    val bytecodeUnit: Boolean = false,
) {
  fun validate() {
    require(Path.of(projectRoot).isAbsolute && Path.of(outputDirectory).isAbsolute)
    require(Path.of(projectRoot).normalize() != Path.of(outputDirectory).normalize())
    require(archives.size in 1..64 && archives.map { it.sha256 }.distinct().size == archives.size)
    archives.forEach(BookImportArchive::validate)
    require(!nativePredictions || archives.size == 1)
    require(!nativeExecution || nativePredictions)
    require(!nativeDebug || nativeExecution)
    if (assessmentFixtures.isNotEmpty()) {
      require(
          nativeDebug ||
              memoryUnit ||
              loopUnit ||
              functionUnit ||
              spriteUnit ||
              rasterUnit ||
              lifeUnit ||
              sortingUnit ||
              fixedUnit ||
              localityUnit ||
              boundedUnit ||
              measurementUnit ||
              msaUnit ||
              pcmUnit ||
              firUnit ||
              deltaUnit ||
              base64Unit ||
              ppmUnit ||
              crcUnit ||
              qoiUnit ||
              cborUnit ||
              chachaUnit ||
              shaUnit ||
              rsaUnit ||
              wavUnit ||
              pngUnit ||
              losslessUnit ||
              h263PredictionUnit ||
              h263StreamUnit ||
              marsBotUnit ||
              mazeUnit ||
              ringUnit ||
              utf8Unit ||
              clippedUnit ||
              expressionUnit ||
              bytecodeUnit,
      )
      val expected =
          when {
            memoryUnit -> setOf("independent/red-component.asm", "independent/independent-copy.asm")
            loopUnit -> setOf("independent/end-pointer.asm")
            functionUnit -> setOf("independent/reverse-and-invert.asm")
            spriteUnit -> setOf("independent/vertical-sprite.asm")
            rasterUnit -> setOf("independent/dashed-line.asm")
            lifeUnit -> setOf("independent/life-changes.asm")
            sortingUnit -> setOf("independent/partition-negative.asm")
            fixedUnit -> setOf("independent/fixed-interpolate.asm")
            localityUnit -> setOf("independent/matrix-rotate.asm")
            boundedUnit -> setOf("independent/bounded-line.asm")
            measurementUnit -> setOf("independent/matrix-vector.asm")
            msaUnit -> setOf("independent/rgb555.asm")
            pcmUnit -> setOf("independent/pcm-subtract.asm")
            firUnit -> setOf("independent/fir-decimate.asm")
            deltaUnit -> setOf("independent/delta-scan.asm")
            base64Unit -> setOf("independent/base64url.asm")
            ppmUnit -> setOf("independent/ppm-negative.asm")
            crcUnit -> setOf("independent/crc32-chunk.asm")
            qoiUnit -> setOf("independent/qoi-mirrored.asm")
            cborUnit -> setOf("independent/cbor-sequence.asm")
            chachaUnit -> setOf("independent/chacha20.asm")
            shaUnit -> setOf("independent/sha256.asm")
            rsaUnit -> setOf("independent/rsa-arithmetic.asm")
            wavUnit -> setOf("independent/wav-reverse-frames.asm")
            pngUnit -> setOf("independent/png-filter-scanline.asm")
            losslessUnit -> setOf("independent/video-change-bounds.asm")
            h263PredictionUnit -> setOf("independent/h263-reconstruct8.asm")
            h263StreamUnit -> setOf("independent/h263-read-bits.asm")
            bytecodeUnit -> setOf("independent/vm.asm")
            expressionUnit -> setOf("independent/expression.asm")
            clippedUnit -> setOf("independent/blit.asm")
            utf8Unit -> setOf("independent/stream.asm")
            ringUnit -> setOf("independent/ring.asm")
            mazeUnit -> setOf("independent/maze.asm")
            marsBotUnit -> setOf("independent/square.asm")
            else -> setOf("independent/plus-one.asm", "independent/wrapped-byte.asm")
          }
      require(assessmentFixtures.map { it.source }.toSet() == expected)
      require(assessmentFixtures.size == expected.size)
      assessmentFixtures.forEach(BookAssessmentFixture::validate)
    }
    require(
        listOf(
                memoryUnit,
                loopUnit,
                functionUnit,
                spriteUnit,
                rasterUnit,
                lifeUnit,
                sortingUnit,
                fixedUnit,
                localityUnit,
                boundedUnit,
                measurementUnit,
                msaUnit,
                pcmUnit,
                firUnit,
                deltaUnit,
                base64Unit,
                ppmUnit,
                crcUnit,
                qoiUnit,
                cborUnit,
                chachaUnit,
                shaUnit,
                rsaUnit,
                wavUnit,
                pngUnit,
                losslessUnit,
                h263PredictionUnit,
                h263StreamUnit,
                marsBotUnit,
                mazeUnit,
                ringUnit,
                utf8Unit,
                clippedUnit,
                expressionUnit,
                bytecodeUnit,
            )
            .count { it } <= 1,
    )
    if (bytecodeUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "018-bytecode-machine" &&
              archives.single().entry == "worked/record.asm",
      )
    }
    if (expressionUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "017-expression-order" &&
              archives.single().entry == "worked/arithmetic.asm",
      )
    }
    if (clippedUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "016-clipped-regions" &&
              archives.single().entry == "worked/interval.asm",
      )
    }
    if (utf8Unit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "015-utf8-streams" &&
              archives.single().entry == "worked/range.asm",
      )
    }
    if (ringUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "014-ring-records" &&
              archives.single().entry == "worked/cursor.asm",
      )
    }
    if (mazeUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "010-maze-frontier" &&
              archives.single().entry == "worked/neighbor.asm",
      )
    }
    if (marsBotUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "006-marsbot-feedback" &&
              archives.single().entry == "worked/east.asm",
      )
    }
    if (h263StreamUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "035-h263-stream" &&
              archives.single().entry == "worked/append-fields.asm",
      )
    }
    if (h263PredictionUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "035-h263-prediction" &&
              archives.single().entry == "worked/sad-windows.asm",
      )
    }
    if (losslessUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "034-lossless-video" &&
              archives.single().entry == "worked/xor-alias-tail.asm",
      )
    }
    if (pngUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "033-png" &&
              archives.single().entry == "worked/pipeline-boundaries.asm",
      )
    }
    if (wavUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "031-pcm-wav" &&
              archives.single().entry == "worked/chunk-offsets.asm",
      )
    }
    if (rsaUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "027-rsa-arithmetic" &&
              archives.single().entry == "worked/remainders.asm",
      )
    }
    if (shaUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "025-sha256" && archives.single().entry == "worked/schedule.asm",
      )
    }
    if (chachaUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(archives.single().id == "026-chacha20" && archives.single().entry == "chacha20.asm")
    }
    if (cborUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "032-cbor-telemetry" &&
              archives.single().entry == "cbor-telemetry.asm",
      )
    }
    if (qoiUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(archives.single().id == "030-qoi" && archives.single().entry == "qoi-codec.asm")
    }
    if (crcUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(archives.single().id == "024-crc32" && archives.single().entry == "crc32.asm")
    }
    if (ppmUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "029-binary-ppm" && archives.single().entry == "ppm-codec.asm",
      )
    }
    if (base64Unit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(archives.single().id == "028-base64" && archives.single().entry == "base64-codec.asm")
    }
    if (deltaUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "023-delta-varint" &&
              archives.single().entry == "delta-codec.asm",
      )
    }
    if (firUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "021-fixed-point-fir" &&
              archives.single().entry == "fir-filter.asm",
      )
    }
    if (pcmUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(archives.single().id == "020-pcm-mixing" && archives.single().entry == "pcm-mix.asm")
    }
    if (msaUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "019-working-with-msa" &&
              archives.single().entry == "rgb-formats.asm",
      )
    }
    if (measurementUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "022-measuring-programs" &&
              archives.single().entry == "matrix-measure.asm",
      )
    }
    if (boundedUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "013-bounded-interfaces" &&
              archives.single().entry == "bounded-packet.asm",
      )
    }
    if (localityUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "011-memory-locality" &&
              archives.single().entry == "matrix-locality.asm",
      )
    }
    if (fixedUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "012-fixed-point" &&
              archives.single().entry == "fixed-point-ball.asm",
      )
    }
    if (sortingUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "009-sorting-costs" &&
              archives.single().entry == "sorting-costs.asm",
      )
    }
    if (lifeUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "008-life-buffers" &&
              archives.single().entry == "life-buffers.asm",
      )
    }
    if (rasterUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "007-integer-raster" &&
              archives.single().entry == "integer-line.asm",
      )
    }
    if (spriteUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "003-sprite-rows" && archives.single().entry == "sprite-rows.asm",
      )
    }
    if (functionUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "003-row-functions" &&
              archives.single().entry == "reverse-rows.asm",
      )
    }
    if (memoryUnit) {
      require(nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted)
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 2)
      require(
          archives.single().id == "002-single-pixel" &&
              archives.single().entry == "memory-pixel.asm",
      )
    }
    if (loopUnit) {
      require(
          nativePredictions && !nativeExecution && !nativeDebug && !resumeCompleted && !memoryUnit,
      )
      require(!activityNavigation && !prerequisiteControls && assessmentFixtures.size == 1)
      require(
          archives.single().id == "005-seven-segment-counter" &&
              archives.single().entry == "range-counter.asm",
      )
    }
    require(!resumeCompleted || (resume && nativeDebug))
    require(!activityNavigation || (resumeCompleted && assessmentFixtures.size == 2))
    require(!resumeActivity || activityNavigation)
    require(
        !prerequisiteControls ||
            (resumeCompleted && assessmentFixtures.size == 2 && !activityNavigation),
    )
    require(!resumePrerequisites || prerequisiteControls)
    if (resume) {
      require(expectedEntryDigests.keys == archives.map { it.sha256 }.toSet())
      require(expectedEntryDigests.values.all { it.matches(Regex("[a-f0-9]{64}")) })
    } else require(expectedEntryDigests.isEmpty())
  }
}

/**
 * Drives production actions and normal Run/Debug only in the explicitly configured authoring
 * project.
 */
class BookPackageImportCaptureActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    val location = System.getProperty("mars.book.package.import.plan") ?: return
    val plan =
        withContext(Dispatchers.IO) {
          GsonBuilder()
              .create()
              .fromJson(Files.readString(Path.of(location)), BookPackageImportPlan::class.java)
        }
    plan.validate()
    if (
        Path.of(checkNotNull(project.basePath)).toRealPath() !=
            Path.of(plan.projectRoot).toRealPath()
    )
        return
    val output = Path.of(plan.outputDirectory)
    withContext(Dispatchers.IO) { Files.createDirectory(output) }
    try {
      withTimeout(600_000) {
        withContext(Dispatchers.EDT) { configureBookPresentation(project) }
        val ready = CompletableDeferred<Unit>()
        withContext(Dispatchers.EDT) {
          ToolWindowManager.getInstance(project).invokeLater { ready.complete(Unit) }
        }
        ready.await()
        capture(project, plan, output)
      }
      if (plan.closeProjectAfterCapture) closeBookCaptureProject(project, output)
    } catch (error: Exception) {
      withContext(NonCancellable + Dispatchers.IO) {
        Files.writeString(output.resolve("failed.txt"), error.stackTraceToString(), CREATE_NEW)
      }
      throw error
    }
  }

  private suspend fun capture(project: Project, plan: BookPackageImportPlan, output: Path) {
    val root = Path.of(plan.projectRoot)
    for (archive in plan.archives) withContext(Dispatchers.IO) {
      check(captureSha256(Files.readAllBytes(Path.of(archive.path))) == archive.sha256)
    }
    val first = plan.archives.first()
    val firstEntry = first.directory(root).resolve(first.entry)
    if (plan.nativePredictions) {
      if (plan.resume) verifyEntries(plan, root, plan.expectedEntryDigests)
      else importArchive(project, first, output, "native", capture = true)
      captureBookNativePredictions(project, plan, output)
      return
    }
    if (plan.resume) {
      verifyEntries(plan, root, plan.expectedEntryDigests)
      importArchive(project, first, output, "reopened", capture = true)
      verifyEntries(plan, root, plan.expectedEntryDigests)
    } else {
      check(!withContext(Dispatchers.IO) { Files.exists(root.resolve("mars-labs/packages")) })
      importArchive(
          project,
          first,
          output,
          "cancel-selection",
          cancelSelection = true,
          capture = true,
      )
      check(!withContext(Dispatchers.IO) { Files.exists(root.resolve("mars-labs/packages")) })
      importArchive(project, first, output, "cancel-preview", cancelPreview = true, capture = true)
      check(!withContext(Dispatchers.IO) { Files.exists(root.resolve("mars-labs/packages")) })
      for ((index, archive) in plan.archives.withIndex()) {
        importArchive(
            project,
            archive,
            output,
            "import-${index + 1}",
            capture = index == 0 || index == plan.archives.lastIndex,
        )
        withContext(Dispatchers.IO) {
          check(
              captureSha256(Files.readAllBytes(archive.directory(root).resolve(archive.entry))) ==
                  archive.entrySha256,
          )
        }
      }
      // A normal student edit, not a completed solution or progress injection.
      val file =
          withContext(Dispatchers.IO) {
            checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(firstEntry))
          }
      withContext(Dispatchers.EDT) {
        val document = checkNotNull(FileDocumentManager.getInstance().getDocument(file))
        WriteCommandAction.runWriteCommandAction(project) {
          document.insertString(0, "# Student notes survive importing the same archive.\n")
        }
        FileDocumentManager.getInstance().saveDocument(document)
      }
      val edited = withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(firstEntry)) }
      importArchive(project, first, output, "reimport", capture = true)
      check(withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(firstEntry)) } == edited)
      runAndDebug(project, firstEntry, output)
    }
    val digests =
        withContext(Dispatchers.IO) {
          plan.archives.associate {
            it.sha256 to captureSha256(Files.readAllBytes(it.directory(root).resolve(it.entry)))
          }
        }
    withContext(Dispatchers.IO) {
      Files.writeString(
          output.resolve("complete.json"),
          GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  mapOf(
                      "archives" to plan.archives,
                      "entryDigests" to digests,
                      "resumed" to plan.resume,
                      "archiveCount" to plan.archives.size,
                      "uniqueProjectCount" to plan.archives.map { it.id }.distinct().size,
                      "editsPreserved" to true,
                      "cancelledImportsCreateNoWorkingCopy" to !plan.resume,
                      "runAndDebugExercised" to !plan.resume,
                      "marsPluginArchiveSha256" to
                          captureSha256(
                              Files.readAllBytes(
                                  Path.of(
                                      checkNotNull(System.getProperty("mars.book.mars.archive")),
                                  ),
                              ),
                          ),
                      "scope" to
                          "Actual native archive preview/import and edited-file preservation; public distribution files are not interpreted as native trainer steps and included grading scripts are not run",
                  ),
              ),
          CREATE_NEW,
      )
    }
  }

  private suspend fun verifyEntries(
      plan: BookPackageImportPlan,
      root: Path,
      digests: Map<String, String>,
  ) =
      withContext(Dispatchers.IO) {
        for (archive in plan.archives) check(
            captureSha256(Files.readAllBytes(archive.directory(root).resolve(archive.entry))) ==
                digests.getValue(archive.sha256),
        )
      }

  private suspend fun importArchive(
      project: Project,
      archive: BookImportArchive,
      output: Path,
      prefix: String,
      cancelSelection: Boolean = false,
      cancelPreview: Boolean = false,
      capture: Boolean = false,
  ) {
    val finished = CompletableDeferred<Unit>()
    val failure = AtomicReference<Throwable>()
    withContext(Dispatchers.EDT) {
      val action =
          checkNotNull(ActionManager.getInstance().getAction("Mars.Learning.ImportPackage"))
      val event =
          AnActionEvent.createEvent(
              action,
              SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build(),
              null,
              ActionPlaces.MAIN_MENU,
              ActionUiKind.NONE,
              null,
          )
      check(ActionUtil.updateAction(action, event).isPerformed && event.presentation.isEnabled)
      ApplicationManager.getApplication()
          .invokeLater(
              {
                try {
                  ActionUtil.performAction(action, event)
                  finished.complete(Unit)
                } catch (error: Throwable) {
                  failure.set(error)
                  finished.completeExceptionally(error)
                }
              },
              ModalityState.nonModal(),
          )
    }
    val choose = dialog("Open a MARS lab package", failure)
    bookModalUi {
      val field =
          captureComponents(choose.rootPane).filterIsInstance<JTextComponent>().single {
            it.isShowing && it.accessibleContext.accessibleName == "Lab link or ZIP archive:"
          }
      field.text = archive.source ?: archive.path
      field.caretPosition = 0
      choose.rootPane.paintImmediately(choose.rootPane.visibleRect)
    }
    delay(200) // Wait for the actual screen paint before the Robot capture.
    if (capture)
        captureBookComponent(choose.rootPane, output, "$prefix-choose", "Lab link or ZIP archive:")
    bookModalUi {
      visibleLessonButton(choose.rootPane, if (cancelSelection) "Cancel" else "Preview lab")
          .doClick()
    }
    if (!cancelSelection) {
      val preview = dialog("Import this MARS lab?", failure)
      val text = bookModalUi { lessonVisibleText(preview.rootPane) }
      check(
          text.contains("Package: ${archive.id}") &&
              text.contains("Version: ${archive.version}") &&
              text.contains(archive.sha256),
      )
      if (capture) captureBookComponent(preview.rootPane, output, "$prefix-preview", archive.sha256)
      bookModalUi {
        visibleLessonButton(preview.rootPane, if (cancelPreview) "Cancel" else "Import lab")
            .doClick()
      }
    }
    finished.await()
    if (!cancelSelection && !cancelPreview) {
      val expected =
          archive.directory(Path.of(checkNotNull(project.basePath))).resolve(archive.entry)
      while (
          !bookModalUi {
            val manager = FileEditorManager.getInstance(project)
            manager.selectedTextEditor?.let {
              FileDocumentManager.getInstance().getFile(it.document)?.toNioPath() == expected
            } == true
          }
      ) delay(100)
      withContext(Dispatchers.IO) {
        val directory = archive.directory(Path.of(checkNotNull(project.basePath)))
        check(Files.readString(directory.resolve(".mars-package.sha256")) == archive.sha256 + "\n")
        check(
            captureSha256(Files.readAllBytes(directory.resolve(".mars-package.zip"))) ==
                archive.sha256,
        )
        check(Files.isRegularFile(directory.resolve(archive.assignment)))
        Files.writeString(
            output.resolve("$prefix-import.json"),
            GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(
                    mapOf(
                        "archive" to archive,
                        "selectedEntry" to expected.toString(),
                        "nativePreviewMatched" to true,
                    ),
                ),
            CREATE_NEW,
        )
      }
    }
  }

  private suspend fun dialog(title: String, failure: AtomicReference<Throwable>): JDialog {
    while (true) {
      failure.get()?.let { throw it }
      val found = bookModalUi {
        Window.getWindows().filterIsInstance<JDialog>().singleOrNull {
          it.isShowing && it.title == title
        }
      }
      if (found != null) {
        delay(500)
        val first = bookModalUi { Rectangle(found.locationOnScreen, found.size) }
        delay(200)
        if (
            bookModalUi {
              found.isShowing && Rectangle(found.locationOnScreen, found.size) == first
            }
        )
            return found
      }
      delay(100)
    }
  }

  private suspend fun runAndDebug(project: Project, source: Path, output: Path) {
    val run = launchBookProgram(project, source, debug = false)
    val runHandler = run.started.await()
    check(run.exit.await() == 0)
    check(run.stdout.toString().isEmpty())
    check(runHandler.isProcessTerminated)
    run.close()
    val file =
        withContext(Dispatchers.IO) {
          checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(source))
        }
    val line =
        withContext(Dispatchers.IO) {
          Files.readString(source).lines().indexOfFirst { it.trim() == "li \$v0, 10" }
        }
    check(line >= 0)
    val type =
        XBreakpointType.EXTENSION_POINT_NAME.extensionList
            .filterIsInstance<XLineBreakpointType<*>>()
            .single { it.id == "MarsMipsLine" }
    val breakpoint = writeAction {
      XDebuggerManager.getInstance(project)
          .breakpointManager
          .addLineBreakpoint(type, file.url, line, null)
    }
    val debug = launchBookProgram(project, source, debug = true)
    try {
      val handler = debug.started.await()
      val session =
          withContext(Dispatchers.EDT) {
            XDebuggerManager.getInstance(project).debugSessions.single {
              it.debugProcess.processHandler === handler
            }
          }
      val observations = mutableListOf<Map<String, Any>>()
      for ((index, value) in listOf(0L, 10L).withIndex()) {
        check(debug.pauses.receive() == line + index)
        awaitBookDebugRegisters(project, handler, mapOf("\$v0" to value))
        delay(300) // Native variable-model updates can precede their Swing paint.
        val rows = awaitBookDebugRegisters(project, handler, mapOf("\$v0" to value))
        val frame =
            withContext(Dispatchers.EDT) {
              checkNotNull(WindowManager.getInstance().getFrame(project)).rootPane.also {
                it.paintImmediately(it.visibleRect)
              }
            }
        delay(100)
        captureBookComponent(
            frame,
            output,
            "imported-debug-$index",
            "Native imported source; v0=$value",
        ) {
          session.topFramePosition?.file?.toNioPath() == source &&
              session.topFramePosition?.line == line + index
        }
        observations += mapOf("line" to line + index + 1, "v0" to value, "renderedRows" to rows)
        withContext(Dispatchers.EDT) {
          if (index == 0) session.stepOver(false) else session.resume()
        }
      }
      check(debug.exit.await() == 0)
      withContext(Dispatchers.IO) {
        Files.writeString(
            output.resolve("execution.json"),
            GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(
                    mapOf(
                        "source" to source.toString(),
                        "runExit" to 0,
                        "runStdout" to "",
                        "debugExit" to 0,
                        "debugObservations" to observations,
                        "scope" to
                            "Normal Run/Debug of the unfinished imported starter's exit sequence, not completed submission grading",
                    ),
                ),
            CREATE_NEW,
        )
      }
    } finally {
      debug.close()
      withContext(NonCancellable + Dispatchers.EDT) {
        debug.session?.stop()
        writeAction {
          XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(breakpoint)
        }
      }
    }
  }
}
