// Complete classifier checks; no whole-image target validation or guest execution is supplied.
val widths=mapOf(0 to 1,1 to 2,2 to 5,16 to 1,17 to 1,18 to 1,19 to 1,32 to 1,33 to 1,34 to 1,35 to 1,48 to 2,49 to 2)
for(opcode in 0..255)for(length in 0..5)case("record opcode $opcode length $length",instructionLimit=1000){
 setWord("record",opcode)
 val original=readBytes("record",6)
 val history=traceMemoryAccesses("record",(readDataAddress("width")+4-readDataAddress("record")).toInt())
 setRegisterToDataAddress("a0","record");setRegister("a1",length);setRegister("a2",0);setRegister("sp",0x7fff0000L)
 val width=widths[opcode]
 val status=if(length==0)-4 else if(width==null)-1 else if(width>length)-2 else 0
 callFunction("record_width","done")
 expectRegister("v0",status);expectRegister("v1",if(status==0)width!! else 0)
 expectCondition(readBytes("record",6).contentEquals(original),"Classification is read-only")
 for(access in history.snapshot())expectCondition(!access.write && length>0 && access.address==readDataAddress("record") && access.length==1,"Read only the selected opcode after proving it exists")
 history.close();expectRegister("sp",0x7fff0000L);runToExit();expectOutput("")
}
case("complete worked classifier caller",instructionLimit=1000){
 runUntil("width_ready");expectRegister("v0",0);expectRegister("v1",5)
 runToExit();expectWord("status",0);expectWord("width",5);expectOutput("")
}
