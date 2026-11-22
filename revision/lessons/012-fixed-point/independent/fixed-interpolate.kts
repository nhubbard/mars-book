// Full original public interpolation corpus, with frozen Kotlin Random samples.
val edges=listOf(Int.MIN_VALUE,Int.MIN_VALUE+1,-1025,-257,-256,-129,-128,-1,0,1,127,128,255,256,511,1024,Int.MAX_VALUE-1,Int.MAX_VALUE)
val weights=listOf(0,1,63,64,127,128,129,192,255,256)
val samples: List<Triple<Int,Int,Int>> = listOf(
    Triple(1374918115,810814104,27),
    Triple(-1528584880,2071885575,146),
    Triple(-1100427963,1281827389,176),
    Triple(1115261233,876586622,120),
    Triple(378001178,1551490527,173),
    Triple(763279843,-1525667029,243),
    Triple(-1604028315,1032303691,126),
    Triple(-1770919863,-823017676,31),
    Triple(866678055,-1890924893,11),
    Triple(1990911057,688088432,70),
    Triple(943331183,415600753,201),
    Triple(1153707311,-50541000,202),
    Triple(1065936414,-1838049603,217),
    Triple(-2100388474,-2122151769,59),
    Triple(1794136933,758009363,33),
    Triple(1048121654,1446290606,0),
    Triple(579649263,-432512905,224),
    Triple(-11442792,486215836,232),
    Triple(33446332,143006550,61),
    Triple(-1758018186,304523727,64),
    Triple(446800463,118577590,42),
    Triple(1026164613,256458188,253),
    Triple(-547010917,1970113145,90),
    Triple(1215612769,1685791969,182),
    Triple(-728779528,140767364,185),
    Triple(1958755376,1874402013,22),
    Triple(-1475458584,1833304663,245),
    Triple(-1282750869,-1340936094,88),
    Triple(2102774624,-1810986156,243),
    Triple(-658336684,-1866686693,95),
    Triple(1191449784,-1317968668,5),
    Triple(1911392612,-488290275,5),
    Triple(114188572,-1186422971,6),
    Triple(-1525208978,570943891,169),
    Triple(511156982,906694466,199),
    Triple(-395925708,-1242001424,93),
    Triple(-831574571,-2075827901,66),
    Triple(1257586322,1111395821,168),
    Triple(982563644,14092300,12),
    Triple(-591121286,-724997768,35),
    Triple(1604404922,-1691415774,18),
    Triple(-1096350455,-474099083,218),
    Triple(-372908939,-254182277,108),
    Triple(445577044,1575242509,161),
    Triple(-1515510074,1543919803,217),
    Triple(1351220142,-321612431,229),
    Triple(-1727435385,-1750897476,107),
    Triple(-231092644,-1297510316,11),
    Triple(-993373398,1345786686,106),
    Triple(-1660241616,-1401596683,184),
    Triple(754388942,-150465440,217),
    Triple(-1963841542,286018080,123),
    Triple(-1488114647,972914839,12),
    Triple(-819313958,1226907222,122),
    Triple(1327487183,-253231633,100),
    Triple(-1304255666,1741246248,66),
    Triple(-2060266049,-641049662,152),
    Triple(365807060,-1135962133,163),
    Triple(1969477159,1829180233,182),
    Triple(-625587627,-1951961640,88),
    Triple(513506022,641537234,187),
    Triple(1214369914,836518044,3),
    Triple(992709333,-1583363473,70),
    Triple(-1786648013,-583905875,21),
    Triple(-616728147,663040348,17),
    Triple(-1965181494,1310732737,44),
    Triple(-1042959817,-1697427053,17),
    Triple(491527442,-1475412030,16),
    Triple(-1332833844,1562915420,145),
    Triple(888929925,998072844,65),
    Triple(2031592312,1655109678,121),
    Triple(-895484278,1489074576,127),
    Triple(286178929,-1880944740,70),
    Triple(1743435909,133635949,45),
    Triple(788023102,1908570061,13),
    Triple(-1975018493,1074587998,230),
    Triple(-238185071,1004466721,237),
    Triple(-1771596378,832508065,232),
    Triple(-1540463982,482792161,185),
    Triple(-1308231389,1123634795,174),
    Triple(1951769864,2144539558,97),
    Triple(-13367154,258139631,22),
    Triple(-1442829527,2004846746,208),
    Triple(422539437,-1837688922,50),
    Triple(1126487837,-2069888706,46),
    Triple(-1701182733,1275817368,44),
    Triple(1228173731,1949155409,198),
    Triple(138208947,1958975984,36),
    Triple(1000464053,-1783017365,139),
    Triple(504057143,-1170344059,90),
    Triple(565708094,-759282450,143),
    Triple(-1684405688,-1536804351,27),
    Triple(-1330170687,-1910603143,26),
    Triple(306891106,-1638728888,185),
    Triple(1594102105,-1893269139,26),
    Triple(-470890240,1775370970,187),
    Triple(1095059955,1554556051,57),
    Triple(-658577779,-531577682,25),
    Triple(389994762,245617222,120),
    Triple(-1291261495,-1521391208,214),
    Triple(-1910879735,45531430,41),
    Triple(1316361997,-88060981,89),
    Triple(-1103985681,801205931,144),
    Triple(-997718358,1800627939,162),
    Triple(492811779,-65374475,26),
    Triple(-455156898,-1015880874,98),
    Triple(755993768,-1951071338,209),
    Triple(10865882,-2083910222,230),
    Triple(173782373,1474695673,69),
    Triple(-1584208446,624457964,139),
    Triple(1816561728,1858390255,31),
    Triple(-1837080579,-867204089,9),
    Triple(-2060472747,-1897471348,221),
    Triple(-1033292003,-435314465,24),
    Triple(-1803148585,-1615801063,240),
    Triple(2080229799,-1247651021,89),
    Triple(59930625,1469565180,234),
    Triple(-2019090933,-1464065271,245),
    Triple(-1830007216,645252535,115),
    Triple(-975769611,-781376238,180),
    Triple(745130430,-728497165,59),
    Triple(-1722173801,152534393,142),
    Triple(896039391,897216692,41),
    Triple(-1831797005,-1485335112,232),
    Triple(-1996739962,-477316040,111),
    Triple(-1963382126,924241,160),
    Triple(88802825,1199322531,7),
    Triple(-369754282,1338599353,28),
)
val trials=edges.flatMap { a -> edges.flatMap { b -> weights.map { w -> Triple(a,b,w) } } }+samples
case("raw interpolation calls 1 through 512",instructionLimit=100000) {
    setBytes("left_raw", 0, 0, 52, 18)
    setBytes("right_raw", 0, 0, 187, 220)
    setBytes("weight", 17, 0, 0, 0)
    setBytes("input_guard", 223, 155, 87, 19)
    setBytes("answer", 0, 0, 0, 106)
    setBytes("answer_guard", 224, 172, 104, 36)
    setRegister("s0", 0x1122334400000000uL.toLong())
    setRegister("s1", 0x1122334401010101uL.toLong())
    setRegister("s2", 0x1122334402020202uL.toLong())
    setRegister("s3", 0x1122334403030303uL.toLong())
    setRegister("s4", 0x1122334404040404uL.toLong())
    setRegister("s5", 0x1122334405050505uL.toLong())
    setRegister("s6", 0x1122334406060606uL.toLong())
    setRegister("s7", 0x1122334407070707uL.toLong())
    setRegister("fp", 0x1020304005060708uL.toLong())
    setRegister("gp", 0x0102030450607080uL.toLong())
    setRegister("sp", 0x000000007fffeffcuL.toLong())
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val saved=listOf("s0","s1","s2","s3","s4","s5","s6","s7","fp","gp").associateWith(::readRegister)
    val caller=readBytes(top,32)
    val protected=listOf(traceMemoryStores("left_raw",4),traceMemoryStores("right_raw",4),traceMemoryStores("weight",4),traceMemoryStores("input_guard",4),traceMemoryStores("answer",4),traceMemoryStores("answer_guard",4),traceMemoryStores(top,32))
    for ((index,trial) in trials.chunked(512)[0].withIndex()) {
        val(a,b,w)=trial
        val expected=Math.floorDiv((256L-w)*a.toLong()+w.toLong()*b.toLong()+128,256)
        setRegister("a0",a)
        setRegister("a1",b)
        setRegister("a2",w)
        callFunction("interpolate_raw",returnTo=if(index%2==0) "interpolate_done" else "interpolate_exit")
        expectCondition(readRegister("v0")==expected,"Call ${index+1} (a0=$a, a1=$b, a2=$w): expected v0=$expected, got ${readRegister("v0")}")
        expectRegister("sp",top)
        saved.forEach { (r,v) -> expectRegister(r,v) }
        expectCondition(readBytes(top,32).contentEquals(caller),"Caller stack remains intact")
        protected.forEach { it.expectNone() }
        expectOutput("")
    }
}

case("raw interpolation calls 513 through 1024",instructionLimit=100000) {
    setBytes("left_raw", 1, 0, 52, 18)
    setBytes("right_raw", 255, 255, 186, 220)
    setBytes("weight", 18, 0, 0, 0)
    setBytes("input_guard", 222, 155, 87, 19)
    setBytes("answer", 1, 0, 0, 106)
    setBytes("answer_guard", 225, 172, 104, 36)
    setRegister("s0", 0x1122334401010101uL.toLong())
    setRegister("s1", 0x1122334402020202uL.toLong())
    setRegister("s2", 0x1122334403030303uL.toLong())
    setRegister("s3", 0x1122334404040404uL.toLong())
    setRegister("s4", 0x1122334405050505uL.toLong())
    setRegister("s5", 0x1122334406060606uL.toLong())
    setRegister("s6", 0x1122334407070707uL.toLong())
    setRegister("s7", 0x1122334408080808uL.toLong())
    setRegister("fp", 0x1020304004070609uL.toLong())
    setRegister("gp", 0x0102030451617181uL.toLong())
    setRegister("sp", 0x000000007fffdffcuL.toLong())
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val saved=listOf("s0","s1","s2","s3","s4","s5","s6","s7","fp","gp").associateWith(::readRegister)
    val caller=readBytes(top,32)
    val protected=listOf(traceMemoryStores("left_raw",4),traceMemoryStores("right_raw",4),traceMemoryStores("weight",4),traceMemoryStores("input_guard",4),traceMemoryStores("answer",4),traceMemoryStores("answer_guard",4),traceMemoryStores(top,32))
    for ((index,trial) in trials.chunked(512)[1].withIndex()) {
        val(a,b,w)=trial
        val expected=Math.floorDiv((256L-w)*a.toLong()+w.toLong()*b.toLong()+128,256)
        setRegister("a0",a)
        setRegister("a1",b)
        setRegister("a2",w)
        callFunction("interpolate_raw",returnTo=if(index%2==0) "interpolate_done" else "interpolate_exit")
        expectCondition(readRegister("v0")==expected,"Call ${index+1} (a0=$a, a1=$b, a2=$w): expected v0=$expected, got ${readRegister("v0")}")
        expectRegister("sp",top)
        saved.forEach { (r,v) -> expectRegister(r,v) }
        expectCondition(readBytes(top,32).contentEquals(caller),"Caller stack remains intact")
        protected.forEach { it.expectNone() }
        expectOutput("")
    }
}

case("raw interpolation calls 1025 through 1536",instructionLimit=100000) {
    setBytes("left_raw", 2, 0, 52, 18)
    setBytes("right_raw", 254, 255, 186, 220)
    setBytes("weight", 19, 0, 0, 0)
    setBytes("input_guard", 221, 155, 87, 19)
    setBytes("answer", 2, 0, 0, 106)
    setBytes("answer_guard", 226, 172, 104, 36)
    setRegister("s0", 0x1122334402020202uL.toLong())
    setRegister("s1", 0x1122334403030303uL.toLong())
    setRegister("s2", 0x1122334404040404uL.toLong())
    setRegister("s3", 0x1122334405050505uL.toLong())
    setRegister("s4", 0x1122334406060606uL.toLong())
    setRegister("s5", 0x1122334407070707uL.toLong())
    setRegister("s6", 0x1122334408080808uL.toLong())
    setRegister("s7", 0x1122334409090909uL.toLong())
    setRegister("fp", 0x102030400704050auL.toLong())
    setRegister("gp", 0x0102030452627282uL.toLong())
    setRegister("sp", 0x000000007fffcffcuL.toLong())
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val saved=listOf("s0","s1","s2","s3","s4","s5","s6","s7","fp","gp").associateWith(::readRegister)
    val caller=readBytes(top,32)
    val protected=listOf(traceMemoryStores("left_raw",4),traceMemoryStores("right_raw",4),traceMemoryStores("weight",4),traceMemoryStores("input_guard",4),traceMemoryStores("answer",4),traceMemoryStores("answer_guard",4),traceMemoryStores(top,32))
    for ((index,trial) in trials.chunked(512)[2].withIndex()) {
        val(a,b,w)=trial
        val expected=Math.floorDiv((256L-w)*a.toLong()+w.toLong()*b.toLong()+128,256)
        setRegister("a0",a)
        setRegister("a1",b)
        setRegister("a2",w)
        callFunction("interpolate_raw",returnTo=if(index%2==0) "interpolate_done" else "interpolate_exit")
        expectCondition(readRegister("v0")==expected,"Call ${index+1} (a0=$a, a1=$b, a2=$w): expected v0=$expected, got ${readRegister("v0")}")
        expectRegister("sp",top)
        saved.forEach { (r,v) -> expectRegister(r,v) }
        expectCondition(readBytes(top,32).contentEquals(caller),"Caller stack remains intact")
        protected.forEach { it.expectNone() }
        expectOutput("")
    }
}

case("raw interpolation calls 1537 through 2048",instructionLimit=100000) {
    setBytes("left_raw", 3, 0, 52, 18)
    setBytes("right_raw", 253, 255, 186, 220)
    setBytes("weight", 20, 0, 0, 0)
    setBytes("input_guard", 220, 155, 87, 19)
    setBytes("answer", 3, 0, 0, 106)
    setBytes("answer_guard", 227, 172, 104, 36)
    setRegister("s0", 0x1122334403030303uL.toLong())
    setRegister("s1", 0x1122334404040404uL.toLong())
    setRegister("s2", 0x1122334405050505uL.toLong())
    setRegister("s3", 0x1122334406060606uL.toLong())
    setRegister("s4", 0x1122334407070707uL.toLong())
    setRegister("s5", 0x1122334408080808uL.toLong())
    setRegister("s6", 0x1122334409090909uL.toLong())
    setRegister("s7", 0x112233440a0a0a0auL.toLong())
    setRegister("fp", 0x102030400605040buL.toLong())
    setRegister("gp", 0x0102030453637383uL.toLong())
    setRegister("sp", 0x000000007fffbffcuL.toLong())
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val saved=listOf("s0","s1","s2","s3","s4","s5","s6","s7","fp","gp").associateWith(::readRegister)
    val caller=readBytes(top,32)
    val protected=listOf(traceMemoryStores("left_raw",4),traceMemoryStores("right_raw",4),traceMemoryStores("weight",4),traceMemoryStores("input_guard",4),traceMemoryStores("answer",4),traceMemoryStores("answer_guard",4),traceMemoryStores(top,32))
    for ((index,trial) in trials.chunked(512)[3].withIndex()) {
        val(a,b,w)=trial
        val expected=Math.floorDiv((256L-w)*a.toLong()+w.toLong()*b.toLong()+128,256)
        setRegister("a0",a)
        setRegister("a1",b)
        setRegister("a2",w)
        callFunction("interpolate_raw",returnTo=if(index%2==0) "interpolate_done" else "interpolate_exit")
        expectCondition(readRegister("v0")==expected,"Call ${index+1} (a0=$a, a1=$b, a2=$w): expected v0=$expected, got ${readRegister("v0")}")
        expectRegister("sp",top)
        saved.forEach { (r,v) -> expectRegister(r,v) }
        expectCondition(readBytes(top,32).contentEquals(caller),"Caller stack remains intact")
        protected.forEach { it.expectNone() }
        expectOutput("")
    }
}

case("raw interpolation calls 2049 through 2560",instructionLimit=100000) {
    setBytes("left_raw", 4, 0, 52, 18)
    setBytes("right_raw", 252, 255, 186, 220)
    setBytes("weight", 21, 0, 0, 0)
    setBytes("input_guard", 219, 155, 87, 19)
    setBytes("answer", 4, 0, 0, 106)
    setBytes("answer_guard", 228, 172, 104, 36)
    setRegister("s0", 0x1122334404040404uL.toLong())
    setRegister("s1", 0x1122334405050505uL.toLong())
    setRegister("s2", 0x1122334406060606uL.toLong())
    setRegister("s3", 0x1122334407070707uL.toLong())
    setRegister("s4", 0x1122334408080808uL.toLong())
    setRegister("s5", 0x1122334409090909uL.toLong())
    setRegister("s6", 0x112233440a0a0a0auL.toLong())
    setRegister("s7", 0x112233440b0b0b0buL.toLong())
    setRegister("fp", 0x102030400102030cuL.toLong())
    setRegister("gp", 0x0102030454647484uL.toLong())
    setRegister("sp", 0x000000007fffaffcuL.toLong())
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val saved=listOf("s0","s1","s2","s3","s4","s5","s6","s7","fp","gp").associateWith(::readRegister)
    val caller=readBytes(top,32)
    val protected=listOf(traceMemoryStores("left_raw",4),traceMemoryStores("right_raw",4),traceMemoryStores("weight",4),traceMemoryStores("input_guard",4),traceMemoryStores("answer",4),traceMemoryStores("answer_guard",4),traceMemoryStores(top,32))
    for ((index,trial) in trials.chunked(512)[4].withIndex()) {
        val(a,b,w)=trial
        val expected=Math.floorDiv((256L-w)*a.toLong()+w.toLong()*b.toLong()+128,256)
        setRegister("a0",a)
        setRegister("a1",b)
        setRegister("a2",w)
        callFunction("interpolate_raw",returnTo=if(index%2==0) "interpolate_done" else "interpolate_exit")
        expectCondition(readRegister("v0")==expected,"Call ${index+1} (a0=$a, a1=$b, a2=$w): expected v0=$expected, got ${readRegister("v0")}")
        expectRegister("sp",top)
        saved.forEach { (r,v) -> expectRegister(r,v) }
        expectCondition(readBytes(top,32).contentEquals(caller),"Caller stack remains intact")
        protected.forEach { it.expectNone() }
        expectOutput("")
    }
}

case("raw interpolation calls 2561 through 3072",instructionLimit=100000) {
    setBytes("left_raw", 5, 0, 52, 18)
    setBytes("right_raw", 251, 255, 186, 220)
    setBytes("weight", 22, 0, 0, 0)
    setBytes("input_guard", 218, 155, 87, 19)
    setBytes("answer", 5, 0, 0, 106)
    setBytes("answer_guard", 229, 172, 104, 36)
    setRegister("s0", 0x1122334405050505uL.toLong())
    setRegister("s1", 0x1122334406060606uL.toLong())
    setRegister("s2", 0x1122334407070707uL.toLong())
    setRegister("s3", 0x1122334408080808uL.toLong())
    setRegister("s4", 0x1122334409090909uL.toLong())
    setRegister("s5", 0x112233440a0a0a0auL.toLong())
    setRegister("s6", 0x112233440b0b0b0buL.toLong())
    setRegister("s7", 0x112233440c0c0c0cuL.toLong())
    setRegister("fp", 0x102030400003020duL.toLong())
    setRegister("gp", 0x0102030455657585uL.toLong())
    setRegister("sp", 0x000000007fff9ffcuL.toLong())
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val saved=listOf("s0","s1","s2","s3","s4","s5","s6","s7","fp","gp").associateWith(::readRegister)
    val caller=readBytes(top,32)
    val protected=listOf(traceMemoryStores("left_raw",4),traceMemoryStores("right_raw",4),traceMemoryStores("weight",4),traceMemoryStores("input_guard",4),traceMemoryStores("answer",4),traceMemoryStores("answer_guard",4),traceMemoryStores(top,32))
    for ((index,trial) in trials.chunked(512)[5].withIndex()) {
        val(a,b,w)=trial
        val expected=Math.floorDiv((256L-w)*a.toLong()+w.toLong()*b.toLong()+128,256)
        setRegister("a0",a)
        setRegister("a1",b)
        setRegister("a2",w)
        callFunction("interpolate_raw",returnTo=if(index%2==0) "interpolate_done" else "interpolate_exit")
        expectCondition(readRegister("v0")==expected,"Call ${index+1} (a0=$a, a1=$b, a2=$w): expected v0=$expected, got ${readRegister("v0")}")
        expectRegister("sp",top)
        saved.forEach { (r,v) -> expectRegister(r,v) }
        expectCondition(readBytes(top,32).contentEquals(caller),"Caller stack remains intact")
        protected.forEach { it.expectNone() }
        expectOutput("")
    }
}

case("raw interpolation calls 3073 through 3368",instructionLimit=100000) {
    setBytes("left_raw", 6, 0, 52, 18)
    setBytes("right_raw", 250, 255, 186, 220)
    setBytes("weight", 23, 0, 0, 0)
    setBytes("input_guard", 217, 155, 87, 19)
    setBytes("answer", 6, 0, 0, 106)
    setBytes("answer_guard", 230, 172, 104, 36)
    setRegister("s0", 0x1122334406060606uL.toLong())
    setRegister("s1", 0x1122334407070707uL.toLong())
    setRegister("s2", 0x1122334408080808uL.toLong())
    setRegister("s3", 0x1122334409090909uL.toLong())
    setRegister("s4", 0x112233440a0a0a0auL.toLong())
    setRegister("s5", 0x112233440b0b0b0buL.toLong())
    setRegister("s6", 0x112233440c0c0c0cuL.toLong())
    setRegister("s7", 0x112233440d0d0d0duL.toLong())
    setRegister("fp", 0x102030400300010euL.toLong())
    setRegister("gp", 0x0102030456667686uL.toLong())
    setRegister("sp", 0x000000007fff8ffcuL.toLong())
    val top=readRegister("sp") and -16L
    setRegister("sp",top)
    val saved=listOf("s0","s1","s2","s3","s4","s5","s6","s7","fp","gp").associateWith(::readRegister)
    val caller=readBytes(top,32)
    val protected=listOf(traceMemoryStores("left_raw",4),traceMemoryStores("right_raw",4),traceMemoryStores("weight",4),traceMemoryStores("input_guard",4),traceMemoryStores("answer",4),traceMemoryStores("answer_guard",4),traceMemoryStores(top,32))
    for ((index,trial) in trials.chunked(512)[6].withIndex()) {
        val(a,b,w)=trial
        val expected=Math.floorDiv((256L-w)*a.toLong()+w.toLong()*b.toLong()+128,256)
        setRegister("a0",a)
        setRegister("a1",b)
        setRegister("a2",w)
        callFunction("interpolate_raw",returnTo=if(index%2==0) "interpolate_done" else "interpolate_exit")
        expectCondition(readRegister("v0")==expected,"Call ${index+1} (a0=$a, a1=$b, a2=$w): expected v0=$expected, got ${readRegister("v0")}")
        expectRegister("sp",top)
        saved.forEach { (r,v) -> expectRegister(r,v) }
        expectCondition(readBytes(top,32).contentEquals(caller),"Caller stack remains intact")
        protected.forEach { it.expectNone() }
        expectOutput("")
    }
}

case("complete interpolation caller",instructionLimit=1000) {
    runToExit()
    expectWord("answer",-64)
    expectOutput("")
}
