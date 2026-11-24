#!/usr/bin/env python3
"""Build standalone student WAV feedback checks from a public host model."""

from kotlin_format import format_kotlin
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]
MODEL = ROOT / "tools/wav-feedback/model.kts"
DEST = ROOT / "drafts/pcm-wav-construction/checks"


def cases():
    groups = {
        name: []
        for name in ["bound", "parse", "filter", "encode", "format", "files", "caller"]
    }

    def add(group, title, body, limit=100000000):
        groups[group].append(
            "case("
            + json.dumps(title)
            + ", instructionLimit="
            + str(limit)
            + ") { "
            + body
            + " }\n"
        )

    for hz, ch, n in [
        (8000, 1, 0),
        (16000, 2, 4),
        (48000, 2, 65536),
        (7999, 1, 1),
        (48001, 2, 1),
        (16000, 0, 1),
        (16000, 3, 1),
        (16000, 1, 65537),
        (16000, 2, 4294967300),
    ]:
        add("bound", f"prefix {hz} {ch} {n}", f"sizeCheck({hz}L,{ch}L,{n}L)")
    for expression in ["Long.MIN_VALUE", "-1L"]:
        add(
            "bound",
            "unsigned frame " + expression,
            "sizeCheck(16000,2," + expression + ")",
        )
    add("bound", "unsigned channel field", "sizeCheck(16000,4294967295L,1)")
    add("parse", "worked ancillary chunks", "parseCheck(workedFile)")
    for ch in [1, 2]:
        for n in [0, 1, 9, 65]:
            add(
                "parse",
                f"parse {ch} channels {n} frames",
                f"parseCheck(sampleFile(22050,{ch},{n},extended={str(n%2==1).lower()}))",
            )
    add(
        "parse",
        "upper frame count",
        "parseCheck(sampleFile(48000,2,65536,extras=false))",
    )
    add(
        "parse",
        "exact overhead budget",
        'parseCheck(container(waveChunk("JUNK",ByteArray(4044))+writeWave(8000,1,byteArrayOf()).copyOfRange(12,44)))',
    )
    add(
        "parse",
        "zero-length unknown chunk",
        'parseCheck(container(waveChunk("zero",byteArrayOf())+writeWave(8000,1,byteArrayOf()).copyOfRange(12,44)))',
    )
    for ch in [1, 2]:
        for n in [0, 1, 7, 64, 65]:
            add(
                "filter",
                f"average {ch} channels {n} frames",
                f"filterCheck(16000,{ch}L,{n}L)",
            )
    for hz, ch, n in [
        (7999, 1, 1),
        (48001, 2, 1),
        (16000, 0, 0),
        (16000, 3, 1),
        (16000, 1, 65537),
        (16000, 1, 4294967300),
    ]:
        add(
            "filter",
            f"bad filter prefix {hz} {ch} {n}",
            f"filterCheck({hz}L,{ch}L,{n}L,byteArrayOf())",
        )
    add(
        "filter",
        "signed rounding ties",
        'filterCheck(16000,1,1,bytes("00000000000000000000000000000040"),bytes("0100")); filterCheck(16000,1,1,bytes("00000000000000000000000000000040"),bytes("ffff"))',
    )
    add(
        "filter",
        "eight wide products",
        'filterCheck(16000,1,8,bytes("0080".repeat(8)),bytes("0080".repeat(8)))',
    )
    add(
        "filter",
        "asymmetric coefficients",
        'filterCheck(48000,2,65,bytes("feff0700f6ff1300e2ff2b00caff7f00"))',
    )
    add(
        "filter",
        "clip total exceeds sixteen bits",
        'filterCheck(16000,2,40000,bytes("0080".repeat(8)),bytes("0080".repeat(80000)))',
    )
    for hz, ch, n, cap in [
        (8000, 1, 0, 44),
        (16000, 2, 4, 60),
        (48000, 2, 65536, 262188),
        (16000, 1, 1, 45),
        (16000, 2, 4, 59),
        (16000, 2, 4, 0),
        (7999, 1, 1, 100),
        (48001, 1, 1, 100),
        (16000, 0, 0, 100),
        (16000, 3, 1, 100),
        (16000, 1, 65537, 266240),
        (16000, 2, 4294967300, 266240),
    ]:
        add(
            "encode",
            f"encode {hz} {ch} {n} capacity {cap}",
            f"encodeCheck({hz}L,{ch}L,{n}L,{cap}L)",
        )
    for expression in ["Long.MIN_VALUE", "-1L"]:
        add(
            "encode",
            "unsigned capacity " + expression,
            "encodeCheck(16000,2,4," + expression + ")",
        )
    for n in [0, 11, 12, 43, 81]:
        add("format", f"truncate worked to {n}", f"parseCheck(workedFile.copyOf({n}))")
    for at, value in [
        (0, 0),
        (8, 0),
        (4, 73),
        (4, 75),
        (32, 3),
        (34, 0),
        (34, 3),
        (46, 8),
        (44, 2),
        (40, 1),
    ]:
        add(
            "format",
            f"alter field byte {at} to {value}",
            f"parseCheck(workedFile.copyOf().also {{ it[{at}]={value}.toByte() }})",
        )
    base = "writeWave(16000,2,patternedPcm(2,4))"
    fmt = 'waveChunk("fmt ",' + base + ".copyOfRange(20,36))"
    data = 'waveChunk("data",' + base + ".copyOfRange(44,60))"
    for title, body in [
        ("missing format", data),
        ("missing data", fmt),
        ("data precedes format", data + "+" + fmt),
        ("repeated format", fmt + "+" + fmt + "+" + data),
        ("repeated data", fmt + "+" + data + "+" + data),
        ("odd incomplete stereo frame", fmt + '+waveChunk("data",bytes("010203"))'),
        (
            "unsupported format length",
            'waveChunk("fmt ",' + base + ".copyOfRange(20,36)+byteArrayOf(0))+" + data,
        ),
        (
            "nonempty extension",
            'waveChunk("fmt ",'
            + base
            + ".copyOfRange(20,36)+byteArrayOf(1,0))+"
            + data,
        ),
        (
            "wrapped padded chunk",
            '"JUNK".toByteArray()+bytes("ffffffff")+' + fmt + "+" + data,
        ),
        (
            "overhead beyond budget",
            'waveChunk("JUNK",ByteArray(4046))+' + fmt + "+" + data,
        ),
        (
            "missing final odd pad",
            fmt + "+" + data + '+waveChunk("note",byteArrayOf(1)).copyOf(9)',
        ),
    ]:
        add("format", title, "parseCheck(container(" + body + "))")
    for count in ["0L", "43L", "266241L", "Long.MIN_VALUE", "-1L"]:
        add(
            "format",
            "early parse count " + count,
            "parseCheck(workedFile," + count + ")",
        )
    for load in [True, False]:
        flag = str(load).lower()
        verb = "load" if load else "save"
        for size in [1, 5, 19, 4096]:
            add(
                "files",
                verb + f" short transfer {size}",
                f"fileCheck({flag},workedFile,transfer={size})",
            )
        add(
            "files",
            verb + " denied open",
            f"fileCheck({flag},workedFile,allowed=false)",
        )
        for failure in [0, 17, 82]:
            add(
                "files",
                verb + f" injected failure {failure}",
                f"fileCheck({flag},workedFile,failure={failure})",
            )
        add(
            "files",
            verb + " malformed trailer",
            f"fileCheck({flag},workedFile+byteArrayOf(0))",
        )
        add(
            "files",
            verb + " empty extended container",
            f"fileCheck({flag},sampleFile(8000,1,0,extended=true))",
        )
    for cap in ["0L", "1L", "81L", "82L", "83L", "266241L", "Long.MIN_VALUE", "-1L"]:
        add(
            "files",
            "loader capacity " + cap,
            "fileCheck(true,workedFile,capacity=" + cap + ")",
        )
    for count in ["0L", "43L", "266241L", "Long.MIN_VALUE", "-1L"]:
        add(
            "files",
            "saver selected count " + count,
            "fileCheck(false,workedFile,selected=" + count + ")",
        )
    for title, body in [
        ("worked pipeline", "pipelineCheck(workedFile)"),
        ("empty pipeline", "pipelineCheck(sampleFile(8000,1,0,extended=true))"),
        ("block boundary pipeline", "pipelineCheck(sampleFile(48000,2,65))"),
        ("malformed pipeline", "pipelineCheck(workedFile+byteArrayOf(0))"),
        ("missing pipeline input", "pipelineCheck(workedFile,missing=true)"),
    ]:
        add("caller", title, body)
    return groups


def build():
    DEST.mkdir(exist_ok=True)
    model = MODEL.read_text()
    groups = cases()
    for domain, declarations in groups.items():
        (DEST / (domain + ".kts")).write_text(
            format_kotlin(model + "\n" + "".join(declarations))
        )
    receipt = {domain: len(declarations) for domain, declarations in groups.items()}
    (DEST / "cases.json").write_text(json.dumps(receipt, indent=2) + "\n")
    print(json.dumps(receipt))


if __name__ == "__main__":
    build()
