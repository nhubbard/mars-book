"""Author native VM trials while keeping guest semantics separate from MIPS execution."""
import argparse
from dataclasses import dataclass
import json
from pathlib import Path

from console_cases import parse as console_parse, render as console_render
from function_lesson_cases import bits
from ring_lesson_cases import address, memory, words, policy
from revision_vm_programs import material

WIDTHS={0:1,1:2,2:5,16:1,17:1,18:1,19:1,32:1,33:1,34:1,35:1,48:2,49:2}
COUNTDOWN=material('countdown')

@dataclass(frozen=True)
class Trial:
    image:bytes
    capacity:int=8
    fuel:int=64

@dataclass(frozen=True)
class Outcome:
    status:int
    pc:int
    retired:int
    fault:int
    stack:tuple


def simulate(image,capacity,budget):
    """Validate starts first, then fetch/decode from bytes at each guest transition."""
    starts=set();offset=0
    while offset<len(image):
        width=WIDTHS.get(image[offset])
        if width is None:return Outcome(-1,0,0,offset,())
        if width>len(image)-offset:return Outcome(-2,0,0,offset,())
        starts.add(offset);offset+=width
    for offset in sorted(starts):
        if image[offset] in (48,49) and image[offset+1] not in starts:return Outcome(-3,0,0,offset,())
    pc=retired=0;stack=[]
    def outcome(status,fault):return Outcome(status,pc,retired,fault,tuple(stack))
    while retired<budget:
        if pc not in starts:return outcome(-4,pc)
        op=image[pc];next_pc=pc+WIDTHS[op]
        if op==0:return Outcome(1,next_pc,retired+1,-1,tuple(stack))
        required=2 if op in (16,17,18,19,34,35) else 1 if op in (32,33,49) else 0
        if len(stack)<required:return outcome(-5,pc)
        if op in (1,2,32,35) and len(stack)==capacity:return outcome(-6,pc)
        if op in (1,2):stack.append(int.from_bytes(image[pc+1:next_pc],'little',signed=True))
        elif op in (16,17,18,19):
            left,right=stack[-2:]
            if op==19 and right==0:return outcome(-8,pc)
            value=left+right if op==16 else left-right if op==17 else left*right if op==18 else (abs(left)//abs(right))*(-1 if (left<0)!=(right<0) else 1)
            if not -(1<<31)<=value<(1<<31):return outcome(-7,pc)
            stack[-2:]=[value]
        elif op==32:stack.append(stack[-1])
        elif op==33:stack.pop()
        elif op==34:stack[-2:]=stack[-2:][::-1]
        elif op==35:stack.append(stack[-2])
        elif op==48:next_pc=image[pc+1]
        elif op==49:
            if stack.pop()==0:next_pc=image[pc+1]
        pc=next_pc;retired+=1
    return outcome(0,-1)


def push(value):return bytes([2])+value.to_bytes(4,'little',signed=True)


KNOWN=(
 (Trial(COUNTDOWN),Outcome(1,18,33,-1,(6,))),
 (Trial(COUNTDOWN,8,4),Outcome(0,7,4,-1,(0,3))),
 (Trial(COUNTDOWN,2),Outcome(-6,4,2,4,(0,3))),
 (Trial(material('bad-target')),Outcome(-3,0,0,5,())),
 (Trial(bytes([0])),Outcome(1,1,1,-1,())),
 (Trial(bytes([0]),0,0),Outcome(0,0,0,-1,())),
 (Trial(b''),Outcome(-4,0,0,0,())),
 (Trial(b'',0,0),Outcome(0,0,0,-1,())),
 (Trial(bytes([48,0]),0,64),Outcome(0,0,64,-1,())),
 (Trial(bytes([0,255]),0,0),Outcome(-1,0,0,1,())),
 (Trial(bytes([48,1,255])),Outcome(-1,0,0,2,())),
 (Trial(bytes([1,1,49,3,0])),Outcome(-3,0,0,2,())),
 (Trial(push((1<<31)-1)+bytes([1,1,16,0])),Outcome(-7,7,2,7,((1<<31)-1,1))),
 (Trial(bytes([1,7,1,0,19,0])),Outcome(-8,4,2,4,(7,0))),
 (Trial(bytes([1,249,1,3,19,0])),Outcome(1,6,4,-1,(-2,))))


def trials():
    rows=[trial for trial,_ in KNOWN]
    for op in range(256):rows += [Trial(bytes([op]),0,0),Trial(bytes([op]),8,1)]
    for capacity in range(9):
        for fuel in (0,1,2,3,4,5,8,16,32,33,34,64):rows.append(Trial(COUNTDOWN,capacity,fuel))
    for target in range(10):rows += [Trial(bytes([48,target,2,255,255,255,127,0])),Trial(bytes([1,1,49,target,0,2,0,0,0,128,0]))]
    for target in (0,2,3,62,63,64,128,255):rows.append(Trial(bytes([48,target])+bytes(62),8,4))
    for cut in range(1,5):rows.append(Trial(push(-(1<<31))[:cut]))
    for index,left in enumerate((0,1,-1,-(1<<31),(1<<31)-1,46340,46341,-46341)):
        for right in (0,1,-1,-(1<<31),(1<<31)-1,46340,46341,-46341):
            for op in range(16,20):rows.append(Trial(push(left)+push(right)+bytes([op,0]),2 if index%2==0 else 8))
    for depth in range(9):
        for capacity in range(9):
            for op in (32,33,34,35):rows.append(Trial(bytes(v for i in range(depth) for v in (1,i+1))+bytes([op,0]),capacity))
    assert len(rows)==1247
    return rows


def programs():
    rows=[Trial(COUNTDOWN,8,fuel) for fuel in (0,1,2,3,4,11,20,29,31,32,33,64)]
    rows += [Trial(COUNTDOWN,2,2),Trial(COUNTDOWN,2,3),Trial(material('bad-target')),Trial(material('add-overflow')),Trial(material('zero-divisor')),Trial(bytes([0,255]),8,0),Trial(bytes([48,0]),0,64),Trial(b'',0,0),Trial(b'',0,1),Trial(bytes([1,7]),8,1),Trial(bytes([1,7]),8,2)]
    assert len(rows)==23
    return rows


def window(label,offset,length):return dict(at=address(label,offset),length=length)


def access(code,code_offset,length,state,stack,stack_offset,capacity):
    writable=[window(state,0,16),window(state,20,4),window(stack,stack_offset*4,capacity*4)]
    readable=[window(code,code_offset,length),window(state,0,32),window(stack,stack_offset*4,capacity*4)]
    return [policy(address('code_a_guard'),address('state_end',4),writable,readable,True)]


def report(state,stack,offset,outcome):
    o=outcome
    return [words(state,[o.pc,len(o.stack),o.retired,o.status]),words(state,[o.fault])|dict(offset=20),*([words(stack,o.stack)|dict(offset=offset*4)] if o.stack else [])]


def base(name,calls,sp=0x7fff0000):
    return dict(name=name,instructionLimit=100000,calls=calls,
        initialRegisters={**{f's{i}':bits(0x3456789000000000+i*1013) for i in range(8)},'fp':bits(0x2345678900000011),'sp':bits(sp)},
        preservedRegisters=[*[f's{i}' for i in range(8)],'fp'],stackAlignment=16,callerStackBytes=0,
        noExecution=[dict(**{'from':'function_return'},until='done')],
        exit=dict(memory=calls[-1]['memory'],accessPolicies=calls[-1]['accessPolicies']))


def call(index,trial,code,stack,state,code_offset,stack_offset,continuation):
    t=trial;o=simulate(t.image,t.capacity,t.fuel);data=[(i*17+index*31+128)&255 for i in range(96)];data[code_offset:code_offset+len(t.image)]=t.image
    initial=[memory(code,data),words(stack,[0x55667788^(index*31+i*1009) for i in range(16)])]
    initial += [words(state,[0x23456789^(offset*103+index)])|dict(offset=offset) for offset in (0,4,8,12,20)];initial += [words(state,[t.capacity])|dict(offset=16)]
    pointer=dict(at=address(state,24),value=address(stack,stack_offset*4))
    return dict(entry='run_vm',returnTo=continuation,arguments={**{f't{i}':bits(0x456789ab00000000+index+i*17) for i in range(10)},'a1':bits(len(t.image)),'a3':bits(t.fuel)},argumentAddresses=dict(a0=address(code,code_offset),a2=address(state)),
        expectedRegisters=dict(v0=bits(o.status),v1=bits(o.retired)),setupMemory=initial,setupAddressMemory=[pointer],
        memory=[memory(code,data),words(state,[t.capacity])|dict(offset=16),*report(state,stack,stack_offset,o)],addressMemory=[pointer],accessPolicies=access(code,code_offset,len(t.image),state,stack,stack_offset,t.capacity))


def kernel_case(index,trial):
    code='code_b' if index%3==0 else 'code_a';stack='stack_b' if index%4==0 else 'stack_a';state='state_b' if index%2==0 else 'state_a'
    return base(f'vm {index} n={len(trial.image)} cap={trial.capacity} fuel={trial.fuel}',[call(index,trial,code,stack,state,index%33,index%9,'function_return' if index%2==0 else 'done')],0x7ffe8000 if index%2==0 else 0x7fff0000)


# Newly authored public replacements, not the ten private rotation sequences.
REPLACEMENTS=((Trial(bytes(18),8,64),Trial(material('bad-target'),8,0),Trial(COUNTDOWN,8,64)),
 (Trial(COUNTDOWN,8,4),Trial(material('zero-divisor')),Trial(bytes([1,7]),8,1)),
 (Trial(material('add-overflow')),Trial(bytes([0]),0,1),Trial(bytes([0,255]),8,0)),
 (Trial(bytes([48,0]),0,64),Trial(bytes([1,249,1,3,19,0])),Trial(COUNTDOWN,2,3)))


def repeated_cases():
    rows=[]
    for index,sequence in enumerate(REPLACEMENTS):
        # Each three-call sequence deliberately reuses one image address and descriptor.
        calls=[call(index*19+phase,t,'code_a','stack_a','state_a',3,index%9,'function_return' if phase%2==0 else 'done') for phase,t in enumerate(sequence)]
        rows.append(base(f'replaced image sequence {index}',calls))
    return rows


def caller_profile():
    rows=[]
    for index,t in enumerate(programs()):
        o=simulate(t.image,t.capacity,t.fuel);code=list(t.image)+[170]*(96-len(t.image));pointer=dict(at=address('state_a',24),value=address('stack_a'));constant=[memory('code_a',code),words('length',[len(t.image),t.fuel]),words('state_a',[t.capacity])|dict(offset=16)]
        original=[*constant,words('state_a',[99,99,99,99]),words('state_a',[99])|dict(offset=20),words('last_status',[99,99])]
        returned=[*constant,*report('state_a','stack_a',0,o),words('last_status',[99,99])];published=[*constant,*report('state_a','stack_a',0,o),words('last_status',[o.status,o.retired])]
        def state(mem,policies,**extra):return dict(memory=mem,addressMemory=[pointer],accessPolicies=policies,**extra)
        readonly=policy(address('code_a_guard'),address('state_end',4),reads=True)
        initialize=policy(address('code_a_guard'),address('state_end',4),[window('state_a',24,8)],[window('length',0,8)],True)
        snapshots=[state(original,[initialize],expectedRegisters=dict(a1=bits(len(t.image)),a3=bits(t.fuel))),state(returned,access('code_a',0,len(t.image),'state_a','stack_a',0,t.capacity),label='vm_ready',expectedRegisters=dict(v0=bits(o.status),v1=bits(o.retired))),state(published,[policy(address('code_a_guard'),address('state_end',4),[window('last_status',0,8)],reads=True)],label='vm_stored',expectedRegisters=dict(v0=bits(o.status),v1=bits(o.retired)))]
        rows.append(dict(name=f'vm caller {index}',input='',stdout='',instructionLimit=100000,publication=dict(label='vm_call',initialMemory=[memory('code_a',code),words('length',[len(t.image),t.fuel]),words('state_a',[t.capacity])|dict(offset=16)],snapshots=snapshots,finalState=state(published,[readonly]))))
    return dict(formatVersion=8,cases=rows)


def build(directory,check=False):
    directory=Path(directory);rows=[kernel_case(i,t) for i,t in enumerate(trials())];groups={k:[] for k in ('structure','targets','completed','runtime')}
    for row in rows:
        status=int(row['calls'][0]['expectedRegisters']['v0'],16);status=status-(1<<64) if status>>63 else status
        groups['structure' if status in (-1,-2) else 'targets' if status==-3 else 'completed' if status>=0 else 'runtime'].append(row)
    files={}
    for kind,selected in groups.items():
        for number,start in enumerate(range(0,len(selected),50)):files[f'{kind}-{number:02d}.cases.json']=json.dumps(dict(formatVersion=23,cases=selected[start:start+50]),indent=2)+'\n'
    files['repeated.cases.json']=json.dumps(dict(formatVersion=23,cases=repeated_cases()),indent=2)+'\n';caller=caller_profile();files['caller.cases.json']=json.dumps(caller,indent=2)+'\n';files['caller.kts']=console_render(console_parse(json.dumps(caller).encode()))
    for name,content in files.items():
        if len(content.encode())>1024*1024:raise ValueError('Oversized profile '+name)
        target=directory/name
        if check:
            if target.read_text()!=content:raise ValueError('Stale profile '+name)
        else:directory.mkdir(parents=True,exist_ok=True);target.write_text(content)
    return {k:len(v) for k,v in groups.items()}


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--output-directory',type=Path,required=True);parser.add_argument('--check',action='store_true');args=parser.parse_args();print(build(args.output_directory,args.check))
