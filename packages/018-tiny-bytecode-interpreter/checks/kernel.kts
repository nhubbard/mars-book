import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class GuestInstruction(val offset: Int, val opcode: Int, val width: Int, val operand: Int)

data class Outcome(
    val status: Int,
    val pc: Int,
    val retired: Int,
    val fault: Int,
    val stack: List<Int>,
)

fun simulate(program: ByteArray, capacity: Int, budget: Int): Outcome {
  val decoded = mutableListOf<GuestInstruction>()
  var cursor = 0
  while (cursor < program.size) {
    val opcode = program[cursor].toInt() and 255
    val width =
        when (opcode) {
          0,
          16,
          17,
          18,
          19,
          32,
          33,
          34,
          35 -> 1
          1,
          48,
          49 -> 2
          2 -> 5
          else -> return Outcome(-1, 0, 0, cursor, emptyList())
        }
    if (cursor + width > program.size) return Outcome(-2, 0, 0, cursor, emptyList())
    val operand =
        when (opcode) {
          1 -> program[cursor + 1].toInt()
          2 -> ByteBuffer.wrap(program, cursor + 1, 4).order(ByteOrder.LITTLE_ENDIAN).int
          48,
          49 -> program[cursor + 1].toInt() and 255
          else -> 0
        }
    decoded += GuestInstruction(cursor, opcode, width, operand)
    cursor += width
  }
  val byOffset = decoded.associateBy { it.offset }
  for (i in decoded) if (i.opcode in listOf(48, 49) && i.operand !in byOffset)
      return Outcome(-3, 0, 0, i.offset, emptyList())
  val stack = mutableListOf<Int>()
  var pc = 0
  var retired = 0
  fun fault(status: Int) = Outcome(status, pc, retired, pc, stack.toList())
  while (retired < budget) {
    val i = byOffset[pc] ?: return fault(-4)
    var next = pc + i.width
    when (i.opcode) {
      0 -> return Outcome(1, next, retired + 1, -1, stack.toList())
      1,
      2 -> {
        if (stack.size == capacity) return fault(-6)
        stack += i.operand
      }
      16,
      17,
      18,
      19 -> {
        if (stack.size < 2) return fault(-5)
        val left = BigInteger.valueOf(stack[stack.lastIndex - 1].toLong())
        val right = BigInteger.valueOf(stack.last().toLong())
        if (i.opcode == 19 && right == BigInteger.ZERO) return fault(-8)
        val value =
            when (i.opcode) {
              16 -> left + right
              17 -> left - right
              18 -> left * right
              else -> left / right
            }
        if (
            value < BigInteger.valueOf(Int.MIN_VALUE.toLong()) ||
                value > BigInteger.valueOf(Int.MAX_VALUE.toLong())
        )
            return fault(-7)
        stack.removeAt(stack.lastIndex)
        stack[stack.lastIndex] = value.toInt()
      }
      32,
      35 -> {
        val required = if (i.opcode == 32) 1 else 2
        if (stack.size < required) return fault(-5)
        if (stack.size == capacity) return fault(-6)
        val value = stack[stack.size - required]
        stack += value
      }
      33 -> {
        if (stack.isEmpty()) return fault(-5)
        stack.removeAt(stack.lastIndex)
      }
      34 -> {
        if (stack.size < 2) return fault(-5)
        val a = stack[stack.lastIndex]
        stack[stack.lastIndex] = stack[stack.lastIndex - 1]
        stack[stack.lastIndex - 1] = a
      }
      48 -> next = i.operand
      49 -> {
        if (stack.isEmpty()) return fault(-5)
        val value = stack.removeAt(stack.lastIndex)
        if (value == 0) next = i.operand
      }
    }
    pc = next
    retired++
  }
  return Outcome(0, pc, retired, -1, stack.toList())
}

fun bytes(vararg values: Int) = values.map { it.toByte() }.toByteArray()

fun push(value: Int): ByteArray =
    bytes(2) + ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()

data class Trial(val image: ByteArray, val capacity: Int = 8, val fuel: Int = 64)

val countdown = bytes(1, 0, 1, 3, 32, 49, 16, 34, 35, 16, 34, 1, 1, 17, 48, 4, 33, 0)
val trials = mutableListOf<Trial>()

fun sample(image: ByteArray, capacity: Int = 8, fuel: Int = 64) {
  require(image.size <= 64)
  trials += Trial(image, capacity, fuel)
}

val known =
    listOf(
        Trial(countdown) to Outcome(1, 18, 33, -1, listOf(6)),
        Trial(countdown, 8, 4) to Outcome(0, 7, 4, -1, listOf(0, 3)),
        Trial(countdown, 2) to Outcome(-6, 4, 2, 4, listOf(0, 3)),
        Trial(bytes(1, 0, 1, 3, 32, 49, 6, 34, 35, 16, 34, 1, 1, 17, 48, 4, 33, 0)) to
            Outcome(-3, 0, 0, 5, emptyList()),
        Trial(bytes(0)) to Outcome(1, 1, 1, -1, emptyList()),
        Trial(bytes(0), 0, 0) to Outcome(0, 0, 0, -1, emptyList()),
        Trial(bytes()) to Outcome(-4, 0, 0, 0, emptyList()),
        Trial(bytes(), 0, 0) to Outcome(0, 0, 0, -1, emptyList()),
        Trial(bytes(48, 0), 0, 64) to Outcome(0, 0, 64, -1, emptyList()),
        Trial(bytes(0, 255), 0, 0) to Outcome(-1, 0, 0, 1, emptyList()),
        Trial(bytes(48, 1, 255)) to Outcome(-1, 0, 0, 2, emptyList()),
        Trial(bytes(1, 1, 49, 3, 0)) to Outcome(-3, 0, 0, 2, emptyList()),
        Trial(push(Int.MAX_VALUE) + bytes(1, 1, 16, 0)) to
            Outcome(-7, 7, 2, 7, listOf(Int.MAX_VALUE, 1)),
        Trial(bytes(1, 7, 1, 0, 19, 0)) to Outcome(-8, 4, 2, 4, listOf(7, 0)),
        Trial(bytes(1, 249, 1, 3, 19, 0)) to Outcome(1, 6, 4, -1, listOf(-2)),
    )

for ((t, expected) in known) {
  check(simulate(t.image, t.capacity, t.fuel) == expected) {
    "Bad manual oracle fixture: $t ${simulate(t.image,t.capacity,t.fuel)} expected $expected"
  }
  trials += t
}

for (op in 0..255) {
  sample(bytes(op), 0, 0)
  sample(bytes(op), 8, 1)
}

for (capacity in 0..8) for (fuel in listOf(0, 1, 2, 3, 4, 5, 8, 16, 32, 33, 34, 64)) sample(
    countdown,
    capacity,
    fuel,
)

for (target in 0..9) {
  sample(bytes(48, target, 2, 255, 255, 255, 127, 0))
  sample(bytes(1, 1, 49, target, 0, 2, 0, 0, 0, 128, 0))
}

for (target in listOf(0, 2, 3, 62, 63, 64, 128, 255)) sample(
    bytes(48, target) + ByteArray(62),
    8,
    4,
)

for (cut in 1..4) sample(push(Int.MIN_VALUE).copyOf(cut))

val values = listOf(0, 1, -1, Int.MIN_VALUE, Int.MAX_VALUE, 46340, 46341, -46341)

for ((index, left) in values.withIndex()) for (right in values) for (op in 16..19) sample(
    push(left) + push(right) + bytes(op, 0),
    if (index % 2 == 0) 2 else 8,
)

for (depth in 0..8) for (capacity in 0..8) for (op in listOf(32, 33, 34, 35)) sample(
    bytes(*IntArray(depth * 2) { if (it % 2 == 0) 1 else it / 2 + 1 }) + bytes(op, 0),
    capacity,
)

for ((index, t) in trials.withIndex()) case(
    "vm $index n=${t.image.size} cap=${t.capacity} fuel=${t.fuel}",
    instructionLimit = 100000,
) {
  val expected = simulate(t.image, t.capacity, t.fuel)
  val code = if (index % 3 == 0) "code_b" else "code_a"
  val stack = if (index % 4 == 0) "stack_b" else "stack_a"
  val state = if (index % 2 == 0) "state_b" else "state_a"
  val codeOffset = index % 33
  val stackOffset = index % 9
  val data = ByteArray(96) { (it * 17 + index * 31 + 128).toByte() }
  t.image.copyInto(data, codeOffset)
  for (i in 0 until 24) {
    var word = 0
    for (j in 0..3) word = word or ((data[i * 4 + j].toInt() and 255) shl (8 * j))
    setWord(code, word, offset = i * 4)
  }
  for (i in 0 until 16) setWord(stack, 0x55667788 xor (index * 31 + i * 1009), offset = i * 4)
  for (offset in listOf(0, 4, 8, 12, 20)) setWord(
      state,
      0x23456789 xor (offset * 103 + index),
      offset = offset,
  )
  setWord(state, t.capacity, offset = 16)
  val codeBase = readDataAddress(code) + codeOffset
  val stackBase = readDataAddress(stack) + stackOffset * 4
  val stateBase = readDataAddress(state)
  setWord(state, stackBase.toInt(), offset = 24)
  setWord(state, (stackBase ushr 32).toInt(), offset = 28)
  val size = (readDataAddress("state_end") + 4 - readDataAddress("code_a_guard")).toInt()
  val before = readBytes("code_a_guard", size)
  val root = readDataAddress("code_a_guard")
  val writable =
      (0 until 16).map { stateBase + it }.toSet() +
          (20 until 24).map { stateBase + it } +
          (0 until t.capacity * 4).map { stackBase + it }
  val readable =
      (0 until t.image.size).map { codeBase + it }.toSet() +
          (0 until 32).map { stateBase + it } +
          (0 until t.capacity * 4).map { stackBase + it }
  val history = traceMemoryAccesses("code_a_guard", size, maxAccesses = 100000)
  for (i in 0..7) setRegister("s$i", 0x3456789000000000L + i * 1013)
  val callerSp = if (index % 2 == 0) 0x7ffe8000L else 0x7fff0000L
  setRegister("fp", 0x2345678900000011L)
  setRegister("sp", callerSp)
  for (i in 0..9) setRegister("t$i", 0x456789ab00000000L + index + i * 17)
  setRegisterToDataAddress("a0", code, codeOffset.toLong())
  setRegister("a1", t.image.size)
  setRegisterToDataAddress("a2", state)
  setRegister("a3", t.fuel)
  val avoided = traceExecution("function_return", "done")
  callFunction("run_vm", if (index % 2 == 0) "function_return" else "done")
  expectRegister("v0", expected.status)
  expectRegister("v1", expected.retired)
  expectWord(state, expected.pc)
  expectWord(state, expected.stack.size, offset = 4)
  expectWord(state, expected.retired, offset = 8)
  expectWord(state, expected.status, offset = 12)
  expectWord(state, expected.fault, offset = 20)
  for (i in expected.stack.indices) expectWord(
      stack,
      expected.stack[i],
      offset = (stackOffset + i) * 4,
  )
  expectCondition(avoided.snapshot().instructions == 0L, "Return through the supplied continuation")
  for (i in 0..7) expectRegister("s$i", 0x3456789000000000L + i * 1013)
  expectRegister("fp", 0x2345678900000011L)
  expectRegister("sp", callerSp)
  fun inspect() {
    val after = readBytes("code_a_guard", size)
    for (i in before.indices) if (root + i !in writable)
        expectCondition(
            after[i] == before[i],
            "Caller bytes outside output fields and stack capacity remain unchanged",
        )
    for (a in history.snapshot()) expectCondition(
        (0 until a.length).all { a.address + it in if (a.write) writable else readable },
        "Every access byte fits the selected image, descriptor and stack ownership",
    )
  }
  inspect()
  runToExit()
  inspect()
  expectOutput("")
}
