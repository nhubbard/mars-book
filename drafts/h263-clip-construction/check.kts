// Check real clip file operations and decode every accepted picture independently.
import edu.missouristate.mars.cli.grading.AssemblyCase
import edu.missouristate.mars.cli.grading.FileAccess.Operation
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sqrt

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

val blockBasis =
    Array(8) { k ->
      DoubleArray(8) { n -> if (k == 0) 1 / sqrt(8.0) else .5 * cos((2 * n + 1) * k * PI / 16) }
    }

fun blockTransform(input: IntArray, inverse: Boolean): IntArray {
  val m =
      Array(8) { k -> DoubleArray(8) { n -> if (inverse) blockBasis[n][k] else blockBasis[k][n] } }
  val first =
      Array(8) { y -> DoubleArray(8) { u -> (0..7).sumOf { x -> input[y * 8 + x] * m[u][x] } } }
  return IntArray(64) { i ->
    floor((0..7).sumOf { y -> first[y][i % 8] * m[i / 8][y] } + .5)
        .toInt()
        .coerceIn(if (inverse) -256 else -2048, if (inverse) 255 else 2047)
  }
}

fun blockLevel(c: Int, q: Int, dc: Boolean): Int {
  if (dc) {
    val d = Math.floorDiv(c + 4, 8).coerceIn(1, 254)
    return if (d == 128) 255 else d
  }
  val m = (abs(c) / (2 * q)).coerceAtMost(127)
  return if (c < 0) -m else m
}

fun blockRec(level: Int, q: Int, dc: Boolean): Int {
  if (dc) return 8 * (if (level == 255) 128 else level)
  if (level == 0) return 0
  return ((q * (2 * abs(level) + 1) - (if (q % 2 == 0) 1 else 0)) * (if (level < 0) -1 else 1))
      .coerceIn(-2048, 2047)
}

fun picturePrediction(reference: IntArray, block: Int, x: Int, y: Int, dx: Int, dy: Int): IntArray {
  val chroma = block >= 4
  val stride = if (chroma) 88 else 176
  val base = if (block == 4) 25344 else if (block == 5) 31680 else 0
  val bx = if (chroma) x / 2 else x + (block % 2) * 8
  val by = if (chroma) y / 2 else y + (block / 2) * 8
  val ox = if (chroma) Math.floorDiv(dx, 2) else dx
  val oy = if (chroma) Math.floorDiv(dy, 2) else dy
  val hx = if (chroma) Math.floorMod(dx, 2) else 0
  val hy = if (chroma) Math.floorMod(dy, 2) else 0
  return IntArray(64) { i ->
    val a = base + (by + oy + i / 8) * stride + bx + ox + i % 8
    when (hx + hy) {
      0 -> reference[a]
      1 -> (reference[a] + reference[a + if (hx == 1) 1 else stride] + 1) / 2
      else ->
          (reference[a] +
              reference[a + 1] +
              reference[a + stride] +
              reference[a + stride + 1] +
              2) / 4
    }
  }
}

fun pictureSearch(current: IntArray, reference: IntArray, x: Int, y: Int): Pair<Int, Int> {
  var best = Long.MAX_VALUE
  var winner = 0 to 0
  val candidates =
      listOf(0 to 0) +
          (-2..2).flatMap { dy -> (-2..2).map { dx -> dx to dy } }.filter { it != 0 to 0 }
  for ((dx, dy) in candidates) {
    if (x + dx !in 0..160 || y + dy !in 0..128) continue
    var sad = 0L
    for (row in 0..15) for (col in 0..15) sad +=
        abs(current[(y + row) * 176 + x + col] - reference[(y + dy + row) * 176 + x + dx + col])
    if (sad < best) {
      best = sad
      winner = dx to dy
    }
  }
  return winner
}

data class ClipInput(
    val kind: String = "good",
    val mode: Long = 1,
    val frames: Long = 2,
    val ticks: Long = 6,
    val quant: Long = 8,
    val expected: Int = 0,
    val accepted: Int = 2,
    val childFailure: Boolean = false,
)

fun clipFrame(index: Int): IntArray {
  val step = index - index / 6
  val left = 16 + 2 * (step % 56)
  val top = 32 + 2 * (step % 16)
  return IntArray(38016) { i ->
    if (i < 25344) {
      val x = i % 176
      val y = i / 176
      if (x in left until left + 32 && y in top until top + 32) 200
      else 32 + 12 * ((x / 8 + 3 * (y / 8)) % 12)
    } else {
      val cr = i >= 31680
      val n = i - if (cr) 31680 else 25344
      val x = n % 88
      val y = n / 88
      if (x in left / 2 until left / 2 + 16 && y in top / 2 until top / 2 + 16) if (cr) 176 else 96
      else 128
    }
  }
}

fun clipDecode(stream: ByteArray, pixels: ByteArray, frames: Int, ticks: Int, q: Int): Int {
  val reader = SyntaxBits(stream.flatMap { b -> (7 downTo 0).map { (b.toInt() ushr it) and 1 } })
  fun align() {
    val n = (8 - reader.position % 8) % 8
    require(reader.take(n) == 0)
  }
  var previous = IntArray(38016)
  repeat(frames) { frame ->
    val current = clipFrame(frame)
    val local = IntArray(38016) { pixels[frame * 38016 + it].toInt() and 255 }
    val p = frame != 0
    require(
        reader.take(22) == 32 &&
            reader.take(8) == frame * ticks % 256 &&
            reader.take(13) == 4160 + (if (p) 16 else 0) &&
            reader.take(5) == q &&
            reader.take(2) == 0,
    ) {
      "Picture header/clock"
    }
    for (row in 0..8) {
      var px = 0L
      var py = 0L
      if (row > 0) {
        align()
        require(reader.take(17) == 1 && reader.take(5) == row && reader.take(2) == if (p) 1 else 0)
        require(reader.take(5) == q)
      }
      for (col in 0..10) {
        val x = col * 16
        val y = row * 16
        val skipped = p && reader.take(1) == 1
        var intra = !p
        var mask = 0
        var mx = 0L
        var my = 0L
        if (!skipped) {
          val index = reader.vlc(if (p) syntaxMcbpc.drop(4) else syntaxMcbpc.take(4))
          intra = if (p) index >= 4 else true
          require(intra == !p)
          val rawY = reader.vlc(syntaxCbpy)
          mask = ((if (intra) rawY else 15 - rawY) shl 2) or (index % 4)
          if (!intra) {
            mx = syntaxWrap(px + reader.vlc(syntaxMvd) - 32)
            my = syntaxWrap(py + reader.vlc(syntaxMvd) - 32)
          }
        }
        if (p) {
          val selected = pictureSearch(current, previous, x, y)
          require(mx == selected.first * 2L && my == selected.second * 2L) {
            "Motion search must use the accepted reconstructed reference"
          }
        }
        for (block in 0..5) {
          val levels =
              if (skipped) IntArray(64) else reader.block(intra, mask and (1 shl (5 - block)) != 0)
          val chroma = block >= 4
          val stride = if (chroma) 88 else 176
          val base = if (block == 4) 25344 else if (block == 5) 31680 else 0
          val bx = if (chroma) x / 2 else x + (block % 2) * 8
          val by = if (chroma) y / 2 else y + (block / 2) * 8
          val prediction =
              if (intra) IntArray(64)
              else picturePrediction(previous, block, x, y, (mx / 2).toInt(), (my / 2).toInt())
          val forward =
              blockTransform(
                  IntArray(64) { i ->
                    current[base + (by + i / 8) * stride + bx + i % 8] - prediction[i]
                  },
                  false,
              )
          for (i in 0..63) require(
              levels[i] in
                  (-1..1).map { d ->
                    blockLevel((forward[i] + d).coerceIn(-2048, 2047), q, intra && i == 0)
                  },
          ) {
            "Levels from actual current/reference"
          }
          val inverse =
              blockTransform(IntArray(64) { i -> blockRec(levels[i], q, intra && i == 0) }, true)
          for (i in 0..63) require(
              abs(
                  local[base + (by + i / 8) * stride + bx + i % 8] -
                      (inverse[i] + prediction[i]).coerceIn(0, 255),
              ) <= 1,
          ) {
            "Reconstruction follows actual emitted levels"
          }
        }
        px = mx
        py = my
      }
    }
    align()
    previous = local
  }
  return reader.position / 8
}

fun AssemblyCase.clip64(name: String, at: Int, v: Long, little: Boolean) {
  setWord("h263Clip_" + name, (if (little) v else v ushr 32).toInt(), at)
  setWord("h263Clip_" + name, (if (little) v ushr 32 else v).toInt(), at + 4)
}

fun AssemblyCase.clipPath(
    name: String,
    text: String,
    little: Boolean,
    unterminated: Boolean = false,
) {
  val bytes = ByteArray(4096) { 165.toByte() }
  if (!unterminated) {
    text.toByteArray().copyInto(bytes)
    bytes[text.length] = 0
  }
  for (i in bytes.indices step 4) {
    var v = 0
    for (j in 0..3) v = v or ((bytes[i + j].toInt() and 255) shl (8 * (if (little) j else 3 - j)))
    setWord("h263Clip_" + name, v, i + 16)
  }
}

fun AssemblyCase.checkClip(c: ClipInput, little: Boolean) {
  val invalid = c.expected == 1
  val k = c.kind
  fun address(name: String) = readDataAddress("h263Clip_" + name) + 16
  clipPath(
      "input_path",
      if (k == "empty-path") "" else "clip.yuv",
      little,
      k == "unterminated-path",
  )
  clipPath("stream_path", if (k == "same-path") "clip.yuv" else "encoded.263", little)
  clipPath("reconstruction_path", "reconstructed.yuv", little)
  val input = (clipFrame(0) + clipFrame(1)).map { it.toByte() }.toByteArray()
  val raw =
      when (k) {
        "short-input" -> input.copyOf(38016 + 20)
        "trailing-input" -> input + byteArrayOf(42)
        else -> input
      }
  val writable =
      setOf("encoded.263", "reconstructed.yuv") -
          when (k) {
            "stream-open" -> setOf("encoded.263")
            "reconstruction-open" -> setOf("reconstructed.yuv")
            else -> emptySet()
          }
  val failRead =
      when (k) {
        "read-first" -> 0
        "read-later" -> 38026
        "read-probe" -> 76032
        else -> null
      }
  val failWrite =
      when (k) {
        "stream-first" -> mapOf("encoded.263" to 0)
        "stream-later" -> mapOf("encoded.263" to 706)
        "reconstruction-first" -> mapOf("reconstructed.yuv" to 100)
        "reconstruction-later" -> mapOf("reconstructed.yuv" to 38116)
        else -> emptyMap()
      }
  val files =
      provideFiles(
          if (k == "missing-input") emptyMap() else mapOf("clip.yuv" to raw),
          maxReadSize = if (k == "short-io") 137 else 4096,
          failAfterBytes = failRead,
          firstDescriptor = if (k == "descriptor-exhaustion") 31 else 17,
          writablePaths = writable,
          maxWriteSize = if (k == "short-io") 113 else 4096,
          failWriteAfterBytesByPath = failWrite,
          maxFileSize = 1048576,
      )
  val fields =
      listOf(
          if (k == "path-wrap") -4L else address("input_path"),
          address("stream_path"),
          address("reconstruction_path"),
          c.frames,
          c.ticks,
          c.quant,
          c.mode,
          if (k == "raw-wrap") -4L else address("raw"),
          address("a"),
          address("b"),
          if (k == "workspace-align") address("workspace") + 1
          else if (k == "workspace-wrap") -16L else address("workspace"),
          if (k == "encoded-wrap") -4L else address("encoded"),
          if (k == "reserved") 1L else 0L,
      )
  for ((i, v) in fields.withIndex()) clip64("request", 16 + i * 8, v, little)
  val request = readBytes("h263Clip_request", 136)
  val progress = readBytes("h263Clip_progress", 56)
  val paths =
      listOf("input_path", "stream_path", "reconstruction_path").associateWith {
        readBytes("h263Clip_" + it, 4128)
      }
  val saved = (0..7).map { "s" + it } + listOf("fp", "gp")
  for ((i, n) in saved.withIndex()) setRegister(n, 0x1234567800000000L + i)
  val sp = readDataAddress("h263Clip_stack", 65552)
  setRegister("sp", sp)
  val rejectedAccesses =
      if (invalid)
          listOf("raw", "a", "b", "workspace", "encoded").map {
            traceMemoryAccesses(
                address(it),
                if (it == "workspace") 169088 else if (it == "encoded") 131072 else 38016,
            )
          }
      else emptyList()
  val work = traceExecution()
  setRegister(
      "a0",
      if (k == "request-align") address("request") + 1
      else if (k == "request-wrap") -8L else address("request"),
  )
  setRegister(
      "a1",
      if (k == "progress-align") address("progress") + 1
      else if (k == "progress-wrap") -8L else address("progress"),
  )
  callFunction("h263_encode_clip", "h263Clip_return")
  expectRegister("v0", c.expected)
  expectRegister("v1", if (invalid) 0 else c.accepted.toLong())
  files.expectAllClosed()
  val history = files.snapshot()
  val opened = history.filter { it.operation == Operation.OPEN && it.result >= 0 }
  val closed = history.filter { it.operation == Operation.CLOSE }
  expectCondition(
      closed.all { it.result == 0 } &&
          closed.map { it.descriptor }.sorted() == opened.map { it.descriptor }.sorted(),
      "Every owned descriptor closed exactly once; no invalid close",
  )
  expectCondition(
      history
          .filter { it.operation == Operation.READ || it.operation == Operation.WRITE }
          .all { it.requestedBytes in 1..4096 },
      "All file requests make bounded nonempty progress",
  )
  if (invalid) {
    expectCondition(history.isEmpty(), "Invalid metadata/paths cause no file operations")
    expectCondition(
        readBytes("h263Clip_progress", 56).contentEquals(progress),
        "Invalid metadata retains result",
    )
    for (t in rejectedAccesses) expectCondition(
        t.snapshot().isEmpty(),
        "Invalid metadata reads no data buffers",
    )
  } else {
    fun field(at: Int): Long {
      val b = readBytes("h263Clip_progress", 8, 16 + at)
      var v = 0L
      for (j in 0..7) v = v or (b[j].toLong() shl (8 * (if (little) j else 7 - j)))
      return v
    }
    expectCondition(
        field(0) == c.accepted.toLong(),
        "Progress counts only pictures whose two writes completed",
    )
    val acceptedBytes = field(8)
    val reference = field(16)
    if (c.accepted == 0) {
      expectCondition(
          acceptedBytes == 0L && reference == 0L,
          "No reference is accepted before complete output",
      )
    }
    val stream =
        if (opened.any { it.path == "encoded.263" }) files.readFile("encoded.263")
        else byteArrayOf()
    val pixels =
        if (opened.any { it.path == "reconstructed.yuv" }) files.readFile("reconstructed.yuv")
        else byteArrayOf()
    if (c.accepted > 0) {
      expectCondition(
          acceptedBytes in 1L..stream.size.toLong() && pixels.size >= c.accepted * 38016,
          "Accepted prefix extents exist",
      )
      runCatching {
        require(
            clipDecode(
                stream.copyOfRange(0, acceptedBytes.toInt()),
                pixels,
                c.accepted,
                c.ticks.toInt(),
                c.quant.toInt(),
            ) == acceptedBytes.toInt(),
        )
      }
          .onFailure {
            expectCondition(false, "Accepted picture syntax/reconstruction: " + it.message)
          }
      expectCondition(
          reference in listOf(address("a"), address("b")),
          "Accepted reference names one supplied picture buffer",
      )
      if (reference in listOf(address("a"), address("b")))
          expectCondition(
              readBytes(reference, 38016)
                  .contentEquals(
                      pixels
                          .copyOfRange((c.accepted - 1) * 38016, c.accepted * 38016)
                          .map { it.toInt() and 255 }
                          .toIntArray(),
                  ),
              "Last accepted reference survives later input/output/child failure",
          )
    }
    if (c.expected == 0) {
      expectCondition(
          stream.size.toLong() == acceptedBytes && pixels.size == c.accepted * 38016,
          "Successful files contain exactly all accepted pictures",
      )
    }
    if (k != "missing-input") files.expectFile("clip.yuv", raw)
    println(
        "CLIP_WORK kind=$k mode=${c.mode} little=$little accepted=${c.accepted} bytes=$acceptedBytes instructions=${work.snapshot().instructions}",
    )
  }
  expectCondition(readBytes("h263Clip_request", 136).contentEquals(request), "Request retained")
  for ((name, bytes) in paths) expectCondition(
      readBytes("h263Clip_" + name, 4128).contentEquals(bytes),
      "Path input retained",
  )
  for ((name, size) in
      listOf(
          "raw" to 38016,
          "a" to 38016,
          "b" to 38016,
          "workspace" to 169088,
          "encoded" to 131072,
          "stack" to 65536,
          "progress" to 24,
      )) expectCondition(
      readBytes("h263Clip_" + name, 16).all { it == 165 } &&
          readBytes("h263Clip_" + name, 16, size + 16).all { it == 165 },
      "Arena guards retained: " + name,
  )
  for ((i, n) in saved.withIndex()) expectRegister(n, 0x1234567800000000L + i)
  expectRegister("sp", sp)
}

for (little in listOf(false, true)) {
  for (c in
      listOf(
          ClipInput(mode = 0),
          ClipInput(),
          ClipInput(kind = "short-io"),
          ClipInput("missing-input", expected = 2, accepted = 0),
          ClipInput("stream-open", expected = 2, accepted = 0),
          ClipInput("reconstruction-open", expected = 2, accepted = 0),
          ClipInput("descriptor-exhaustion", expected = 2, accepted = 0),
          ClipInput("short-input", expected = 3, accepted = 1),
          ClipInput("read-first", expected = 3, accepted = 0),
          ClipInput("read-later", expected = 3, accepted = 1),
          ClipInput("read-probe", expected = 3),
          ClipInput("trailing-input", expected = 3),
          ClipInput("stream-first", expected = 5, accepted = 0),
          ClipInput("stream-later", expected = 5, accepted = 1),
          ClipInput("reconstruction-first", expected = 5, accepted = 0),
          ClipInput("reconstruction-later", expected = 5, accepted = 1),
      )) {
    case(
        "Clip $c little=$little",
        instructionLimit = 150000000,
        byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
    ) {
      checkClip(c, little)
    }
  }
  val bad =
      listOf(
              "request-align",
              "request-wrap",
              "progress-align",
              "progress-wrap",
              "path-wrap",
              "raw-wrap",
              "workspace-align",
              "workspace-wrap",
              "encoded-wrap",
              "empty-path",
              "unterminated-path",
              "same-path",
              "reserved",
          )
          .map { ClipInput(it, expected = 1, accepted = 0) } +
          listOf(
              ClipInput("frames", frames = 0, expected = 1, accepted = 0),
              ClipInput("frames", frames = 300, expected = 1, accepted = 0),
              ClipInput("ticks", ticks = 0, expected = 1, accepted = 0),
              ClipInput("ticks", ticks = 256, expected = 1, accepted = 0),
              ClipInput("duration", frames = 2, ticks = 255, expected = 1, accepted = 0),
              ClipInput("quant", quant = 0, expected = 1, accepted = 0),
              ClipInput("quant", quant = 32, expected = 1, accepted = 0),
              ClipInput("mode", mode = 4294967297, expected = 1, accepted = 0),
          )
  for (c in bad) case(
      "Clip invalid $c little=$little",
      instructionLimit = 200000,
      byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
  ) {
    checkClip(c, little)
  }
}
