from pygments.style import Style
from pygments.token import Token
from . import manifest


def contrast_on_white(color):
    channels = [int(color[i : i + 2], 16) / 255 for i in (0, 2, 4)]
    linear = [
        v / 12.92 if v <= 0.04045 else ((v + 0.055) / 1.055) ** 2.4 for v in channels
    ]
    luminance = sum(
        weight * value for weight, value in zip((0.2126, 0.7152, 0.0722), linear)
    )
    return 1.05 / (luminance + 0.05)


def print_color(color):
    """Retain the IDE hue; darken only when small text needs more contrast on white."""
    if contrast_on_white(color) >= 4.5:
        return color
    channels = [int(color[i : i + 2], 16) for i in (0, 2, 4)]
    # Choose the lightest integer scale that meets the print threshold.
    for scale in range(254, -1, -1):
        candidate = "".join(f"{value * scale // 255:02x}" for value in channels)
        if contrast_on_white(candidate) >= 4.5:
            return candidate
    raise AssertionError("Black always meets contrast on white")


def _styles():
    _, appearances = manifest()
    styles = {Token: "#000000"}
    for token, appearance in appearances.items():
        styles[token] = " ".join(
            [
                "#" + print_color(appearance["foreground"]),
                "bold" if appearance["bold"] else "nobold",
                "italic" if appearance["italic"] else "noitalic",
            ]
        )
    return styles


class MarsIdeaLightStyle(Style):
    name = "mars-idea-light"
    background_color = "#ffffff"
    styles = _styles()
