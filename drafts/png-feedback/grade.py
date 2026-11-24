#!/usr/bin/env python3
"""Run finite public PNG completed-interface feedback with separate test storage."""

import argparse, hashlib, json, subprocess, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
DOMAINS = ("loader", "collect", "inflate", "rows", "pack", "decode", "reuse", "caller")


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("sources", nargs="+", type=Path)
    p.add_argument("--cli", type=Path, required=True)
    p.add_argument("--java", default="java")
    p.add_argument("--domain", choices=DOMAINS, action="append")
    p.add_argument("--include", type=Path, action="append", default=[])
    p.add_argument("--report", type=Path)
    a = p.parse_args()
    if a.report and a.report.exists():
        p.error("Report already exists; choose a fresh path")
    domains = []
    for name in dict.fromkeys(a.domain or DOMAINS):
        check = ROOT / "checks" / (name + ".kts")
        harness = ROOT / "checks/harness.asm"
        args = [
            a.java,
            "-Xmx3g",
            "-jar",
            str(a.cli.resolve()),
            "grade",
            str(check),
            *[str(s.resolve()) for s in a.sources],
            str(harness),
        ]
        for include in a.include:
            args += ["--include", str(include.resolve())]
        result = subprocess.run(args, capture_output=True, text=True)
        lines = result.stdout.splitlines()
        passed = sum(s.startswith("PASS ") for s in lines)
        errors = [s for s in lines if s.startswith(("FAIL ", "ERROR "))]
        domains.append(
            dict(
                domain=name,
                exitCode=result.returncode,
                passed=passed,
                failed=sum(s.startswith("FAIL ") for s in errors),
                errors=errors,
                checkSha256=hashlib.sha256(check.read_bytes()).hexdigest(),
                stderr=result.stderr if result.returncode else "",
            )
        )
        print(
            name
            + ": "
            + str(passed)
            + " passed"
            + (", failed" if result.returncode else ""),
            file=sys.stderr,
        )
    report = dict(
        package="033-png",
        passed=all(d["exitCode"] == 0 and d["passed"] > 0 for d in domains),
        sources={
            str(s.resolve()): hashlib.sha256(s.read_bytes()).hexdigest()
            for s in a.sources
        },
        cliSha256=hashlib.sha256(a.cli.read_bytes()).hexdigest(),
        harnessSha256=hashlib.sha256(harness.read_bytes()).hexdigest(),
        domains=domains,
    )
    text = json.dumps(report, indent=2) + "\n"
    if a.report:
        with a.report.open("x") as output:
            output.write(text)
    print(text, end="")
    return 0 if report["passed"] else 1


if __name__ == "__main__":
    sys.exit(main())
