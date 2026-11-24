#!/usr/bin/env python3
"""Run MARS CRC file services on a real file and check the reported checksum."""

import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import zlib

ROOT = Path(__file__).resolve().parents[1]


def run(
    java,
    cli,
    output,
    capacity=7,
    method="bit",
    byte_count=None,
    input_file=None,
    source=None,
):
    if not 1 <= capacity <= 256 or method not in ("bit", "table"):
        raise ValueError("Choose capacity 1..256 and method bit or table")
    if byte_count is not None and (
        not 0 <= byte_count <= 65536 or input_file is not None
    ):
        raise ValueError(
            "Choose either --input or a generated --bytes count from 0 through 65536"
        )
    data = (
        input_file.read_bytes()
        if input_file is not None
        else (
            b"123456789"
            if byte_count is None
            else bytes((i * 73 + 11) & 255 for i in range(byte_count))
        )
    )
    if len(data) > 65536:
        raise ValueError("This repeatable runner limits input to 65536 bytes")
    source = source or ROOT / "revision/labs/files/crc32.asm"
    text = source.read_text()
    replacements = {
        "exercise: .word 0": "exercise: .word 3",
        "buffer_capacity: .word 7": f"buffer_capacity: .word {capacity}",
        "method: .word 0": f"method: .word {int(method == 'table')}",
    }
    for old, new in replacements.items():
        if text.count(old) != 1:
            raise ValueError("Use an unchanged worked CRC caller")
        text = text.replace(old, new, 1)
    output.mkdir(parents=True, exist_ok=False)
    caller = output / "file-crc.asm"
    caller.write_text(text)
    fixture = output / "fixture.bin"
    fixture.write_bytes(data)
    check = output / "result-addresses.kts"
    check.write_text(
        'case("result addresses") { println("ADDR,"+readDataAddress("file_crc")+","+readDataAddress("file_status")) }\n'
    )
    prefix = [str(java.resolve()), "-jar", str(cli.resolve())]
    query_command = prefix + ["grade", str(check.resolve()), str(caller.resolve())]
    query = subprocess.run(
        query_command, cwd=output, capture_output=True, text=True, timeout=180
    )
    (output / "address-query.stdout").write_text(query.stdout)
    (output / "address-query.stderr").write_text(query.stderr)
    rows = [
        line.split(",")
        for line in query.stdout.splitlines()
        if line.startswith("ADDR,")
    ]
    if query.returncode or len(rows) != 1:
        raise AssertionError("Could not obtain the assembled result addresses")
    low, high = map(int, rows[0][1:])
    dump = output / "result.hex"
    command = prefix + [
        "--warnings-as-errors",
        "--start-at-main",
        "--max-steps",
        "10000000",
        "--dump",
        f"{hex(low)}-{hex(high)}",
        "HexText",
        str(dump.resolve()),
        str(caller.resolve()),
    ]
    result = subprocess.run(
        command, cwd=output, capture_output=True, text=True, timeout=180
    )
    (output / "stdout.txt").write_text(result.stdout)
    (output / "stderr.txt").write_text(result.stderr)
    expected = zlib.crc32(data)
    actual = dump.read_text().lower().split() if dump.exists() else []
    if (
        result.returncode
        or result.stdout
        or actual != [f"{expected:08x}", "00000000"]
        or fixture.read_bytes() != data
    ):
        raise AssertionError(
            (result.returncode, actual, result.stdout[-1000:], result.stderr[-1000:])
        )
    sha = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
    receipt = dict(
        command=command,
        addressQuery=query_command,
        workingDirectory=str(output.resolve()),
        sourceSha256=sha(source),
        callerSha256=sha(caller),
        cliSha256=sha(cli),
        toolSha256=sha(Path(__file__)),
        inputBytes=len(data),
        inputSha256=sha(fixture),
        capacity=capacity,
        method=method,
        checksum=f"{expected:08x}",
        resultWords=actual,
        scope="Actual host-file syscall path and independent Python zlib checksum. The separate grade query obtains addresses without executing file operations. No virtual file fixture, forced short-read or native IDE claim.",
    )
    (output / "receipt.json").write_text(json.dumps(receipt, indent=2) + "\n")
    print(f"Verified {len(data)} bytes with {method}: {expected:08x}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    for name in ("java", "cli", "output"):
        parser.add_argument("--" + name, type=Path, required=True)
    parser.add_argument("--capacity", type=int, default=7)
    parser.add_argument("--method", choices=("bit", "table"), default="bit")
    group = parser.add_mutually_exclusive_group()
    group.add_argument("--bytes", type=int, dest="byte_count")
    group.add_argument("--input", type=Path, dest="input_file")
    parser.add_argument("--source", type=Path)
    run(**vars(parser.parse_args()))
