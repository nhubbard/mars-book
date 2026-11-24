import edu.missouristate.mars.cli.grading.AssemblyCase
import edu.missouristate.mars.cli.grading.FileAccess.Operation
import java.lang.Integer.rotateRight
import java.security.MessageDigest

// Public arithmetic model uses native Int wraparound; the private prototype uses masked Longs.
val shaIv =
    intArrayOf(
        0x6a09e667.toInt(),
        0xbb67ae85.toInt(),
        0x3c6ef372.toInt(),
        0xa54ff53a.toInt(),
        0x510e527f.toInt(),
        0x9b05688c.toInt(),
        0x1f83d9ab.toInt(),
        0x5be0cd19.toInt(),
    )
val shaConstants =
    intArrayOf(
        0x428a2f98.toInt(),
        0x71374491.toInt(),
        0xb5c0fbcf.toInt(),
        0xe9b5dba5.toInt(),
        0x3956c25b.toInt(),
        0x59f111f1.toInt(),
        0x923f82a4.toInt(),
        0xab1c5ed5.toInt(),
        0xd807aa98.toInt(),
        0x12835b01.toInt(),
        0x243185be.toInt(),
        0x550c7dc3.toInt(),
        0x72be5d74.toInt(),
        0x80deb1fe.toInt(),
        0x9bdc06a7.toInt(),
        0xc19bf174.toInt(),
        0xe49b69c1.toInt(),
        0xefbe4786.toInt(),
        0x0fc19dc6.toInt(),
        0x240ca1cc.toInt(),
        0x2de92c6f.toInt(),
        0x4a7484aa.toInt(),
        0x5cb0a9dc.toInt(),
        0x76f988da.toInt(),
        0x983e5152.toInt(),
        0xa831c66d.toInt(),
        0xb00327c8.toInt(),
        0xbf597fc7.toInt(),
        0xc6e00bf3.toInt(),
        0xd5a79147.toInt(),
        0x06ca6351.toInt(),
        0x14292967.toInt(),
        0x27b70a85.toInt(),
        0x2e1b2138.toInt(),
        0x4d2c6dfc.toInt(),
        0x53380d13.toInt(),
        0x650a7354.toInt(),
        0x766a0abb.toInt(),
        0x81c2c92e.toInt(),
        0x92722c85.toInt(),
        0xa2bfe8a1.toInt(),
        0xa81a664b.toInt(),
        0xc24b8b70.toInt(),
        0xc76c51a3.toInt(),
        0xd192e819.toInt(),
        0xd6990624.toInt(),
        0xf40e3585.toInt(),
        0x106aa070.toInt(),
        0x19a4c116.toInt(),
        0x1e376c08.toInt(),
        0x2748774c.toInt(),
        0x34b0bcb5.toInt(),
        0x391c0cb3.toInt(),
        0x4ed8aa4a.toInt(),
        0x5b9cca4f.toInt(),
        0x682e6ff3.toInt(),
        0x748f82ee.toInt(),
        0x78a5636f.toInt(),
        0x84c87814.toInt(),
        0x8cc70208.toInt(),
        0x90befffa.toInt(),
        0xa4506ceb.toInt(),
        0xbef9a3f7.toInt(),
        0xc67178f2.toInt(),
    )

fun expandedWords(block: List<Int>): IntArray {
  val w = IntArray(64)
  for (i in 0..15) for (j in 0..3) w[i] = w[i] or (block[4 * i + j] shl (24 - 8 * j))
  for (i in 16..63) {
    val p = w[i - 15]
    val q = w[i - 2]
    w[i] =
        w[i - 16] +
            (rotateRight(p, 7) xor rotateRight(p, 18) xor (p ushr 3)) +
            w[i - 7] +
            (rotateRight(q, 17) xor rotateRight(q, 19) xor (q ushr 10))
  }
  return w
}

fun compressedWords(state: IntArray, block: List<Int>): IntArray {
  val w = expandedWords(block)
  var a = state[0]
  var b = state[1]
  var c = state[2]
  var d = state[3]
  var e = state[4]
  var f = state[5]
  var g = state[6]
  var h = state[7]
  for (t in 0..63) {
    val first =
        h +
            (rotateRight(e, 6) xor rotateRight(e, 11) xor rotateRight(e, 25)) +
            ((e and f) xor (e.inv() and g)) +
            shaConstants[t] +
            w[t]
    val second =
        (rotateRight(a, 2) xor rotateRight(a, 13) xor rotateRight(a, 22)) +
            ((a and b) xor (a and c) xor (b and c))
    h = g
    g = f
    f = e
    e = d + first
    d = c
    c = b
    b = a
    a = first + second
  }
  return intArrayOf(
      a + state[0],
      b + state[1],
      c + state[2],
      d + state[3],
      e + state[4],
      f + state[5],
      g + state[6],
      h + state[7],
  )
}

fun referenceDigest(bytes: List<Int>) =
    MessageDigest.getInstance("SHA-256").digest(bytes.map { it.toByte() }.toByteArray()).map {
      it.toInt() and 255
    }

fun finalFromContext(initial: IntArray, total: Long, tail: List<Int>): List<Int> {
  val blocks = tail.toMutableList()
  blocks.add(128)
  while (blocks.size % 64 != 56) blocks.add(0)
  val bits = total shl 3
  for (shift in 56 downTo 0 step 8) blocks.add(((bits ushr shift) and 255).toInt())
  var state = initial.copyOf()
  for (block in blocks.chunked(64)) state = compressedWords(state, block)
  return state.flatMap { word -> (3 downTo 0).map { (word ushr (8 * it)) and 255 } }
}

fun contained(address: Long, n: Int, base: Long, size: Int) =
    address >= base && n.toLong() <= size && address - base <= size.toLong() - n

fun AssemblyCase.writeOctets(label: String, bytes: List<Int>) {
  require(bytes.size % 4 == 0)
  for (i in bytes.indices step 4) setWord(
      label,
      (0..3).fold(0) { v, j -> v or (bytes[i + j] shl (8 * j)) },
      i,
  )
}

val arenas =
    listOf(
        "data" to 4160,
        "state" to 96,
        "schedule" to 320,
        "context" to 832,
        "digest" to 96,
        "path" to 128,
        "scratch" to 320,
    )

fun AssemblyCase.beginCase(poison: Int = 165): List<Long> {
  for ((label, n) in arenas) writeOctets(label, List(n) { poison })
  setWord("status", -1)
  val saved = List(9) { 0x456789ab00000000L + it * 509 }
  for (i in 0..7) setRegister("s$i", saved[i])
  setRegister("fp", saved[8])
  setRegister("sp", 0x7fff0000L)
  for (i in 0..9) setRegister("t$i", -999L - i)
  return saved
}

fun AssemblyCase.checkSaved(saved: List<Long>) {
  for (i in 0..7) expectRegister("s$i", saved[i])
  expectRegister("fp", saved[8])
  expectRegister("sp", 0x7fff0000L)
}

fun AssemblyCase.guarded(
    readable: List<Pair<Long, Int>>,
    writable: List<Pair<Long, Int>>,
    action: () -> Unit,
) {
  val base = readDataAddress("data")
  val n = (readDataAddress("status") + 4 - base).toInt()
  val before = readBytes("data", n)
  val owned =
      (arenas +
              listOf(
                  "data_end" to 4,
                  "length" to 4,
                  "mode" to 4,
                  "buffer_capacity" to 4,
                  "status" to 4,
              ))
          .map { (label, size) -> readDataAddress(label) to size }
  val history = traceMemoryAccesses(base, n, maxAccesses = 200000)
  action()
  history.close()
  for (access in history.snapshot()) {
    val known = owned.any { (start, size) ->
      access.address < start + size && access.address + access.length > start
    }
    expectCondition(
        if (access.write)
            writable.any { (start, size) -> contained(access.address, access.length, start, size) }
        else
            !known ||
                readable.any { (start, size) ->
                  contained(access.address, access.length, start, size)
                },
        "Use only selected caller slices; submission-owned read-only constants and private stack are allowed",
    )
  }
  val after = readBytes("data", n)
  for (i in before.indices) if (
      writable.none { (start, size) -> contained(base + i, 1, start, size) }
  )
      expectCondition(after[i] == before[i], "Other caller bytes remain unchanged")
}

fun AssemblyCase.invoke(entry: String, saved: List<Long>) {
  callFunction(entry, "function_return")
  expectRegister("v0", 0)
  checkSaved(saved)
}

fun AssemblyCase.done() {
  runToExit()
  expectOutput("")
}

fun AssemblyCase.checkSchedule(block: List<Int>, offset: Int = 3) {
  val saved = beginCase()
  val out = 4 * ((offset + 3) % 16)
  writeOctets("data", List(4160) { if (it - offset in block.indices) block[it - offset] else 165 })
  setRegisterToDataAddress("a0", "data", offset.toLong())
  setRegisterToDataAddress("a1", "schedule", out.toLong())
  val input = readDataAddress("data", offset.toLong())
  val destination = readDataAddress("schedule", out.toLong())
  guarded(listOf(input to 64, destination to 256), listOf(destination to 256)) {
    invoke("sha256_schedule", saved)
  }
  for ((i, word) in expandedWords(block).withIndex()) expectWord("schedule", word, out + 4 * i)
  done()
}

fun AssemblyCase.checkCompression(block: List<Int>, initial: IntArray = shaIv, offset: Int = 3) {
  val saved = beginCase()
  val so = 4 * ((offset + 1) % 16)
  val wo = 4 * ((offset + 5) % 16)
  writeOctets("data", List(4160) { if (it - offset in block.indices) block[it - offset] else 165 })
  for (i in 0..7) setWord("state", initial[i], so + 4 * i)
  setRegisterToDataAddress("a0", "state", so.toLong())
  setRegisterToDataAddress("a1", "data", offset.toLong())
  setRegisterToDataAddress("a2", "schedule", wo.toLong())
  val state = readDataAddress("state", so.toLong())
  val input = readDataAddress("data", offset.toLong())
  val workspace = readDataAddress("schedule", wo.toLong())
  guarded(
      listOf(state to 32, input to 64, workspace to 256),
      listOf(state to 32, workspace to 256),
  ) {
    invoke("sha256_compress", saved)
  }
  for ((i, word) in compressedWords(initial, block).withIndex()) expectWord(
      "state",
      word,
      so + 4 * i,
  )
  done()
}

fun AssemblyCase.initializeContext(saved: List<Long>, co: Int) {
  val context = readDataAddress("context", co.toLong())
  setRegisterToDataAddress("a0", "context", co.toLong())
  guarded(listOf(context to 368), listOf(context to 368)) { invoke("sha256_init", saved) }
  for (i in 0..7) expectWord("context", shaIv[i], co + 4 * i)
  expectWord("context", 0, co + 32)
  expectWord("context", 0, co + 36)
  expectWord("context", 0, co + 40)
}

fun AssemblyCase.updateContext(saved: List<Long>, co: Int, sourceOffset: Int, count: Int) {
  val context = readDataAddress("context", co.toLong())
  val input = readDataAddress("data", sourceOffset.toLong())
  setRegisterToDataAddress("a0", "context", co.toLong())
  setRegisterToDataAddress("a1", "data", sourceOffset.toLong())
  setRegister("a2", count)
  guarded(listOf(context to 368, input to count), listOf(context to 368)) {
    invoke("sha256_update", saved)
  }
}

fun AssemblyCase.assertContext(co: Int, state: IntArray, total: Long, tail: List<Int>) {
  for (i in 0..7) expectWord("context", state[i], co + 4 * i)
  expectWord("context", total.toInt(), co + 32)
  expectWord("context", (total ushr 32).toInt(), co + 36)
  expectWord("context", tail.size, co + 40)
  expectCondition(
      readBytes("context", tail.size, co + 48).toList() == tail,
      "Retain the incomplete block prefix; unused context workspace is unspecified",
  )
}

fun AssemblyCase.finalizeContext(saved: List<Long>, co: Int, out: Int, expected: List<Int>) {
  val context = readDataAddress("context", co.toLong())
  val destination = readDataAddress("digest", out.toLong())
  setRegisterToDataAddress("a0", "context", co.toLong())
  setRegisterToDataAddress("a1", "digest", out.toLong())
  guarded(listOf(context to 368), listOf(context to 368, destination to 32)) {
    invoke("sha256_final", saved)
  }
  expectCondition(
      readBytes("digest", 32, out).toList() == expected,
      "Publish exactly the expected big-endian digest bytes",
  )
}

fun AssemblyCase.checkInitialization(co: Int, poison: Int = 165) {
  val saved = beginCase(poison)
  initializeContext(saved, co)
  done()
}

fun AssemblyCase.checkStream(
    bytes: List<Int>,
    chunks: List<Int>,
    offset: Int = 3,
    caller: Boolean = false,
) {
  val saved = beginCase()
  val co = 8 * (offset % 8)
  val out = 1 + (offset * 3) % 32
  val sourceOffset = if (caller) 0 else offset
  writeOctets(
      "data",
      List(4160) { if (it - sourceOffset in bytes.indices) bytes[it - sourceOffset] else 165 },
  )
  if (caller) {
    setWord("mode", 0)
    setWord("length", bytes.size)
    startAt("main")
    runUntil("memory_call")
    val context = readDataAddress("context")
    val input = readDataAddress("data")
    val output = readDataAddress("digest")
    guarded(listOf(context to 368, input to bytes.size), listOf(context to 368, output to 32)) {
      runUntil("main_done")
      expectRegister("v0", 0)
      checkSaved(saved)
    }
    expectWord("status", -1)
    expectCondition(
        readBytes("digest", 32).toList() == referenceDigest(bytes),
        "Actual memory caller produces the complete digest",
    )
    runUntil("results_ready")
    expectWord("status", 0)
  } else {
    initializeContext(saved, co)
    var consumed = 0
    for (count in chunks) {
      updateContext(saved, co, offset + consumed, count)
      consumed += count
      var state = shaIv.copyOf()
      for (block in bytes.take(consumed / 64 * 64).chunked(64)) state =
          compressedWords(state, block)
      assertContext(co, state, consumed.toLong(), bytes.subList(consumed / 64 * 64, consumed))
    }
    expectCondition(consumed == bytes.size, "Fixture consumes its exact byte interval")
    finalizeContext(saved, co, out, referenceDigest(bytes))
  }
  done()
}

fun AssemblyCase.installContext(co: Int, state: IntArray, total: Long, tail: List<Int>) {
  for (i in 0..7) setWord("context", state[i], co + 4 * i)
  setWord("context", total.toInt(), co + 32)
  setWord("context", (total ushr 32).toInt(), co + 36)
  setWord("context", tail.size, co + 40)
  val bytes = readBytes("context", 832).toMutableList()
  for (i in tail.indices) bytes[co + 48 + i] = tail[i]
  writeOctets("context", bytes)
}

fun AssemblyCase.checkSyntheticFinal(
    total: Long,
    state: IntArray,
    tail: List<Int>,
    offset: Int = 1,
) {
  val saved = beginCase()
  val co = 8 * (offset % 8)
  val out = 1 + (offset % 32)
  installContext(co, state, total, tail)
  finalizeContext(saved, co, out, finalFromContext(state, total, tail))
  done()
}

fun AssemblyCase.checkSyntheticUpdate(
    total: Long,
    state: IntArray,
    tail: List<Int>,
    input: List<Int>,
    offset: Int = 3,
) {
  val saved = beginCase()
  val co = 8 * (offset % 8)
  installContext(co, state, total, tail)
  writeOctets("data", List(4160) { if (it - offset in input.indices) input[it - offset] else 165 })
  updateContext(saved, co, offset, input.size)
  val combined = tail + input
  var expected = state.copyOf()
  for (block in combined.take(combined.size / 64 * 64).chunked(64)) expected =
      compressedWords(expected, block)
  assertContext(co, expected, total + input.size, combined.takeLast(combined.size % 64))
  done()
}

fun AssemblyCase.checkInterleaved(seed: Int) {
  val saved = beginCase()
  val a = List(129 + seed) { (it * 37 + seed) % 256 }
  val b = List(65 + seed) { (it * 73 + 128) % 256 }
  writeOctets(
      "data",
      List(4160) {
        if (it in a.indices) a[it] else if (it - 512 in b.indices) b[it - 512] else 165
      },
  )
  initializeContext(saved, 8)
  initializeContext(saved, 408)
  updateContext(saved, 8, 0, 55)
  updateContext(saved, 408, 512, 64)
  updateContext(saved, 8, 55, a.size - 55)
  updateContext(saved, 408, 576, b.size - 64)
  finalizeContext(saved, 8, 1, referenceDigest(a))
  finalizeContext(saved, 408, 41, referenceDigest(b))
  initializeContext(saved, 8)
  updateContext(saved, 8, 0, 0)
  finalizeContext(saved, 8, 1, referenceDigest(emptyList()))
  done()
}

fun AssemblyCase.checkFile(
    bytes: List<Int>,
    capacity: Int,
    maximum: Int = Int.MAX_VALUE,
    failAt: Int? = null,
    missing: Boolean = false,
    caller: Boolean = false,
    firstDescriptor: Int = 3,
) {
  val saved = beginCase()
  val po = if (caller) 0 else 1 + (capacity % 16)
  val so = if (caller) 0 else capacity % 32
  val out = if (caller) 0 else 1 + capacity % 32
  val filename = "sha-$capacity-${bytes.size}.bin"
  writeOctets(
      "path",
      List(128) {
        if (it - po in filename.indices) filename[it - po].code
        else if (it == po + filename.length) 0 else 165
      },
  )
  val files =
      provideFiles(
          if (missing) emptyMap() else mapOf(filename to bytes.map { it.toByte() }.toByteArray()),
          maxReadSize = maximum,
          failAfterBytes = failAt,
          firstDescriptor = firstDescriptor,
      )
  val status = if (missing) 1 else if (failAt != null && failAt <= bytes.size) 2 else 0
  val path = readDataAddress("path", po.toLong())
  val scratch = readDataAddress("scratch", so.toLong())
  val output = readDataAddress("digest", out.toLong())
  if (caller) {
    setWord("mode", 1)
    setWord("buffer_capacity", capacity)
    startAt("main")
    runUntil("file_call")
  } else {
    setRegisterToDataAddress("a0", "path", po.toLong())
    setRegisterToDataAddress("a1", "scratch", so.toLong())
    setRegister("a2", capacity)
    setRegisterToDataAddress("a3", "digest", out.toLong())
  }
  guarded(
      listOf(path to filename.length + 1, scratch to capacity),
      listOf(scratch to capacity) + (if (status == 0) listOf(output to 32) else emptyList()),
  ) {
    if (caller) runUntil("main_done") else callFunction("sha256_file", "function_return")
    expectRegister("v0", status)
    checkSaved(saved)
  }
  files.expectAllClosed()
  val events = files.snapshot()
  val opens = events.filter { it.operation == Operation.OPEN }
  val reads = events.filter { it.operation == Operation.READ }
  val closes = events.filter { it.operation == Operation.CLOSE }
  expectCondition(
      opens.size == 1 && opens[0].path == filename && opens[0].flags == 0,
      "Open only the supplied path read-only",
  )
  if (missing)
      expectCondition(
          opens[0].result == -1 && reads.isEmpty() && closes.isEmpty(),
          "Unsuccessful open acquires no descriptor",
      )
  else {
    val fd = opens[0].descriptor
    expectCondition(
        fd >= 3 && closes.size == 1 && closes[0].descriptor == fd && closes[0].result == 0,
        "Close the actual acquired descriptor once",
    )
    expectCondition(
        events.first().operation == Operation.OPEN &&
            events.last().operation == Operation.CLOSE &&
            events.drop(1).dropLast(1).all { it.operation == Operation.READ },
        "Read only between open and close",
    )
    expectCondition(
        reads.isNotEmpty() && reads.last().result == (if (status == 0) 0 else -1),
        "Distinguish EOF from a read error",
    )
    expectCondition(
        reads.all {
          it.descriptor == fd &&
              it.requestedBytes in 1..capacity &&
              it.result <= minOf(it.requestedBytes, maximum)
        },
        "Equivalent positive read requests up to capacity are accepted",
    )
    expectCondition(
        reads.dropLast(1).all { it.result > 0 } &&
            reads.sumOf { maxOf(0, it.result) } == (if (status == 0) bytes.size else failAt!!),
        "Consume the successful prefix before the terminal read result",
    )
  }
  expectCondition(
      readBytes("digest", 32, out).toList() ==
          (if (status == 0) referenceDigest(bytes) else List(32) { 165 }),
      "Publish the complete digest only on success; errors preserve output",
  )
  if (caller) {
    expectWord("status", -1)
    runUntil("results_ready")
    expectWord("status", status)
  }
  done()
}

// Reuse one session and vary the fixture descriptor without assuming its numeric identity.
fun AssemblyCase.checkRepeatedFiles(firstDescriptor: Int, failure: Int?) {
  val saved = beginCase()
  val first = List(if (failure == null) 65 else 1) { (it * 37 + 11) % 256 }
  val second = List(129) { (it * 73 + 128) % 256 }
  val contents =
      mapOf(
          "repeat-one.bin" to first.map { it.toByte() }.toByteArray(),
          "repeat-two.bin" to second.map { it.toByte() }.toByteArray(),
      )
  val files =
      provideFiles(
          contents,
          maxReadSize = 2,
          failAfterBytes = failure,
          firstDescriptor = firstDescriptor,
      )
  for ((index, filename) in
      listOf("repeat-one.bin", "repeat-two.bin", "repeat-one.bin", "missing.bin").withIndex()) {
    val bytes = if (index == 1) second else first
    val missing = index == 3
    val status = if (missing) 1 else if (failure != null && failure <= bytes.size) 2 else 0
    val out = if (index < 2) 1 else 41
    val cap = listOf(7, 64, 3, 1)[index]
    val beforeOutput = readBytes("digest", 32, out).toList()
    val eventStart = files.snapshot().size
    writeOctets(
        "path",
        List(128) {
          if (it - 5 in filename.indices) filename[it - 5].code
          else if (it == filename.length + 5) 0 else 165
        },
    )
    setRegisterToDataAddress("a0", "path", 5)
    setRegisterToDataAddress("a1", "scratch", 5)
    setRegister("a2", cap)
    setRegisterToDataAddress("a3", "digest", out.toLong())
    val path = readDataAddress("path", 5)
    val scratch = readDataAddress("scratch", 5)
    val output = readDataAddress("digest", out.toLong())
    guarded(
        listOf(path to filename.length + 1, scratch to cap),
        listOf(scratch to cap) + (if (status == 0) listOf(output to 32) else emptyList()),
    ) {
      callFunction("sha256_file", "function_return")
      expectRegister("v0", status)
      checkSaved(saved)
    }
    files.expectAllClosed()
    val events = files.snapshot().drop(eventStart)
    val opens = events.filter { it.operation == Operation.OPEN }
    val reads = events.filter { it.operation == Operation.READ }
    val closes = events.filter { it.operation == Operation.CLOSE }
    expectCondition(
        opens.size == 1 && opens[0].path == filename && opens[0].flags == 0,
        "Each file call opens its own supplied path",
    )
    if (missing)
        expectCondition(
            opens[0].result == -1 && reads.isEmpty() && closes.isEmpty(),
            "Missing file acquires no descriptor on a reused session",
        )
    else {
      val fd = opens[0].descriptor
      expectCondition(
          fd >= 3 && closes.size == 1 && closes[0].descriptor == fd && closes[0].result == 0,
          "Close each actual descriptor before the next call",
      )
      expectCondition(
          events.first().operation == Operation.OPEN &&
              events.last().operation == Operation.CLOSE &&
              events.drop(1).dropLast(1).all { it.operation == Operation.READ },
          "Each call reads between its own open and close",
      )
      expectCondition(
          reads.isNotEmpty() &&
              reads.last().result == (if (status == 0) 0 else -1) &&
              reads.dropLast(1).all { it.result > 0 },
          "Each call reaches its own EOF or error",
      )
      expectCondition(
          reads.all {
            it.descriptor == fd &&
                it.requestedBytes in 1..cap &&
                it.result <= minOf(2, it.requestedBytes)
          } && reads.sumOf { maxOf(0, it.result) } == (if (status == 0) bytes.size else failure!!),
          "Read exactly this call's successful file prefix",
      )
    }
    expectCondition(
        readBytes("digest", 32, out).toList() ==
            (if (status == 0) referenceDigest(bytes) else beforeOutput),
        "Calls start fresh; errors preserve the existing successful digest",
    )
  }
  done()
}

val filePattern = { n: Int -> List(n) { (it * 73 + it / 3 + 128) % 256 } }

for (cap in listOf(1, 7, 64, 256)) for (n in listOf(0, 3, 56, 65, 129)) case(
    "file capacity $cap length $n",
    instructionLimit = 3000000,
) {
  checkFile(filePattern(n), cap)
}

for (cap in listOf(7, 64)) for (short in listOf(1, 3)) for (n in listOf(63, 120)) case(
    "short file $cap $short $n",
    instructionLimit = 3000000,
) {
  checkFile(filePattern(n), cap, short)
}

for (cap in listOf(1, 7, 256)) for (failure in listOf(0, 55, 65)) case(
    "read error $cap at $failure",
    instructionLimit = 3000000,
) {
  checkFile(filePattern(65), cap, 3, failure)
}

for (cap in listOf(1, 7, 256)) case("missing file $cap") {
  checkFile(emptyList(), cap, missing = true)
}
