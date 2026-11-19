# Serialize CBOR telemetry

This is the draft student package for chapter 32. It assumes basic programming knowledge
and the preceding assembly work on counted binary data, nested calls and unsigned bounds.
Use the book's native reading and this package's assignment fragment for the complete
profile. The record's counter, binary identifier and signed readings have separate
wire and local representations; optional metadata must validate before being dropped.

Copy starter/main.asm to a working source and keep editing that file. Run
`python3 prepare.py s01-1 --source working.asm --output observation.asm` with a fresh
output name for an IDE observation. This changes fixture bytes and controls, preserving
all authored instructions. The package contains 56 steps, 52 unfinished regions,
56 quizzes, 112 hints and 57 recipes. No quiz keys or complete MIPS solutions are included.

With the MARS CLI jar on JDK 25, run a completed check as
`java -jar /path/to/cli.jar grade checks/head.kts working.asm`. Repeat for skip, decode,
format, bound, encode and caller. Run a guided check as
`java -jar /path/to/cli.jar grade checks/construction/s26.kts working.asm`.
These scripts install the named recipe data themselves. Guided checks stop at their
checkpoint and remain usable when later bodies are complete. They follow the illustrated
frames and registers. Completed checks accept other private organization and algorithms;
keep the public entries and harness arenas/caller labels used to supply inputs.

The counter and identifier observations deliberately move those fields first, so their
bodies can be checked before later fields exist. Step 55 has separate short-capacity
encode and trailing-byte decode recipes. Completed failures must preserve their promised
caller outputs. Development status four with zero count means an unfinished region;
it may follow earlier valid work and is not a completed format/capacity failure.

Generate fixed observation bytes with `python3 fixtures/fixture.py trace.cbor`; use
--fixture canonical, empty, maximum or budget as needed, or --record for the local image.
Existing outputs are refused. The feedback scripts contain a host-side model, not an
assembly solution. Finite public checks provide feedback rather than proof for all inputs;
instructors can supply additional cases under the same contract.

Native LaTeX text is the publication authority. reading.json indexes exercises and
planned assets; it does not regenerate the prose. The fragments use package paths from
the public book repository when composed into its publication. Instructor progression,
hint and mode policies are documented for future delivery; remote enforcement is not
implemented. The four IntelliJ Light capture plans are uncaptured. Accessibility and
substantive human editorial review remain pending.
