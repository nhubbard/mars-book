package edu.missouristate.mars.book

import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.application.EDT
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import java.nio.file.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jdom.Element
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class BookCaptureLaunchTest {
    private val projectFixture = codeInsightProjectFixture()
    private val fixture by codeInsightFixture(projectFixture)

    @Test
    fun `native MARS configuration retains explicit main entry for later main fixtures`() = timeoutRunBlocking {
        val path = Files.createTempFile("book-later-main-", ".asm")
        try {
            Files.writeString(path, ".text\nearlier_example: nop\nmain: nop\n")
            withContext(Dispatchers.EDT) {
                val type = ConfigurationType.CONFIGURATION_TYPE_EP.extensionList.single { it.id == "MarsMipsRun" }
                val configuration = RunManager.getInstance(fixture.project)
                    .createConfiguration("Book later-main regression", type.configurationFactories.single()).configuration
                configureBookCaptureLaunch(configuration, path)
                val persisted = Element("configuration")
                configuration.writeExternal(persisted)
                val options = persisted.getChildren("option").associate {
                    it.getAttributeValue("name") to it.getAttributeValue("value")
                }
                assertEquals("ENABLED", options["startAtMain"])
                assertEquals(path.toString(), options["sourcePath"])
                assertEquals(path.parent.toString(), options["workingDirectory"])
                assertEquals("1", persisted.getAttributeValue("marsExecutionDefaults"))
            }
        } finally {
            Files.delete(path)
        }
    }
}
