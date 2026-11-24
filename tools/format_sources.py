"""Audit maintained source formatting while preserving frozen verification evidence."""

import argparse
from concurrent.futures import ThreadPoolExecutor
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile

from kotlin_format import format_kotlin

ROOT = Path(__file__).resolve().parents[1]
EXTENSIONS = {
    "kotlin": {".kt", ".kts"},
    "python": {".py"},
    "latex": {".tex", ".sty", ".cls"},
}
LITERAL = re.compile(
    r"\\begin\{(?P<env>minted|verbatim\*?|Verbatim|BVerbatim|SaveVerbatim|lstlisting|luacode\*?|filecontents\*?)\}[^\n]*\n(?P<body>.*?)^[ \t]*\\end\{(?P=env)\}",
    re.M | re.S,
)


def source_files(root, language):
    result = subprocess.run(
        ["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"],
        cwd=root,
        capture_output=True,
        check=True,
    )
    names = sorted(set(result.stdout.decode().split("\0")) - {""})
    maintained, archived = [], []
    for name in names:
        path = root / name
        if path.suffix not in EXTENSIONS[language] or not path.is_file():
            continue
        if path.is_symlink():
            raise ValueError("Source formatting refuses symlinks: " + name)
        target = (
            archived if Path(name).parts[0].startswith("verification") else maintained
        )
        target.append(path)
    return maintained, archived


def format_python(source):
    import black

    try:
        return black.format_file_contents(
            source,
            fast=False,
            mode=black.Mode(target_versions={black.TargetVersion.PY312}),
        )
    except black.NothingChanged:
        return source


def format_latex(path, executable):
    with tempfile.TemporaryDirectory(prefix="mars-latexindent-") as temporary:
        output = Path(temporary) / "formatted.tex"
        command = [
            executable,
            "-s",
            "-l=" + str(ROOT / ".latexindent.yaml"),
            "-c=" + temporary,
            "-o=" + str(output),
            str(path),
        ]
        result = subprocess.run(command, capture_output=True, text=True)
        if result.returncode:
            raise ValueError("latexindent failed: " + result.stdout + result.stderr)
        formatted = output.read_text()
        original = path.read_text()
        if [m.group("body") for m in LITERAL.finditer(original)] != [
            m.group("body") for m in LITERAL.finditer(formatted)
        ]:
            raise ValueError("Refusing a change to a literal code environment")
        return formatted


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--language", choices=EXTENSIONS, required=True)
    parser.add_argument("--apply", action="store_true")
    parser.add_argument("--report", type=Path)
    parser.add_argument(
        "--latexindent",
        default=shutil.which("latexindent") or "/Library/TeX/texbin/latexindent",
    )
    parser.add_argument("--jobs", type=int, default=1)
    args = parser.parse_args(argv)
    root = args.root.resolve()
    maintained, archived = source_files(root, args.language)

    def check(path):
        original = path.read_text(encoding="utf-8")
        try:
            if args.language == "kotlin":
                formatted = format_kotlin(original, script=path.suffix == ".kts")
            elif args.language == "python":
                formatted = format_python(original)
            else:
                formatted = format_latex(path, args.latexindent)
            changed = formatted != original
            if changed and args.apply:
                path.write_text(formatted, encoding="utf-8")
            return dict(
                path=str(path.relative_to(root)),
                changed=changed,
                beforeSha256=hashlib.sha256(original.encode()).hexdigest(),
                afterSha256=hashlib.sha256(formatted.encode()).hexdigest(),
            )
        except Exception as failure:
            return dict(path=str(path.relative_to(root)), error=str(failure))

    with ThreadPoolExecutor(max_workers=max(1, args.jobs)) as pool:
        rows = list(pool.map(check, maintained))
    errors = [row for row in rows if "error" in row]
    changed = sum(row.get("changed", False) for row in rows)
    report = dict(
        root=str(root),
        language=args.language,
        applied=args.apply,
        maintainedFiles=len(rows),
        changed=changed,
        errors=errors,
        files=rows,
        frozenEvidence=[
            dict(
                path=str(path.relative_to(root)),
                sha256=hashlib.sha256(path.read_bytes()).hexdigest(),
                reason="Historical execution/publication evidence; retain the bytes named by its original receipt",
            )
            for path in archived
        ],
    )
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2) + "\n")
    print(
        f"{root.name}: {args.language}: {len(rows)} maintained files; {changed} require formatting; {len(errors)} errors; {len(archived)} frozen evidence files inventoried",
        flush=True,
    )
    for row in errors:
        print(row["path"] + ": " + row["error"], file=sys.stderr)
    return 1 if errors or (changed and not args.apply) else 0


if __name__ == "__main__":
    raise SystemExit(main())
