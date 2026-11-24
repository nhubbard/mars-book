"""Independent floating reference and the published scale-65536 constants."""

import math

BASIS = tuple(
    tuple(
        1 / math.sqrt(8) if u == 0 else 0.5 * math.cos((2 * x + 1) * u * math.pi / 16)
        for x in range(8)
    )
    for u in range(8)
)
SCALED = tuple(tuple(math.floor(v * 65536 + 0.5) for v in row) for row in BASIS)


def reference(samples, inverse=False):
    if len(samples) != 64:
        raise ValueError("Supply one eight-by-eight block")
    matrix = tuple(zip(*BASIS)) if inverse else BASIS
    first = [
        [sum(samples[y * 8 + x] * matrix[u][x] for x in range(8)) for u in range(8)]
        for y in range(8)
    ]
    low, high = (-256, 255) if inverse else (-2048, 2047)
    return [
        max(
            low,
            min(
                high,
                math.floor(sum(first[y][u] * matrix[v][y] for y in range(8)) + 0.5),
            ),
        )
        for v in range(8)
        for u in range(8)
    ]
