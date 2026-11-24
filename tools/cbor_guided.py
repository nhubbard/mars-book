#!/usr/bin/env python3
"""Export public checkpoint feedback from native exercise metadata and published observations."""

from pathlib import Path

from kotlin_format import format_kotlin
import importlib.util
import json

ROOT = Path(__file__).resolve().parents[1]
FOLDER = ROOT / "drafts/cbor-construction"
MODEL = ROOT / "tools/cbor-feedback/model.kts"
DEST = FOLDER / "checks/construction"
TARGETS = ["head", "skip", "decode", "bound", "encode", "pipeline", "size", "write"]


def literal(value):
    n = int(value)
    return str(n) + "uL.toLong()" if n >= 2**63 else str(n) + "L"


def checkpoint(recipe, caller):
    n = int(recipe["step"][1:])
    target = recipe["target"]
    cp = recipe["callerCheckpoint" if caller else "functionCheckpoint"]
    text = (
        "case("
        + json.dumps(
            recipe["id"] + (" caller checkpoint" if caller else " direct checkpoint")
        )
        + ", instructionLimit=2000000) {\n val saved=arrange()\n"
    )
    text += (
        ' install("source",bytes("'
        + recipe["inputHex"]
        + '")); install("record",bytes("'
        + recipe["recordHex"]
        + '"))\n'
    )
    text += ' setWord("mode",' + str(TARGETS.index(target)) + ")\n"
    for field, label in [
        ("count", "byte_count"),
        ("capacity", "byte_capacity"),
        ("depth", "inherited_depth"),
        ("sizeArgument", "size_argument"),
        ("writeMajor", "write_major"),
        ("writeArgument", "write_value"),
    ]:
        text += ' store("' + label + '",little(' + literal(recipe[field]) + ",8))\n"
    text += " val files=provideFiles(emptyMap())\n"
    if caller:
        text += ' startAt("main")\n'
    else:
        pointers = {
            "head": [("a0", "source")],
            "skip": [("a0", "source")],
            "decode": [("a0", "source"), ("a2", "record")],
            "bound": [("a0", "record")],
            "encode": [("a0", "record"), ("a1", "destination")],
            "size": [],
            "write": [("a2", "destination")],
        }[target]
        for reg, label in pointers:
            text += ' setRegisterToDataAddress("' + reg + '","' + label + '")\n'
        controls = {
            "head": [("a1", "count")],
            "skip": [("a1", "count"), ("a2", "depth")],
            "decode": [("a1", "count")],
            "bound": [],
            "encode": [("a2", "capacity")],
            "size": [("a0", "sizeArgument")],
            "write": [("a0", "writeMajor"), ("a1", "writeArgument")],
        }[target]
        for reg, field in controls:
            text += ' setRegister("' + reg + '",' + literal(recipe[field]) + ")\n"
        entries = {
            "head": "cbor_head",
            "skip": "cbor_skip",
            "decode": "telemetry_decode",
            "bound": "telemetry_bound",
            "encode": "telemetry_encode",
            "size": "head_size",
            "write": "write_head",
        }
        text += (
            ' setRegisterToTextAddress("ra","function_return");startAt("'
            + entries[target]
            + '")\n'
        )
    text += ' runUntil("' + cp + '")\n'

    def register(reg, value):
        nonlocal text
        if isinstance(value, str):
            name, _, offset = value.partition("+")
            expression = 'readDataAddress("' + name + '",' + (offset or "0") + "L)"
        else:
            expression = literal(value)
        text += ' expectRegister("' + reg + '",' + expression + ")\n"

    states = {
        1: {"a0": "source", "a1": 52},
        2: {"t0": 184, "v1": 0, "a2": 0, "a3": 0},
        3: {"t1": 5, "a2": 0},
        4: {"t0": 24},
        5: {"t3": 1, "t4": 2},
        6: {"a3": 4, "v1": 2},
        7: {"a3": 23, "v1": 1},
        8: {"v0": 0, "v1": 2, "a2": 5, "a3": 4},
        9: {"a1": 52, "a2": 0},
        10: {"s0": "source+2", "s1": 50, "s2": 0},
        11: {"s0": "source+1", "s2": 0},
        12: {"v0": 0, "v1": 1},
        13: {"s0": "source+3", "s1": 0},
        14: {"s2": 1},
        15: {"s4": 1},
        16: {"s4": 1, "s5": 0},
        17: {"s5": 1, "s0": "source+2"},
        18: {"s4": 0, "s0": "source+3"},
        19: {"a1": 52},
        20: {"s0": "source", "s1": 52},
        21: {"s0": "source+2", "s1": 50, "a2": 5, "a3": 4},
        22: {"s4": 8, "s5": 3},
        23: {"s4": 8, "s5": 3},
        24: {"s0": "source+12", "s1": 40},
        25: {"s0": "source+13"},
        26: {"s0": "source+5"},
        27: {"s0": "source+8", "s1": 44},
        28: {"s7": 10, "s6": 0},
        29: {"s6": 1, "s0": "source+16"},
        30: {"s1": 0, "s4": 15},
        32: {"v0": 0, "v1": 52},
        33: {"v0": 0, "v1": 1},
        34: {"v1": 2},
        35: {"v0": 0, "v1": 9},
        36: {"s1": 3, "s2": 10},
        37: {"s1": 3, "s2": 10},
        38: {"s3": 18},
        39: {"s3": 37},
        40: {"v0": 0, "v1": 37},
        41: {"v1": 2},
        42: {"t3": 1},
        45: {"v0": 0, "v1": 2},
        46: {"v0": 0, "v1": 37},
        47: {"s3": 37, "s4": "destination"},
        48: {"s1": "destination+11"},
        49: {"s1": "destination+13"},
        50: {"s1": "destination+16"},
        51: {"s1": "destination+18", "s2": 10},
        52: {"s1": "destination+37", "s2": 0},
        53: {"v0": 0, "v1": 37},
        54: {"v0": 0, "v1": 37},
        56: {"v0": 0, "v1": 241},
    }
    for reg, value in states.get(n, {}).items():
        register(reg, value)
    if n == 55:
        register("v0", 2 if target == "encode" else 1)
        register("v1", 0)
        text += " retained(saved)\n"
    text += ' image("source",bytes("' + recipe["inputHex"] + '"))\n'
    record = 'bytes("' + recipe["recordHex"] + '")'
    if n == 31:
        record = "flat(worked).copyOf(16)"
    if n == 32:
        record = "flat(worked)"
    if n in [54, 56]:
        record = 'flat(telemetry(bytes("' + recipe["inputHex"] + '"))!!)'
    text += ' image("record",' + record + ")\n"
    destination = "byteArrayOf()"
    if n in [42, 43, 44, 45]:
        destination = 'bytes("' + {42: "18", 43: "1818", 44: "17", 45: "1818"}[n] + '")'
    if 48 <= n <= 53:
        destination = (
            "preferred(worked).copyOf("
            + str({48: 11, 49: 13, 50: 16, 51: 18, 52: 37, 53: 37}[n])
            + ")"
        )
    if n in [54, 56]:
        destination = 'preferred(telemetry(bytes("' + recipe["inputHex"] + '"))!!)'
    text += ' image("destination",' + destination + ")\n"
    if n == 25:
        text += ' at("workspace",8192-288+96,little(worked.ticks.toLong(),8))\n'
    if n in [26, 27]:
        text += ' at("workspace",8192-288+104,little(3,4))\n'
    if n == 27:
        text += ' at("workspace",8192-288+112,worked.device)\n'
    if n in [28, 29]:
        text += ' at("workspace",8192-288+108,little(10,4))\n'
    if n == 29:
        text += ' at("workspace",8192-288+144,little(0,2))\n'
    if n in [30, 31, 32]:
        text += ' at("workspace",8192-288+96,flat(worked).copyOf(16));at("workspace",8192-288+112,worked.device);at("workspace",8192-288+144,flat(worked).copyOfRange(48,68))\n'
    if n in [1, 54, 56]:
        text += " retained(saved)\n"
    text += ' files.expectAllClosed();expectCondition(files.snapshot().isEmpty(),"Checkpoint performs no file I/O");expectOutput("")\n}\n'
    return text


def build():
    recipes = json.loads((FOLDER / "inputs.json").read_text())["recipes"]
    steps = json.loads((FOLDER / "steps.json").read_text())["steps"]
    model = (
        MODEL.read_text()
        + """\nfun AssemblyCase.at(label:String,offset:Int,expected:ByteArray){expectCondition(readBytes(label,expected.size,offset).contentEquals(expected.map{it.toInt() and 255}.toIntArray()),"Illustrated private field differs at $label+$offset")}
"""
    )
    DEST.mkdir(parents=True, exist_ok=True)
    counts = {}
    for step in steps:
        selected = [r for r in recipes if r["id"] in step["recipes"]]
        bodies = []
        for recipe in selected:
            if not recipe["callerOnly"]:
                bodies.append(checkpoint(recipe, False))
            bodies.append(checkpoint(recipe, True))
        (DEST / (step["id"] + ".kts")).write_text(
            format_kotlin(model + "\n" + "".join(bodies))
        )
        counts[step["id"]] = len(bodies)
    return counts


if __name__ == "__main__":
    counts = build()
    print(json.dumps({"scripts": len(counts), "cases": sum(counts.values())}))
