package edu.missouristate.mars.book

import com.google.gson.GsonBuilder
import com.intellij.debugger.ui.DebuggerContentInfo.VARIABLES_CONTENT
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.RunContentManager
import com.intellij.ide.ui.LafManager
import com.intellij.notification.Notification
import com.intellij.notification.NotificationsManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import java.awt.Rectangle
import java.awt.Robot
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import javax.imageio.ImageIO
import javax.swing.JComponent
import javax.swing.JTree
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import training.lang.LangManager

internal suspend fun <T> bookModalUi(action: () -> T): T =
    withContext(Dispatchers.EDT + ModalityState.any().asContextElement()) { action() }

internal fun closeBookCaptureProject(project: Project, output: Path) {
    val application = ApplicationManager.getApplication()
    application.invokeLater({
        val closed = ProjectManager.getInstance().closeAndDispose(project)
        val disposed = project.isDisposed
        application.executeOnPooledThread {
            Files.writeString(output.resolve("project-closed.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
                "closed" to closed, "disposed" to disposed,
                "scope" to "Native save, close and dispose completed; a later IDE process must verify loaded progress",
            )), CREATE_NEW)
        }
    }, ModalityState.nonModal())
}

internal fun configureBookPresentation(project: Project) {
    val themes = LafManager.getInstance()
    themes.autodetect = false
    themes.setCurrentUIThemeLookAndFeel(themes.installedThemes.single { it.name == "IntelliJ Light" && !it.isDark })
    themes.updateUI()
    val colors = EditorColorsManager.getInstance()
    colors.setGlobalScheme(checkNotNull(colors.getScheme(EditorColorsManager.getDefaultSchemeName())))
    val frame = checkNotNull(WindowManager.getInstance().getFrame(project))
    val screen = frame.graphicsConfiguration.bounds
    frame.setBounds(screen.x + 20, screen.y + 40, minOf(1280, screen.width - 40), minOf(900, screen.height - 80))
    frame.toFront()
    LangManager.getInstance().updateLangSupport("MARS_MIPS")
}

internal suspend fun awaitBookDebugRegisters(project: Project, handler: ProcessHandler, expected: Map<String, Long>, vectors: Map<String, String> = emptyMap()): List<String> {
    val view = if (vectors.isEmpty()) lessonDebugRegisterView else BookDebuggerTreeView(
        expand = listOf(listOf("Integer registers"), listOf("MSA vector registers"), listOf("MSA vector registers", "\$w4")),
        reveal = listOf("MSA vector registers", "\$w4"),
    )
    while (true) {
        val rows = withContext(Dispatchers.EDT) {
            val descriptor = RunContentManager.getInstance(project).findContentDescriptor(
                DefaultDebugExecutor.getDebugExecutorInstance(), handler,
            ) ?: return@withContext emptyList()
            val ui = descriptor.runnerLayoutUi ?: return@withContext emptyList()
            val content = ui.findContent(VARIABLES_CONTENT) ?: ui.contents.singleOrNull { isRegisterCaptureTab(it.displayName) }
                ?: return@withContext emptyList()
            if (!content.component.isShowing) {
                ui.selectAndFocus(content, false, true)
                return@withContext emptyList()
            }
            val trees = captureComponents(content.component).filterIsInstance<JTree>().filter { it.isShowing }.toList()
            val tree = selectDebuggerTree(trees, view)
            if (tree == null || !applyDebuggerTreeView(tree, view)) emptyList()
            else (0 until tree.rowCount).map { renderedDebuggerRow(tree, tree.getPathForRow(it)) }
        }
        if (expected.all { (register, value) -> lessonRegisterValue(rows, register) == value } && vectors.all { (register, value) -> lessonVectorValue(rows, register) == value.lowercase() }) return rows
        delay(100)
    }
}

internal suspend fun captureBookComponent(root: JComponent, output: Path, id: String, text: String, visible: () -> Boolean = { lessonVisibleText(root).contains(text) }) {
    // Match the established raw-figure driver: dismiss native update balloons before photography.
    bookModalUi {
        val manager = NotificationsManager.getNotificationsManager()
        manager.getNotificationsOfType(Notification::class.java, null).toList()
            .mapNotNull { it.balloon }.filterNot { it.isDisposed }.distinct().forEach { it.hide() }
    }
    delay(150)
    val bounds = bookModalUi {
        check(visible()) { "Expected lesson text is not visible: $text" }
        check(LafManager.getInstance().currentUIThemeLookAndFeel?.name == "IntelliJ Light")
        Rectangle(root.locationOnScreen, root.size).also {
            requireVisibleCaptureViewport(it, root.graphicsConfiguration.bounds)
        }
    }
    withContext(Dispatchers.IO) {
        val image = Robot().createScreenCapture(bounds)
        val photograph = requireBookLightPhotograph(image)
        val png = output.resolve("$id.png")
        Files.newOutputStream(png, CREATE_NEW).use { check(ImageIO.write(image, "png", it)) }
        Files.writeString(output.resolve("$id.json"), GsonBuilder().setPrettyPrinting().create().toJson(mapOf(
            "theme" to "IntelliJ Light", "assertionsPassed" to true, "observedText" to text,
            "photograph" to photograph, "width" to image.width, "height" to image.height, "pngSha256" to captureSha256(Files.readAllBytes(png)),
        )), CREATE_NEW)
    }
}
