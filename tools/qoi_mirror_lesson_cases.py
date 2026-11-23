"""Preserve the original bounded QOI reflection cases as native function data.

The strict published host decoder supplies expected RGBA. This module authors
finite checks and does not execute or recognize an assembly implementation.
"""
import argparse
import json
from pathlib import Path
import struct
from revision_qoi_format import Image, decode, END
from function_lesson_cases import bits

ARENAS = {'input_path':128,'output_path':128,'source':20544,'metadata':64,
          'framebuffer':16448,'destination':20544,'status':4,'count':8,'mode':4,
          'byte_count':8,'pixel_capacity':8,'dimensions':8,'byte_capacity':8}
TRACE = bytes.fromhex('716f696600000008000000010400c0feff0000c0357fa597ff07060580040000000000000001')
CROSSING_RUN = bytes.fromhex('716f696600000007000000090400c0feff0000fbff0000ffff0000000000000001')
INITIAL_INDEX = bytes.fromhex('716f696600000001000000010400000000000000000001')
CACHE_POISON = bytes.fromhex('716f69660000000200000001040000ff010000170000000000000001')
MIN_LONG = -(1 << 63)
MASK = (1 << 64) - 1


def raster(w, h, channels=4, colorspace=0):
    return Image(w,h,bytes((n*53+n//7*19)&255 for n in range(4*w*h)),channels,colorspace)


def literal(image):
    return b'qoif'+struct.pack('>IIBB',image.width,image.height,image.channels,image.colorspace)+b''.join(
        b'\xff'+image.rgba[n:n+4] for n in range(0,len(image.rgba),4))+END


def reflected_words(image):
    rgba=image.rgba;out=bytearray()
    for y in range(image.height):
        for x in range(image.width-1,-1,-1):
            r,g,b,a=rgba[4*(y*image.width+x):4*(y*image.width+x)+4]
            out.extend((b,g,r,a))
    return bytes(out)


def trial(name,data,capacity=4096,offset=3,output_offset=8,limit=100000,group='chunks'):
    return dict(name=name,data=data,capacity=capacity,selected=len(data),offset=offset,
                outputOffset=output_offset,instructionLimit=limit,group=group,kind='memory')


def trials():
    rows=[]
    for w in [1,2,3,7,8,9,31,32,63,64]:
        for h in [1,2,3,7,64]:
            for cap in [0,w*h,MIN_LONG]:
                rows.append(trial(f'mirror {w} by {h} capacity {cap}',literal(raster(w,h)),cap,
                                  (w+h)%32,((w+h)%8)*4,6000000,'geometry'))
    for cap in [0,7,8,MIN_LONG]:rows.append(trial(f'all six chunks mirror capacity {cap}',TRACE,cap))
    rows.append(trial('run crosses output rows',CROSSING_RUN))
    for channels in [3,4]:
        for space in [0,1]:rows.append(trial(f'flags {channels} {space} retain alpha',literal(raster(8,2,channels,space))))
    for offset in range(32):rows.append(trial(f'unaligned stream {offset} aligned output',TRACE,4096,offset,(offset%8)*4,6000000,'alignment'))
    malformed=[TRACE[:37],TRACE+b'\0',TRACE[:-1]+b'\0',CROSSING_RUN[:14]+b'\xfd'+CROSSING_RUN[15:]]
    for data in malformed:
        total=sum(n if n<128 else n-256 for n in data)
        rows.append(trial(f'malformed {len(data)} {total}',data,0,group='malformed'))
    for count in [0,1,22,20503,0x100000000,MIN_LONG,-1]:
        rows.append(dict(name=f'rejected count {count} before pointers',kind='count',selected=count,
                         instructionLimit=100000,group='counts'))
    rows.append(dict(name='ordinary mirrored caller',kind='caller',instructionLimit=100000,group='caller'))
    rows.append(trial('initial INDEX zero reconstructs transparent black',INITIAL_INDEX,group='initial-state'))
    rows.append(trial('validation cache cannot seed the emitting pass',CACHE_POISON,group='initial-state'))
    return rows


def preserved():
    return {**{f's{n}':bits(0x2345678900000000+271*n) for n in range(8)},'fp':bits(0x2345678900000000+271*8)}


def setup(row):
    memory={label:bytes([165])*size for label,size in ARENAS.items()}
    if row['kind']=='memory':
        source=bytearray(memory['source']);at=row['offset'];source[at:at+len(row['data'])]=row['data'];memory['source']=bytes(source)
    memory['workspace']=bytes([165])*8192
    return memory


def outcome(row):
    if row['kind']=='count':return dict(status=2 if row['selected']&MASK>20502 else 1,count=0,result=b'')
    try:image=decode(row['data'])
    except ValueError:image=None
    status=1 if image is None else 2 if row['capacity']&MASK<image.width*image.height else 0
    return dict(status=status,count=0 if status else (image.height<<32)|image.width,
                result=b'' if status else reflected_words(image))


def window(label,offset,length):return dict(at=dict(label=label,offset=offset),length=length)


def definition(row):
    if row['kind']=='caller':
        return dict(name=row['name'],instructionLimit=row['instructionLimit'],callerStackBytes=0,
                    initialRegisterAddresses={'sp':dict(label='workspace',offset=8192)},
                    calls=[dict(entry='main',returnTo='mirrored_return',arguments={},
                                expectedRegisters={'v0':bits(0),'v1':bits(0x100000008)},
                                memory=[dict(label='framebuffer',bytes=reflected_words(decode(TRACE)).hex())])],
                    exit=dict(code=0,memory=[dict(label='status',bytes=struct.pack('<i',0).hex()),
                                            dict(label='count',bytes=struct.pack('<Q',0x100000008).hex())]))
    result=outcome(row);saved=preserved()
    call=dict(entry='qoi_decode_mirrored',returnTo='function_return',
              arguments={'a1':bits(row['selected']),'a3':bits(row.get('capacity',0))},
              expectedRegisters={'v0':bits(result['status']),'v1':bits(result['count'])},
              expectedAddresses={'sp':dict(label='workspace',offset=8192)})
    if row['kind']=='memory':
        call['argumentAddresses']={'a0':dict(label='source',offset=row['offset']),
                                   'a2':dict(label='framebuffer',offset=row['outputOffset'])}
        writable=[] if result['status'] else [window('framebuffer',row['outputOffset'],len(result['result']))]
        readable=[window('source',row['offset'],len(row['data']))]+writable
        call['accessPolicies']=[dict(from_=dict(label=label),until=dict(label=label,offset=size),
                                    readable=readable,writable=writable,maxAccesses=200000) for label,size in ARENAS.items()]
        for policy in call['accessPolicies']:policy['from']=policy.pop('from_')
        call['accessHistory']=dict(regions=[window(label,0,size) for label,size in ARENAS.items()],maxAccesses=200000)
        if not result['status']:call['memory']=[dict(label='framebuffer',offset=row['outputOffset'],bytes=result['result'].hex())]
    else:call['arguments'].update(a0=bits(1),a2=bits(1))
    return dict(name=row['name'],instructionLimit=row['instructionLimit'],initialRegisters=saved,
                initialRegisterAddresses={'sp':dict(label='workspace',offset=8192)},preservedRegisters=list(saved),
                callerStackBytes=0,stackAlignment=16,
                initialMemory=[dict(label=label,bytes=data.hex()) for label,data in setup(row).items()],calls=[call])


def profiles():
    groups={}
    for row in trials():groups.setdefault(row['group'],[]).append(definition(row))
    out={}
    for group,cases in groups.items():
        batch=[];part=0
        for case in cases:
            candidate=batch+[case];value=dict(formatVersion=14,cases=candidate)
            if batch and (sum(c['instructionLimit'] for c in candidate)>10000000 or len(json.dumps(value,separators=(',',':')).encode())>1024*1024):
                out[f'qoi-mirror-{group}-{part:03d}.cases.json']=dict(formatVersion=14,cases=batch);batch=[];part+=1
            batch.append(case)
        if batch:out[f'qoi-mirror-{group}-{part:03d}.cases.json']=dict(formatVersion=14,cases=batch)
    return out


def build(directory,check=False):
    directory=Path(directory)
    if not check:directory.mkdir(parents=True,exist_ok=True)
    files={name:json.dumps(profile,separators=(',',':'))+'\n' for name,profile in profiles().items()}
    for name,text in files.items():
        path=directory/name
        if check:
            if not path.is_file() or path.read_text()!=text:raise ValueError(f'Stale QOI profile: {path}')
        else:path.write_text(text)
    return files


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('directory',type=Path);parser.add_argument('--check',action='store_true');args=parser.parse_args();build(args.directory,args.check)
