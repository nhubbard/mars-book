from pygments.style import Style
from pygments.token import Token
from . import manifest


def _styles():
    _, appearances = manifest()
    styles = {Token: "#000000"}
    for token, appearance in appearances.items():
        styles[token] = " ".join([
            "#" + appearance["foreground"],
            "bold" if appearance["bold"] else "nobold",
            "italic" if appearance["italic"] else "noitalic",
        ])
    return styles


class MarsIdeaLightStyle(Style):
    name = "mars-idea-light"
    background_color = "#ffffff"
    styles = _styles()
