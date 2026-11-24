#!/usr/bin/env python3
"""Validate and compile directly edited LaTeX. Never regenerate prose from task metadata."""

import argparse
from contextlib import contextmanager
import errno
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import struct
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "book/highlighting/intellij-light.json"
CODE = re.compile(
    r"\\begin\{minted\}(?:\[[^\]]*\])?\{mars-idea\}\n(.*?)\\end\{minted\}", re.S
)
INCLUDE = re.compile(r"\\(?:input|include)\{([^}]+)\}")


def sources(entry="main.tex", root=ROOT):
    """Walk literal native includes in publication order; a path cannot leave this repository."""

    def visit(path, stack):
        path = path.resolve()
        if not path.is_relative_to(root.resolve()) or path in stack:
            raise ValueError(f"Escaping or cyclic LaTeX include: {path}")
        text = path.read_text(encoding="utf-8")
        yield path, text
        # This manuscript uses root-relative includes. Comments do not activate includes.
        uncommented = re.sub(r"(?<!\\)%[^\n]*", "", text)
        for name in INCLUDE.findall(uncommented):
            child = root / name
            if child.suffix != ".tex":
                child = child.with_suffix(".tex")
            yield from visit(child, stack + [path])

    return list(visit(root / entry, []))


def snippets(entry="main.tex", root=ROOT):
    return [
        match[1] for _, text in sources(entry, root) for match in CODE.finditer(text)
    ]


def validate(entry="main.tex"):
    document = json.loads(MANIFEST.read_text())
    if document["formatVersion"] != 1:
        raise ValueError("Unsupported IntelliJ export")
    records = {}
    for export in document["exports"]:
        source = export["source"]
        digest = hashlib.sha256(source.encode()).hexdigest()
        if digest != export["sha256"] or digest in records:
            raise ValueError("Corrupt or duplicate IntelliJ export")
        offset = 0
        for run in export["runs"]:
            if run["start"] != offset or not offset < run["end"] <= len(source):
                raise ValueError("Invalid Unicode highlight span coverage")
            offset = run["end"]
        if offset != len(source):
            raise ValueError("Incomplete highlight span coverage")
        records[digest] = source
    code = snippets(entry)
    for source in code:
        if records.get(hashlib.sha256(source.encode()).hexdigest()) != source:
            raise ValueError(
                "Source changed: export fresh IntelliJ highlighting with the authoring plugin"
            )
    print(
        f"Valid native composition: {len(sources(entry))} included files, {len(code)} highlighted code blocks"
    )
    return code


def export_input():
    folder = ROOT / "build/highlight-input"
    folder.mkdir(parents=True, exist_ok=True)
    for path in folder.glob("*.asm"):
        path.unlink()
    entries = ["main.tex", "specimen.tex"]
    entries += [
        entry
        for entry in (
            "revision/learning.tex",
            "revision/lab-book.tex",
            "revision/advanced-pilots.tex",
        )
        if (ROOT / entry).is_file()
    ]
    for source in [source for entry in entries for source in snippets(entry)]:
        digest = hashlib.sha256(source.encode()).hexdigest()
        (folder / (digest + ".asm")).write_text(source, encoding="utf-8")
    print(folder)


def check_fonts():
    folder = Path(
        os.environ.get("MARS_FONT_DIR", Path.home() / "Library/Fonts")
    ).resolve()
    names = [
        "MinionPro-Regular.otf",
        "MinionPro-It.otf",
        "MinionPro-Bold.otf",
        "MinionPro-BoldIt.otf",
        "NotoSansMath-Regular.otf",
    ]
    for name in names:
        if not (folder / name).is_file():
            raise ValueError(
                f"Missing installed font {name}; set MARS_FONT_DIR to its directory"
            )
    data = (folder / names[-1]).read_bytes()
    count = struct.unpack_from(">H", data, 4)[0]
    tags = {data[12 + i * 16 : 16 + i * 16] for i in range(count)}
    if b"MATH" not in tags:
        raise ValueError("Noto Sans Math needs an OpenType MATH table")
    return folder


def reference_state(recorder, root=ROOT):
    """Fingerprint reference files actually read or written by this LuaLaTeX job."""
    references = set()
    for line in recorder.read_text().splitlines():
        kind, separator, name = line.partition(" ")
        if separator and kind in ("INPUT", "OUTPUT"):
            path = Path(name)
            if not path.is_absolute():
                path = root / path
            if path.suffix in (".aux", ".toc", ".out"):
                references.add(path.resolve())
    if not references:
        raise ValueError("LuaLaTeX recorder contains no reference files")
    digest = hashlib.sha256()
    for path in sorted(references):
        digest.update(str(path).encode())
        digest.update(b"\0")
        digest.update(path.read_bytes())
        digest.update(b"\0")
    return digest.digest()


@contextmanager
def build_lock(build_dir):
    """Hold the OS file lock while any entry uses the shared auxiliary/cache tree."""
    with (build_dir / ".build.lock").open("a+b") as handle:
        handle.seek(0)
        if not handle.read(1):
            handle.write(b"\0")
            handle.flush()
        handle.seek(0)
        if sys.platform == "win32":
            import msvcrt
            import time

            while True:
                try:
                    msvcrt.locking(handle.fileno(), msvcrt.LK_NBLCK, 1)
                    break
                except OSError as error:
                    if error.errno not in {errno.EACCES, errno.EAGAIN, errno.EDEADLK}:
                        raise
                    time.sleep(0.1)
        else:
            import fcntl

            fcntl.flock(handle.fileno(), fcntl.LOCK_EX)
        # Closing the handle releases the OS lock, including after a failed build.
        yield


def build(entry):
    build_dir = ROOT / "build/native"
    build_dir.mkdir(parents=True, exist_ok=True)
    with build_lock(build_dir):
        return _build_locked(entry)


def _build_locked(entry):
    validate(entry)
    fonts = check_fonts()
    build_dir = ROOT / "build/native"
    build_dir.mkdir(parents=True, exist_ok=True)
    for path, _ in sources(entry):
        (build_dir / path.relative_to(ROOT).parent).mkdir(parents=True, exist_ok=True)
    venv_bin = ROOT / "build/latex-venv" / ("Scripts" if os.name == "nt" else "bin")
    if not (venv_bin / "latexminted").exists():
        raise ValueError(
            "Create build/latex-venv and install tools/pygments plus latexminted==0.7.0; see docs/latex-publication.md"
        )
    tex_bin = Path("/Library/TeX/texbin")
    environment = dict(
        os.environ,
        PATH=os.pathsep.join([str(venv_bin), str(tex_bin), os.environ.get("PATH", "")]),
        MARS_HIGHLIGHT_MANIFEST=str(MANIFEST),
        MARS_FONT_DIR=str(fonts) + "/",
    )
    for key in ("TEXMFVAR", "TEXMFCONFIG", "TEXMFHOME", "TEXMFCACHE"):
        environment[key] = str(build_dir / key.lower())
        Path(environment[key]).mkdir(parents=True, exist_ok=True)
    engine = shutil.which("lualatex", path=environment["PATH"])
    if not engine:
        raise ValueError("LuaLaTeX is required")
    fingerprint = hashlib.sha256(
        MANIFEST.read_bytes()
        + b"".join(
            p.read_bytes()
            for p in sorted((ROOT / "tools/pygments").rglob("*.py"))
            if "__pycache__" not in str(p)
        )
    ).hexdigest()
    receipt = build_dir / "highlight-input.sha256"
    if not receipt.exists() or receipt.read_text() != fingerprint:
        shutil.rmtree(build_dir / "_minted", ignore_errors=True)
        receipt.write_text(fingerprint)
    previous = None
    stem = Path(entry).stem
    for iteration in range(1, 6):
        print(f"LuaLaTeX pass {iteration}: {entry}", flush=True)
        with (build_dir / f"{stem}-pass-{iteration}.log").open("w") as log:
            subprocess.run(
                [
                    engine,
                    "-recorder",
                    "-interaction=nonstopmode",
                    "-halt-on-error",
                    "-file-line-error",
                    "-output-directory=" + str(build_dir),
                    entry,
                ],
                cwd=ROOT,
                env=environment,
                stdout=log,
                stderr=subprocess.STDOUT,
                check=True,
            )
        state = reference_state(build_dir / f"{stem}.fls")
        if iteration >= 2 and state == previous:
            break
        previous = state
    else:
        raise ValueError("LaTeX references did not stabilize in five passes")
    log = (build_dir / (stem + ".log")).read_text()
    if any(
        marker in log
        for marker in (
            "Missing character:",
            "Overfull \\hbox",
            "Overfull \\vbox",
            "Float too large for page",
            "LaTeX Font Warning:",
        )
    ):
        raise ValueError(
            "Resolve missing glyphs, font substitutions, or overflowing content before accepting this PDF"
        )
    pdf = build_dir / (stem + ".pdf")
    text_tool = shutil.which("pdftotext")
    if not text_tool:
        raise ValueError("Install Poppler to verify the compiled text")
    text = subprocess.run(
        [text_tool, str(pdf), "-"], text=True, capture_output=True, check=True
    ).stdout
    if "<MINTED>" in text:
        raise ValueError("Minted placeholder remains; the PDF is incomplete")
    print(pdf)
    return pdf


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=["validate", "export-input", "build"])
    parser.add_argument(
        "--entry",
        choices=[
            "revision/advanced-pilots.tex",
            "revision/learning.tex",
            "revision/lab-book.tex",
            "main.tex",
            "specimen.tex",
            "drafts/matrix-transpose.tex",
            "drafts/fixed-point-ball.tex",
            "drafts/binary-packet-parser.tex",
            "drafts/ring-buffer-logger.tex",
            "drafts/utf8-scanner.tex",
            "drafts/clipped-sprite-blitter.tex",
            "drafts/expression-evaluator.tex",
            "drafts/tiny-bytecode-interpreter.tex",
            "drafts/rgb-formats.tex",
            "drafts/pcm-mixing.tex",
            "drafts/fixed-point-fir.tex",
            "drafts/blocked-matmul.tex",
            "drafts/delta-varint.tex",
            "drafts/crc32.tex",
            "drafts/sha256.tex",
            "drafts/chacha20.tex",
            "drafts/rsa-arithmetic.tex",
            "drafts/base64.tex",
            "drafts/binary-ppm.tex",
            "drafts/qoi.tex",
            "drafts/pcm-wav.tex",
            "drafts/pcm-wav-assignment.tex",
            "drafts/cbor-telemetry.tex",
            "drafts/cbor-assignment.tex",
            "drafts/png.tex",
            "drafts/lossless-video.tex",
            "drafts/lossless-video-assignment.tex",
            "drafts/video-vector-figure.tex",
            "drafts/video-index-figure.tex",
            "drafts/video-record-figure.tex",
            "drafts/video-independent-seek-figure.tex",
            "drafts/video-record-selection-figure.tex",
            "drafts/video-playback-progress-figure.tex",
            "drafts/png-file-eof-figure.tex",
            "drafts/png-container-fields-figure.tex",
            "drafts/png-row-reconstruction-figure.tex",
            "drafts/png-vector-packing-figure.tex",
            "drafts/png-final-publication-figure.tex",
            "drafts/h263.tex",
            "drafts/h263-assignment.tex",
            "drafts/h263-sad-result-figure.tex",
            "drafts/h263-motion-result-figure.tex",
            "drafts/h263-prediction-residual-figure.tex",
            "drafts/h263-block-reconstruction-figure.tex",
            "drafts/h263-coefficient-bits-figure.tex",
            "drafts/h263-clip-accepted-figure.tex",
            "drafts/console-calculation.tex",
            "drafts/console-project-figure.tex",
            "drafts/console-console-figure.tex",
            "drafts/console-before-add-figure.tex",
            "drafts/console-after-add-figure.tex",
            "drafts/console-after-one-figure.tex",
            "drafts/single-pixel.tex",
            "drafts/pixel-base-color-figure.tex",
            "drafts/pixel-byte-update-figure.tex",
            "drafts/pixel-memory-state-figure.tex",
            "drafts/eight-byte-sprite.tex",
            "drafts/sprite-source-row-figure.tex",
            "drafts/sprite-reversal-figure.tex",
            "drafts/sprite-first-row-figure.tex",
            "drafts/sprite-complete-sprite-figure.tex",
            "drafts/bit-inspector.tex",
            "drafts/bits-extensions-figure.tex",
            "drafts/bits-result-memory-figure.tex",
            "drafts/bits-word-report-figure.tex",
            "drafts/bits-width-boundary-figure.tex",
            "drafts/seven-segment-counter.tex",
            "drafts/seven-before-write-figure.tex",
            "drafts/seven-zero-glyph-figure.tex",
            "drafts/seven-a-glyph-figure.tex",
            "drafts/seven-final-f-figure.tex",
            "drafts/marsbot-square.tex",
            "drafts/bot-east-figure.tex",
            "drafts/bot-south-figure.tex",
            "drafts/bot-finished-figure.tex",
            "drafts/bot-paused-figure.tex",
            "drafts/bresenham-line.tex",
            "drafts/line-first-figure.tex",
            "drafts/line-update-figure.tex",
            "drafts/line-middle-figure.tex",
            "drafts/line-complete-figure.tex",
            "drafts/life-generations.tex",
            "drafts/life-initial-figure.tex",
            "drafts/life-zero-figure.tex",
            "drafts/life-one-figure.tex",
            "drafts/life-two-figure.tex",
            "drafts/sorting-visualizer.tex",
            "drafts/sorting-initial-figure.tex",
            "drafts/sorting-insertion-figure.tex",
            "drafts/sorting-selection-figure.tex",
            "drafts/sorting-final-figure.tex",
            "drafts/maze-explorer.tex",
            "drafts/maze-initial-figure.tex",
            "drafts/maze-frontier-figure.tex",
            "drafts/maze-path-figure.tex",
            "drafts/maze-full-figure.tex",
            "drafts/transpose-ordinary-figure.tex",
            "drafts/transpose-tile-figure.tex",
            "drafts/transpose-full-figure.tex",
            "drafts/transpose-direct-figure.tex",
            "drafts/ball-floor-figure.tex",
            "drafts/ball-nearest-figure.tex",
            "drafts/ball-reflection-figure.tex",
            "drafts/ball-overflow-figure.tex",
            "drafts/packet-default-figure.tex",
            "drafts/packet-prefix-figure.tex",
            "drafts/packet-checksum-figure.tex",
            "drafts/ring-wrap-figure.tex",
            "drafts/ring-full-figure.tex",
            "drafts/ring-retry-figure.tex",
            "drafts/utf8-pending-figure.tex",
            "drafts/utf8-finished-figure.tex",
            "drafts/utf8-malformed-figure.tex",
            "drafts/utf8-truncated-figure.tex",
            "drafts/blit-left-figure.tex",
            "drafts/blit-full-figure.tex",
            "drafts/blit-top-right-figure.tex",
            "drafts/blit-bottom-left-figure.tex",
            "drafts/expression-success-figure.tex",
            "drafts/expression-missing-close-figure.tex",
            "drafts/expression-literal-overflow-figure.tex",
            "drafts/expression-zero-divisor-figure.tex",
            "drafts/vm-halt-figure.tex",
            "drafts/vm-budget-figure.tex",
            "drafts/vm-capacity-figure.tex",
            "drafts/vm-target-figure.tex",
            "drafts/rgb-packed-figure.tex",
            "drafts/rgb-expanded-figure.tex",
            "drafts/rgb-preview-figure.tex",
            "drafts/rgb-tail-figure.tex",
            "drafts/pcm-saturated-figure.tex",
            "drafts/pcm-wrapping-figure.tex",
            "drafts/pcm-comparison-figure.tex",
            "drafts/pcm-tail-figure.tex",
            "drafts/fir-impulse-figure.tex",
            "drafts/fir-published-figure.tex",
            "drafts/fir-tie-figure.tex",
            "drafts/fir-clipped-figure.tex",
            "drafts/fir-wide-figure.tex",
            "drafts/matmul-scalar-figure.tex",
            "drafts/matmul-zero-figure.tex",
            "drafts/matmul-segment-figure.tex",
            "drafts/matmul-comparison-figure.tex",
            "drafts/delta-atomic-figure.tex",
            "drafts/delta-scan-figure.tex",
            "drafts/delta-failures-figure.tex",
            "drafts/delta-sizes-figure.tex",
            "drafts/crc-bit-change-figure.tex",
            "drafts/crc-table-figure.tex",
            "drafts/crc-streaming-figure.tex",
            "drafts/crc-failure-figure.tex",
            "drafts/sha-schedule-figure.tex",
            "drafts/sha-round-figure.tex",
            "drafts/sha-padding-figure.tex",
            "drafts/sha-file-figure.tex",
            "drafts/chacha-quarter-figure.tex",
            "drafts/chacha-scalar-figure.tex",
            "drafts/chacha-msa-figure.tex",
            "drafts/chacha-stream-figure.tex",
            "drafts/rsa-product-figure.tex",
            "drafts/rsa-sum-figure.tex",
            "drafts/rsa-remainder-figure.tex",
            "drafts/rsa-power-figure.tex",
            "drafts/base64-pair-figure.tex",
            "drafts/base64-padding-figure.tex",
            "drafts/base64-sextets-figure.tex",
            "drafts/base64-compact-figure.tex",
            "drafts/ppm-descriptor-figure.tex",
            "drafts/ppm-pixels-figure.tex",
            "drafts/ppm-header-figure.tex",
            "drafts/ppm-save-figure.tex",
            "drafts/qoi-descriptor-figure.tex",
            "drafts/qoi-alpha-figure.tex",
            "drafts/qoi-encoded-figure.tex",
            "drafts/qoi-save-figure.tex",
            "drafts/wav-chunk-figure.tex",
            "drafts/wav-candidate-figure.tex",
            "drafts/wav-boundary-figure.tex",
            "drafts/wav-pipeline-figure.tex",
            "drafts/cbor-head-figure.tex",
            "drafts/cbor-depth-figure.tex",
            "drafts/cbor-record-figure.tex",
            "drafts/cbor-pipeline-figure.tex",
        ],
        default="main.tex",
    )
    args = parser.parse_args()
    try:
        if args.action == "validate":
            validate(args.entry)
        elif args.action == "export-input":
            export_input()
        else:
            build(args.entry)
    except (ValueError, OSError, subprocess.CalledProcessError) as error:
        parser.exit(1, str(error) + "\n")


if __name__ == "__main__":
    main()
