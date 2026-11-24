import edu.missouristate.mars.cli.grading.AssemblyCase
import java.math.BigInteger

val zero = BigInteger.ZERO
val one = BigInteger.ONE
val max128 = one.shiftLeft(128).subtract(one)
val max64 = one.shiftLeft(64).subtract(one)
val max32 = one.shiftLeft(32).subtract(one)
val entries = listOf("gcd64", "pow32", "mul128", "addmod128", "reduce128", "mulmod128", "pow128")

fun unsigned64(n: Long) = BigInteger(n.toULong().toString())

fun bytes(n: BigInteger, count: Int) = IntArray(count) { n.shiftRight(it * 8).toInt() and 255 }

fun fits(a: Long, n: Int, b: Long, size: Int) = a >= b && n <= size && a - b <= size.toLong() - n

fun AssemblyCase.put(label: String, n: BigInteger, offset: Int = 0, count: Int = 16) {
  require(n.signum() >= 0 && n.bitLength() <= count * 8 && offset % 4 == 0 && count % 4 == 0)
  val octets = bytes(n, count)
  for (i in octets.indices step 4) setWord(
      label,
      (0..3).fold(0) { v, j -> v or (octets[i + j] shl (8 * j)) },
      offset + i,
  )
}

fun AssemblyCase.initialize(poison: Boolean = false): List<Long> {
  for ((name, size) in
      listOf(
          "left" to 128,
          "right" to 128,
          "modulus" to 128,
          "exponent" to 128,
          "output" to 256,
          "workspace" to 1024,
      )) for (i in 0 until size step 4) setWord(name, 0xa5a5a5a5.toInt(), i)
  setWord("mode", 0)
  setWord("status", -1)
  val saved = List(9) { 0x3141592600000000L + 977 * it }
  for (i in 0..7) setRegister("s$i", saved[i])
  setRegister("fp", saved[8])
  val top = if (poison) readDataAddress("workspace", 1024) else 0x7fff0000L
  setRegister("sp", top)
  for (i in 0..9) setRegister("t$i", -999L - i)
  return saved + top
}

fun AssemblyCase.preserved(saved: List<Long>) {
  for (i in 0..7) expectRegister("s$i", saved[i])
  expectRegister("fp", saved[8])
  expectRegister("sp", saved[9])
  expectOutput("")
}

fun AssemblyCase.callerObjects() =
    listOf(
            "left" to 128,
            "right" to 128,
            "modulus" to 128,
            "exponent" to 128,
            "output" to 256,
            "mode" to 4,
            "status" to 4,
        )
        .map { (label, size) -> Triple(label, readDataAddress(label), size) }

fun AssemblyCase.observe(
    reads: List<Pair<Long, Int>>,
    writes: List<Pair<Long, Int>>,
    action: () -> Unit,
) {
  val objects = callerObjects()
  val old = objects.map { (name, _, size) -> readBytes(name, size) }
  val lo = objects.minOf { it.second }
  val hi = objects.maxOf { it.second + it.third }
  val trace = traceMemoryAccesses(lo, (hi - lo).toInt(), maxAccesses = 2000000)
  action()
  trace.close()
  // Candidate-owned gaps, constants and private workspace are independent of caller-owned objects.
  for (e in trace.snapshot()) if (
      objects.any { (_, base, size) -> e.address < base + size && base < e.address + e.length }
  )
      expectCondition(
          (if (e.write) writes else reads).any { (base, size) ->
            fits(e.address, e.length, base, size)
          },
          "Caller object access stays within selected intervals",
      )
  for ((i, obj) in objects.withIndex()) {
    val (name, base, size) = obj
    val after = readBytes(name, size)
    for (j in after.indices) if (writes.none { (b, n) -> fits(base + j, 1, b, n) })
        expectCondition(
            old[i][j] == after[j],
            "Caller bytes outside selected output remain unchanged",
        )
  }
}

fun AssemblyCase.expected(label: String, n: BigInteger, count: Int = 16, offset: Int = 0) {
  expectCondition(
      readBytes(label, count, offset).contentEquals(bytes(n, count)),
      "Every little-endian result byte agrees with an independent integer calculation",
  )
}

fun AssemblyCase.runArithmetic(
    target: String,
    a: BigInteger,
    b: BigInteger = zero,
    n: BigInteger = one,
    index: Int = 0,
    alias: Int = 0,
    caller: Boolean = false,
    reset: Boolean = true,
    sharedInputs: Boolean = false,
) {
  require(target in entries && alias in 0..3)
  val saved =
      if (reset) initialize(index % 2 == 1 || caller)
      else {
        val state = List(9) { 0x3141592600000000L + 977 * it }
        for (i in 0..7) setRegister("s$i", state[i])
        setRegister("fp", state[8])
        val top = readDataAddress("workspace", 1024)
        setRegister("sp", top)
        state + top
      }
  val ao = if (caller) 0 else 4 * (index % 17)
  val bo = if (caller) 0 else 4 * ((index + 3) % 17)
  val no = if (caller) 0 else 4 * ((index + 6) % 17)
  val oo = if (caller) 0 else 4 * ((index + 9) % 25)
  put("left", a, ao)
  put(if (target == "pow128") "exponent" else "right", b, bo)
  put("modulus", n, no)
  val second =
      if (sharedInputs || alias == 3) "left" else if (target == "pow128") "exponent" else "right"
  val secondOffset = if (sharedInputs || alias == 3) ao else bo
  val destination = if (alias == 1 || alias == 3) "left" else if (alias == 2) "right" else "output"
  val destinationOffset = if (alias == 1 || alias == 3) ao else if (alias == 2) bo else oo
  val ap = readDataAddress("left", ao.toLong())
  val bp = readDataAddress(second, secondOffset.toLong())
  val np = readDataAddress("modulus", no.toLong())
  val out = readDataAddress(destination, destinationOffset.toLong())
  if (target == "gcd64" || target == "pow32") {
    setRegister("a0", a.toLong())
    setRegister("a1", b.toLong())
    if (target == "pow32") {
      setRegister("a2", n.toLong())
      setRegisterToDataAddress("a3", destination, destinationOffset.toLong())
    }
  } else {
    setRegisterToDataAddress("a0", "left", ao.toLong())
    setRegisterToDataAddress(
        "a1",
        if (target == "reduce128") "modulus" else second,
        (if (target == "reduce128") no else secondOffset).toLong(),
    )
    setRegisterToDataAddress(
        "a2",
        if (target in listOf("mul128", "reduce128")) destination else "modulus",
        (if (target in listOf("mul128", "reduce128")) destinationOffset else no).toLong(),
    )
    if (target !in listOf("mul128", "reduce128"))
        setRegisterToDataAddress("a3", destination, destinationOffset.toLong())
  }
  if (caller) {
    setWord("mode", entries.indexOf(target))
    startAt("main")
    runUntil(target + "_call")
  }
  val valid = target in listOf("gcd64", "mul128") || n != zero
  val read =
      when (target) {
        "gcd64",
        "pow32" -> emptyList()
        "mul128" -> listOf(ap to 16, bp to 16)
        "reduce128" -> if (valid) listOf(ap to 16, np to 16) else listOf(np to 16)
        else -> if (valid) listOf(ap to 16, bp to 16, np to 16) else listOf(np to 16)
      }
  val count = if (target == "pow32") 4 else if (target == "mul128") 32 else 16
  val write = if (valid && target != "gcd64") listOf(out to count) else emptyList()
  observe(read + write, write) {
    if (caller) runUntil(target + "_return") else callFunction("rsa_" + target, "function_return")
    if (target == "gcd64") {
      expectRegister("v0", a.gcd(b).toLong())
      expectRegister("v1", 0)
    } else expectRegister("v0", if (valid) 0 else 1)
    preserved(saved)
  }
  val actualB = if (sharedInputs) a else b
  if (valid && target != "gcd64")
      expected(
          destination,
          when (target) {
            "pow32",
            "pow128" -> a.modPow(actualB, n)
            "mul128" -> a.multiply(actualB)
            "addmod128" -> a.add(if (alias == 3) a else actualB).mod(n)
            "reduce128" -> a.mod(n)
            else -> a.multiply(actualB).mod(n)
          },
          count,
          destinationOffset,
      )
  if (caller) {
    runToExit()
    expectWord("status", if (valid) 0 else 1)
    expectOutput("")
  }
}

fun AssemblyCase.invalidModulus(target: String) {
  val saved = initialize(true)
  put("modulus", zero)
  for (i in 0..3) setRegister("a$i", 1)
  val reads =
      if (target == "pow32") {
        setRegister("a2", 0)
        emptyList()
      } else {
        setRegisterToDataAddress(if (target == "reduce128") "a1" else "a2", "modulus")
        listOf(readDataAddress("modulus") to 16)
      }
  observe(reads, emptyList()) {
    callFunction("rsa_" + target, "function_return")
    expectRegister("v0", 1)
    preserved(saved)
  }
}

val anchors = listOf(zero, one, max32, one.shiftLeft(64), one.shiftLeft(127), max128)

for ((i, a) in anchors.withIndex()) for (b in listOf(one, max128)) case(
    "public full product $i $b",
) {
  runArithmetic("mul128", a, b, index = i)
}

case("shared full product input") {
  runArithmetic("mul128", max128, max128, index = 5, sharedInputs = true)
}
