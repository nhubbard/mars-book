import java.util.Random
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class GuestInstruction(val offset:Int,val opcode:Int,val width:Int,val operand:Int)
data class Outcome(val status:Int,val pc:Int,val retired:Int,val fault:Int,val stack:List<Int>)
fun simulate(program:ByteArray,capacity:Int,budget:Int):Outcome {
    val decoded=mutableListOf<GuestInstruction>();var cursor=0
    while(cursor<program.size){
        val opcode=program[cursor].toInt() and 255
        val width=when(opcode){0,16,17,18,19,32,33,34,35->1;1,48,49->2;2->5;else->return Outcome(-1,0,0,cursor,emptyList())}
        if(cursor+width>program.size)return Outcome(-2,0,0,cursor,emptyList())
        val operand=when(opcode){1->program[cursor+1].toInt();2->ByteBuffer.wrap(program,cursor+1,4).order(ByteOrder.LITTLE_ENDIAN).int;48,49->program[cursor+1].toInt() and 255;else->0}
        decoded+=GuestInstruction(cursor,opcode,width,operand);cursor+=width
    }
    val byOffset=decoded.associateBy{it.offset}
    for(i in decoded)if(i.opcode in listOf(48,49) && i.operand !in byOffset)return Outcome(-3,0,0,i.offset,emptyList())
    val stack=mutableListOf<Int>();var pc=0;var retired=0
    fun fault(status:Int)=Outcome(status,pc,retired,pc,stack.toList())
    while(retired<budget){
        val i=byOffset[pc] ?: return fault(-4)
        var next=pc+i.width
        when(i.opcode){
            0->return Outcome(1,next,retired+1,-1,stack.toList())
            1,2->{if(stack.size==capacity)return fault(-6);stack+=i.operand}
            16,17,18,19->{
                if(stack.size<2)return fault(-5)
                val left=BigInteger.valueOf(stack[stack.lastIndex-1].toLong());val right=BigInteger.valueOf(stack.last().toLong())
                if(i.opcode==19 && right==BigInteger.ZERO)return fault(-8)
                val value=when(i.opcode){16->left+right;17->left-right;18->left*right;else->left/right}
                if(value<BigInteger.valueOf(Int.MIN_VALUE.toLong()) || value>BigInteger.valueOf(Int.MAX_VALUE.toLong()))return fault(-7)
                stack.removeAt(stack.lastIndex);stack[stack.lastIndex]=value.toInt()
            }
            32,35->{val required=if(i.opcode==32)1 else 2;if(stack.size<required)return fault(-5);if(stack.size==capacity)return fault(-6);val value=stack[stack.size-required];stack+=value}
            33->{if(stack.isEmpty())return fault(-5);stack.removeAt(stack.lastIndex)}
            34->{if(stack.size<2)return fault(-5);val a=stack[stack.lastIndex];stack[stack.lastIndex]=stack[stack.lastIndex-1];stack[stack.lastIndex-1]=a}
            48->next=i.operand
            49->{if(stack.isEmpty())return fault(-5);val value=stack.removeAt(stack.lastIndex);if(value==0)next=i.operand}
        }
        pc=next;retired++
    }
    return Outcome(0,pc,retired,-1,stack.toList())
}
fun bytes(vararg values:Int)=values.map{it.toByte()}.toByteArray()
fun push(value:Int):ByteArray=bytes(2)+ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
data class Trial(val image:ByteArray,val capacity:Int=8,val fuel:Int=64)
val countdown=bytes(1,0,1,3,32,49,16,34,35,16,34,1,1,17,48,4,33,0)
val programs=mutableListOf<Trial>()
for(fuel in listOf(0,1,2,3,4,11,20,29,31,32,33,64))programs+=Trial(countdown,8,fuel)
programs+=Trial(countdown,2,2);programs+=Trial(countdown,2,3)
programs+=Trial(countdown.copyOf().also{it[6]=6})
programs+=Trial(push(Int.MAX_VALUE)+bytes(1,1,16,0))
programs+=Trial(bytes(1,7,1,0,19,0))
programs+=Trial(bytes(0,255),8,0);programs+=Trial(bytes(48,0),0,64)
programs+=Trial(bytes(),0,0);programs+=Trial(bytes(),0,1)
programs+=Trial(bytes(1,7),8,1);programs+=Trial(bytes(1,7),8,2)
for((index,t) in programs.withIndex())case("caller $index",instructionLimit=100000){
 val expected=simulate(t.image,t.capacity,t.fuel);val prepared=ByteArray(96){170.toByte()};t.image.copyInto(prepared)
 for(i in 0 until 24){var word=0;for(j in 0..3)word=word or ((prepared[i*4+j].toInt() and 255) shl (8*j));setWord("code_a",word,offset=i*4)}
 setWord("length",t.image.size);setWord("fuel",t.fuel);setWord("state_a",t.capacity,offset=16)
 runUntil("vm_call")
 val whole=(readDataAddress("state_end")+4-readDataAddress("code_a_guard")).toInt();val before=readBytes("code_a_guard",whole);val root=readDataAddress("code_a_guard");val sb=readDataAddress("state_a");val stack=readDataAddress("stack_a")
 val allowed=(0 until 16).map{sb+it}.toSet()+(20 until 24).map{sb+it}+(0 until t.capacity*4).map{stack+it}
 val readable=(0 until t.image.size).map{readDataAddress("code_a")+it}.toSet()+(0 until 32).map{sb+it}+(0 until t.capacity*4).map{stack+it}
 val history=traceMemoryAccesses("code_a_guard",whole)
 runUntil("vm_ready");expectRegister("v0",expected.status);expectRegister("v1",expected.retired)
 expectWord("state_a",expected.pc);expectWord("state_a",expected.stack.size,offset=4);expectWord("state_a",expected.retired,offset=8);expectWord("state_a",expected.status,offset=12);expectWord("state_a",expected.fault,offset=20)
 expectWord("last_status",99);expectWord("last_steps",99)
 for(i in expected.stack.indices)expectWord("stack_a",expected.stack[i],offset=i*4)
 val after=readBytes("code_a_guard",whole);for(i in before.indices)if(root+i !in allowed)expectCondition(before[i]==after[i],"Caller data outside VM outputs/workspace remains unchanged")
 for(a in history.snapshot())expectCondition((0 until a.length).all{a.address+it in if(a.write)allowed else readable},"Caller execution respects the selected VM image and workspace")
 history.close();runToExit();expectWord("last_status",expected.status);expectWord("last_steps",expected.retired);expectCondition(readBytes("code_a",96).contentEquals(prepared.map{it.toInt() and 255}.toIntArray()),"The caller preserves its bytecode image");expectOutput("")
}
