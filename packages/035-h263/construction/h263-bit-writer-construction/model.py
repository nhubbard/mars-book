"""A bit-list oracle independent of target byte-mask scheduling."""


def append(data, used, value, width):
    if (
        not isinstance(data, bytes)
        or len(data) > 1048576
        or any(type(v) is not int for v in (used, value, width))
        or not 0 <= used <= 8 * len(data)
        or not 0 <= width <= 32
        or not 0 <= value < (1 << width)
    ):
        raise ValueError(
            "Invalid buffer, used count or fitting 0..32-bit unsigned field"
        )
    if width > 8 * len(data) - used:
        raise BufferError("Insufficient capacity")
    bits = [(b >> shift) & 1 for b in data for shift in range(7, -1, -1)]
    field = [(value >> shift) & 1 for shift in range(width - 1, -1, -1)]
    bits[used : used + width] = field
    result = bytes(
        sum(bits[8 * i + j] << (7 - j) for j in range(8)) for i in range(len(data))
    )
    return result, used + width
