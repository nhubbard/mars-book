# SHA-256 companion to word transformations

Section 22.7 revises original project `025-sha256`. It occupies lab pages 146–153
in the 153-page proof, sharing page 146 with the preceding ChaCha20 exercise.
The shared lab chapter is now titled “Word transformations”; the main book is
unchanged at 145 pages. Native LaTeX remains the directly edited composition.

## Teaching sequence and student work

Seven conceptual subsections distinguish message bytes, numeric schedule words
and serialized digest bytes; derive four schedule predecessors and the two small
sigmas; trace a compression round before feed-forward; define the context
invariant; prove one-block versus two-block padding; and connect successful read
prefixes to final publication at EOF. The final subsection asks the reader to
build and defend independent end-to-end evidence. Two short instruction excerpts,
two native tables and four actual Debug figures support the explanation.

The student implements all six interfaces in `labs/files/sha256.asm`. That file
supplies only caller/data arenas, fixed constants and intentional break entries.
Full solutions and alternatives remain private. Completed grading permits
independent private labels, frames, allocation, helpers and equivalent algorithms.
The original package's cumulative construction source and 47 steps remain
available separately; the printed companion does not repeat them as sections.

The existing `packages/025-sha256/prepare.py` creates deterministic observations.
The printed command is executed against the new skeleton: exact input bytes are
checked, instruction text is preserved, and repeat invocation refuses existing
outputs without changing either output or source. The prose names the supplied
caller defaults required by the tool. An ordinary host file is not described as
forcing short reads or injected failures.

## Finite execution evidence

Reference, equivalent alternative and above-32-bit-data variants each pass 54
public kernel, 40 file, 12 caller/program, 22 reuse, 3341 private and two additional
4096-byte file cases: 10,413 positive grading-case executions. These are case
executions, not a count of distinct programs or function calls. The unfinished
independent skeleton fails all 54 kernel cases as intended.

Fourteen assembled semantic faults are rejected. Their exact wide-address
reference baseline passes the same 54 kernel and 40 file cases. Faults cover
arithmetic sigma shifts, wrong predecessors/constants, overwritten round state,
missing feed-forward, narrowed pointers, both padding thresholds, missing marker,
word-sized length operations, hashing a requested rather than returned count,
read error treated as EOF and descriptor leakage.

Three additional exact written observations verify the first compression round,
a 63-plus-two-byte update and the synthetic full-width bit-length field. Four
capture probes verify the schedule row, complete abc digest, final padding length
and unchanged destination after a read error. A separate Python integer model
agrees with hashlib for sixteen complete messages and supplies the worked trace.
The large-count example tests serialization of a synthetic context; no huge file
was read. Both implementations also pass eight real host-file observations each:
seven existing files including padding boundaries and 4096 bytes, plus one missing
file. These ordinary syscall runs are independent of virtual file fixtures.

## Publication evidence and limits

The separate authoring plugin passes 104 Jupiter cases and is launched through
actual `clean cleanSandbox runIde` tasks. Four IntelliJ Light Debug captures are
byte-identical to the published PNGs and retain native coordinate receipts.
The read-error capture also records actual virtual operations: open descriptor 19,
read three, read two, fail a read and close. Its closed-session receipt confirms
no descriptor remains open. The exact I/O fixture stays in the authoring plugin;
no authoring code or new source changes were added to MARS.

Native LaTeX owns figure titles, outlines, crops, captions and alternative text.
The caption distinguishes what the image shows from separately checked status
and closure. Both printed excerpts reuse exact existing actual IntelliJ export
records; the 825-record manifest is unchanged. Minion Pro, Noto Sans Math and
JetBrains Mono remain in the embedded font set. All twelve pages affected by the
shared heading and companion receive color review; pages 147–152 receive grayscale
review. The exact final tagged PDF and passing automated PDF/UA-2 report are
retained privately, together with earlier proof/report pairs from layout review.

These finite checks establish observed behavior and publication structure. They
do not establish NIST certification, hardware timing, author approval, teaching
effectiveness or assistive-technology usability. Author, representative learner
and assistive-technology reviews remain pending. The native guided lesson and
enforced progression/hint policies for this advanced companion remain open.

The overall goal remains active. The RSA companion is next, followed by WAV,
PNG and the retained lossless/H.263 video sequence. The deferred opening
explanation of MARS versus hardware has not been added.
