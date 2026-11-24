"""Public MarsBot profiles using actual device state, ordered trails and execution-origin stores."""

import argparse
import json
from pathlib import Path


def profile(square=True):
    pairs = (
        [(16, 64), (1, 64), (63, 17)]
        if square
        else [(side, rate) for side in [1, 2, 16, 63] for rate in [8, 17, 64, 128]]
    )
    rows = []
    for side, rate in pairs:
        guard = 0x13579BDF ^ (side * 97 + rate) if square else 0x13579BDF
        initial = (side.to_bytes(4, "little") + guard.to_bytes(4, "little")).hex()
        vertices = (
            [(0, 0), (side, 0), (side, side), (0, side), (0, 0)]
            if square
            else [(0, 0), (side, 0)]
        )
        rows.append(
            dict(
                name=(
                    f"side {side} clock {rate}" if square else f"east {side} rate{rate}"
                ),
                input="",
                stdout="",
                instructionLimit=100000,
                initialMemory=[dict(address="0x0000000010010000", bytes=initial)],
                memory=[dict(address="0x0000000010010000", bytes=initial)],
                stores=[
                    dict(kind="none", address=address, length=length)
                    for address, length in [
                        ("0x0000000010010000", 8),
                        ("0x00000000ffff8030", 4),
                        ("0x00000000ffff8040", 4),
                    ]
                ],
                marsBot=dict(
                    instructionsPerTick=rate,
                    x=0 if square else side,
                    y=0,
                    heading=0 if square else 90,
                    moving=False,
                    penDown=False,
                    trail=[dict(x=x, y=y) for x, y in vertices],
                ),
            )
        )
    return dict(formatVersion=5, cases=rows)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--east", action="store_true")
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(profile(not args.east), indent=2) + "\n")
