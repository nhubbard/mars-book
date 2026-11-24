fun triangle(position: Int, amplitude: Int): Int {
  val u = position % 64
  val level =
      when {
        u < 16 -> u
        u < 48 -> 32 - u
        else -> u - 64
      }
  return amplitude * level / 16
}

val scenes =
    listOf(Triple("quiet", 6000, 0), Triple("clipping", 24000, 0), Triple("phase-offset", 24000, 8))

for ((scene, amplitude, phase) in scenes) case("listening $scene", instructionLimit = 5000000) {
  var totalClipped = 0
  for (start in 0 until 32000 step 64) {
    val a = IntArray(64) { triangle(start + it, amplitude) }
    val b = IntArray(64) { triangle(start + it + phase, amplitude) }
    val t = MixTrial(a, b)
    val saturated = checkMix(t, start / 64 * 2)
    val wrapped = checkMix(t, start / 64 * 2 + 1, wrap = true)
    totalClipped += clipped(t)
    for ((name, values) in
        listOf("a" to a, "b" to b, "saturated" to saturated, "wrapped" to wrapped)) println(
        "PCM,$scene,$name,$start," +
            bytes(values).joinToString("") { it.toString(16).padStart(2, '0') },
    )
  }
  println("PCM-END,$scene,32000,16000,$totalClipped")
  runToExit()
  expectOutput("")
}
