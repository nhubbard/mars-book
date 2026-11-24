package edu.missouristate.mars.book

import com.intellij.execution.configurations.RunConfiguration
import java.nio.file.Path
import org.jdom.Element

internal fun configureBookCaptureLaunch(configuration: RunConfiguration, path: Path) {
  val state = Element("configuration").setAttribute("marsExecutionDefaults", "1")
  state.addContent(
      Element("option").setAttribute("name", "sourcePath").setAttribute("value", path.toString()),
  )
  state.addContent(
      Element("option")
          .setAttribute("name", "workingDirectory")
          .setAttribute("value", path.parent.toString()),
  )
  // Use the public persisted run-configuration contract; MARS's concrete option types
  // are internal. A fixture's main may follow earlier example code in the text section.
  state.addContent(
      Element("option").setAttribute("name", "startAtMain").setAttribute("value", "ENABLED"),
  )
  configuration.readExternal(state)
  configuration.checkConfiguration()
}
