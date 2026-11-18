import java.util.Random
import edu.missouristate.mars.cli.grading.AssemblyCase

data class ColorTrial(val unpack:Boolean,val input:IntArray,val count:Int=input.size/(if(unpack)2 else 3))
fun packPixel(r:Int,g:Int,b:Int):Int=(r/8)*2048+(g/4)*32+b/8
fun expandPixel(p:Int):List<Int>{val r=p/2048;val g=(p/32)%64;val b=p%32;return listOf(r*8+r/4,g*4+g/16,b*8+b/4)}
fun expected(t:ColorTrial):IntArray=if(t.unpack)(0 until t.count).flatMap{expandPixel(t.input[it*2]+t.input[it*2+1]*256)}.toIntArray() else (0 until t.count).flatMap{val p=packPixel(t.input[it*3],t.input[it*3+1],t.input[it*3+2]);listOf(p%256,p/256)}.toIntArray()
check(packPixel(255,0,0)==0xf800);check(packPixel(0,255,0)==0x07e0);check(packPixel(0,0,255)==31);check(packPixel(7,3,7)==0);check(expandPixel(65535)==listOf(255,255,255));check(expandPixel(0x8410)==listOf(132,130,132))
fun randomTrial(unpack:Boolean,n:Int,r:Random)=ColorTrial(unpack,IntArray(n*(if(unpack)2 else 3)){r.nextInt(256)},n)
fun AssemblyCase.checkColor(t:ColorTrial,index:Int){
 val source=(if(t.unpack)"packed" else "rgb")+(if(index%3==1)"_b" else "_a");val dest=(if(t.unpack)"rgb" else "packed")+(if(index%2==1)"_a" else "_b")
 val sourceSize=if(t.unpack)96 else 128;val destSize=if(t.unpack)128 else 96
 val sourceOffset=index%32;val destOffset=(index*7+3)%32;val result=expected(t)
 val input=IntArray(sourceSize){(index*17+it*31+193) and 255};t.input.copyInto(input,sourceOffset)
 val output=IntArray(destSize){(index*37+it*13+79) and 255};for(j in result.indices)output[destOffset+j]=result[j] xor 255
 fun fill(label:String,bytes:IntArray){for(i in 0 until bytes.size/4){var w=0;for(j in 0..3)w=w or (bytes[i*4+j] shl (j*8));setWord(label,w,offset=i*4)}}
 fill(source,input);fill(dest,output)
 val root=readDataAddress("preview");val size=(readDataAddress("caller_end")+4-root).toInt();val before=readBytes("preview",size)
 val inputBase=readDataAddress(source)+sourceOffset;val outputBase=readDataAddress(dest)+destOffset
 val history=traceMemoryAccesses("preview",size,maxAccesses=20000)
 for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1009)
 val sp=if(index%2==0)0x7fff0000L else 0x7ffe8000L
 setRegister("sp",sp);setRegister("fp",0x2345678900000033L)
 setRegisterToDataAddress("a0",source,sourceOffset.toLong());setRegisterToDataAddress("a1",dest,destOffset.toLong());setRegister("a2",t.count)
 val avoided=traceExecution("function_return","done")
 callFunction(if(t.unpack)"unpack_rgb565" else "pack_rgb565",if(index%2==0)"function_return" else "done")
 expectRegister("v0",t.count);expectRegister("sp",sp);expectRegister("fp",0x2345678900000033L)
 for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1009)
 expectCondition(avoided.snapshot().instructions==0L,"Return through supplied continuation")
 for(j in result.indices)expectBytes(dest,result[j],offset=destOffset+j)
 val after=readBytes("preview",size)
 for(i in before.indices)if(root+i !in outputBase until outputBase+result.size)expectCondition(before[i]==after[i],"Only logical output bytes change")
 for(a in history.snapshot())expectCondition((0 until a.length).all{val b=a.address+it;if(a.write)b in outputBase until outputBase+result.size else b in inputBase until inputBase+t.input.size || b in outputBase until outputBase+result.size},"Each caller access byte fits selected slices")
 history.close();avoided.close()
}
for(n in 0..32)case("caller n=$n",instructionLimit=100000){
 setWord("count",n);val original=readBytes("rgb_a",128)
 val packed=expected(ColorTrial(false,original.take(n*3).toIntArray(),n));val recovered=expected(ColorTrial(true,packed,n))
 runUntil("pack_ready");expectRegister("v0",n);expectWord("packed_count",99);expectWord("expanded_count",99)
 for(j in packed.indices)expectBytes("packed_a",packed[j],offset=j)
 runUntil("unpack_ready");expectRegister("v0",n);expectWord("packed_count",n);expectWord("expanded_count",99)
 for(j in recovered.indices)expectBytes("rgb_b",recovered[j],offset=j)
 runUntil("preview_ready");expectWord("expanded_count",n)
 for(i in 0 until 32){val first=if(i<n)(original[i*3]*65536+original[i*3+1]*256+original[i*3+2]) else 0;val second=if(i<n)(recovered[i*3]*65536+recovered[i*3+1]*256+recovered[i*3+2]) else 0;expectWord("preview",first,offset=i*4);expectWord("preview",second,offset=128+i*4)}
 expectCondition(readBytes("rgb_a",128).contentEquals(original),"Input remains unchanged")
 runToExit();expectOutput("")
}
