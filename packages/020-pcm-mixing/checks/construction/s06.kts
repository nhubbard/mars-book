case("s06-1 unaligned",instructionLimit=100000){
 fun put(label:String,values:IntArray){for(i in 0 until 48){var w=0;for(j in 0..3)w=w or(values[i*4+j] shl(j*8));setWord(label,w,offset=i*4)}}
 val left=IntArray(192);val right=IntArray(192);val l=listOf<Int>(24,252);val r=listOf<Int>(208,7)
 for(j in l.indices)left[j+3]=l[j];for(j in r.indices)right[j+5]=r[j]
 put("pcm_a",left);put("pcm_b",right);put("pcm_c",IntArray(192){165});setWord("count",1)
 val root=readDataAddress("pcm_a_guard");val size=(readDataAddress("caller_end")+4-root).toInt();val before=readBytes("pcm_a_guard",size)
 val first=readDataAddress("pcm_a")+3;val second=readDataAddress("pcm_b")+5;val output=readDataAddress("pcm_c")+7
 for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1009)
 setRegister("sp",0x7fff0000L);setRegister("fp",0x2345678900000033L);setRegister("v0",123);setRegister("v1",91)
 for(i in 0..9)setRegister("t$i",0x456789ab00000000L+i*17)
 setRegisterToDataAddress("a0","pcm_a",3);setRegisterToDataAddress("a1","pcm_b",5);setRegisterToDataAddress("a2","pcm_c",7);setRegister("a3",1)
 val history=traceMemoryAccesses("pcm_a_guard",size,maxAccesses=20000)
 callFunction("mix_pcm16","function_return")
 expectRegister("v0",1);expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L)
 for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1009)
 expectRegister("t0",64536L)
 val expected=listOf<Int>();for(j in 0 until 2)expectBytes("pcm_c",if(j<expected.size)expected[j] else 165,offset=7+j)
 val after=readBytes("pcm_a_guard",size);for(j in before.indices)if(root+j !in output until output+2)expectCondition(before[j]==after[j],"Other caller bytes preserved")
 for(access in history.snapshot())expectCondition((0 until access.length).all{val b=access.address+it;if(access.write)b in output until output+2 else b in first until first+2 || b in second until second+2 || b in output until output+2},"Each access byte fits its logical slice")
 history.close();runToExit();expectOutput("")
}
case("s06-1 caller",instructionLimit=100000){
 fun put(label:String,values:IntArray){for(i in 0 until 48){var w=0;for(j in 0..3)w=w or(values[i*4+j] shl(j*8));setWord(label,w,offset=i*4)}}
 val left=IntArray(192);val right=IntArray(192);val l=listOf<Int>(24,252);val r=listOf<Int>(208,7)
 for(j in l.indices)left[j+0]=l[j];for(j in r.indices)right[j+0]=r[j]
 put("pcm_a",left);put("pcm_b",right);put("pcm_c",IntArray(192){165});setWord("count",1)
 val root=readDataAddress("pcm_a_guard");val size=(readDataAddress("caller_end")+4-root).toInt();val before=readBytes("pcm_a_guard",size)
 val first=readDataAddress("pcm_a")+0;val second=readDataAddress("pcm_b")+0;val output=readDataAddress("pcm_c")+0
 for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1009)
 setRegister("sp",0x7fff0000L);setRegister("fp",0x2345678900000033L);setRegister("v0",123);setRegister("v1",91)
 for(i in 0..9)setRegister("t$i",0x456789ab00000000L+i*17)
 runUntil("mix_call")
 val history=traceMemoryAccesses("pcm_a_guard",size,maxAccesses=20000)
 runUntil("mix_ready")
 expectRegister("v0",1);expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L)
 for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1009)
 expectRegister("t0",64536L)
 val expected=listOf<Int>();for(j in 0 until 2)expectBytes("pcm_c",if(j<expected.size)expected[j] else 165,offset=0+j)
 val after=readBytes("pcm_a_guard",size);for(j in before.indices)if(root+j !in output until output+2)expectCondition(before[j]==after[j],"Other caller bytes preserved")
 for(access in history.snapshot())expectCondition((0 until access.length).all{val b=access.address+it;if(access.write)b in output until output+2 else b in first until first+2 || b in second until second+2 || b in output until output+2},"Each access byte fits its logical slice")
 history.close();runToExit();expectOutput("")
}
