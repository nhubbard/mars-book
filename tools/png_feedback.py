#!/usr/bin/env python3
"""Build public PNG feedback from independently encoded, known pixel observations."""

from kotlin_format import format_kotlin
import importlib.util, json, struct, zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "drafts/png-feedback/checks"


def load(name, path):
    s = importlib.util.spec_from_file_location(name, path)
    m = importlib.util.module_from_spec(s)
    s.loader.exec_module(m)
    return m


def cases():
    fixture = load("png_public_vectors", ROOT / "drafts/png-fixtures/fixture.py")
    huff = load(
        "png_public_huffman_vectors",
        ROOT / "drafts/png-huffman-construction/fixtures.py",
    )
    rows = load(
        "png_public_row_vectors", ROOT / "drafts/png-row-construction/fixtures.py"
    )
    groups = {
        n: []
        for n in (
            "loader",
            "collect",
            "inflate",
            "rows",
            "pack",
            "decode",
            "reuse",
            "caller",
        )
    }
    decl = []
    vectors = {}

    def value(raw):
        h = raw.hex()
        if h not in vectors:
            name = "pngData" + str(len(vectors))
            vectors[h] = name
            decl.append("val " + name + "=octets(" + json.dumps(h) + ")")
        return vectors[h]

    def add(group, name, body, little):
        groups[group].append(
            "case("
            + json.dumps(name + f" little={little}")
            + ",instructionLimit=30000000,byteOrder=ByteOrder."
            + ("LITTLE_ENDIAN" if little else "BIG_ENDIAN")
            + "){"
            + body
            + "}\n"
        )

    known = []
    for w in (1, 2, 15, 16, 17):
        for compression in ("stored", "fixed", "auto"):
            m = fixture.material(w, 5, compression)
            m.update(width=w, height=5, name=f"{w}x5-{compression}")
            known.append(m)
    worked = known[3]
    assert worked["width"] == 2 and len(worked["filtered"]) == 35
    container = []

    def bad(name, raw):
        container.append((name, raw))

    raw = worked["png"]
    head = raw[8:33]
    stream = worked["zlib"]
    chunk = fixture.chunk
    sig = fixture.SIGNATURE
    for n in (0, 1, 7, 8, 32, len(raw) - 1):
        bad("truncated-" + str(n), raw[:n])
    bad("signature", bytes([raw[0] ^ 1]) + raw[1:])
    bad("outer-crc", raw[:29] + bytes([raw[29] ^ 1]) + raw[30:])
    bad("file-suffix", raw + b"x")
    bad(
        "duplicate-header",
        sig + head + head + chunk(b"IDAT", stream) + chunk(b"IEND", b""),
    )
    bad(
        "split-IDAT-run",
        sig
        + head
        + chunk(b"IDAT", stream[:1])
        + chunk(b"tEXt", b"")
        + chunk(b"IDAT", stream[1:])
        + chunk(b"IEND", b""),
    )
    bad("no-IDAT", sig + head + chunk(b"IEND", b""))
    bad("empty-IDAT", sig + head + chunk(b"IDAT", b"") + chunk(b"IEND", b""))
    bad("IEND-payload", sig + head + chunk(b"IDAT", stream) + chunk(b"IEND", b"x"))
    for kind in (b"tRNS", b"acTL", b"fcTL", b"fdAT", b"JUNK", b"a1CD", b"abcd"):
        bad(
            "unsupported-" + kind.decode(),
            sig
            + head
            + chunk(kind, b"")
            + chunk(b"IDAT", stream)
            + chunk(b"IEND", b""),
        )
    for w, h, depth, color, method, filtering, interlace in [
        (513, 5, 8, 2, 0, 0, 0),
        (2, 0, 8, 2, 0, 0, 0),
        (2, 5, 16, 2, 0, 0, 0),
        (2, 5, 8, 0, 0, 0, 0),
        (2, 5, 8, 2, 1, 0, 0),
        (2, 5, 8, 2, 0, 1, 0),
        (2, 5, 8, 2, 0, 0, 1),
    ]:
        bad(
            "header-" + str((w, h, depth, color, method, filtering, interlace)),
            sig
            + chunk(
                b"IHDR",
                struct.pack(
                    ">IIBBBBB", w, h, depth, color, method, filtering, interlace
                ),
            )
            + chunk(b"IDAT", stream)
            + chunk(b"IEND", b""),
        )
    inflate = []
    for m in known:
        inflate.append((m["name"], m["zlib"], m["filtered"], 0, len(m["filtered"])))
    valid_huff = {
        "fixed-literals",
        "fixed-match",
        "dynamic-literals",
        "repeat16",
        "repeat17",
        "repeat18",
        "mixed",
    }
    for name in huff.NAMES:
        m = huff.material(name)
        good = name in valid_huff
        if good:
            assert zlib.decompress(m["zlib"]) == m["plain"]
        inflate.append((name, m["zlib"], m["plain"], 0 if good else 1, len(m["plain"])))
    for n in (0, 1, 5, len(stream) - 1):
        inflate.append(("short-zlib-" + str(n), stream[:n], worked["filtered"], 1, 35))
    inflate.extend(
        [
            ("wrong-produced-count", stream, worked["filtered"], 1, 36),
            ("Adler-collision-produced-count", zlib.compress(b"A"), b"", 1, 65522),
            ("empty-compressed", zlib.compress(b""), b"", 0, 0),
            ("trailing-compressed", stream + b"x", worked["filtered"], 1, 35),
        ]
    )
    for little in (False, True):
        for n in (0, 1, 115, 4097):
            payload = bytes((37 * i + 11) & 255 for i in range(n))
            for cap in sorted({max(1, n), max(1, n - 1), max(1, n + 1)}):
                add(
                    "loader",
                    f"{n} bytes capacity {cap}",
                    f"loader({value(payload)},{cap}L,{0 if n and n<=cap else 1},{str(little).lower()})",
                    little,
                )
        for name, extra in [("zero-capacity", "0L"), ("wide-capacity", "4294967296L")]:
            add(
                "loader",
                name,
                f'loader({value(b"A")},{extra},1,{str(little).lower()})',
                little,
            )
        add(
            "loader",
            "missing-file",
            f'loader({value(b"")},115L,1,{str(little).lower()},missing=true)',
            little,
        )
        add(
            "loader",
            "error-at-EOF",
            f'loader({value(b"ABCDE")},5L,1,{str(little).lower()},failure=5)',
            little,
        )
        for m in known:
            add(
                "collect",
                m["name"],
                f'collect({value(m["png"])},{m["width"]},5,{value(m["zlib"])},0,{str(little).lower()})',
                little,
            )
        add(
            "collect",
            "short-joined-capacity",
            f"collect({value(raw)},2,5,{value(stream)},1,{str(little).lower()},capacity={len(stream)-1})",
            little,
        )
        for name, data in container:
            add(
                "collect",
                name,
                f'collect({value(data)},0,0,{value(b"")},1,{str(little).lower()})',
                little,
            )
        for name, data, plain, status, expected in inflate:
            add(
                "inflate",
                name,
                f"inflate({value(data)},{value(plain)},{status},{str(little).lower()},expected={expected}L)",
                little,
            )
        for msa in (False, True):
            for m in known[::3]:
                w = m["width"]
                stride = w + 3
                cap = 4 * (4 * stride + w)
                add(
                    "rows",
                    m["name"] + f" msa={msa}",
                    f'rows({value(m["filtered"])},{value(m["rgb"])},{w},5,0,{str(msa).lower()},{str(little).lower()})',
                    little,
                )
                for capacity, status in ((cap, 0), (cap - 1, 1)):
                    add(
                        "pack",
                        m["name"] + f" capacity={capacity} msa={msa}",
                        f'pack({value(m["filtered"])},{value(m["rgb"])},{w},5,{stride},{capacity},{status},{str(msa).lower()},{str(little).lower()})',
                        little,
                    )
            bad_row = rows.material("bad-selector")
            add(
                "rows",
                "late-selector" + str(msa),
                f'rows({value(bad_row["filtered"])},{value(bad_row["rgb"])},2,5,1,{str(msa).lower()},{str(little).lower()})',
                little,
            )
            for m in known:
                add(
                    "decode",
                    m["name"] + f" msa={msa}",
                    f'decode({value(m["png"])},{value(m["rgb"])},{m["width"]},5,0,{str(msa).lower()},{str(little).lower()})',
                    little,
                )
            for name, data in container:
                add(
                    "decode",
                    name + f" msa={msa}",
                    f'decode({value(data)},{value(b"")},2,5,1,{str(msa).lower()},{str(little).lower()})',
                    little,
                )
            add(
                "decode",
                "late-filter" + str(msa),
                f'decode({value(bad_row["png"])},{value(bad_row["rgb"])},2,5,1,{str(msa).lower()},{str(little).lower()})',
                little,
            )
            for name, kw in [
                ("short-rows", "rowcap=34"),
                ("short-compressed", f"compcap={len(stream)-1}"),
                ("short-frame", "framecap=87"),
            ]:
                add(
                    "decode",
                    name + str(msa),
                    f'decode({value(raw)},{value(worked["rgb"])},2,5,1,{str(msa).lower()},{str(little).lower()},{kw})',
                    little,
                )
            sequence = []
            for i, m in enumerate([worked, bad_row, known[12], worked]):
                sequence.append(
                    f'decode({value(m["png"])},{value(m["rgb"])},{m.get("width",2)},5,{1 if i==1 else 0},{str(msa).lower()},{str(little).lower()},reuse={str(i>0).lower()})'
                )
            add(
                "reuse",
                "success/late-failure/fresh/success " + str(msa),
                ";".join(sequence),
                little,
            )
    # Caller feedback uses the actual supplied entry and its independent file backend.
    caller = """fun AssemblyCase.caller(raw:ByteArray,rgb:ByteArray,w:Int,h:Int,mode:Int,status:Int,little:Boolean){val files=provideFiles(mapOf("image.png" to raw),maxReadSize=7,firstDescriptor=19);setWord("vector_mode",mode);startAt("main");runUntil("decode_return");expectRegister("v0",status);files.expectAllClosed();val base=readDataAddress("png_context");if(status==0){val frame=readRegister("s0");for(y in 0 until h)for(x in 0 until w){val at=3*(y*w+x);val v=((rgb[at].toInt()and 255)shl 16)or((rgb[at+1].toInt()and 255)shl 8)or(rgb[at+2].toInt()and 255);val b=readBytes(frame+4*(512*y+x),4);var n=0;for(j in 0..3)n=n or(b[j]shl(8*(if(little)j else 3-j)));expectCondition(n==v,"Actual caller pixels")};val values=readBytes(base+48,24);val expected=IntArray(24);for((i,v)in listOf(w,h,4*w*h).withIndex())field(expected,8*i,v.toLong(),little);expectCondition(values.contentEquals(expected),"Actual caller final fields")}else expectCondition(readBytes(base+48,24).all{it==165},"Rejected caller retains result sentinels");runToExit(if(status==0)0 else 1);expectOutput(if(status==0)"PNG displayed.\\n"else"Invalid or unsupported PNG.\\n")}
"""
    for little in (False, True):
        for mode in (0, 1):
            for m, status in [(worked, 0), (rows.material("bad-selector"), 1)]:
                add(
                    "caller",
                    f"actual main mode={mode} status={status}",
                    f'caller({value(m["png"])},{value(m["rgb"])},2,5,{mode},{status},{str(little).lower()})',
                    little,
                )
    return groups, decl, caller


def build():
    DEST.mkdir(parents=True, exist_ok=True)
    groups, declarations, caller = cases()
    common = (ROOT / "tools/png-feedback/common.kts").read_text()
    shared = common + "\n".join(declarations) + "\n"
    for name, lines in groups.items():
        (DEST / (name + ".kts")).write_text(
            format_kotlin(
                shared + (caller if name == "caller" else "") + "".join(lines)
            )
        )
    counts = {k: len(v) for k, v in groups.items()}
    (ROOT / "drafts/png-feedback/counts.json").write_text(
        json.dumps(counts, indent=2) + "\n"
    )
    return counts


if __name__ == "__main__":
    print(json.dumps(build(), indent=2))
