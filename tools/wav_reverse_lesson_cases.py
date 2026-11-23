"""Native data contracts for the 35 public WAV frame-reversal cases.

The six-function worked pipeline remains a complete public example. This model
specifies its frame-order extension without providing an assembly solution.
"""
import argparse
import json
from pathlib import Path
from function_lesson_cases import bits

OBJECTS = dict(input_path=128,output_path=128,source=266272,metadata=64,samples=262176,
               destination=266272,coeff=64,kernel_window=160,kernel_output=160,status=4,
               count=8,clips=8,mode=4,byte_count=8,byte_capacity=8,kernel_count=8)
TRACE = bytes.fromhex('524946464a000000574156454a554e4b03000000010203a5666d74201000000001000200803e000000fa0000040010006461746110000000e02e20d10000803ec0e00000ff7f00806e6f746501000000427f')


def addr(label,offset=0):
    return dict(label=label,offset=offset)


def window(label,offset,length):
    return dict(at=addr(label,offset),length=length)


def little(n,width=4):
    return (n & ((1 << (width * 8)) - 1)).to_bytes(width,'little')


def memory(label,raw,offset=0):
    raw=bytes(raw)
    assert raw
    result=dict(label=label,offset=offset,bytes=raw.hex())
    if len(raw)>1024:
        for period in [1,2,4,8,16,32,64,128,256,512,1024]:
            if len(raw)%period==0 and raw[:period]*(len(raw)//period)==raw:
                result.update(bytes=raw[:period].hex(),repeat=len(raw)//period)
                break
    return result


def expanded(row):
    return bytes.fromhex(row['bytes'])*row.get('repeat',1)


def description(rate,channels,frames):
    return little(rate)+little(channels)+little(frames,8)


def valid(rate,channels,frames):
    return 8000<=rate<=48000 and 1<=channels<=2 and 0<=frames<=65536


def patterned(count,seed):
    return bytes((i*73+(i//2)*11+seed*19)&255 for i in range(count))


def reverse_frames(raw,frame_bytes):
    assert len(raw)%frame_bytes==0
    return b''.join(raw[i:i+frame_bytes] for i in range(len(raw)-frame_bytes,-1,-frame_bytes))


def reversal(rate,channels,frames,alias,seed):
    accepted=valid(rate,channels,frames)
    count=2*channels*frames if accepted else 0
    raw=patterned(count,seed)
    source='source' if seed%2==0 else 'samples'
    destination=source if alias else 'destination'
    start=1+seed%5
    end=start if alias else 7-seed%5
    prefix=8*(seed%5)
    padded=((start+count+3)//4)*4
    setup=[memory(source,bytes([165])*start)]
    if count: setup.append(memory(source,raw,start))
    if padded>start+count: setup.append(memory(source,bytes([165])*(padded-start-count),start+count))
    setup.append(memory('metadata',description(rate,channels,frames),prefix))
    pointers=dict(a1=addr('metadata',prefix))
    arguments={}
    if accepted and frames:
        pointers.update(a0=addr(source,start),a2=addr(destination,end))
    else:
        arguments=dict(a0=bits(1),a2=bits(1))
    reads=[window('metadata',prefix,16)]
    if accepted:
        reads += [window(source,start,count),window(destination,end,count)]
    writes=[window(destination,end,count)] if accepted else []
    results=[memory(destination,reverse_frames(raw,2*channels),end)] if count else []
    return dict(entry='wav_reverse_frames',returnTo='function_return',arguments=arguments,argumentAddresses=pointers,
                expectedRegisters=dict(v0=bits(0 if accepted else 1),v1=bits(frames if accepted else 0)),
                setupMemory=setup,memory=results,
                callerAccess=dict(objects=[window(k,0,n) for k,n in OBJECTS.items()],readable=reads,writable=writes,maxAccesses=1000000))


def case(name,calls,budget=10000000):
    saved={f's{i}':bits(0x3141592600000000+977*i) for i in range(8)} | dict(fp=bits(0x3141592600000000+977*8))
    return dict(name=name,calls=calls,instructionLimit=budget,initialRegisters=saved,
                initialRegisterAddresses=dict(sp=addr('workspace',8192)),preservedRegisters=list(saved),
                stackAlignment=8,callerStackBytes=0)


def canonical(rate,channels,pcm):
    return (b'RIFF'+little(36+len(pcm))+b'WAVEfmt '+little(16)+little(1,2)+little(channels,2)+
            little(rate)+little(2*channels*rate)+little(2*channels,2)+little(16,2)+b'data'+little(len(pcm))+pcm)


def filtered_trace():
    samples=[int.from_bytes(TRACE[i:i+2],'little',signed=True) for i in range(56,72,2)]
    result=[]
    for frame in range(4):
        for channel in range(2):
            total=sum(samples[2*previous+channel]*4096 for previous in range(max(0,frame-7),frame+1))
            value=max(-32768,min(32767,(total+16384)>>15))
            result.append(little(value,2))
    return b''.join(result)


def pipeline():
    output=canonical(16000,2,reverse_frames(filtered_trace(),4))
    call=dict(entry='main',returnTo='results_ready',arguments={},expectedRegisters=dict(v0=bits(0),v1=bits(len(output))),
              expectedFiles={'output.wav':output.hex()})
    return dict(name='reverse the complete filtered WAV pipeline',calls=[call],instructionLimit=10000000,
                initialRegisterAddresses=dict(sp=addr('workspace',8192)),stackAlignment=8,callerStackBytes=0,
                initialMemory=[memory('mode',little(6))],
                fileSystem=dict(files={'input.wav':TRACE.hex(),'output.wav':b'old'.hex()},writablePaths=['output.wav'],maxReadSize=7,maxWriteSize=5,firstDescriptor=19),
                exit=dict(code=0,memory=[memory('status',little(0)),memory('count',little(len(output),8)),memory('clips',little(0,8))]))


def cases():
    result=[]
    for channels in [1,2]:
        for frames in [0,1,2,3,7,65]:
            for alias in [False,True]:
                name=f'reverse {channels} channels {frames} frames alias {str(alias).lower()}'
                result.append(case(name,[reversal(16000,channels,frames,alias,(frames+channels)%19)]))
    for rate,channels,frames in [(7999,1,1),(48001,2,1),(16000,0,0),(16000,3,1),(16000,1,65537),(16000,1,4294967296),(4294967295,1,1),(16000,1,-(1<<63))]:
        result.append(case(f'invalid reverse description {rate} {channels} {frames}',[reversal(rate,channels,frames,False,5)],100000))
    result.append(case('maximum stereo in-place reversal',[reversal(48000,2,65536,True,9)]))
    requests=[(16000,2,65,True,2),(8000,1,0,False,3),(48000,1,3,False,4),(16000,1,-(1<<63),True,5),(16000,2,64,False,6),(16000,2,65,True,7)]
    result.append(case('independent repeated reversals',[reversal(*r) for r in requests]))
    result.append(pipeline())
    return result


def profiles():
    result={};batch=[];budget=0
    for c in cases():
        if batch and budget+c['instructionLimit']>40000000:
            result[f'reverse-{len(result):02d}.json']=dict(formatVersion=20,cases=batch)
            batch=[];budget=0
        batch.append(c);budget+=c['instructionLimit']
    if batch:result[f'reverse-{len(result):02d}.json']=dict(formatVersion=20,cases=batch)
    return result


def build(directory,check=False):
    root=Path(directory)
    if not check:root.mkdir(parents=True,exist_ok=True)
    result=profiles()
    for name,data in result.items():
        path=root/name;text=json.dumps(data,separators=(',',':'))+'\n'
        if check:
            if not path.is_file() or path.read_text()!=text:raise ValueError(f'Stale WAV reversal profile: {path}')
        else:path.write_text(text)
    return result


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('directory',type=Path);parser.add_argument('--check',action='store_true');args=parser.parse_args()
    result=build(args.directory,args.check);print(len(result),'profiles;',sum(len(v['cases']) for v in result.values()),'cases')
