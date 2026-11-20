"""The exercise's forward policy and baseline coefficient reconstruction."""

def quantize(coefficients, quant=8, intra=False):
    if type(quant) is not int or not 1 <= quant <= 31 or type(intra) is not bool:
        raise ValueError('Use quantizer 1..31 and a Boolean INTRA selector')
    if len(coefficients) != 64 or any(type(c) is not int or not -2048 <= c <= 2047 for c in coefficients):
        raise ValueError('Supply 64 signed twelve-bit coefficients in natural order')
    levels = []; reconstructed = []
    for index, value in enumerate(coefficients):
        if intra and index == 0:
            dc = min(254, max(1, (value + 4) // 8))
            levels.append(255 if dc == 128 else dc)
            reconstructed.append(8 * dc)
        else:
            magnitude = min(127, abs(value) // (2 * quant))
            level = -magnitude if value < 0 else magnitude
            levels.append(level)
            raw = 0 if level == 0 else (quant * (2 * magnitude + 1) - (1 if quant % 2 == 0 else 0)) * (-1 if level < 0 else 1)
            reconstructed.append(min(2047, max(-2048, raw)))
    return levels, reconstructed, sum(v != 0 for v in levels[1 if intra else 0:])
