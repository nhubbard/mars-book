# Lossless video: student package

Follow the native reading and its 122 construction steps, or use `starter/main.asm`
for free writing; `starter/interfaces.asm` is a smaller entry-only alternative.
Nine construction folders retain the smaller stages. Keep XOR work
separate until the delta attachment imports it; prepare each observation using its
matching stage working file. All attachments preserve earlier instruction text.

`python3 grade.py working.asm --domain playback --cli /path/to/cli.jar --java
/path/to/java` runs one completed public domain; omit `--domain` for all eleven.
The runner supplies separate grading storage. Reserve the `videoCheck_` and
`videoPrivate_` prefixes and the wide-data region for feedback. Private boundary
checks require student-owned ordinary data within the first two MiB and provide a 32768-byte private
stack. Constants and alternative frames/layouts are allowed. The supplied compact
starter is below that budget and retains the supported function input limits.

`construction/compact.py --source working.asm --output submit.asm` reduces only
unused provided file/source arena tails and their capacity controls. It preserves
instructions and other data, refusing changed markers, oversized input or an
existing output. A custom free-writing implementation need not use it. Do not submit
all historical examples together or copy the grading harness into your program.

Checks compare semantic outputs and retained state, accepting valid equivalent
full/delta choices. Required periodic full records, CRCs, exact file boundaries and
pixel reconstruction still apply. Scripted playback time checks each accepted picture
without host waiting; actual CLI/IDE timing can be late. Follow each routine's own
failure contract rather than clearing every output indiscriminately.

Completed checks are expected to reject unfinished status-four bodies. During guided
work, compare the named native checkpoint first. Instructor quiz/hint/progression
controls are recorded; automatic remote enforcement remains future work. All six
light-theme IDE figures are captured and composed with native LaTeX annotations:
word lanes/count, record publication, index publication, independent seek, mandatory
full-record selection, and completed playback. The playback plate records real
host-clock lateness separately from the scripted-clock checks. Independent
accessibility review and the author's substantive editing remain separate work.
No completed assembly codec or quiz keys are included in this student distribution.
