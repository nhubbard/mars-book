# Revised functions and stack unit

The native main chapter is `main/functions-and-stack.tex`; its written lab is
`labs/functions-and-stack.tex`. The initial unit qualification contained 30 main-book
pages and 19 lab pages. Those counts describe the historical unit proof; see
[the current proof index](README.md) for the full revised books.

The main chapter derives a small standalone calling convention through one byte
reversal and a routine that calls it twice. It explains the compact call's return
link, leaf/non-leaf routines, argument/result lifetimes, saved registers,
full-width stack slots, frame release and the caller's use of preserved state.
The convention is explicitly not an operating-system ABI. The complete worked
program is `labs/files/reverse-rows.asm`. It is the exact composition of the three
printed listings, not a separately maintained solution.

| Outcome | Written activity | Technical evidence |
| --- | --- | --- |
| State arguments, results and side effects | Trace the low byte of 0x16 and 0x81 | All byte values and upper-bit patterns |
| Follow a continuation through calls | Inspect outer and two inner return links | Stopped-session call/return boundary checks |
| Retain live values across calls | Track the second input and first result | Equivalent helper deliberately clobbers allowed registers |
| Explain a frame and preservation | Account for every eight-byte slot and restored pointer | Full-width register sentinels and caller/data guards |
| Implement a related function | Reverse and invert independently | Public exhaustive check accepts ordinary-call and tail-transfer solutions |

Repeat the worked-program qualification with JDK 25:

```sh
python3 tools/check_revision_functions.py \
  --java /absolute/path/to/jdk-25/bin/java \
  --cli /absolute/path/to/cli-1.0-SNAPSHOT.jar \
  --output /new/report/directory
```

Per accepted implementation, the check performs 768 leaf calls, 341 pair calls
and 256 complete-driver cases. Private qualification also checks a different
stack-slot implementation and a clobbering helper. Seven meaningful faulty
variants are rejected. The stricter author boundary probe checks this worked
frame's exact slots and instruction/branch counts; those details are not imposed
on students' independent implementations.

The independent starter is `labs/files/reverse-and-invert.asm`; its intentionally
failing body is the student's work area. Its public formative checks are
`labs/checks/reverse-and-invert.kts`. They accept different temporaries and frame
layouts and permit a valid tail transfer, while requiring actual execution of
the supplied helper. Instructor implementations remain private. No new grader
primitive was required; stopped calls, execution traces, full-width register
checks and address-based byte inspection were already supported.

The three new listings use actual IntelliJ exports from the separate authoring
plugin, preserving all 777 prior records. Public tooling passes 391 tests and the
separate authoring plugin passes 104 Jupiter tests. The unchanged sprite package
still validates and builds. The final proofs pass automated PDF/UA-2 and visual
review, including a grayscale listing. The original manuscript is preserved.

No fifth native interactive lesson, new IDE screenshots or graphical IDE run is
claimed in this text/source milestone. The execution boundaries are core-session
observations, not screenshots of debugger pauses. Author and representative
learner review remain open. Arrays and images are next in the agreed main-book
sequence. The opening MARS-versus-hardware explanation remains deferred.


## Prerequisite correction after the full-sequence audit

The chapter now names its actual entry requirements: byte masks, loads, stores
and compact branches. Its leaf-function section introduces `sll`, `srl` and
`or`, including operand order, discarded bits, zero filling, word width and the
cleared-bit invariant used to assemble the reversed byte. The independent
exercise explains XOR and `xori` before asking the reader to invert the result.
This corrects a first-use explanation gap; the three literal listings and
independent function contract are unchanged.

The [chapter assessment map](../docs/revised-learning-outcomes.md) relates the
current written activities to three outcomes per chapter. It records opportunities
to assess learning, not achieved student outcomes. Renewed executable, visual and
automated PDF/UA-2 evidence is retained in the private
`functions-prerequisites-01` record. No native lesson or new IDE capture is claimed.
