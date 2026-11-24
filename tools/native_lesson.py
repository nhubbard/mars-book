"""Package explicitly exported native lesson files; MARS owns lesson-format validation."""

import argparse
import hashlib
import json
from pathlib import Path, PurePosixPath
import re
import tempfile
import zipfile


def exported_file(root, relative):
    if not isinstance(relative, str) or not relative or "\\" in relative:
        raise ValueError("Expected a portable relative export path")
    parts = PurePosixPath(relative)
    if (
        parts.is_absolute()
        or parts.as_posix() != relative
        or any(p in {".", ".."} for p in parts.parts)
    ):
        raise ValueError("Unsafe export path")
    path = root
    for part in parts.parts:
        path = path / part
        if path.is_symlink():
            raise ValueError("Lesson exports must not follow symlinks")
    if not path.is_file():
        raise ValueError(f"Missing exported file: {relative}")
    return path


def bundle(root, output):
    root = Path(root).resolve(strict=True)
    output = Path(output).resolve()
    if output.is_relative_to(root):
        raise ValueError("Build the archive outside the lesson source folder")
    manifest = exported_file(root, "package.json")
    data = json.loads(manifest.read_text(encoding="utf-8"))
    package_id = data["id"]
    if not isinstance(package_id, str) or not re.fullmatch(
        r"[0-9]{3}-[a-z][a-z0-9-]{0,90}", package_id
    ):
        raise ValueError("Invalid package ID")
    supporting = data.get("supportingFiles", [])
    if not isinstance(supporting, list):
        raise ValueError("supportingFiles must be a list")
    names = [
        "package.json",
        data["entry"],
        data["assignment"],
        data["nativeLesson"],
        *supporting,
    ]
    files = [(name, exported_file(root, name)) for name in names]
    if len(set(names)) != len(names):
        raise ValueError("Duplicate exported path")
    output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(
        dir=output.parent, prefix=".native-lesson-", suffix=".zip", delete=False
    ) as temporary:
        temporary_path = Path(temporary.name)
    try:
        with zipfile.ZipFile(temporary_path, "w") as archive:
            for name, path in sorted(files):
                item = zipfile.ZipInfo(
                    f"{package_id}/{name}", date_time=(2026, 1, 1, 0, 0, 0)
                )
                item.compress_type = zipfile.ZIP_DEFLATED
                item.external_attr = 0o100644 << 16
                archive.writestr(item, path.read_bytes())
        temporary_path.replace(output)
    finally:
        temporary_path.unlink(missing_ok=True)
    digest = hashlib.sha256(output.read_bytes()).hexdigest()
    output.with_suffix(output.suffix + ".sha256").write_text(
        f"{digest}  {output.name}\n", encoding="ascii"
    )
    return output


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("lesson", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    print(bundle(args.lesson, args.output))
