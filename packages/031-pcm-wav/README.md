# PCM WAV student package

This is a native LaTeX chapter snapshot and an assembly exercise package. The book's
master text lives under includes/chapters/031-pcm-wav in the public repository. The
native/ files and assignment.tex retain native prose; JSON files index exercises and
capture plans and do not generate or control book composition. Read the compiled book
for typeset text. Rebuilding its LaTeX requires the full public book source and documented
font/tool setup; no proprietary font files are distributed here.

Copy starter/main.asm to a working file. From this package directory, prepare a manual
observation with `python3 prepare.py s01-1 --source working.asm --output observation.asm`.
Use a fresh output name. Preparation preserves the complete instruction section.

Run `java -jar /path/to/cli.jar grade checks/construction/s34.kts working.asm` for a guided
checkpoint, or choose the step you are implementing. These checks install their own
observation data and file fixtures. They pause at the described state; later regions
need not remain unfinished. Guided mode follows the illustrated regions and frames.

Run each of bound, parse, filter, encode, format, files and caller under checks/ after
finishing the relevant interfaces. Those completed tests accept equivalent algorithms
and private state layouts while requiring the exact published behavior. Keep supplied
harness data/caller labels so the grader can invoke the public entries. The file tests
use isolated in-memory fixtures and do not open host files.

For an ordinary pipeline run, create a real input.wav in its launch working directory:
`python3 fixtures/fixture.py input.wav --fixture trace`. Select listen for the two-second
input. Prepare s63-1 for the trace or s64-1 for listen, then launch the prepared
assembly with that directory as its working directory. The helper refuses to replace an existing file. Manually prepared assembly alone
does not create a host file or force short reads and writes.

The environment is the development MARS MIPS64 Release 6 CLI on JDK 25, pinned in the
manifest. It includes the public grading APIs listed there. The four IntelliJ Light
capture plans have no captured PNGs or annotation receipts yet. Remote progression and
hint enforcement, independent accessibility validation and substantive prose editing
remain pending. Instructor solutions, private cases and quiz keys are distributed
separately and are absent from this archive.
