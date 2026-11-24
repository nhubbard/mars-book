#!/usr/bin/env python3
"""Run the 64 public completed-macroblock cases with the separate high-address harness."""

import argparse
import hashlib
import json
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parent

if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("source", type=Path)
    p.add_argument("--cli", type=Path, required=True)
    p.add_argument("--java", default="java")
    p.add_argument("--report", type=Path)
    a = p.parse_args()
    if not a.source.is_file() or not a.cli.is_file():
        p.error("Provide an existing assembly source and MARS CLI archive")
    if a.report and a.report.exists():
        p.error("Existing grading report refused")
    result = subprocess.run(
        [
            a.java,
            "-jar",
            str(a.cli),
            "grade",
            str(ROOT / "check.kts"),
            str(a.source),
            str(ROOT / "harness.asm"),
        ],
        capture_output=True,
        text=True,
    )
    print(result.stdout, end="")
    print(result.stderr, end="")
    passed = sum(line.startswith("PASS ") for line in result.stdout.splitlines())
    failed = sum(line.startswith("FAIL ") for line in result.stdout.splitlines())
    success = result.returncode == 0 and passed == 64 and failed == 0
    if a.report:
        data = dict(
            passed=success,
            casesPassed=passed,
            casesFailed=failed,
            exitCode=result.returncode,
            sourceSha256=hashlib.sha256(a.source.read_bytes()).hexdigest(),
            checkSha256=hashlib.sha256((ROOT / "check.kts").read_bytes()).hexdigest(),
            harnessSha256=hashlib.sha256(
                (ROOT / "harness.asm").read_bytes()
            ).hexdigest(),
            scope="Public completed six-block prediction behavior only; no whole-encoder claim",
        )
        with a.report.open("x") as out:
            json.dump(data, out, indent=2)
            out.write("\n")
    raise SystemExit(0 if success else result.returncode or 1)
