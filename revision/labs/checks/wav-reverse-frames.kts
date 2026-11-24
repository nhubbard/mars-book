import edu.missouristate.mars.cli.grading.AssemblyCase

// Independent extension contract: validate the original 16-byte WAV description
// prefix, reverse complete PCM frames, return status in v0 and frame count in v1.
// PCM/output may be disjoint or exactly identical. Other overlap is out of scope.
val reverseObjects =
    listOf(
        "input_path" to 128,
        "output_path" to 128,
        "source" to 266272,
        "metadata" to 64,
        "samples" to 262176,
        "destination" to 266272,
        "coeff" to 64,
        "kernel_window" to 160,
        "kernel_output" to 160,
        "status" to 4,
        "count" to 8,
        "clips" to 8,
        "mode" to 4,
        "byte_count" to 8,
        "byte_capacity" to 8,
        "kernel_count" to 8,
    )

fun insideReverse(address: Long, length: Int, base: Long, size: Int) =
    address >= base && length <= size && address - base <= size.toLong() - length

fun AssemblyCase.reverseCheck(
    rate: Long,
    channels: Long,
    frames: Long,
    inPlace: Boolean,
    seed: Int = 0,
) {
  val valid = rate in 8000L..48000L && channels in 1L..2L && frames in 0L..65536L
  val count = if (valid) (2 * channels * frames).toInt() else 0
  val frameBytes = if (valid) (2 * channels).toInt() else 2
  val input = IntArray(count) { (it * 73 + (it / 2) * 11 + seed * 19) and 255 }
  val sourceLabel = if (seed % 2 == 0) "source" else "samples"
  val destinationLabel = if (inPlace) sourceLabel else "destination"
  val inputOffset = 1 + seed % 5
  val outputOffset = if (inPlace) inputOffset else 7 - seed % 5
  val prefixOffset = 8 * (seed % 5)
  for (i in 0 until (inputOffset + count + 3) / 4) {
    var word = 0
    for (j in 0..3) {
      val index = 4 * i + j - inputOffset
      word = word or ((if (index in input.indices) input[index] else 165) shl (8 * j))
    }
    setWord(sourceLabel, word, 4 * i)
  }
  setWord("metadata", rate.toInt(), prefixOffset)
  setWord("metadata", channels.toInt(), prefixOffset + 4)
  setWord("metadata", frames.toInt(), prefixOffset + 8)
  setWord("metadata", (frames ushr 32).toInt(), prefixOffset + 12)
  val saved = List(9) { 0x3141592600000000L + 977 * it }
  for (i in 0..7) setRegister("s$i", saved[i])
  setRegister("fp", saved[8])
  val stack = readDataAddress("workspace", 8192)
  setRegister("sp", stack)
  setRegisterToDataAddress("a1", "metadata", prefixOffset.toLong())
  val source = readDataAddress(sourceLabel, inputOffset.toLong())
  val destination = readDataAddress(destinationLabel, outputOffset.toLong())
  if (valid && frames > 0) {
    setRegister("a0", source)
    setRegister("a2", destination)
  } else {
    setRegister("a0", 1)
    setRegister("a2", 1)
  }
  val objects = reverseObjects.map { (label, size) -> Triple(label, readDataAddress(label), size) }
  val before = reverseObjects.map { (label, size) -> readBytes(label, size) }
  val low = objects.minOf { it.second }
  val high = objects.maxOf { it.second + it.third }
  val trace = traceMemoryAccesses(low, (high - low).toInt(), maxAccesses = 1000000)
  callFunction("wav_reverse_frames", "function_return")
  trace.close()
  expectRegister("v0", if (valid) 0 else 1)
  expectRegister("v1", if (valid) frames else 0)
  for (i in 0..7) expectRegister("s$i", saved[i])
  expectRegister("fp", saved[8])
  expectRegister("sp", stack)
  expectOutput("")
  val prefix = readDataAddress("metadata", prefixOffset.toLong())
  for (event in trace.snapshot()) if (
      objects.any { (_, base, size) ->
        event.address < base + size && base < event.address + event.length
      }
  ) {
    val allowed =
        if (event.write) valid && insideReverse(event.address, event.length, destination, count)
        else
            insideReverse(event.address, event.length, prefix, 16) ||
                valid &&
                    (insideReverse(event.address, event.length, source, count) ||
                        insideReverse(event.address, event.length, destination, count))
    expectCondition(
        allowed,
        "Only the validated prefix and selected PCM/output intervals may be accessed",
    )
  }
  val expected =
      IntArray(count) { i ->
        input[((frames.toInt() - 1 - i / frameBytes) * frameBytes) + i % frameBytes]
      }
  for ((i, obj) in objects.withIndex()) {
    val (label, _, size) = obj
    val wanted = before[i].copyOf()
    if (valid && label == destinationLabel)
        for (j in expected.indices) wanted[outputOffset + j] = expected[j]
    expectCondition(
        readBytes(label, size).contentEquals(wanted),
        "Complete caller object $label obeys frame order and ownership",
    )
  }
}

fun reverseHex(text: String) = text.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun AssemblyCase.reversePipeline() {
  val input =
      reverseHex(
          "524946464a000000574156454a554e4b03000000010203a5666d74201000000001000200803e000000fa0000040010006461746110000000e02e20d10000803ec0e00000ff7f00806e6f746501000000427f",
      )
  val expected =
      reverseHex(
          "524946463400000057415645666d74201000000001000200803e000000fa0000040010006461746110000000f411f4f1f401f401dc05f401dc0524fa",
      )
  val files =
      provideFiles(
          mapOf("input.wav" to input, "output.wav" to "old".toByteArray()),
          writablePaths = setOf("output.wav"),
          maxReadSize = 7,
          maxWriteSize = 5,
          firstDescriptor = 19,
      )
  setWord("mode", 6)
  startAt("main")
  runToExit()
  expectWord("status", 0)
  expectWord("count", 60)
  expectWord("count", 0, 4)
  expectWord("clips", 0)
  expectWord("clips", 0, 4)
  files.expectFile("output.wav", expected)
  files.expectAllClosed()
  expectOutput("")
}

// Public cases. Private labels and a particular loop organization are not required.
for (channels in 1L..2L) for (frames in listOf(0L, 1L, 2L, 3L, 7L, 65L)) for (alias in
    listOf(false, true)) case(
    "reverse $channels channels $frames frames alias $alias",
    instructionLimit = 10000000,
) {
  reverseCheck(16000, channels, frames, alias, (frames.toInt() + channels.toInt()) % 19)
}

for ((rate, channels, frames) in
    listOf(
        Triple(7999L, 1L, 1L),
        Triple(48001L, 2L, 1L),
        Triple(16000L, 0L, 0L),
        Triple(16000L, 3L, 1L),
        Triple(16000L, 1L, 65537L),
        Triple(16000L, 1L, 4294967296L),
        Triple(4294967295L, 1L, 1L),
        Triple(16000L, 1L, Long.MIN_VALUE),
    )) case("invalid reverse description $rate $channels $frames") {
  reverseCheck(rate, channels, frames, false, 5)
}

case("maximum stereo in-place reversal", instructionLimit = 10000000) {
  reverseCheck(48000, 2, 65536, true, 9)
}

case("independent repeated reversals", instructionLimit = 10000000) {
  reverseCheck(16000, 2, 65, true, 2)
  reverseCheck(8000, 1, 0, false, 3)
  reverseCheck(48000, 1, 3, false, 4)
  reverseCheck(16000, 1, Long.MIN_VALUE, true, 5)
  reverseCheck(16000, 2, 64, false, 6)
  reverseCheck(16000, 2, 65, true, 7)
}

case("reverse the complete filtered WAV pipeline", instructionLimit = 10000000) {
  reversePipeline()
}
