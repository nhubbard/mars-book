import org.jetbrains.intellij.platform.gradle.TestFrameworkType

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
