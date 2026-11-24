#!/usr/bin/env python3
"""Export authoritative native H.263 prose and student assets; never compose book text."""

import argparse
import hashlib
import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ID = "035-h263"
STAGES = [
    ("sad", "h263-sad-construction", "sad-construction", 32),
    ("motion", "h263-motion-construction", "motion-construction", 56),
    ("prediction", "h263-prediction-construction", "prediction-construction", 96),
    ("macroblock", "h263-macroblock-construction", "macroblock-construction", 64),
    ("transform", "h263-transform-construction", "transform-construction", 96),
    (
        "quantization",
        "h263-quantization-construction",
        "quantization-construction",
        204,
    ),
    ("block", "h263-reconstruction-construction", "block-construction", 144),
    ("writer", "h263-bit-writer-construction", "bit-writer-construction", 160),
    ("coefficients", "h263-coefficient-construction", "coefficient-construction", 100),
    ("syntax", "h263-macroblock-syntax", "macroblock-syntax-construction", 116),
    ("picture", "h263-picture-construction", "picture-construction", 54),
    ("clip", "h263-clip-construction", "clip-construction", 74),
]
ENTRIES = (
    "h263_sad16",
    "h263_sad16_msa",
    "h263_search16",
    "h263_predict8",
    "h263_predict_mb",
    "h263_transform8",
    "h263_quantize8",
    "h263_encode_block8",
    "h263_write_bits",
    "h263_emit_coefficients",
    "h263_emit_macroblock",
    "h263_encode_picture",
    "h263_encode_clip",
)


def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()


def write(p, x):
    p.write_text(json.dumps(x, indent=2) + "\n")


def rewrite(text):
    return text.replace("drafts/h263-", "construction/h263-").replace(
        "book/captures/035-h263/", "packages/035-h263/captures/"
    )


def uniform_fills(text):
    """Preserve uniform arena bytes while reducing assembler initialization work."""
    offset = 0
    out = []
    for line in text.splitlines():
        align = re.match(r"\.align ([0-9]+)$", line)
        if align:
            boundary = 1 << int(align[1])
            offset = (offset + boundary - 1) // boundary * boundary
        byte = re.search(r"\.byte -91:([0-9]+)$", line)
        wide = re.search(r"\.dword -6510615555426900571:([0-9]+)$", line)
        if byte:
            size = int(byte[1])
            if offset % 8 or size % 8:
                raise ValueError(
                    "Uniform arena needs its original eight-byte alignment and extent"
                )
            line = (
                line[: byte.start()] + ".dword -6510615555426900571:" + str(size // 8)
            )
            offset += size
        elif wide:
            offset = (offset + 7) // 8 * 8 + 8 * int(wide[1])
        out.append(line)
    return "\n".join(out) + "\n"


def harness(paths):
    data = []
    code = []
    labels = set()
    for p in paths:
        text = p.read_text()
        a, b = text.split(".text\n", 1)
        a = re.sub(r"^\.data(?:\s+[^\n]+)?\n", "", a, count=1)
        for name in re.findall(r"^(\w+):", text, re.M):
            if name in labels:
                raise ValueError("Duplicate grading label: " + name)
            labels.add(name)
        data.append(a)
        code.append(b)
    return (
        uniform_fills(".data 0x100000000\n" + "\n".join(data))
        + "\n.text\n"
        + "\n".join(code)
    )


def build(root=ROOT):
    dest = root / "packages" / ID
    native = root / "includes/chapters" / ID
    previous = dest / "snapshot.json"
    if previous.exists():
        old = json.loads(previous.read_text())
        protected = {
            name: item["exportSha256"] for name, item in old.get("sources", {}).items()
        }
        protected.update(old.get("nativeExports", {}))
        for name, digest in protected.items():
            path = dest / name
            if path.exists() and sha(path) != digest:
                raise ValueError(
                    "Edited export retained; edit its authoritative source before re-exporting: "
                    + name
                )
    dest.mkdir(parents=True, exist_ok=True)
    for name in ("native", "checks", "starter", "construction", "captures"):
        (dest / name).mkdir(exist_ok=True)
    provenance = {}

    def copy(source, target, prose=False):
        if source.is_symlink() or not source.resolve().is_relative_to(root.resolve()):
            raise ValueError("Escaping or symlink export source: " + str(source))
        target.parent.mkdir(parents=True, exist_ok=True)
        if prose:
            target.write_text(rewrite(source.read_text()))
        else:
            shutil.copyfile(source, target)
        provenance[str(target.relative_to(dest))] = dict(
            source=str(source.relative_to(root)),
            sourceSha256=sha(source),
            exportSha256=sha(target),
            transform="path-prefix only" if prose else "byte copy",
        )

    for folder in [s[1] for s in STAGES] + ["h263-fixtures"]:
        for p in sorted((root / "drafts" / folder).iterdir()):
            if p.is_file() and p.suffix in (".py", ".asm", ".json", ".md"):
                copy(p, dest / "construction" / folder / p.name)
    for domain, folder, _, _ in STAGES:
        copy(
            root / "drafts" / folder / "check.kts", dest / "checks" / (domain + ".kts")
        )
    for domain, folder, _, _ in STAGES:
        source = root / "drafts" / folder / "harness.asm"
        target = dest / "checks" / ("harness-" + domain + ".asm")
        target.write_text(harness([source]))
        provenance[str(target.relative_to(dest))] = dict(
            source=str(source.relative_to(root)),
            sourceSha256=sha(source),
            exportSha256=sha(target),
            transform="equivalent uniform A5 initialization in aligned doublewords",
        )
    write(
        dest / "checks/harnesses.json",
        {s[0]: "harness-" + s[0] + ".asm" for s in STAGES},
    )
    # The complete caller stays unfinished; dependencies and correct target bodies are never exported.
    copy(root / "drafts/h263-clip-construction/main.asm", dest / "starter/main.asm")
    bare = ".text\nmain:\n    li $v0, 10\n    syscall\n"
    for entry in ENTRIES:
        bare += entry + ":\n    li $v0, 7\n    move $v1, $zero\n    jr $ra\n    nop\n"
    (dest / "starter/interfaces.asm").write_text(bare)
    stage_by_native = {s[2]: s for s in STAGES}
    steps = []
    segments = []
    source_order = re.findall(
        r"\\input\{includes/chapters/035-h263/([^}]+)\}",
        (native / "chapter.tex").read_text(),
    )
    for name in source_order:
        p = native / (name + ".tex")
        text = p.read_text()
        copy(p, dest / "native" / p.name, True)
        if name.startswith("fig-"):
            continue
        if name not in stage_by_native:
            segments.append(
                dict(
                    id="p-" + name,
                    kind="prose",
                    content="native/" + p.name,
                    origin="ai",
                    typesetting=dict(nativeLatex="native/" + p.name),
                )
            )
            continue
        domain, folder, _, _ = stage_by_native[name]
        pattern = r"(?=\\section\{Step [0-9]+ )"
        sections = [s for s in re.split(pattern, text) if s.strip()]
        if len(sections) != 10:
            raise ValueError("Expected ten construction sections: " + name)
        for section in sections:
            match = re.match(
                r"\\section\{Step ([0-9]+) ([^\n]+)\}\\label\{([^}]+)\}", section
            )
            assert match, name
            number, title, ident = match.groups()
            prompt = re.search(
                r"\\paragraph\{Check\}\s*(.*?)\\begin\{itemize\}", section, re.S
            )[1].strip()
            options = [
                dict(id=m[0], text=m[1].strip())
                for m in re.findall(
                    r"\\item\[([A-Z])\.\]\s*(.*?)(?=\\item\[|\\end\{itemize\})",
                    section,
                    re.S,
                )
            ]
            assert len(options) == 3, (name, ident)
            content = "native/" + ident + ".tex"
            (dest / content).write_text(rewrite(section).rstrip() + "\n")
            step = dict(
                id=ident,
                number=int(number),
                title=title,
                kind="edit",
                stage=folder,
                domain=domain,
                content=content,
                prerequisites=[steps[-1]["id"]] if steps else [],
                quiz=dict(id=ident, format="latex", prompt=prompt, choices=options),
                completion=dict(
                    type="native-observation",
                    completedStageCheck="checks/" + domain + ".kts",
                    scope="Observe the matching stage working source; completed-entry feedback separately checks the whole stated contract",
                ),
            )
            steps.append(step)
            segments.append(dict(id="step-" + ident, kind="step", step=ident))
    assert len(steps) == 120 and [s["number"] for s in steps] == list(range(1, 121))
    figures = []
    specs = [
        (
            "h263-sad-result",
            10,
            "sad_caller_return",
            "h263-sad-construction",
            "SAD result before the caller stores it",
            "The completed MSA SAD call returns status zero and unsigned difference sum 7872; the source is stopped before the caller copies returned fields.",
        ),
        (
            "h263-motion-result",
            20,
            "motion_caller_return",
            "h263-motion-construction",
            "Selected motion and its residual cost",
            "At the completed search return, dx and dy are minus two, SAD is 2976 and the unique admitted candidate count is 25.",
        ),
        (
            "h263-prediction-residual",
            30,
            "prediction_caller_return",
            "h263-prediction-construction",
            "Prediction and signed residual are separate arrays",
            "The completed prediction record begins with sample 120, its first signed residual is eight, and total residual SAD is 232.",
        ),
        (
            "h263-block-reconstruction",
            70,
            "block_caller_return",
            "h263-reconstruction-construction",
            "Selected levels determine the reconstructed pixel",
            "The completed block exposes level one, reconstructed DC coefficient 23, inverse residual three and reconstructed pixel 131.",
        ),
        (
            "h263-coefficient-bits",
            90,
            "coeff_caller_return",
            "h263-coefficient-construction",
            "A complete coefficient payload at an unaligned cursor",
            "The compact worked INTRA block appends 16 bits; the retained context cursor and touched bytes show the bit boundary while unused surrounding bits remain intact.",
        ),
        (
            "h263-clip-accepted",
            120,
            "clip_caller_return",
            "h263-clip-construction",
            "The complete generated clip is accepted",
            "After eight actual encoded pictures and checked output writes, clip status is zero, accepted count is eight and progress names the final accepted reconstructed reference.",
        ),
    ]
    for ident, number, checkpoint, folder, caption, alt in specs:
        step = steps[number - 1]["id"]
        figures.append(
            dict(
                id=ident,
                step=step,
                stage=folder,
                checkpoint=checkpoint,
                theme="IntelliJ Light",
                captureStatus="planned",
                caption=caption,
                alt=alt,
                source="construction/" + folder + "/main.asm",
                captureImplementation="Separate MARS Book Authoring plugin depending on MARS; fixture and expected values must be verified before publishing coordinates",
            )
        )
        at = next(i for i, s in enumerate(segments) if s.get("step") == step)
        segments.insert(at + 1, dict(id="figure-" + ident, kind="figure", figure=ident))
    capture_plan = dest / "captures/plan.json"
    if capture_plan.exists():
        saved = json.loads(capture_plan.read_text())
        if [f["id"] for f in saved["figures"]] != [f["id"] for f in figures]:
            raise ValueError(
                "Capture identities changed; retain and reconcile the reviewed plan"
            )
    else:
        write(capture_plan, dict(formatVersion=1, figures=figures))
    approved = root / "book/captures" / ID
    if (approved / "figures.json").exists():
        previous_plan = capture_plan.read_text()
        figures = json.loads(previous_plan)["figures"]
        seen = set()
        for figure in json.loads((approved / "figures.json").read_text())["figures"]:
            if figure["id"] in seen:
                raise ValueError("Duplicate approved H.263 figure")
            seen.add(figure["id"])
            canonical = next(f for f in figures if f["id"] == figure["id"])
            if any(figure[k] != canonical[k] for k in ("step", "stage", "checkpoint")):
                raise ValueError(
                    "Approved H.263 figure differs from its canonical checkpoint"
                )
            paths = [Path(figure[k]) for k in ("artifact", "receipt")]
            if (
                any(p.is_absolute() or ".." in p.parts for p in paths)
                or paths[0].parent != paths[1].parent
            ):
                raise ValueError("Unsafe approved H.263 asset")
            source = approved / paths[0].parent
            if source.is_symlink() or not source.resolve().is_relative_to(
                approved.resolve()
            ):
                raise ValueError("Unsafe approved H.263 folder")
            if any(p.is_symlink() for p in source.rglob("*")):
                raise ValueError("Symlink approved H.263 asset")
            target = dest / "captures" / paths[0].parent
            shutil.copytree(source, target, dirs_exist_ok=True)
            receipt = json.loads((target / paths[1].name).read_text())
            composition = native / Path(figure["nativeAnnotations"]).name
            if sha(composition) != receipt["nativeSourceSha256"]:
                raise ValueError("Native H.263 composition changed since review")
            receipt["nativeExportSha256"] = sha(dest / figure["nativeAnnotations"])
            write(target / paths[1].name, receipt)
            canonical.update(
                figure,
                artifact="captures/" + figure["artifact"],
                receipt="captures/" + figure["receipt"],
            )
        updated_plan = dict(formatVersion=1, figures=figures)
        if json.loads(previous_plan) != updated_plan:
            write(capture_plan, updated_plan)
    write(dest / "steps.json", dict(steps=steps))
    write(dest / "reading.json", dict(formatVersion=1, segments=segments))
    write(dest / "checks/counts.json", {s[0]: s[3] for s in STAGES})
    copy(
        root / "drafts/h263-fixtures/fixture.py",
        dest / "construction/h263-fixtures/fixture.py",
    )
    (dest / "assignment.tex").write_text(
        "% Native fragment; use the book preamble.\n\\input{packages/035-h263/native/assignment}\n"
    )
    # Chapter fragment exported in the same include order; the directly edited book remains authoritative.
    chapter_export = dest / "native/chapter.tex"
    chapter_export.write_text(
        (native / "chapter.tex")
        .read_text()
        .replace("includes/chapters/035-h263/", "packages/035-h263/native/")
    )
    provenance["native/chapter.tex"] = dict(
        source=str((native / "chapter.tex").relative_to(root)),
        sourceSha256=sha(native / "chapter.tex"),
        exportSha256=sha(chapter_export),
        transform="include-prefix only",
    )
    manifest = dict(
        formatVersion=1,
        id=ID,
        version="0.1.0",
        status="draft",
        title="Encode a restricted H.263 stream",
        audience="Students with basic programming competence and prior assembly/MSA experience",
        environment=dict(
            isa="MIPS64 Release 6",
            syscallAbi="MARS educational",
            jdk="25",
            ide="IntelliJ IDEA 2026.3",
            upstreamCommit=(root / "docs/h263-grader-revision.txt").read_text().strip(),
            byteOrder="both native byte orders in grading",
            graderRequirements=[
                "callFunction",
                "readDataAddress",
                "readBytes",
                "traceMemoryAccesses",
                "provideFiles.failWriteAfterBytesByPath",
            ],
            memoryLayout="Student ordinary data below the reserved wide-address grading arena; each domain supplies its documented private stack",
        ),
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
            localImplementation="Native stage observations and completed feedback; automatic remote enforcement remains pending",
        ),
        entry="starter/main.asm",
        assignment="assignment.tex",
        reading="reading.json",
        gradingHarness="checks/harness-clip.asm",
        checkHarnesses={
            "checks/" + s[0] + ".kts": "checks/harness-" + s[0] + ".asm" for s in STAGES
        },
        prerequisites=[
            "MIPS64 calling conventions, full-width addressing and MSA",
            "Bounded file I/O and the earlier image/video chapters",
        ],
        learningObjectives=[
            "Build interoperable restricted baseline QCIF H.263 output",
            "Preserve accuracy through selected levels and local reconstruction",
            "Keep accepted reconstructed references separate from raw and staged pictures",
            "Validate complete buffers and publish according to each stated failure contract",
            "Compare finite work, file size and decoded quality independently",
        ],
        functionContract=dict(
            entries=list(ENTRIES),
            profile="QCIF planar Y/Cb/Cr input; one initial I and INTER-only P pictures; explicit row GOB headers; bounded full-pixel search; baseline coding without optional annex modes",
            maxPictures=299,
            maxDurationSeconds=10,
            additionalGates=[
                "Full inverse transform Annex A accuracy and exact-zero checks",
                "Independent complete-stream decoding, source quality and local reconstruction comparison",
            ],
        ),
        steps=steps,
        figures="captures/plan.json",
        publicChecks=["checks/" + s[0] + ".kts" for s in STAGES],
        exports=[
            "package.json",
            "README.md",
            "assignment.tex",
            "starter",
            "native",
            "checks",
            "captures",
            "reading.json",
            "construction",
            "steps.json",
            "grade.py",
            "snapshot.json",
        ],
    )
    # Record the full minimum revision without depending on Git in an extracted archive.
    manifest["environment"]["upstreamCommit"] = (
        (root / "docs/h263-grader-revision.txt").read_text().strip()
    )
    assert re.fullmatch(r"[0-9a-f]{40}", manifest["environment"]["upstreamCommit"])
    copy(root / "drafts/h263-package/README.md", dest / "README.md")
    copy(root / "drafts/h263-package/grade.py", dest / "grade.py")
    write(dest / "package.json", manifest)
    write(
        dest / "snapshot.json",
        dict(
            formatVersion=1,
            scope="Copy/export receipts only; no prose is generated from metadata",
            sources=provenance,
            nativeExports={
                str(p.relative_to(dest)): sha(p)
                for p in sorted((dest / "native").glob("*.tex"))
            }
            | {"assignment.tex": sha(dest / "assignment.tex")},
        ),
    )
    return manifest


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--root", type=Path, default=ROOT)
    a = p.parse_args()
    m = build(a.root)
    print(ID, len(m["steps"]), "steps;", sum(s[3] for s in STAGES), "public cases")
