case("s09-1 offset function",instructionLimit=500000){
 val av=intArrayOf(1,2,3,4,5,6);val bv=intArrayOf(7,8,9,10,11,12)
 for(i in 0 until 272){setWord("left",if(i in 3 until 3+av.size)av[i-3] else 0,offset=i*4);setWord("right",if(i in 5 until 5+bv.size)bv[i-5] else 0,offset=i*4)}
 for(i in 0 until 544)setWord("product",0x5a5aa5a5,offset=i*4)
 for((i,value) in listOf(2,3,2,2).withIndex())setWord("shape",value,offset=i*4)
 setWord("method",0);setWord("completed",-1)
 for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1009)
 setRegister("fp",0x2345678900000033L);setRegister("sp",0x7fff0000L)
 for(i in 0..9)setRegister("t$i",0x456789ab00000000L+i*17)
 setRegisterToDataAddress("a0","left",12L);setRegisterToDataAddress("a1","right",20L);setRegisterToDataAddress("a2","product",56L);setRegisterToDataAddress("a3","shape")
 val root=readDataAddress("left");val size=(readDataAddress("completed")+4-root).toInt();val before=readBytes("left",size)
 val first=readDataAddress("left",12L);val right=readDataAddress("right",20L);val output=readDataAddress("product",56L);val descriptor=readDataAddress("shape")
 val history=traceMemoryAccesses(root,size,maxAccesses=100000)
 callFunction("matmul_naive","matmul_done")
 history.close();expectRegister("v0",4);expectWord("completed",-1)
 expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L)
 for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1009)
 expectRegister("t6",8L)
 expectRegister("t5",readDataAddress("right",20L))
 val values=listOf<Long>(6510698340921550245L,6510698340921550245L,6510698340921550245L,6510698340921550245L)
 fun verifyProduct(){for(index in 0 until 272){val d=index-7;val value=if(d in values.indices)values[d] else 0x5a5aa5a55a5aa5a5L;expectWord("product",value.toInt(),offset=index*8);expectWord("product",(value ushr 32).toInt(),offset=index*8+4)}}
 verifyProduct();val after=readBytes("left",size)
 for(i in before.indices)if(root+i !in output until output+32)expectCondition(before[i]==after[i],"Other caller bytes unchanged")
 val initialized=BooleanArray(32)
 fun fits(address:Long,length:Int,base:Long,bytes:Int)=address>=base && length.toLong()<=bytes && address-base<=bytes.toLong()-length
 for(e in history.snapshot()){
  val product=fits(e.address,e.length,output,32)
  val inputs=4>0 && (fits(e.address,e.length,first,24) || fits(e.address,e.length,right,24))
  expectCondition(if(e.write)product else product || inputs || fits(e.address,e.length,descriptor,16),"Every notice stays inside permitted logical slices")
  if(product)for(byte in (e.address-output).toInt() until (e.address-output).toInt()+e.length)if(e.write)initialized[byte]=true else expectCondition(initialized[byte],"Initialize partial-sum bytes before reading")
 }
 runUntil("results_ready");expectWord("completed",4);verifyProduct();runToExit();verifyProduct();expectOutput("")
}
case("s09-1 caller",instructionLimit=500000){
 val av=intArrayOf(1,2,3,4,5,6);val bv=intArrayOf(7,8,9,10,11,12)
 for(i in 0 until 272){setWord("left",if(i in 0 until 0+av.size)av[i-0] else 0,offset=i*4);setWord("right",if(i in 0 until 0+bv.size)bv[i-0] else 0,offset=i*4)}
 for(i in 0 until 544)setWord("product",0x5a5aa5a5,offset=i*4)
 for((i,value) in listOf(2,3,2,2).withIndex())setWord("shape",value,offset=i*4)
 setWord("method",0);setWord("completed",-1)
 for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1009)
 setRegister("fp",0x2345678900000033L);setRegister("sp",0x7fff0000L)
 for(i in 0..9)setRegister("t$i",0x456789ab00000000L+i*17)
 runUntil("matmul_call")
 val root=readDataAddress("left");val size=(readDataAddress("completed")+4-root).toInt();val before=readBytes("left",size)
 val first=readDataAddress("left",0L);val right=readDataAddress("right",0L);val output=readDataAddress("product",0L);val descriptor=readDataAddress("shape")
 val history=traceMemoryAccesses(root,size,maxAccesses=100000)
 runUntil("matmul_done")
 history.close();expectRegister("v0",4);expectWord("completed",-1)
 expectRegister("sp",0x7fff0000L);expectRegister("fp",0x2345678900000033L)
 for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1009)
 expectRegister("t6",8L)
 expectRegister("t5",readDataAddress("right",0L))
 val values=listOf<Long>(6510698340921550245L,6510698340921550245L,6510698340921550245L,6510698340921550245L)
 fun verifyProduct(){for(index in 0 until 272){val d=index-0;val value=if(d in values.indices)values[d] else 0x5a5aa5a55a5aa5a5L;expectWord("product",value.toInt(),offset=index*8);expectWord("product",(value ushr 32).toInt(),offset=index*8+4)}}
 verifyProduct();val after=readBytes("left",size)
 for(i in before.indices)if(root+i !in output until output+32)expectCondition(before[i]==after[i],"Other caller bytes unchanged")
 val initialized=BooleanArray(32)
 fun fits(address:Long,length:Int,base:Long,bytes:Int)=address>=base && length.toLong()<=bytes && address-base<=bytes.toLong()-length
 for(e in history.snapshot()){
  val product=fits(e.address,e.length,output,32)
  val inputs=4>0 && (fits(e.address,e.length,first,24) || fits(e.address,e.length,right,24))
  expectCondition(if(e.write)product else product || inputs || fits(e.address,e.length,descriptor,16),"Every notice stays inside permitted logical slices")
  if(product)for(byte in (e.address-output).toInt() until (e.address-output).toInt()+e.length)if(e.write)initialized[byte]=true else expectCondition(initialized[byte],"Initialize partial-sum bytes before reading")
 }
 runUntil("results_ready");expectWord("completed",4);verifyProduct();runToExit();verifyProduct();expectOutput("")
}
