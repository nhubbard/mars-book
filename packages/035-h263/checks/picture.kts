// Decode every picture macroblock and independently reconstruct its selected levels.
import edu.missouristate.mars.cli.grading.AssemblyCase
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

data class PictureInput(
    val picture: Long = 0,
    val tr: Long = 0,
    val q: Long = 8,
    val mode: Long = 1,
    val gfid: Long = 0,
    val pattern: String = "flat",
    val capacity: Long = 131072,
    val bad: String = "",
    val forcedFailure: Boolean = false,
)

fun picturePixels(pattern: String, reference: Boolean): IntArray =
    IntArray(38016) { i ->
      if (pattern == "flat") 128
      else if (i >= 25344) {
        if (i < 31680) 96 else 176
      } else {
        val x = i % 176
        val y = i / 176
        val shift = if (reference) 0 else 2
        if (pattern == "texture") 32 + ((x * 13 + y * 7) % 160)
        else 32 + 12 * ((((x - shift).coerceAtLeast(0)) / 8 + 3 * (y / 8)) % 12)
      }
    }

fun AssemblyCase.picture64(name: String, at: Int, v: Long, little: Boolean) {
  setWord(name, (if (little) v else v ushr 32).toInt(), at)
  setWord(name, (if (little) v ushr 32 else v).toInt(), at + 4)
}

fun AssemblyCase.picturePut(name: String, a: IntArray, little: Boolean) {
  for (i in a.indices step 4) {
    var v = 0
    for (j in 0..3) v = v or (a[i + j] shl (8 * (if (little) j else 3 - j)))
    setWord(name, v, i + 16)
  }
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

fun AssemblyCase.checkPicture(c: PictureInput, little: Boolean) {
  val valid = c.bad.isEmpty()
  val current = picturePixels(c.pattern, false)
  val reference = picturePixels(c.pattern, true)
  picturePut("h263Picture_current", current, little)
  picturePut("h263Picture_reference", reference, little)
  fun address(name: String) = readDataAddress("h263Picture_" + name) + 16
  val ctx = address("context")
  val output = address("buffer")
  for ((i, v) in
      listOf(
              if (c.bad == "buffer-wrap") -4L else output,
              c.capacity,
              if (c.bad == "used") 1L else 0L,
          )
          .withIndex()) picture64("h263Picture_context", 16 + i * 8, v, little)
  val fields =
      listOf(
          if (c.bad == "current-wrap") -4L else address("current"),
          if (c.picture == 1L || c.bad == "I-reference")
              if (c.bad == "reference-wrap") -4L else address("reference")
          else 0L,
          if (c.bad == "reconstruction-wrap") -4L else address("reconstruction"),
          if (c.bad == "context-align") ctx + 1 else if (c.bad == "context-wrap") -8L else ctx,
          if (c.bad == "workspace-align") address("workspace") + 1
          else if (c.bad == "workspace-wrap") -16L else address("workspace"),
          if (c.bad == "workspace-size") 169087L else 169088L,
          c.picture,
          c.tr,
          c.q,
          c.mode,
          c.gfid,
          if (c.bad == "reserved") 1L else 0L,
      )
  for ((i, v) in fields.withIndex()) picture64("h263Picture_request", 16 + i * 8, v, little)
  val request = readBytes("h263Picture_request", 128)
  val context = readBytes("h263Picture_context", 56)
  val saved = (0..7).map { "s" + it } + listOf("fp", "gp")
  for ((i, n) in saved.withIndex()) setRegister(n, 0x1234567800000000L + i)
  val sp = readDataAddress("h263Picture_stack", 65552)
  setRegister("sp", sp)
  val reads =
      if (!valid)
          listOf("current", "reference", "workspace").map {
            traceMemoryAccesses(address(it), if (it == "workspace") 169088 else 38016)
          }
      else emptyList()
  val writes = traceMemoryAccesses(readDataAddress("h263Picture_buffer"), 131104)
  val rw = traceMemoryAccesses(readDataAddress("h263Picture_reconstruction"), 38048)
  val cw = traceMemoryAccesses(readDataAddress("h263Picture_context"), 56)
  val work = traceExecution()
  setRegister(
      "a0",
      if (c.bad == "request-align") address("request") + 1
      else if (c.bad == "request-wrap") -8L else address("request"),
  )
  callFunction("h263_encode_picture", "h263Picture_return")
  val status = readRegister("v0")
  val bytes = readRegister("v1")
  val success = valid && c.capacity >= 105070 && !c.forcedFailure
  expectRegister(
      "v0",
      if (!valid) 1 else if (c.forcedFailure) 3 else if (c.capacity == 0L) 2 else 0,
  )
  if (success) {
    expectCondition(bytes in 1..105070, "Complete picture byte length is inside the derived bound")
    val raw = readBytes("h263Picture_buffer", bytes.coerceIn(1, 105070).toInt(), 16)
    val r = SyntaxBits(raw.flatMap { b -> (7 downTo 0).map { (b ushr it) and 1 } })
    val reconstruction = readBytes("h263Picture_reconstruction", 38016, 16)
    runCatching {
      require(
          r.take(22) == 32 &&
              r.take(8) == c.tr.toInt() &&
              r.take(13) == 4160 + (c.picture.toInt() shl 4) &&
              r.take(5) == c.q.toInt() &&
              r.take(2) == 0,
      ) {
        "Picture header differs from admitted controls"
      }
      fun align() {
        val n = (8 - r.position % 8) % 8
        require(r.take(n) == 0) { "Nonzero start-code/alignment stuffing" }
      }
      var nonzeroMotion = 0
      var skips = 0
      for (row in 0..8) {
        var px = 0L
        var py = 0L
        if (row > 0) {
          align()
          require(
              r.take(17) == 1 &&
                  r.take(5) == row &&
                  r.take(2) == c.gfid.toInt() &&
                  r.take(5) == c.q.toInt(),
          ) {
            "GOB header mismatch"
          }
        }
        for (col in 0..10) {
          val x = col * 16
          val y = row * 16
          val p = c.picture == 1L
          val skipped = p && r.take(1) == 1
          var intra = !p
          var mask = 0
          var mx = 0L
          var my = 0L
          if (skipped) skips++
          else {
            val index = r.vlc(if (p) syntaxMcbpc.drop(4) else syntaxMcbpc.take(4))
            intra = if (p) index >= 4 else true
            require(intra == !p) {
              "This controller's stated policy selects INTRA only for I-pictures"
            }
            val rawY = r.vlc(syntaxCbpy)
            mask = ((if (intra) rawY else 15 - rawY) shl 2) or (index % 4)
            if (!intra) {
              mx = syntaxWrap(px + r.vlc(syntaxMvd) - 32)
              my = syntaxWrap(py + r.vlc(syntaxMvd) - 32)
            }
          }
          if (p) {
            val selected = pictureSearch(current, reference, x, y)
            require(mx == selected.first * 2L && my == selected.second * 2L) {
              "Search inputs/origin or selected motion disagree"
            }
            if (mx != 0L || my != 0L) nonzeroMotion++
          }
          for (block in 0..5) {
            val levels =
                if (skipped) IntArray(64) else r.block(intra, mask and (1 shl (5 - block)) != 0)
            val chroma = block >= 4
            val stride = if (chroma) 88 else 176
            val base = if (block == 4) 25344 else if (block == 5) 31680 else 0
            val bx = if (chroma) x / 2 else x + (block % 2) * 8
            val by = if (chroma) y / 2 else y + (block / 2) * 8
            val prediction =
                if (intra) IntArray(64)
                else picturePrediction(reference, block, x, y, (mx / 2).toInt(), (my / 2).toInt())
            val forward =
                blockTransform(
                    IntArray(64) { i ->
                      current[base + (by + i / 8) * stride + bx + i % 8] - prediction[i]
                    },
                    false,
                )
            for (i in 0..63) {
              val dc = intra && i == 0
              require(
                  levels[i] in
                      (-1..1).map { d ->
                        blockLevel((forward[i] + d).coerceIn(-2048, 2047), c.q.toInt(), dc)
                      },
              ) {
                "Selected levels disagree with current/prediction transform"
              }
            }
            val inverse =
                blockTransform(
                    IntArray(64) { i -> blockRec(levels[i], c.q.toInt(), intra && i == 0) },
                    true,
                )
            for (i in 0..63) {
              val actual = reconstruction[base + (by + i / 8) * stride + bx + i % 8]
              val predicted = (inverse[i] + prediction[i]).coerceIn(0, 255)
              require(abs(actual - predicted) <= 1) {
                "Published picture is not an accurate reconstruction of emitted levels"
              }
            }
          }
          px = mx
          py = my
        }
      }
      align()
      require(r.position == raw.size * 8) { "Trailing data after complete picture" }
      println(
          "PICTURE_WORK picture=${c.picture} pattern=${c.pattern} mode=${c.mode} little=$little bytes=$bytes motion=$nonzeroMotion skips=$skips instructions=${work.snapshot().instructions}",
      )
    }
        .onFailure {
          expectCondition(false, "Independent picture decoding/reconstruction: " + it.message)
        }
  } else {
    expectRegister("v1", 0)
    expectCondition(
        readBytes("h263Picture_buffer", 131104).all { it == 165 } &&
            readBytes("h263Picture_reconstruction", 38048).all { it == 165 },
        "Failed picture retains both output arenas",
    )
  }
  val expected = context.copyOf()
  if (success)
      for (j in 0..7) expected[32 + j] =
          ((bytes * 8 ushr (8 * (if (little) j else 7 - j))) and 255).toInt()
  expectCondition(
      readBytes("h263Picture_context", 56).contentEquals(expected),
      "Only success publishes complete used count",
  )
  for (e in writes.snapshot()) expectCondition(
      success && e.write && e.address >= output && e.address + e.length <= output + bytes,
      "Caller stream is write-only during final successful publication",
  )
  for (e in rw.snapshot()) expectCondition(
      success &&
          e.write &&
          e.address >= address("reconstruction") &&
          e.address + e.length <= address("reconstruction") + 38016,
      "Caller reconstruction is write-only during successful publication",
  )
  for (e in cw.snapshot()) expectCondition(
      !e.write || (success && e.address >= ctx + 16 && e.address + e.length <= ctx + 24),
      "Only successful context count writes",
  )
  for (t in reads) expectCondition(
      t.snapshot().isEmpty(),
      "Invalid metadata reads no pixels/workspace",
  )
  expectCondition(
      readBytes("h263Picture_current", 38016, 16).contentEquals(current) &&
          readBytes("h263Picture_reference", 38016, 16).contentEquals(reference),
      "Input pictures remain unchanged",
  )
  expectCondition(readBytes("h263Picture_request", 128).contentEquals(request), "Request retained")
  for ((name, size) in
      listOf(
          "current" to 38016,
          "reference" to 38016,
          "reconstruction" to 38016,
          "workspace" to 169088,
          "buffer" to 131072,
          "stack" to 65536,
      )) expectCondition(
      readBytes("h263Picture_" + name, 16).all { it == 165 } &&
          readBytes("h263Picture_" + name, 16, size + 16).all { it == 165 },
      "Arena guards retained: " + name,
  )
  for ((i, n) in saved.withIndex()) expectRegister(n, 0x1234567800000000L + i)
  expectRegister("sp", sp)
}

for (little in listOf(false, true)) {
  for (c in
      listOf(
          PictureInput(),
          PictureInput(mode = 0, pattern = "texture", tr = 255, q = 31, gfid = 3),
          PictureInput(picture = 1, tr = 6, gfid = 1),
          PictureInput(picture = 1, pattern = "moving", tr = 12, gfid = 1),
          PictureInput(picture = 1, pattern = "moving", mode = 0, tr = 18, gfid = 2),
          PictureInput(capacity = 0),
      )) {
    case(
        "Picture $c little=$little",
        instructionLimit = 100000000,
        byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
    ) {
      checkPicture(c, little)
    }
  }
  val bad =
      listOf(
          PictureInput(bad = "request-align"),
          PictureInput(bad = "request-wrap"),
          PictureInput(bad = "current-wrap"),
          PictureInput(picture = 1, bad = "reference-wrap"),
          PictureInput(bad = "reconstruction-wrap"),
          PictureInput(bad = "context-align"),
          PictureInput(bad = "context-wrap"),
          PictureInput(bad = "workspace-align"),
          PictureInput(bad = "workspace-wrap"),
          PictureInput(bad = "workspace-size"),
          PictureInput(bad = "I-reference"),
          PictureInput(bad = "reserved"),
          PictureInput(bad = "used"),
          PictureInput(bad = "buffer-wrap"),
          PictureInput(picture = 4294967297, bad = "picture"),
          PictureInput(tr = 256, bad = "tr"),
          PictureInput(q = 0, bad = "quant"),
          PictureInput(q = 32, bad = "quant"),
          PictureInput(mode = 4294967297, bad = "mode"),
          PictureInput(gfid = 4, bad = "gfid"),
          PictureInput(capacity = 1048577, bad = "capacity"),
      )
  for (c in bad) case(
      "Picture invalid $c little=$little",
      instructionLimit = 10000,
      byteOrder = if (little) ByteOrder.LITTLE_ENDIAN else ByteOrder.BIG_ENDIAN,
  ) {
    checkPicture(c, little)
  }
}
