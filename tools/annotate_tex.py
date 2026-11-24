#!/usr/bin/env python3
"""Create an editable native LaTeX figure from a real PNG and coordinate receipt."""

import argparse
import json
import re
from pathlib import Path

from annotate import validate_capture


def tex_text(text):
    escapes = {
        "\\": r"\textbackslash{}",
        "{": r"\{",
        "}": r"\}",
        "#": r"\#",
        "$": r"\$",
        "%": r"\%",
        "&": r"\&",
        "_": r"\_",
        "~": r"\textasciitilde{}",
        "^": r"\textasciicircum{}",
    }
    return "".join(escapes.get(char, char) for char in " ".join(text.split()))


def render(png, receipt, output, caption, alt, *, crop=None, image_reference=None):
    _, width, height = validate_capture(png, receipt)
    if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9:_-]*", receipt["figure"]):
        raise ValueError("Figure needs a stable native label ID")
    if not caption.strip() or not alt.strip() or not receipt["callouts"]:
        raise ValueError("Caption, alternative description and callouts are required")
    reference = str(png.resolve()) if image_reference is None else str(image_reference)
    if any(char in reference for char in "{}\\%\n\r\t\0"):
        raise ValueError("Unsafe image reference")
    if Path(reference).resolve() != png.resolve():
        raise ValueError("Image reference must resolve to the verified original PNG")
    x, y, w, h = (0, 0, width, height) if crop is None else crop
    if (
        any(type(value) is not int for value in (x, y, w, h))
        or min(x, y) < 0
        or min(w, h) <= 0
        or x + w > width
        or y + h > height
    ):
        raise ValueError("Crop is outside the screenshot")
    coordinates = []
    for callout in receipt["callouts"]:
        box = callout["box"]
        if (
            box["x"] < x
            or box["y"] < y
            or box["x"] + box["width"] > x + w
            or box["y"] + box["height"] > y + h
        ):
            raise ValueError("Crop excludes a callout; select a different composition")
        left, right = (box["x"] - x) / w, (box["x"] + box["width"] - x) / w
        top, bottom = 1 - (box["y"] - y) / h, 1 - (box["y"] + box["height"] - y) / h
        coordinates.append((left, right, top, bottom))
    # Measure the verified image as an artifact. Fractions of its natural dimensions
    # preserve pixel crops even when resolution metadata changes its physical size.
    image_path = r"\detokenize{" + reference + "}"
    image = (
        r"\includegraphics[width=\linewidth,artifact,"
        + r"trim={\MARSCaptureLeft{} \MARSCaptureBottom{} \MARSCaptureRight{} \MARSCaptureTop{}},clip]{"
        + image_path
        + "}"
    )
    trims = [
        ("Left", x / width, "wd"),
        ("Bottom", (height - y - h) / height, "ht"),
        ("Right", (width - x - w) / width, "wd"),
        ("Top", y / height, "ht"),
    ]
    lines = [
        "% Generated once from a verified raw capture; edit this native include directly.",
        "% Raw PNG SHA-256: " + receipt["pngSha256"],
        r"\begin{figure}[htbp]",
        r"\centering",
        r"\sbox0{\includegraphics[artifact]{" + image_path + "}}",
    ]
    lines += [
        f"\\edef\\MARSCapture{name}{{\\the\\dimexpr{fraction:.9f}\\{dimension}0\\relax}}"
        for name, fraction, dimension in trims
    ]
    lines += [
        r"\begin{tikzpicture}[alt={" + tex_text(alt) + "}]",
        r"\node[anchor=south west,inner sep=0] (capture) at (0,0) {" + image + "};",
        r"\begin{scope}[x={(capture.south east)},y={(capture.north west)}]",
    ]
    for index, (left, right, top, bottom) in enumerate(coordinates, 1):
        lines += [
            f"\\draw[black,line width=0.7pt] ({left:.9f},{bottom:.9f}) rectangle ({right:.9f},{top:.9f});",
            f"\\node[draw=black,fill=white,circle,inner sep=1.5pt,font=\\footnotesize] at ({max(0.015, left - .015):.9f},{top:.9f}) {{{index}}};",
        ]
    lines += [
        r"\end{scope}",
        r"\end{tikzpicture}",
        r"\begin{minipage}{\linewidth}",
        r"\begin{enumerate}",
    ]
    lines += [r"\item " + tex_text(callout["label"]) for callout in receipt["callouts"]]
    lines += [
        r"\end{enumerate}",
        r"\end{minipage}",
        r"\caption{" + tex_text(caption) + "}",
        r"\label{fig:" + receipt["figure"] + "}",
        r"\end{figure}",
        "",
    ]
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("x") as destination:
        destination.write("\n".join(lines))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("png", type=Path)
    parser.add_argument("receipt", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--caption", required=True)
    parser.add_argument("--alt", required=True)
    parser.add_argument(
        "--crop", type=int, nargs=4, metavar=("X", "Y", "WIDTH", "HEIGHT")
    )
    parser.add_argument("--image-reference")
    args = parser.parse_args()
    render(
        args.png,
        json.loads(args.receipt.read_text()),
        args.output,
        args.caption,
        args.alt,
        crop=args.crop,
        image_reference=args.image_reference,
    )
