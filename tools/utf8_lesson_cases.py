"""Author public UTF-8 stream and caller cases as bounded native data.

The oracle reasons about scalar-value intervals of accepted prefixes. It does not
execute MIPS or generate book prose. Original stream ordering and fixture salts
are retained so historical DSL checks can be compared directly.
"""
import argparse
from dataclasses import dataclass
import json
from pathlib import Path

from console_cases import parse as console_parse, render as console_render
from function_lesson_cases import bits
from ring_lesson_cases import address, memory, words, policy

EMPTY = (0, 0, 0, 128, 191, -1, 0, -1)
FIXTURE = (0x41, 0xe2, 0x82, 0xac, 0xf0, 0x9f, 0x99, 0x82, 0, 0xc3, 0xa9)


def width(first):
    if first & 0xe0 == 0xc0:return 2
    if first & 0xf0 == 0xe0:return 3
    if first & 0xf8 == 0xf0:return 4
    return 0


def possible(prefix, size):
    value = prefix[0] & ((1 << (7-size))-1)
    for byte in prefix[1:]:value = (value << 6) | (byte & 63)
    shift = 6*(size-len(prefix))
    lower = max(value << shift, {2:0x80, 3:0x800, 4:0x10000}[size])
    upper = min(((value+1) << shift)-1, {2:0x7ff, 3:0xffff, 4:0x10ffff}[size])
    return lower <= upper and (lower < 0xd800 or upper > 0xdfff)


class Model:
    def __init__(self):
        self.total = self.count = self.width = self.status = 0
        self.start = self.error = -1
        self.pending = []

    def fields(self):
        remaining = self.width-len(self.pending) if self.pending else 0
        candidates = [b for b in range(128,192) if possible(self.pending+[b], self.width)] if self.pending else [128,191]
        return [self.total,self.count,remaining,candidates[0],candidates[-1],self.start,self.status,self.error]

    def feed(self, data, final=False):
        if self.status:return -3,0
        used = 0
        for byte in data:
            if not self.pending:
                if byte < 128:
                    self.count += 1;self.total += 1;used += 1
                    continue
                self.start = self.total
                size = width(byte)
                if not size or not possible([byte],size):
                    self.status = -1;self.error = self.total
                    break
                self.width = size;self.pending.append(byte)
            else:
                if not 128 <= byte <= 191 or not possible(self.pending+[byte],self.width):
                    self.status = -1;self.error = self.total
                    break
                self.pending.append(byte)
                if len(self.pending)==self.width:
                    self.count += 1;self.pending.clear();self.width = 0;self.start = -1
            self.total += 1;used += 1
        if not self.status and final:
            self.status = -2 if self.pending else 1
            if self.status == -2:self.error = self.total
        return self.status,used


@dataclass(frozen=True)
class Stream:
    name: str
    prefix: tuple
    chunks: tuple
    final: bool = True
    final_index: int = -1
    prefix_final: bool = False

    def is_final(self,index):
        return index == self.final_index if self.final_index >= 0 else self.final and index == len(self.chunks)-1


def encode(point):return tuple(chr(point).encode('utf-8'))


def streams():
    rows = []
    for point in [0,0x41,0x7f,0x80,0x7ff,0x800,0xd7ff,0xe000,0xffff,0x10000,0x10ffff,0x301,0xfeff,0x1f642]:
        data = encode(point)
        for cut in range(len(data)+1):rows.append(Stream(f'scalar {point} cut{cut}',(),(data[:cut],(),data[cut:],())))
    for byte in range(256):rows.append(Stream(f'one byte {byte}',(),((byte,),)))
    for prefix in [(),(0xc2,),(0xe0,),(0xed,),(0xf0,),(0xf4,),(0xf0,0x90),(0xf4,0x8f,0xbf)]:
        for byte in [0,0x41,0x7f,0x80,0x8f,0x90,0x9f,0xa0,0xbf,0xc0,0xc2,0xff]:
            rows.append(Stream(f'prefix {list(prefix)} next{byte}',(0x41,0)+prefix,((byte,),(0x41,),())))
    for cut in range(len(FIXTURE)+1):rows.append(Stream(f'mixed cut{cut}',(),(FIXTURE[:cut],FIXTURE[cut:])))
    for prefix in [(0xc0,),(0xed,0xa0),(0x41,0x80),(0xf4,0x90)]:rows.append(Stream(f'latched {list(prefix)}',prefix,(FIXTURE,())))
    rows += [Stream('no finalization',(),((0xf0,),(),(0x90,)),False),
             Stream('full ASCII chunk',(),(tuple(range(64)),())),
             Stream('finished initial state',(0x41,),(FIXTURE,()),prefix_final=True),
             Stream('truncated initial state',(0xe2,0x82),(FIXTURE,()),prefix_final=True),
             Stream('finish then reuse',(),((0x41,),FIXTURE,()),final_index=0),
             Stream('truncate then reuse',(),((0xe0,),FIXTURE,()),final_index=0),
             Stream('stream maximum',(0x41,)*4090,(encode(0x1f642)+(0,0x41),)),
             Stream('pending at maximum',(0x41,)*4095,((0xf0,),()))]
    return rows


def arena(data,offset,salt):
    result = [(i*37+salt*17+19)&255 for i in range(96)]
    result[offset:offset+len(data)] = data
    return result


def policies(target,offset,length,terminal=False,metadata=False):
    writes = [] if terminal else [dict(at=address(target),length=32)]
    if metadata:writes += [dict(at=address(label),length=4) for label in ['chunk_index','last_status','last_used']]
    return [policy(address('state_a'),address('state_end',4),writable=writes),
            policy(address('input_guard'),address('input_end',4),readable=[] if terminal else [dict(at=address('input',offset),length=length)],reads=True)]


def function_case(index,stream):
    target = 'state_a' if index%2==0 else 'state_b'
    model = Model();model.feed(stream.prefix,stream.prefix_final)
    calls = []
    for number,data in enumerate(stream.chunks):
        offset = (index+number)%8;before = arena(data,offset,index+number);terminal = bool(model.status)
        status,used = model.feed(data,stream.is_final(number))
        calls.append(dict(entry='scan_utf8',returnTo='function_return' if index%3==0 else 'done',
            arguments={**{f't{i}':bits(0x456789ab00000000+index*131+number*31+i) for i in range(10)},'v0':bits(777),'v1':bits(888),'a2':bits(len(data)),'a3':bits(stream.is_final(number))},
            argumentAddresses=dict(a0=address(target),a1=address('input',offset)),
            setupMemory=[memory('input',before)],expectedRegisters=dict(v0=bits(status),v1=bits(used)),
            memory=[words(target,model.fields()),memory('input',before)],accessPolicies=policies(target,offset,len(data),terminal)))
    initial = Model();initial.feed(stream.prefix,stream.prefix_final)
    return base_case(stream.name,calls,[words(target,initial.fields())])


def base_case(name,calls,initial):
    return dict(name=name,instructionLimit=50000,calls=calls,initialMemory=initial,
        initialRegisters={**{f's{i}':bits(0x3456789000000000+i*1013) for i in range(8)},'sp':bits(0x7fff0000)},
        preservedRegisters=[f's{i}' for i in range(8)],stackAlignment=16,callerStackBytes=0,
        noExecution=[dict(**{'from':'function_return'},until='done')],
        exit=dict(memory=calls[-1]['memory'],accessPolicies=[policy(address('state_a'),address('state_end',4))]))


def interleaved_cases():
    patterns = [((0xe2,),(0xc3,),(0x82,0xac),(0xa9,)),
                ((0xf0,),(0xed,),(0x90,0x80,0x80),(0x9f,0xbf)),
                ((0xe0,),(0xf4,),(0xa0,0x80),(0x8f,0xbf,0xbf)),
                ((0xe0,),(0xc2,),(0x80,),(0x80,)),
                ((0x41,0),(0xf0,0x9f),(),(0x99,0x82)),
                ((),(0x80,),(),(0x41,))]
    rows = []
    for number,pattern in enumerate(patterns):
        for swapped in [False,True]:
            models = {'state_a':Model(),'state_b':Model()};calls = []
            # Revisit both terminal states with nonempty input, then an empty call.
            sequence = [(0,pattern[0],False),(1,pattern[1],False),(0,pattern[2],True),(1,pattern[3],True),(0,FIXTURE,False),(1,(),True)]
            for index,(selected,data,final) in enumerate(sequence):
                target = 'state_b' if bool(selected)^swapped else 'state_a';model=models[target]
                terminal=bool(model.status);offset=(number+index)%8;before=arena(data,offset,number*7+index)
                status,used=model.feed(data,final)
                calls.append(dict(entry='scan_utf8',returnTo='done' if index%2 else 'function_return',
                    arguments={**{f't{i}':bits(0x89abcdef00000000+number*131+index*31+i) for i in range(10)},'v0':bits(777),'v1':bits(888),'a2':bits(len(data)),'a3':bits(final)},
                    argumentAddresses=dict(a0=address(target),a1=address('input',offset)),setupMemory=[memory('input',before)],
                    expectedRegisters=dict(v0=bits(status),v1=bits(used)),
                    memory=[words(label,m.fields()) for label,m in models.items()]+[memory('input',before)],
                    accessPolicies=policies(target,offset,len(data),terminal)))
            rows.append(base_case(f'interleaved {number} swap{int(swapped)}',calls,[words(label,EMPTY) for label in models]))
    return rows


def caller_profile():
    cases = []
    for count in range(5):
        for mode in range(3):
            model = Model();data = list(FIXTURE)+[0]*85
            if mode==1:data[4:8]=[0xf0,0x8f,0x99,0x82]
            chunks = [(0,2,False),(2,3,False),(5,2,False),(7,0 if mode==2 else 4,True)]
            snapshots = []
            for index,(offset,length,final) in enumerate(chunks[:count]):
                terminal=bool(model.status);status,used=model.feed(data[offset:offset+length],final)
                snapshots.append(dict(addressMemory=[],memory=[words('state_a',model.fields()),memory('input',data),words('chunk_index',[index]),words('last_status',[status]),words('last_used',[used])],
                    accessPolicies=policies('state_a',offset,length,terminal,True)))
            initial=[words('chunk_count',[count])]
            if mode==1:initial.append(memory('input',[0xf0,0x8f,0x99,0x82],4))
            if mode==2:initial.append(words('chunks',[0]) | dict(offset=40))
            final_memory=[words('state_a',model.fields()),memory('input',data)]
            cases.append(dict(name=f'main count{count} mode{mode}',input='',stdout='',instructionLimit=50000,
                publication=dict(label='chunk_ready',initialMemory=initial,snapshots=snapshots,
                    finalState=dict(addressMemory=[],memory=final_memory,accessPolicies=[policy(address('state_a'),address('state_end',4))]))))
    return dict(formatVersion=7,cases=cases)


def build(directory,check=False):
    directory=Path(directory);rows=[function_case(i,s) for i,s in enumerate(streams())];assert len(rows)==426
    files={}
    for number,start in enumerate(range(0,len(rows),50)):
        files[f'stream-{number:02d}.cases.json']=json.dumps(dict(formatVersion=23,cases=rows[start:start+50]),indent=2)+'\n'
    files['interleaved.cases.json']=json.dumps(dict(formatVersion=23,cases=interleaved_cases()),indent=2)+'\n'
    caller=caller_profile();files['caller.cases.json']=json.dumps(caller,indent=2)+'\n';files['caller.kts']=console_render(console_parse(json.dumps(caller).encode()))
    for name,content in files.items():
        if len(content.encode())>1024*1024:raise ValueError('Profile exceeds native byte limit: '+name)
        target=directory/name
        if check:
            if target.read_text()!=content:raise ValueError('Stale UTF-8 data: '+name)
        else:
            directory.mkdir(parents=True,exist_ok=True);target.write_text(content)
    return files


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--output-directory',type=Path,required=True);parser.add_argument('--check',action='store_true');args=parser.parse_args();build(args.output_directory,args.check)
