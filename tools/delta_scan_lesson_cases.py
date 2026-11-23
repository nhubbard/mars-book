"""Retain the complete public read-only delta scanner corpus as bounded profiles."""
import argparse
import json
from pathlib import Path
from function_lesson_cases import bits
from partition_lesson_cases import word_bytes

WIRE=[208,15,4,1,0,238,168,2]
CONTROL={'stream_guard':0x13579bdf,'stream_end':0x2468ace0,'byte_count':8,'reading_limit':32,
         'input_used':-1,'reading_count':-1,'last_reading':-1,'status':-1,'caller_end':0x31415926}


def groups(value):
    assert 0<=value<=8589934591
    out=[]
    while True:
        digit=value&127;value>>=7;out.append(digit|(128 if value else 0))
        if not value:return out


def encode(readings):
    predecessor=0;wire=[]
    for value in readings:
        delta=value-predecessor;wire+=groups(2*delta if delta>=0 else -2*delta-1);predecessor=value
    return wire


def decode(wire,limit):
    values=[];committed=0;predecessor=0
    while committed<len(wire):
        if len(values)==limit:return committed,values,1
        candidate=wire[committed:committed+5]
        terminal=next((index for index,byte in enumerate(candidate) if byte<128),None)
        if terminal is None:return committed,values,2
        item=candidate[:terminal+1]
        if (len(item)==5 and item[-1]>=32) or (len(item)>1 and item[-1]==0):return committed,values,2
        unsigned=0
        for byte in reversed(item):unsigned=unsigned*128+(byte&127)
        delta=unsigned//2 if unsigned%2==0 else -(unsigned+1)//2
        value=predecessor+delta
        if not -2147483648<=value<=2147483647:return committed,values,3
        values.append(value);predecessor=value;committed+=len(item)
    return committed,values,0


def trials():
    sequences=[[],[0],[-1],[1000,1002,1001,1001,20000],[-2147483648,2147483647,-2147483648],
               [1000]*32,list(range(-16,16)),[1000000 if i==16 else 0 for i in range(32)]]
    rows=[dict(name=f'scan sequence {j} limit={limit}',wire=encode(values),limit=limit,seed=j*33+limit,group='sequences')
          for j,values in enumerate(sequences) for limit in range(33)]
    rejected=[[128],[128,0],[129,0],[128,128,128,128,32],[128,128,128,128,128],
              [255,255,255,255,31],[254,255,255,255,15,2],[2,128,0]]
    rows += [dict(name=f'scan rejected candidate {j} limit={limit}',wire=wire,limit=limit,seed=j*4+limit,group='rejected')
             for j,wire in enumerate(rejected) for limit in range(4)]
    return rows


def construction(trial):
    seed=trial['seed'];offset=seed%32;arena=[(i*31+seed*17+193)&255 for i in range(192)]
    arena[offset:offset+len(trial['wire'])]=trial['wire'];used,values,status=decode(trial['wire'],trial['limit'])
    return dict(offset=offset,arena=arena,inputUsed=used,values=values,status=status)


def memory(arena):
    return [dict(label='stream',bytes=bytes(arena).hex())]+[dict(label=name,bytes=word_bytes([value])) for name,value in CONTROL.items()]


def saved():
    state={**{f's{i}':bits(0x56789abc00000000+i*509) for i in range(8)},'fp':bits(0x2345678900000055)}
    return {**state,'sp':bits(0x7fff0000),'v0':bits(0x12345678),'v1':bits(0x23456789)},list(state)


def policy(offset,length):
    return {'from':dict(label='stream_guard'),'until':dict(label='caller_end',offset=4),
            'readable':[dict(at=dict(label='stream',offset=offset),length=length)],'writable':[]}


def invocation(wire,limit,offset,seed,initial):
    used,values,status=decode(wire,limit)
    return dict(entry='scan_delta',returnTo='function_return' if seed%2==0 else 'done',
                arguments={'a1':bits(len(wire)),'a2':bits(limit),'a3':bits(99)},argumentAddresses={'a0':dict(label='stream',offset=offset)},
                expectedRegisters={'v0':bits(used),'v1':bits(len(values)),'a2':bits(values[-1] if values else 0),'a3':bits(status)},
                memory=initial,accessPolicies=[policy(offset,len(wire) if limit>0 else 0)])


def definition(trial):
    row=construction(trial);initial=memory(row['arena']);registers,preserved=saved()
    call=invocation(trial['wire'],trial['limit'],row['offset'],trial['seed'],initial)
    return dict(name=trial['name'],instructionLimit=1000000,calls=[call],initialMemory=initial,initialRegisters=registers,
                preservedRegisters=preserved,stackAlignment=16,callerStackBytes=32,
                noExecution=[{'from':'function_return','until':'done'}],accessPolicies=call['accessPolicies'])


def caller_case():
    case=definition(trials()[0]);case['name']='default scanner publishes last reading'
    initial=memory(WIRE+[165]*184);final=[dict(row) for row in initial]
    for row in final:
        if row['label'] in ['input_used','reading_count','last_reading','status']:
            row['bytes']=word_bytes([{'input_used':8,'reading_count':5,'last_reading':20000,'status':0}[row['label']]])
    read=[dict(at=dict(label=name),length=length) for name,length in [('stream',8),('byte_count',4),('reading_limit',4)]]
    write=[dict(at=dict(label='input_used'),length=16)];p=policy(0,8);p.update(readable=read,writable=write)
    expected={'v0':bits(8),'v1':bits(5),'a2':bits(20000),'a3':bits(0)}
    case.update(initialMemory=initial,accessPolicies=[p],calls=[
        dict(entry='main',returnTo='scan_ready',arguments={},expectedRegisters=expected,memory=initial,accessPolicies=[{**p,'writable':[]}]),
        dict(entry='scan_ready',returnTo='results_ready',arguments={},expectedRegisters=expected,memory=final,accessPolicies=[{**p,'readable':[]}])])
    return case


def reuse_case():
    case=definition(trials()[0]);case['name']='independent calls reset predecessor counts and empty-state results'
    selected=[([2,128,0],32,3),(WIRE,32,41),([128,0],0,83),(encode([-1,0]),32,123),([],32,171)]
    arena=[(i*19+107)&255 for i in range(192)]
    for wire,_,offset in selected:arena[offset:offset+len(wire)]=wire
    initial=memory(arena);calls=[invocation(wire,limit,offset,index,initial) for index,(wire,limit,offset) in enumerate(selected)]
    case.update(initialMemory=initial,calls=calls,accessPolicies=[{**policy(0,0),'readable':[window for call in calls for window in call['accessPolicies'][0]['readable']]}])
    return case


def corpus():
    cases={'sequences':[],'rejected':[],'state':[caller_case(),reuse_case()]}
    for trial in trials():cases[trial['group']].append(definition(trial))
    return cases


def profiles():
    return {f'{group}-{start//10:02d}':dict(formatVersion=9,cases=rows[start:start+10])
            for group,rows in corpus().items() for start in range(0,len(rows),10)}


def build(directory,check=False):
    directory=Path(directory);files={f'delta-scan-{name}.cases.json':json.dumps(profile,separators=(',',':'))+'\n' for name,profile in profiles().items()}
    files['caller-exit.cases.json']=json.dumps(dict(formatVersion=1,cases=[dict(name='silent scanner caller exit',input='',stdout='',instructionLimit=1000000)]),indent=2)+'\n'
    if not check:directory.mkdir(parents=True,exist_ok=True)
    for name,text in files.items():
        path=directory/name
        if check:
            if not path.is_file() or path.read_text()!=text:raise ValueError(f'Stale scanner profile: {path}')
        else:path.write_text(text)
    return files


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('directory',type=Path);parser.add_argument('--check',action='store_true');args=parser.parse_args();build(args.directory,args.check)
