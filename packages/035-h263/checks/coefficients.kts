// Decode emitted syntax; equivalent compact and escape choices are accepted.
import edu.missouristate.mars.cli.grading.AssemblyCase
import java.nio.ByteOrder

data class CoeffCode(val last: Int, val run: Int, val mag: Int, val code: Int, val width: Int)

val coeffScan =
    intArrayOf(
        0,
        1,
        8,
        16,
        9,
        2,
        3,
        10,
        17,
        24,
        32,
        25,
        18,
        11,
        4,
        5,
        12,
        19,
        26,
        33,
        40,
        48,
        41,
        34,
        27,
        20,
        13,
        6,
        7,
        14,
        21,
        28,
        35,
        42,
        49,
        56,
        57,
        50,
        43,
        36,
        29,
        22,
        15,
        23,
        30,
        37,
        44,
        51,
        58,
        59,
        52,
        45,
        38,
        31,
        39,
        46,
        53,
        60,
        61,
        54,
        47,
        55,
        62,
        63,
    )
val coeffCodes =
    listOf(
        CoeffCode(0, 0, 1, 4, 3),
        CoeffCode(0, 0, 2, 30, 5),
        CoeffCode(0, 0, 3, 42, 7),
        CoeffCode(0, 0, 4, 46, 8),
        CoeffCode(0, 0, 5, 62, 9),
        CoeffCode(0, 0, 6, 74, 10),
        CoeffCode(0, 0, 7, 72, 10),
        CoeffCode(0, 0, 8, 66, 11),
        CoeffCode(0, 0, 9, 64, 11),
        CoeffCode(0, 0, 10, 14, 12),
        CoeffCode(0, 0, 11, 12, 12),
        CoeffCode(0, 0, 12, 64, 12),
        CoeffCode(0, 1, 1, 12, 4),
        CoeffCode(0, 1, 2, 40, 7),
        CoeffCode(0, 1, 3, 60, 9),
        CoeffCode(0, 1, 4, 30, 11),
        CoeffCode(0, 1, 5, 66, 12),
        CoeffCode(0, 1, 6, 160, 13),
        CoeffCode(0, 2, 1, 28, 5),
        CoeffCode(0, 2, 2, 58, 9),
        CoeffCode(0, 2, 3, 28, 11),
        CoeffCode(0, 2, 4, 162, 13),
        CoeffCode(0, 3, 1, 26, 6),
        CoeffCode(0, 3, 2, 70, 10),
        CoeffCode(0, 3, 3, 26, 11),
        CoeffCode(0, 4, 1, 24, 6),
        CoeffCode(0, 4, 2, 68, 10),
        CoeffCode(0, 4, 3, 164, 13),
        CoeffCode(0, 5, 1, 22, 6),
        CoeffCode(0, 5, 2, 24, 11),
        CoeffCode(0, 5, 3, 166, 13),
        CoeffCode(0, 6, 1, 38, 7),
        CoeffCode(0, 6, 2, 22, 11),
        CoeffCode(0, 6, 3, 168, 13),
        CoeffCode(0, 7, 1, 36, 7),
        CoeffCode(0, 7, 2, 20, 11),
        CoeffCode(0, 8, 1, 34, 7),
        CoeffCode(0, 8, 2, 18, 11),
        CoeffCode(0, 9, 1, 32, 7),
        CoeffCode(0, 9, 2, 16, 11),
        CoeffCode(0, 10, 1, 44, 8),
        CoeffCode(0, 10, 2, 170, 13),
        CoeffCode(0, 11, 1, 42, 8),
        CoeffCode(0, 12, 1, 40, 8),
        CoeffCode(0, 13, 1, 56, 9),
        CoeffCode(0, 14, 1, 54, 9),
        CoeffCode(0, 15, 1, 66, 10),
        CoeffCode(0, 16, 1, 64, 10),
        CoeffCode(0, 17, 1, 62, 10),
        CoeffCode(0, 18, 1, 60, 10),
        CoeffCode(0, 19, 1, 58, 10),
        CoeffCode(0, 20, 1, 56, 10),
        CoeffCode(0, 21, 1, 54, 10),
        CoeffCode(0, 22, 1, 52, 10),
        CoeffCode(0, 23, 1, 68, 12),
        CoeffCode(0, 24, 1, 70, 12),
        CoeffCode(0, 25, 1, 172, 13),
        CoeffCode(0, 26, 1, 174, 13),
        CoeffCode(1, 0, 1, 14, 5),
        CoeffCode(1, 0, 2, 50, 10),
        CoeffCode(1, 0, 3, 10, 12),
        CoeffCode(1, 1, 1, 30, 7),
        CoeffCode(1, 1, 2, 8, 12),
        CoeffCode(1, 2, 1, 28, 7),
        CoeffCode(1, 3, 1, 26, 7),
        CoeffCode(1, 4, 1, 24, 7),
        CoeffCode(1, 5, 1, 38, 8),
        CoeffCode(1, 6, 1, 36, 8),
        CoeffCode(1, 7, 1, 34, 8),
        CoeffCode(1, 8, 1, 32, 8),
        CoeffCode(1, 9, 1, 52, 9),
        CoeffCode(1, 10, 1, 50, 9),
        CoeffCode(1, 11, 1, 48, 9),
        CoeffCode(1, 12, 1, 46, 9),
        CoeffCode(1, 13, 1, 44, 9),
        CoeffCode(1, 14, 1, 42, 9),
        CoeffCode(1, 15, 1, 40, 9),
        CoeffCode(1, 16, 1, 38, 9),
        CoeffCode(1, 17, 1, 48, 10),
        CoeffCode(1, 18, 1, 46, 10),
        CoeffCode(1, 19, 1, 44, 10),
        CoeffCode(1, 20, 1, 42, 10),
        CoeffCode(1, 21, 1, 40, 10),
        CoeffCode(1, 22, 1, 38, 10),
        CoeffCode(1, 23, 1, 36, 10),
        CoeffCode(1, 24, 1, 34, 10),
        CoeffCode(1, 25, 1, 14, 11),
        CoeffCode(1, 26, 1, 12, 11),
        CoeffCode(1, 27, 1, 10, 11),
        CoeffCode(1, 28, 1, 8, 11),
        CoeffCode(1, 29, 1, 72, 12),
        CoeffCode(1, 30, 1, 74, 12),
        CoeffCode(1, 31, 1, 76, 12),
        CoeffCode(1, 32, 1, 78, 12),
        CoeffCode(1, 33, 1, 176, 13),
        CoeffCode(1, 34, 1, 178, 13),
        CoeffCode(1, 35, 1, 180, 13),
        CoeffCode(1, 36, 1, 182, 13),
        CoeffCode(1, 37, 1, 184, 13),
        CoeffCode(1, 38, 1, 186, 13),
        CoeffCode(1, 39, 1, 188, 13),
        CoeffCode(1, 40, 1, 190, 13),
    )

fun decodeCoeff(bits: List<Int>, intra: Boolean): IntArray {
  var p = 0
  var index = if (intra) 0 else -1
  val result = IntArray(64)
  fun take(n: Int): Int {
    require(p + n <= bits.size) { "Truncated coefficient code" }
    var v = 0
    repeat(n) { v = (v shl 1) or bits[p++] }
    return v
  }
  if (intra) {
    val dc = take(8)
    require(dc != 0 && dc != 128) { "Forbidden INTRA DC" }
    result[0] = dc
  }
  var events = false
  var ended = false
  while (p < bits.size) {
    events = true
    var prefix = 0
    var found: Triple<Int, Int, Int>? = null
    for (width in 1..12) {
      prefix = (prefix shl 1) or take(1)
      if (width == 7 && prefix == 3) {
        val last = take(1)
        val run = take(6)
        val raw = take(8)
        val level = if (raw >= 128) raw - 256 else raw
        require(level != 0 && level != -128) { "Forbidden escape level" }
        found = Triple(last, run, level)
        break
      }
      val entry = coeffCodes.firstOrNull { it.width - 1 == width && (it.code ushr 1) == prefix }
      if (entry != null) {
        found = Triple(entry.last, entry.run, if (take(1) == 0) entry.mag else -entry.mag)
        break
      }
    }
    require(found != null) { "Unknown VLC" }
    val (last, run, level) = found
    index += run + 1
    require(index < 64) { "Run exceeds block" }
    result[coeffScan[index]] = level
    if (last == 1) {
      ended = true
      require(p == bits.size) { "Trailing bits after LAST" }
      break
    }
  }
  require(!events || ended) { "Missing LAST" }
  return result
}

data class CoeffInput(
    val intra: Long = 1,
    val pattern: String = "worked",
    val used: Long = 3,
    val capacity: Long = 192,
    val bad: String = "",
)

fun AssemblyCase.coeff64(name: String, offset: Int, value: Long, little: Boolean) {
  setWord(name, (if (little) value else value ushr 32).toInt(), offset)
  setWord(name, (if (little) value ushr 32 else value).toInt(), offset + 4)
}

fun AssemblyCase.checkCoeff(c: CoeffInput, little: Boolean) {
  val metadataGood = c.bad.isEmpty() || c.bad in listOf("level-high", "level-low", "dc")
  val domainGood = metadataGood && !c.bad.startsWith("level") && c.bad != "dc"
  val levels =
      IntArray(64) { i ->
        when (c.pattern) {
          "dense" -> if (i % 2 == 0) 1 else -1
          "mixed" -> if (i % 7 == 0) (i * 19) % 255 - 127 else 0
          else -> 0
        }
      }
  if (c.pattern == "worked") {
    levels[1] = 1
    levels[8] = -1
    if (c.intra == 0L) levels[0] = 1
  }
  if (c.pattern == "tail") levels[63] = -127
  if (c.intra == 1L) levels[0] = 255
  if (c.bad == "level-high") levels[63] = 128
  if (c.bad == "level-low") levels[63] = -128
  if (c.bad == "dc") levels[0] = 128
  val input = ByteArray(160) { 165.toByte() }
  for (i in 0..63) {
    input[18 + 2 * i] = (if (little) levels[i] else levels[i] ushr 8).toByte()
    input[19 + 2 * i] = (if (little) levels[i] ushr 8 else levels[i]).toByte()
  }
  for (i in input.indices step 4) {
    var v = 0
    for (j in 0..3) v = v or ((input[i + j].toInt() and 255) shl (8 * (if (little) j else 3 - j)))
    setWord("h263Coeff_levels", v, i)
  }
  val levelBase = readDataAddress("h263Coeff_levels") + 18
  val buffer = readDataAddress("h263Coeff_buffer") + 17
  val context = readDataAddress("h263Coeff_context") + 16
  for ((i, v) in
      listOf(if (c.bad == "buffer-wrap") -4L else buffer, c.capacity, c.used).withIndex()) coeff64(
      "h263Coeff_context",
      16 + i * 8,
      v,
      little,
  )
  for ((i, v) in
      listOf(
              if (c.bad == "level-align") levelBase + 1
              else if (c.bad == "level-wrap") -2L else levelBase,
              if (c.bad == "context-align") context + 1
              else if (c.bad == "context-wrap") -8L else context,
              c.intra,
              if (c.bad == "reserved") 1L else 0L,
          )
          .withIndex()) coeff64("h263Coeff_request", 16 + i * 8, v, little)
  val requestBefore = readBytes("h263Coeff_request", 64)
  val contextBefore = readBytes("h263Coeff_context", 56)
  val saved = (0..7).map { "s" + it } + listOf("fp", "gp")
  for ((i, n) in saved.withIndex()) setRegister(n, 0x1234567800000000L + i)
  setRegister("sp", readDataAddress("h263Coeff_stack", 32784))
  val sp = readRegister("sp")
  val ir = traceMemoryAccesses(readDataAddress("h263Coeff_levels"), 160)
  val br = traceMemoryAccesses(readDataAddress("h263Coeff_buffer"), 1048608)
  val cw = traceMemoryAccesses(readDataAddress("h263Coeff_context"), 56)
  val work = traceExecution()
  setRegister(
      "a0",
      when (c.bad) {
        "request-align" -> readDataAddress("h263Coeff_request") + 17
        "request-wrap" -> -8L
        else -> readDataAddress("h263Coeff_request") + 16
      },
  )
  callFunction("h263_emit_coefficients", "h263Coeff_return")
  val status = readRegister("v0")
  val bits = readRegister("v1")
  val intra = c.intra == 1L
  val count = levels.drop(if (intra) 1 else 0).count { it != 0 }
  val upper = (if (intra) 8 else 0) + 22 * count
  if (!domainGood) {
    expectRegister("v0", 1)
    expectRegister("v1", 0)
  } else {
    val mayBeFull = 8 * c.capacity - c.used < upper
    expectCondition(
        status == 0L || (mayBeFull && status == 2L),
        "Valid coefficient input succeeds when worst-case capacity fits, or rejects its chosen nonfitting plan",
    )
    if (status == 0L) {
      expectCondition(
          bits in 0L..upper.toLong(),
          "Appended bit count is within the legal event bound",
      )
      val n = bits.coerceIn(0L, upper.toLong()).toInt()
      val first = (c.used / 8).toInt()
      val offset = (c.used % 8).toInt()
      val bytes =
          if (n == 0) IntArray(0)
          else readBytes("h263Coeff_buffer", (offset + n + 7) / 8, 17 + first)
      val sequence =
          (0 until n).map { i -> (bytes[(offset + i) / 8] ushr (7 - (offset + i) % 8)) and 1 }
      runCatching { decodeCoeff(sequence, intra) }
          .onSuccess { decoded ->
            expectCondition(
                decoded.contentEquals(levels),
                "Independent baseline decoder recovers every natural-order input level",
            )
          }
          .onFailure {
            expectCondition(false, "Valid complete baseline block syntax: " + it.message)
          }
      for (i in 0 until offset) if (bytes.isNotEmpty())
          expectCondition(
              ((bytes[0] ushr (7 - i)) and 1) == ((165 ushr (7 - i)) and 1),
              "Existing prefix bits retained",
          )
      if (bytes.isNotEmpty())
          for (i in offset + n until bytes.size * 8) expectCondition(
              ((bytes[i / 8] ushr (7 - i % 8)) and 1) == ((165 ushr (7 - i % 8)) and 1),
              "Unused trailing bits retained",
          )
      println(
          "COEFF_WORK intra=$intra pattern=${c.pattern} used=${c.used} little=$little bits=$bits instructions=${work.snapshot().instructions}",
      )
    } else expectRegister("v1", 0)
  }
  val committed = status == 0L && bits > 0
  val expectedContext = contextBefore.copyOf()
  if (committed) {
    val value = c.used + bits
    for (j in 0..7) expectedContext[32 + j] =
        ((value ushr (8 * (if (little) j else 7 - j))) and 255).toInt()
  }
  expectCondition(
      readBytes("h263Coeff_context", 56).contentEquals(expectedContext),
      "Only successful nonempty publication changes the caller bit count",
  )
  for (e in ir.snapshot()) expectCondition(
      metadataGood && !e.write && e.address >= levelBase && e.address + e.length <= levelBase + 128,
      "Input reads follow metadata preflight and stay in active levels",
  )
  for (e in br.snapshot()) expectCondition(
      committed &&
          (0 until e.length).all { i ->
            val at = e.address - buffer + i
            at >= c.used / 8 && at <= (c.used + bits - 1) / 8
          },
      "Caller output accesses occur only for a fitting valid completed block and touched byte window",
  )
  for (e in cw.snapshot()) expectCondition(
      !e.write || (committed && e.address >= context + 16 && e.address + e.length <= context + 24),
      "Only committed used count is written",
  )
  expectCondition(
      readBytes("h263Coeff_request", 64).contentEquals(requestBefore),
      "Request and guards retained",
  )
  expectCondition(
      readBytes("h263Coeff_levels", 160)
          .contentEquals(input.map { it.toInt() and 255 }.toIntArray()),
      "Levels and guards retained",
  )
  expectCondition(
      readBytes("h263Coeff_buffer", 17).all { it == 165 } &&
          readBytes("h263Coeff_buffer", 15, 1048593).all { it == 165 },
      "Buffer outer guards retained",
  )
  expectCondition(
      readBytes("h263Coeff_stack", 16).all { it == 165 } &&
          readBytes("h263Coeff_stack", 16, 32784).all { it == 165 },
      "Stack outer guards retained",
  )
  for ((i, n) in saved.withIndex()) expectRegister(n, 0x1234567800000000L + i)
  expectRegister("sp", sp)
}

for (little in listOf(false, true)) {
  for (intra in 0L..1L) for (pattern in
      listOf("zero", "worked", "tail", "dense", "mixed")) for (used in listOf(0L, 3L, 7L)) {
    case(
        "Coefficients intra=$intra $pattern used=$used little=$little",
        instructionLimit = 300000,
        byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
    ) {
      checkCoeff(CoeffInput(intra, pattern, used), little)
    }
  }
  for (c in
      listOf(
          CoeffInput(0, "tail", 2, 3),
          CoeffInput(0, "tail", 3, 3),
          CoeffInput(1, "zero", 0, 1),
          CoeffInput(0, "zero", 0, 0),
          CoeffInput(1, "zero", 0, 0),
          CoeffInput(0, "tail", 8388586, 1048576),
      )) {
    case(
        "Coefficients capacity $c little=$little",
        instructionLimit = 300000,
        byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
    ) {
      checkCoeff(c, little)
    }
  }
  for (c in
      listOf(
          CoeffInput(bad = "request-align"),
          CoeffInput(bad = "request-wrap"),
          CoeffInput(bad = "level-align"),
          CoeffInput(bad = "level-wrap"),
          CoeffInput(bad = "context-align"),
          CoeffInput(bad = "context-wrap"),
          CoeffInput(bad = "buffer-wrap"),
          CoeffInput(bad = "reserved"),
          CoeffInput(intra = 4294967297, bad = "intra"),
          CoeffInput(capacity = 1048577, bad = "capacity"),
          CoeffInput(used = 1537, bad = "used"),
          CoeffInput(bad = "level-high"),
          CoeffInput(bad = "level-low"),
          CoeffInput(bad = "dc"),
      )) {
    case(
        "Coefficients invalid $c little=$little",
        instructionLimit = 300000,
        byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
    ) {
      checkCoeff(c, little)
    }
  }
}
