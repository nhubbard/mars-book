plugins { java }

repositories { mavenCentral() }

dependencies { runtimeOnly("org.jetbrains.kotlinx:ktfmt:0.65") }

tasks.register("writeClasspath") {
  val destination = layout.buildDirectory.file("ktfmt-classpath.txt")
  val formatterRuntime = configurations.runtimeClasspath
  outputs.file(destination)
  inputs.files(formatterRuntime)
  doLast { destination.get().asFile.writeText(formatterRuntime.get().asPath) }
}
