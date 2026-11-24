#!/usr/bin/env python3
"""Generate complete encoded video inputs, including isolated file defects."""

import argparse, importlib.util
from pathlib import Path

ROOT = Path(__file__).resolve().parent
s = importlib.util.spec_from_file_location(
    "video_index_inputs", ROOT.parent / "video-fixtures/fixture.py"
)
video = importlib.util.module_from_spec(s)
s.loader.exec_module(video)
CASES = ("valid", "bad-magic", "file-suffix", "late-crc", "late-command")


def material(fixture="moving", case="valid"):
    m = video.material(fixture)
    raw = m["file"]
    if case == "bad-magic":
        raw = b"BAD!" + raw[4:]
    elif case == "file-suffix":
        raw += b"X"
    elif case == "late-crc":
        at = m["directory"][-1]["offset"] + 12
        raw = raw[:at] + bytes([raw[at] ^ 1]) + raw[at + 1 :]
    elif case == "late-command":
        if fixture != "moving":
            raise ValueError("The late-command observation uses moving")
        at = m["directory"][-1]["offset"]
        raw = raw[:at] + video.frame_record(
            1, len(m["frames"]) - 1, video.commands([(0, [(1, 0, 0)]), (4096, [])])
        )
    elif case != "valid":
        raise ValueError("Unknown index fixture defect")
    return dict(
        file=raw,
        directory=m["directory"],
        frames=m["frames"],
        period=m["period"],
        duration_ms=m["duration_ms"],
        valid=case == "valid",
    )


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("fixture", choices=video.NAMES)
    p.add_argument("--case", choices=CASES, default="valid")
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    with a.output.open("xb") as out:
        out.write(material(a.fixture, a.case)["file"])


if __name__ == "__main__":
    main()
