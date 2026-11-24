#!/usr/bin/env python3
"""Create public checkpoint-only WAV feedback; native LaTeX remains the prose authority."""

from kotlin_format import format_kotlin
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
FOLDER = ROOT / "drafts/pcm-wav-construction"
TARGETS = ["bound", "parse", "filter", "encode", "load", "save", "pipeline", "kernel"]


def literal(text):
    if len(text) <= 8192:
        return 'bytes("' + text + '")'
    return (
        "bytes(listOf("
        + ",".join('"' + text[i : i + 8192] + '"' for i in range(0, len(text), 8192))
        + ').joinToString(""))'
    )


def case(step, recipe, caller):
    n = int(step["id"][1:])
    target = step["target"]
    cp = recipe["callerCheckpoint"] if caller else recipe["functionCheckpoint"]
    lines = [
        'case("'
        + recipe["id"]
        + (" caller checkpoint" if caller else " direct checkpoint")
        + '", instructionLimit=100000000) {',
        " val saved=arrange()",
        " val hz="
        + recipe["rate"]
        + "L; val channels="
        + recipe["channels"]
        + "L; val frames="
        + recipe["frames"]
        + "L",
        " val selected="
        + literal(recipe["fileHex"])
        + "; val incoming="
        + literal(recipe["inputFileHex"]),
        " val plain="
        + literal(recipe["pcmHex"])
        + "; val coefficients="
        + literal(recipe["coefficientHex"]),
        " val window="
        + literal(recipe["kernelHex"])
        + "; val previous="
        + literal(recipe["outputFileHex"]),
        ' install("source",selected); install("samples",plain); install("coeff",coefficients); install("kernel_window",window)',
        ' install("input_path","input.wav".toByteArray()+byteArrayOf(0)); install("output_path","output.wav".toByteArray()+byteArrayOf(0))',
        ' install("mode",little('
        + str(TARGETS.index(target))
        + ')); install("byte_count",little('
        + recipe["count"]
        + "L,8))",
        ' install("byte_capacity",little('
        + recipe["capacity"]
        + 'L,8)); install("kernel_count",little('
        + recipe["kernelCount"]
        + "L,8))",
    ]
    if target in ["bound", "filter", "encode"]:
        lines += [' install("metadata",description(hz,channels,frames))']
    lines += [
        ' val files=provideFiles(mapOf("input.wav" to incoming,"output.wav" to previous),writablePaths=setOf("output.wav"),maxReadSize='
        + str(recipe["readChunk"])
        + ",maxWriteSize="
        + str(recipe["writeChunk"])
        + ",firstDescriptor="
        + str(recipe["firstDescriptor"])
        + ")"
    ]
    if caller:
        lines += [' startAt("main")']
    else:
        pointers = {
            "bound": [("a0", "metadata")],
            "parse": [("a0", "source"), ("a2", "metadata")],
            "filter": [
                ("a0", "source"),
                ("a1", "metadata"),
                ("a2", "samples"),
                ("a3", "coeff"),
            ],
            "encode": [("a0", "samples"), ("a1", "metadata"), ("a2", "destination")],
            "load": [("a0", "input_path"), ("a1", "source"), ("a3", "metadata")],
            "save": [("a0", "output_path"), ("a1", "source")],
            "kernel": [
                ("a0", "kernel_window"),
                ("a1", "coeff"),
                ("a2", "kernel_output"),
            ],
        }[target]
        lines += [
            ' setRegisterToDataAddress("' + r + '","' + label + '")'
            for r, label in pointers
        ]
        control = {
            "parse": ("a1", "count"),
            "encode": ("a3", "capacity"),
            "load": ("a2", "capacity"),
            "save": ("a2", "count"),
            "kernel": ("a3", "kernelCount"),
        }.get(target)
        if control:
            lines += [' setRegister("' + control[0] + '",' + recipe[control[1]] + "L)"]
        lines += [
            ' setRegisterToTextAddress("ra","function_return"); startAt("'
            + ("fir_q15" if target == "kernel" else "wav_" + target)
            + '")'
        ]
    lines += [' runUntil("' + cp + '")']
    if target == "bound":
        lines += [
            ' image("metadata",description(hz,channels,frames)); image("source",selected)'
        ]
        if n == 1:
            lines += [' expectRegister("a0",readDataAddress("metadata"))']
        if n >= 3:
            lines += [
                ' expectRegister("t0",hz); expectRegister("t1",channels); expectRegister("t2",frames)'
            ]
        if n == 5:
            lines += [
                ' expectRegister("v0",0); expectRegister("v1",44+2*channels*frames)'
            ]
    elif target == "kernel":
        lines += [
            " val calculated=q15(window.copyOfRange(14,14+2*"
            + recipe["kernelCount"]
            + "),1,coefficients)",
            ' image("kernel_output",calculated.first); expectRegister("v0",'
            + recipe["kernelCount"]
            + '); expectRegister("v1",calculated.second); preservedCheckpoint(saved)',
        ]
    elif target == "parse":
        lines += [
            ' val decoded=readWave(selected); image("source",selected)',
            ' image("metadata",'
            + ("decoded!!.descriptor()" if n in [26, 28] else "byteArrayOf()")
            + ")",
        ]
        if n == 9:
            lines += [
                ' expectRegister("s3",12); expectRegister("t2",readDataAddress("source",20)); expectRegister("t3",3); expectRegister("t4",24)'
            ]
        if n in [10, 11, 12]:
            lines += [
                ' expectRegister("s3",24); expectRegister("t8",16); expectRegister("t9",readDataAddress("source",32))'
            ]
        if 14 <= n <= 26:
            lines += [' expectRegister("s6",channels)']
        if 15 <= n <= 26:
            lines += [' expectRegister("s5",hz)']
        if n == 19:
            lines += [' expectRegister("t8",18)']
        if n == 20:
            lines += [' expectRegister("s4",1); expectRegister("s3",48)']
        if 21 <= n <= 25:
            lines += [' expectRegister("s7",56); expectRegister("fp",16)']
        if 22 <= n <= 25:
            lines += [' checkpointBytes("workspace",little(4,8),8192-160+80)']
        if n == 23:
            lines += [' expectRegister("s4",3); expectRegister("s3",72)']
        if n in [24, 25]:
            lines += [' expectRegister("s4",3); expectRegister("s3",82)']
        if n in [26, 27, 28]:
            lines += [
                ' expectRegister("v0",'
                + ("1" if n == 27 else "0")
                + '); expectRegister("v1",'
                + ("0L" if n == 27 else "decoded!!.frames")
                + ")"
            ]
    elif target == "filter":
        lines += [
            " val calculated=q15(selected,channels.toInt(),coefficients)",
            ' image("source",selected); image("metadata",description(hz,channels,frames)); image("coeff",coefficients)',
        ]
        if n < 36:
            lines += [' image("samples")']
        elif n < 40:
            lines += [
                ' val partial=ByteArray(selected.size){0xa5.toByte()}; for(frame in 0 until frames.toInt())calculated.first.copyInto(partial,4*frame,4*frame,4*frame+2); image("samples",partial)'
            ]
        else:
            lines += [' image("samples",calculated.first)']
        if n == 29:
            lines += [' expectRegister("v0",0); expectRegister("v1",44+selected.size)']
        if 30 <= n <= 40:
            lines += [
                ' expectRegister("s1",frames); expectRegister("s2",channels); expectRegister("s7",0)'
            ]
        if 31 <= n <= 39:
            lines += [
                ' expectRegister("s5",0); expectRegister("s6",'
                + ("4" if n == 39 else "0")
                + ")"
            ]
        if 32 <= n <= 39:
            lines += [' expectRegister("fp",4)']
        if n == 31:
            lines += [' checkpointBytes("workspace",ByteArray(14),8192-512+112)']
        if 33 <= n <= 37:
            lines += [
                ' val left=ByteArray(8){selected[(it/2)*4+it%2]}; checkpointBytes("workspace",ByteArray(14)+left,8192-512+112)'
            ]
        if n in [38, 39]:
            lines += [
                ' val left=ByteArray(8){selected[(it/2)*4+it%2]}; checkpointBytes("workspace",ByteArray(6)+left,8192-512+112)'
            ]
        if n == 34:
            lines += [' expectRegister("v0",4); expectRegister("v1",0)']
        if n == 40:
            lines += [' expectRegister("s5",2); expectRegister("s6",4)']
        if n >= 41:
            lines += [' expectRegister("v0",0); expectRegister("v1",calculated.second)']
    elif target == "encode":
        length = {
            43: 0,
            44: 0,
            45: 12,
            46: 24,
            47: 32,
            48: 36,
            49: 44,
            50: -1,
            51: -1,
            52: -1,
        }[n]
        lines += [
            ' val encoded=writeWave(hz,channels,plain); image("source",selected); image("samples",plain); image("metadata",description(hz,channels,frames))',
            ' image("destination",'
            + ("encoded" if length < 0 else "encoded.copyOf(" + str(length) + ")")
            + ")",
        ]
        if n == 43:
            lines += [
                ' expectRegister("v0",0); expectRegister("v1",encoded.size); expectRegister("s4",encoded.size)'
            ]
        if 44 <= n <= 51:
            lines += [
                ' expectRegister("s4",encoded.size); expectRegister("s5",hz); expectRegister("s6",channels); expectRegister("fp",plain.size)'
            ]
        if n >= 51:
            lines += [' expectRegister("v0",0); expectRegister("v1",encoded.size)']
    elif target == "load":
        length = 0 if n < 55 else recipe["readChunk"] if n == 55 else -1
        lines += [
            ' image("source",'
            + ("incoming" if length < 0 else "incoming.copyOf(" + str(length) + ")")
            + '); image("metadata",'
            + ("readWave(incoming)!!.descriptor()" if n == 57 else "byteArrayOf()")
            + ")"
        ]
        if n in [54, 55, 56]:
            lines += [
                ' expectRegister("s3",'
                + str(recipe["firstDescriptor"])
                + '); expectRegister("s4",'
                + str(
                    0
                    if n == 54
                    else (
                        recipe["readChunk"]
                        if n == 55
                        else len(bytes.fromhex(recipe["inputFileHex"]))
                    )
                )
                + ")"
            ]
        if n == 57:
            lines += [
                ' expectRegister("s3",-1); expectRegister("v0",0); expectRegister("v1",incoming.size)'
            ]
    elif target == "save":
        lines += [' image("source",selected); image("metadata")']
        if n == 59:
            lines += [
                ' checkpointBytes("workspace",readWave(selected)!!.descriptor(),8192-128+80); expectRegister("v0",0); expectRegister("v1",frames)'
            ]
        if n in [60, 61]:
            lines += [
                ' expectRegister("s3",'
                + str(recipe["firstDescriptor"])
                + '); expectRegister("s4",'
                + str(0 if n == 60 else recipe["writeChunk"])
                + ")"
            ]
        if n == 62:
            lines += [
                ' expectRegister("s3",-1); expectRegister("v0",0); expectRegister("v1",selected.size)'
            ]
    elif target == "pipeline":
        lines += [
            " val decoded=readWave(incoming)!!; val calculated=q15(decoded.samples,decoded.channels.toInt(),coefficients); val encoded=writeWave(decoded.hz,decoded.channels,calculated.first)",
            ' image("source",incoming); image("metadata",decoded.descriptor()); image("samples",calculated.first); image("destination",encoded)',
            ' expectRegister("v0",0); expectRegister("v1",encoded.size); checkpointBytes("clips",little(calculated.second,8)); preservedCheckpoint(saved)',
        ]
    fileExpression = "previous"
    if target == "save" and n >= 60:
        fileExpression = (
            "byteArrayOf()"
            if n == 60
            else (
                "selected.copyOf(" + str(recipe["writeChunk"]) + ")"
                if n == 61
                else "selected"
            )
        )
    if target == "pipeline":
        fileExpression = "encoded"
    opened = (
        target == "load" and n in [54, 55, 56] or target == "save" and n in [60, 61]
    )
    lines += [
        ' files.expectFile("output.wav",' + fileExpression + ")",
        " val events=files.snapshot(); expectCondition(events.count{it.operation==Operation.OPEN&&it.result>=0}-events.count{it.operation==Operation.CLOSE}=="
        + str(1 if opened else 0)
        + ',"Checkpoint descriptor ownership")',
        ' expectCondition(events.filter{it.operation==Operation.CLOSE}.all{it.result==0},"Every close so far is valid")',
        ' expectOutput("")',
        "}",
    ]
    return "\n".join(lines) + "\n"


def build():
    steps = json.loads((FOLDER / "steps.json").read_text())["steps"]
    recipes = json.loads((FOLDER / "inputs.json").read_text())["recipes"]
    output = FOLDER / "checks/construction"
    output.mkdir(exist_ok=True)
    model = (
        (ROOT / "tools/wav-feedback/model.kts").read_text()
        + '\nfun AssemblyCase.checkpointBytes(label:String,raw:ByteArray,offset:Int=0){expectCondition(readBytes(label,raw.size,offset).contentEquals(raw.map{it.toInt()and 255}.toIntArray()),"Exact published checkpoint bytes")}\n'
        + 'fun AssemblyCase.preservedCheckpoint(saved:List<Long>){for(i in 0..7)expectRegister("s$i",saved[i]);expectRegister("fp",saved[8]);expectRegister("sp",saved[9])}\n'
    )
    counts = {}
    for step in steps:
        selected = [q for q in recipes if q["id"] in step["recipes"]]
        checks = [
            case(step, q, caller)
            for q in selected
            for caller in ([True] if q["callerOnly"] else [False, True])
        ]
        (output / (step["id"] + ".kts")).write_text(
            format_kotlin(model + "\n" + "".join(checks))
        )
        counts[step["id"]] = len(checks)
    (output / "cases.json").write_text(json.dumps(counts, indent=2) + "\n")
    print(
        "Built",
        len(steps),
        "public guided scripts with",
        sum(counts.values()),
        "checkpoint cases",
    )


if __name__ == "__main__":
    build()
