#!/usr/bin/env python3
"""Retain record construction while adding complete-file encoding and storage."""

import argparse, re
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def attach(source):
    if (
        source.count("\n.text\n") != 1
        or len(re.findall(r"^main:", source, re.M)) != 1
        or any(
            source.count("\n" + name + ":\n") != 1
            for name in (
                "video_encode_record",
                "video_index",
                "video_seek",
                "video_load",
            )
        )
    ):
        raise ValueError("Use one complete record-encoder working file")
    if re.search(
        r"^(?:fe_\w+|vs_\w+|file_encode_\w+|video_encode_file|video_store|record_encoder_example_main):",
        source,
        re.M,
    ):
        raise ValueError("File-encoder labels already exist")
    data, code = source.split("\n.text\n")
    if data.count("\nindex_file_data:") != 1:
        raise ValueError("Keep the existing staging buffer label")
    data = data.replace(
        "\nindex_file_data:", "\nfile_encode_output:\nindex_file_data:", 1
    )
    code = re.sub(r"^main:", "record_encoder_example_main:", code, count=1, flags=re.M)
    return (
        data
        + "\n"
        + (ROOT / "data.asm").read_text()
        + "\n.text\n"
        + (ROOT / "caller.asm").read_text()
        + "\n"
        + (ROOT / "encoder.asm").read_text()
        + "\n"
        + (ROOT / "store.asm").read_text()
        + "\n"
        + code
    )


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--source", type=Path, required=True)
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    result = attach(a.source.read_text())
    with a.output.open("x") as out:
        out.write(result)
