package edu.missouristate.mars.book

import com.intellij.execution.ExecutionListener
import com.intellij.execution.ExecutionManager
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.util.messages.MessageBusConnection
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebugSessionListener
import com.intellij.xdebugger.XDebuggerManager
import java.nio.file.Path
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import org.jdom.Element

/** Lifecycle-owned observations of ordinary native Run/Debug launches, for authoring only. */
internal class BookProgramCapture(val connection: MessageBusConnection, private val lifetime: Disposable) : AutoCloseable {
    val started = CompletableDeferred<ProcessHandler>()
    val exit = CompletableDeferred<Int>()
    val pauses = Channel<Int>(Channel.UNLIMITED)
    val stdout = StringBuffer()
    var session: XDebugSession? = null
    override fun close() { Disposer.dispose(lifetime); pauses.close() }
}

internal suspend fun launchBookProgram(project: Project, source: Path, debug: Boolean,
    name: String = if (debug) "Book imported Debug" else "Book imported Run",
): BookProgramCapture {
    val lifetime = Disposer.newDisposable("MARS book execution capture")
    Disposer.register(project, lifetime)
    val launch = BookProgramCapture(project.messageBus.connect(lifetime), lifetime)
    try {
        launch.connection.subscribe(ExecutionManager.EXECUTION_TOPIC, object : ExecutionListener {
            override fun processStarting(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
                if (env.runProfile.name != name) return
                val listener = object : ProcessListener {
                    override fun processTerminated(event: ProcessEvent) { launch.exit.complete(event.exitCode) }
                    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                        if (ProcessOutputType.isStdout(outputType)) launch.stdout.append(event.text)
                    }
                }
                handler.addProcessListener(listener)
                Disposer.register(lifetime) { handler.removeProcessListener(listener) }
                if (debug) {
                    val session = XDebuggerManager.getInstance(project).debugSessions.single { it.debugProcess.processHandler === handler }
                    launch.session = session
                    session.addSessionListener(object : XDebugSessionListener {
                        override fun sessionPaused() { launch.pauses.trySend(checkNotNull(session.topFramePosition).line) }
                    }, lifetime)
                }
                launch.started.complete(handler)
            }
        })
        withContext(Dispatchers.EDT) {
            val type = ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.single { it.id == "MarsMipsRun" }
            val settings = RunManager.getInstance(project).createConfiguration(name, type.configurationFactories.single())
            settings.configuration.readExternal(Element("configuration").setAttribute("marsExecutionDefaults", "1")
                .addContent(Element("option").setAttribute("name", "sourcePath").setAttribute("value", source.toString())))
            settings.configuration.checkConfiguration()
            val executor = if (debug) DefaultDebugExecutor.getDebugExecutorInstance() else DefaultRunExecutor.getRunExecutorInstance()
            ExecutionEnvironmentBuilder.create(executor, settings).buildAndExecute()
        }
        return launch
    } catch (error: Throwable) { launch.close(); throw error }
}
