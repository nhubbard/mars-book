#!/usr/bin/env python3
"""Export reviewed figure assets; native TeX stays the editable authority."""

import hashlib
import json
import re
from pathlib import Path
import shutil

ROOT = Path(__file__).resolve().parents[1]
ID = "001-console-calculation"


def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()


def export(root=ROOT, package_id=ID):
    if not isinstance(package_id, str) or not re.fullmatch(
        r"\d{3}-[a-z0-9]+(?:-[a-z0-9]+)*", package_id
    ):
        raise ValueError("Unsafe package identity")
    package = root / "packages" / package_id
    approved = root / "book/captures" / package_id
    registry = json.loads((approved / "figures.json").read_text())
    plan_path = package / "captures/plan.json"
    plan = json.loads(plan_path.read_text())
    seen = set()
    for figure in registry["figures"]:
        ident = figure["id"]
        if ident in seen:
            raise ValueError("Duplicate reviewed figure")
        seen.add(ident)
        canonical = next(f for f in plan["figures"] if f["id"] == ident)
        paths = [Path(figure[k]) for k in ["artifact", "receipt", "nativeAnnotations"]]
        if (
            any(p.is_absolute() or ".." in p.parts for p in paths)
            or paths[0].parent != paths[1].parent
        ):
            raise ValueError("Unsafe reviewed asset path")
        folder = approved / paths[1].parent
        if (
            folder.is_symlink()
            or not folder.resolve().is_relative_to(approved.resolve())
            or any(p.is_symlink() for p in folder.rglob("*"))
        ):
            raise ValueError("Symlink reviewed asset")
        if paths[2].parent != Path("native") or paths[2].suffix != ".tex":
            raise ValueError("Native annotations need their own export folder")
        source = root / "includes/chapters" / package_id / paths[2].name
        if source.is_symlink():
            raise ValueError("Symlink native composition")
        receipt = json.loads((approved / paths[1]).read_text())
        if sha(source) != receipt["nativeSourceSha256"]:
            raise ValueError("Native composition changed after review")
        target = package / paths[2]
        old_receipt = package / "captures" / paths[1]
        if (
            target.is_symlink()
            or old_receipt.is_symlink()
            or not target.resolve().is_relative_to(package.resolve())
        ):
            raise ValueError("Unsafe native export destination")
        if target.exists() and not old_receipt.exists():
            raise ValueError("Untracked native export retained")
        target.parent.mkdir(parents=True, exist_ok=True)
        if (
            target.exists()
            and old_receipt.exists()
            and sha(target) != json.loads(old_receipt.read_text())["nativeExportSha256"]
        ):
            raise ValueError("Edited native export retained")
        target.write_text(
            source.read_text().replace(
                "book/captures/" + package_id + "/",
                "packages/" + package_id + "/captures/",
            )
        )
        shutil.copytree(
            folder, package / "captures" / paths[1].parent, dirs_exist_ok=True
        )
        receipt["nativeExportSha256"] = sha(target)
        old_receipt.write_text(json.dumps(receipt, indent=2) + "\n")
        canonical.update(
            figure,
            artifact="captures/" + figure["artifact"],
            receipt="captures/" + figure["receipt"],
        )
    plan_path.write_text(json.dumps(plan, indent=2) + "\n")
    manifest_path = package / "package.json"
    manifest = json.loads(manifest_path.read_text())
    if "native" not in manifest["exports"]:
        manifest["exports"].append("native")
    manifest_path.write_text(json.dumps(manifest, indent=2) + "\n")
    print("Exported", len(seen), "reviewed native figures for", package_id)


if __name__ == "__main__":
    export()
