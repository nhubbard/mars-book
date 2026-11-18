import edu.missouristate.mars.cli.grading.AssemblyCase

data class Shape(val width:Int,val height:Int,val stride:Int)
data class Trial(val frame:Shape,val sprite:Shape,val x:Int,val y:Int,val key:Int,val salt:Int,val identical:Boolean=false)
fun AssemblyCase.putPointer(label:String,address:Long) {
    setWord(label,address.toInt());setWord(label,(address ushr 32).toInt(),offset=4)
}
val shapes=listOf(Shape(0,0,0),Shape(0,8,0),Shape(8,0,16),Shape(1,1,1),Shape(3,5,7),Shape(5,4,7),Shape(8,6,16),Shape(8,8,8))
val keys=listOf(0,-1,0x00ff00ff,0x01ff00ff,Int.MIN_VALUE)
val trials=mutableListOf<Trial>()
for(frame in shapes)for(sprite in shapes) {
    val positions=listOf(-sprite.width to -sprite.height,-1 to -1,0 to 0,(frame.width-1) to (frame.height-1),frame.width to frame.height,Int.MIN_VALUE to Int.MAX_VALUE,Int.MAX_VALUE to Int.MIN_VALUE,1 to -1,-1 to 1)
    for((x,y) in positions){val i=trials.size;trials+=Trial(frame,sprite,x,y,keys[i%keys.size],i)}
}
for(shape in shapes.filter{it.width>0 && it.height>0})trials+=Trial(shape,shape,0,0,0x00ff00ff,3,true)
for((index,t) in trials.withIndex())case("blit $index ${t.frame} ${t.sprite} ${t.x}/${t.y}",instructionLimit=50000) {
    val frame=if(index%3==0)"frame_b" else "frame_a";val source=if(index%4==0)"sprite_b" else "sprite_a"
    val fd=if(index%2==0)"frame_desc_a" else "frame_desc_b";val sd=if(index%3==0)"sprite_desc_b" else "sprite_desc_a"
    val frameOffset=index%8;val sourceOffset=(index*3)%8
    val frameBase=readDataAddress(frame)+frameOffset*4;val sourceBase=readDataAddress(source)+sourceOffset*4
    val input=MutableList(128){0x55667788 xor (it*0x010103+t.salt*17)}
    val output=MutableList(128){0x20304050 xor (it*0x010103+t.salt*31)}
    val palette=listOf(0,0x00ff00ff,0x01ff00ff,0x00ff0000,0x0000ff00,0x000000ff,Int.MIN_VALUE,-1)
    val sourceRead=mutableSetOf<Long>();val frameRead=mutableSetOf<Long>()
    for(y in 0 until t.sprite.height)for(x in 0 until t.sprite.width) {
        val value=when(t.salt%4){0->t.key;1->t.key xor 0x01000000;2->if((x+y)%2==0)t.key else t.key xor 0x01000000;else->palette[(x+y*3+t.salt)%palette.size]}
        input[sourceOffset+y*t.sprite.stride+x]=value
        for(b in 0..3)sourceRead+=sourceBase+(y*t.sprite.stride+x)*4+b
    }
    for(y in 0 until t.frame.height)for(x in 0 until t.frame.width)for(b in 0..3)frameRead+=frameBase+(y*t.frame.stride+x)*4+b
    if(t.identical)for(sy in 0 until t.sprite.height)for(sx in 0 until t.sprite.width) {
        val dx=t.x.toLong()+sx;val dy=t.y.toLong()+sy
        if(dx in 0L until t.frame.width.toLong() && dy in 0L until t.frame.height.toLong())output[frameOffset+dy.toInt()*t.frame.stride+dx.toInt()]=input[sourceOffset+sy*t.sprite.stride+sx]
    }
    for(i in 0 until 128){setWord(source,input[i],offset=i*4);setWord(frame,output[i],offset=i*4)}
    for((descriptor,shape,base) in listOf(Triple(fd,t.frame,frameBase),Triple(sd,t.sprite,sourceBase))) {
        putPointer(descriptor,base);setWord(descriptor,shape.width,offset=8);setWord(descriptor,shape.height,offset=12);setWord(descriptor,shape.stride,offset=16)
    }
    setWord(sd,t.key,offset=20)
    var visible=0;var copied=0;val writable=mutableSetOf<Long>()
    for(sy in 0 until t.sprite.height)for(sx in 0 until t.sprite.width) {
        val dx=t.x.toLong()+sx;val dy=t.y.toLong()+sy
        if(dx in 0L until t.frame.width.toLong() && dy in 0L until t.frame.height.toLong()) {
            visible++;val value=input[sourceOffset+sy*t.sprite.stride+sx]
            if(value!=t.key) {
                copied++;val target=frameOffset+dy.toInt()*t.frame.stride+dx.toInt();output[target]=value
                for(b in 0..3)writable+=readDataAddress(frame)+target*4+b
            }
        }
    }
    val permittedReads=sourceRead+frameRead+
        (0 until 20).map{readDataAddress(fd)+it}+(0 until 24).map{readDataAddress(sd)+it}
    val data=traceMemoryAccesses("bitmap_origin",(readDataAddress("state_end")+4-readDataAddress("bitmap_origin")).toInt(),maxAccesses=100_000)
    val sourceTrace=traceMemoryAccesses(source+"_guard",520,maxAccesses=100_000)
    val frameTrace=traceMemoryAccesses(if(frame=="frame_a")"bitmap_origin" else "frame_b_guard",520,maxAccesses=100_000)
    val continuation=if(index%2==0)"function_return" else "done";val avoided=traceExecution("function_return","done")
    for(i in 0..7)setRegister("s$i",0x3456789000000000L+i*1013)
    setRegister("fp",0x2345678900000011L);setRegister("sp",0x7fff0000L)
    for(i in 0..9)setRegister("t$i",0x456789ab00000000L+i*17+index)
    setRegisterToDataAddress("a0",fd);setRegisterToDataAddress("a1",sd);setRegister("a2",t.x);setRegister("a3",t.y)
    callFunction("blit_sprite",continuation);expectRegister("v0",copied);expectRegister("v1",visible)
    fun inspect() {
        for(i in 0 until 128){expectWord(source,input[i],offset=i*4);expectWord(frame,output[i],offset=i*4)}
        for(a in data.snapshot().filter{!it.write})expectCondition((0 until a.length).all{a.address+it in permittedReads},"Caller-data reads belong to the selected descriptors or logical pixel cells")
        for(a in data.snapshot().filter{it.write})expectCondition((0 until a.length).all{a.address+it in writable},"Every store byte belongs to a visible opaque destination pixel")
        for(a in sourceTrace.snapshot().filter{!it.write})expectCondition((0 until a.length).all{a.address+it in sourceRead},"Source reads stay in logical pixels, not guards or row padding")
        for(a in frameTrace.snapshot().filter{!it.write})expectCondition((0 until a.length).all{a.address+it in frameRead},"Destination reads stay in logical pixels, not guards or row padding")
    }
    inspect();expectCondition(avoided.snapshot().instructions==0L,"Return through the supplied continuation")
    for(i in 0..7)expectRegister("s$i",0x3456789000000000L+i*1013)
    expectRegister("fp",0x2345678900000011L);expectRegister("sp",0x7fff0000L)
    runToExit();inspect();expectOutput("")
}
