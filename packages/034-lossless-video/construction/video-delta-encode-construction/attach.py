#!/usr/bin/env python3
"""Attach delta construction and namespace the earlier XOR example's local labels."""

import argparse, re
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def attach(source, kernel):
    if (
        source.count("\n.text\n") != 1
        or len(re.findall(r"^main:", source, re.M)) != 1
        or source.count("\nvideo_encode_full:\n") != 1
    ):
        raise ValueError("Use one full-encoder working file")
    if kernel.count("\n.text\n") != 1 or any(
        kernel.count("\n" + n + ":\n") != 1
        for n in ("video_xor_words", "video_xor_words_msa")
    ):
        raise ValueError("Supply the earlier XOR working file")
    if re.search(
        r"^(?:dr_\w+|delta_\w+|drCrc_\w+|deltaKernel_\w+|video_encode_record|video_record_crc|video_xor_words|video_xor_words_msa|full_encoder_example_main):",
        source,
        re.M,
    ):
        raise ValueError("Delta/kernel labels already exist")
    labels = re.findall(r"^\s*([^\s:#]+):", kernel, re.M)
    if any(re.fullmatch(r"[A-Za-z_]\w*", n) is None for n in labels):
        raise ValueError("Use ordinary identifier labels in the guided kernel file")
    keep = {"video_xor_words", "video_xor_words_msa"}
    rename = {n: "deltaKernel_" + n for n in labels if n not in keep}
    token = re.compile(
        r"(?<![\w.$])(" + "|".join(sorted(rename, key=len, reverse=True)) + r")\b"
    )

    def operands(text):
        parts = re.split(r"""("(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|#[^\n]*)""", text)
        return "".join(
            (
                part
                if part.startswith(('"', "'", "#"))
                else token.sub(lambda m: rename[m[0]], part)
            )
            for part in parts
        )

    def line(text):
        match = re.match(r"^(\s*)([A-Za-z_]\w*):", text)
        prefix = ""
        if match:
            prefix = match[1] + rename.get(match[2], match[2]) + ":"
            text = text[match.end() :]
        match = re.match(r"^(\s*)([^\s#]+)(.*)$", text)
        if match:
            return prefix + match[1] + match[2] + operands(match[3])
        return prefix + text

    kernel = "\n".join(line(text) for text in kernel.split("\n"))
    data, code = source.split("\n.text\n")
    kdata, kcode = kernel.split("\n.text\n")
    kdata = re.sub(r"^\.data[^\n]*\n", "", kdata, count=1, flags=re.M)
    code = re.sub(r"^main:", "full_encoder_example_main:", code, count=1, flags=re.M)
    return (
        data
        + "\n"
        + kdata
        + "\n"
        + (ROOT / "data.asm").read_text()
        + "\n.text\n"
        + (ROOT / "caller.asm").read_text()
        + "\n"
        + (ROOT / "encoder.asm").read_text()
        + "\n"
        + (ROOT / "crc.asm").read_text()
        + "\n"
        + kcode
        + "\n"
        + code
    )


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--source", type=Path, required=True)
    p.add_argument("--kernel-source", type=Path, required=True)
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    result = attach(a.source.read_text(), a.kernel_source.read_text())
    with a.output.open("x") as out:
        out.write(result)
