import java.util.Random
import java.lang.Math.floorDiv
import java.lang.Math.floorMod
import edu.missouristate.mars.cli.grading.AssemblyCase

data class FirTrial(val input:IntArray,val taps:IntArray,val count:Int) {
 init {require(count in 0..32);require(input.size==if(count==0)7 else 2*count+6);require(taps.size==8);require((input.asSequence()+taps.asSequence()).all{it in -32768..32767})}
}
fun exact(t:FirTrial)=LongArray(t.count){i->(0..7).sumOf{j->t.input[2*i+j].toLong()*t.taps[j]}}
fun quantized(t:FirTrial)=exact(t).map{floorDiv(it+16384,32768)}.toLongArray()
fun expected(t:FirTrial)=quantized(t).map{it.coerceIn(-32768,32767).toInt()}.toIntArray()
fun clips(t:FirTrial)=quantized(t).count{it !in -32768L..32767L}
fun bytes(samples:IntArray)=samples.flatMap{listOf(it and 255,(it ushr 8) and 255)}.toIntArray()
fun signed16(a:IntArray)=IntArray(a.size/2){(a[it*2]+a[it*2+1]*256).toShort().toInt()}
fun randomTrial(n:Int,r:Random)=FirTrial(IntArray(if(n==0)7 else 2*n+6){r.nextInt(65536)-32768},IntArray(8){r.nextInt(65536)-32768},n)
fun AssemblyCase.checkFir(t:FirTrial,index:Int):IntArray{
 val src=if(index%2==0)"source" else "output";val dest=if(index%2==0)"output" else "source";val so=index%32;val co=(index*7+1)%32;val oo=(index*13+3)%32;val expected=expected(t);val encoded=bytes(expected);val n=t.count
 fun prepare(label:String,size:Int,offset:Int,values:IntArray,poison:Boolean=false){val data=IntArray(size){(it*31+index*17+193) and 255};for(i in values.indices)data[offset+i]=if(poison)values[i] xor 255 else values[i];for(i in 0 until size/4){var w=0;for(j in 0..3)w=w or(data[i*4+j] shl(8*j));setWord(label,w,offset=i*4)}}
 prepare(src,192,so,bytes(t.input));prepare("coeff",64,co,bytes(t.taps));prepare(dest,192,oo,encoded,true)
 val root=readDataAddress("source_guard");val size=(readDataAddress("caller_end")+4-root).toInt();val before=readBytes("source_guard",size);val input=readDataAddress(src)+so;val coefficients=readDataAddress("coeff")+co;val out=readDataAddress(dest)+oo
 val history=traceMemoryAccesses("source_guard",size,maxAccesses=20000)
 for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1009)
 setRegister("sp",if(index%2==0)0x7fff0000L else 0x7ffe8000L);val sp=if(index%2==0)0x7fff0000L else 0x7ffe8000L;setRegister("fp",0x2345678900000033L)
 setRegister("v0",0x12345678);setRegister("v1",0x23456789)
 setRegisterToDataAddress("a0",src,so.toLong());setRegisterToDataAddress("a1","coeff",co.toLong());setRegisterToDataAddress("a2",dest,oo.toLong());setRegister("a3",n)
 val avoided=traceExecution("function_return","done");callFunction("decimate_q15",if(index%2==0)"function_return" else "done")
 expectRegister("v0",n);expectRegister("v1",clips(t));expectRegister("sp",sp);expectRegister("fp",0x2345678900000033L);for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1009)
 expectCondition(avoided.snapshot().instructions==0L,"Return uses supplied continuation")
 val result=readBytes(dest,n*2,offset=oo);expectCondition(result.contentEquals(encoded),"Every sample equals exact accumulation, one rounding and saturation")
 val after=readBytes("source_guard",size);for(i in before.indices)if(root+i !in out until out+n*2)expectCondition(before[i]==after[i],"Only logical output changes")
 for(a in history.snapshot())expectCondition(n>0 && (0 until a.length).all{val p=a.address+it;if(a.write)p in out until out+n*2 else p in input until input+2*t.input.size || p in coefficients until coefficients+16 || p in out until out+n*2},"Every caller-data access fits the selected slices; no access for zero outputs")
 history.close();avoided.close();return signed16(result)
}
for(n in 0..32)for(variant in 0..3)case("decimate count=$n variant=$variant",instructionLimit=100000) {
 val random=Random(21162026L+n*97+variant)
 val t=when(variant) {
  0->randomTrial(n,random)
  1->FirTrial(IntArray(if(n==0)7 else 2*n+6){if(it==7)16000 else if(it==9)-8000 else 0},intArrayOf(1024,2048,3072,4096,5120,6144,7168,8192),n)
  2->FirTrial(IntArray(if(n==0)7 else 2*n+6){-32768},IntArray(8){-32768},n)
  else->FirTrial(IntArray(if(n==0)7 else 2*n+6){if(it%2==0)-32768 else 32767},IntArray(8){if(it%2==0)32767 else -32768},n)
 }
 checkFir(t,n*4+variant);runToExit();expectOutput("")
}
val boundaries=listOf(16383,16384,16385,-16383,-16384,-16385)
for((i,c)in boundaries.withIndex())case("signed rounding $c",instructionLimit=100000){
 checkFir(FirTrial(IntArray(8){if(it==0)1 else 0},IntArray(8){if(it==0)c else 0},1),i+1);runToExit();expectOutput("")
}
for((i,trial)in listOf(
 FirTrial(intArrayOf(32767,1,1,0,0,0,0,0),intArrayOf(32767,32767,1,0,0,0,0,0),1),
 FirTrial(intArrayOf(-32768,3,0,0,0,0,0,0),intArrayOf(32767,-16384,0,0,0,0,0,0),1),
 FirTrial(IntArray(8){1},IntArray(8){16384},1)
).withIndex())case("round once and clip after rounding $i",instructionLimit=100000){checkFir(trial,i+3);runToExit();expectOutput("")}
case("default caller publishes eight outputs",instructionLimit=100000){
 val original=readBytes("source",192);val coefficients=readBytes("coeff",64)
 runUntil("decimate_ready");expectRegister("v0",8);expectRegister("v1",0);expectWord("processed",99);expectWord("clipped",99)
 for(i in 0..7){val v=if(i<4)2000 else 0;expectBytes("output",v and 255,v ushr 8,offset=i*2)}
 expectCondition(readBytes("source",192).contentEquals(original)&&readBytes("coeff",64).contentEquals(coefficients),"Caller inputs immutable")
 runUntil("results_ready");expectWord("processed",8);expectWord("clipped",0);runToExit();expectOutput("")
}
