case("s24-1 unaligned",instructionLimit=100000){
 fun put(label:String,values:IntArray){for(i in 0 until values.size/4){var w=0;for(j in 0..3)w=w or(values[i*4+j] shl(j*8));setWord(label,w,offset=i*4)}}
 val input=IntArray(192);val coefficients=IntArray(64);val x=listOf<Int>(0,0,0,0,0,0,0,0,0,0,0,0,0,0,128,62,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);val c=listOf<Int>(0,16,0,16,0,16,0,16,0,16,0,16,0,16,0,16)
 for(j in x.indices)input[j+3]=x[j];for(j in c.indices)coefficients[j+5]=c[j]
 put("source",input);put("coeff",coefficients);put("output",IntArray(192){165});setWord("count",16)
 val root=readDataAddress("source_guard");val size=(readDataAddress("caller_end")+4-root).toInt();val before=readBytes("source_guard",size)
 val first=readDataAddress("source")+3;val taps=readDataAddress("coeff")+5;val output=readDataAddress("output")+7
 for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1009)
 setRegister("sp",0x7fff0000L);setRegister("fp",0x2345678900000033L);setRegister("v0",123);setRegister("v1",91)
 for(i in 0..9)setRegister("t$i",0x456789ab00000000L+i*17)
 setRegisterToDataAddress("a0","source",3);setRegisterToDataAddress("a1","coeff",5);setRegisterToDataAddress("a2","output",7);setRegister("a3",16)
 val history=traceMemoryAccesses("source_guard",size,maxAccesses=20000)
 callFunction("fir_q15","function_return")
 expectRegister("v0",16);expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L)
 for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1009)
 expectRegister("a3",0L)
 expectRegister("v1",0L)
 expectRegister("a0",first+32)
 expectRegister("a2",output+32)
 val expected=listOf<Int>(208,7,208,7,208,7,208,7,208,7,208,7,208,7,208,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);for(j in 0 until 32)expectBytes("output",if(j<expected.size)expected[j] else 165,offset=7+j)
 val after=readBytes("source_guard",size);for(j in before.indices)if(root+j !in output until output+32)expectCondition(before[j]==after[j],"Other caller bytes preserved")
 for(access in history.snapshot())expectCondition(16>0 && (0 until access.length).all{val b=access.address+it;if(access.write)b in output until output+32 else b in first until first+46 || b in taps until taps+16 || b in output until output+32},"Every access byte fits logical slices; empty output has no caller access")
 history.close();runToExit();expectOutput("")
}
case("s24-1 caller",instructionLimit=100000){
 fun put(label:String,values:IntArray){for(i in 0 until values.size/4){var w=0;for(j in 0..3)w=w or(values[i*4+j] shl(j*8));setWord(label,w,offset=i*4)}}
 val input=IntArray(192);val coefficients=IntArray(64);val x=listOf<Int>(0,0,0,0,0,0,0,0,0,0,0,0,0,0,128,62,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);val c=listOf<Int>(0,16,0,16,0,16,0,16,0,16,0,16,0,16,0,16)
 for(j in x.indices)input[j+0]=x[j];for(j in c.indices)coefficients[j+0]=c[j]
 put("source",input);put("coeff",coefficients);put("output",IntArray(192){165});setWord("count",16)
 val root=readDataAddress("source_guard");val size=(readDataAddress("caller_end")+4-root).toInt();val before=readBytes("source_guard",size)
 val first=readDataAddress("source")+0;val taps=readDataAddress("coeff")+0;val output=readDataAddress("output")+0
 for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1009)
 setRegister("sp",0x7fff0000L);setRegister("fp",0x2345678900000033L);setRegister("v0",123);setRegister("v1",91)
 for(i in 0..9)setRegister("t$i",0x456789ab00000000L+i*17)
 runUntil("filter_call")
 val history=traceMemoryAccesses("source_guard",size,maxAccesses=20000)
 runUntil("filter_ready")
 expectRegister("v0",16);expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L)
 for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1009)
 expectRegister("a3",0L)
 expectRegister("v1",0L)
 expectRegister("a0",first+32)
 expectRegister("a2",output+32)
 val expected=listOf<Int>(208,7,208,7,208,7,208,7,208,7,208,7,208,7,208,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);for(j in 0 until 32)expectBytes("output",if(j<expected.size)expected[j] else 165,offset=0+j)
 val after=readBytes("source_guard",size);for(j in before.indices)if(root+j !in output until output+32)expectCondition(before[j]==after[j],"Other caller bytes preserved")
 for(access in history.snapshot())expectCondition(16>0 && (0 until access.length).all{val b=access.address+it;if(access.write)b in output until output+32 else b in first until first+46 || b in taps until taps+16 || b in output until output+32},"Every access byte fits logical slices; empty output has no caller access")
 history.close();runToExit();expectOutput("")
}
case("s24-2 unaligned",instructionLimit=100000){
 fun put(label:String,values:IntArray){for(i in 0 until values.size/4){var w=0;for(j in 0..3)w=w or(values[i*4+j] shl(j*8));setWord(label,w,offset=i*4)}}
 val input=IntArray(192);val coefficients=IntArray(64);val x=listOf<Int>(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,64,0,0);val c=listOf<Int>(0,4,0,8,0,12,0,16,0,20,0,24,0,28,0,32)
 for(j in x.indices)input[j+3]=x[j];for(j in c.indices)coefficients[j+5]=c[j]
 put("source",input);put("coeff",coefficients);put("output",IntArray(192){165});setWord("count",2)
 val root=readDataAddress("source_guard");val size=(readDataAddress("caller_end")+4-root).toInt();val before=readBytes("source_guard",size)
 val first=readDataAddress("source")+3;val taps=readDataAddress("coeff")+5;val output=readDataAddress("output")+7
 for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1009)
 setRegister("sp",0x7fff0000L);setRegister("fp",0x2345678900000033L);setRegister("v0",123);setRegister("v1",91)
 for(i in 0..9)setRegister("t$i",0x456789ab00000000L+i*17)
 setRegisterToDataAddress("a0","source",3);setRegisterToDataAddress("a1","coeff",5);setRegisterToDataAddress("a2","output",7);setRegister("a3",2)
 val history=traceMemoryAccesses("source_guard",size,maxAccesses=20000)
 callFunction("fir_q15","function_return")
 expectRegister("v0",2);expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L)
 for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1009)
 expectRegister("a3",0L)
 expectRegister("v1",0L)
 expectRegister("a0",first+4)
 expectRegister("a2",output+4)
 val expected=listOf<Int>(0,16,0,14);for(j in 0 until 4)expectBytes("output",if(j<expected.size)expected[j] else 165,offset=7+j)
 val after=readBytes("source_guard",size);for(j in before.indices)if(root+j !in output until output+4)expectCondition(before[j]==after[j],"Other caller bytes preserved")
 for(access in history.snapshot())expectCondition(2>0 && (0 until access.length).all{val b=access.address+it;if(access.write)b in output until output+4 else b in first until first+18 || b in taps until taps+16 || b in output until output+4},"Every access byte fits logical slices; empty output has no caller access")
 history.close();runToExit();expectOutput("")
}
case("s24-2 caller",instructionLimit=100000){
 fun put(label:String,values:IntArray){for(i in 0 until values.size/4){var w=0;for(j in 0..3)w=w or(values[i*4+j] shl(j*8));setWord(label,w,offset=i*4)}}
 val input=IntArray(192);val coefficients=IntArray(64);val x=listOf<Int>(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,64,0,0);val c=listOf<Int>(0,4,0,8,0,12,0,16,0,20,0,24,0,28,0,32)
 for(j in x.indices)input[j+0]=x[j];for(j in c.indices)coefficients[j+0]=c[j]
 put("source",input);put("coeff",coefficients);put("output",IntArray(192){165});setWord("count",2)
 val root=readDataAddress("source_guard");val size=(readDataAddress("caller_end")+4-root).toInt();val before=readBytes("source_guard",size)
 val first=readDataAddress("source")+0;val taps=readDataAddress("coeff")+0;val output=readDataAddress("output")+0
 for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1009)
 setRegister("sp",0x7fff0000L);setRegister("fp",0x2345678900000033L);setRegister("v0",123);setRegister("v1",91)
 for(i in 0..9)setRegister("t$i",0x456789ab00000000L+i*17)
 runUntil("filter_call")
 val history=traceMemoryAccesses("source_guard",size,maxAccesses=20000)
 runUntil("filter_ready")
 expectRegister("v0",2);expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L)
 for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1009)
 expectRegister("a3",0L)
 expectRegister("v1",0L)
 expectRegister("a0",first+4)
 expectRegister("a2",output+4)
 val expected=listOf<Int>(0,16,0,14);for(j in 0 until 4)expectBytes("output",if(j<expected.size)expected[j] else 165,offset=0+j)
 val after=readBytes("source_guard",size);for(j in before.indices)if(root+j !in output until output+4)expectCondition(before[j]==after[j],"Other caller bytes preserved")
 for(access in history.snapshot())expectCondition(2>0 && (0 until access.length).all{val b=access.address+it;if(access.write)b in output until output+4 else b in first until first+18 || b in taps until taps+16 || b in output until output+4},"Every access byte fits logical slices; empty output has no caller access")
 history.close();runToExit();expectOutput("")
}
case("s24-3 unaligned",instructionLimit=100000){
 fun put(label:String,values:IntArray){for(i in 0 until values.size/4){var w=0;for(j in 0..3)w=w or(values[i*4+j] shl(j*8));setWord(label,w,offset=i*4)}}
 val input=IntArray(192);val coefficients=IntArray(64);val x=listOf<Int>(0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128);val c=listOf<Int>(0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128)
 for(j in x.indices)input[j+3]=x[j];for(j in c.indices)coefficients[j+5]=c[j]
 put("source",input);put("coeff",coefficients);put("output",IntArray(192){165});setWord("count",64)
 val root=readDataAddress("source_guard");val size=(readDataAddress("caller_end")+4-root).toInt();val before=readBytes("source_guard",size)
 val first=readDataAddress("source")+3;val taps=readDataAddress("coeff")+5;val output=readDataAddress("output")+7
 for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1009)
 setRegister("sp",0x7fff0000L);setRegister("fp",0x2345678900000033L);setRegister("v0",123);setRegister("v1",91)
 for(i in 0..9)setRegister("t$i",0x456789ab00000000L+i*17)
 setRegisterToDataAddress("a0","source",3);setRegisterToDataAddress("a1","coeff",5);setRegisterToDataAddress("a2","output",7);setRegister("a3",64)
 val history=traceMemoryAccesses("source_guard",size,maxAccesses=20000)
 callFunction("fir_q15","function_return")
 expectRegister("v0",64);expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L)
 for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1009)
 expectRegister("a3",0L)
 expectRegister("v1",64L)
 expectRegister("a0",first+128)
 expectRegister("a2",output+128)
 val expected=listOf<Int>(255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127);for(j in 0 until 128)expectBytes("output",if(j<expected.size)expected[j] else 165,offset=7+j)
 val after=readBytes("source_guard",size);for(j in before.indices)if(root+j !in output until output+128)expectCondition(before[j]==after[j],"Other caller bytes preserved")
 for(access in history.snapshot())expectCondition(64>0 && (0 until access.length).all{val b=access.address+it;if(access.write)b in output until output+128 else b in first until first+142 || b in taps until taps+16 || b in output until output+128},"Every access byte fits logical slices; empty output has no caller access")
 history.close();runToExit();expectOutput("")
}
case("s24-3 caller",instructionLimit=100000){
 fun put(label:String,values:IntArray){for(i in 0 until values.size/4){var w=0;for(j in 0..3)w=w or(values[i*4+j] shl(j*8));setWord(label,w,offset=i*4)}}
 val input=IntArray(192);val coefficients=IntArray(64);val x=listOf<Int>(0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128);val c=listOf<Int>(0,128,0,128,0,128,0,128,0,128,0,128,0,128,0,128)
 for(j in x.indices)input[j+0]=x[j];for(j in c.indices)coefficients[j+0]=c[j]
 put("source",input);put("coeff",coefficients);put("output",IntArray(192){165});setWord("count",64)
 val root=readDataAddress("source_guard");val size=(readDataAddress("caller_end")+4-root).toInt();val before=readBytes("source_guard",size)
 val first=readDataAddress("source")+0;val taps=readDataAddress("coeff")+0;val output=readDataAddress("output")+0
 for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1009)
 setRegister("sp",0x7fff0000L);setRegister("fp",0x2345678900000033L);setRegister("v0",123);setRegister("v1",91)
 for(i in 0..9)setRegister("t$i",0x456789ab00000000L+i*17)
 runUntil("filter_call")
 val history=traceMemoryAccesses("source_guard",size,maxAccesses=20000)
 runUntil("filter_ready")
 expectRegister("v0",64);expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L)
 for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1009)
 expectRegister("a3",0L)
 expectRegister("v1",64L)
 expectRegister("a0",first+128)
 expectRegister("a2",output+128)
 val expected=listOf<Int>(255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127,255,127);for(j in 0 until 128)expectBytes("output",if(j<expected.size)expected[j] else 165,offset=0+j)
 val after=readBytes("source_guard",size);for(j in before.indices)if(root+j !in output until output+128)expectCondition(before[j]==after[j],"Other caller bytes preserved")
 for(access in history.snapshot())expectCondition(64>0 && (0 until access.length).all{val b=access.address+it;if(access.write)b in output until output+128 else b in first until first+142 || b in taps until taps+16 || b in output until output+128},"Every access byte fits logical slices; empty output has no caller access")
 history.close();runToExit();expectOutput("")
}
