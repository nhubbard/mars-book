# Decode bounded RGB8 PNG images

This is the student package for chapter 33. Read the native chapter for the accepted
PNG subset and its exact interfaces. It assumes the earlier assembly, memory,
checksum and vector work. The publication source remains ordinary native LaTeX;
exercise metadata indexes the steps and planned assets without composing prose.

Guided work begins with `construction/png-construction/main.asm`. Copy it to a
working source and retain that source as you fill each stage. The six stage folders
contain strict preparation and attachment commands. Their attachment scripts add
unfinished regions without replacing your implementations. Follow the native steps
in order; use each stage's own `prepare.py` and named recipe for an observation.
The public package has 100 steps, 97 editable regions, 100 quizzes and 200 hints.
Free-writing work may begin with `starter/main.asm`, which combines every unfinished
stage. Internal loops, private frames and algorithms may differ from the examples.

Run completed-interface feedback with JDK 25 and the MARS CLI:

```sh
python3 grade.py working.asm --domain decode --cli /path/to/cli.jar --java /path/to/java
python3 grade.py working.asm --cli /path/to/cli.jar --java /path/to/java --report feedback.json
```

Domains are `loader`, `collect`, `inflate`, `rows`, `pack`, `decode`, `reuse`, and
`caller`. The runner supplies `checks/harness.asm` as another source, so no grading
code or buffers need to be copied into your program. Reserve the `pngCheck_` label
prefix for those test resources. The harness is disjoint from the actual caller's
heap. Existing reports are refused. An unfinished stage should fail its completed
feedback; develop it with the native checkpoint observation, then run that stage's
domain. Public feedback is finite and does not replace additional instructor cases.

Create a real input with `python3 construction/png-fixtures/fixture.py image.png`.
Its default is the worked two-by-five image. Use `--width`, `--height`,
`--compression stored|fixed|auto` and `--split` for variations; `--part` emits a
particular stage's bytes. The other fixture scripts generate named malformed,
Huffman, row and packing observations. Every output command refuses an existing
file. The program's ordinary working directory determines its relative image path.

Retain the documented callable entries and argument contracts. The completed
loader, collector and inflater have different scratch-failure rules from row
prevalidation and framebuffer publication. Neither context retention nor scratch
changes alone establish whether pixels were published. Both byte orders are tested.
Memory addresses retain all 64 bits, including above 4 GiB. Grading does not prescribe
private stack offsets or exact instruction counts. `png_load`, `png_collect`,
`png_inflate`, both unfilter entries, both blit entries and both complete decode
entries remain callable independently. Keep the supplied actual caller and its
public control labels for caller feedback.

Instructor materials, keys, completed MIPS implementations and private grading
remain in the separate private distribution. Instructors can specify hint,
progression and mode policies; remote enforcement is not implemented. Five actual
light-theme IDE figures and an independent accessibility audit remain pending.
The native fragments use book-repository paths when composed into the publication.
Substantive editorial work is a separate future goal.
