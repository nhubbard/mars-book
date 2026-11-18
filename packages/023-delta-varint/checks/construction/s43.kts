case("s43-1 offset function",instructionLimit=1000000){
 val input=intArrayOf(208,15,4,1,0,238,168,2);val words=IntArray(40){0x6a6b6c6d};val stream=IntArray(192){165}
 for(j in input.indices)stream[7+j]=input[j]
 for(j in words.indices)setWord("words",words[j],offset=j*4)
 for(j in 0 until 48){var value=0;for(k in 0..3)value=value or(stream[j*4+k] shl(k*8));setWord("stream",value,offset=j*4)}
 setWord("method",1);setWord("byte_count",8);setWord("word_capacity",32)
 for(name in listOf("input_used","output_used","status"))setWord(name,-1)
 for(j in 0..7)setRegister("s$j",0x3456789a00000000L+j*1009)
 setRegister("fp",0x2345678900000033L);setRegister("sp",0x7fff0000L)
 for(j in 0..9)setRegister("t$j",0x456789ab00000000L+j*17)
 setRegisterToDataAddress("a0","stream",7L);setRegister("a1",8);setRegisterToDataAddress("a2","words",12L);setRegister("a3",32)
 val root=readDataAddress("words");val size=(readDataAddress("control_end")+4-root).toInt();val before=readBytes("words",size)
 val source=readDataAddress("stream",7L);val destination=readDataAddress("words",12L)
 val objects=listOf("words" to 160,"stream" to 192,"words_end" to 4,"stream_end" to 4,"word_count" to 4,"byte_capacity" to 4,"byte_count" to 4,"word_capacity" to 4,"method" to 4,"input_used" to 4,"output_used" to 4,"status" to 4,"control_end" to 4).map{(name,length)->readDataAddress(name) to length}
 val history=traceMemoryAccesses(root,size,maxAccesses=100000)
 callFunction("delta_decode","function_return")
 expectRegister("v0",0)
 expectRegister("v1",0)
 expectRegister("a3",0)
 expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L);for(j in 0..7)expectRegister("s$j",0x3456789a00000000L+j*1009)
 for(name in listOf("input_used","output_used","status"))expectWord(name,-1)
 expectRegister("t1",2000L)
 expectRegister("t3",2L)
 fun verifyMemory(output:List<Int>){
 for(j in words.indices)expectWord("words",if(j in 3 until 3+output.size)output[j-3] else words[j],offset=j*4)
 for(j in stream.indices)expectBytes("stream",stream[j],offset=j)
 val after=readBytes("words",size);val resultAddresses=listOf("input_used","output_used","status").map{readDataAddress(it)}
 for(j in before.indices)if(root+j !in destination until destination+output.size*4 && resultAddresses.none{root+j in it until it+4})expectCondition(before[j]==after[j],"Other caller bytes preserved")
 }
 fun checkHistory(bytes:Int){
 fun fits(address:Long,length:Int,base:Long,size:Int)=address>=base && length.toLong()<=size && address-base<=size.toLong()-length
 val initialized=BooleanArray(bytes)
 for(e in history.snapshot()){val output=fits(e.address,e.length,destination,bytes);val input=fits(e.address,e.length,source,8);val known=objects.any{(base,length)->e.address<base+length && e.address+e.length>base}
 expectCondition(if(e.write)output else output || input || !known,"Every caller access stays in its logical source or expected provisional output; no input access at zero capacity")
 if(output)for(j in (e.address-destination).toInt() until (e.address-destination).toInt()+e.length)if(e.write)initialized[j]=true else expectCondition(initialized[j],"Output reads follow initialization")
 }
 expectCondition(initialized.all{it},"Every expected output byte is written")
 }
 val observed=listOf<Int>();verifyMemory(observed);checkHistory(observed.size*4)
 history.close();runUntil("results_ready");expectWord("input_used",0);expectWord("output_used",0);expectWord("status",0);verifyMemory(observed);runToExit();verifyMemory(observed);expectOutput("")
}
case("s43-1 caller",instructionLimit=1000000){
 val input=intArrayOf(208,15,4,1,0,238,168,2);val words=IntArray(40){0x6a6b6c6d};val stream=IntArray(192){165}
 for(j in input.indices)stream[0+j]=input[j]
 for(j in words.indices)setWord("words",words[j],offset=j*4)
 for(j in 0 until 48){var value=0;for(k in 0..3)value=value or(stream[j*4+k] shl(k*8));setWord("stream",value,offset=j*4)}
 setWord("method",1);setWord("byte_count",8);setWord("word_capacity",32)
 for(name in listOf("input_used","output_used","status"))setWord(name,-1)
 for(j in 0..7)setRegister("s$j",0x3456789a00000000L+j*1009)
 setRegister("fp",0x2345678900000033L);setRegister("sp",0x7fff0000L)
 for(j in 0..9)setRegister("t$j",0x456789ab00000000L+j*17)
 runUntil("codec_call")
 val root=readDataAddress("words");val size=(readDataAddress("control_end")+4-root).toInt();val before=readBytes("words",size)
 val source=readDataAddress("stream",0L);val destination=readDataAddress("words",0L)
 val objects=listOf("words" to 160,"stream" to 192,"words_end" to 4,"stream_end" to 4,"word_count" to 4,"byte_capacity" to 4,"byte_count" to 4,"word_capacity" to 4,"method" to 4,"input_used" to 4,"output_used" to 4,"status" to 4,"control_end" to 4).map{(name,length)->readDataAddress(name) to length}
 val history=traceMemoryAccesses(root,size,maxAccesses=100000)
 runUntil("codec_done")
 expectRegister("v0",0)
 expectRegister("v1",0)
 expectRegister("a3",0)
 expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L);for(j in 0..7)expectRegister("s$j",0x3456789a00000000L+j*1009)
 for(name in listOf("input_used","output_used","status"))expectWord(name,-1)
 expectRegister("t1",2000L)
 expectRegister("t3",2L)
 fun verifyMemory(output:List<Int>){
 for(j in words.indices)expectWord("words",if(j in 0 until 0+output.size)output[j-0] else words[j],offset=j*4)
 for(j in stream.indices)expectBytes("stream",stream[j],offset=j)
 val after=readBytes("words",size);val resultAddresses=listOf("input_used","output_used","status").map{readDataAddress(it)}
 for(j in before.indices)if(root+j !in destination until destination+output.size*4 && resultAddresses.none{root+j in it until it+4})expectCondition(before[j]==after[j],"Other caller bytes preserved")
 }
 fun checkHistory(bytes:Int){
 fun fits(address:Long,length:Int,base:Long,size:Int)=address>=base && length.toLong()<=size && address-base<=size.toLong()-length
 val initialized=BooleanArray(bytes)
 for(e in history.snapshot()){val output=fits(e.address,e.length,destination,bytes);val input=fits(e.address,e.length,source,8);val known=objects.any{(base,length)->e.address<base+length && e.address+e.length>base}
 expectCondition(if(e.write)output else output || input || !known,"Every caller access stays in its logical source or expected provisional output; no input access at zero capacity")
 if(output)for(j in (e.address-destination).toInt() until (e.address-destination).toInt()+e.length)if(e.write)initialized[j]=true else expectCondition(initialized[j],"Output reads follow initialization")
 }
 expectCondition(initialized.all{it},"Every expected output byte is written")
 }
 val observed=listOf<Int>();verifyMemory(observed);checkHistory(observed.size*4)
 history.close();runUntil("results_ready");expectWord("input_used",0);expectWord("output_used",0);expectWord("status",0);verifyMemory(observed);runToExit();verifyMemory(observed);expectOutput("")
}
case("s43-2 offset function",instructionLimit=1000000){
 val input=intArrayOf(255,255,255,255,15);val words=IntArray(40){0x6a6b6c6d};val stream=IntArray(192){165}
 for(j in input.indices)stream[7+j]=input[j]
 for(j in words.indices)setWord("words",words[j],offset=j*4)
 for(j in 0 until 48){var value=0;for(k in 0..3)value=value or(stream[j*4+k] shl(k*8));setWord("stream",value,offset=j*4)}
 setWord("method",1);setWord("byte_count",5);setWord("word_capacity",32)
 for(name in listOf("input_used","output_used","status"))setWord(name,-1)
 for(j in 0..7)setRegister("s$j",0x3456789a00000000L+j*1009)
 setRegister("fp",0x2345678900000033L);setRegister("sp",0x7fff0000L)
 for(j in 0..9)setRegister("t$j",0x456789ab00000000L+j*17)
 setRegisterToDataAddress("a0","stream",7L);setRegister("a1",5);setRegisterToDataAddress("a2","words",12L);setRegister("a3",32)
 val root=readDataAddress("words");val size=(readDataAddress("control_end")+4-root).toInt();val before=readBytes("words",size)
 val source=readDataAddress("stream",7L);val destination=readDataAddress("words",12L)
 val objects=listOf("words" to 160,"stream" to 192,"words_end" to 4,"stream_end" to 4,"word_count" to 4,"byte_capacity" to 4,"byte_count" to 4,"word_capacity" to 4,"method" to 4,"input_used" to 4,"output_used" to 4,"status" to 4,"control_end" to 4).map{(name,length)->readDataAddress(name) to length}
 val history=traceMemoryAccesses(root,size,maxAccesses=100000)
 callFunction("delta_decode","function_return")
 expectRegister("v0",0)
 expectRegister("v1",0)
 expectRegister("a3",0)
 expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L);for(j in 0..7)expectRegister("s$j",0x3456789a00000000L+j*1009)
 for(name in listOf("input_used","output_used","status"))expectWord(name,-1)
 expectRegister("t0",5L)
 expectRegister("t3",5L)
 fun verifyMemory(output:List<Int>){
 for(j in words.indices)expectWord("words",if(j in 3 until 3+output.size)output[j-3] else words[j],offset=j*4)
 for(j in stream.indices)expectBytes("stream",stream[j],offset=j)
 val after=readBytes("words",size);val resultAddresses=listOf("input_used","output_used","status").map{readDataAddress(it)}
 for(j in before.indices)if(root+j !in destination until destination+output.size*4 && resultAddresses.none{root+j in it until it+4})expectCondition(before[j]==after[j],"Other caller bytes preserved")
 }
 fun checkHistory(bytes:Int){
 fun fits(address:Long,length:Int,base:Long,size:Int)=address>=base && length.toLong()<=size && address-base<=size.toLong()-length
 val initialized=BooleanArray(bytes)
 for(e in history.snapshot()){val output=fits(e.address,e.length,destination,bytes);val input=fits(e.address,e.length,source,5);val known=objects.any{(base,length)->e.address<base+length && e.address+e.length>base}
 expectCondition(if(e.write)output else output || input || !known,"Every caller access stays in its logical source or expected provisional output; no input access at zero capacity")
 if(output)for(j in (e.address-destination).toInt() until (e.address-destination).toInt()+e.length)if(e.write)initialized[j]=true else expectCondition(initialized[j],"Output reads follow initialization")
 }
 expectCondition(initialized.all{it},"Every expected output byte is written")
 }
 val observed=listOf<Int>();verifyMemory(observed);checkHistory(observed.size*4)
 history.close();runUntil("results_ready");expectWord("input_used",0);expectWord("output_used",0);expectWord("status",0);verifyMemory(observed);runToExit();verifyMemory(observed);expectOutput("")
}
case("s43-2 caller",instructionLimit=1000000){
 val input=intArrayOf(255,255,255,255,15);val words=IntArray(40){0x6a6b6c6d};val stream=IntArray(192){165}
 for(j in input.indices)stream[0+j]=input[j]
 for(j in words.indices)setWord("words",words[j],offset=j*4)
 for(j in 0 until 48){var value=0;for(k in 0..3)value=value or(stream[j*4+k] shl(k*8));setWord("stream",value,offset=j*4)}
 setWord("method",1);setWord("byte_count",5);setWord("word_capacity",32)
 for(name in listOf("input_used","output_used","status"))setWord(name,-1)
 for(j in 0..7)setRegister("s$j",0x3456789a00000000L+j*1009)
 setRegister("fp",0x2345678900000033L);setRegister("sp",0x7fff0000L)
 for(j in 0..9)setRegister("t$j",0x456789ab00000000L+j*17)
 runUntil("codec_call")
 val root=readDataAddress("words");val size=(readDataAddress("control_end")+4-root).toInt();val before=readBytes("words",size)
 val source=readDataAddress("stream",0L);val destination=readDataAddress("words",0L)
 val objects=listOf("words" to 160,"stream" to 192,"words_end" to 4,"stream_end" to 4,"word_count" to 4,"byte_capacity" to 4,"byte_count" to 4,"word_capacity" to 4,"method" to 4,"input_used" to 4,"output_used" to 4,"status" to 4,"control_end" to 4).map{(name,length)->readDataAddress(name) to length}
 val history=traceMemoryAccesses(root,size,maxAccesses=100000)
 runUntil("codec_done")
 expectRegister("v0",0)
 expectRegister("v1",0)
 expectRegister("a3",0)
 expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L);for(j in 0..7)expectRegister("s$j",0x3456789a00000000L+j*1009)
 for(name in listOf("input_used","output_used","status"))expectWord(name,-1)
 expectRegister("t0",5L)
 expectRegister("t3",5L)
 fun verifyMemory(output:List<Int>){
 for(j in words.indices)expectWord("words",if(j in 0 until 0+output.size)output[j-0] else words[j],offset=j*4)
 for(j in stream.indices)expectBytes("stream",stream[j],offset=j)
 val after=readBytes("words",size);val resultAddresses=listOf("input_used","output_used","status").map{readDataAddress(it)}
 for(j in before.indices)if(root+j !in destination until destination+output.size*4 && resultAddresses.none{root+j in it until it+4})expectCondition(before[j]==after[j],"Other caller bytes preserved")
 }
 fun checkHistory(bytes:Int){
 fun fits(address:Long,length:Int,base:Long,size:Int)=address>=base && length.toLong()<=size && address-base<=size.toLong()-length
 val initialized=BooleanArray(bytes)
 for(e in history.snapshot()){val output=fits(e.address,e.length,destination,bytes);val input=fits(e.address,e.length,source,5);val known=objects.any{(base,length)->e.address<base+length && e.address+e.length>base}
 expectCondition(if(e.write)output else output || input || !known,"Every caller access stays in its logical source or expected provisional output; no input access at zero capacity")
 if(output)for(j in (e.address-destination).toInt() until (e.address-destination).toInt()+e.length)if(e.write)initialized[j]=true else expectCondition(initialized[j],"Output reads follow initialization")
 }
 expectCondition(initialized.all{it},"Every expected output byte is written")
 }
 val observed=listOf<Int>();verifyMemory(observed);checkHistory(observed.size*4)
 history.close();runUntil("results_ready");expectWord("input_used",0);expectWord("output_used",0);expectWord("status",0);verifyMemory(observed);runToExit();verifyMemory(observed);expectOutput("")
}
