#!/usr/bin/env python3
"""Audit the revised native publications; never compose or rewrite their sources.

This checks literal includes and references used by this manuscript, not arbitrary
TeX expansion. It verifies structural coverage, not instructional adequacy.
"""

import argparse
from collections import Counter
import json
from pathlib import Path
import re

import book

ROOT = Path(__file__).resolve().parents[1]
ENTRIES = ("revision/learning.tex", "revision/lab-book.tex")
LABEL = re.compile(r"\\label\{([^}]+)\}")
REFERENCE = re.compile(r"\\(?:ref|pageref|autoref|eqref)\*?\{([^}]+)\}")
LITERAL = re.compile(r"\\begin\{(minted|verbatim|lstlisting)\}.*?\\end\{\1\}", re.S)


def prose(text):
    # Strip literal environments before comments: assembly may contain percent signs.
    return re.sub(r"(?<!\\)%[^\n]*", "", LITERAL.sub("", text))


def publication(entry, root):
    documents = book.sources(entry, root)
    labels = Counter()
    references = set()
    chapters = []
    owners = {}
    current = None
    for path, text in documents:
        text = prose(text)
        if re.search(r"\\chapter(?:\[[^\]]*\])?\{", text):
            current = path.relative_to(root).as_posix()
            chapters.append(current)
        owners[path.relative_to(root).as_posix()] = current
        labels.update(LABEL.findall(text))
        references.update(REFERENCE.findall(text))
    duplicates = sorted(key for key, count in labels.items() if count > 1)
    if duplicates:
        raise ValueError(f"{entry}: duplicate labels: {duplicates}")
    missing = sorted(references - labels.keys())
    if missing:
        raise ValueError(f"{entry}: unresolved references: {missing}")
    return chapters, owners, len(labels), len(references)


def local_file(root, name):
    if (
        not isinstance(name, str)
        or Path(name).is_absolute()
        or ".." in Path(name).parts
    ):
        raise ValueError(f"Invalid repository path: {name!r}")
    path = (root / name).resolve()
    if not path.is_relative_to(root.resolve()) or not path.is_file():
        raise ValueError(f"Missing or escaping repository file: {name}")
    return path


def audit(root=ROOT, index=None):
    root = root.resolve()
    if index is None:
        index = json.loads((root / "revision/project-locations.json").read_text())
    if index.get("formatVersion") != 1:
        raise ValueError("Unsupported project-location index")
    main, main_owners, main_labels, main_refs = publication(ENTRIES[0], root)
    labs, lab_owners, lab_labels, lab_refs = publication(ENTRIES[1], root)
    expected_labs = [
        path.replace("revision/main/", "revision/labs/", 1) for path in main
    ]
    if not main or labs != expected_labs:
        raise ValueError("Main and lab chapter sequence differs")
    projects = {}
    for manifest in sorted((root / "packages").glob("*/package.json")):
        package = json.loads(manifest.read_text())
        if package["id"] != manifest.parent.name or package["id"] in projects:
            raise ValueError(f"Invalid original package identity: {manifest}")
        projects[package["id"]] = package
    if not projects:
        raise ValueError("No original project packages found")
    covered = set()
    locations = set()
    companions = set()
    for placement in index["placements"]:
        project = placement["project"]
        if project not in projects:
            raise ValueError(f"Unknown original project: {project}")
        chapter = placement["chapter"]
        if not isinstance(chapter, str) or not re.fullmatch(
            r"[a-z]+(?:-[a-z]+)*", chapter
        ):
            raise ValueError(f"Invalid chapter ID: {chapter!r}")
        main_path = f"revision/main/{chapter}.tex"
        lab_path = f"revision/labs/{chapter}.tex"
        if main_path not in main or lab_path not in labs:
            raise ValueError(
                f"Project {project}: chapter is absent from native composition"
            )
        location = placement["lab"]
        local_file(root, location)
        if lab_owners.get(location) != lab_path:
            raise ValueError(
                f"Project {project}: lab is absent or included under another chapter"
            )
        key = (project, chapter, location)
        if key in locations:
            raise ValueError(f"Duplicate project placement: {key}")
        locations.add(key)
        local_file(root, placement["verificationNote"])
        covered.add(project)
        if location != lab_path:
            companions.add(location)
    if covered != projects.keys():
        raise ValueError(
            f"Original projects without revised locations: {sorted(projects.keys() - covered)}"
        )
    included_companions = {
        path for path in lab_owners if path.startswith("revision/labs/companions/")
    }
    if companions != included_companions:
        raise ValueError(
            "Native companion includes and indexed project locations differ"
        )
    return dict(
        formatVersion=1,
        chapters=len(main),
        originalProjects=len(projects),
        placements=len(locations),
        companions=len(companions),
        main=dict(labels=main_labels, referencedLabels=main_refs),
        lab=dict(labels=lab_labels, referencedLabels=lab_refs),
        scope="Structural coverage only; prerequisite, learner and accessibility review remain separate.",
    )


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.parse_args()
    try:
        print(json.dumps(audit(), indent=2))
    except (ValueError, KeyError, OSError) as error:
        parser.exit(1, f"Curriculum audit failed: {error}\n")


if __name__ == "__main__":
    main()
