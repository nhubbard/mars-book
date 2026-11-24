// Finite sample accuracy and interfaces; the separate Annex A gate is also required.
import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sqrt

data class TransformInput(
    val inverse: Boolean = false,
    val pattern: String = "ramp",
    val stride: Long = 16,
    val bad: String = "",
)

val transformBasis =
    Array(8) { u ->
      DoubleArray(8) { x -> if (u == 0) 1 / sqrt(8.0) else .5 * cos((2 * x + 1) * u * PI / 16) }
    }

fun transformReference(input: IntArray, inverse: Boolean): IntArray {
  val matrix =
      Array(8) { u ->
        DoubleArray(8) { x -> if (inverse) transformBasis[x][u] else transformBasis[u][x] }
      }
  val first =
      Array(8) { y ->
        DoubleArray(8) { u -> (0..7).sumOf { x -> input[y * 8 + x] * matrix[u][x] } }
      }
  return IntArray(64) { index ->
    val v = index / 8
    val u = index % 8
    floor((0..7).sumOf { y -> first[y][u] * matrix[v][y] } + .5)
        .toInt()
        .coerceIn(if (inverse) -256 else -2048, if (inverse) 255 else 2047)
  }
}

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

fun AssemblyCase.checkTransform(c: TransformInput, mode: Int, little: Boolean) {
  val metadataGood = c.bad.isEmpty() || c.bad == "domain"
  val success = c.bad.isEmpty()
  val input =
      IntArray(64) { index ->
        val x = index % 8
        val y = index / 8
        when (c.pattern) {
          "zero" -> 0
          "constant" ->
              if (c.inverse) {
                if (index == 0) 1024 else 0
              } else 128
          "minimum" -> if (c.inverse) -2048 else -256
          "maximum" -> if (c.inverse) 2047 else 255
          "impulse" ->
              if (index == 11) {
                if (c.inverse) 2047 else 255
              } else 0
          "checker" ->
              if ((x + y) % 2 == 0) {
                if (c.inverse) -2048 else -256
              } else {
                if (c.inverse) 2047 else 255
              }
          else ->
              if (c.inverse) (257 * x + 601 * y + 17) % 4096 - 2048
              else (31 * x + 47 * y + 13) % 512 - 256
        }
      }
  if (c.bad == "domain") input[63] = if (c.inverse) 2048 else 256
  val bytes = ByteArray(28736) { 165.toByte() }
  if (metadataGood)
      for (index in 0..63) {
        val at = 18 + (index / 8) * c.stride.toInt() + 2 * (index % 8)
        val value = input[index]
        bytes[at] = (if (little) value else value ushr 8).toByte()
        bytes[at + 1] = (if (little) value ushr 8 else value).toByte()
      }
  put("h263Transform_input", bytes, little)
  val base = readDataAddress("h263Transform_input") + 18
  val inputPointer =
      when (c.bad) {
        "input-align" -> base + 1
        "input-wrap" -> -2L
        else -> base
      }
  val direction =
      if (c.bad == "direction") 2L
      else if (c.bad == "wide-direction") 4294967296L else if (c.inverse) 1L else 0L
  val fields =
      listOf(
          inputPointer,
          c.stride,
          direction,
          if (c.bad == "mode") 2L else mode.toLong(),
          if (c.bad == "reserved") 1L else 0L,
          if (c.bad == "second-reserved") 1L else 0L,
      )
  for ((index, value) in fields.withIndex()) put64(
      "h263Transform_request",
      index * 8,
      value,
      little,
  )
  val requestBefore = readBytes("h263Transform_request", 48)
  val outputBefore = readBytes("h263Transform_output", 160)
  val saved = (0..7).map { "s" + it } + listOf("fp", "gp")
  for ((i, name) in saved.withIndex()) setRegister(name, 0x1234567800000000L + i)
  setRegister("sp", readDataAddress("h263Transform_stack", 16400))
  val stack = readRegister("sp")
  val reads = traceMemoryAccesses(readDataAddress("h263Transform_input"), 28736)
  val writes = traceMemoryAccesses(readDataAddress("h263Transform_output"), 160)
  val work = traceExecution()
  val request =
      when (c.bad) {
        "request-align" -> readDataAddress("h263Transform_request") + 1
        "request-wrap" -> -8L
        else -> readDataAddress("h263Transform_request")
      }
  val output =
      when (c.bad) {
        "output-align" -> readDataAddress("h263Transform_output") + 24
        "output-wrap" -> -16L
        else -> readDataAddress("h263Transform_output") + 16
      }
  setRegister("a0", request)
  setRegister("a1", output)
  callFunction("h263_transform8", "h263Transform_return")
  expectRegister("v0", if (success) 0 else if (metadataGood) 2 else 1)
  if (success) {
    expectRegister("v1", 64)
    val expected = transformReference(input, c.inverse)
    val actual = readBytes("h263Transform_output", 128, 16)
    for (index in 0..63) {
      val bits =
          if (little) actual[2 * index] or (actual[2 * index + 1] shl 8)
          else (actual[2 * index] shl 8) or actual[2 * index + 1]
      val value = bits.toShort().toInt()
      expectCondition(
          abs(value - expected[index]) <= 1,
          "Every transform output is within one of the independent floating reference",
      )
      expectCondition(
          value in (if (c.inverse) -256..255 else -2048..2047),
          "Direction-specific clipped output range",
      )
      if (c.pattern == "zero") expectCondition(value == 0, "Exact all-zero transform")
    }
    println(
        "TRANSFORM_WORK inverse=${c.inverse} mode=$mode pattern=${c.pattern} stride=${c.stride} instructions=${work.snapshot().instructions}",
    )
  } else {
    expectRegister("v1", 0)
    expectCondition(
        readBytes("h263Transform_output", 160).contentEquals(outputBefore),
        "Rejected metadata/sample domain retains every result byte",
    )
  }
  for (event in reads.snapshot()) expectCondition(
      metadataGood &&
          !event.write &&
          (0 until event.length).all { i ->
            val at = event.address - base + i
            at >= 0 && at / c.stride < 8 && at % c.stride < 16
          },
      "Only sixteen active input bytes in eight rows; no writes",
  )
  for (event in writes.snapshot()) expectCondition(
      !event.write ||
          (success && event.address >= output && event.address + event.length <= output + 128),
      "Only successful output payload is written",
  )
  expectCondition(
      readBytes("h263Transform_input", 28736)
          .contentEquals(bytes.map { it.toInt() and 255 }.toIntArray()),
      "Input and all guards unchanged",
  )
  expectCondition(
      readBytes("h263Transform_request", 48).contentEquals(requestBefore),
      "Request unchanged",
  )
  expectCondition(
      readBytes("h263Transform_output", 16).contentEquals(outputBefore.copyOfRange(0, 16)) &&
          readBytes("h263Transform_output", 16, 144)
              .contentEquals(outputBefore.copyOfRange(144, 160)),
      "Output outer guards unchanged",
  )
  expectCondition(
      readBytes("h263Transform_stack", 16).all { it == 165 } &&
          readBytes("h263Transform_stack", 16, 16400).all { it == 165 },
      "Private stack outer guards unchanged",
  )
  for ((i, name) in saved.withIndex()) expectRegister(name, 0x1234567800000000L + i)
  expectRegister("sp", stack)
}

case(
    "Transform False-zero mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "zero"), 0, false)
}

case(
    "Transform False-zero mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "zero"), 1, false)
}

case(
    "Transform False-zero mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "zero"), 0, true)
}

case(
    "Transform False-zero mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "zero"), 1, true)
}

case(
    "Transform False-constant mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "constant"), 0, false)
}

case(
    "Transform False-constant mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "constant"), 1, false)
}

case(
    "Transform False-constant mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "constant"), 0, true)
}

case(
    "Transform False-constant mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "constant"), 1, true)
}

case(
    "Transform False-minimum mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "minimum"), 0, false)
}

case(
    "Transform False-minimum mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "minimum"), 1, false)
}

case(
    "Transform False-minimum mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "minimum"), 0, true)
}

case(
    "Transform False-minimum mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "minimum"), 1, true)
}

case(
    "Transform False-maximum mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "maximum"), 0, false)
}

case(
    "Transform False-maximum mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "maximum"), 1, false)
}

case(
    "Transform False-maximum mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "maximum"), 0, true)
}

case(
    "Transform False-maximum mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "maximum"), 1, true)
}

case(
    "Transform False-impulse mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "impulse"), 0, false)
}

case(
    "Transform False-impulse mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "impulse"), 1, false)
}

case(
    "Transform False-impulse mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "impulse"), 0, true)
}

case(
    "Transform False-impulse mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "impulse"), 1, true)
}

case(
    "Transform False-ramp mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "ramp"), 0, false)
}

case(
    "Transform False-ramp mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "ramp"), 1, false)
}

case(
    "Transform False-ramp mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "ramp"), 0, true)
}

case(
    "Transform False-ramp mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "ramp"), 1, true)
}

case(
    "Transform False-checker mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "checker"), 0, false)
}

case(
    "Transform False-checker mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "checker"), 1, false)
}

case(
    "Transform False-checker mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "checker"), 0, true)
}

case(
    "Transform False-checker mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = false, pattern = "checker"), 1, true)
}

case(
    "Transform True-zero mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "zero"), 0, false)
}

case(
    "Transform True-zero mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "zero"), 1, false)
}

case(
    "Transform True-zero mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "zero"), 0, true)
}

case(
    "Transform True-zero mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "zero"), 1, true)
}

case(
    "Transform True-constant mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "constant"), 0, false)
}

case(
    "Transform True-constant mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "constant"), 1, false)
}

case(
    "Transform True-constant mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "constant"), 0, true)
}

case(
    "Transform True-constant mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "constant"), 1, true)
}

case(
    "Transform True-minimum mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "minimum"), 0, false)
}

case(
    "Transform True-minimum mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "minimum"), 1, false)
}

case(
    "Transform True-minimum mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "minimum"), 0, true)
}

case(
    "Transform True-minimum mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "minimum"), 1, true)
}

case(
    "Transform True-maximum mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "maximum"), 0, false)
}

case(
    "Transform True-maximum mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "maximum"), 1, false)
}

case(
    "Transform True-maximum mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "maximum"), 0, true)
}

case(
    "Transform True-maximum mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "maximum"), 1, true)
}

case(
    "Transform True-impulse mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "impulse"), 0, false)
}

case(
    "Transform True-impulse mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "impulse"), 1, false)
}

case(
    "Transform True-impulse mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "impulse"), 0, true)
}

case(
    "Transform True-impulse mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "impulse"), 1, true)
}

case(
    "Transform True-ramp mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "ramp"), 0, false)
}

case(
    "Transform True-ramp mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "ramp"), 1, false)
}

case(
    "Transform True-ramp mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "ramp"), 0, true)
}

case(
    "Transform True-ramp mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "ramp"), 1, true)
}

case(
    "Transform True-checker mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "checker"), 0, false)
}

case(
    "Transform True-checker mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "checker"), 1, false)
}

case(
    "Transform True-checker mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "checker"), 0, true)
}

case(
    "Transform True-checker mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, pattern = "checker"), 1, true)
}

case(
    "Transform forward-padding mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(stride = 18), 0, false)
}

case(
    "Transform forward-padding mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(stride = 18), 1, false)
}

case(
    "Transform forward-padding mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(stride = 18), 0, true)
}

case(
    "Transform forward-padding mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(stride = 18), 1, true)
}

case(
    "Transform inverse-wide mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, stride = 4096), 0, false)
}

case(
    "Transform inverse-wide mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, stride = 4096), 1, false)
}

case(
    "Transform inverse-wide mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, stride = 4096), 0, true)
}

case(
    "Transform inverse-wide mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, stride = 4096), 1, true)
}

case(
    "Transform input-align mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(bad = "input-align"), 0, false)
}

case(
    "Transform input-align mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(bad = "input-align"), 1, false)
}

case(
    "Transform input-align mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(bad = "input-align"), 0, true)
}

case(
    "Transform input-align mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(bad = "input-align"), 1, true)
}

case(
    "Transform short-stride mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(stride = 15, bad = "stride"), 0, false)
}

case(
    "Transform short-stride mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(stride = 15, bad = "stride"), 1, false)
}

case(
    "Transform short-stride mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(stride = 15, bad = "stride"), 0, true)
}

case(
    "Transform short-stride mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(stride = 15, bad = "stride"), 1, true)
}

case(
    "Transform odd-stride mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(stride = 17, bad = "stride"), 0, false)
}

case(
    "Transform odd-stride mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(stride = 17, bad = "stride"), 1, false)
}

case(
    "Transform odd-stride mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(stride = 17, bad = "stride"), 0, true)
}

case(
    "Transform odd-stride mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(stride = 17, bad = "stride"), 1, true)
}

case(
    "Transform direction mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(bad = "direction"), 0, false)
}

case(
    "Transform direction mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(bad = "direction"), 1, false)
}

case(
    "Transform direction mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(bad = "direction"), 0, true)
}

case(
    "Transform direction mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(bad = "direction"), 1, true)
}

case(
    "Transform mode mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(bad = "mode"), 0, false)
}

case(
    "Transform mode mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(bad = "mode"), 1, false)
}

case(
    "Transform mode mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(bad = "mode"), 0, true)
}

case(
    "Transform mode mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(bad = "mode"), 1, true)
}

case(
    "Transform reserved mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(bad = "reserved"), 0, false)
}

case(
    "Transform reserved mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(bad = "reserved"), 1, false)
}

case(
    "Transform reserved mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(bad = "reserved"), 0, true)
}

case(
    "Transform reserved mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(bad = "reserved"), 1, true)
}

case(
    "Transform forward-domain mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(bad = "domain"), 0, false)
}

case(
    "Transform forward-domain mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(bad = "domain"), 1, false)
}

case(
    "Transform forward-domain mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(bad = "domain"), 0, true)
}

case(
    "Transform forward-domain mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(bad = "domain"), 1, true)
}

case(
    "Transform inverse-domain mode=0 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, bad = "domain"), 0, false)
}

case(
    "Transform inverse-domain mode=1 little=False",
    instructionLimit = 100000,
    byteOrder = ByteOrder.BIG_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, bad = "domain"), 1, false)
}

case(
    "Transform inverse-domain mode=0 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, bad = "domain"), 0, true)
}

case(
    "Transform inverse-domain mode=1 little=True",
    instructionLimit = 100000,
    byteOrder = ByteOrder.LITTLE_ENDIAN,
) {
  checkTransform(TransformInput(inverse = true, bad = "domain"), 1, true)
}
