#!/usr/bin/env python3
"""Grade selected H.263 domains against the package's independent per-domain storage."""

import argparse
import hashlib
import json
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def run(argv=None):
    counts = json.loads((ROOT / "checks/counts.json").read_text())
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("sources", nargs="+", type=Path)
    p.add_argument("--cli", type=Path, required=True)
    p.add_argument("--java", default="java")
    p.add_argument("--domain", choices=tuple(counts), action="append")
    p.add_argument("--report", type=Path)
    p.add_argument("--include", type=Path, action="append", default=[])
    a = p.parse_args(argv)
    if any(not s.is_file() for s in a.sources) or not a.cli.is_file():
        p.error("Supply existing source files and a matching CLI archive")
    if a.report and a.report.exists():
        p.error("Existing report refused")
    records = []
    harness_map = (
        json.loads((ROOT / "checks/harnesses.json").read_text())
        if (ROOT / "checks/harnesses.json").exists()
        else {}
    )
    for name in dict.fromkeys(a.domain or counts):
        harness = ROOT / "checks" / harness_map.get(name, "harness.asm")
        script = ROOT / "checks" / (name + ".kts")
        command = [
            a.java,
            "-Xmx3g",
            "-jar",
            str(a.cli.resolve()),
            "grade",
            str(script),
            *[str(s.resolve()) for s in a.sources],
            str(harness),
        ]
        for include in a.include:
            command += ["--include", str(include.resolve())]
        r = subprocess.run(command, capture_output=True, text=True)
        lines = r.stdout.splitlines()
        passed = sum(l.startswith("PASS ") for l in lines)
        failed = sum(l.startswith("FAIL ") for l in lines)
        errors = [
            l
            for l in (r.stdout + r.stderr).splitlines()
            if l.startswith(("ERROR ", "FAIL "))
        ]
        success = r.returncode == 0 and passed == counts[name] and failed == 0
        records.append(
            dict(
                domain=name,
                passed=success,
                casesPassed=passed,
                casesFailed=failed,
                expectedCases=counts[name],
                exitCode=r.returncode,
                errors=errors,
                checkSha256=hashlib.sha256(script.read_bytes()).hexdigest(),
            )
        )
        print(
            f"{name}: {passed}/{counts[name]} passed" + ("" if success else " (failed)")
        )
        if not success:
            print("\n".join(errors) or r.stderr[-2000:], file=sys.stderr)
    result = dict(
        package="035-h263",
        passed=all(r["passed"] for r in records),
        casesPassed=sum(r["casesPassed"] for r in records),
        casesFailed=sum(r["casesFailed"] for r in records),
        domains=records,
        sources={
            str(s.resolve()): hashlib.sha256(s.read_bytes()).hexdigest()
            for s in a.sources
        },
        cliSha256=hashlib.sha256(a.cli.read_bytes()).hexdigest(),
        harnessSha256={
            r["domain"]: hashlib.sha256(
                (
                    ROOT / "checks" / harness_map.get(r["domain"], "harness.asm")
                ).read_bytes()
            ).hexdigest()
            for r in records
        },
        limits=[
            "Finite contract checks supplement the required full inverse-accuracy gate",
            "Independent full-stream decoding and quality/drift checks remain required",
            "No private exact-source or exact-entropy-byte acceptance rule",
        ],
    )
    if a.report:
        with a.report.open("x") as out:
            json.dump(result, out, indent=2)
            out.write("\n")
    return 0 if result["passed"] else 1


if __name__ == "__main__":
    raise SystemExit(run())
