import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel.COMPATIBILITY_PROBLEMS
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel.INTERNAL_API_USAGES
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel.OVERRIDE_ONLY_API_USAGES

plugins {
    kotlin("jvm") version "2.4.21"
    id("org.jetbrains.intellij.platform") version "2.20.0"
}
version = "0.1.0"
repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}
dependencies {
    intellijPlatform {
        intellijIdea("2026.3")
        bundledModule("intellij.platform.monolith")
        bundledModule("intellij.platform.debugger")
        bundledPlugin("training")
        bundledPlugin("com.intellij.modules.compose")
        localPlugin(providers.gradleProperty("marsPluginArchive"))
        testBundledPlugin("com.intellij.java")
        testBundledPlugin("org.jetbrains.kotlin")
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)
        testFramework(TestFrameworkType.JUnit5CodeInsight)
    }
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
kotlin {
    jvmToolchain(25)
    compilerOptions { allWarningsAsErrors.set(true) }
}
intellijPlatform {
    instrumentCode.set(false)
    buildSearchableOptions.set(false)
    pluginVerification {
        ides { current() }
        failureLevel.set(listOf(COMPATIBILITY_PROBLEMS, INTERNAL_API_USAGES, OVERRIDE_ONLY_API_USAGES))
    }
    pluginConfiguration {
        id.set("edu.missouristate.mars.book.authoring")
        name.set("MARS Book Authoring")
        ideaVersion { sinceBuild.set("263"); untilBuild.set("263.*") }
    }
}
tasks.test {
    useJUnitPlatform { includeEngines("junit-jupiter") }
    systemProperty("jetbrains.ide.builtin.descovery.enabled", false)
    for (name in listOf("highlightInput", "highlightOutput", "marsPluginArchive")) {
        providers.gradleProperty(name).orNull?.let { systemProperty(name, it) }
    }
}

tasks.test {
    providers.gradleProperty("highlightInput").orNull?.let { inputs.dir(it) }
    providers.gradleProperty("highlightOutput").orNull?.let { outputs.file(it) }
}

// Only the separate authoring IDE receives these opt-in local fixture parameters.
tasks.runIde {
    systemProperty("mars.book.mars.archive", providers.gradleProperty("marsPluginArchive").get())
    providers.gradleProperty("bookCapturePlan").orNull?.let { systemProperty("mars.book.capture.plan", it) }
    providers.gradleProperty("bookLessonCapturePlan").orNull?.let { systemProperty("mars.book.lesson.capture.plan", it) }
    providers.gradleProperty("bookPackageImportPlan").orNull?.let { systemProperty("mars.book.package.import.plan", it) }
    providers.gradleProperty("bookCaptureProject").orNull?.let { args(it) }
}

// Local authoring test setup, never packaged into either plugin. Trust only the explicitly
// supplied generated fixture in this disposable IDE configuration; retain project-trust checks.
tasks.runIde {
    if (providers.gradleProperty("bookCaptureTrustFixture").orNull == "true") {
        doFirst {
            val fixture = file(providers.gradleProperty("bookCaptureProject").get()).canonicalFile
            require(fixture.isDirectory) { "Missing generated capture fixture" }
            val config = sandboxConfigDirectory.get().asFile.canonicalFile
            require(config.toPath().startsWith(layout.projectDirectory.dir(".intellijPlatform").asFile.canonicalFile.toPath())) {
                "Fixture trust setup requires the dedicated authoring IDE configuration"
            }
            val path = fixture.path.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;")
            val options = config.resolve("options").also { it.mkdirs() }
            options.resolve("trusted-paths.xml").writeText(
                "<application><component name=\"Trusted.Paths.Settings\"><option name=\"TRUSTED_PATHS\"><list><option value=\"$path\"/></list></option></component></application>\n"
            )
        }
    }
}
