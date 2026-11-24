"""Use the same pinned KTFMT style for maintained files and generated grading scripts."""

import atexit
import base64
from functools import lru_cache, wraps
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import threading

ROOT = Path(__file__).resolve().parents[1]
TOOLS = ROOT / "tools/formatting"
_LOCK = threading.Lock()
_PROCESS = None
_LOG = None


def _stop():
    global _PROCESS, _LOG
    if _PROCESS is not None:
        _PROCESS.stdin.close()
        try:
            _PROCESS.wait(timeout=5)
        except subprocess.TimeoutExpired:
            _PROCESS.terminate()
            _PROCESS.wait(timeout=5)
        _PROCESS.stdout.close()
        _PROCESS = None
    if _LOG is not None:
        _LOG.close()
        _LOG = None


atexit.register(_stop)


def _start():
    global _PROCESS, _LOG
    if _PROCESS is not None and _PROCESS.poll() is None:
        return _PROCESS
    _stop()
    command = [
        str(ROOT / "tools/authoring-plugin/gradlew"),
        "-p",
        str(TOOLS),
        "writeClasspath",
        "--quiet",
        "--console=plain",
    ]
    result = subprocess.run(command, capture_output=True, text=True)
    if result.returncode:
        raise RuntimeError(
            "Could not prepare pinned KTFMT: " + result.stdout + result.stderr
        )
    classpath = (TOOLS / "build/ktfmt-classpath.txt").read_text()
    java = os.environ.get("MARS_FORMAT_JAVA")
    if not java:
        java = (
            str(Path(os.environ["JAVA_HOME"]) / "bin/java")
            if "JAVA_HOME" in os.environ
            else shutil.which("java")
        )
    if not java:
        raise RuntimeError("Java is required for the project Kotlin formatter")
    _LOG = tempfile.TemporaryFile(mode="w+t")
    _PROCESS = subprocess.Popen(
        [java, "-Xmx2g", "-cp", classpath, str(TOOLS / "KotlinFormattingServer.java")],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=_LOG,
        text=True,
        encoding="utf-8",
    )
    return _PROCESS


@lru_cache(maxsize=32)
def format_kotlin(source, *, script=True):
    """Format a complete Kotlin file; string literals retain their program contents."""
    with _LOCK:
        server_hash = hashlib.sha256(
            (TOOLS / "KotlinFormattingServer.java").read_bytes()
        ).hexdigest()
        key = hashlib.sha256(
            (
                "ktfmt-0.65-meta:" + server_hash + ":" + str(script) + ":" + source
            ).encode()
        ).hexdigest()
        cache = TOOLS / "build/formatted-cache" / (key + ".txt")
        if cache.exists():
            return cache.read_text(encoding="utf-8")
        process = _start()
        kind = "SCRIPT" if script else "REGULAR"
        process.stdin.write(
            kind + " " + base64.b64encode(source.encode()).decode("ascii") + "\n"
        )
        process.stdin.flush()
        response = process.stdout.readline().rstrip("\n")
        if not response:
            _LOG.seek(0)
            raise RuntimeError("KTFMT stopped without a result: " + _LOG.read())
        status, payload = response.split(" ", 1)
        output = base64.b64decode(payload).decode("utf-8")
        if status != "OK":
            raise ValueError("KTFMT rejected Kotlin source: " + output)
        cache.parent.mkdir(parents=True, exist_ok=True)
        cache.write_text(output, encoding="utf-8")
        return output


def formatted_kotlin(function):
    """Keep a grading-script renderer's output in the repository's Kotlin style."""

    @wraps(function)
    def render(*args, **kwargs):
        return format_kotlin(function(*args, **kwargs))

    return render
