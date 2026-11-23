"""Author the original ring trials as bounded native data; no publication prose."""
import argparse
import json
from pathlib import Path

from console_cases import parse as console_parse, render as console_render
from function_lesson_cases import bits

OPERATIONS = ((False,3,0),(False,2,8),(True,31,0),(False,3,16),(True,31,8),(True,31,16),
              (False,0,24),(True,0,24),(False,7,32),(False,0,24),(True,6,32),(True,7,32),
              (True,31,48),(False,8,48))
COUNTS = (0,1,2,3,4,6,8,9,10,11,12,14)


def payload(length, salt):
    return [(i * 129 + salt * 37 + 128) & 255 for i in range(length)]


def trials():
    result = []
    for capacity in (1,2,3,7,8,13,31,32):
        for head in sorted({0, capacity - 1}):
            queues = ([], [[]], [payload(capacity - 1, capacity)], [[] for _ in range(capacity)])
            for number, records in enumerate(queues):
                for pop in (False, True):
                    for amount in dict.fromkeys((0,1,capacity-1,capacity,31)):
                        if amount <= 31:
                            result.append((capacity, head, records, pop, amount, capacity*97+head*7+number))
    return result


def address(label, offset=0):
    return dict(label=label, offset=offset)


def memory(label, data, offset=0):
    return dict(label=label, bytes=bytes(data).hex(), offset=offset)


def words(label, values):
    return memory(label, b''.join((value & 0xffffffff).to_bytes(4, 'little') for value in values))


def windows(label, indexes):
    indexes = sorted(set(indexes))
    result = []
    for index in indexes:
        if result and result[-1]['at']['offset'] + result[-1]['length'] == index:
            result[-1]['length'] += 1
        else:
            result.append(dict(at=address(label, index), length=1))
    return result


def policy(first, until, writable=(), readable=(), reads=False):
    return dict(**{'from': first}, until=until, writable=list(writable), readable=list(readable),
                checkReads=reads, maxAccesses=100000)


def kernel_case(index, trial):
    capacity, head, records, pop, amount, salt = trial
    used = sum(len(record) + 1 for record in records)
    tail = (head + used) % capacity
    descriptor = 'descriptor_a' if index % 2 == 0 else 'descriptor_b'
    ring = 'ring_b' if index % 3 == 0 else 'ring_a'
    source_offset, output_offset = index % 8, index // 3 % 8
    source, output, initial_ring = payload(64, salt), payload(64, salt+1), payload(32, salt+2)
    cursor = head
    for record in records:
        for byte in [len(record), *record]:
            initial_ring[cursor] = byte
            cursor = (cursor + 1) % capacity
    result_ring = initial_ring.copy()
    result_output = output.copy()
    next_head, next_tail, next_used = head, tail, used
    ring_written, output_written = [], []
    if not pop:
        if amount >= capacity:
            status, auxiliary = -2, 0
        elif amount + 1 > capacity - used:
            status, auxiliary = -1, 0
        else:
            status, auxiliary = 0, amount + 1
            for byte in [amount, *source[source_offset:source_offset+amount]]:
                result_ring[next_tail] = byte
                ring_written.append(next_tail)
                next_tail = (next_tail + 1) % capacity
            next_used += auxiliary
    elif not records:
        status, auxiliary = -1, 0
    elif amount < len(records[0]):
        status, auxiliary = -2, len(records[0])
    else:
        status, auxiliary = len(records[0]), len(records[0]) + 1
        for i, byte in enumerate(records[0]):
            result_output[output_offset+i] = byte
            output_written.append(output_offset+i)
        next_head = (head + auxiliary) % capacity
        next_used -= auxiliary
    writable = windows(ring, ring_written) + windows('output', output_written)
    if status >= 0:
        writable += [dict(at=address(descriptor), length=12)]
    policies = [policy(address('descriptor_a'), address('state_end',4), writable),
                policy(address(ring+'_guard'), address(ring+'_end',4), windows(ring, ring_written),
                       [dict(at=address(ring), length=capacity)], True)]
    if not pop:
        policies.append(policy(address('source_guard'), address('source_end',4),
                               readable=[dict(at=address('source',source_offset), length=amount)], reads=True))
    initial = [memory('source',source), memory('output',output), memory(ring,initial_ring),
               words(descriptor,[head,tail,used,capacity])]
    expected = [memory('source',source), memory('output',result_output), memory(ring,result_ring),
                words(descriptor,[next_head,next_tail,next_used,capacity])]
    pointer = dict(at=address(descriptor,16), value=address(ring))
    continuation = 'function_return' if index % 2 == 0 else 'done'
    call = dict(entry='pop_record' if pop else 'push_record', returnTo=continuation, arguments=dict(a2=bits(amount)),
                argumentAddresses=dict(a0=address(descriptor), a1=address('output' if pop else 'source',output_offset if pop else source_offset)),
                expectedRegisters=dict(v0=bits(status),v1=bits(auxiliary)), memory=expected,
                addressMemory=[pointer], setupAddressMemory=[pointer], accessPolicies=policies)
    return dict(name=f'{"pop" if pop else "push"} C{capacity} h{head} q{[len(r) for r in records]} n{amount} {index}',
                instructionLimit=5000, calls=[call], initialMemory=initial,
                initialRegisters={**{f's{i}':bits(0x3456789000000000+i*1013) for i in range(8)},'sp':bits(0x7fff0000)},
                preservedRegisters=[f's{i}' for i in range(8)], stackAlignment=16, callerStackBytes=0,
                noExecution=[dict(**{'from':'function_return'},until='done')],
                exit=dict(memory=expected,accessPolicies=policies))


def caller_profile():
    # These literal arrays are the public retained caller's initial data.
    source = [65,66,67,0,0,0,0,0,120,121,0,0,0,0,0,0,127,128,255]+[0]*13+[0,1,2,127,128,254,255,0]+[0]*24
    assert len(source) == 64
    cases = []
    for count in COUNTS:
        head = tail = used = 0
        queue, ring, output, snapshots = [], [0]*32, [0x55]*64, []
        def state(writable):
            return dict(memory=[words('descriptor_a',[head,tail,used,8]),memory('ring_a',ring),
                                memory('source',source),memory('output',output)],
                        addressMemory=[dict(at=address('descriptor_a',16),value=address('ring_a'))],
                        accessPolicies=[policy(address('descriptor_a'),address('state_end',4),writable)])
        for index, (pop, amount, offset) in enumerate(OPERATIONS[:count]):
            writable = [dict(at=address('descriptor_a',16),length=8)] if index == 0 else []
            changed = []
            if not pop:
                if amount >= 8:
                    status, auxiliary = -2, 0
                elif amount + 1 > 8 - used:
                    status, auxiliary = -1, 0
                else:
                    status, auxiliary = 0, amount + 1
                    body = source[offset:offset+amount];queue.append(body)
                    for byte in [amount,*body]:
                        ring[tail] = byte;changed.append(tail);tail = (tail+1)%8
                    used += auxiliary
                    writable += windows('ring_a',changed)
            elif not queue:
                status, auxiliary = -1, 0
            elif amount < len(queue[0]):
                status, auxiliary = -2, len(queue[0])
            else:
                body = queue.pop(0);status, auxiliary = len(body),len(body)+1
                head = (head+auxiliary)%8;used -= auxiliary
                output[offset:offset+len(body)] = body
                writable += windows('output',range(offset,offset+len(body)))
            if status >= 0:writable.append(dict(at=address('descriptor_a'),length=12))
            writable += [dict(at=address(label),length=4) for label in ['last_status','last_aux','operation_index']]
            snapshot = state(writable)
            snapshot['memory'] += [words('last_status',[status]),words('last_aux',[auxiliary]),words('operation_index',[index])]
            snapshots.append(snapshot)
        final = state([dict(at=address('descriptor_a',16),length=8)] if count == 0 else [])
        if count:
            final['memory'] += [words('last_status',[status]),words('last_aux',[auxiliary]),words('operation_index',[count-1])]
        publication = dict(label='operation_ready',initialMemory=[words('command_count',[count])],snapshots=snapshots,finalState=final)
        cases.append(dict(name=f'main first {count} commands',input='',stdout='',instructionLimit=20000,publication=publication))
    return dict(formatVersion=7,cases=cases)


def build(directory, check=False):
    directory = Path(directory)
    rows = [kernel_case(index, trial) for index, trial in enumerate(trials())]
    assert len(rows) == 520
    files = {}
    for batch, start in enumerate(range(0,len(rows),100)):
        files[f'kernels-{batch:02d}.cases.json'] = json.dumps(dict(formatVersion=23,cases=rows[start:start+100]),indent=2)+'\n'
    for operation in ['push', 'pop']:
        selected = [row for row in rows if row['calls'][0]['entry'] == operation + '_record']
        for batch, start in enumerate(range(0,len(selected),100)):
            files[f'{operation}-{batch:02d}.cases.json'] = json.dumps(dict(formatVersion=23,cases=selected[start:start+100]),indent=2)+'\n'
    caller = caller_profile();cases = console_parse(json.dumps(caller).encode())
    files['caller.cases.json'] = json.dumps(caller,indent=2)+'\n'
    files['caller.kts'] = console_render(cases)
    for name,text in files.items():
        target = directory/name
        if check:
            if target.read_text()!=text:raise ValueError('Stale ring cases: '+str(target))
        else:
            directory.mkdir(parents=True,exist_ok=True);target.write_text(text)
    return files


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output-directory',type=Path,required=True);parser.add_argument('--check',action='store_true')
    args = parser.parse_args();build(args.output_directory,args.check)
