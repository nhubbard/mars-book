package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.intellij.CommonBundle
import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebugSessionListener
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import java.awt.Point
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Window
import java.awt.event.InputEvent
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.swing.AbstractButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JSpinner
import javax.swing.JTextPane
import javax.swing.JViewport
import javax.swing.SwingUtilities
import javax.swing.text.JTextComponent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.jdom.Element
import training.learn.CourseManager
import training.learn.course.Lesson
import training.learn.lesson.LessonListener
import training.statistic.LessonStartingWay
import training.ui.LearningUiHighlightingManager

internal data class BookLessonCapturePlan(
    val projectRoot: String = "",
    val outputDirectory: String = "",
    val sourceSha256: String = "",
    val captureAttempts: Boolean = false,
    val resumeAttempts: Boolean = false,
    val captureRequirements: Boolean = false,
    val resumeRequirements: Boolean = false,
    val captureReset: Boolean = false,
    val resumeReset: Boolean = false,
    val captureRun: Boolean = false,
    val captureDebug: Boolean = false,
    val captureOutput: Boolean = false,
    val captureDiagnosis: Boolean = false,
    val captureRepair: Boolean = false,
    val captureIndependent: Boolean = false,
    val captureHints: Boolean = false,
    val capturePolicy: Boolean = false,
    val captureModes: Boolean = false,
    val resumeModes: Boolean = false,
    val resumeGuided: Boolean = false,
    val expectGuidedRecheck: Boolean = false,
    val guidedSourceSha256: Map<String, String> = emptyMap(),
    val resumeIndependent: Boolean = false,
    val expectIndependentRecheck: Boolean = false,
    val independentSourceSha256: String = "",
    val closeProjectAfterCapture: Boolean = false,
    val independentSolutionPath: String = "",
    val independentSolutionSha256: String = "",
) {
  fun validate() {
    require(Path.of(projectRoot).isAbsolute && Path.of(outputDirectory).isAbsolute)
    require(Path.of(projectRoot).normalize() != Path.of(outputDirectory).normalize())
    require(sourceSha256.matches(Regex("[a-f0-9]{64}")))
    require(!captureHints || captureIndependent) { "Hint capture requires the independent lesson" }
    if (captureAttempts || resumeAttempts) {
      require(captureAttempts != resumeAttempts)
      require(
          !captureRequirements &&
              !resumeRequirements &&
              !captureReset &&
              !resumeReset &&
              !captureRun &&
              !captureDebug &&
              !captureOutput &&
              !captureDiagnosis &&
              !captureRepair &&
              !captureIndependent &&
              !captureHints &&
              !capturePolicy &&
              !captureModes &&
              !resumeModes &&
              !resumeGuided &&
              !resumeIndependent,
      )
      if (resumeAttempts) require(independentSourceSha256.matches(Regex("[a-f0-9]{64}")))
    }
    if (captureRequirements || resumeRequirements) {
      require(captureRequirements != resumeRequirements)
      require(
          !captureReset &&
              !resumeReset &&
              !captureIndependent &&
              !captureHints &&
              !capturePolicy &&
              !captureModes &&
              !resumeModes &&
              !resumeGuided &&
              !resumeIndependent,
      )
      if (captureRequirements)
          require(captureRun && captureDebug && captureOutput && captureDiagnosis && captureRepair)
      else {
        require(
            !captureRun && !captureDebug && !captureOutput && !captureDiagnosis && !captureRepair,
        )
        require(
            guidedSourceSha256.keys ==
                setOf(
                    "first-program.asm",
                    "wrong-output.asm",
                    "repair-output.asm",
                    "independent.asm",
                ),
        )
        require(guidedSourceSha256.values.all { it.matches(Regex("[a-f0-9]{64}")) })
        require(guidedSourceSha256["first-program.asm"] == sourceSha256)
      }
    }
    if (captureReset || resumeReset) {
      require(captureReset != resumeReset)
      require(
          !captureIndependent &&
              !captureHints &&
              !capturePolicy &&
              !captureModes &&
              !resumeModes &&
              !resumeGuided &&
              !resumeIndependent,
      )
      if (captureReset)
          require(captureRun && captureDebug && captureOutput && captureDiagnosis && captureRepair)
      else {
        require(
            !captureRun && !captureDebug && !captureOutput && !captureDiagnosis && !captureRepair,
        )
        require(
            guidedSourceSha256.keys ==
                setOf(
                    "first-program.asm",
                    "wrong-output.asm",
                    "repair-output.asm",
                    "independent.asm",
                ),
        )
        require(guidedSourceSha256.values.all { it.matches(Regex("[a-f0-9]{64}")) })
        require(guidedSourceSha256["first-program.asm"] == sourceSha256)
      }
    }
    require(!expectIndependentRecheck || resumeIndependent)
    require(!expectGuidedRecheck || resumeGuided)
    if (resumeGuided) {
      require(
          !captureRun &&
              !captureDebug &&
              !captureOutput &&
              !captureDiagnosis &&
              !captureRepair &&
              !captureIndependent &&
              !captureHints &&
              !capturePolicy &&
              !captureModes &&
              !resumeModes &&
              !resumeIndependent,
      )
      require(
          guidedSourceSha256.keys ==
              setOf("first-program.asm", "wrong-output.asm", "repair-output.asm"),
      )
      require(guidedSourceSha256.values.all { it.matches(Regex("[a-f0-9]{64}")) })
      require(guidedSourceSha256["first-program.asm"] == sourceSha256)
    }
    if (resumeModes) {
      require(captureModes && independentSourceSha256.matches(Regex("[a-f0-9]{64}")))
    }
    if (captureModes) {
      require(
          captureIndependent &&
              captureHints &&
              !capturePolicy &&
              !resumeIndependent &&
              !captureRun &&
              !captureDebug &&
              !captureOutput &&
              !captureDiagnosis &&
              !captureRepair,
      )
    }
    if (capturePolicy) {
      require(
          captureIndependent &&
              captureHints &&
              !resumeIndependent &&
              !captureRun &&
              !captureDebug &&
              !captureOutput &&
              !captureDiagnosis &&
              !captureRepair,
      )
    }
    if (resumeIndependent) {
      require(
          captureIndependent &&
              !captureRun &&
              !captureDebug &&
              !captureOutput &&
              !captureDiagnosis &&
              !captureRepair,
      )
      require(independentSourceSha256.matches(Regex("[a-f0-9]{64}")))
    }
    if (captureIndependent && !resumeIndependent && !capturePolicy && !captureModes) {
      require(Path.of(independentSolutionPath).isAbsolute)
      require(independentSolutionSha256.matches(Regex("[a-f0-9]{64}")))
    }
  }
}

/** Opt-in authoring driver only. No screenshot or tutor automation is packaged in MARS. */
class BookLessonCaptureActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    val location = System.getProperty("mars.book.lesson.capture.plan") ?: return
    val plan =
        withContext(Dispatchers.IO) {
          GsonBuilder()
              .create()
              .fromJson(Files.readString(Path.of(location)), BookLessonCapturePlan::class.java)
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
      withTimeout(180_000) {
        // Public queue waits for native toolwindow/layout initialization on reopened projects.
        val toolWindowsReady = CompletableDeferred<Unit>()
        withContext(Dispatchers.EDT) {
          ToolWindowManager.getInstance(project).invokeLater { toolWindowsReady.complete(Unit) }
        }
        toolWindowsReady.await()
        when {
          plan.captureAttempts || plan.resumeAttempts -> captureAttempts(project, plan, output)
          plan.resumeRequirements -> captureRequirementsResume(project, plan, output)
          plan.resumeReset -> captureResetRecovery(project, plan, output)
          plan.resumeGuided -> captureGuidedResume(project, plan, output)
          plan.resumeModes -> captureModesResume(project, plan, output)
          plan.captureModes -> captureModes(project, plan, output)
          plan.capturePolicy -> capturePolicy(project, plan, output)
          plan.resumeIndependent -> captureResume(project, plan, output)
          else -> capture(project, plan, output)
        }
      }
      if (plan.closeProjectAfterCapture) closeProject(project, output)
    } catch (error: Exception) {
      withContext(NonCancellable + Dispatchers.IO) {
        Files.writeString(output.resolve("failed.txt"), error.stackTraceToString(), CREATE_NEW)
      }
      throw error
    }
  }

  private suspend fun capture(project: Project, plan: BookLessonCapturePlan, output: Path) {
    val source = Path.of(plan.projectRoot).resolve("mars-labs/console/first-program.asm")
    val predictionPasses = AtomicInteger()
    val predictionListener =
        object : LessonListener {
          override fun lessonPassed(lesson: Lesson) {
            predictionPasses.incrementAndGet()
          }
        }
    val lesson =
        withContext(Dispatchers.EDT) {
          configurePresentation(project)
          val manager = CourseManager.instance
          manager.modules
              .single { it.id == "mars.console" }
              .lessons
              .single { it.id == "mars.console.predictions" }
              .also {
                it.addLessonListener(predictionListener)
                Disposer.register(project) { it.removeLessonListener(predictionListener) }
                manager.openLesson(
                    project,
                    it,
                    LessonStartingWay.LEARN_TAB,
                    forceStartLesson = true,
                )
              }
        }
    awaitFrame(project, "With 20 in", source)
    check(
        withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(source)) } ==
            plan.sourceSha256,
    )
    withContext(Dispatchers.EDT) {
      val visible =
          lessonVisibleText(checkNotNull(WindowManager.getInstance().getFrame(project)).rootPane)
      check(!visible.contains("The first operand is the destination. Both source operands read 20"))
      check(!visible.contains("Doubling gives -6, then adding one gives -5"))
    }
    captureFrame(project, output, "prediction-start", "With 20 in")
    answer(
        project,
        output,
        "doubling",
        "Mars.Learning.PredictDoubling",
        "\$t0 is 20; reading \$v0 twice consumes the input.",
        "Reading a register does not consume its value.",
        "\$t0 is 40; \$v0 is still 20.",
    )
    awaitFrame(project, "Now consider input -3", source)
    withContext(Dispatchers.EDT) {
      val visible =
          lessonVisibleText(checkNotNull(WindowManager.getInstance().getFrame(project)).rootPane)
      check(visible.contains("The first operand is the destination. Both source operands read 20"))
      check(!visible.contains("Doubling gives -6, then adding one gives -5"))
    }
    captureFrame(project, output, "after-doubling", "Now consider input -3")
    answer(
        project,
        output,
        "negative",
        "Mars.Learning.PredictNegative",
        "7",
        "The u does not turn -3 into a positive magnitude.",
        "-5",
    )
    awaitFrame(project, "The destination tells you where to look", source)
    awaitUi { predictionPasses.get() >= 1 }
    captureFrame(project, output, "predictions-complete", "The destination tells you where to look")
    val edited =
        withContext(Dispatchers.EDT) {
          val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
          WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(
                editor.document.textLength,
                "\n# Student work retained after lesson restart.\n",
            )
          }
          FileDocumentManager.getInstance().saveDocument(editor.document)
          editor.document.text
        }
    withContext(Dispatchers.EDT) {
      CourseManager.instance.openLesson(
          project,
          lesson,
          LessonStartingWay.RESTART_BUTTON,
          forceStartLesson = true,
      )
    }
    awaitFrame(project, "The destination tells you where to look", source)
    awaitUi { predictionPasses.get() >= 2 }
    withContext(Dispatchers.EDT) { lesson.removeLessonListener(predictionListener) }
    val retained =
        withContext(Dispatchers.EDT) {
          checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor).document.text ==
              edited
        }
    check(retained) { "Lesson restart replaced student source" }
    if (plan.captureRequirements) captureRequirementsBefore(project, output, source)
    if (plan.captureRun) captureRun(project, output, source)
    if (plan.captureDebug) captureDebug(project, output, source)
    if (plan.captureOutput) captureDebug(project, output, source, outputPreparation = true)
    if (plan.captureDiagnosis)
        captureDebug(project, output, source.resolveSibling("wrong-output.asm"), diagnosis = true)
    if (plan.captureRepair)
        captureRun(project, output, source.resolveSibling("repair-output.asm"), repair = true)
    if (plan.captureIndependent)
        captureIndependent(project, output, source.resolveSibling("independent.asm"), plan)
    if (plan.captureRequirements) captureRequirementsAfter(project, output, source)
    if (plan.captureReset) captureResetStart(project, output, source)
    val receipt =
        mapOf(
            "nativeGuidedRequirementsObserved" to plan.captureRequirements,
            "nativeResetObserved" to plan.captureReset,
            "scope" to
                "Only the native segments marked observed below; instructor policies and full-unit integration remain incomplete",
            "nativePredictionPassesObserved" to predictionPasses.get(),
            "nativeRunPassedObserved" to plan.captureRun,
            "nativeDebugPassedObserved" to plan.captureDebug,
            "nativeOutputPassedObserved" to plan.captureOutput,
            "nativeDiagnosisPassedObserved" to plan.captureDiagnosis,
            "nativeRepairPassedObserved" to plan.captureRepair,
            "nativeIndependentPassedObserved" to plan.captureIndependent,
            "nativeHintsObserved" to plan.captureHints,
            "platformBuild" to ApplicationInfo.getInstance().build.asString(),
            "sourceSha256" to plan.sourceSha256,
            "retainedSourceSha256" to captureSha256(edited.toByteArray(Charsets.UTF_8)),
            "studentEditsRetainedAfterRestart" to retained,
            "marsPluginArchiveSha256" to
                withContext(Dispatchers.IO) {
                  captureSha256(
                      Files.readAllBytes(
                          Path.of(checkNotNull(System.getProperty("mars.book.mars.archive"))),
                      ),
                  )
                },
        )
    withContext(Dispatchers.IO) {
      Files.writeString(
          output.resolve("complete.json"),
          GsonBuilder().setPrettyPrinting().create().toJson(receipt),
          CREATE_NEW,
      )
    }
  }

  private suspend fun captureAttempts(project: Project, plan: BookLessonCapturePlan, output: Path) {
    withContext(Dispatchers.EDT) { configurePresentation(project) }
    val source = Path.of(plan.projectRoot).resolve("mars-labs/console/independent.asm")
    if (plan.resumeAttempts)
        check(
            withContext(Dispatchers.IO) {
              captureSha256(Files.readAllBytes(source)) == plan.independentSourceSha256
            },
        )
    openIndependent(project)
    awaitFrame(project, "implement 3x+1", source)
    awaitUi { independentCheckEnabled(project) }
    if (!plan.resumeAttempts) {
      setPolicy(
          project,
          output,
          "attempts-limit-one",
          released = 4,
          requiredChecks = emptySet(),
          requirePredictions = false,
          maxAssessments = 1,
      )
      checkIndependent(
          project,
          output,
          "attempts-first-feedback",
          passed = false,
          status = "Completed assessments: 1 of 1",
      )
      checkIndependent(
          project,
          output,
          "attempts-limit-feedback",
          passed = false,
          limited = true,
          status = "used 1 of the 1",
      )
      withContext(Dispatchers.EDT) {
        val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
        WriteCommandAction.runWriteCommandAction(project) {
          editor.document.insertString(0, "# Student revision preserves assessment count.\n")
        }
        FileDocumentManager.getInstance().saveDocument(editor.document)
      }
      openIndependent(project)
      awaitFrame(project, "implement 3x+1", source)
      awaitUi { independentCheckEnabled(project) }
      checkIndependent(
          project,
          output,
          "attempts-edit-still-limited",
          passed = false,
          limited = true,
          status = "used 1 of the 1",
      )
    } else {
      checkIndependent(
          project,
          output,
          "attempts-reopened-still-limited",
          passed = false,
          limited = true,
          status = "used 2 of the 2",
      )
    }
    val limit = if (plan.resumeAttempts) 3 else 2
    setPolicy(
        project,
        output,
        "attempts-release-$limit",
        released = 4,
        requiredChecks = emptySet(),
        requirePredictions = false,
        maxAssessments = limit,
    )
    checkIndependent(
        project,
        output,
        "attempts-released-feedback",
        passed = false,
        status = "Completed assessments: $limit of $limit",
    )
    checkIndependent(
        project,
        output,
        "attempts-released-limit",
        passed = false,
        limited = true,
        status = "used $limit of the $limit",
    )
    val digest = withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(source)) }
    if (plan.resumeAttempts) check(digest == plan.independentSourceSha256)
    withContext(Dispatchers.IO) {
      Files.writeString(
          output.resolve("complete.json"),
          GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  mapOf(
                      "completedAssessments" to limit,
                      "assessmentLimit" to limit,
                      "sourceSha256" to digest,
                      "reopened" to plan.resumeAttempts,
                      "blockedRetriesPreserveCount" to true,
                      "instructorReleaseObserved" to true,
                      "starterStillRejected" to true,
                      "marsPluginArchiveSha256" to
                          captureSha256(
                              Files.readAllBytes(
                                  Path.of(
                                      checkNotNull(System.getProperty("mars.book.mars.archive")),
                                  ),
                              ),
                          ),
                      "scope" to
                          "Native settings and independent check feedback; no progress, answers or completed solution injected",
                  ),
              ),
          CREATE_NEW,
      )
    }
  }

  private fun independentCheckEnabled(project: Project): Boolean {
    val action =
        checkNotNull(ActionManager.getInstance().getAction("Mars.Learning.CheckIndependent"))
    val learn = checkNotNull(ToolWindowManager.getInstance(project).getToolWindow("Learn"))
    val data = DataManager.getInstance().getDataContext(learn.component)
    val event =
        AnActionEvent.createEvent(
            action,
            data,
            null,
            ActionPlaces.LEARN_TOOLWINDOW,
            ActionUiKind.NONE,
            null,
        )
    check(ActionUtil.updateAction(action, event).isPerformed)
    return event.presentation.isEnabled
  }

  private suspend fun assertIndependentCheckDisabled(project: Project) =
      withContext(Dispatchers.EDT) {
        check(!independentCheckEnabled(project))
      }

  private suspend fun openIndependent(project: Project) =
      withContext(Dispatchers.EDT) {
        CourseManager.instance.openLesson(
            project,
            checkNotNull(CourseManager.instance.findLessonById("mars.console.independent")),
            LessonStartingWay.LEARN_TAB,
            forceStartLesson = true,
        )
      }

  private suspend fun captureRequirementsBefore(project: Project, output: Path, worked: Path) {
    setPolicy(
        project,
        output,
        "requirements-all-selected",
        released = 4,
        requiredChecks = setOf("run", "debug", "output", "diagnosis", "repair"),
    )
    openIndependent(project)
    awaitFrame(
        project,
        "Your course requires these activities",
        worked.resolveSibling("independent.asm"),
    )
    captureFrame(
        project,
        output,
        "requirements-before-observations",
        "Your course requires these activities",
    )
    assertIndependentCheckDisabled(project)
  }

  private suspend fun captureRequirementsAfter(project: Project, output: Path, worked: Path) {
    val independent = worked.resolveSibling("independent.asm")
    openIndependent(project)
    awaitFrame(project, "implement 3x+1", independent)
    awaitUi { independentCheckEnabled(project) }
    captureFrame(project, output, "requirements-all-satisfied", "implement 3x+1")
    checkIndependent(project, output, "requirements-starter-feedback", passed = false)
    val mutation =
        withContext(Dispatchers.EDT) {
          val file = checkNotNull(LocalFileSystem.getInstance().findFileByNioFile(worked))
          val document = checkNotNull(FileDocumentManager.getInstance().getDocument(file))
          val text = document.text
          val before = document.modificationStamp
          check(FileEditorManager.getInstance(project).selectedTextEditor?.document !== document)
          WriteCommandAction.runWriteCommandAction(project) {
            document.setText(text + "\n# Changed after leaving its guided activity\n")
            document.setText(text)
          }
          val afterEdit = document.modificationStamp
          check(afterEdit != before)
          // Saving unchanged final text may restore its disk stamp; the actual edit already
          // invalidated evidence.
          FileDocumentManager.getInstance().saveDocument(document)
          mapOf(
              "beforeStamp" to before,
              "afterStamp" to afterEdit,
              "afterSaveStamp" to document.modificationStamp,
              "sourceSha256" to captureSha256(document.text.toByteArray(Charsets.UTF_8)),
          )
        }
    openIndependent(project)
    awaitFrame(project, "Your course requires these activities", independent)
    captureFrame(
        project,
        output,
        "requirements-edit-back-blocked",
        "Your course requires these activities",
    )
    assertIndependentCheckDisabled(project)
    setPolicy(
        project,
        output,
        "requirements-retain-two",
        released = 4,
        requiredChecks = setOf("diagnosis", "repair"),
    )
    openIndependent(project)
    awaitFrame(project, "implement 3x+1", independent)
    awaitUi { independentCheckEnabled(project) }
    captureFrame(project, output, "requirements-selected-two-satisfied", "implement 3x+1")
    val sources =
        withContext(Dispatchers.IO) {
          listOf("first-program.asm", "wrong-output.asm", "repair-output.asm", "independent.asm")
              .associateWith { captureSha256(Files.readAllBytes(worked.resolveSibling(it))) }
        }
    withContext(Dispatchers.IO) {
      Files.writeString(
          output.resolve("requirements-state.json"),
          GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  mapOf(
                      "allFiveOriginallyRequired" to true,
                      "allFiveSatisfiedThroughNativeObservations" to true,
                      "inactiveEditBackRequiresFreshEvidence" to true,
                      "mutation" to mutation,
                      "finalRequiredChecks" to listOf("diagnosis", "repair"),
                      "sourceSha256" to sources,
                      "scope" to
                          "Native settings and observations; unchanged text after an inactive edit cannot resurrect passes. Private solutions are not installed.",
                  ),
              ),
          CREATE_NEW,
      )
    }
  }

  private suspend fun captureRequirementsResume(
      project: Project,
      plan: BookLessonCapturePlan,
      output: Path,
  ) {
    withContext(Dispatchers.EDT) { configurePresentation(project) }
    val directory = Path.of(plan.projectRoot).resolve("mars-labs/console")
    suspend fun verifySources() =
        withContext(Dispatchers.IO) {
          check(
              plan.guidedSourceSha256.all { (name, digest) ->
                captureSha256(Files.readAllBytes(directory.resolve(name))) == digest
              },
          )
        }
    verifySources()
    openIndependent(project)
    awaitFrame(
        project,
        "Your course requires these activities",
        directory.resolve("independent.asm"),
    )
    captureFrame(
        project,
        output,
        "requirements-offline-edit-blocked",
        "Your course requires these activities",
    )
    assertIndependentCheckDisabled(project)
    setPolicy(
        project,
        output,
        "requirements-release-repair",
        released = 4,
        requiredChecks = setOf("diagnosis"),
    )
    openIndependent(project)
    awaitFrame(project, "implement 3x+1", directory.resolve("independent.asm"))
    awaitUi { independentCheckEnabled(project) }
    captureFrame(project, output, "requirements-unchanged-diagnosis-accepted", "implement 3x+1")
    checkIndependent(project, output, "requirements-reopened-starter-feedback", passed = false)
    verifySources()
    withContext(Dispatchers.IO) {
      Files.writeString(
          output.resolve("complete.json"),
          GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  mapOf(
                      "sourceSha256" to plan.guidedSourceSha256,
                      "sourceUnchanged" to true,
                      "persistedRequirementsObserved" to true,
                      "offlineRepairEditBlocked" to true,
                      "unchangedDiagnosisStillAccepted" to true,
                      "unfinishedIndependentRejected" to true,
                      "marsPluginArchiveSha256" to
                          captureSha256(
                              Files.readAllBytes(
                                  Path.of(
                                      checkNotNull(System.getProperty("mars.book.mars.archive")),
                                  ),
                              ),
                          ),
                      "scope" to
                          "Fresh-process policy/source validation and native release; no progress, quiz answers or private solutions injected",
                  ),
              ),
          CREATE_NEW,
      )
    }
  }

  private suspend fun assertHistoryActionDisabled(project: Project, id: String) =
      withContext(Dispatchers.EDT) {
        val action = checkNotNull(ActionManager.getInstance().getAction(id))
        val data = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build()
        val event =
            AnActionEvent.createEvent(
                action,
                data,
                null,
                ActionPlaces.MAIN_MENU,
                ActionUiKind.NONE,
                null,
            )
        check(ActionUtil.updateAction(action, event).isPerformed && !event.presentation.isEnabled)
      }

  private suspend fun historyAction(
      project: Project,
      output: Path,
      prefix: String,
      recover: Boolean,
      confirm: Boolean,
  ) {
    val failure = AtomicReference<Throwable>()
    withContext(Dispatchers.EDT) {
      val action =
          checkNotNull(
              ActionManager.getInstance()
                  .getAction(if (recover) "Mars.Learning.Recover" else "Mars.Learning.Reset"),
          )
      val data = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build()
      val event =
          AnActionEvent.createEvent(
              action,
              data,
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
                } catch (error: Throwable) {
                  failure.set(error)
                }
              },
              ModalityState.nonModal(),
          )
    }
    val dialog =
        awaitDialog(
            failure,
            if (recover) "Recover this unit's history?" else "Reset this unit's progress?",
        )
    // macOS can expose the Swing hierarchy before the native sheet finishes placement.
    // Wait for placement/paint, then ensure capture uses settled screen coordinates.
    delay(500)
    val settled = onModalUi { Rectangle(dialog.rootPane.locationOnScreen, dialog.rootPane.size) }
    delay(200)
    check(
        onModalUi { Rectangle(dialog.rootPane.locationOnScreen, dialog.rootPane.size) } == settled,
    )
    captureComponent(
        dialog.rootPane,
        output,
        prefix,
        if (recover) "Execution checks must run again"
        else "Your source files and course settings stay as they are",
    )
    onModalUi {
      visibleLessonButton(
              dialog.rootPane,
              if (!confirm) "Cancel" else if (recover) "Recover history" else "Reset progress",
          )
          .doClick()
    }
    awaitUi { !dialog.isShowing }
  }

  private suspend fun captureResetStart(project: Project, output: Path, worked: Path) {
    val independent = worked.resolveSibling("independent.asm")
    withContext(Dispatchers.EDT) {
      val lesson = checkNotNull(CourseManager.instance.findLessonById("mars.console.independent"))
      CourseManager.instance.openLesson(
          project,
          lesson,
          LessonStartingWay.LEARN_TAB,
          forceStartLesson = true,
      )
    }
    awaitFrame(project, "implement 3x+1", independent)
    captureHints(project, output, "reset-before-hints", existing = 0, revealTo = 1)
    val sources =
        withContext(Dispatchers.IO) {
          listOf(
                  worked,
                  worked.resolveSibling("wrong-output.asm"),
                  worked.resolveSibling("repair-output.asm"),
                  independent,
              )
              .associate { it.fileName.toString() to captureSha256(Files.readAllBytes(it)) }
        }
    historyAction(project, output, "reset-cancel", recover = false, confirm = false)
    captureHints(project, output, "reset-cancel-preserved-hints", existing = 1, revealTo = 1)
    val predictions =
        withContext(Dispatchers.EDT) {
          checkNotNull(CourseManager.instance.findLessonById("mars.console.predictions"))
        }
    val passes = AtomicInteger()
    val listener =
        object : LessonListener {
          override fun lessonPassed(lesson: Lesson) {
            passes.incrementAndGet()
          }
        }
    withContext(Dispatchers.EDT) { predictions.addLessonListener(listener) }
    try {
      historyAction(project, output, "reset-confirm", recover = false, confirm = true)
      awaitFrame(project, "With 20 in", worked)
      delay(1_500)
      check(passes.get() == 0)
      captureFrame(project, output, "reset-prediction-required", "With 20 in")
      assertHistoryActionDisabled(project, "Mars.Learning.Reset")
    } finally {
      withContext(Dispatchers.EDT) { predictions.removeLessonListener(listener) }
    }
    withContext(Dispatchers.IO) {
      check(
          sources.all { (name, digest) ->
            captureSha256(Files.readAllBytes(worked.resolveSibling(name))) == digest
          },
      )
      Files.writeString(
          output.resolve("reset-state.json"),
          GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  mapOf(
                      "sourceSha256" to sources,
                      "resetCancellationPreservedHint" to true,
                      "resetRequiredPrediction" to true,
                      "resetDisabledWithoutNewHistory" to true,
                      "nativeResetPassCallbacks" to passes.get(),
                      "sourceUnchanged" to true,
                      "scope" to
                          "Actual reset after six guided observations and one hint; recovery copy must survive native save/close and a fresh process",
                  ),
              ),
          CREATE_NEW,
      )
    }
  }

  private suspend fun captureResetRecovery(
      project: Project,
      plan: BookLessonCapturePlan,
      output: Path,
  ) {
    withContext(Dispatchers.EDT) { configurePresentation(project) }
    val folder = Path.of(plan.projectRoot).resolve("mars-labs/console")
    suspend fun verifySources() =
        withContext(Dispatchers.IO) {
          check(
              plan.guidedSourceSha256.all { (name, digest) ->
                captureSha256(Files.readAllBytes(folder.resolve(name))) == digest
              },
          )
        }
    verifySources()
    val executions = AtomicInteger()
    val connection = project.messageBus.connect()
    connection.subscribe(
        ExecutionManager.EXECUTION_TOPIC,
        object : ExecutionListener {
          override fun processStarting(
              executorId: String,
              env: ExecutionEnvironment,
              handler: ProcessHandler,
          ) {
            executions.incrementAndGet()
          }
        },
    )
    val predictions =
        withContext(Dispatchers.EDT) {
          checkNotNull(CourseManager.instance.findLessonById("mars.console.predictions"))
        }
    val passes = AtomicInteger()
    val listener =
        object : LessonListener {
          override fun lessonPassed(lesson: Lesson) {
            passes.incrementAndGet()
          }
        }
    withContext(Dispatchers.EDT) { predictions.addLessonListener(listener) }
    try {
      historyAction(project, output, "recovery-cancel", recover = true, confirm = false)
      historyAction(project, output, "recovery-confirm", recover = true, confirm = true)
      awaitFrame(
          project,
          "The destination tells you where to look",
          folder.resolve("first-program.asm"),
      )
      awaitUi { passes.get() == 1 }
      captureFrame(
          project,
          output,
          "recovered-predictions",
          "The destination tells you where to look",
      )
      assertHistoryActionDisabled(project, "Mars.Learning.Recover")
      for ((id, text, name) in
          listOf(
              Triple("run", "Run first-program.asm using the normal MARS Run", "first-program.asm"),
              Triple("repair", "repair-output.asm is your working copy", "repair-output.asm"),
          )) {
        val completed = AtomicInteger()
        val pendingListener =
            object : LessonListener {
              override fun lessonPassed(lesson: Lesson) {
                completed.incrementAndGet()
              }
            }
        val lesson =
            withContext(Dispatchers.EDT) {
              checkNotNull(CourseManager.instance.findLessonById("mars.console.$id")).also {
                it.addLessonListener(pendingListener)
                CourseManager.instance.openLesson(
                    project,
                    it,
                    LessonStartingWay.LEARN_TAB,
                    forceStartLesson = true,
                )
              }
            }
        try {
          awaitFrame(project, text, folder.resolve(name))
          delay(1_500)
          check(completed.get() == 0)
          captureFrame(project, output, "recovered-$id-recheck", text)
        } finally {
          withContext(Dispatchers.EDT) { lesson.removeLessonListener(pendingListener) }
        }
      }
      withContext(Dispatchers.EDT) {
        CourseManager.instance.openLesson(
            project,
            checkNotNull(CourseManager.instance.findLessonById("mars.console.independent")),
            LessonStartingWay.LEARN_TAB,
            forceStartLesson = true,
        )
      }
      awaitFrame(project, "implement 3x+1", folder.resolve("independent.asm"))
      captureHints(project, output, "recovered-hints", existing = 1, revealTo = 1)
      verifySources()
      check(executions.get() == 0)
      withContext(Dispatchers.IO) {
        Files.writeString(
            output.resolve("complete.json"),
            GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(
                    mapOf(
                        "sourceSha256" to plan.guidedSourceSha256,
                        "sourceUnchanged" to true,
                        "nativePredictionPassCallbacks" to passes.get(),
                        "runAndRepairRequireRecheck" to true,
                        "hintRecoveredAfterFreshProcess" to true,
                        "recoveryCopyConsumed" to true,
                        "processStarts" to executions.get(),
                        "marsPluginArchiveSha256" to
                            captureSha256(
                                Files.readAllBytes(
                                    Path.of(
                                        checkNotNull(System.getProperty("mars.book.mars.archive")),
                                    ),
                                ),
                            ),
                        "scope" to
                            "Native recovery after fresh process without source/progress/answer injection; no execution credit restored",
                    ),
                ),
            CREATE_NEW,
        )
      }
    } finally {
      connection.disconnect()
      withContext(Dispatchers.EDT) { predictions.removeLessonListener(listener) }
    }
  }

  private suspend fun chooseMode(
      project: Project,
      output: Path,
      prefix: String,
      selection: String?,
      expectedSelected: String,
      expectedGuided: String,
      freeAllowed: Boolean = true,
  ) {
    val failure = AtomicReference<Throwable>()
    withContext(Dispatchers.EDT) {
      val action = checkNotNull(ActionManager.getInstance().getAction("Mars.Learning.Open"))
      val data = SimpleDataContext.builder().add(CommonDataKeys.PROJECT, project).build()
      val event =
          AnActionEvent.createEvent(
              action,
              data,
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
                } catch (error: Throwable) {
                  failure.set(error)
                }
              },
              ModalityState.nonModal(),
          )
    }
    val dialog = awaitDialog(failure, "Choose how to work")
    awaitUi {
      lessonVisibleText(dialog.rootPane).contains("Guided work returns to: $expectedGuided.")
    }
    onModalUi {
      check(visibleLessonButton(dialog.rootPane, expectedSelected).isSelected)
      check(visibleLessonButton(dialog.rootPane, "Free writing").isEnabled == freeAllowed)
    }
    captureComponent(dialog.rootPane, output, prefix, "Guided work returns to:")
    onModalUi {
      if (selection == null) visibleLessonButton(dialog.rootPane, "Cancel").doClick()
      else {
        val button = visibleLessonButton(dialog.rootPane, selection)
        check(button.isEnabled)
        button.doClick()
        visibleLessonButton(dialog.rootPane, "Open lesson").doClick()
      }
    }
    awaitUi { !dialog.isShowing }
    failure.get()?.let { throw it }
  }

  /** A later JVM loads the native workspace; this path never writes source, answers or hints. */
  /** Reopens saved native tasks without injecting progress, quiz answers or execution. */
  private suspend fun captureGuidedResume(
      project: Project,
      plan: BookLessonCapturePlan,
      output: Path,
  ) {
    withContext(Dispatchers.EDT) { configurePresentation(project) }
    val folder = Path.of(plan.projectRoot).resolve("mars-labs/console")
    suspend fun verifySources() =
        withContext(Dispatchers.IO) {
          for ((name, digest) in plan.guidedSourceSha256) {
            check(captureSha256(Files.readAllBytes(folder.resolve(name))) == digest) {
              "Saved source differs: $name"
            }
          }
        }
    verifySources()
    val executions = AtomicInteger()
    val connection = project.messageBus.connect()
    connection.subscribe(
        ExecutionManager.EXECUTION_TOPIC,
        object : ExecutionListener {
          override fun processStarting(
              executorId: String,
              env: ExecutionEnvironment,
              handler: ProcessHandler,
          ) {
            executions.incrementAndGet()
          }
        },
    )
    val observations = mutableListOf<Map<String, Any>>()
    try {
      for ((segment, completedText, pendingText) in
          listOf(
              Triple("predictions", "The destination tells you where to look", ""),
              Triple(
                  "run",
                  "The console result agrees with the prediction",
                  "Run first-program.asm using the normal MARS Run",
              ),
              Triple(
                  "debug",
                  "You have observed the input, doubled value",
                  "Set a breakpoint on addu",
              ),
              Triple(
                  "output",
                  "You observed the answer survive",
                  "Stop the preceding Debug session",
              ),
              Triple("diagnosis", "You located an argument error", ""),
              Triple("repair", "The repaired source printed 41", ""),
          )) {
        val requiresRecheck = plan.expectGuidedRecheck && segment in setOf("run", "debug", "output")
        val file =
            folder.resolve(
                when (segment) {
                  "diagnosis" -> "wrong-output.asm"
                  "repair" -> "repair-output.asm"
                  else -> "first-program.asm"
                },
            )
        val passes = AtomicInteger()
        val listener =
            object : LessonListener {
              override fun lessonPassed(lesson: Lesson) {
                passes.incrementAndGet()
              }
            }
        val lesson =
            withContext(Dispatchers.EDT) {
              CourseManager.instance.modules
                  .single { it.id == "mars.console" }
                  .lessons
                  .single { it.id == "mars.console.$segment" }
                  .also {
                    it.addLessonListener(listener)
                    CourseManager.instance.openLesson(
                        project,
                        it,
                        LessonStartingWay.LEARN_TAB,
                        forceStartLesson = true,
                    )
                  }
            }
        try {
          val text = if (requiresRecheck) pendingText else completedText
          awaitFrame(project, text, file)
          if (requiresRecheck) {
            // Observe several native timer ticks; merely seeing instruction text is insufficient.
            delay(1_500)
            check(passes.get() == 0) { "Edited source retained an execution pass: $segment" }
          } else awaitUi { passes.get() == 1 }
          captureFrame(project, output, "guided-reopened-$segment", text)
          observations.add(
              mapOf(
                  "lesson" to lesson.id,
                  "requiresExecutionRecheck" to requiresRecheck,
                  "nativePassCallbacks" to passes.get(),
                  "source" to file.fileName.toString(),
              ),
          )
        } finally {
          withContext(Dispatchers.EDT) { lesson.removeLessonListener(listener) }
        }
      }
      verifySources()
      check(executions.get() == 0) { "Resume verification unexpectedly executed a process" }
      withContext(Dispatchers.IO) {
        Files.writeString(
            output.resolve("complete.json"),
            GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(
                    mapOf(
                        "platformBuild" to ApplicationInfo.getInstance().build.asString(),
                        "observations" to observations,
                        "processStarts" to executions.get(),
                        "sourceSha256" to plan.guidedSourceSha256,
                        "marsPluginArchiveSha256" to
                            captureSha256(
                                Files.readAllBytes(
                                    Path.of(
                                        checkNotNull(System.getProperty("mars.book.mars.archive")),
                                    ),
                                ),
                            ),
                        "scope" to
                            "Fresh-process guided restoration with unchanged or offline-edited source; no source, progress, answers, policy or execution injected",
                    ),
                ),
            CREATE_NEW,
        )
      }
    } finally {
      connection.disconnect()
    }
  }

  private suspend fun captureModesResume(
      project: Project,
      plan: BookLessonCapturePlan,
      output: Path,
  ) {
    withContext(Dispatchers.EDT) { configurePresentation(project) }
    val folder = Path.of(plan.projectRoot).resolve("mars-labs/console")
    val worked = folder.resolve("first-program.asm")
    val independent = folder.resolve("independent.asm")
    suspend fun assertSource() =
        withContext(Dispatchers.IO) {
          check(captureSha256(Files.readAllBytes(worked)) == plan.sourceSha256)
          check(captureSha256(Files.readAllBytes(independent)) == plan.independentSourceSha256)
        }
    assertSource()
    chooseMode(
        project,
        output,
        "modes-reopened-choice",
        "Guided work",
        "Free writing",
        "Run the program",
    )
    val runReady = "Run first-program.asm using the normal MARS Run"
    awaitFrame(project, runReady, worked)
    captureFrame(project, output, "modes-reopened-guided", runReady)
    chooseMode(
        project,
        output,
        "modes-reopened-free",
        "Free writing",
        "Guided work",
        "Run the program",
    )
    awaitFrame(project, "implement 3x+1", independent)
    captureHints(project, output, "modes-reopened-history", existing = 1, revealTo = 1)
    assertSource()
    withContext(Dispatchers.IO) {
      Files.writeString(
          output.resolve("complete.json"),
          GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  mapOf(
                      "platformBuild" to ApplicationInfo.getInstance().build.asString(),
                      "freshProcessModeRestoration" to true,
                      "savedModeObserved" to "FREE_WRITING",
                      "savedGuidedLessonObserved" to "mars.console.run",
                      "hintHistoryObserved" to true,
                      "sourceUnchanged" to true,
                      "workedSha256" to plan.sourceSha256,
                      "independentSha256" to plan.independentSourceSha256,
                      "marsPluginArchiveSha256" to
                          captureSha256(
                              Files.readAllBytes(
                                  Path.of(
                                      checkNotNull(System.getProperty("mars.book.mars.archive")),
                                  ),
                              ),
                          ),
                      "scope" to
                          "Native workspace restoration in a fresh JVM without source, progress, quiz, policy or hint injection; no execution check is invoked",
                  ),
              ),
          CREATE_NEW,
      )
    }
  }

  /** Explicit mode changes through the production action; comments are the only fixture edits. */
  private suspend fun captureModes(project: Project, plan: BookLessonCapturePlan, output: Path) {
    withContext(Dispatchers.EDT) { configurePresentation(project) }
    val folder = Path.of(plan.projectRoot).resolve("mars-labs/console")
    val worked = folder.resolve("first-program.asm")
    val independent = folder.resolve("independent.asm")
    val predictions = "Predict register changes"
    val run = "Run the program"
    chooseMode(project, output, "modes-initial-cancel", null, "Guided work", predictions)
    check(withContext(Dispatchers.IO) { !Files.exists(folder) })
    chooseMode(project, output, "modes-guided-open", "Guided work", "Guided work", predictions)
    awaitFrame(project, "With 20 in", worked)
    check(
        withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(worked)) } ==
            plan.sourceSha256,
    )
    answer(
        project,
        output,
        "modes-doubling",
        "Mars.Learning.PredictDoubling",
        "\$t0 is 20; reading \$v0 twice consumes the input.",
        "Reading a register does not consume its value.",
        "\$t0 is 40; \$v0 is still 20.",
    )
    awaitFrame(project, "Now consider input -3", worked)
    answer(
        project,
        output,
        "modes-negative",
        "Mars.Learning.PredictNegative",
        "7",
        "The u does not turn -3 into a positive magnitude.",
        "-5",
    )
    awaitFrame(project, "The destination tells you where to look", worked)
    withContext(Dispatchers.EDT) {
      val lesson =
          CourseManager.instance.modules
              .single { it.id == "mars.console" }
              .lessons
              .single { it.id == "mars.console.run" }
      CourseManager.instance.openLesson(
          project,
          lesson,
          LessonStartingWay.LEARN_TAB,
          forceStartLesson = true,
      )
    }
    val runReady = "Run first-program.asm using the normal MARS Run"
    awaitFrame(project, runReady, worked)
    val workedText =
        withContext(Dispatchers.EDT) {
          val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
          WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(0, "# Guided work retained through mode changes.\n")
          }
          FileDocumentManager.getInstance().saveDocument(editor.document)
          editor.document.text
        }
    captureFrame(project, output, "modes-guided-edited", runReady)
    chooseMode(project, output, "modes-to-free", "Free writing", "Guided work", run)
    awaitFrame(project, "implement 3x+1", independent)
    captureFrame(project, output, "modes-independent-ready", "implement 3x+1")
    captureHints(project, output, "modes-first-hint", existing = 0, revealTo = 1)
    val independentText =
        withContext(Dispatchers.EDT) {
          val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
          WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(0, "# Independent work retained through mode changes.\n")
          }
          FileDocumentManager.getInstance().saveDocument(editor.document)
          editor.document.text
        }
    chooseMode(project, output, "modes-free-cancel", null, "Free writing", run)
    chooseMode(project, output, "modes-back-guided", "Guided work", "Free writing", run)
    awaitFrame(project, runReady, worked)
    check(withContext(Dispatchers.IO) { Files.readString(worked) } == workedText)
    captureFrame(project, output, "modes-guided-resumed", runReady)
    chooseMode(project, output, "modes-free-resumed", "Free writing", "Guided work", run)
    awaitFrame(project, "implement 3x+1", independent)
    check(withContext(Dispatchers.IO) { Files.readString(independent) } == independentText)
    captureHints(project, output, "modes-hint-retained", existing = 1, revealTo = 1)
    checkIndependent(project, output, "modes-starter-feedback", passed = false)
    setPolicy(project, output, "modes-guided-required", released = 4, allowFreeWriting = false)
    chooseMode(
        project,
        output,
        "modes-free-disabled",
        null,
        "Guided work",
        run,
        freeAllowed = false,
    )
    withContext(Dispatchers.EDT) {
      val lesson =
          CourseManager.instance.modules
              .single { it.id == "mars.console" }
              .lessons
              .single { it.id == "mars.console.independent" }
      CourseManager.instance.openLesson(
          project,
          lesson,
          LessonStartingWay.LEARN_TAB,
          forceStartLesson = true,
      )
    }
    awaitFrame(project, "Your course currently requires guided work", independent)
    captureFrame(
        project,
        output,
        "modes-direct-entry-blocked",
        "Your course currently requires guided work",
    )
    chooseMode(
        project,
        output,
        "modes-forced-guided",
        "Guided work",
        "Guided work",
        run,
        freeAllowed = false,
    )
    awaitFrame(project, runReady, worked)
    setPolicy(project, output, "modes-free-released", released = 4, allowFreeWriting = true)
    chooseMode(project, output, "modes-final-free", "Free writing", "Guided work", run)
    awaitFrame(project, "implement 3x+1", independent)
    captureHints(project, output, "modes-final-history", existing = 1, revealTo = 1)
    withContext(Dispatchers.IO) {
      check(
          Files.readString(worked) == workedText &&
              Files.readString(independent) == independentText,
      )
      Files.writeString(
          output.resolve("complete.json"),
          GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  mapOf(
                      "platformBuild" to ApplicationInfo.getInstance().build.asString(),
                      "nativeModeChoiceObserved" to true,
                      "cancelCreatedNoSource" to true,
                      "guidedResumeLessonObserved" to "mars.console.run",
                      "sourceEditsRetained" to true,
                      "hintHistoryRetained" to true,
                      "unfinishedIndependentRejected" to true,
                      "courseDisabledFreeChoice" to true,
                      "directIndependentEntryBlocked" to true,
                      "freeWritingReleasedAgain" to true,
                      "workedSha256" to captureSha256(workedText.toByteArray(Charsets.UTF_8)),
                      "independentSha256" to
                          captureSha256(independentText.toByteArray(Charsets.UTF_8)),
                      "marsPluginArchiveSha256" to
                          captureSha256(
                              Files.readAllBytes(
                                  Path.of(
                                      checkNotNull(System.getProperty("mars.book.mars.archive")),
                                  ),
                              ),
                          ),
                      "scope" to
                          "Mode navigation, saved segment choice, source/hint preservation and policy. This batch does not execute normal Run/Debug or prove fresh-process mode restoration.",
                  ),
              ),
          CREATE_NEW,
      )
    }
  }

  /**
   * Uses only native settings, lesson actions and rendered dialogs; never injects policy/progress
   * state.
   */
  private suspend fun capturePolicy(project: Project, plan: BookLessonCapturePlan, output: Path) {
    withContext(Dispatchers.EDT) { configurePresentation(project) }
    setPolicy(project, output, "policy-restricted", released = 0)
    val source = Path.of(plan.projectRoot).resolve("mars-labs/console/independent.asm")
    val passes = AtomicInteger()
    val listener =
        object : LessonListener {
          override fun lessonPassed(lesson: Lesson) {
            passes.incrementAndGet()
          }
        }
    val independent =
        withContext(Dispatchers.EDT) {
          CourseManager.instance.modules
              .single { it.id == "mars.console" }
              .lessons
              .single { it.id == "mars.console.independent" }
              .also {
                it.addLessonListener(listener)
                CourseManager.instance.openLesson(
                    project,
                    it,
                    LessonStartingWay.LEARN_TAB,
                    forceStartLesson = true,
                )
              }
        }
    try {
      val required = "Your course requires both prediction quizzes"
      awaitFrame(project, required, source)
      delay(600)
      check(passes.get() == 0)
      captureFrame(project, output, "policy-prerequisite-required", required)
      withContext(Dispatchers.EDT) {
        val learn = checkNotNull(ToolWindowManager.getInstance(project).getToolWindow("Learn"))
        val data = DataManager.getInstance().getDataContext(learn.component)
        for (id in listOf("Mars.Learning.CheckIndependent", "Mars.Learning.IndependentHint")) {
          val action = checkNotNull(ActionManager.getInstance().getAction(id))
          val event =
              AnActionEvent.createEvent(
                  action,
                  data,
                  null,
                  ActionPlaces.LEARN_TOOLWINDOW,
                  ActionUiKind.NONE,
                  null,
              )
          check(ActionUtil.updateAction(action, event).isPerformed) {
            "Native action update did not complete"
          }
          check(!event.presentation.isEnabled) { "Blocked lesson exposed $id" }
        }
        val predictions =
            CourseManager.instance.modules
                .single { it.id == "mars.console" }
                .lessons
                .single { it.id == "mars.console.predictions" }
        CourseManager.instance.openLesson(
            project,
            predictions,
            LessonStartingWay.LEARN_TAB,
            forceStartLesson = true,
        )
      }
      val worked = source.resolveSibling("first-program.asm")
      awaitFrame(project, "With 20 in", worked)
      check(
          withContext(Dispatchers.IO) { captureSha256(Files.readAllBytes(worked)) } ==
              plan.sourceSha256,
      )
      answer(
          project,
          output,
          "policy-doubling",
          "Mars.Learning.PredictDoubling",
          "\$t0 is 20; reading \$v0 twice consumes the input.",
          "Reading a register does not consume its value.",
          "\$t0 is 40; \$v0 is still 20.",
      )
      awaitFrame(project, "Now consider input -3", worked)
      answer(
          project,
          output,
          "policy-negative",
          "Mars.Learning.PredictNegative",
          "7",
          "The u does not turn -3 into a positive magnitude.",
          "-5",
      )
      awaitFrame(project, "The destination tells you where to look", worked)
      withContext(Dispatchers.EDT) {
        CourseManager.instance.openLesson(
            project,
            independent,
            LessonStartingWay.LEARN_TAB,
            forceStartLesson = true,
        )
      }
      awaitFrame(project, "implement 3x+1", source)
      val before = withContext(Dispatchers.IO) { Files.readAllBytes(source) }
      captureHints(project, output, "policy-hints-zero", existing = 0, revealTo = 0, released = 0)
      setPolicy(project, output, "policy-release-two", released = 2)
      captureHints(project, output, "policy-hints-two", existing = 0, revealTo = 2, released = 2)
      setPolicy(project, output, "policy-release-zero-again", released = 0)
      captureHints(
          project,
          output,
          "policy-hints-retained",
          existing = 2,
          revealTo = 2,
          released = 0,
      )
      setPolicy(project, output, "policy-release-four", released = 4)
      captureHints(project, output, "policy-hints-four", existing = 2, revealTo = 4)
      check(passes.get() == 0)
      checkIndependent(project, output, "policy-starter-feedback", passed = false)
      check(passes.get() == 0)
      withContext(Dispatchers.IO) {
        check(Files.readAllBytes(source).contentEquals(before))
        Files.writeString(
            output.resolve("complete.json"),
            GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(
                    mapOf(
                        "platformBuild" to ApplicationInfo.getInstance().build.asString(),
                        "nativeSettingsApplied" to true,
                        "prerequisiteBlockedIndependent" to true,
                        "nativePredictionAnswersUnlockedIndependent" to true,
                        "hintReleaseObserved" to listOf(0, 2, 0, 4),
                        "revealedHintsRetainedAfterRestriction" to true,
                        "independentPassesObserved" to passes.get(),
                        "starterRejectedByFiveCases" to true,
                        "sourceUnchangedByPolicyAndHints" to true,
                        "independentSourceSha256" to captureSha256(before),
                        "marsPluginArchiveSha256" to
                            captureSha256(
                                Files.readAllBytes(
                                    Path.of(
                                        checkNotNull(System.getProperty("mars.book.mars.archive")),
                                    ),
                                ),
                            ),
                        "scope" to
                            "Native project policy settings and first-unit prediction prerequisite; other progression policies and mode controls remain open",
                    ),
                ),
            CREATE_NEW,
        )
      }
    } finally {
      withContext(NonCancellable + Dispatchers.EDT) { independent.removeLessonListener(listener) }
    }
  }

  private suspend fun setPolicy(
      project: Project,
      output: Path,
      prefix: String,
      released: Int,
      allowFreeWriting: Boolean = true,
      requiredChecks: Set<String>? = null,
      requirePredictions: Boolean = true,
      maxAssessments: Int? = null,
  ) {
    val requirementLabels =
        withContext(Dispatchers.EDT) {
          listOf("run", "debug", "output", "diagnosis", "repair").associateWith { id ->
            checkNotNull(CourseManager.instance.findLessonById("mars.console.$id")).name
          }
        }
    val failure = AtomicReference<Throwable>()
    withContext(Dispatchers.EDT) {
      ApplicationManager.getApplication()
          .invokeLater(
              {
                try {
                  ShowSettingsUtil.getInstance().showSettingsDialog(project, "MARS Learning")
                } catch (error: Throwable) {
                  failure.set(error)
                }
              },
              ModalityState.nonModal(),
          )
    }
    val label = "Require both prediction quizzes before independent work"
    var settings: JDialog? = null
    while (settings == null) {
      failure.get()?.let { throw it }
      settings = onModalUi {
        Window.getWindows().filterIsInstance<JDialog>().singleOrNull {
          it.isShowing && lessonVisibleText(it.rootPane).contains(label)
        }
      }
      if (settings == null) delay(100)
    }
    val dialog = settings
    awaitUi {
      captureComponents(dialog.rootPane).filterIsInstance<JLabel>().none {
        it.isShowing && it.text == CommonBundle.getLoadingTreeNodeText()
      }
    }
    onModalUi {
      val widgets = captureComponents(dialog.rootPane)
      widgets.filterIsInstance<JSpinner>().single { it.isShowing }.value = released
      if (maxAssessments != null)
          widgets
              .filterIsInstance<JTextComponent>()
              .single {
                it.isShowing &&
                    it.accessibleContext.accessibleName ==
                        "Maximum completed independent assessments (0 means unlimited):"
              }
              .text = maxAssessments.toString()
      widgets.filterIsInstance<JCheckBox>().single { it.isShowing && it.text == label }.isSelected =
          requirePredictions
      widgets
          .filterIsInstance<JCheckBox>()
          .single { it.isShowing && it.text == "Allow free-writing mode" }
          .isSelected = allowFreeWriting
      if (requiredChecks != null)
          for ((id, title) in requirementLabels) {
            widgets
                .filterIsInstance<JCheckBox>()
                .single { it.isShowing && it.text == title }
                .isSelected = id in requiredChecks
          }
    }
    awaitUi {
      val widgets = captureComponents(dialog.rootPane)
      widgets.filterIsInstance<JSpinner>().single { it.isShowing }.value == released &&
          (maxAssessments == null ||
              widgets
                  .filterIsInstance<JTextComponent>()
                  .single {
                    it.isShowing &&
                        it.accessibleContext.accessibleName ==
                            "Maximum completed independent assessments (0 means unlimited):"
                  }
                  .text == maxAssessments.toString()) &&
          widgets
              .filterIsInstance<JCheckBox>()
              .single { it.isShowing && it.text == label }
              .isSelected == requirePredictions &&
          widgets
              .filterIsInstance<JCheckBox>()
              .single { it.isShowing && it.text == "Allow free-writing mode" }
              .isSelected == allowFreeWriting &&
          (requiredChecks == null ||
              requirementLabels.all { (id, title) ->
                widgets
                    .filterIsInstance<JCheckBox>()
                    .single { it.isShowing && it.text == title }
                    .isSelected == (id in requiredChecks)
              })
    }
    captureComponent(dialog.rootPane, output, prefix, label)
    onModalUi { visibleLessonButton(dialog.rootPane, "OK").doClick() }
    awaitUi { !dialog.isShowing }
    failure.get()?.let { throw it }
  }

  /**
   * Only the dedicated authoring fixture opts into closing; the native API saves before disposal.
   */
  private fun closeProject(project: Project, output: Path) =
      closeBookCaptureProject(project, output)

  private fun configurePresentation(project: Project) = configureBookPresentation(project)

  /** Reopen existing work without seeding progress or installing a solution. */
  private suspend fun captureResume(project: Project, plan: BookLessonCapturePlan, output: Path) {
    val directory = Path.of(plan.projectRoot).resolve("mars-labs/console")
    val source = directory.resolve("independent.asm")
    withContext(Dispatchers.IO) {
      check(
          captureSha256(Files.readAllBytes(directory.resolve("first-program.asm"))) ==
              plan.sourceSha256,
      )
      check(captureSha256(Files.readAllBytes(source)) == plan.independentSourceSha256)
    }
    val passes = AtomicInteger()
    val listener =
        object : LessonListener {
          override fun lessonPassed(lesson: Lesson) {
            passes.incrementAndGet()
          }
        }
    val lesson =
        withContext(Dispatchers.EDT) {
          configurePresentation(project)
          CourseManager.instance.modules
              .single { it.id == "mars.console" }
              .lessons
              .single { it.id == "mars.console.independent" }
              .also {
                it.addLessonListener(listener)
                CourseManager.instance.openLesson(
                    project,
                    it,
                    LessonStartingWay.LEARN_TAB,
                    forceStartLesson = true,
                )
              }
        }
    try {
      awaitFrame(project, "", source)
      val learnShown = CompletableDeferred<Unit>()
      withContext(Dispatchers.EDT) {
        checkNotNull(ToolWindowManager.getInstance(project).getToolWindow("Learn")).show {
          learnShown.complete(Unit)
        }
      }
      learnShown.await()
      captureFrame(project, output, "reopened-start", "")
      if (plan.expectIndependentRecheck) {
        awaitFrame(project, "implement 3x+1", source)
        delay(600) // Several native timer checks must reject the old source evidence.
        check(passes.get() == 0) { "Offline edit reused an earlier source check" }
        captureFrame(project, output, "reopened-recheck-required", "implement 3x+1")
        if (plan.captureHints)
            captureHints(project, output, "reopened-hints", existing = 4, revealTo = 4)
        checkIndependent(project, output, "reopened-rechecked-feedback", passed = true)
      }
      awaitUi { passes.get() == 1 }
      awaitFrame(project, "Your current source passed all five", source)
      captureFrame(project, output, "reopened-complete", "Your current source passed all five")
      withContext(Dispatchers.IO) {
        check(captureSha256(Files.readAllBytes(source)) == plan.independentSourceSha256)
        Files.writeString(
            output.resolve("complete.json"),
            GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(
                    mapOf(
                        "platformBuild" to ApplicationInfo.getInstance().build.asString(),
                        "resumedIndependent" to true,
                        "nativeIndependentPassesObserved" to passes.get(),
                        "checkActionInvoked" to plan.expectIndependentRecheck,
                        "offlineEditRequiredRecheck" to plan.expectIndependentRecheck,
                        "hintHistoryObserved" to
                            (plan.expectIndependentRecheck && plan.captureHints),
                        "sourceUnchangedByResume" to true,
                        "independentSourceSha256" to plan.independentSourceSha256,
                        "marsPluginArchiveSha256" to
                            captureSha256(
                                Files.readAllBytes(
                                    Path.of(
                                        checkNotNull(System.getProperty("mars.book.mars.archive")),
                                    ),
                                ),
                            ),
                        "scope" to
                            "Existing project in a fresh IDE process; no progress injection or solution installation",
                    ),
                ),
            CREATE_NEW,
        )
      }
    } finally {
      val observation =
          withContext(NonCancellable + Dispatchers.EDT) {
            lesson.removeLessonListener(listener)
            mapOf(
                "nativeIndependentPassesObserved" to passes.get(),
                "selectedSource" to
                    FileEditorManager.getInstance(project).selectedTextEditor?.let {
                      FileDocumentManager.getInstance().getFile(it.document)?.path
                    },
                "nativeVisibleText" to
                    WindowManager.getInstance().getFrame(project)?.let {
                      lessonVisibleText(it.rootPane)
                    },
            )
          }
      withContext(NonCancellable + Dispatchers.IO) {
        Files.writeString(
            output.resolve("resume-state.json"),
            GsonBuilder().setPrettyPrinting().create().toJson(observation),
            CREATE_NEW,
        )
      }
    }
  }

  private suspend fun captureIndependent(
      project: Project,
      output: Path,
      source: Path,
      plan: BookLessonCapturePlan,
  ) {
    val solution =
        withContext(Dispatchers.IO) {
          Files.readString(Path.of(plan.independentSolutionPath)).also {
            check(captureSha256(it.toByteArray(Charsets.UTF_8)) == plan.independentSolutionSha256)
          }
        }
    val passes = AtomicInteger()
    val listener =
        object : LessonListener {
          override fun lessonPassed(lesson: Lesson) {
            passes.incrementAndGet()
          }
        }
    val lesson =
        withContext(Dispatchers.EDT) {
          CourseManager.instance.modules
              .single { it.id == "mars.console" }
              .lessons
              .single { it.id == "mars.console.independent" }
              .also {
                it.addLessonListener(listener)
                CourseManager.instance.openLesson(
                    project,
                    it,
                    LessonStartingWay.LEARN_TAB,
                    forceStartLesson = true,
                )
              }
        }
    try {
      awaitFrame(project, "implement 3x+1", source)
      captureFrame(project, output, "independent-ready", "implement 3x+1")
      checkIndependent(project, output, "independent-starter-feedback", passed = false)
      check(passes.get() == 0)
      if (plan.captureHints) {
        val beforeHints = withContext(Dispatchers.IO) { Files.readString(source) }
        captureHints(project, output, "hints-initial", existing = 0, revealTo = 2)
        captureHints(project, output, "hints-reopened", existing = 2, revealTo = 4)
        check(withContext(Dispatchers.IO) { Files.readString(source) } == beforeHints)
        check(passes.get() == 0)
      }
      withContext(Dispatchers.EDT) {
        val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
        WriteCommandAction.runWriteCommandAction(project) { editor.document.setText(solution) }
        FileDocumentManager.getInstance().saveDocument(editor.document)
      }
      checkIndependent(project, output, "independent-passed-feedback", passed = true)
      awaitUi { passes.get() == 1 }
      awaitFrame(project, "Your current source passed all five", source)
      captureFrame(project, output, "independent-complete", "Your current source passed all five")
      var expectedPasses = 1
      withContext(Dispatchers.EDT) {
        val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
        WriteCommandAction.runWriteCommandAction(project) {
          editor.document.insertString(0, "# Source edit requires a fresh check.\n")
        }
        FileDocumentManager.getInstance().saveDocument(editor.document)
        CourseManager.instance.openLesson(
            project,
            lesson,
            LessonStartingWay.RESTART_BUTTON,
            forceStartLesson = true,
        )
      }
      awaitFrame(project, "implement 3x+1", source)
      delay(600) // Allow several native timer checks to reject the invalidated earlier pass.
      check(passes.get() == expectedPasses)
      if (plan.captureHints)
          captureHints(project, output, "hints-after-edit", existing = 4, revealTo = 4)
      captureFrame(project, output, "independent-edited", "implement 3x+1")
      checkIndependent(project, output, "independent-rechecked-feedback", passed = true)
      expectedPasses++
      awaitUi { passes.get() == expectedPasses }
      val retained = withContext(Dispatchers.IO) { Files.readString(source) }
      withContext(Dispatchers.EDT) {
        CourseManager.instance.openLesson(
            project,
            lesson,
            LessonStartingWay.RESTART_BUTTON,
            forceStartLesson = true,
        )
      }
      expectedPasses++
      awaitUi { passes.get() == expectedPasses }
      withContext(Dispatchers.IO) {
        check(Files.readString(source) == retained)
        Files.writeString(
            output.resolve("independent-observations.json"),
            GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(
                    mapOf(
                        "starterRejected" to true,
                        "publicCasesObservedPerCheck" to 5,
                        "nativeIndependentPassesObserved" to passes.get(),
                        "sourceEditRequiredRecheck" to true,
                        "workRetainedAfterRestart" to true,
                        "hintHistoryObserved" to plan.captureHints,
                        "solutionSha256" to plan.independentSolutionSha256,
                        "retainedSourceSha256" to
                            captureSha256(retained.toByteArray(Charsets.UTF_8)),
                        "scope" to
                            "Public formative checks and lesson restart; project reopen and human review remain separate",
                    ),
                ),
            CREATE_NEW,
        )
      }
    } finally {
      withContext(NonCancellable + Dispatchers.EDT) { lesson.removeLessonListener(listener) }
    }
  }

  private suspend fun captureHints(
      project: Project,
      output: Path,
      prefix: String,
      existing: Int,
      revealTo: Int,
      released: Int = 4,
  ) {
    val titles =
        listOf(
            "Plan the value flow",
            "Keep the original input available",
            "Combine the values deliberately",
            "Trace one possible calculation",
        )
    val failure = AtomicReference<Throwable>()
    clickLessonLink(project, "Open a hint")
    val dialog = awaitDialog(failure, "Independent program hints")
    suspend fun assertRevealed(count: Int) {
      awaitUi {
        val text = lessonVisibleText(dialog.rootPane)
        titles.take(count).all(text::contains) && titles.drop(count).none(text::contains)
      }
    }
    assertRevealed(existing)
    check(onModalUi { lessonVisibleText(dialog.rootPane).contains("releases $released of 4") })
    captureComponent(dialog.rootPane, output, "$prefix-open", "Reveal as much help as you need")
    for (index in existing until revealTo) {
      onModalUi { visibleLessonButton(dialog.rootPane, "Reveal next hint").doClick() }
      assertRevealed(index + 1)
      captureComponent(dialog.rootPane, output, "$prefix-${index + 1}", titles[index])
    }
    if (revealTo >= released)
        check(onModalUi { !visibleLessonButton(dialog.rootPane, "Reveal next hint").isEnabled })
    onModalUi { visibleLessonButton(dialog.rootPane, "Return to editor").doClick() }
    awaitUi { !dialog.isShowing }
  }

  private suspend fun checkIndependent(
      project: Project,
      output: Path,
      id: String,
      passed: Boolean,
      limited: Boolean = false,
      status: String? = null,
  ) {
    val failure = AtomicReference<Throwable>()
    clickLessonLink(project, "Check my program")
    val dialog = awaitDialog(failure, "Independent program results")
    val expected =
        if (limited) "completed assessments allowed by your course"
        else if (passed) "PASS input 20" else "FAIL input 20"
    awaitUi { lessonVisibleText(dialog.rootPane).contains(expected) }
    val report = onModalUi { lessonVisibleText(dialog.rootPane) }
    if (limited) check(!report.contains("PASS input ") && !report.contains("FAIL input "))
    else
        check(
            report.lines().count { it.startsWith(if (passed) "PASS input " else "FAIL input ") } ==
                5,
        )
    if (status != null) check(report.contains(status))
    captureComponent(dialog.rootPane, output, id, expected)
    if (status != null && !limited) {
      onModalUi {
        val text =
            captureComponents(dialog.rootPane).filterIsInstance<JTextComponent>().single {
              it.isShowing
            }
        text.caretPosition = text.document.length
      }
      delay(300) // Let the native text viewport paint its assessment-count footer.
      captureComponent(dialog.rootPane, output, "$id-status", status)
    }
    onModalUi { visibleLessonButton(dialog.rootPane, "Return to editor").doClick() }
    awaitUi { !dialog.isShowing }
  }

  private suspend fun captureRun(
      project: Project,
      output: Path,
      source: Path,
      repair: Boolean = false,
  ) {
    val id = if (repair) "mars.console.repair" else "mars.console.run"
    val prefix = if (repair) "repair" else "run"
    val readyText =
        if (repair) "repair-output.asm is your working copy"
        else "Run first-program.asm using the normal MARS Run"
    val completeText =
        if (repair) "The repaired source printed 41"
        else "The console result agrees with the prediction"
    val passes = AtomicInteger()
    val passed = CompletableDeferred<Unit>()
    val listener =
        object : LessonListener {
          override fun lessonPassed(lesson: Lesson) {
            passes.incrementAndGet()
            passed.complete(Unit)
          }
        }
    val lesson =
        withContext(Dispatchers.EDT) {
          CourseManager.instance.modules
              .single { it.id == "mars.console" }
              .lessons
              .single { it.id == id }
              .also {
                it.addLessonListener(listener)
                CourseManager.instance.openLesson(
                    project,
                    it,
                    LessonStartingWay.LEARN_TAB,
                    forceStartLesson = true,
                )
              }
        }
    val connection = project.messageBus.connect()
    var owned: ProcessHandler? = null
    try {
      awaitFrame(project, readyText, source)
      captureFrame(project, output, "$prefix-ready", readyText)
      if (repair) {
        withContext(Dispatchers.EDT) {
          val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
          val faulty = "move \$a0, \$v0"
          val text = editor.document.text
          check(text.split(faulty).size == 2)
          WriteCommandAction.runWriteCommandAction(project) {
            editor.document.setText(text.replace(faulty, "or \$a0, \$t0, \$zero"))
          }
          FileDocumentManager.getInstance().saveDocument(editor.document)
        }
      }
      val process = CompletableDeferred<ProcessHandler>()
      val exit = CompletableDeferred<Int>()
      val transcript = StringBuilder()
      val name = "Book lesson $prefix observation"
      connection.subscribe(
          ExecutionManager.EXECUTION_TOPIC,
          object : ExecutionListener {
            override fun processStarting(
                executorId: String,
                env: ExecutionEnvironment,
                handler: ProcessHandler,
            ) {
              if (executorId == DefaultRunExecutor.EXECUTOR_ID && env.runProfile.name == name) {
                handler.addProcessListener(
                    object : ProcessListener {
                      override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                        if (outputType == ProcessOutputTypes.STDOUT)
                            synchronized(transcript) { transcript.append(event.text) }
                      }

                      override fun processTerminated(event: ProcessEvent) {
                        exit.complete(event.exitCode)
                      }
                    },
                )
                process.complete(handler)
              }
            }
          },
      )
      withContext(Dispatchers.EDT) {
        val type =
            ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.single { it.id == "MarsMipsRun" }
        val settings =
            RunManager.getInstance(project)
                .createConfiguration(name, type.configurationFactories.single())
        val state = Element("configuration").setAttribute("marsExecutionDefaults", "1")
        state.addContent(
            Element("option")
                .setAttribute("name", "sourcePath")
                .setAttribute("value", source.toString()),
        )
        settings.configuration.readExternal(state)
        settings.configuration.checkConfiguration()
        ExecutionEnvironmentBuilder.create(DefaultRunExecutor.getRunExecutorInstance(), settings)
            .buildAndExecute()
      }
      owned = process.await()
      while (!owned.isStartNotified) delay(50)
      withContext(Dispatchers.IO) { deliverBookStandardInput(owned, "20\n") }
      check(exit.await() == 0)
      val observed = synchronized(transcript) { transcript.toString() }
      check(observed == "Enter a small integer: Twice your number plus one is: 41\n")
      withContext(Dispatchers.IO) {
        Files.writeString(
            output.resolve("$prefix-transcript.json"),
            GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(
                    mapOf(
                        "exitCode" to 0,
                        "stdin" to "20\n",
                        "stdout" to observed,
                        "scope" to
                            "Actual Run only; native lesson completion must also be observed",
                    ),
                ),
            CREATE_NEW,
        )
      }
      passed.await()
      awaitFrame(project, completeText, source)
      captureFrame(project, output, "$prefix-complete", completeText)
      if (repair) {
        val retained = withContext(Dispatchers.IO) { Files.readString(source) }
        withContext(Dispatchers.EDT) {
          CourseManager.instance.openLesson(
              project,
              lesson,
              LessonStartingWay.RESTART_BUTTON,
              forceStartLesson = true,
          )
        }
        awaitFrame(project, completeText, source)
        awaitUi { passes.get() >= 2 }
        withContext(Dispatchers.IO) {
          check(Files.readString(source) == retained)
          val counterexample = Files.readString(source.resolveSibling("wrong-output.asm"))
          check(counterexample.contains("move \$a0, \$v0"))
          Files.writeString(
              output.resolve("repair-preservation.json"),
              GsonBuilder()
                  .setPrettyPrinting()
                  .create()
                  .toJson(
                      mapOf(
                          "nativeRepairPassesObserved" to passes.get(),
                          "repairRetainedAfterRestart" to true,
                          "repairSha256" to captureSha256(retained.toByteArray(Charsets.UTF_8)),
                          "counterexampleSha256" to
                              captureSha256(counterexample.toByteArray(Charsets.UTF_8)),
                          "alternativePreparation" to "or \$a0, \$t0, \$zero",
                      ),
                  ),
              CREATE_NEW,
          )
        }
      }
    } finally {
      connection.disconnect()
      withContext(NonCancellable + Dispatchers.EDT) {
        lesson.removeLessonListener(listener)
        owned?.takeUnless { it.isProcessTerminated }?.destroyProcess()
      }
    }
  }

  private suspend fun captureDebug(
      project: Project,
      output: Path,
      source: Path,
      outputPreparation: Boolean = false,
      diagnosis: Boolean = false,
  ) {
    val outputStyle = outputPreparation || diagnosis
    val id =
        if (diagnosis) "mars.console.diagnosis"
        else if (outputPreparation) "mars.console.output" else "mars.console.debug"
    val prefix = if (diagnosis) "diagnosis" else if (outputPreparation) "output" else "debug"
    val passed = CompletableDeferred<Unit>()
    val listener =
        object : LessonListener {
          override fun lessonPassed(lesson: Lesson) {
            passed.complete(Unit)
          }
        }
    val lesson =
        withContext(Dispatchers.EDT) {
          CourseManager.instance.modules
              .single { it.id == "mars.console" }
              .lessons
              .single { it.id == id }
              .also {
                it.addLessonListener(listener)
                CourseManager.instance.openLesson(
                    project,
                    it,
                    LessonStartingWay.LEARN_TAB,
                    forceStartLesson = true,
                )
              }
        }
    if (outputPreparation) {
      awaitFrame(project, "Track the most recent writer", source)
      answer(
          project,
          output,
          "output",
          "Mars.Learning.PredictOutput",
          "\$v0 is 20; \$a0 is 41; \$t0 is 0.",
          "\$v0 no longer holds the input:",
          "\$v0 is 1; \$a0 is 41; \$t0 is 41.",
      )
    }
    if (diagnosis) {
      awaitFrame(project, "Find the last writer", source)
      answer(
          project,
          output,
          "wrong-output",
          "Mars.Learning.PredictWrongOutput",
          "20",
          "The input call returned 20",
          "4",
      )
    }
    awaitFrame(
        project,
        if (diagnosis) "Stop other Debug sessions"
        else if (outputPreparation) "Stop the preceding Debug session"
        else "Set a breakpoint on addu",
        source,
    )
    val file =
        withContext(Dispatchers.EDT) {
          checkNotNull(LocalFileSystem.getInstance().findFileByNioFile(source))
        }
    val line =
        withContext(Dispatchers.IO) {
          Files.readString(source).lines().indexOfFirst {
            it.trim().startsWith(if (outputStyle) "move " else "addu ")
          }
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
    val started = CompletableDeferred<XDebugSession>()
    val pauses = Channel<Int>(Channel.UNLIMITED)
    val connection = project.messageBus.connect()
    var owned: XDebugSession? = null
    val rows = mutableListOf<Map<String, Any>>()
    try {
      val name = "Book lesson $prefix observation"
      connection.subscribe(
          ExecutionManager.EXECUTION_TOPIC,
          object : ExecutionListener {
            override fun processStarting(
                executorId: String,
                env: ExecutionEnvironment,
                handler: ProcessHandler,
            ) {
              if (executorId == DefaultDebugExecutor.EXECUTOR_ID && env.runProfile.name == name) {
                val session =
                    XDebuggerManager.getInstance(project).debugSessions.single {
                      it.debugProcess.processHandler === handler
                    }
                session.addSessionListener(
                    object : XDebugSessionListener {
                      override fun sessionPaused() {
                        val position = checkNotNull(session.topFramePosition)
                        check(position.file.toNioPath() == source)
                        pauses.trySend(position.line)
                      }
                    },
                )
                started.complete(session)
              }
            }
          },
      )
      withContext(Dispatchers.EDT) {
        val runType =
            ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.single { it.id == "MarsMipsRun" }
        val settings =
            RunManager.getInstance(project)
                .createConfiguration(name, runType.configurationFactories.single())
        val state = Element("configuration").setAttribute("marsExecutionDefaults", "1")
        state.addContent(
            Element("option")
                .setAttribute("name", "sourcePath")
                .setAttribute("value", source.toString()),
        )
        settings.configuration.readExternal(state)
        settings.configuration.checkConfiguration()
        ExecutionEnvironmentBuilder.create(
                DefaultDebugExecutor.getDebugExecutorInstance(),
                settings,
            )
            .buildAndExecute()
      }
      val session = started.await()
      owned = session
      val handler = session.debugProcess.processHandler
      while (!handler.isStartNotified) delay(50)
      withContext(Dispatchers.IO) { deliverBookStandardInput(handler, "20\n") }
      for ((index, expected) in
          (if (outputStyle) listOf(41L, 41L, 41L) else listOf(0L, 40L, 41L)).withIndex()) {
        val sourceLine = pauses.receive()
        check(sourceLine >= line)
        val text =
            if (diagnosis)
                when (index) {
                  0 -> "Stop other Debug sessions"
                  1 -> "Step Over li"
                  else -> "The arithmetic result is correct, but the output argument is wrong."
                }
            else if (outputPreparation)
                when (index) {
                  0 -> "Stop the preceding Debug session"
                  1 -> "Step Over li"
                  else -> "You observed the answer survive"
                }
            else
                when (index) {
                  0 -> "Set a breakpoint on addu"
                  1 -> "The first addition produced 40"
                  else -> "You have observed the input"
                }
        if (index == 2 && !diagnosis) passed.await()
        awaitFrame(project, text, source)
        val registers = buildMap {
          put("\$v0", if (outputStyle) (if (index == 2) 1L else 4L) else 20L)
          put("\$t0", expected)
          if (outputStyle && index > 0) put("\$a0", if (diagnosis) 4L else 41L)
        }
        val observed = awaitDebugRegisters(project, handler, registers)
        rows +=
            mapOf(
                "sourceLine" to sourceLine + 1,
                "standardInput" to 20,
                "expectedRegisters" to registers,
                "renderedRows" to observed,
            )
        captureFrame(
            project,
            output,
            "$prefix-" +
                (if (outputStyle) listOf("before-copy", "after-copy", "before-syscall")
                else listOf("before-add", "after-add", "after-one"))[index],
            text,
        )
        if (index < 2) withContext(Dispatchers.EDT) { session.stepOver(false) }
      }
      if (diagnosis) {
        answer(
            project,
            output,
            "diagnosis",
            "Mars.Learning.DiagnoseOutput",
            "Change the arithmetic because the printed answer is wrong.",
            "The stopped arithmetic result is already 41",
            "Prepare the integer-output argument from the retained answer instead of the service selector.",
        )
        passed.await()
        awaitFrame(project, "You located an argument error", source)
        captureFrame(project, output, "diagnosis-complete", "You located an argument error")
      }
      withContext(Dispatchers.IO) {
        Files.writeString(
            output.resolve("$prefix-observations.json"),
            GsonBuilder().setPrettyPrinting().create().toJson(rows),
            CREATE_NEW,
        )
      }
    } finally {
      connection.disconnect()
      pauses.close()
      withContext(NonCancellable + Dispatchers.EDT) {
        lesson.removeLessonListener(listener)
        owned?.stop()
      }
      withContext(NonCancellable) {
        writeAction {
          XDebuggerManager.getInstance(project).breakpointManager.removeBreakpoint(breakpoint)
        }
      }
    }
  }

  private suspend fun awaitDebugRegisters(
      project: Project,
      handler: ProcessHandler,
      expected: Map<String, Long>,
  ): List<String> = awaitBookDebugRegisters(project, handler, expected)

  private suspend fun answer(
      project: Project,
      output: Path,
      id: String,
      actionId: String,
      wrong: String,
      feedback: String,
      correct: String,
  ) {
    // Exercise the learner's actual in-pane links. The legacy action ID is retained
    // only in the receipt; no prediction dialog or Find Action interaction is invoked.
    val focus =
        withContext(Dispatchers.EDT) {
          val editor = checkNotNull(FileEditorManager.getInstance(project).selectedTextEditor)
          val rectangle =
              checkNotNull(LearningUiHighlightingManager.getRectangle(editor.contentComponent))
          val line = editor.document.getLineNumber(editor.caretModel.offset)
          val text =
              editor.document
                  .getText(
                      TextRange(
                          editor.document.getLineStartOffset(line),
                          editor.document.getLineEndOffset(line),
                      ),
                  )
                  .trim()
          val mnemonic =
              when (actionId) {
                "Mars.Learning.PredictDoubling" -> "addu"
                "Mars.Learning.PredictNegative" -> "addiu"
                else -> "move"
              }
          check(text.startsWith("$mnemonic ")) { "Question points to the wrong instruction: $text" }
          mapOf(
              "sourceLine" to line + 1,
              "instruction" to text,
              "highlight" to
                  mapOf(
                      "x" to rectangle.x,
                      "y" to rectangle.y,
                      "width" to rectangle.width,
                      "height" to rectangle.height,
                  ),
          )
        }
    clickLessonLink(project, wrong, answerChoice = true)
    val frame =
        withContext(Dispatchers.EDT) { checkNotNull(WindowManager.getInstance().getFrame(project)) }
    awaitUi { lessonVisibleText(frame.rootPane).contains(feedback) }
    captureFrame(project, output, "$id-feedback", feedback)
    clickLessonLink(project, correct, answerChoice = true)
    awaitUi {
      val text = lessonVisibleText(frame.rootPane)
      bookInlineChoiceOffset(text, wrong) == null && bookInlineChoiceOffset(text, correct) == null
    }
    withContext(Dispatchers.IO) {
      Files.writeString(
          output.resolve("$id-inline-answer.json"),
          GsonBuilder()
              .setPrettyPrinting()
              .create()
              .toJson(
                  mapOf(
                      "route" to "native lesson callback links",
                      "legacyActionId" to actionId,
                      "legacyActionInvoked" to false,
                      "completedChoicesRetired" to true,
                      "wrongChoice" to wrong,
                      "feedback" to feedback,
                      "correctChoice" to correct,
                      "focus" to focus,
                  ),
              ),
          CREATE_NEW,
      )
    }
  }

  private suspend fun clickLessonLink(
      project: Project,
      answer: String,
      answerChoice: Boolean = false,
  ) {
    fun target(): Pair<JTextPane, Int>? {
      val frame = WindowManager.getInstance().getFrame(project) ?: return null
      return captureComponents(frame.rootPane)
          .filterIsInstance<JTextPane>()
          .filter { it.isShowing }
          .mapNotNull { pane ->
            val text = pane.document.getText(0, pane.document.length)
            val offset =
                if (answerChoice) bookInlineChoiceOffset(text, answer)
                else bookLessonActionOffset(text, answer)
            offset?.let { pane to it }
          }
          .singleOrNull()
    }
    awaitUi {
      val (pane, offset) = target() ?: return@awaitUi false
      val rectangle = bookLessonLinkBounds(pane, offset) ?: return@awaitUi false
      pane.scrollRectToVisible(rectangle)
      pane.visibleRect.intersects(rectangle)
    }
    val point =
        withContext(Dispatchers.EDT) {
          val (pane, offset) = checkNotNull(target())
          val bounds = checkNotNull(bookLessonLinkBounds(pane, offset))
          Logger.getInstance(BookLessonCaptureActivity::class.java)
              .info(
                  "Lesson link click: label=$answer; offset=$offset; rawBounds=${pane.modelToView2D(offset)}; clickBounds=$bounds",
              )
          Point(bounds.x + 2, bounds.y + bounds.height / 2).also {
            SwingUtilities.convertPointToScreen(it, pane)
          }
        }
    withContext(Dispatchers.IO) {
      Robot().apply {
        mouseMove(point.x, point.y)
        mousePress(InputEvent.BUTTON1_DOWN_MASK)
        mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
      }
    }
  }

  private suspend fun awaitDialog(
      failure: AtomicReference<Throwable>,
      title: String = "Predict the register state",
  ): JDialog {
    while (true) {
      failure.get()?.let { throw it }
      val dialog = onModalUi {
        Window.getWindows().filterIsInstance<JDialog>().singleOrNull {
          it.isShowing && it.title == title
        }
      }
      if (dialog != null) return dialog
      delay(100)
    }
  }

  private suspend fun awaitFrame(project: Project, text: String, source: Path) {
    var polls = 0
    while (true) {
      val ready =
          withContext(Dispatchers.EDT) {
            val frame = WindowManager.getInstance().getFrame(project)
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            val file = editor?.let { FileDocumentManager.getInstance().getFile(it.document) }
            frame?.isShowing == true &&
                file?.toNioPath() == source &&
                lessonVisibleText(frame.rootPane).contains(text)
          }
      if (ready) {
        delay(300)
        return
      }
      if (++polls == 50) {
        val observation =
            withContext(Dispatchers.EDT) {
              val frame = WindowManager.getInstance().getFrame(project)
              val editor = FileEditorManager.getInstance(project).selectedTextEditor
              val file = editor?.let { FileDocumentManager.getInstance().getFile(it.document) }
              "expectedText=$text; expectedFile=$source; actualFile=${file?.toNioPath()}; " +
                  "frameShowing=${frame?.isShowing}; visibleText=${frame?.let { lessonVisibleText(it.rootPane) }}"
            }
        Logger.getInstance(BookLessonCaptureActivity::class.java)
            .warn("Lesson capture wait: $observation")
      }
      delay(100)
    }
  }

  private suspend fun awaitUi(predicate: () -> Boolean) {
    while (!onModalUi(predicate)) delay(100)
    delay(200)
  }

  private suspend fun captureFrame(project: Project, output: Path, id: String, text: String) {
    val root =
        withContext(Dispatchers.EDT) {
          checkNotNull(WindowManager.getInstance().getFrame(project)).rootPane
        }
    val layout =
        withContext(Dispatchers.EDT) {
          checkNotNull(lessonHorizontalLayout(root)).also {
            check(it.overflowPixels == 0) { "Native lesson is clipped horizontally: $it" }
          }
        }
    captureComponent(root, output, id, text)
    withContext(Dispatchers.IO) {
      Files.writeString(
          output.resolve("$id-layout.json"),
          GsonBuilder().setPrettyPrinting().create().toJson(layout),
          CREATE_NEW,
      )
    }
  }

  private suspend fun captureComponent(root: JComponent, output: Path, id: String, text: String) =
      captureBookComponent(root, output, id, text)

  private suspend fun <T> onModalUi(action: () -> T): T =
      withContext(Dispatchers.EDT + ModalityState.any().asContextElement()) { action() }
}

internal fun lessonVisibleText(root: JComponent): String =
    captureComponents(root)
        .filter { it.isShowing }
        .mapNotNull {
          when (it) {
            is JTextComponent -> it.document.getText(0, it.document.length)
            is JLabel -> it.text
            is AbstractButton -> it.text
            else -> null
          }
        }
        .joinToString("\n")

internal fun visibleLessonButton(root: JComponent, text: String): AbstractButton =
    captureComponents(root).filterIsInstance<AbstractButton>().single {
      it.isShowing && it.text == text
    }

/** Read actual scalar register rendering; do not accept duplicate or ambiguous native rows. */
internal fun lessonRegisterValue(rows: List<String>, register: String): Long? {
  val matches = rows.filter { it.substringBefore(" = ").removeSuffix(" (written)") == register }
  check(matches.size <= 1) { "Ambiguous debugger register: $register" }
  val value = matches.singleOrNull()?.substringAfter(" = ")?.substringBefore(' ') ?: return null
  return if (value.startsWith("0x")) value.drop(2).toULongOrNull(16)?.toLong()
  else value.toLongOrNull()
}

internal val lessonDebugRegisterView =
    BookDebuggerTreeView(
        expand = listOf(listOf("Integer registers")),
        reveal = listOf("Integer registers", "\$t0"),
    )

/** Geometry from ordinary Swing components; native lesson implementation types are not accessed. */
internal data class BookLessonHorizontalLayout(
    val viewportWidth: Int,
    val panelWidth: Int,
    val overflowPixels: Int,
)

internal fun lessonHorizontalLayout(root: JComponent): BookLessonHorizontalLayout? {
  val panel =
      captureComponents(root).singleOrNull { it.name == "lessonPanel" && it.isShowing }
          ?: return null
  val viewport = panel.parent as? JViewport ?: return null
  val width = viewport.extentSize.width
  val components =
      captureComponents(panel).filter {
        it.name == "lessonMessagePane" || it is AbstractButton
      }
  val overflow =
      components.maxOfOrNull {
        val bounds = SwingUtilities.convertRectangle(it.parent, it.bounds, viewport)
        maxOf(0, -bounds.x, bounds.x + bounds.width - width)
      } ?: 0
  return BookLessonHorizontalLayout(width, panel.width, maxOf(0, panel.width - width, overflow))
}

/** Select answer text within a native lesson paragraph, rather than matching unrelated prose. */
internal fun bookInlineChoiceOffset(text: String, answer: String): Int? =
    Regex("(?m)^[ABC]\\. " + Regex.escape(answer) + "(?=\\R|$)").findAll(text).lastOrNull()?.let {
      it.range.first + minOf(4, it.value.length - 1)
    }

internal fun bookLessonActionOffset(text: String, label: String): Int? {
  require(label.isNotEmpty())
  val offset = text.lastIndexOf(label)
  return if (offset < 0) null else offset + minOf(4, label.lastIndex)
}

/** A Swing caret rectangle may have zero width even for a visible character. */
internal fun bookLessonLinkBounds(pane: JTextPane, offset: Int): Rectangle? =
    pane.modelToView2D(offset)?.bounds?.apply { width = width.coerceAtLeast(1) }
