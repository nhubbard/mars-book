# Restricted baseline H.263: student package

Follow the native reading and its 120 incremental construction steps, or use
`starter/interfaces.asm` for free writing against the same thirteen callable
entries. Twelve folders under `construction/` retain the earlier working sources,
preparation helpers and public models. Keep your own implementations and carry
them forward with the matching composition helper; no completed assembly encoder
or quiz answer keys are included. `starter/main.asm` provides the final ordinary
clip caller with unfinished high-level entries, not a prebuilt solution.

The native LaTeX fragments are exported from the directly edited book includes.
`reading.json` and `steps.json` index those fragments and quizzes; they do not
compose or regenerate the book. Within this archive, chapter command examples
use `construction/` in place of the source repository's `drafts/` prefix. The
native fragments require the book preamble for typesetting, as do the earlier
chapter packages. `snapshot.json` pins the authoritative sources and exported
bytes, including the explicit path-only changes in the exported reading.

Run completed-interface feedback while building one domain:

```sh
python3 grade.py working.asm --domain sad --java /path/to/java \
  --cli /path/to/mars-cli.jar --report sad-feedback.json
```

Omit `--domain` for all twelve domains after composing the complete source. All
1196 cases run against separate reserved grading storage; do not copy that harness
into a submission. Keep ordinary student data outside the wide-address grading
arena and reserve the prefixes listed in the native assignment. Each interface
supplies its documented private stack; constants and alternate private frames are
allowed. Finite feedback accepts equivalent legal entropy codes, skip choices
and accurate transform rounding while checking the stated policy and failure
behavior. Unfinished development statuses are expected to fail completed checks.

Use the minimum full MARS revision in `package.json` or a compatible newer build.
The independently targeted output-error cases require
`provideFiles(..., failWriteAfterBytesByPath = ...)`, added in commit `6c66f101`.
Older grader archives lack that option. The full inverse-transform Annex A
accuracy gate is an additional required instructor check; the finite transform
domain alone does not establish the complete inverse error limits.

The final working source can be run end to end with generated raw input:

```sh
python3 construction/h263-clip-construction/run.py joined-clip.asm \
  --java /path/to/java --cli /path/to/mars-cli.jar --output-dir run-eight
```

The output directory must be new. Python generates raw QCIF samples and controls,
then the actual MIPS encoder writes its stream and reconstruction. FFmpeg is
required only for independent decoding/checking. Reports retain exact source,
input and tool hashes, quality, local/decoder drift and the finite guest count.
Use the corresponding `prepare.py` alone for an IDE Run/Debug observation. The
default is eight pictures at 5000/1001 Hz, lasting 1.6016 seconds; input limits
remain at most thirty pictures/s and ten seconds. Both accurate reference/alternate encoders have independently decoded the default;
a reference 44-picture run also verifies temporal-reference wraparound.

Guided work follows native Observe/Check sections; free writing shares the same
interfaces and semantic acceptance. Instructor choices for quizzes, hints, stage
checks, progression and mode switching are recorded. Downloadable remote lessons
and automatic progression enforcement remain future service/plugin work. Six
light-theme IDE figures now retain verified original captures, native LaTeX annotations,
numbered legends and reviewed color/grayscale proofs. Capture
implementation belongs in the separate authoring plugin depending on MARS.
Independent accessibility review and later substantive editing remain separate.
