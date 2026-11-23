// Generated public behavior checks; edit the adjacent .cases.json and regenerate.
// Each case runs in a fresh simulator; labels and scratch registers are not checked.

case("side 16 clock 64", input = "", instructionLimit = 100000) {
    val bot = connectMarsBot(64)
    setBytes(0x0000000010010000L, 0x10, 0x00, 0x00, 0x00, 0x8f, 0x9d, 0x57, 0x13)
    val stores0 = traceMemoryStores(0x0000000010010000L, 8)
    val stores1 = traceMemoryStores(0x00000000ffff8030L, 4)
    val stores2 = traceMemoryStores(0x00000000ffff8040L, 4)
    runToExit()
    bot.expectState(0.0, 0.0, 0, moving = false, penDown = false)
    bot.expectTrail(0.0 to 0.0, 16.0 to 0.0, 16.0 to 16.0, 0.0 to 16.0, 0.0 to 0.0)
    expectOutput("")
    expectBytes(0x0000000010010000L, 0x10, 0x00, 0x00, 0x00, 0x8f, 0x9d, 0x57, 0x13)
    stores0.expectNone()
    stores1.expectNone()
    stores2.expectNone()
}

case("side 1 clock 64", input = "", instructionLimit = 100000) {
    val bot = connectMarsBot(64)
    setBytes(0x0000000010010000L, 0x01, 0x00, 0x00, 0x00, 0x7e, 0x9b, 0x57, 0x13)
    val stores0 = traceMemoryStores(0x0000000010010000L, 8)
    val stores1 = traceMemoryStores(0x00000000ffff8030L, 4)
    val stores2 = traceMemoryStores(0x00000000ffff8040L, 4)
    runToExit()
    bot.expectState(0.0, 0.0, 0, moving = false, penDown = false)
    bot.expectTrail(0.0 to 0.0, 1.0 to 0.0, 1.0 to 1.0, 0.0 to 1.0, 0.0 to 0.0)
    expectOutput("")
    expectBytes(0x0000000010010000L, 0x01, 0x00, 0x00, 0x00, 0x7e, 0x9b, 0x57, 0x13)
    stores0.expectNone()
    stores1.expectNone()
    stores2.expectNone()
}

case("side 63 clock 17", input = "", instructionLimit = 100000) {
    val bot = connectMarsBot(17)
    setBytes(0x0000000010010000L, 0x3f, 0x00, 0x00, 0x00, 0x2f, 0x8c, 0x57, 0x13)
    val stores0 = traceMemoryStores(0x0000000010010000L, 8)
    val stores1 = traceMemoryStores(0x00000000ffff8030L, 4)
    val stores2 = traceMemoryStores(0x00000000ffff8040L, 4)
    runToExit()
    bot.expectState(0.0, 0.0, 0, moving = false, penDown = false)
    bot.expectTrail(0.0 to 0.0, 63.0 to 0.0, 63.0 to 63.0, 0.0 to 63.0, 0.0 to 0.0)
    expectOutput("")
    expectBytes(0x0000000010010000L, 0x3f, 0x00, 0x00, 0x00, 0x2f, 0x8c, 0x57, 0x13)
    stores0.expectNone()
    stores1.expectNone()
    stores2.expectNone()
}
