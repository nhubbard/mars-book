"""Render verified IntelliJ spans; do not infer assembly syntax in Python."""
from functools import lru_cache
import hashlib
import json
import os
from pathlib import Path
import re

from pygments.lexer import Lexer
from pygments.token import Token


def appearance_token(appearance):
    if set(appearance) != {"foreground", "background", "bold", "italic"}:
        raise ValueError("Unexpected IntelliJ appearance fields")
    for key in ("foreground", "background"):
        if not re.fullmatch(r"[0-9a-f]{6}", appearance[key]):
            raise ValueError("Invalid exported color")
    if appearance["background"] != "ffffff":
        raise ValueError("Publication code must use a white background")
    for key in ("bold", "italic"):
        if type(appearance[key]) is not bool:
            raise ValueError("Invalid exported font attribute")
    name = f'C{appearance["foreground"]}B{int(appearance["bold"])}I{int(appearance["italic"])}'
    return getattr(Token.Mars, name)


@lru_cache(maxsize=4)
def _load(path):
    document = json.loads(Path(path).read_text(encoding="utf-8"))
    if document["formatVersion"] != 1:
        raise ValueError("Unsupported IntelliJ export format")
    exports = document["exports"]
    records = {}
    appearances = {}
    for export in exports:
        source = export["source"]
        sha = hashlib.sha256(source.encode("utf-8")).hexdigest()
        if sha != export["sha256"] or sha in records:
            raise ValueError("Corrupt or duplicate IntelliJ source export")
        offset = 0
        for run in export["runs"]:
            if run["start"] != offset or not offset < run["end"] <= len(source):
                raise ValueError("IntelliJ spans must cover the exact source once")
            offset = run["end"]
            token = appearance_token(run["appearance"])
            appearances[token] = run["appearance"]
        if offset != len(source):
            raise ValueError("Incomplete IntelliJ span coverage")
        records[sha] = export
    if not records:
        raise ValueError("Empty IntelliJ export")
    return records, appearances


def manifest():
    path = os.environ.get("MARS_HIGHLIGHT_MANIFEST")
    if not path:
        raise ValueError("Set MARS_HIGHLIGHT_MANIFEST to the verified IntelliJ export")
    return _load(str(Path(path).resolve()))


class MarsIdeaLexer(Lexer):
    name = "MARS IntelliJ export"
    aliases = ["mars-idea"]
    filenames = []

    def get_tokens_unprocessed(self, text):
        records, _ = manifest()
        sha = hashlib.sha256(text.encode("utf-8")).hexdigest()
        export = records.get(sha)
        if export is None or export["source"] != text:
            raise ValueError("Assembly source has changed: refresh its IntelliJ highlight export before building")
        for run in export["runs"]:
            start, end = run["start"], run["end"]
            yield start, appearance_token(run["appearance"]), text[start:end]
