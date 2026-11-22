"""Generate stable sign-partition cases with active-span access contracts."""
import argparse
import json
from pathlib import Path

from console_cases import kotlin_string
from function_lesson_cases import bits, saved_values

EXAMPLES = (
    (), (0,), (-2147483648,), (0,1,2147483647), (-1,-4,-2147483648),
    (2,-2,3,-4,0,1,-3,-1), (-2,)*8, (0,)*8, (3,1,2,0), (-1,-4,-3,-2),
    (2147483647,-2147483648,-1,0,1,-2147483648,2147483647,-2),
)
BASE=(3,-4,0,-2147483648,2147483647,-1,2,-3)
GUARDS=('values_before','values_guard','count_guard','result_guard')


def trials():
    return [(list(values)+[0x5a000000+cell for cell in range(8-len(values))],0,len(values)) for values in EXAMPLES]+[
        (list(BASE),offset,length) for offset in range(9) for length in range(9-offset)]


def partition(words,offset,length):
    active=words[offset:offset+length]
    return words[:offset]+[v for v in active if v<0]+[v for v in active if v>=0]+words[offset+length:]


def word_bytes(words):
    return b''.join((value&0xffffffff).to_bytes(4,'little') for value in words).hex()


def profile():
    cases=[]
    for number,(words,offset,length) in enumerate(trials()):
        initial=[dict(label='values',bytes=word_bytes(words)),dict(label='count',bytes=word_bytes([(length+1)%9])),
                 dict(label='negative_count',bytes=word_bytes([0x6a000000|number]))]
        initial += [dict(label=label,bytes=word_bytes([0x13579bdf^(number*31+index)])) for index,label in enumerate(GUARDS)]
        memory=[dict(label='values',bytes=word_bytes(partition(words,offset,length))),*initial[1:]]
        calls=[dict(entry='partition_negative',returnTo=label,arguments={'a1':bits(length)},
                    argumentAddresses={'a0':dict(label='values',offset=4*offset)},
                    expectedRegisters={'v0':bits(sum(v<0 for v in words[offset:offset+length]))},memory=memory)
               for label in ('partition_done','partition_exit')]
        kept=saved_values(0x1122334400000000+number*0x01010101)
        cases.append(dict(name=f'stable partition {number} offset {offset} length {length}',instructionLimit=10000,
                          calls=calls,initialMemory=initial,noStores=[dict(label=item['label'],length=4) for item in initial[1:]],
                          accessBounds=[dict(label='values',length=40,offset=-4,permittedOffset=4*offset,permittedLength=4*length)],
                          protectCallerStack=True,initialRegisters={**{r:bits(v) for r,v in kept.items()},'sp':bits(0x7fffeffc-number*4096)},
                          preservedRegisters=list(kept),stackAlignment=16,callerStackBytes=32))
    return dict(formatVersion=4,cases=cases)


def script():
    lines=['// Generated stable-partition checks; regenerate with tools/partition_lesson_cases.py.\n',
           '// Word byte strings target the book little-endian environment.\n\n']
    for case in profile()['cases']:
        lines.append(f'case({kotlin_string(case["name"])}, instructionLimit = 10000) {{\n')
        for item in case['initialMemory']:
            values=', '.join(str(b) for b in bytes.fromhex(item['bytes']))
            lines.append(f'    setBytes({kotlin_string(item["label"])}, {values})\n')
        for reg,value in case['initialRegisters'].items():lines.append(f'    setRegister({kotlin_string(reg)}, {value}uL.toLong())\n')
        lines.append('    val top = readRegister("sp") and -16L\n    setRegister("sp", top)\n')
        lines.append('    val saved = listOf("s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "fp", "gp").associateWith(::readRegister)\n')
        lines.append('    val callerBytes = readBytes(top, 32)\n    val callerStores = traceMemoryStores(top, 32)\n')
        lines.append('    val protected = listOf(\n')
        for item in case['noStores']:lines.append(f'        traceMemoryStores({kotlin_string(item["label"])}, 4),\n')
        lines.append('    )\n    val accesses = traceMemoryAccesses("values", 40, offset = -4)\n')
        region=case['accessBounds'][0]
        lines.append(f'    val first = readDataAddress("values", {region["permittedOffset"]}L)\n    val length = {region["permittedLength"]}\n')
        for call in case['calls']:
            lines.append(f'    setRegisterToDataAddress("a0", "values", {call["argumentAddresses"]["a0"]["offset"]}L)\n')
            lines.append(f'    setRegister("a1", {call["arguments"]["a1"]}uL.toLong())\n')
            lines.append(f'    callFunction("partition_negative", returnTo = {kotlin_string(call["returnTo"])})\n')
            lines.append(f'    expectRegister("v0", {int(call["expectedRegisters"]["v0"],16)})\n')
            for item in call['memory']:
                values=', '.join(str(b) for b in bytes.fromhex(item['bytes']))
                lines.append(f'    expectBytes({kotlin_string(item["label"])}, {values})\n')
            lines.append('    for (access in accesses.snapshot()) expectCondition(access.address >= first && access.length <= length && access.address-first <= length.toLong()-access.length, "Access outside active span")\n')
            lines.append('    protected.forEach { it.expectNone() }\n    callerStores.expectNone()\n    expectRegister("sp", top)\n')
            lines.append('    saved.forEach { (reg, value) -> expectRegister(reg, value) }\n')
            lines.append('    expectCondition(readBytes(top, 32).contentEquals(callerBytes), "Caller-owned stack bytes changed")\n    expectOutput("")\n')
        lines.append('}\n\n')
    return ''.join(lines).removesuffix('\n')


def build(directory,check=False):
    directory=Path(directory)
    files={'partition-negative.cases.json':json.dumps(profile(),indent=2)+'\n','partition-negative.kts':script()}
    for name,text in files.items():
        path=directory/name
        if check:
            if path.read_text(encoding='utf-8')!=text:raise ValueError(f'Stale generated partition checks: {path}')
        else:
            directory.mkdir(parents=True,exist_ok=True);path.write_text(text,encoding='utf-8',newline='\n')
    return files


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output-directory',type=Path,required=True);parser.add_argument('--check',action='store_true')
    args=parser.parse_args();build(args.output_directory,args.check)
