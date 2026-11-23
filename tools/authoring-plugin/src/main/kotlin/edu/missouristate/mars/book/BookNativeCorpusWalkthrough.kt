package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import edu.missouristate.mars.plugin.language.MipsTypes
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XDebuggerUtil
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import training.learn.lesson.LessonManager

/** Separate publication automation: read the pinned lesson and operate real student controls. */
internal suspend fun captureBookNativeCorpus(
    project: Project, plan: BookPackageImportPlan, output: Path,
    awaitText: suspend (String) -> Unit, capture: suspend (String, String) -> Unit,
    click: suspend (String, Boolean) -> Unit, focus: suspend (String, String) -> Unit,
) {
    val capturePrefix = when { plan.clippedUnit -> "clipped"; plan.utf8Unit -> "utf8"; plan.ringUnit -> "ring"; plan.mazeUnit -> "maze"; plan.marsBotUnit -> "marsbot"; plan.h263StreamUnit -> "h263-stream"; plan.h263PredictionUnit -> "h263-prediction"; plan.losslessUnit -> "lossless"; plan.pngUnit -> "png"; plan.wavUnit -> "wav"; plan.rsaUnit -> "rsa"; plan.shaUnit -> "sha"; plan.chachaUnit -> "chacha"; plan.cborUnit -> "cbor"; plan.qoiUnit -> "qoi"; plan.crcUnit -> "crc"; plan.ppmUnit -> "ppm"; else -> "base64" }
    val firstAssessmentId = when { plan.clippedUnit -> "check-empty"; plan.utf8Unit -> "check-stream"; plan.ringUnit -> "check-push"; plan.mazeUnit -> "check-render"; plan.marsBotUnit -> "check-square"; plan.h263StreamUnit -> "check-short-fields"; plan.h263PredictionUnit -> "check-samples"; plan.losslessUnit -> "check-equality"; plan.pngUnit -> "check-none"; plan.wavUnit -> "check-mono"; plan.rsaUnit -> "complete-gcd"; plan.shaUnit -> "complete-schedule"; plan.chachaUnit -> "complete-quarter"; plan.cborUnit -> "sequence-depth"; plan.qoiUnit -> "mirror-geometry"; plan.crcUnit -> "check-short-chunks"; plan.ppmUnit -> "negative-small-width"; else -> "url-short" }
    val archive = plan.archives.single()
    val directory = archive.directory(Path.of(plan.projectRoot))
    // Production MARS has already validated/imported this exact archive and every activity.
    // This trusted authoring reader follows those activities; it does not validate student content.
    val lesson = withContext(Dispatchers.IO) { JsonParser.parseString(Files.readString(directory.resolve("lesson.json"))).asJsonObject }
    val steps = lesson.getAsJsonArray("steps").map { it.asJsonObject }
    val receipts = mutableListOf<Map<String, Any>>()
    suspend fun record(id: String, facts: Map<String, Any>) {
        receipts += mapOf("id" to id) + facts
        withContext(Dispatchers.IO) {
            Files.writeString(output.resolve("$id-observation.json"), GsonBuilder().setPrettyPrinting().create().toJson(facts), CREATE_NEW)
        }
    }
    suspend fun continueLesson() = withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
        val action = checkNotNull(ActionManager.getInstance().getAction("Mars.Learning.ContinueDownloaded"))
        val event = AnActionEvent.createEvent(action, SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build(),
            null, ActionPlaces.MAIN_MENU, ActionUiKind.NONE, null)
        check(ActionUtil.updateAction(action, event).isPerformed && event.presentation.isEnabled)
        ActionUtil.performAction(action, event)
    }
    suspend fun selected(path: Path) {
        while (!bookModalUi {
            FileEditorManager.getInstance(project).selectedTextEditor?.document?.let {
                FileDocumentManager.getInstance().getFile(it)?.toNioPath() == path
            } == true
        }) delay(100)
    }
    fun visible(step: JsonObject): String = step.get(if (step.get("kind").asString == "prediction") "question" else "explanation").asString
    fun nextText(index: Int) = steps.drop(index + 1).firstOrNull { !plan.resume || it.get("kind").asString != "prediction" }
        ?.let(::visible) ?: lesson.get("conclusion").asString
    if (plan.resume) continueLesson()
    val fixture = plan.assessmentFixtures.single()
    val bytes = withContext(Dispatchers.IO) { Files.readAllBytes(Path.of(fixture.path)) }
    check(captureSha256(bytes) == fixture.sha256) { "Changed private authoring fixture" }
    var firstPrediction = true
    for ((index, step) in steps.withIndex()) {
        val id = step.get("id").asString
        val kind = step.get("kind").asString
        val relative = step.get("source")?.asString ?: archive.entry
        val path = directory.resolve(relative)
        when (kind) {
            "prediction" -> if (!plan.resume) {
                val question = step.get("question").asString
                awaitText(question); selected(path)
                focus(id, step.getAsJsonObject("anchor").get("mnemonic").asString)
                capture(id, question)
                val choices = step.getAsJsonArray("choices").map { it.asJsonObject }
                val correct = choices.single { it.get("id").asString == step.get("correctChoice").asString }.get("text").asString
                val wrong = if (firstPrediction) choices.first { it.get("text").asString != correct }.get("text").asString else null
                if (wrong != null) click(wrong, true)
                click(correct, true)
                record(id, mapOf("actualInlineChoiceClicked" to correct, "wrongChoiceFirst" to (wrong ?: "none")))
                firstPrediction = false
            }
            "debug" -> {
                awaitText(visible(step)); selected(path)
                val rawStops = step.getAsJsonArray("stops").map { it.asJsonObject }
                val stops = rawStops.mapIndexed { stopIndex, stop ->
                    val label = stop.getAsJsonObject("anchor").get("label").asString
                    val registers = stop.getAsJsonObject("registers").entrySet().associate { (register, value) ->
                        register to value.asString.removePrefix("0x").toULong(16).toLong()
                    }
                    CorpusPause(label, stop.getAsJsonObject("anchor").get("mnemonic").asString, stop.getAsJsonObject("anchor").get("occurrence").asInt,
                        registers,
                        rawStops.getOrNull(stopIndex + 1)?.get("explanation")?.asString ?: nextText(index),
                        stop.getAsJsonObject("vectorRegisters")?.entrySet()?.associate { (name, value) -> name to value.asString } ?: emptyMap(),
                        bookRingCondition(plan.ringUnit, label, registers) ?: bookUtf8Condition(plan.utf8Unit, label, registers))
                }
                val oldOutput = if (plan.ppmUnit && id == "trace-save-invalid") withContext(Dispatchers.IO) {
                    Files.readAllBytes(directory.resolve("worked/output.ppm"))
                } else null
                val expectedOutput = steps.singleOrNull { it.get("kind").asString == "run" && it.get("source")?.asString == relative }?.get("stdout")?.asString ?: ""
                captureCorpusDebug(project, path, ::record, awaitText, capture, id, stops, expectedOutput,
                    if (plan.marsBotUnit) { stopIndex ->
                        val short = relative == "worked/east-slow.asm"
                        val view = BookMarsBotView(if (short) 2 else 16, 0, 90, moving = stopIndex == 0,
                            penDown = stopIndex < 2, instructionsPerTick = if (short) 128 else 64,
                            trailVertices = listOf(BookBotPoint(), BookBotPoint(if (short) 2 else 16, 0)))
                        captureCorpusBotStatus(project, "Book $id Debug", view)
                    } else null)
                if (oldOutput != null) {
                    val after = withContext(Dispatchers.IO) { Files.readAllBytes(directory.resolve("worked/output.ppm")) }
                    check(after.contentEquals(oldOutput)) { "Invalid source changed the existing host output" }
                    record("invalid-save-host-file", mapOf("unchanged" to true, "bytes" to after.size,
                        "sha256" to captureSha256(after)))
                }
            }
            "run" -> {
                awaitText(visible(step)); selected(path)
                val run = launchBookProgram(project, path, debug = false)
                try {
                    run.started.await(); check(run.exit.await() == 0 && run.stdout.toString() == step.get("stdout").asString)
                    record(id, mapOf("source" to relative, "exitCode" to 0, "stdout" to run.stdout.toString()))
                } finally { run.close() }
                if (plan.ppmUnit && id == "run-pipeline") {
                    val input = withContext(Dispatchers.IO) { Files.readAllBytes(directory.resolve("worked/input.ppm")) }
                    val result = withContext(Dispatchers.IO) { Files.readAllBytes(directory.resolve("worked/output.ppm")) }
                    check(input.size == 23 && result.contentEquals(input)) { "Host PPM pipeline bytes differ" }
                    record("pipeline-host-file", mapOf("bytes" to result.size, "inputSha256" to captureSha256(input),
                        "outputSha256" to captureSha256(result), "exactCanonicalBytes" to true))
                }
                capture("after-$id", "")
            }
            "assessment" -> {
                awaitText(visible(step)); selected(path)
                val profilePaths = step.getAsJsonArray("caseProfiles")?.map { it.asString }
                    ?: listOf(step.get("caseProfile").asString)
                val caseCount = withContext(Dispatchers.IO) {
                    profilePaths.sumOf { relativeProfile ->
                        JsonParser.parseString(Files.readString(directory.resolve(relativeProfile))).asJsonObject.getAsJsonArray("cases").size()
                    }
                }
                if (id == firstAssessmentId) {
                    if (!plan.resume) {
                        click("Check this program", false)
                        capture("$capturePrefix-starter", "Public cases passed: 0 of $caseCount.")
                        withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
                            val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
                            check(FileDocumentManager.getInstance().getFile(editor.document)?.toNioPath() == path)
                            WriteCommandAction.runWriteCommandAction(project) { editor.document.setText(bytes.toString(Charsets.UTF_8)) }
                        }
                    } else check(withContext(Dispatchers.IO) { Files.readString(path) } == "# Recheck this edited independent source.\n" + bytes.toString(Charsets.UTF_8))
                    click("Check this program", false)
                    capture("$capturePrefix-passed", step.get("success").asString)
                    record(id, mapOf("source" to relative, "solutionSha256" to fixture.sha256, "completedPublicCases" to caseCount,
                        "starterPassedPublicCases" to if (plan.resume) "not rerun" else "0 of $caseCount", "checkedUnsavedText" to !plan.resume))
                } else {
                    click("Check this program", false)
                    capture("$capturePrefix-$id-passed", step.get("success").asString)
                    record(id, mapOf("source" to relative, "completedPublicCases" to caseCount))
                }
            }
            else -> error("Unexpected corpus activity: $kind")
        }
    }
    withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) { FileDocumentManager.getInstance().saveAllDocuments() }
    val conclusion = lesson.get("conclusion").asString
    awaitText(conclusion)
    while (bookModalUi { LessonManager.instance.lessonIsRunning() }) delay(100)
    capture("$capturePrefix-unit-complete", conclusion)
    val sources = (listOf(archive.entry) + steps.filter { it.get("kind").asString in setOf("debug", "run") }
        .map { it.get("source")?.asString ?: archive.entry } + fixture.source).distinct()
    if (!plan.resume) {
        for (relative in sources) {
            val path = directory.resolve(relative)
            val note = if (relative == fixture.source) "Recheck this edited independent source." else "Recheck this edited worked source."
            val file = withContext(Dispatchers.IO) { checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)) }
            withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
                val document = checkNotNull(FileDocumentManager.getInstance().getDocument(file))
                WriteCommandAction.runWriteCommandAction(project) { document.insertString(0, "# $note\n") }
                FileDocumentManager.getInstance().saveDocument(document)
            }
        }
        continueLesson()
        capture("$capturePrefix-source-invalidated", steps.first { it.get("kind").asString == "debug" }.get("explanation").asString)
    }
    val digests = withContext(Dispatchers.IO) { sources.associateWith { captureSha256(Files.readAllBytes(directory.resolve(it))) } }
    val running = bookModalUi { LessonManager.instance.lessonIsRunning() }
    withContext(Dispatchers.IO) {
        Files.writeString(output.resolve("complete.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
            "resumed" to plan.resume, "entryDigests" to mapOf(archive.sha256 to digests.getValue(archive.entry)),
            "sourceDigests" to digests, "observations" to receipts, "lessonRunningAtCaptureEnd" to running,
            "marsPluginArchiveSha256" to captureSha256(Files.readAllBytes(Path.of(checkNotNull(System.getProperty("mars.book.mars.archive"))))),
            "scope" to "Actual inline predictions, ordered source-bound Debug observations, ordinary Runs and independent function/caller/exit checks. Human learner review remains separate.",
        )), CREATE_NEW)
    }
}

private data class CorpusPause(val label: String, val mnemonic: String, val occurrence: Int, val registers: Map<String, Long>, val nextText: String, val vectors: Map<String, String>, val condition: String?)

private suspend fun captureCorpusDebug(
    project: Project, entry: Path, record: suspend (String, Map<String, Any>) -> Unit,
    awaitText: suspend (String) -> Unit, capture: suspend (String, String) -> Unit,
    prefix: String, stops: List<CorpusPause>, expectedOutput: String,
    deviceObservation: (suspend (Int) -> Map<String, Any>)? = null,
) {
    var targetLine = corpusBreakpointLine(project, entry, stops.first())
    val file = withContext(Dispatchers.IO) { checkNotNull(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(entry)) }
    val type = XBreakpointType.EXTENSION_POINT_NAME.extensionList.filterIsInstance<XLineBreakpointType<*>>().single { it.id == "MarsMipsLine" }
    val manager = XDebuggerManager.getInstance(project).breakpointManager
    fun addBreakpoint(line: Int, condition: String?) = manager.addLineBreakpoint(type, file.url, line, null).also { breakpoint ->
        condition?.let {
            breakpoint.conditionExpression = XDebuggerUtil.getInstance().createExpression(it, null, null, EvaluationMode.EXPRESSION)
        }
    }
    val breakpoints = writeAction { mutableListOf(addBreakpoint(targetLine, stops.first().condition)) }
    val name = "Book $prefix Debug"
    val launch = launchBookProgram(project, entry, debug = true, name = name)
    try {
        val handler = launch.started.await(); val session = checkNotNull(launch.session)
        for ((index, stop) in stops.withIndex()) {
            if (index > 0) {
                targetLine = corpusBreakpointLine(project, entry, stop)
                writeAction {
                    breakpoints.forEach(manager::removeBreakpoint); breakpoints.clear()
                    breakpoints += addBreakpoint(targetLine, stop.condition)
                }
                bookModalUi { session.resume() }
            }
            check(launch.pauses.receive() == targetLine) { "Wrong lesson stop: ${stop.label}" }
            val registers = awaitBookDebugRegisters(project, handler, stop.registers, stop.vectors,
                bookCorpusVectorFocus(stop.vectors.keys), 8)
            awaitText(stop.nextText)
            val facts = mutableMapOf<String, Any>("source" to entry.toString(), "line" to targetLine + 1,
                "renderedRegisters" to registers, "expectedVectorBits" to stop.vectors, "nativeNextStepObserved" to stop.nextText)
            val actualCondition = bookModalUi { breakpoints.single().conditionExpression?.expression }
            check(actualCondition == stop.condition) { "Native breakpoint condition changed" }
            facts["nativeBreakpointCondition"] = actualCondition ?: "none"
            deviceObservation?.let { facts["actualMarsBotStatus"] = it(index) }
            record("debug-$prefix-$index", facts)
            capture("debug-$prefix-$index", stop.nextText)
        }
        writeAction { breakpoints.forEach(manager::removeBreakpoint); breakpoints.clear() }
        bookModalUi { session.resume() }
        check(launch.exit.await() == 0 && launch.stdout.toString() == expectedOutput)
    } finally {
        withContext(NonCancellable + Dispatchers.EDT) {
            launch.session?.stop()
            writeAction { breakpoints.forEach(manager::removeBreakpoint) }
        }
        launch.close()
    }
}

/** Resolve trusted fixture breakpoints through the same public PSI shapes used by MARS. */
private suspend fun corpusBreakpointLine(project: Project, entry: Path, stop: CorpusPause): Int {
    while (true) {
        val line = withContext(Dispatchers.EDT + ModalityState.nonModal().asContextElement()) {
            runReadActionBlocking {
                val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@runReadActionBlocking null
                if (FileDocumentManager.getInstance().getFile(editor.document)?.toNioPath() != entry) return@runReadActionBlocking null
                val manager = PsiDocumentManager.getInstance(project)
                if (!manager.isCommitted(editor.document)) return@runReadActionBlocking null
                val file = checkNotNull(manager.getPsiFile(editor.document))
                val labels = mutableListOf<PsiElement>()
                val instructions = mutableListOf<PsiElement>()
                file.accept(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        when (element.node.elementType) {
                            MipsTypes.psi("LABEL") -> labels += element
                            MipsTypes.psi("INSTRUCTION") -> instructions += element
                        }
                        super.visitElement(element)
                    }
                })
                val label = labels.single { (it as? PsiNamedElement)?.name == stop.label }
                val end = labels.firstOrNull { it.textOffset > label.textOffset }?.textOffset ?: file.textLength
                val instruction = instructions.filter {
                    it.textRange.startOffset >= label.textRange.endOffset && it.textRange.endOffset <= end &&
                        it.firstChild.firstChild.text.equals(stop.mnemonic, ignoreCase = true)
                }[stop.occurrence]
                editor.document.getLineNumber(instruction.textOffset)
            }
        }
        if (line != null) return line
        delay(50)
    }
}

/** Focus an actually observed bank row while preserving the earlier capture preferences. */
internal fun bookCorpusVectorFocus(registers: Set<String>): String =
    listOf("\$w4", "\$w7", "\$w5", "\$w3", "\$w1").firstOrNull { it in registers }
        ?: registers.firstOrNull() ?: "\$w1"

/** Device flags are read from the actual native tool; short ink hidden by the marker is not a trail proof. */
private suspend fun captureCorpusBotStatus(project: Project, execution: String, view: BookMarsBotView): Map<String, Any> {
    while (true) {
        val result = bookModalUi {
            val tool = ToolWindowManager.getInstance(project).getToolWindow("MARS Tools") ?: return@bookModalUi null
            if (!tool.isVisible) { tool.show(); return@bookModalUi null }
            val content = tool.contentManager.contents.singleOrNull { it.displayName == "MarsBot" } ?: return@bookModalUi null
            if (tool.contentManager.selectedContent != content) { tool.contentManager.setSelectedContent(content, false); return@bookModalUi null }
            val status = observeBookBotStatus(content.component, view, execution) ?: return@bookModalUi null
            mapOf<String, Any>("expected" to view, "selectedExecution" to status.selectedExecution, "rows" to status.rows,
                "scope" to "Actual native tool labels, movement rate and selected execution; complete trail is checked by the independent grader, not inferred from short screen ink")
        }
        if (result != null) return result
        delay(100)
    }
}


/** The retained ring caller publishes its operation index in s3; no private function state is selected. */
internal fun bookRingCondition(enabled: Boolean, label: String, registers: Map<String, Long>): String? {
    if (!enabled || label != "operation_ready") return null
    val index = checkNotNull(registers["\$s3"]) { "A ring observation needs its public operation index" }
    require(index in 0L..13L) { "Ring operation index is outside the retained caller" }
    return "\$s3 == $index"
}

/** The shared chunk caller publishes its zero-based index in s3 before its marker. */
internal fun bookUtf8Condition(enabled: Boolean, label: String, registers: Map<String, Long>): String? {
    if (!enabled || label != "chunk_ready") return null
    val index = checkNotNull(registers["\$s3"]) { "The UTF-8 marker requires its public chunk index" }
    require(index in 0L..3L) { "The retained UTF-8 caller contains four chunks" }
    return "\$s3 == $index"
}
