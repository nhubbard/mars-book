// Decode macroblock meaning instead of requiring one private byte string.
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

val syntaxMcbpc =
    listOf(
        1 to 1,
        1 to 3,
        2 to 3,
        3 to 3,
        1 to 1,
        3 to 4,
        2 to 4,
        5 to 6,
        3 to 5,
        4 to 8,
        3 to 8,
        3 to 7,
    )
val syntaxCbpy =
    listOf(
        3 to 4,
        5 to 5,
        4 to 5,
        9 to 4,
        3 to 5,
        7 to 4,
        2 to 6,
        11 to 4,
        2 to 5,
        3 to 6,
        5 to 4,
        10 to 4,
        4 to 4,
        8 to 4,
        6 to 4,
        3 to 2,
    )
val syntaxMvd =
    listOf(
        5 to 13,
        7 to 13,
        5 to 12,
        7 to 12,
        9 to 12,
        11 to 12,
        13 to 12,
        15 to 12,
        9 to 11,
        11 to 11,
        13 to 11,
        15 to 11,
        17 to 11,
        19 to 11,
        21 to 11,
        23 to 11,
        25 to 11,
        27 to 11,
        29 to 11,
        31 to 11,
        33 to 11,
        35 to 11,
        19 to 10,
        21 to 10,
        23 to 10,
        7 to 8,
        9 to 8,
        11 to 8,
        7 to 7,
        3 to 5,
        3 to 4,
        3 to 3,
        1 to 1,
        2 to 3,
        2 to 4,
        2 to 5,
        6 to 7,
        10 to 8,
        8 to 8,
        6 to 8,
        22 to 10,
        20 to 10,
        18 to 10,
        34 to 11,
        32 to 11,
        30 to 11,
        28 to 11,
        26 to 11,
        24 to 11,
        22 to 11,
        20 to 11,
        18 to 11,
        16 to 11,
        14 to 11,
        12 to 11,
        10 to 11,
        8 to 11,
        14 to 12,
        12 to 12,
        10 to 12,
        8 to 12,
        6 to 12,
        4 to 12,
        6 to 13,
    )

class SyntaxBits(val bits: List<Int>) {
  var position = 0

  fun take(n: Int): Int {
    require(position + n <= bits.size) { "Truncated macroblock" }
    var v = 0
    repeat(n) { v = (v shl 1) or bits[position++] }
    return v
  }

  fun vlc(table: List<Pair<Int, Int>>): Int {
    var prefix = 0
    for (width in 1..13) {
      prefix = (prefix shl 1) or take(1)
      val at = table.indexOfFirst { it.first == prefix && it.second == width }
      if (at >= 0) return at
    }
    error("Unsupported macroblock code")
  }

  fun block(intra: Boolean, active: Boolean): IntArray {
    val out = IntArray(64)
    var scan = if (intra) 0 else -1
    if (intra) {
      val dc = take(8)
      require(dc != 0 && dc != 128)
      out[0] = dc
    }
    if (!active) return out
    while (true) {
      var prefix = 0
      var event: Triple<Int, Int, Int>? = null
      for (width in 1..12) {
        prefix = (prefix shl 1) or take(1)
        if (width == 7 && prefix == 3) {
          val last = take(1)
          val run = take(6)
          val b = take(8)
          val level = if (b >= 128) b - 256 else b
          require(level != 0 && level != -128)
          event = Triple(last, run, level)
          break
        }
        val entry = coeffCodes.firstOrNull { it.width - 1 == width && (it.code ushr 1) == prefix }
        if (entry != null) {
          event = Triple(entry.last, entry.run, if (take(1) == 0) entry.mag else -entry.mag)
          break
        }
      }
      require(event != null)
      val (last, run, level) = event
      scan += run + 1
      require(scan < 64)
      out[coeffScan[scan]] = level
      if (last == 1) return out
    }
  }
}

fun syntaxWrap(value: Long) = ((value + 32) and 63) - 32

data class SyntaxInput(
    val picture: Long = 0,
    val intra: Long = 1,
    val mask: Int = 0,
    val pattern: String = "single",
    val mx: Long = 0,
    val my: Long = 0,
    val px: Long = 0,
    val py: Long = 0,
    val allowSkip: Long = 1,
    val used: Long = 3,
    val capacity: Long = 1152,
    val bad: String = "",
)

fun syntaxBlocks(c: SyntaxInput): Array<IntArray> {
  val blocks =
      Array(6) { block ->
        IntArray(64) { i ->
          if (c.mask and (1 shl (5 - block)) != 0) {
            when (c.pattern) {
              "dense" -> if (i % 2 == 0) 1 else -1
              "tail" ->
                  if (i == 63) {
                    if (block % 2 == 0) 127 else -127
                  } else 0
              else ->
                  if (i == (if (c.intra == 1L) 1 else 0)) {
                    if (block % 2 == 0) 1 else -1
                  } else 0
            }
          } else 0
        }
      }
  if (c.intra == 1L) for (b in blocks) b[0] = 255
  if (c.bad == "level-high") blocks[5][63] = 128
  if (c.bad == "level-low") blocks[5][63] = -128
  if (c.bad == "dc") blocks[5][0] = 128
  return blocks
}

fun AssemblyCase.syntax64(name: String, offset: Int, value: Long, little: Boolean) {
  setWord(name, (if (little) value else value ushr 32).toInt(), offset)
  setWord(name, (if (little) value ushr 32 else value).toInt(), offset + 4)
}

fun AssemblyCase.checkSyntax(c: SyntaxInput, little: Boolean) {
  val metadataGood = c.bad.isEmpty() || c.bad in listOf("level-high", "level-low", "dc")
  val domainGood = c.bad.isEmpty()
  val blocks = syntaxBlocks(c)
  val bytes = ByteArray(800) { 165.toByte() }
  for (block in 0..5) for (i in 0..63) {
    val at = 18 + block * 128 + i * 2
    val v = blocks[block][i]
    bytes[at] = (if (little) v else v ushr 8).toByte()
    bytes[at + 1] = (if (little) v ushr 8 else v).toByte()
  }
  for (i in bytes.indices step 4) {
    var v = 0
    for (j in 0..3) v = v or ((bytes[i + j].toInt() and 255) shl (8 * (if (little) j else 3 - j)))
    setWord("h263Syntax_levels", v, i)
  }
  val levels = readDataAddress("h263Syntax_levels") + 18
  val context = readDataAddress("h263Syntax_context") + 16
  val buffer = readDataAddress("h263Syntax_buffer") + 17
  for ((i, v) in
      listOf(if (c.bad == "buffer-wrap") -4L else buffer, c.capacity, c.used).withIndex()) syntax64(
      "h263Syntax_context",
      16 + i * 8,
      v,
      little,
  )
  val fields =
      listOf(
          if (c.bad == "level-align") levels + 1 else if (c.bad == "level-wrap") -2L else levels,
          if (c.bad == "context-align") context + 1
          else if (c.bad == "context-wrap") -8L else context,
          c.picture,
          c.intra,
          c.mx,
          c.my,
          c.px,
          c.py,
          c.allowSkip,
          if (c.bad == "reserved") 1L else 0L,
      )
  for ((i, v) in fields.withIndex()) syntax64("h263Syntax_request", 16 + i * 8, v, little)
  val requestBefore = readBytes("h263Syntax_request", 112)
  val contextBefore = readBytes("h263Syntax_context", 56)
  val saved = (0..7).map { "s" + it } + listOf("fp", "gp")
  for ((i, n) in saved.withIndex()) setRegister(n, 0x1234567800000000L + i)
  setRegister("sp", readDataAddress("h263Syntax_stack", 32784))
  val sp = readRegister("sp")
  val lr = traceMemoryAccesses(readDataAddress("h263Syntax_levels"), 800)
  val br = traceMemoryAccesses(readDataAddress("h263Syntax_buffer"), 1048608)
  val cw = traceMemoryAccesses(readDataAddress("h263Syntax_context"), 56)
  val work = traceExecution()
  setRegister(
      "a0",
      when (c.bad) {
        "request-align" -> readDataAddress("h263Syntax_request") + 17
        "request-wrap" -> -8L
        else -> readDataAddress("h263Syntax_request") + 16
      },
  )
  callFunction("h263_emit_macroblock", "h263Syntax_return")
  val status = readRegister("v0")
  val appended = readRegister("v1")
  val intra = c.intra == 1L
  val picture = c.picture == 1L
  val group = if (!picture) 0 else if (intra) 2 else 1
  val cbpy = if (intra) c.mask ushr 2 else 15 - (c.mask ushr 2)
  var upper =
      if (domainGood) {
        (if (picture) 1 else 0) +
            syntaxMcbpc[group * 4 + (c.mask and 3)].second +
            syntaxCbpy[cbpy].second
      } else 0
  if (domainGood && !intra)
      upper +=
          syntaxMvd[(syntaxWrap(c.mx - c.px) + 32).toInt()].second +
              syntaxMvd[(syntaxWrap(c.my - c.py) + 32).toInt()].second
  if (domainGood)
      for (b in blocks) upper +=
          (if (intra) 8 else 0) + 22 * b.drop(if (intra) 1 else 0).count { it != 0 }
  if (!domainGood) {
    expectRegister("v0", 1)
    expectRegister("v1", 0)
  } else {
    expectCondition(
        status == 0L || (status == 2L && 8 * c.capacity - c.used < upper),
        "Valid macroblock succeeds with worst-case capacity or rejects a chosen nonfitting encoding",
    )
    if (status == 0L) {
      expectCondition(
          appended in 1L..upper.toLong(),
          "Complete macroblock bit count within baseline bound",
      )
      val n = appended.coerceIn(1L, upper.toLong()).toInt()
      val offset = (c.used % 8).toInt()
      val raw = readBytes("h263Syntax_buffer", (offset + n + 7) / 8, 17 + (c.used / 8).toInt())
      val bits = List(n) { i -> (raw[(offset + i) / 8] ushr (7 - (offset + i) % 8)) and 1 }
      runCatching {
        val reader = SyntaxBits(bits)
        val skip = picture && reader.take(1) == 1
        if (skip) {
          require(!intra && c.allowSkip == 1L && c.mask == 0 && c.mx == 0L && c.my == 0L)
          require(reader.position == n)
        } else {
          val index = reader.vlc(if (picture) syntaxMcbpc.drop(4) else syntaxMcbpc.take(4))
          val decodedIntra = if (picture) index >= 4 else true
          val cbpc = index % 4
          val rawY = reader.vlc(syntaxCbpy)
          val decodedMask = ((if (decodedIntra) rawY else 15 - rawY) shl 2) or cbpc
          require(decodedIntra == intra && decodedMask == c.mask) { "Type/presence mismatch" }
          if (!decodedIntra) {
            val x = syntaxWrap(c.px + reader.vlc(syntaxMvd) - 32)
            val y = syntaxWrap(c.py + reader.vlc(syntaxMvd) - 32)
            require(x == c.mx && y == c.my) { "Decoded motion mismatch" }
          }
          for (block in 0..5) {
            val active = decodedMask and (1 shl (5 - block)) != 0
            val decoded = reader.block(decodedIntra, active)
            require(decoded.contentEquals(blocks[block])) { "Decoded block mismatch" }
          }
          require(reader.position == n) { "Trailing macroblock bits" }
        }
      }
          .onFailure {
            expectCondition(false, "Independent baseline macroblock decoding: " + it.message)
          }
      for (i in 0 until offset) expectCondition(
          ((raw[0] ushr (7 - i)) and 1) == ((165 ushr (7 - i)) and 1),
          "Existing prefix retained",
      )
      for (i in offset + n until raw.size * 8) expectCondition(
          ((raw[i / 8] ushr (7 - i % 8)) and 1) == ((165 ushr (7 - i % 8)) and 1),
          "Unused suffix retained",
      )
      println(
          "SYNTAX_WORK picture=${c.picture} intra=$intra mask=${c.mask} pattern=${c.pattern} used=${c.used} little=$little bits=$appended instructions=${work.snapshot().instructions}",
      )
    } else expectRegister("v1", 0)
  }
  val committed = status == 0L
  val expectedContext = contextBefore.copyOf()
  if (committed) {
    val value = c.used + appended
    for (j in 0..7) expectedContext[32 + j] =
        ((value ushr (8 * (if (little) j else 7 - j))) and 255).toInt()
  }
  expectCondition(
      readBytes("h263Syntax_context", 56).contentEquals(expectedContext),
      "Only successful publication changes caller used count",
  )
  for (e in lr.snapshot()) expectCondition(
      metadataGood && !e.write && e.address >= levels && e.address + e.length <= levels + 768,
      "All six level windows are read only after valid metadata",
  )
  for (e in br.snapshot()) expectCondition(
      committed &&
          (0 until e.length).all { i ->
            val at = e.address - buffer + i
            at >= c.used / 8 && at <= (c.used + appended - 1) / 8
          },
      "Caller buffer accessed only for a completed fitting macroblock and touched byte window",
  )
  for (e in cw.snapshot()) expectCondition(
      !e.write || (committed && e.address >= context + 16 && e.address + e.length <= context + 24),
      "Only committed bit count is written",
  )
  expectCondition(
      readBytes("h263Syntax_levels", 800)
          .contentEquals(bytes.map { it.toInt() and 255 }.toIntArray()),
      "All source levels and guards retained",
  )
  expectCondition(
      readBytes("h263Syntax_request", 112).contentEquals(requestBefore),
      "Request and guards retained",
  )
  expectCondition(
      readBytes("h263Syntax_buffer", 17).all { it == 165 } &&
          readBytes("h263Syntax_buffer", 15, 1048593).all { it == 165 },
      "Buffer guards retained",
  )
  expectCondition(
      readBytes("h263Syntax_stack", 16).all { it == 165 } &&
          readBytes("h263Syntax_stack", 16, 32784).all { it == 165 },
      "Private stack guards retained",
  )
  for ((i, n) in saved.withIndex()) expectRegister(n, 0x1234567800000000L + i)
  expectRegister("sp", sp)
}

for (little in listOf(false, true)) {
  for ((picture, intra) in listOf(0L to 1L, 1L to 1L, 1L to 0L)) for (mask in
      listOf(0, 1, 2, 4, 8, 16, 32, 33, 63)) {
    case(
        "Macroblock picture=$picture intra=$intra mask=$mask little=$little",
        instructionLimit = 2000000,
        byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
    ) {
      checkSyntax(
          SyntaxInput(
              picture,
              intra,
              mask,
              mx = if (intra == 0L) -4 else 0,
              my = if (intra == 0L) 6 else 0,
              px = if (intra == 0L) 3 else 0,
              py = if (intra == 0L) -2 else 0,
          ),
          little,
      )
    }
  }
  for (c in
      listOf(
          SyntaxInput(1, 0, 0),
          SyntaxInput(1, 0, 0, px = 31, py = -32),
          SyntaxInput(1, 0, 0, allowSkip = 0),
          SyntaxInput(1, 0, 0, px = 31, py = -32, allowSkip = 0),
          SyntaxInput(1, 0, 0, used = 7, capacity = 1),
          SyntaxInput(0, 1, 0, capacity = 7),
          SyntaxInput(0, 1, 0, used = 0, capacity = 0),
          SyntaxInput(0, 1, 63, "dense"),
          SyntaxInput(1, 0, 63, "tail", mx = -32, my = 31, px = 31, py = -32),
          SyntaxInput(1, 1, 33, used = 7),
      )) {
    case(
        "Macroblock boundary $c little=$little",
        instructionLimit = 2000000,
        byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
    ) {
      checkSyntax(c, little)
    }
  }
  for (c in
      listOf(
          SyntaxInput(bad = "request-align"),
          SyntaxInput(bad = "request-wrap"),
          SyntaxInput(bad = "level-align"),
          SyntaxInput(bad = "level-wrap"),
          SyntaxInput(bad = "context-align"),
          SyntaxInput(bad = "context-wrap"),
          SyntaxInput(bad = "buffer-wrap"),
          SyntaxInput(bad = "reserved"),
          SyntaxInput(picture = 4294967297, bad = "picture"),
          SyntaxInput(intra = 4294967297, bad = "intra"),
          SyntaxInput(picture = 0, intra = 0, bad = "I-INTER"),
          SyntaxInput(mx = 1, bad = "INTRA-motion"),
          SyntaxInput(px = 1, bad = "INTRA-predictor"),
          SyntaxInput(picture = 1, intra = 0, mx = 32, bad = "motion"),
          SyntaxInput(picture = 1, intra = 0, py = -33, bad = "predictor"),
          SyntaxInput(allowSkip = 4294967297, bad = "allow-skip"),
          SyntaxInput(capacity = 1048577, bad = "capacity"),
          SyntaxInput(used = 9217, bad = "used"),
          SyntaxInput(bad = "level-high"),
          SyntaxInput(bad = "level-low"),
          SyntaxInput(bad = "dc"),
      )) {
    case(
        "Macroblock invalid $c little=$little",
        instructionLimit = 2000000,
        byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
    ) {
      checkSyntax(c, little)
    }
  }
}
