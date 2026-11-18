# Public PPM completion checks

Run `java -jar /path/to/cli.jar grade checks/parse.kts /path/to/your-main.asm`
from the PPM construction directory. These are completed-interface checks. An
unfinished region can fail them while passing its current guided checkpoint.

- `parse.kts`: metadata, comments, header bounds and exact raster extent.
- `decode.kts`: packed dimensions, little-endian color words, binary samples and capacity.
- `encode.kts`: canonical decimal headers, ignored high pixel bytes and exact output bounds.
- `format.kts`: malformed headers, truncation, trailers, precedence and unsigned counts.
- `files.kts`: short reads/writes, exact-capacity EOF, injected errors, selected byte counts,
  denied opens, preservation before validation and descriptor cleanup.
- `caller.kts`: connected load/decode/encode/save, canonicalization, same-session recovery,
  result publication and changed memory inputs.

The host model parses the declared teaching subset with an independent regular expression.
Expected samples are constructed directly as bytes. Tests inspect caller memory footprints,
preserved full-width registers, exact results and file state. They do not inspect student
source or require algorithm labels, scratch-register choices or a particular frame size.
Keep the five published entry labels, data arenas and `function_return` test sentinel.
The caller domain additionally exercises the supplied `main` and `results_ready` boundaries.

These finite public domains supplement the private grading cases. They do not establish
exhaustive correctness. Writable paths are declared explicitly in the in-memory file fixture;
these checks do not overwrite host files. The current simulator write syscall reads one
extra source byte, so save calls retain a readable guard without including it in the file.
