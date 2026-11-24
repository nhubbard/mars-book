"""Stage the original SHA assessment grid and add update-only prefix checks.

Original completed cases are unchanged. Prefix checks end before finalization;
they do not install an implementation's intermediate workspace state.
"""

import argparse
from copy import deepcopy
import json
from pathlib import Path
from sha_lesson_cases import groups


def stages():
    original = groups()
    kernel = original["kernel"]
    update = []
    for case in kernel:
        if case["name"].startswith(("message ", "synthetic update ")):
            prefix = deepcopy(case)
            prefix["name"] = "update prefix: " + case["name"]
            if case["name"].startswith("message "):
                assert prefix["calls"][-1]["entry"] == "sha256_final"
                prefix["calls"].pop()
            update.append(prefix)
    return dict(
        schedule=kernel[:16],
        compression=kernel[16:28],
        initialization=kernel[28:31],
        update=update,
        streaming=kernel[31:],
        file=original["file"],
        caller=original["program"],
        reuse=original["reuse"],
    )


def profiles():
    result = {}
    for stage, cases in stages().items():
        batch, part = [], 0
        for case in cases:
            candidate = dict(formatVersion=16, cases=batch + [case])
            if batch and (
                sum(c["instructionLimit"] for c in candidate["cases"]) > 10000000
                or len(json.dumps(candidate, separators=(",", ":")).encode()) + 1
                > 1048576
            ):
                result[f"sha-{stage}-{part:03d}.cases.json"] = dict(
                    formatVersion=16, cases=batch
                )
                batch, part = [], part + 1
            batch.append(case)
        if batch:
            result[f"sha-{stage}-{part:03d}.cases.json"] = dict(
                formatVersion=16, cases=batch
            )
    return result


def build(directory, check=False):
    directory = Path(directory)
    if not check:
        directory.mkdir(parents=True, exist_ok=True)
    files = {
        name: json.dumps(value, separators=(",", ":")) + "\n"
        for name, value in profiles().items()
    }
    for name, text in files.items():
        path = directory / name
        if check:
            if not path.is_file() or path.read_text() != text:
                raise ValueError(f"Stale staged SHA profile: {path}")
        else:
            path.write_text(text)
    return files


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    build(args.directory, args.check)
