#!/usr/bin/env python3
"""Select encoded records and known previous frames for scalar record observations."""

import argparse, importlib.util
from pathlib import Path

ROOT = Path(__file__).resolve().parent
s = importlib.util.spec_from_file_location(
    "video_record_inputs", ROOT.parent / "video-fixtures/fixture.py"
)
video = importlib.util.module_from_spec(s)
s.loader.exec_module(video)


def material(fixture="moving", index=1, case="valid"):
    m = video.material(fixture)
    if type(index) is not int or not 0 <= index < len(m["frames"]):
        raise ValueError("Choose an existing frame index")
    info = m["directory"][index]
    raw = m["file"][info["offset"] : info["offset"] + info["record_bytes"]]
    before = m["frames"][index - 1] if index else bytes([255, 0, 255]) * 4096
    after = m["frames"][index]
    if case == "bad-crc":
        raw = raw[:12] + bytes([raw[12] ^ 1]) + raw[13:]
        after = before
    elif case == "late-skip":
        if fixture != "moving" or index != 1:
            raise ValueError("Late skip observation uses moving record one")
        raw = video.frame_record(1, 1, video.commands([(0, [(1, 0, 0)]), (4096, [])]))
        after = before
    elif case != "valid":
        raise ValueError("Unknown record defect")
    return dict(
        record=raw,
        before=before,
        after=after,
        index=index,
        period=m["period"],
        kind=info["kind"],
        case=case,
    )


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--fixture", choices=video.NAMES, default="moving")
    p.add_argument("--index", type=int, default=1)
    p.add_argument("--case", choices=("valid", "bad-crc", "late-skip"), default="valid")
    p.add_argument("--output", type=Path, required=True)
    a = p.parse_args()
    with a.output.open("xb") as out:
        out.write(material(a.fixture, a.index, a.case)["record"])


if __name__ == "__main__":
    main()
