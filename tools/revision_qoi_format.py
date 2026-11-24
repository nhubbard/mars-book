"""Strict host model of the documented bounded QOI domain, used to verify file artifacts."""

from dataclasses import dataclass
import struct

END = bytes(7) + b"\x01"
MAX_FILE = 20502


@dataclass(frozen=True)
class Image:
    width: int
    height: int
    rgba: bytes
    channels: int = 4
    colorspace: int = 0


def slot(pixel):
    return sum(a * b for a, b in zip(pixel, (3, 5, 7, 11))) & 63


def decode(data):
    if not 23 <= len(data) <= MAX_FILE or data[:4] != b"qoif":
        raise ValueError("file extent or magic")
    width, height, channels, colorspace = struct.unpack(">IIBB", data[4:14])
    if (
        not 1 <= width <= 64
        or not 1 <= height <= 64
        or channels not in (3, 4)
        or colorspace not in (0, 1)
    ):
        raise ValueError("header domain")
    cache = [(0, 0, 0, 0)] * 64
    previous = (0, 0, 0, 255)
    pixels = []
    position = 14
    last_index = None
    count = width * height
    while len(pixels) < count:
        if position >= len(data) - 8:
            raise ValueError("missing chunk")
        tag = data[position]
        position += 1
        size = 3 if tag == 254 else 4 if tag == 255 else 1 if tag >> 6 == 2 else 0
        if position + size > len(data) - 8:
            raise ValueError("truncated chunk")
        run = 1
        index = None
        if tag == 254:
            previous = tuple(data[position : position + 3]) + (previous[3],)
        elif tag == 255:
            previous = tuple(data[position : position + 4])
        elif tag >> 6 == 0:
            index = tag & 63
            if last_index == index:
                raise ValueError("consecutive identical index chunks")
            previous = cache[index]
        elif tag >> 6 == 1:
            delta = tuple(((tag >> shift) & 3) - 2 for shift in (4, 2, 0))
            previous = tuple((a + b) & 255 for a, b in zip(previous[:3], delta)) + (
                previous[3],
            )
        elif tag >> 6 == 2:
            green = (tag & 63) - 32
            packed = data[position]
            delta = (green + (packed >> 4) - 8, green, green + (packed & 15) - 8)
            previous = tuple((a + b) & 255 for a, b in zip(previous[:3], delta)) + (
                previous[3],
            )
        else:
            run = (tag & 63) + 1
        position += size
        if len(pixels) + run > count:
            raise ValueError("run exceeds image")
        pixels.extend([previous] * run)
        cache[slot(previous)] = previous
        last_index = index
    if data[position:] != END:
        raise ValueError("marker or trailing bytes")
    return Image(
        width, height, bytes(v for pixel in pixels for v in pixel), channels, colorspace
    )
