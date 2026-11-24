// Exact baseline interpolation and signed residuals; never accept output hashes alone.
import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder
import kotlin.math.abs

data class PredictionInput(
    val px: Long = 0,
    val py: Long = 0,
    val stride: Long = 88,
    val referenceStride: Long = 88,
    val pattern: String = "ramp",
    val bad: String = "",
    val alias: Boolean = false,
)

fun AssemblyCase.put(name: String, bytes: ByteArray, little: Boolean) {
  for (i in bytes.indices step 4) {
    var value = 0
    for (j in 0..3) value =
        value or ((bytes[i + j].toInt() and 255) shl (8 * (if (little) j else 3 - j)))
    if (value != -1515870811) setWord(name, value, i)
  }
}

fun AssemblyCase.put64(name: String, offset: Int, value: Long, little: Boolean) {
  setWord(name, (if (little) value else value ushr 32).toInt(), offset)
  setWord(name, (if (little) value ushr 32 else value).toInt(), offset + 4)
}

fun AssemblyCase.checkPrediction(c: PredictionInput, mode: Int, little: Boolean) {
  val current = ByteArray(28720) { 165.toByte() }
  val reference = ByteArray(32832) { 165.toByte() }
  val good = c.bad.isEmpty()
  if (good) {
    for (y in 0 until 8 + c.py.toInt()) for (x in 0 until 8 + c.px.toInt()) {
      val sample =
          when (c.pattern) {
            "positive" -> 0
            "negative" -> 255
            "equal" -> 73
            "corner" -> if (x % 2 == 1 && y % 2 == 1) 1 else 0
            else -> (31 * x + 17 * y + 3) % 256
          }
      reference[31 + y * c.referenceStride.toInt() + x] = sample.toByte()
    }
    for (y in 0..7) for (x in 0..7) {
      val sample =
          when (c.pattern) {
            "positive" -> 255
            "negative" -> 0
            "equal" -> 73
            "corner" -> 0
            else -> (197 + 11 * x + 23 * y) % 256
          }
      current[17 + y * c.stride.toInt() + x] = sample.toByte()
    }
  }
  put("h263Prediction_current", current, little)
  put("h263Prediction_reference", reference, little)
  val refBase = readDataAddress("h263Prediction_reference") + 31
  val curBase = if (c.alias) refBase else readDataAddress("h263Prediction_current") + 17
  val fields =
      listOf(
          if (c.bad == "current-wrap") -8L else curBase,
          if (c.bad == "reference-wrap") -8L else refBase,
          c.stride,
          c.referenceStride,
          c.px,
          c.py,
          if (c.bad == "mode") 2L else mode.toLong(),
          if (c.bad == "reserved") 1L else 0L,
      )
  for ((index, value) in fields.withIndex()) put64(
      "h263Prediction_request",
      index * 8,
      value,
      little,
  )
  val requestBefore = readBytes("h263Prediction_request", 64)
  val outputBefore = readBytes("h263Prediction_output", 224)
  val saved = (0..7).map { "s" + it } + listOf("fp", "gp")
  for ((i, name) in saved.withIndex()) setRegister(name, 0x1234567800000000L + i)
  setRegister("sp", readDataAddress("h263Prediction_stack", 16400))
  val stack = readRegister("sp")
  val readsA = traceMemoryAccesses(readDataAddress("h263Prediction_current"), 28720)
  val readsB = traceMemoryAccesses(readDataAddress("h263Prediction_reference"), 32832)
  val writes = traceMemoryAccesses(readDataAddress("h263Prediction_output"), 224)
  val work = traceExecution()
  val request =
      when (c.bad) {
        "request-align" -> readDataAddress("h263Prediction_request") + 1
        "request-wrap" -> -8L
        else -> readDataAddress("h263Prediction_request")
      }
  val output =
      when (c.bad) {
        "output-align" -> readDataAddress("h263Prediction_output") + 24
        "output-wrap" -> -16L
        else -> readDataAddress("h263Prediction_output") + 16
      }
  setRegister("a0", request)
  setRegister("a1", output)
  callFunction("h263_predict8", "h263Prediction_return")
  expectRegister("v0", if (good) 0 else 1)
  if (good) {
    var sad = 0L
    val actual = readBytes("h263Prediction_output", 192, 16)
    for (y in 0..7) for (x in 0..7) {
      val samples =
          (0..c.py.toInt()).flatMap { dy ->
            (0..c.px.toInt()).map { dx ->
              reference[31 + (y + dy) * c.referenceStride.toInt() + x + dx].toInt() and 255
            }
          }
      val prediction = (samples.sum() + samples.size / 2) / samples.size
      val input =
          (if (c.alias) reference[31 + y * c.stride.toInt() + x]
              else current[17 + y * c.stride.toInt() + x])
              .toInt() and 255
      val residual = input - prediction
      val at = y * 8 + x
      val half =
          if (little) actual[64 + 2 * at] or (actual[65 + 2 * at] shl 8)
          else (actual[64 + 2 * at] shl 8) or actual[65 + 2 * at]
      val signed = half.toShort().toInt()
      expectCondition(
          actual[at] == prediction,
          "Every predicted byte uses one exact baseline rounding",
      )
      expectCondition(signed == residual, "Every residual is signed current minus prediction")
      expectCondition(
          actual[at] + signed == input,
          "Unquantized prediction plus residual recovers the current sample",
      )
      sad += abs(residual)
    }
    expectRegister("v1", sad)
    println(
        "PREDICTION_WORK mode=$mode px=${c.px} py=${c.py} pattern=${c.pattern} sad=$sad instructions=${work.snapshot().instructions}",
    )
  } else {
    expectRegister("v1", 0)
    expectCondition(
        readBytes("h263Prediction_output", 224).contentEquals(outputBefore),
        "Rejected request leaves all result bytes unchanged",
    )
  }
  fun inside(
      address: Long,
      length: Int,
      start: Long,
      stride: Long,
      width: Int,
      height: Int,
  ): Boolean {
    if (address < start || stride <= 0) return false
    return (0 until length).all { index ->
      val offset = address - start + index
      offset / stride < height && offset % stride < width
    }
  }
  for (event in readsA.snapshot() + readsB.snapshot()) expectCondition(
      good &&
          !event.write &&
          (inside(event.address, event.length, curBase, c.stride, 8, 8) ||
              inside(
                  event.address,
                  event.length,
                  refBase,
                  c.referenceStride,
                  8 + c.px.toInt(),
                  8 + c.py.toInt(),
              )),
      "Read only required current samples and reference halo, without writes or padding reads",
  )
  for (event in writes.snapshot()) expectCondition(
      !event.write ||
          (good && event.address >= output && event.address + event.length <= output + 192),
      "Writes stay in the successful 192-byte record",
  )
  expectCondition(
      readBytes("h263Prediction_current", 28720)
          .contentEquals(current.map { it.toInt() and 255 }.toIntArray()),
      "Current samples and all guards unchanged",
  )
  expectCondition(
      readBytes("h263Prediction_reference", 32832)
          .contentEquals(reference.map { it.toInt() and 255 }.toIntArray()),
      "Reference samples and all guards unchanged",
  )
  expectCondition(
      readBytes("h263Prediction_request", 64).contentEquals(requestBefore),
      "Request unchanged",
  )
  expectCondition(
      readBytes("h263Prediction_output", 16).contentEquals(outputBefore.copyOfRange(0, 16)) &&
          readBytes("h263Prediction_output", 16, 208)
              .contentEquals(outputBefore.copyOfRange(208, 224)),
      "Output outer guards unchanged",
  )
  expectCondition(
      readBytes("h263Prediction_stack", 16).all { it == 165 } &&
          readBytes("h263Prediction_stack", 16, 16400).all { it == 165 },
      "Private stack outer guards retained",
  )
  for ((i, name) in saved.withIndex()) expectRegister(name, 0x1234567800000000L + i)
  expectRegister("sp", stack)
}

case(
    "Prediction positive-0-0 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "positive"), 0, false)
}

case(
    "Prediction positive-0-0 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "positive"), 1, false)
}

case(
    "Prediction positive-0-0 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "positive"), 0, true)
}

case(
    "Prediction positive-0-0 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "positive"), 1, true)
}

case(
    "Prediction negative-0-0 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "negative"), 0, false)
}

case(
    "Prediction negative-0-0 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "negative"), 1, false)
}

case(
    "Prediction negative-0-0 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "negative"), 0, true)
}

case(
    "Prediction negative-0-0 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "negative"), 1, true)
}

case(
    "Prediction ramp-0-0 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "ramp"), 0, false)
}

case(
    "Prediction ramp-0-0 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "ramp"), 1, false)
}

case(
    "Prediction ramp-0-0 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "ramp"), 0, true)
}

case(
    "Prediction ramp-0-0 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "ramp"), 1, true)
}

case(
    "Prediction corner-0-0 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "corner"), 0, false)
}

case(
    "Prediction corner-0-0 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "corner"), 1, false)
}

case(
    "Prediction corner-0-0 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "corner"), 0, true)
}

case(
    "Prediction corner-0-0 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "corner"), 1, true)
}

case(
    "Prediction equal-0-0 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "equal"), 0, false)
}

case(
    "Prediction equal-0-0 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "equal"), 1, false)
}

case(
    "Prediction equal-0-0 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "equal"), 0, true)
}

case(
    "Prediction equal-0-0 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 0, pattern = "equal"), 1, true)
}

case(
    "Prediction positive-0-1 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "positive"), 0, false)
}

case(
    "Prediction positive-0-1 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "positive"), 1, false)
}

case(
    "Prediction positive-0-1 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "positive"), 0, true)
}

case(
    "Prediction positive-0-1 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "positive"), 1, true)
}

case(
    "Prediction negative-0-1 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "negative"), 0, false)
}

case(
    "Prediction negative-0-1 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "negative"), 1, false)
}

case(
    "Prediction negative-0-1 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "negative"), 0, true)
}

case(
    "Prediction negative-0-1 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "negative"), 1, true)
}

case(
    "Prediction ramp-0-1 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "ramp"), 0, false)
}

case(
    "Prediction ramp-0-1 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "ramp"), 1, false)
}

case(
    "Prediction ramp-0-1 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "ramp"), 0, true)
}

case(
    "Prediction ramp-0-1 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "ramp"), 1, true)
}

case(
    "Prediction corner-0-1 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "corner"), 0, false)
}

case(
    "Prediction corner-0-1 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "corner"), 1, false)
}

case(
    "Prediction corner-0-1 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "corner"), 0, true)
}

case(
    "Prediction corner-0-1 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "corner"), 1, true)
}

case(
    "Prediction equal-0-1 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "equal"), 0, false)
}

case(
    "Prediction equal-0-1 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "equal"), 1, false)
}

case(
    "Prediction equal-0-1 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "equal"), 0, true)
}

case(
    "Prediction equal-0-1 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 0, py = 1, pattern = "equal"), 1, true)
}

case(
    "Prediction positive-1-0 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "positive"), 0, false)
}

case(
    "Prediction positive-1-0 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "positive"), 1, false)
}

case(
    "Prediction positive-1-0 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "positive"), 0, true)
}

case(
    "Prediction positive-1-0 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "positive"), 1, true)
}

case(
    "Prediction negative-1-0 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "negative"), 0, false)
}

case(
    "Prediction negative-1-0 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "negative"), 1, false)
}

case(
    "Prediction negative-1-0 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "negative"), 0, true)
}

case(
    "Prediction negative-1-0 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "negative"), 1, true)
}

case(
    "Prediction ramp-1-0 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "ramp"), 0, false)
}

case(
    "Prediction ramp-1-0 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "ramp"), 1, false)
}

case(
    "Prediction ramp-1-0 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "ramp"), 0, true)
}

case(
    "Prediction ramp-1-0 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "ramp"), 1, true)
}

case(
    "Prediction corner-1-0 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "corner"), 0, false)
}

case(
    "Prediction corner-1-0 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "corner"), 1, false)
}

case(
    "Prediction corner-1-0 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "corner"), 0, true)
}

case(
    "Prediction corner-1-0 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "corner"), 1, true)
}

case(
    "Prediction equal-1-0 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "equal"), 0, false)
}

case(
    "Prediction equal-1-0 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "equal"), 1, false)
}

case(
    "Prediction equal-1-0 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "equal"), 0, true)
}

case(
    "Prediction equal-1-0 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 0, pattern = "equal"), 1, true)
}

case(
    "Prediction positive-1-1 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "positive"), 0, false)
}

case(
    "Prediction positive-1-1 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "positive"), 1, false)
}

case(
    "Prediction positive-1-1 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "positive"), 0, true)
}

case(
    "Prediction positive-1-1 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "positive"), 1, true)
}

case(
    "Prediction negative-1-1 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "negative"), 0, false)
}

case(
    "Prediction negative-1-1 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "negative"), 1, false)
}

case(
    "Prediction negative-1-1 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "negative"), 0, true)
}

case(
    "Prediction negative-1-1 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "negative"), 1, true)
}

case(
    "Prediction ramp-1-1 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "ramp"), 0, false)
}

case(
    "Prediction ramp-1-1 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "ramp"), 1, false)
}

case(
    "Prediction ramp-1-1 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "ramp"), 0, true)
}

case(
    "Prediction ramp-1-1 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "ramp"), 1, true)
}

case(
    "Prediction corner-1-1 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "corner"), 0, false)
}

case(
    "Prediction corner-1-1 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "corner"), 1, false)
}

case(
    "Prediction corner-1-1 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "corner"), 0, true)
}

case(
    "Prediction corner-1-1 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "corner"), 1, true)
}

case(
    "Prediction equal-1-1 mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "equal"), 0, false)
}

case(
    "Prediction equal-1-1 mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "equal"), 1, false)
}

case(
    "Prediction equal-1-1 mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "equal"), 0, true)
}

case(
    "Prediction equal-1-1 mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, py = 1, pattern = "equal"), 1, true)
}

case(
    "Prediction short-current mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(stride = 7, bad = "stride"), 0, false)
}

case(
    "Prediction short-current mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(stride = 7, bad = "stride"), 1, false)
}

case(
    "Prediction short-current mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(stride = 7, bad = "stride"), 0, true)
}

case(
    "Prediction short-current mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(stride = 7, bad = "stride"), 1, true)
}

case(
    "Prediction short-halo mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, referenceStride = 8, bad = "stride"), 0, false)
}

case(
    "Prediction short-halo mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, referenceStride = 8, bad = "stride"), 1, false)
}

case(
    "Prediction short-halo mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, referenceStride = 8, bad = "stride"), 0, true)
}

case(
    "Prediction short-halo mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 1, referenceStride = 8, bad = "stride"), 1, true)
}

case(
    "Prediction bad-phase mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 2, bad = "phase"), 0, false)
}

case(
    "Prediction bad-phase mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 2, bad = "phase"), 1, false)
}

case(
    "Prediction bad-phase mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 2, bad = "phase"), 0, true)
}

case(
    "Prediction bad-phase mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(px = 2, bad = "phase"), 1, true)
}

case(
    "Prediction bad-mode mode=0 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(bad = "mode"), 0, false)
}

case(
    "Prediction bad-mode mode=1 little=False",
    instructionLimit = 50000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkPrediction(PredictionInput(bad = "mode"), 1, false)
}

case(
    "Prediction bad-mode mode=0 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(bad = "mode"), 0, true)
}

case(
    "Prediction bad-mode mode=1 little=True",
    instructionLimit = 50000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkPrediction(PredictionInput(bad = "mode"), 1, true)
}
