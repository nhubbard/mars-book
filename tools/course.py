#!/usr/bin/env python3
"""Validate, assemble Markdown, export student packages, and run existing MARS checks."""
import argparse
import hashlib
import json
import os
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
        files.update([path] if path.is_file() else [p for p in path.rglob('*') if p.is_file()])
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


def render_markdown(path, output, heading_prefix='##'):
    text = path.read_text()
    text = re.sub(r'^(#{1,4}) ', lambda match: heading_prefix + match[1] + ' ', text, flags=re.M)
    def link(match):
        target = match[1]
        if target.startswith('#') or re.match(r'[a-zA-Z]+:', target):
            return match[0]
        location, separator, anchor = target.partition('#')
        resolved = (path.parent / location).resolve()
        if not resolved.is_relative_to(ROOT.resolve()) or not resolved.exists():
            raise ValueError(f'Missing or escaping Markdown link: {path}: {target}')
        relative = Path(os.path.relpath(resolved, output.parent)).as_posix()
        return '](' + relative + (separator + anchor if separator else '') + ')'
    return re.sub(r'\]\(([^)]+)\)', link, text)


def book(output=None):
    config = read_json(ROOT / 'book/book.json')
    output = output or ROOT / 'build/book.md'
    sections = [f"# {config['title']}\n"]
    for chapter in config['chapters']:
        root, data = validate(chapter['package'])
        if chapter['reading'] != data['reading']:
            raise ValueError('Chapter and package reading sequence must agree')
        segments = reading_segments(root, data)
        anchors = {segment['id'] for segment in segments}
        insertions = chapter.get('insertions', [])
        for insertion in insertions:
            if insertion['anchor'] not in anchors or insertion['position'] not in ('before', 'after'):
                raise ValueError('Unknown human insertion anchor or position')
            within(ROOT, insertion['content'])
        sections.append(f"## {data['title']}\n")
        assignment = render_markdown(root / data['assignment'], output, heading_prefix='#')
        sections.append(assignment.split('\n', 1)[1])
        steps = {step['id']: step for step in data['steps']}
        figures = {figure['id']: figure for figure in read_json(root / data['figures'])['figures']}
        for segment in segments:
            for insertion in insertions:
                if insertion['position'] == 'before' and insertion['anchor'] == segment['id']:
                    sections.append(render_markdown(within(ROOT, insertion['content']), output))
            if segment['kind'] in ('prose', 'fixture'):
                if segment['kind'] == 'fixture':
                    sections.append(f"<!-- fixture {segment['id']}; role {segment['role']} -->")
                sections.append(render_markdown(within(root, segment['content']), output))
            elif segment['kind'] == 'step':
                sections.append(render_markdown(root / steps[segment['step']]['content'], output))
            else:
                figure = figures[segment['figure']]
                if figure['captureStatus'] == 'verified':
                    artifact = Path(os.path.relpath(root / figure['artifact'], output.parent)).as_posix()
                    sections.append(f"![{figure['alt']}]({artifact})\n\n{figure['caption']}\n")
                else:
                    sections.append(f"> Figure {figure['id']} planned: {figure['caption']}\n")
            for insertion in insertions:
                if insertion['position'] == 'after' and insertion['anchor'] == segment['id']:
                    sections.append(render_markdown(within(ROOT, insertion['content']), output))
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text('\n\n'.join(sections))
    return output


def grade(package_id, cli, source, java='java'):
    root, data = validate(package_id)
    for check in data['publicChecks']:
        subprocess.run([java, '-jar', str(cli.resolve()), 'grade', str(root / check), str(source.resolve())], check=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=['validate', 'build', 'book', 'check'])
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
    elif args.action == 'book':
        print(book())
    else:
        if not args.cli or not args.source:
            parser.error('check requires --cli and --source')
        grade(args.package, args.cli, args.source, args.java)


if __name__ == '__main__':
    main()
