import edu.missouristate.mars.cli.grading.AssemblyCase
import edu.missouristate.mars.cli.grading.FileAccess.Operation

// Student feedback: a host model of the published subset, not a MIPS implementation.
data class WaveDescription(val hz: Long, val channels: Long, val frames: Long, val pcmAt: Int, val samples: ByteArray) {
    fun descriptor() = description(hz, channels, frames) + little(pcmAt.toLong(), 8) + little(samples.size.toLong(), 8)
}
fun little(value: Long, width: Int = 4) = ByteArray(width) { (value ushr (8 * it)).toByte() }
fun bytes(hex: String) = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
fun description(hz: Long, channels: Long, frames: Long) = little(hz) + little(channels) + little(frames, 8)
fun supported(hz: Long, channels: Long, frames: Long) = hz in 8000..48000 && channels in 1..2 && frames in 0..65536
fun field(raw: ByteArray, at: Int, width: Int): Long = (0 until width).fold(0L) { value, lane ->
    value or ((raw[at + lane].toLong() and 255) shl (8 * lane))
}
fun identify(raw: ByteArray, at: Int) = raw.copyOfRange(at, at + 4).toString(Charsets.ISO_8859_1)
fun readWave(raw: ByteArray): WaveDescription? = runCatching {
    require(raw.size in 44..266240)
    require(identify(raw, 0) == "RIFF" && identify(raw, 8) == "WAVE")
    require(field(raw, 4, 4) + 8 == raw.size.toLong())
    var cursor = 12L
    var format: Pair<Long, Long>? = null
    var pcmAt = -1
    var pcmLength = 0
    while (cursor < raw.size) {
        require(raw.size - cursor >= 8)
        val header = cursor.toInt()
        val payload = cursor + 8
        val length = field(raw, header + 4, 4)
        val next = payload + length + (length and 1)
        require(next <= raw.size)
        when (identify(raw, header)) {
            "fmt " -> {
                require(format == null && pcmAt < 0 && (length == 16L || length == 18L))
                val base = payload.toInt()
                val channels = field(raw, base + 2, 2)
                val hz = field(raw, base + 4, 4)
                require(field(raw, base, 2) == 1L && field(raw, base + 14, 2) == 16L)
                require(supported(hz, channels, 0))
                require(field(raw, base + 12, 2) == 2 * channels)
                require(field(raw, base + 8, 4) == 2 * channels * hz)
                if (length == 18L) require(field(raw, base + 16, 2) == 0L)
                format = hz to channels
            }
            "data" -> {
                val accepted = requireNotNull(format)
                require(pcmAt < 0 && length % (2 * accepted.second) == 0L)
                require(length / (2 * accepted.second) <= 65536)
                pcmAt = payload.toInt()
                pcmLength = length.toInt()
            }
        }
        cursor = next
    }
    val accepted = requireNotNull(format)
    require(pcmAt >= 0 && raw.size - pcmLength <= 4096)
    WaveDescription(accepted.first, accepted.second, pcmLength / (2 * accepted.second), pcmAt,
        raw.copyOfRange(pcmAt, pcmAt + pcmLength))
}.getOrNull()
fun writeWave(hz: Long, channels: Long, pcm: ByteArray): ByteArray =
    "RIFF".toByteArray() + little(36L + pcm.size) + "WAVEfmt ".toByteArray() + little(16) + little(1, 2) +
    little(channels, 2) + little(hz) + little(2 * channels * hz) + little(2 * channels, 2) + little(16, 2) +
    "data".toByteArray() + little(pcm.size.toLong()) + pcm
fun waveChunk(tag: String, payload: ByteArray) = tag.toByteArray() + little(payload.size.toLong()) + payload +
    if (payload.size % 2 == 0) byteArrayOf() else byteArrayOf(0x6d)
fun container(body: ByteArray) = "RIFF".toByteArray() + little(4L + body.size) + "WAVE".toByteArray() + body
fun patternedPcm(channels: Int, frames: Int) = ByteArray(2 * channels * frames) { at ->
    val sample = ((at / 2 * 2411 + 17003) % 65536) - 32768
    (sample shr (8 * (at % 2))).toByte()
}
fun sampleFile(hz: Long = 22050, channels: Int = 2, frames: Int = 9, extended: Boolean = false, extras: Boolean = true): ByteArray {
    val canonical = writeWave(hz, channels.toLong(), patternedPcm(channels, frames))
    val format = canonical.copyOfRange(20, 36) + if (extended) byteArrayOf(0, 0) else byteArrayOf()
    return container((if (extras) waveChunk("JUNK", bytes("35a47f")) else byteArrayOf()) +
        waveChunk("fmt ", format) + waveChunk("data", canonical.copyOfRange(44, canonical.size)) +
        if (extras) waveChunk("note", bytes("c2")) else byteArrayOf())
}
fun q15(pcm: ByteArray, channels: Int, coefficients: ByteArray): Pair<ByteArray, Long> {
    val frames = pcm.size / (2 * channels)
    val output = ByteArray(pcm.size)
    var clips = 0L
    fun signed(raw: ByteArray, at: Int) = field(raw, at, 2).toShort().toLong()
    for (n in 0 until frames) for (channel in 0 until channels) {
        var total = 0L
        for (tap in 0..7) {
            val inputFrame = n + tap - 7
            if (inputFrame >= 0) total += signed(pcm, 2 * (inputFrame * channels + channel)) * signed(coefficients, 2 * tap)
        }
        val rounded = (total + 16384) shr 15
        val clipped = rounded.coerceIn(-32768L, 32767L)
        if (clipped != rounded) clips++
        val at = 2 * (n * channels + channel)
        output[at] = clipped.toByte(); output[at + 1] = (clipped shr 8).toByte()
    }
    return output to clips
}
val averaging = ByteArray(16) { if (it % 2 == 0) 0 else 16 }
val workedFile = bytes("524946464a000000574156454a554e4b03000000010203a5666d74201000000001000200803e000000fa0000040010006461746110000000e02e20d10000803ec0e00000ff7f00806e6f746501000000427f")
val callerObjects = linkedMapOf("input_path" to 128, "output_path" to 128, "source" to 266272,
    "metadata" to 64, "samples" to 262176, "destination" to 266272, "coeff" to 64,
    "kernel_window" to 160, "kernel_output" to 160, "status" to 4, "count" to 8, "clips" to 8,
    "mode" to 4, "byte_count" to 8, "byte_capacity" to 8, "kernel_count" to 8)
fun AssemblyCase.install(label: String, raw: ByteArray = byteArrayOf(), offset: Int = 0) {
    val image = ByteArray(callerObjects.getValue(label)) { 0xa5.toByte() }
    raw.copyInto(image, offset)
    for (i in image.indices step 4) setWord(label, field(image, i, 4).toInt(), i)
}
fun AssemblyCase.arrange(): List<Long> {
    callerObjects.keys.forEach { install(it) }
    for (i in 0 until 8192 step 4) setWord("workspace", 0xa5a5a5a5.toInt(), i)
    val state = List(9) { 0x456789ab00000000L + 1009 * it }
    for (i in 0..7) setRegister("s$i", state[i])
    setRegister("fp", state[8]); val stack = readDataAddress("workspace", 8192); setRegister("sp", stack)
    return state + stack
}
fun AssemblyCase.result(state: List<Long>, status: Int, count: Long) {
    expectRegister("v0", status); expectRegister("v1", count)
    for (i in 0..7) expectRegister("s$i", state[i])
    expectRegister("fp", state[8]); expectRegister("sp", state[9]); expectOutput("")
}
fun AssemblyCase.image(label: String, raw: ByteArray = byteArrayOf(), offset: Int = 0) {
    val expected = ByteArray(callerObjects.getValue(label)) { 0xa5.toByte() }; raw.copyInto(expected, offset)
    expectCondition(readBytes(label, expected.size).contentEquals(expected.map { it.toInt() and 255 }.toIntArray()),
        "Complete caller arena $label matches its result and untouched sentinels")
}
fun inside(address: Long, width: Int, interval: Pair<Long, Int>) = address >= interval.first &&
    width <= interval.second && address - interval.first <= interval.second - width
fun AssemblyCase.accesses(reads: List<Pair<Long, Int>>, writes: List<Pair<Long, Int>>, run: () -> Unit) {
    val objects = callerObjects.map { (name, size) -> readDataAddress(name) to size }
    val low = objects.minOf { it.first }; val high = objects.maxOf { it.first + it.second }
    val trace = traceMemoryAccesses(low, (high - low).toInt(), maxAccesses = 8000000)
    try { run() } finally { trace.close() }
    for (access in trace.snapshot()) if (objects.any { access.address < it.first + it.second && it.first < access.address + access.length }) {
        expectCondition((if (access.write) writes else reads + writes).any { inside(access.address, access.length, it) },
            "Caller access stays within the selected readable or writable intervals")
    }
}
fun AssemblyCase.sizeCheck(hz: Long, channels: Long, frames: Long) {
    val state = arrange(); val prefix = description(hz, channels, frames); install("metadata", prefix, 8)
    setRegisterToDataAddress("a0", "metadata", 8)
    val valid = supported(hz, channels, frames)
    accesses(listOf(readDataAddress("metadata", 8) to 16), emptyList()) {
        callFunction("wav_bound", "function_return"); result(state, if (valid) 0 else 1, if (valid) 44 + 2 * channels * frames else 0)
    }
    image("metadata", prefix, 8)
}
fun AssemblyCase.parseCheck(raw: ByteArray, supplied: Long = raw.size.toLong(), position: Int = 3) {
    val state = arrange(); install("source", raw, position)
    val extent = if (supplied.toULong() <= 266240uL) ByteArray(supplied.toInt()) { raw.getOrElse(it) { 0xa5.toByte() } } else byteArrayOf()
    val accepted = readWave(extent)
    val status = if (supplied.toULong() > 266240uL) 2 else if (accepted == null) 1 else 0
    setRegister("a1", supplied)
    if (supplied.toULong() in 44uL..266240uL) {
        setRegisterToDataAddress("a0", "source", position.toLong()); setRegisterToDataAddress("a2", "metadata", 8)
    } else { setRegister("a0", 0x123456780001L); setRegister("a2", 0x123456780003L) }
    accesses(if (supplied.toULong() in 44uL..266240uL) listOf(readDataAddress("source", position.toLong()) to extent.size) else emptyList(),
        if (accepted != null) listOf(readDataAddress("metadata", 8) to 32) else emptyList()) {
        callFunction("wav_parse", "function_return"); result(state, status, accepted?.frames ?: 0)
    }
    image("source", raw, position); image("metadata", accepted?.descriptor() ?: byteArrayOf(), 8)
}
fun AssemblyCase.filterCheck(hz: Long, channels: Long, frames: Long, coefficients: ByteArray = averaging, pcm: ByteArray = if (supported(hz, channels, frames)) patternedPcm(channels.toInt(), frames.toInt()) else byteArrayOf()) {
    val state = arrange(); val prefix = description(hz, channels, frames); val valid = supported(hz, channels, frames)
    val computed = if (valid && frames > 0) q15(pcm, channels.toInt(), coefficients) else byteArrayOf() to 0L
    install("metadata", prefix, 8); install("source", pcm, 3); install("coeff", coefficients, 1)
    setRegisterToDataAddress("a1", "metadata", 8)
    if (valid && frames > 0) { setRegisterToDataAddress("a0", "source", 3); setRegisterToDataAddress("a2", "samples", 5); setRegisterToDataAddress("a3", "coeff", 1) }
    else { setRegister("a0", 0x123456780001L); setRegister("a2", 0x123456780003L); setRegister("a3", 0x123456780005L) }
    accesses(listOf(readDataAddress("metadata", 8) to 16) + if (valid && frames > 0) listOf(readDataAddress("source", 3) to pcm.size, readDataAddress("coeff", 1) to 16) else emptyList(),
        if (valid && frames > 0) listOf(readDataAddress("samples", 5) to pcm.size) else emptyList()) {
        callFunction("wav_filter", "function_return"); result(state, if (valid) 0 else 1, computed.second)
    }
    image("metadata", prefix, 8); image("source", pcm, 3); image("coeff", coefficients, 1); image("samples", computed.first, 5)
}
fun AssemblyCase.encodeCheck(hz: Long, channels: Long, frames: Long, capacity: Long = 266240) {
    val state = arrange(); val prefix = description(hz, channels, frames); val valid = supported(hz, channels, frames)
    val pcm = if (valid) patternedPcm(channels.toInt(), frames.toInt()) else byteArrayOf()
    val required = if (valid) 44 + pcm.size else 0
    val status = if (!valid) 1 else if (capacity.toULong() < required.toULong()) 2 else 0
    install("metadata", prefix, 8); install("source", pcm, 3)
    setRegisterToDataAddress("a1", "metadata", 8); setRegister("a3", capacity)
    if (status == 0) { setRegisterToDataAddress("a0", "source", 3); setRegisterToDataAddress("a2", "destination", 5) }
    else { setRegister("a0", 0x123456780001L); setRegister("a2", 0x123456780003L) }
    val encoded = if (status == 0) writeWave(hz, channels, pcm) else byteArrayOf()
    accesses(listOf(readDataAddress("metadata", 8) to 16) + if (status == 0 && pcm.isNotEmpty()) listOf(readDataAddress("source", 3) to pcm.size) else emptyList(),
        if (status == 0) listOf(readDataAddress("destination", 5) to encoded.size) else emptyList()) {
        callFunction("wav_encode", "function_return"); result(state, status, encoded.size.toLong())
    }
    image("metadata", prefix, 8); image("source", pcm, 3); image("destination", encoded, 5)
}
fun AssemblyCase.fileCheck(load: Boolean, raw: ByteArray, capacity: Long = 266240, selected: Long = raw.size.toLong(), transfer: Int = 7, failure: Int? = null, allowed: Boolean = true) {
    val state = arrange(); val previous = "previous result".toByteArray(); val name = if (load) "in.wav" else "out.wav"
    install(if (load) "input_path" else "output_path", name.toByteArray() + byteArrayOf(0), 1)
    if (!load) install("source", raw, 3)
    val logical = if (selected.toULong() <= 266240uL) ByteArray(selected.toInt()) { raw.getOrElse(it) { 0xa5.toByte() } } else byteArrayOf()
    val accepted = readWave(if (load) raw else logical)
    val validCapacity = capacity.toULong() in 1uL..266240uL
    val progress = if (!allowed || load && !validCapacity || !load && accepted == null) 0 else
        minOf(if (load) raw.size else logical.size, if (load) capacity.toInt() else Int.MAX_VALUE, failure ?: Int.MAX_VALUE)
    val status = if (load) {
        if (!validCapacity) 2 else if (!allowed || failure != null && failure <= minOf(raw.size, capacity.toInt())) 3
        else if (raw.size > capacity) 2 else if (accepted == null) 1 else 0
    } else if (selected.toULong() > 266240uL) 2 else if (accepted == null) 1
        else if (!allowed || failure != null && failure < logical.size) 3 else 0
    val files = provideFiles(if (load) { if (allowed) mapOf(name to raw) else emptyMap() } else mapOf(name to previous),
        writablePaths = if (!load && allowed) setOf(name) else emptySet(), maxReadSize = transfer, maxWriteSize = transfer,
        failAfterBytes = if (load) failure else null, failWriteAfterBytes = if (load) null else failure, firstDescriptor = 17)
    val pointerSafe = if (load) validCapacity else selected.toULong() in 44uL..266240uL
    if (pointerSafe) {
        setRegisterToDataAddress("a0", if (load) "input_path" else "output_path", 1); setRegisterToDataAddress("a1", "source", 3)
        if (load) setRegisterToDataAddress("a3", "metadata", 8)
    } else { setRegister("a0", 0x123456780001L); setRegister("a1", 0x123456780003L); setRegister("a3", 0x123456780005L) }
    setRegister("a2", if (load) capacity else selected)
    val reads = mutableListOf<Pair<Long, Int>>(); val writes = mutableListOf<Pair<Long, Int>>()
    if (load && validCapacity) {
        reads += readDataAddress("input_path", 1) to name.length + 1
        if (progress > 0) writes += readDataAddress("source", 3) to progress
        if (status == 0) writes += readDataAddress("metadata", 8) to 32
    } else if (!load && pointerSafe) {
        reads += readDataAddress("source", 3) to logical.size + if (accepted != null && allowed) 1 else 0
        if (accepted != null) reads += readDataAddress("output_path", 1) to name.length + 1
    }
    accesses(reads, writes) { callFunction(if (load) "wav_load" else "wav_save", "function_return"); result(state, status,
        if (status == 0) (if (load) raw.size else logical.size).toLong() else 0) }
    image("source", if (load) raw.copyOf(progress) else raw, 3)
    image("metadata", if (load && status == 0) accepted!!.descriptor() else byteArrayOf(), 8)
    files.expectAllClosed(); val events = files.snapshot()
    expectCondition(events.filter { it.operation == Operation.CLOSE }.all { it.result == 0 }, "Every close is valid")
    expectCondition(events.count { it.operation == Operation.OPEN && it.result >= 0 } == events.count { it.operation == Operation.CLOSE }, "Every acquired descriptor closes once")
    if (!load) files.expectFile(name, if (accepted != null && allowed) logical.copyOf(progress) else previous)
    if (load && !validCapacity || !load && accepted == null) expectCondition(events.isEmpty(), "Reject preflight failures before opening")
}
fun AssemblyCase.pipelineCheck(raw: ByteArray, missing: Boolean = false) {
    val state = arrange(); val old = "retain this output".toByteArray()
    install("input_path", "input.wav".toByteArray() + byteArrayOf(0)); install("output_path", "output.wav".toByteArray() + byteArrayOf(0))
    install("coeff", averaging); install("mode", little(6)); install("byte_capacity", little(266240, 8))
    val files = provideFiles(if (missing) mapOf("output.wav" to old) else mapOf("input.wav" to raw, "output.wav" to old),
        writablePaths = setOf("output.wav"), maxReadSize = 13, maxWriteSize = 19, firstDescriptor = 21)
    val parsed = if (missing) null else readWave(raw)
    val audio = parsed?.let { q15(it.samples, it.channels.toInt(), averaging) }
    val encoded = if (parsed == null) byteArrayOf() else writeWave(parsed.hz, parsed.channels, audio!!.first)
    startAt("main"); runUntil("results_ready")
    val status = if (missing) 3 else if (parsed == null) 1 else 0
    result(state, status, encoded.size.toLong()); files.expectAllClosed()
    image("source", if (missing) byteArrayOf() else raw)
    image("metadata", parsed?.descriptor() ?: byteArrayOf()); image("samples", audio?.first ?: byteArrayOf()); image("destination", encoded)
    expectCondition(readBytes("clips", 8).contentEquals(little(audio?.second ?: 0, 8).map { it.toInt() and 255 }.toIntArray()), "Caller retains the separate clipped-sample count")
    files.expectFile("output.wav", if (parsed == null) old else encoded)
    runToExit(); expectWord("status", status); expectWord("count", encoded.size); expectWord("count", 0, 4)
}

fun AssemblyCase.checkpointBytes(label:String,raw:ByteArray,offset:Int=0){expectCondition(readBytes(label,raw.size,offset).contentEquals(raw.map{it.toInt()and 255}.toIntArray()),"Exact published checkpoint bytes")}
fun AssemblyCase.preservedCheckpoint(saved:List<Long>){for(i in 0..7)expectRegister("s$i",saved[i]);expectRegister("fp",saved[8]);expectRegister("sp",saved[9])}

case("s41-1 direct checkpoint", instructionLimit=100000000) {
 val saved=arrange()
 val hz=16000L; val channels=2L; val frames=4L
 val selected=bytes("e02e20d10000803ec0e00000ff7f0080"); val incoming=bytes("")
 val plain=bytes(""); val coefficients=bytes("00100010001000100010001000100010")
 val window=bytes("0000000000000000000000000000e02e0000c0e0ff7f"); val previous=bytes("70726576696f7573206f7574707574")
 install("source",selected); install("samples",plain); install("coeff",coefficients); install("kernel_window",window)
 install("input_path","input.wav".toByteArray()+byteArrayOf(0)); install("output_path","output.wav".toByteArray()+byteArrayOf(0))
 install("mode",little(2)); install("byte_count",little(16L,8))
 install("byte_capacity",little(266240L,8)); install("kernel_count",little(4L,8))
 install("metadata",description(hz,channels,frames))
 val files=provideFiles(mapOf("input.wav" to incoming,"output.wav" to previous),writablePaths=setOf("output.wav"),maxReadSize=7,maxWriteSize=11,firstDescriptor=9)
 setRegisterToDataAddress("a0","source")
 setRegisterToDataAddress("a1","metadata")
 setRegisterToDataAddress("a2","samples")
 setRegisterToDataAddress("a3","coeff")
 setRegisterToTextAddress("ra","function_return"); startAt("wav_filter")
 runUntil("after_f_return")
 val calculated=q15(selected,channels.toInt(),coefficients)
 image("source",selected); image("metadata",description(hz,channels,frames)); image("coeff",coefficients)
 image("samples",calculated.first)
 expectRegister("v0",0); expectRegister("v1",calculated.second)
 files.expectFile("output.wav",previous)
 val events=files.snapshot(); expectCondition(events.count{it.operation==Operation.OPEN&&it.result>=0}-events.count{it.operation==Operation.CLOSE}==0,"Checkpoint descriptor ownership")
 expectCondition(events.filter{it.operation==Operation.CLOSE}.all{it.result==0},"Every close so far is valid")
 expectOutput("")
}
case("s41-1 caller checkpoint", instructionLimit=100000000) {
 val saved=arrange()
 val hz=16000L; val channels=2L; val frames=4L
 val selected=bytes("e02e20d10000803ec0e00000ff7f0080"); val incoming=bytes("")
 val plain=bytes(""); val coefficients=bytes("00100010001000100010001000100010")
 val window=bytes("0000000000000000000000000000e02e0000c0e0ff7f"); val previous=bytes("70726576696f7573206f7574707574")
 install("source",selected); install("samples",plain); install("coeff",coefficients); install("kernel_window",window)
 install("input_path","input.wav".toByteArray()+byteArrayOf(0)); install("output_path","output.wav".toByteArray()+byteArrayOf(0))
 install("mode",little(2)); install("byte_count",little(16L,8))
 install("byte_capacity",little(266240L,8)); install("kernel_count",little(4L,8))
 install("metadata",description(hz,channels,frames))
 val files=provideFiles(mapOf("input.wav" to incoming,"output.wav" to previous),writablePaths=setOf("output.wav"),maxReadSize=7,maxWriteSize=11,firstDescriptor=9)
 startAt("main")
 runUntil("after_f_return")
 val calculated=q15(selected,channels.toInt(),coefficients)
 image("source",selected); image("metadata",description(hz,channels,frames)); image("coeff",coefficients)
 image("samples",calculated.first)
 expectRegister("v0",0); expectRegister("v1",calculated.second)
 files.expectFile("output.wav",previous)
 val events=files.snapshot(); expectCondition(events.count{it.operation==Operation.OPEN&&it.result>=0}-events.count{it.operation==Operation.CLOSE}==0,"Checkpoint descriptor ownership")
 expectCondition(events.filter{it.operation==Operation.CLOSE}.all{it.result==0},"Every close so far is valid")
 expectOutput("")
}
