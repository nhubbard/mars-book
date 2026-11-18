#!/usr/bin/env python3
"""Validate and compile directly edited LaTeX. Never regenerate prose from task metadata."""
import argparse
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
MANIFEST = ROOT / 'book/highlighting/intellij-light.json'
CODE = re.compile(r'\\begin\{minted\}(?:\[[^\]]*\])?\{mars-idea\}\n(.*?)\\end\{minted\}', re.S)
INCLUDE = re.compile(r'\\(?:input|include)\{([^}]+)\}')


def sources(entry='main.tex', root=ROOT):
    """Walk literal native includes in publication order; a path cannot leave this repository."""
    def visit(path, stack):
        path = path.resolve()
        if not path.is_relative_to(root.resolve()) or path in stack:
            raise ValueError(f'Escaping or cyclic LaTeX include: {path}')
        text = path.read_text(encoding='utf-8')
        yield path, text
        # This manuscript uses root-relative includes. Comments do not activate includes.
        uncommented = re.sub(r'(?<!\\)%[^\n]*', '', text)
        for name in INCLUDE.findall(uncommented):
            child = root / name
            if child.suffix != '.tex':
                child = child.with_suffix('.tex')
            yield from visit(child, stack + [path])
    return list(visit(root / entry, []))


def snippets(entry='main.tex', root=ROOT):
    return [match[1] for _, text in sources(entry, root) for match in CODE.finditer(text)]


def validate(entry='main.tex'):
    document = json.loads(MANIFEST.read_text())
    if document['formatVersion'] != 1:
        raise ValueError('Unsupported IntelliJ export')
    records = {}
    for export in document['exports']:
        source = export['source']
        digest = hashlib.sha256(source.encode()).hexdigest()
        if digest != export['sha256'] or digest in records:
            raise ValueError('Corrupt or duplicate IntelliJ export')
        offset = 0
        for run in export['runs']:
            if run['start'] != offset or not offset < run['end'] <= len(source):
                raise ValueError('Invalid Unicode highlight span coverage')
            offset = run['end']
        if offset != len(source):
            raise ValueError('Incomplete highlight span coverage')
        records[digest] = source
    code = snippets(entry)
    for source in code:
        if records.get(hashlib.sha256(source.encode()).hexdigest()) != source:
            raise ValueError('Source changed: export fresh IntelliJ highlighting with the authoring plugin')
    print(f'Valid native composition: {len(sources(entry))} included files, {len(code)} highlighted code blocks')
    return code


def export_input():
    folder = ROOT / 'build/highlight-input'
    folder.mkdir(parents=True, exist_ok=True)
    for path in folder.glob('*.asm'):
        path.unlink()
    for source in snippets() + snippets('specimen.tex'):
        digest = hashlib.sha256(source.encode()).hexdigest()
        (folder / (digest + '.asm')).write_text(source, encoding='utf-8')
    print(folder)


def check_fonts():
    folder = Path(os.environ.get('MARS_FONT_DIR', Path.home() / 'Library/Fonts')).resolve()
    names = ['MinionPro-Regular.otf', 'MinionPro-It.otf', 'MinionPro-Bold.otf', 'MinionPro-BoldIt.otf', 'NotoSansMath-Regular.otf']
    for name in names:
        if not (folder / name).is_file():
            raise ValueError(f'Missing installed font {name}; set MARS_FONT_DIR to its directory')
    data = (folder / names[-1]).read_bytes()
    count = struct.unpack_from('>H', data, 4)[0]
    tags = {data[12 + i * 16:16 + i * 16] for i in range(count)}
    if b'MATH' not in tags:
        raise ValueError('Noto Sans Math needs an OpenType MATH table')
    return folder


def build(entry):
    validate(entry)
    fonts = check_fonts()
    build_dir = ROOT / 'build/native'
    build_dir.mkdir(parents=True, exist_ok=True)
    for path, _ in sources(entry):
        (build_dir / path.relative_to(ROOT).parent).mkdir(parents=True, exist_ok=True)
    venv_bin = ROOT / 'build/latex-venv' / ('Scripts' if os.name == 'nt' else 'bin')
    if not (venv_bin / 'latexminted').exists():
        raise ValueError('Create build/latex-venv and install tools/pygments plus latexminted==0.7.0; see docs/latex-publication.md')
    tex_bin = Path('/Library/TeX/texbin')
    environment = dict(os.environ, PATH=os.pathsep.join([str(venv_bin), str(tex_bin), os.environ.get('PATH', '')]),
                       MARS_HIGHLIGHT_MANIFEST=str(MANIFEST), MARS_FONT_DIR=str(fonts) + '/')
    for key in ('TEXMFVAR', 'TEXMFCONFIG', 'TEXMFHOME', 'TEXMFCACHE'):
        environment[key] = str(build_dir / key.lower())
        Path(environment[key]).mkdir(parents=True, exist_ok=True)
    engine = shutil.which('lualatex', path=environment['PATH'])
    if not engine:
        raise ValueError('LuaLaTeX is required')
    fingerprint = hashlib.sha256(MANIFEST.read_bytes() + b''.join(p.read_bytes() for p in sorted((ROOT / 'tools/pygments').rglob('*.py')) if '__pycache__' not in str(p))).hexdigest()
    receipt = build_dir / 'highlight-input.sha256'
    if not receipt.exists() or receipt.read_text() != fingerprint:
        shutil.rmtree(build_dir / '_minted', ignore_errors=True)
        receipt.write_text(fingerprint)
    previous = None
    stem = Path(entry).stem
    for iteration in range(1, 6):
        print(f'LuaLaTeX pass {iteration}: {entry}', flush=True)
        with (build_dir / f'{stem}-pass-{iteration}.log').open('w') as log:
            subprocess.run([engine, '-interaction=nonstopmode', '-halt-on-error', '-file-line-error', '-output-directory=' + str(build_dir), entry],
                           cwd=ROOT, env=environment, stdout=log, stderr=subprocess.STDOUT, check=True)
        state = b''.join(p.read_bytes() for p in sorted(build_dir.rglob('*')) if p.suffix in ('.aux', '.toc', '.out'))
        if iteration >= 2 and state == previous:
            break
        previous = state
    else:
        raise ValueError('LaTeX references did not stabilize in five passes')
    log = (build_dir / (stem + '.log')).read_text()
    if any(marker in log for marker in ('Missing character:', 'Overfull \\hbox', 'Overfull \\vbox', 'LaTeX Font Warning:')):
        raise ValueError('Resolve missing glyphs, font substitutions, or overflowing content before accepting this PDF')
    pdf = build_dir / (stem + '.pdf')
    text_tool = shutil.which('pdftotext')
    if not text_tool:
        raise ValueError('Install Poppler to verify the compiled text')
    text = subprocess.run([text_tool, str(pdf), '-'], text=True, capture_output=True, check=True).stdout
    if '<MINTED>' in text:
        raise ValueError('Minted placeholder remains; the PDF is incomplete')
    print(pdf)
    return pdf


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=['validate', 'export-input', 'build'])
    parser.add_argument('--entry', choices=['main.tex', 'specimen.tex', 'drafts/matrix-transpose.tex', 'drafts/fixed-point-ball.tex', 'drafts/binary-packet-parser.tex', 'drafts/ring-buffer-logger.tex', 'drafts/utf8-scanner.tex', 'drafts/clipped-sprite-blitter.tex', 'drafts/expression-evaluator.tex', 'drafts/tiny-bytecode-interpreter.tex', 'drafts/rgb-formats.tex', 'drafts/pcm-mixing.tex'], default='main.tex')
    args = parser.parse_args()
    try:
        if args.action == 'validate':
            validate(args.entry)
        elif args.action == 'export-input':
            export_input()
        else:
            build(args.entry)
    except (ValueError, OSError, subprocess.CalledProcessError) as error:
        parser.exit(1, str(error) + '\n')


if __name__ == '__main__':
    main()
