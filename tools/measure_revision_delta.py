#!/usr/bin/env python3
"""Compare representation bytes and checked codec instruction work on fixed datasets."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def measure(java, cli, output):
    source = ROOT / 'revision/labs/files/delta-codec.asm'
    model = ROOT / 'packages/023-delta-varint/checks/kernel.kts'
    output.mkdir(parents=True, exist_ok=False)
    checks = output / 'measure.kts'
    prefix = model.read_text().split('val examples=', 1)[0]
    checks.write_text(prefix + '''val datasets=linkedMapOf(
 "steady" to List(32){1000},
 "ramp" to List(32){it-16},
 "timestamps" to List(32){1000000000+1000*it},
 "spike" to List(32){if(it==16)1000000 else 0},
 "extremes" to List(32){if(it%2==0)Int.MIN_VALUE else Int.MAX_VALUE})
for((name,input)in datasets)case("measure $name",instructionLimit=1000000){
 val encoding=traceExecution();checkWire(input,160,false,finish=false)
 val encoderInstructions=encoding.snapshot().instructions;encoding.close()
 val expected=encodedReadings(input,160).values
 val actual=readBytes("stream",expected.size,offset=(input.size+480)%8).toList()
 expectCondition(actual==expected,"Measure only a verified complete encoding")
 val decoding=traceExecution();checkWire(actual,32,true,finish=false)
 val decoderInstructions=decoding.snapshot().instructions;decoding.close()
 val independent=input.sumOf{unsignedGroups(if(it<0)-2L*it-1 else 2L*it).size}
 println("MEASURE|$name|${input.size*4}|$independent|${actual.size}|$encoderInstructions|$decoderInstructions")
}
''')
    command = [str(java), '-jar', str(cli), 'grade', str(checks), str(source)]
    result = subprocess.run(command, capture_output=True, text=True, timeout=300)
    (output / 'stdout.txt').write_text(result.stdout)
    (output / 'stderr.txt').write_text(result.stderr)
    keys = ['dataset', 'wordBytes', 'independentBytes', 'deltaBytes', 'encoderInstructions', 'decoderInstructions']
    rows = []
    for line in result.stdout.splitlines():
        if line.startswith('MEASURE|'):
            values = line.split('|')[1:]
            rows.append({key: value if key == 'dataset' else int(value) for key, value in zip(keys, values)})
    if result.returncode or len(rows) != 5 or sum(line.startswith('PASS ') for line in result.stdout.splitlines()) != 5:
        raise AssertionError((result.returncode, result.stdout[-2000:], result.stderr[-2000:]))
    sha = lambda f: hashlib.sha256(f.read_bytes()).hexdigest()
    receipt = dict(sourceSha256=sha(source), cliSha256=sha(cli), modelSha256=sha(model), toolSha256=sha(Path(__file__)), command=command, rows=rows,
                   scope='Thirty-two readings per dataset. Representation sizes exclude framing. Completed instructions cover each checked function entry through return, including its frame and validation; caller setup, publication, host preparation/inspection, assembly, session startup, host time and hardware cycles are excluded. Encoder and decoder use actual wire output in the same execution session, with predecessor reset on each call. Exact work is a property of this source, not a free-writing acceptance condition.')
    (output / 'measurements.json').write_text(json.dumps(receipt, indent=2) + '\n')
    print(json.dumps(rows, indent=2))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ('java', 'cli', 'output'):
        parser.add_argument('--' + name, type=Path, required=True)
    args = parser.parse_args()
    measure(args.java, args.cli, args.output)
