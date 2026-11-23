import java.util.Random
import java.math.BigInteger

class EvaluationFailure(val status:Int,val offset:Int):RuntimeException()
class Model(private val bytes:ByteArray) {
    private var at=0
    private val min=BigInteger.valueOf(Int.MIN_VALUE.toLong())
    private val max=BigInteger.valueOf(Int.MAX_VALUE.toLong())
    private fun byte()=bytes[at].toInt() and 255
    private fun space(){while(at<bytes.size && byte() in listOf(9,10,13,32))at++}
    private fun checked(value:BigInteger,where:Int):BigInteger {if(value<min || value>max)throw EvaluationFailure(-2,where);return value}
    private fun primary():BigInteger {
        space();if(at==bytes.size)throw EvaluationFailure(-1,at)
        if(byte()==40){at++;val value=expression();space();if(at==bytes.size || byte()!=41)throw EvaluationFailure(-1,at);at++;return value}
        if(byte() !in 48..57)throw EvaluationFailure(-1,at)
        var value=BigInteger.ZERO
        while(at<bytes.size && byte() in 48..57){value=checked(value*BigInteger.TEN+BigInteger.valueOf((byte()-48).toLong()),at);at++}
        return value
    }
    private fun unary():BigInteger {
        space()
        if(at<bytes.size && byte() in listOf(43,45)){val sign=byte();val position=at++;val value=unary();return if(sign==45)checked(-value,position)else value}
        return primary()
    }
    private fun term():BigInteger {
        var value=unary()
        while(true){space();if(at==bytes.size || byte() !in listOf(42,47))return value
            val op=byte();val position=at++;val right=unary()
            if(op==47 && right==BigInteger.ZERO)throw EvaluationFailure(-3,position)
            value=checked(if(op==42)value*right else value/right,position)
        }
    }
    private fun expression():BigInteger {
        var value=term()
        while(true){space();if(at==bytes.size || byte() !in listOf(43,45))return value
            val op=byte();val position=at++;val right=term()
            value=checked(if(op==43)value+right else value-right,position)
        }
    }
    fun result():Pair<Int,Int> = try {val value=expression();space();if(at!=bytes.size)throw EvaluationFailure(-1,at);0 to value.toInt()}catch(e:EvaluationFailure){e.status to e.offset}
}
val inputs=mutableListOf<ByteArray>()
fun sample(s:String){inputs+=s.encodeToByteArray()}
val known=listOf(
    "18 - 3 * (2 + -4)" to (0 to 24),"1+2*3" to (0 to 7),"(1+2)*3" to (0 to 9),
    "1-2-3" to (0 to -4),"8/3/2" to (0 to 1),"-7/3" to (0 to -2),"7/-3" to (0 to -2),
    "--5" to (0 to 5),"1++2" to (0 to 3),"-2147483648" to (-2 to 10),
    "(-2147483647-1)" to (0 to Int.MIN_VALUE),"(-2147483647-1)/-1" to (-2 to 15),
    "1/(2-2)" to (-3 to 1),"1/(2-2" to (-1 to 6),"2147483647+1?" to (-2 to 10),
    "2147483647+1*?" to (-1 to 13),"1 2" to (-1 to 2),"" to (-1 to 0)
)
for((s,result) in known){check(Model(s.encodeToByteArray()).result()==result){"Bad fixed oracle fixture: $s ${Model(s.encodeToByteArray()).result()} expected $result"};sample(s)}
for(s in listOf("0","00","0000000000000000000000000000000000000000000000000000000000000000","2147483647","2147483648","99999999999","2147483647+1","-2147483647-2","46340*46340","46341*46341","(-2147483647-1)*1","(-2147483647-1)*-1","-(-2147483647-1)","1/0","0/0","1/(3-3)","1/-0","1/(-0)","1/(1/0)","1/0+?","1/0*?","1/0+2147483648","2147483648/0","(2147483647+1)*0","1+","()","(1","1)","1**2","2(3)","1e2","0x10","1.2","+","-"," ","\t\r\n","1\u00002","é","1+é","1\u000b+2","  - ( 2 +3) * + 4 \n"))sample(s)
for(b in 0..255)inputs+=byteArrayOf(b.toByte())
for(sign in listOf("-","+"))for(n in 0..63)sample(sign.repeat(n)+"1")
for(n in 0..31)sample("(".repeat(n)+"1"+")".repeat(n))
for(n in 0..31){sample("1+".repeat(n)+"1");sample("1-".repeat(n)+"1")}
for((index,bytes) in inputs.withIndex())case("expression $index "+bytes.joinToString(""){(it.toInt() and 255).toString(16).padStart(2,'0')},instructionLimit=100000) {
    require(bytes.size<=64)
    val expected=Model(bytes).result()
    val label=if(index%3==0)"input_b" else "input_a";val offset=index%33
    val prepared=ByteArray(96){(it*37+index*17+128).toByte()};bytes.copyInto(prepared,offset)
    for(i in 0 until 24){var word=0;for(j in 0..3)word=word or ((prepared[i*4+j].toInt() and 255) shl (j*8));setWord(label,word,offset=i*4)}
    val whole=(readDataAddress("state_end")+4-readDataAddress("input_a_guard")).toInt()
    val before=readBytes("input_a_guard",whole);val start=readDataAddress(label)+offset
    val memory=traceMemoryAccesses("input_a_guard",whole,maxAccesses=100000)
    for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1013)
    val callerSp=if(index%2==0)0x7ffe8000L else 0x7fff0000L
    setRegister("fp",0x2345678900000011L);setRegister("sp",callerSp)
    for(i in 0..9)setRegister("t$i",0x456789ab00000000L+i*17+index)
    setRegisterToDataAddress("a0",label,offset.toLong());setRegister("a1",bytes.size)
    val avoided=traceExecution("function_return","done")
    callFunction("eval_expr",if(index%2==0)"function_return" else "done")
    expectRegister("v0",expected.first);expectRegister("v1",expected.second)
    expectCondition(avoided.snapshot().instructions==0L,"Return through the supplied continuation")
    for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1013)
    expectRegister("fp",0x2345678900000011L);expectRegister("sp",callerSp)
    fun inspect(){
        expectCondition(readBytes("input_a_guard",whole).contentEquals(before),"Caller data, input and guards remain unchanged")
        for(a in memory.snapshot()){
            expectCondition(!a.write,"Caller data is read-only, even temporarily")
            expectCondition(a.address>=start && a.address+a.length<=start+bytes.size,"Every input read lies wholly inside the supplied byte slice")
        }
    }
    inspect();runToExit();inspect();expectOutput("")
}
