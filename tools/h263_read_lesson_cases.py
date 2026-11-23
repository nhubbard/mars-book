"""Exact native inputs and ownership contracts for the 592 public MSB-first reader cases.

The file defines data and expected observations, never an assembly implementation.
"""
import argparse
import json
from pathlib import Path
from function_lesson_cases import bits

INPUT_BYTES=1048608
INVALID=[(-1,0,0,0),(1048577,8,0,1),(0x100000002,12,3,9),
         (2,17,0,1),(2,-1,0,1),(2,0x10000000c,3,9),
         (2,12,13,0),(2,12,-1,0),(2,12,0x100000003,1),
         (2,12,3,33),(2,12,3,-1),(2,12,3,0x100000009)]


def address(label,offset=0):return dict(label=label,offset=offset)
def window(label,offset,length):return dict(at=address(label,offset),length=length)
def native(value,little):return (value&((1<<64)-1)).to_bytes(8,'little' if little else 'big')
def memory(label,raw,offset=0):return dict(label=label,offset=offset,bytes=bytes(raw).hex())
def input_window(offset,length):
    return window('reader_bytes_tail',offset-1048544,length) if offset>1048576 else window('reader_bytes',offset,length)


def pointer(offset):return dict(at=address('reader_context'),value=address('reader_bytes',offset))
def value(at,pattern):return [165,255,0,(at*73+19)&255,128 if at%2==0 else 1][pattern%5]


def invocation(capacity,valid,cursor,width,little,pattern=0,bad='',reuse=False):
    metadata=0<=capacity<=1048576 and 0<=valid<=8*capacity and 0<=cursor<=valid and 0<=width<=32 and not bad
    success=metadata and width<=valid-cursor
    source_offset=0 if reuse else 1+pattern%7
    first_byte=cursor//8 if metadata else 0
    zone=max(0,min(1048544,source_offset+first_byte-16))//4*4
    raw=bytes([186,53]+[0]*62) if reuse else bytes(value(zone+i,pattern) for i in range(64))
    descriptor=(native(-1,little) if bad=='buffer-wrap' else bytes(8))+b''.join(native(v,little) for v in [capacity,valid,cursor])+bytes([165])*16
    setup=[] if reuse else [memory('reader_bytes',raw,zone),memory('reader_context',descriptor)]
    pointers=[] if reuse or bad=='buffer-wrap' else [pointer(source_offset)]
    arguments=dict(a1=bits(width));addresses={}
    if bad=='context-wrap':arguments['a0']=bits(-16)
    else:addresses['a0']=address('reader_context',4 if bad=='context-align' else 0)
    expected=0
    if success:
        for bit in range(cursor,cursor+width):
            sample=raw[bit//8] if reuse else value(source_offset+bit//8,pattern)
            expected=(expected<<1)|((sample>>(7-bit%8))&1)
    readable=[] if bad in ['context-align','context-wrap'] else [window('reader_context',0,32)]
    input_reads=[input_window(source_offset+cursor//8,(cursor+width+7)//8-cursor//8)] if success and width else []
    writable=[window('reader_context',24,8)] if success and width else []
    observed=([window('reader_bytes',0 if reuse else zone,64)] if reuse or zone!=1048544 else [])+[window('reader_bytes_tail',0,64),window('reader_context',0,48),window('reader_status',0,8),window('reader_value',0,8)]
    return dict(entry='h263_read_bits',returnTo='reader_function_return',arguments=arguments,argumentAddresses=addresses,
                expectedRegisters=dict(v0=bits(0 if success else 2 if metadata else 1),v1=bits(expected)),
                setupMemory=setup,setupAddressMemory=pointers,
                memory=[memory('reader_context',native(cursor+width,little),24)] if success and width else [],
                callerAccess=dict(objects=observed,readable=readable+input_reads,writable=writable,maxAccesses=2000),
                accessPolicies=[dict(from_=address('reader_bytes'),until=address('reader_bytes',1048576),readable=readable+input_reads,writable=writable,maxAccesses=2000)])


def normalized(call):
    # JSON reserves no keyword, but Python does; use the native key at serialization.
    for policy in call['accessPolicies']:policy['from']=policy.pop('from_')
    return call


def case(name,calls,little):
    names=[f's{i}' for i in range(8)]+['fp','gp']
    return dict(name=name,calls=[normalized(c) for c in calls],instructionLimit=100000,byteOrder='little' if little else 'big',
                initialRegisters={n:bits(0x3141592600000000+977*i) for i,n in enumerate(names)},preservedRegisters=names,
                initialRegisterAddresses=dict(sp=address('reader_stack',8192)),stackAlignment=16,callerStackBytes=0)


def cases():
    rows=[]
    for little in [False,True]:
        for width in range(33):
            for phase in range(8):
                cursor=8+phase;valid=cursor+width+width%3;capacity=(valid+7)//8
                rows.append(case(f'reader width={width} phase={phase} little={str(little).lower()}',[invocation(capacity,valid,cursor,width,little,(width+phase)%5)],little))
    for little in [False,True]:
        for pattern in range(5):rows.append(case(f'unsigned full field pattern={pattern} little={str(little).lower()}',[invocation(8,64,7,32,little,pattern)],little))
    for little in [False,True]:
        for width in [0,1,7,32]:
            for name,args in [('zero storage',(0,0,0,width)),('last available bit',(2,12,12,width))]:
                rows.append(case(f'{name} width={width} little={str(little).lower()}',[invocation(*args,little,0 if name=='zero storage' else 3)],little))
    for little in [False,True]:
        for args in INVALID:rows.append(case('invalid reader '+' '.join(str(v) for v in args)+f' little={str(little).lower()}',[invocation(*args,little,4)],little))
    for little in [False,True]:
        for bad in ['context-align','context-wrap','buffer-wrap']:rows.append(case(f'invalid reader {bad} little={str(little).lower()}',[invocation(2,12,3,0,little,3,bad)],little))
    for little in [False,True]:
        for cursor in [8388576,8388607,8388608]:rows.append(case(f'maximum capacity cursor={cursor} little={str(little).lower()}',[invocation(1048576,8388608,cursor,32 if cursor==8388576 else 1 if cursor==8388607 else 0,little,1)],little))
    for little in [False,True]:
        calls=[];cursor=0
        for width,status in [(3,0),(9,0),(1,2),(0,0)]:
            call=invocation(2,12,cursor,width,little,reuse=True)
            if not calls:
                call['setupMemory']=[memory('reader_bytes',bytes([186,53]+[0]*62)),memory('reader_context',bytes(8)+native(2,little)+native(12,little)+native(0,little)+bytes([165])*16)]
                call['setupAddressMemory']=[pointer(0)]
            calls.append(call)
            if status==0:cursor+=width
        rows.append(case(f'success, failure and empty read share one cursor little={str(little).lower()}',calls,little))
    return rows


def profiles():
    groups={f'fields-{order}-{part}':[] for order in ['big','little'] for part in ['short','wide']}
    groups.update(boundaries=[],metadata=[],capacity=[],reuse=[])
    for row in cases():
        name=row['name']
        if name.startswith('reader width='):
            width=int(name.split()[1].split('=')[1]);key='fields-'+row['byteOrder']+('-short' if width<16 else '-wide')
        else:key='metadata' if name.startswith('invalid reader') else 'capacity' if name.startswith('maximum capacity') else 'reuse' if name.startswith('success, failure') else 'boundaries'
        groups[key].append(row)
    return {name+'.json':dict(formatVersion=22,cases=rows) for name,rows in groups.items()}


def build(directory,check=False):
    root=Path(directory)
    if not check:root.mkdir(parents=True,exist_ok=True)
    result=profiles()
    for name,data in result.items():
        text=json.dumps(data,separators=(',',':'))+'\n';path=root/name
        if check:
            if not path.is_file() or path.read_text()!=text:raise ValueError(f'Stale reader profile: {path}')
        else:path.write_text(text)
    return result


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('directory',type=Path);parser.add_argument('--check',action='store_true');args=parser.parse_args();r=build(args.directory,args.check);print(len(r),'profiles;',sum(len(v['cases']) for v in r.values()),'cases')
