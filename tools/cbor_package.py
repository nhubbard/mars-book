#!/usr/bin/env python3
"""Export chapter 32's native sources and public exercise artifacts without composing prose."""

from pathlib import Path
import json
import hashlib
import re
import shutil

from console_capture_exports import export

ROOT = Path(__file__).resolve().parents[1]
ID = "032-cbor-telemetry"
PACKAGE = ROOT / "packages" / ID
NATIVE = ROOT / "includes/chapters" / ID
DRAFT = ROOT / "drafts/cbor-construction"


def write_json(path, data):
    path.write_text(json.dumps(data, indent=2) + "\n")


def build():
    PACKAGE.mkdir(exist_ok=True)
    registry = ROOT / "book/captures" / ID / "figures.json"
    reviewed = json.loads(registry.read_text())["figures"] if registry.exists() else []
    reviewed_names = {Path(f["nativeAnnotations"]).name for f in reviewed}
    # Check every existing reviewed export before any package files are copied.
    for figure in reviewed:
        target = PACKAGE / figure["nativeAnnotations"]
        receipt = PACKAGE / "captures" / figure["receipt"]
        if target.exists():
            if not receipt.exists():
                raise ValueError("Untracked native export retained")
            expected = json.loads(receipt.read_text())["nativeExportSha256"]
            if hashlib.sha256(target.read_bytes()).hexdigest() != expected:
                raise ValueError("Edited native export retained")
    for name in ["starter", "native", "checks", "captures", "fixtures"]:
        (PACKAGE / name).mkdir(exist_ok=True)
    shutil.copyfile(DRAFT / "main.asm", PACKAGE / "starter/main.asm")
    for name in ["inputs.json"]:
        shutil.copyfile(DRAFT / name, PACKAGE / name)
    preparation = (
        (DRAFT / "prepare.py")
        .read_text()
        .replace('default=ROOT / "main.asm"', 'default=ROOT / "starter/main.asm"')
    )
    (PACKAGE / "prepare.py").write_text(preparation)
    shutil.copyfile(
        ROOT / "drafts/cbor-fixtures/fixture.py", PACKAGE / "fixtures/fixture.py"
    )
    for path in (DRAFT / "checks").rglob("*.kts"):
        target = PACKAGE / "checks" / path.relative_to(DRAFT / "checks")
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(path, target)
    for path in NATIVE.glob("*.tex"):
        if path.name in reviewed_names:
            continue  # The reviewed exporter checks old receipts before updating figure exports.
        text = (
            path.read_text()
            .replace("includes/chapters/" + ID, "packages/" + ID + "/native")
            .replace("drafts/cbor-construction/main.asm", "starter/main.asm")
            .replace("drafts/cbor-construction", "the package")
            .replace("drafts/cbor-fixtures/fixture.py", "fixtures/fixture.py")
        )
        (PACKAGE / "native" / path.name).write_text(text)
    draft_steps = json.loads((DRAFT / "steps.json").read_text())["steps"]
    steps = []
    for index, step in enumerate(draft_steps):
        steps.append(
            dict(
                id=step["id"],
                title=step["title"],
                kind=step["kind"],
                quiz=step["quiz"],
                hints=step["hints"],
                content="native/" + step["id"] + ".tex",
                prerequisites=[] if index == 0 else [draft_steps[index - 1]["id"]],
                completion=dict(
                    type="cli-grade", check="checks/construction/" + step["id"] + ".kts"
                ),
                recipes=step["recipes"],
                region=step["region"],
                target=step["target"],
            )
        )
    write_json(PACKAGE / "steps.json", dict(steps=steps))
    plans = []
    recipe_steps = {
        r["id"]: r["step"]
        for r in json.loads((DRAFT / "inputs.json").read_text())["recipes"]
    }
    for plan in json.loads((DRAFT / "captures/plan.json").read_text())["figures"]:
        plans.append(
            dict(
                id=plan["id"],
                step=recipe_steps[plan["recipe"]],
                recipe=plan["recipe"],
                checkpoint=plan["checkpoint"],
                theme="IntelliJ Light",
                captureStatus="planned",
                caption=plan["purpose"],
                alt="Planned view of "
                + ", ".join(plan["panes"])
                + " at "
                + plan["checkpoint"]
                + ". No screenshot has been captured.",
                nativeAnnotations="Pending actual capture coordinates and semantic descriptions",
            )
        )
    write_json(PACKAGE / "captures/plan.json", dict(figures=plans))
    reading = []
    native_names = re.findall(
        r"\\input\{includes/chapters/" + ID + r"/([^}]+)\}",
        (NATIVE / "chapter.tex").read_text(),
    )
    figure_names = {p["id"].removeprefix("cbor-"): p for p in plans}
    for name in native_names:
        if re.fullmatch(r"s\d\d", name):
            reading.append(dict(id="step-" + name, kind="step", step=name))
            reading += [
                dict(id="figure-" + p["id"], kind="figure", figure=p["id"])
                for p in plans
                if p["step"] == name
                and p["id"].removeprefix("cbor-") not in native_names
            ]
        elif name in figure_names:
            figure = figure_names[name]
            reading.append(
                dict(
                    id="figure-" + figure["id"],
                    kind="figure",
                    figure=figure["id"],
                    typesetting=dict(nativeLatex="native/" + name + ".tex"),
                )
            )
        else:
            reading.append(
                dict(
                    id="p-" + name,
                    kind="prose",
                    content="native/" + name + ".tex",
                    origin="ai",
                    typesetting=dict(nativeLatex="native/" + name + ".tex"),
                )
            )
    write_json(
        PACKAGE / "reading.json",
        dict(
            formatVersion=1,
            authority="Native LaTeX; exercise and asset index only",
            segments=reading,
        ),
    )
    write_json(
        PACKAGE / "references.json",
        dict(
            sources=[
                dict(
                    title="RFC 8949",
                    url="https://www.rfc-editor.org/rfc/rfc8949.html",
                    scope="CBOR heads, types and core deterministic encoding",
                ),
                dict(
                    title="Local telemetry teaching profile",
                    source="native/profile.tex",
                    scope="Bounded fields, selected subset and application projection",
                ),
            ]
        ),
    )
    environment = dict(
        isa="MIPS64 Release 6",
        syscallAbi="MARS educational",
        jdk="25",
        ide="IntelliJ IDEA 2026.3",
        upstreamCommit="3f0f46080d037eb25fbc0fa55f6efe09f7072838",
        memoryLayout="Default",
        byteOrder="little-endian",
        graderRequirements=[
            "callFunction",
            "readDataAddress",
            "readBytes",
            "traceMemoryAccesses",
            "setRegisterToTextAddress",
            "startAt",
            "runUntil",
            "provideFiles",
        ],
    )
    manifest = dict(
        formatVersion=1,
        id=ID,
        version="0.1.0",
        status="draft",
        audience="Students with basic programming knowledge learning assembly",
        title="Serialize CBOR telemetry",
        environment=environment,
        modes=["guided", "free-writing"],
        progression=dict(
            default="recommended-order",
            instructorControls=[
                "require-quiz",
                "require-check",
                "hint-availability",
                "allow-skip",
                "allow-mode-switch",
            ],
            localImplementation="Native prose and public intermediate/final CLI checks; remote delivery and enforced policies pending",
        ),
        entry="starter/main.asm",
        assignment="assignment.tex",
        reading="reading.json",
        prerequisites=[
            "Counted binary fields and explicit byte order",
            "Nested calls and full-width caller state",
            "Unsigned bounds, bit masks and selected memory intervals",
        ],
        learningObjectives=[
            "Separate item heads, payloads, elements and map pairs",
            "Validate bounded nested data before publishing a record",
            "Retain full unsigned counter bits and signed sixteen-bit readings",
            "Project optional metadata into an exact required-field encoding",
            "Preserve caller outputs on completed failure",
        ],
        inputDomain=dict(
            types=[0, 1, 2, 4, 5],
            definiteOnly=True,
            mapKeys="Unique unsigned values zero through seven in every map",
            maxByteString=32,
            maxArray=64,
            maxMap=8,
            maxOpenContainers=8,
            completeInputBytes=[7, 1024],
            requiredFields={
                "0": "uint64 counter",
                "1": "byte identifier zero through thirty-two bytes",
                "2": "zero through sixty-four signed sixteen-bit readings",
            },
            optionalKeys=[3, 4, 5, 6, 7],
            nonpreferredHeads="Accepted on input; shortest heads on output",
        ),
        functionContract=dict(
            entries=[
                "cbor_head",
                "cbor_skip",
                "telemetry_decode",
                "telemetry_bound",
                "telemetry_encode",
            ],
            statuses={
                "0": "success",
                "1": "format, field or depth",
                "2": "selected count or capacity",
                "4": "construction-only unfinished body",
            },
            failureCount=0,
            recordBytes=176,
            maxEncodedBytes=241,
            memory="Disjoint caller objects; byte pointers may be unaligned; flat records aligned to eight bytes",
            publication="Completed decode failure preserves all record bytes; success writes prefix and active fields only",
            preflight="Encoder checks exact capacity before identifier reads or output writes",
        ),
        steps=steps,
        figures="captures/plan.json",
        publicChecks=[
            "checks/" + name + ".kts"
            for name in [
                "head",
                "skip",
                "decode",
                "format",
                "bound",
                "encode",
                "caller",
            ]
        ],
        exports=[
            "package.json",
            "README.md",
            "assignment.tex",
            "starter",
            "native",
            "checks",
            "captures",
            "reading.json",
            "fixtures",
            "references.json",
            "inputs.json",
            "steps.json",
            "prepare.py",
        ],
    )
    write_json(PACKAGE / "package.json", manifest)
    if reviewed:
        export(ROOT, ID)
    return manifest


if __name__ == "__main__":
    data = build()
    print(data["id"], len(data["steps"]), "steps exported")
