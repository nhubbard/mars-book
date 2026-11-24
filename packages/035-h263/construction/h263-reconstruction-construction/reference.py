"""Independent floating teaching trace; accurate target transforms may round differently."""

import importlib.util
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def load(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


MATRIX = load("block_matrix", ROOT.parent / "h263-transform-construction/matrix.py")
QUANT = load("block_quant", ROOT.parent / "h263-quantization-construction/model.py")


def reconstruct(current, prediction, quant=8, intra=False):
    if (
        type(intra) is not bool
        or len(current) != 64
        or len(prediction) != 64
        or any(type(v) is not int or not 0 <= v <= 255 for v in current + prediction)
    ):
        raise ValueError("Supply two 64-sample byte blocks and Boolean INTRA")
    residual = [c - (0 if intra else p) for c, p in zip(current, prediction)]
    forward = MATRIX.reference(residual)
    levels, rec, count = QUANT.quantize(forward, quant, intra)
    spatial = MATRIX.reference(rec, True)
    pixels = [
        max(0, min(255, d + (0 if intra else p))) for d, p in zip(spatial, prediction)
    ]
    return dict(
        residual=residual,
        forward=forward,
        levels=levels,
        reconstructedCoefficients=rec,
        inverse=spatial,
        pixels=pixels,
        count=count,
    )
