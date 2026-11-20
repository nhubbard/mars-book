#!/usr/bin/env python3
"""Validate and export student packages, and run existing MARS checks."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def read_json(path):
    return json.loads(path.read_text())


def within(root, relative):
    if not isinstance(relative, str) or not relative or Path(relative).is_absolute():
        raise ValueError(f'Expected a relative path: {relative!r}')
    path = root / relative
    if '..' in Path(relative).parts or path.is_symlink():
        raise ValueError(f'Unsafe path: {relative}')
    if not path.resolve().is_relative_to(root.resolve()) or not path.exists():
        raise ValueError(f'Missing or escaping path: {relative}')
    return path


def package_path(package_id):
    if not re.fullmatch(r'[a-z0-9]+(?:-[a-z0-9]+)*', package_id):
        raise ValueError('Invalid package ID')
    return within(ROOT / 'packages', package_id)



def validate_capture_mode(figure, raw):
    """Presentation figures attest native editor/Run observations instead of a Debug stop."""
    mode = figure.get('captureMode', 'debug')
    if mode not in ('debug', 'editor', 'run') or raw.get('mode', 'debug') != mode:
        raise ValueError('Capture mode differs')
    if mode == 'editor':
        if raw.get('exitCode') is not None or raw.get('standardInput') is not None or raw.get('stdout') != '' or raw.get('stderr') != '':
            raise ValueError('Editor capture must have no process input or output')
    elif mode == 'run':
        if raw.get('exitCode') != 0 or raw.get('stderr') != '' or raw.get('stdout') != figure.get('expectedStdout') or raw.get('standardInput') != figure.get('standardInput'):
            raise ValueError('Completed Run input, output or exit differs')
        if not figure.get('expectedStdout') or not figure.get('standardInput') or raw.get('inputMethod') != 'Native console typing and Enter handlers':
            raise ValueError('Run capture needs native input and output evidence')


def validate_composite_capture(root, figure, receipt):
    """Validate native multi-view figures without treating a rendered preview as a raw capture."""
    from annotate import validate_capture

    if receipt.get('formatVersion') != 2 or receipt.get('kind') != 'native-composite':
        raise ValueError('Unsupported native composite receipt')
    if receipt.get('figure') != figure['id']:
        raise ValueError('Composite figure identity differs')
    parent = Path(figure['receipt']).parent
    parts = {}
    shared = receipt['sharedState']
    for part in receipt['parts']:
        if not re.fullmatch(r'[a-z][a-z0-9-]*', part['id']) or part['id'] in parts:
            raise ValueError('Invalid or duplicate composite part')
        raw_path = within(root, str(parent / part['receipt']))
        if part.get('receiptSha256') and hashlib.sha256(raw_path.read_bytes()).hexdigest() != part['receiptSha256']:
            raise ValueError('Raw capture receipt changed after review')
        raw = read_json(raw_path)
        validate_capture_mode(figure, raw)
        validate_capture(within(root, str(parent / part['image'])), raw)
        if raw.get('visibleAnalysisIndicators') != [] or raw.get('notificationBalloonsVisible') != 0:
            raise ValueError('Composite capture was not visibly settled')
        if any(raw.get(key) != value for key, value in shared.items()):
            raise ValueError('Composite captures differ from their shared state')
        parts[part['id']] = raw
    if not 1 <= len(parts) <= 8 or len(receipt['panels']) < 2 or shared.get('checkpoint') != figure['checkpoint']:
        raise ValueError('Composite needs at least two panels at the declared checkpoint')
    identities = ('sourceSha256', 'source', 'checkpoint', 'sourceLine', 'publicCommit',
                  'upstreamCommit', 'upstreamDirtyFiles', 'ideBuild', 'captureDriverSha256',
                  'marsPluginArchiveSha256', 'locale', 'scale')
    first = next(iter(parts.values()))
    if any(any(raw.get(key) != first.get(key) for key in identities) for raw in parts.values()):
        raise ValueError('Composite views have different source or runtime provenance')
    panel_ids = set()
    used_parts = set()
    for panel in receipt['panels']:
        if panel['id'] in panel_ids or panel['part'] not in parts:
            raise ValueError('Invalid composite panel identity')
        panel_ids.add(panel['id'])
        used_parts.add(panel['part'])
        raw = parts[panel['part']]
        indices = [panel['callout'], *panel.get('additionalCallouts', [])]
        if any(type(index) is not int or not 0 <= index < len(raw['callouts']) for index in indices):
            raise ValueError('Missing composite callout')
        if len(set(indices)) != len(indices):
            raise ValueError('Duplicate composite callout')
        crop = panel['crop']
        if len(crop) != 4 or any(type(v) is not int for v in crop):
            raise ValueError('Composite crop must use integer pixels')
        x, y, w, h = crop
        if min(x, y) < 0 or min(w, h) <= 0 or x + w > raw['viewport']['width'] or y + h > raw['viewport']['height']:
            raise ValueError('Composite crop leaves its raw image')
        for index in indices:
            box = raw['callouts'][index]['box']
            if box['x'] < x or box['y'] < y or box['x'] + box['width'] > x + w or box['y'] + box['height'] > y + h:
                raise ValueError('Composite crop clips its asserted callout')
    if used_parts != set(parts):
        raise ValueError('Composite contains an unused raw view')
    native = within(root, figure['nativeAnnotations'])
    preview = within(root, figure['artifact'])
    if hashlib.sha256(native.read_bytes()).hexdigest() != receipt['nativeExportSha256']:
        raise ValueError('Native annotations changed after review')
    if hashlib.sha256(preview.read_bytes()).hexdigest() != receipt['previewSha256']:
        raise ValueError('Rendered preview changed after review')


def validate(package_id):
    root = package_path(package_id)
    data = read_json(root / 'package.json')
    if data['formatVersion'] != 1 or data['id'] != package_id:
        raise ValueError('Unsupported format or mismatched ID')
    if not re.fullmatch(r'\d+\.\d+\.\d+', data['version']):
        raise ValueError('Package version must have three numeric components')
    if not re.fullmatch(r'[0-9a-f]{40}', data['environment']['upstreamCommit']):
        raise ValueError('Pin a full upstream commit')
    for name in [data['entry'], data['assignment'], data['figures'], *data['publicChecks']]:
        within(root, name)
    if data.get('gradingHarness'):
        within(root, data['gradingHarness'])
    harnesses = data.get('checkHarnesses', {})
    if not isinstance(harnesses, dict) or not set(harnesses).issubset(data['publicChecks']):
        raise ValueError('Per-check harnesses must name declared public checks')
    for harness in harnesses.values():
        within(root, harness)
    seen = set()
    quiz_ids = set()
    if not data['steps']:
        raise ValueError('Package must contain steps')
    for step in data['steps']:
        if step['id'] in seen or not set(step['prerequisites']).issubset(seen):
            raise ValueError('Duplicate step or unresolved prerequisite')
        seen.add(step['id'])
        within(root, step['content'])
        quiz = step['quiz']
        ids = [choice['id'] for choice in quiz['choices']]
        if quiz['id'] in quiz_ids or len(ids) < 2 or len(set(ids)) != len(ids):
            raise ValueError('Duplicate quiz or invalid choices')
        quiz_ids.add(quiz['id'])
        if 'correctChoice' in quiz or 'answer' in quiz:
            raise ValueError('Quiz keys belong in the instructor repository')
    plan = read_json(root / data['figures'])
    figure_ids = set()
    for figure in plan['figures']:
        if figure['id'] in figure_ids or figure['step'] not in seen:
            raise ValueError('Duplicate figure or unknown figure step')
        figure_ids.add(figure['id'])
        if figure['theme'] != 'IntelliJ Light' or not figure['alt'] or not figure['caption']:
            raise ValueError('Figures require light theme, alt text, and captions')
        if figure['captureStatus'] not in ('planned', 'verified'):
            raise ValueError('Unknown capture status')
        if figure['captureStatus'] == 'verified':
            within(root, figure['artifact'])
            receipt = read_json(within(root, figure['receipt']))
            if receipt['theme'] != figure['theme'] or receipt['assertionsPassed'] is not True:
                raise ValueError('Capture receipt does not attest to required state')
            if figure.get('captureFormat') == 'native-composite' or receipt.get('formatVersion') == 2 or receipt.get('kind') == 'native-composite':
                validate_composite_capture(root, figure, receipt)
    for name in data['exports']:
        path = within(root, name)
        candidates = [path, *path.rglob('*')] if path.is_dir() else [path]
        for candidate in candidates:
            rel = candidate.relative_to(root)
            if candidate.is_symlink() or any(part.startswith('.') for part in rel.parts):
                raise ValueError(f'Hidden or symlink export: {rel}')
            if any(part.lower() in {'instructor', 'reference', 'solutions', 'private'} for part in rel.parts):
                raise ValueError(f'Instructor path in student export: {rel}')
    reading_segments(root, data, figure_ids)
    return root, data


def student_files(root, data):
    files = set()
    for name in data['exports']:
        path = within(root, name)
        candidates = [path] if path.is_file() else [p for p in path.rglob('*') if p.is_file()]
        files.update(p for p in candidates if '__pycache__' not in p.parts and p.suffix not in {'.pyc', '.pyo'})
    return sorted(files)


def bundle(package_id, output=None):
    root, data = validate(package_id)
    output = output or ROOT / 'build' / f"{package_id}-{data['version']}-student.zip"
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, 'w', compression=zipfile.ZIP_DEFLATED) as archive:
        for path in student_files(root, data):
            item = zipfile.ZipInfo(str(Path(package_id) / path.relative_to(root)), date_time=(2026, 1, 1, 0, 0, 0))
            item.compress_type = zipfile.ZIP_DEFLATED
            item.external_attr = 0o100644 << 16
            archive.writestr(item, path.read_bytes())
    digest = hashlib.sha256(output.read_bytes()).hexdigest()
    output.with_suffix(output.suffix + '.sha256').write_text(f'{digest}  {output.name}\n')
    return output


def reading_segments(root, data, figure_ids=None):
    reading = read_json(within(root, data['reading']))
    if reading['formatVersion'] != 1:
        raise ValueError('Unsupported reading sequence format')
    figure_ids = figure_ids if figure_ids is not None else {
        figure['id'] for figure in read_json(root / data['figures'])['figures']}
    ids = set()
    steps = []
    figures = []
    for segment in reading['segments']:
        if not segment['id'] or segment['id'] in ids:
            raise ValueError('Duplicate or empty reading segment ID')
        ids.add(segment['id'])
        kind = segment['kind']
        if kind in ('prose', 'fixture'):
            within(root, segment['content'])
            if kind == 'fixture' and not segment.get('role'):
                raise ValueError('Teaching fixture needs a semantic role')
            native = segment.get('typesetting', {}).get('nativeLatex')
            if native:
                within(root, native)
        elif kind == 'step':
            steps.append(segment['step'])
        elif kind == 'figure':
            figures.append(segment['figure'])
        else:
            raise ValueError('Unknown reading segment kind')
    if steps != [step['id'] for step in data['steps']]:
        raise ValueError('Reading sequence must cover steps once in prerequisite order')
    if len(set(figures)) != len(figures) or set(figures) != figure_ids:
        raise ValueError('Reading sequence must cover each figure once')
    return reading['segments']


def grade(package_id, cli, source, java='java'):
    root, data = validate(package_id)
    for check in data['publicChecks']:
        command = [java, '-jar', str(cli.resolve()), 'grade', str(root / check), str(source.resolve())]
        harness = data.get('checkHarnesses', {}).get(check, data.get('gradingHarness'))
        if harness:
            command.append(str(root / harness))
        subprocess.run(command, check=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=['validate', 'build', 'check'])
    parser.add_argument('--package', default='001-console-calculation')
    parser.add_argument('--cli', type=Path)
    parser.add_argument('--source', type=Path)
    parser.add_argument('--java', default='java')
    args = parser.parse_args()
    if args.action == 'validate':
        _, data = validate(args.package)
        print(f"Valid {data['id']} {data['version']}: {len(data['steps'])} steps")
    elif args.action == 'build':
        print(bundle(args.package))
    else:
        if not args.cli or not args.source:
            parser.error('check requires --cli and --source')
        grade(args.package, args.cli, args.source, args.java)


if __name__ == '__main__':
    main()
