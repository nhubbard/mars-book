#!/usr/bin/env python3
"""Check the worked encoder's actual files; never generate an encoded stream."""

import argparse
import importlib.util
import json
from pathlib import Path
import tempfile

ROOT = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location(
    "h263_native_stream_check", ROOT / "h263-picture-construction/check_stream.py"
)
CHECK = importlib.util.module_from_spec(spec)
spec.loader.exec_module(CHECK)


def inspect(folder):
    folder = Path(folder).resolve()
    raw = (folder / "clip.yuv").read_bytes()
    if len(raw) != 8 * 38016:
        raise ValueError("This worked caller requires eight complete QCIF pictures")
    if (folder / "truncated.yuv").read_bytes() != raw[: 38016 + 16]:
        raise ValueError(
            "Truncated input must retain one picture and sixteen bytes of the next"
        )
    if (folder / "trailing.yuv").read_bytes() != raw[: 2 * 38016] + b"\xa5":
        raise ValueError(
            "Trailing input must retain two pictures followed by one extra A5 byte"
        )
    results = []
    for name, stream, local, frames, status in [
        ("complete", "encoded.263", "reconstructed.yuv", 8, 0),
        ("truncated", "truncated.263", "truncated-local.yuv", 1, 3),
        ("trailing", "trailing.263", "trailing-local.yuv", 2, 3),
    ]:
        with tempfile.TemporaryDirectory(prefix="mars-h263-accepted-") as temporary:
            prefix = Path(temporary) / "accepted-input.yuv"
            prefix.write_bytes(raw[: frames * 38016])
            result = CHECK.check(
                folder / stream, prefix, folder / local, frames=frames, tick_step=6
            )
        results.append(
            dict(
                experiment=name,
                expectedLibraryStatus=status,
                acceptedPictures=frames,
                interpretation=(
                    "Complete request succeeded"
                    if status == 0
                    else "Only the accepted output prefix is verified; the complete request failed"
                ),
                verification=result,
            )
        )
    return dict(
        scope="Actual MIPS-produced complete output and accepted prefixes: restricted syntax, independent decoding, source quality and local-reference drift. Expected library statuses describe the worked caller; inspect its console or Debug state for the actual return values. This is not the full inverse-accuracy gate.",
        experiments=results,
    )


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("folder", type=Path)
    parser.add_argument("--report", type=Path, required=True)
    args = parser.parse_args()
    if args.report.exists():
        parser.error("Existing report refused")
    try:
        result = inspect(args.folder)
    except (ValueError, OSError) as error:
        parser.error(str(error))
    args.report.parent.mkdir(parents=True, exist_ok=True)
    with args.report.open("x") as output:
        json.dump(result, output, indent=2, allow_nan=False)
        output.write("\n")
    print(
        "Verified eight complete pictures, one accepted truncated-input picture, and two accepted trailing-input pictures; the latter requests remain failures."
    )
