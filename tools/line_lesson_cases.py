"""Retain the complete public binary-delimiter corpus as bounded native function data."""
import argparse
import json
from pathlib import Path

from function_lesson_cases import bits, saved_values
from partition_lesson_cases import word_bytes

GUARDS=('line_a_guard','line_b_guard','state_end')


def trials():
    result=[]
    for length in (0,1,2,3,7,31,32):
        data=[(n*129+65)&255 for n in range(length)]+[10,66,255]
        result += [dict(name=f'prefix {length}/{n}',data=data,count=n,offset=n%8,group='bounds') for n in range(length+2)]
        result += [dict(name=f'suffix {length}',data=data,count=len(data),offset=7,group='bounds')]
    result += [dict(name=f'one byte {byte}',data=[byte,10,99],count=3,offset=byte%8,group='bytes') for byte in range(256)]
    result += [dict(name=f'no delimiter n{n}',data=[65]*256,count=n,offset=0,group='bounds') for n in (0,1,31,32,33,34,80,256)]
    result += [dict(name=f'limit {length} offset{offset}',data=[65]*length+[10],count=length+1,offset=offset,group='bounds') for length in (31,32,33,64) for offset in range(8)]
    return result


def oracle(data,count):
    prefix=data[:min(count,33)]
    if 10 in prefix:
        length=prefix.index(10)
        return length+1,length,sum(prefix[:length])&255
    return (-1 if count<33 else -2),None,None


def arena(trial):
    prefix=[165]*trial['offset']+trial['data']
    return prefix+[(n*37+19)&255 for n in range(256-len(prefix))]


def function_case(number,trial):
    result,length,total=oracle(trial['data'],trial['count']);target='line_a' if number%2==0 else 'line_b';other='line_b' if target=='line_a' else 'line_a'
    selected=word_bytes([0x5a123400,0x5a123401]);untouched=word_bytes([0x6b234500,0x6b234501]);image=bytes.fromhex(word_bytes([0x13579bdf]))+bytes(arena(trial))+bytes.fromhex(word_bytes([0x2468ace0]))
    initial=[dict(label='input_guard',bytes=image.hex()),dict(label=target,bytes=selected),dict(label=other,bytes=untouched),dict(label='available',bytes=word_bytes([(trial['count']+1)%257]))]
    output=word_bytes([length,total]) if result>0 else selected
    calls=[]
    for continuation in ('line_function_return','line_done'):
        call=dict(entry='read_line',returnTo=continuation,arguments={'a1':bits(trial['count'])},argumentAddresses={'a0':dict(label='input',offset=trial['offset']),'a2':dict(label=target)},expectedRegisters={'v0':bits(result)},memory=[dict(label=target,bytes=output),dict(label=other,bytes=untouched)])
        if result>0:call['expectedAddresses']={'v1':dict(label='input',offset=trial['offset'])}
        else:call['expectedRegisters']['v1']=bits(0)
        calls.append(call)
    kept=saved_values(0x1122334400000000+number*0x01010101)
    permitted=result if result>0 else min(trial['count'],33)
    store_bound=dict(from_=dict(label='input_guard'),until=dict(label='state_end',offset=4))
    store_bound['from']=store_bound.pop('from_')
    if result>0:store_bound.update(permittedFrom=dict(label=target),permittedLength=8)
    protected=[dict(label='input_guard',length=264),dict(label=other,length=8),*[dict(label=label,length=4) for label in (*GUARDS,'available','status')],dict(label='payload_address',length=8)]
    if result<0:protected.append(dict(label=target,length=8))
    return dict(name=trial['name'],instructionLimit=5000,calls=calls,initialMemory=initial,
                initialRegisters={**{r:bits(v) for r,v in kept.items()},'sp':bits(0x7fffeffc-number*4096)},preservedRegisters=list(kept),stackAlignment=16,callerStackBytes=32,protectCallerStack=True,
                noExecution=[{'from':'line_function_return','until':'line_done'}],noStores=protected,accessBounds=[dict(label='input_guard',length=264,permittedOffset=4+trial['offset'],permittedLength=permitted)],storeBounds=[store_bound])


def caller_case():
    return dict(name='complete default binary-line caller',instructionLimit=5000,
                calls=[dict(entry='main',returnTo='line_done',arguments={},expectedRegisters={'v0':bits(4)},expectedAddresses={'v1':dict(label='input')},
                            memory=[dict(label='line_a',bytes=word_bytes([3,193])),dict(label='status',bytes=word_bytes([4]))],
                            addressMemory=[dict(at=dict(label='payload_address'),value=dict(label='input'))])],
                noStores=[dict(label='input_guard',length=264),dict(label='line_b',length=8),*[dict(label=label,length=4) for label in (*GUARDS,'available')]],
                accessBounds=[dict(label='input_guard',length=264,permittedOffset=4,permittedLength=4)],preservedRegisters=list(saved_values(0)),stackAlignment=16,callerStackBytes=32,protectCallerStack=True)


def profiles():
    groups={'bounds':[],'bytes':[]}
    for number,trial in enumerate(trials()):groups[trial['group']].append(function_case(number,trial))
    groups['bounds'].append(function_case(394,dict(name='default zero and high binary payload',data=[65,0,128,10,66],count=5,offset=0)))
    groups['bounds'].append(caller_case())
    return {name:dict(formatVersion=6,cases=cases) for name,cases in groups.items()}


def build(directory,check=False):
    directory=Path(directory)
    files={f'bounded-line-{name}.cases.json':json.dumps(profile,separators=(',',':'))+'\n' for name,profile in profiles().items()}
    files['caller-exit.cases.json']=json.dumps(dict(formatVersion=1,cases=[dict(name='silent normal binary-line caller',input='',stdout='',instructionLimit=5000)]),indent=2)+'\n'
    for name,text in files.items():
        if len(text.encode())>1048576:raise ValueError(f'Native profile exceeds one MiB: {name}')
        path=directory/name
        if check:
            if path.read_text()!=text:raise ValueError(f'Stale native line cases: {path}')
        else:directory.mkdir(parents=True,exist_ok=True);path.write_text(text,encoding='utf-8',newline='\n')
    return files


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--output-directory',type=Path,required=True);parser.add_argument('--check',action='store_true');args=parser.parse_args();build(args.output_directory,args.check)
