# Streaming UTF-8 companion

The revised independent scanner is section 12.6 of the lab book, on pages 59–65.
Its directly edited native source is `labs/companions/utf8.tex`; the compact caller
and unfinished work area are `labs/files/utf8-stream.asm`. The original versioned
015-utf8-scanner package, checks and data layout are preserved. The combined lab
proof now has 65 pages; the main book remains 78 pages.

The companion explains scalar values versus bytes and grapheme clusters, derives
restricted first continuations, defines all eight state words, traces the supplied
stream across four calls and separates malformed input from final truncation.
It makes the accepted-prefix contract explicit: stream progress is retained when
an error occurs, unlike the preceding packet parser's no-output failure. Terminal
reuse preserves the original diagnostic and permits no input reads or caller-data
stores. Interleaved streams keep their own pending state.

Three native tables supply lead classes, state layout and completed chunk states.
Four actual IntelliJ Light Memory captures appear in two native LaTeX comparisons:
pending need/minimum changes, and malformed/truncated final states. PNGs remain
unchanged; semantic descriptions, coordinates and source/image/archive bindings
are under `captures/utf8`. Complete observation implementations and expanded
checks stay private. The public screenshots display only the common caller and
native state, not the completed scanner body.

Four implementations (branches, lookup table, private stack, wide data) each pass
426 public streams, 15 caller cases and 4,522 state-transition cases. Branch and
table versions each additionally pass 17,376 scalar blocks covering every one of
the 1,112,064 scalar values, and all 65,536 two-byte pairs. These are explicit
finite domains; they do not exhaust arbitrary streams. All four also pass seven
written-example/interleaved-state probes. Twelve deliberate faults are rejected;
the independent starter fails all 426 public streams until implemented.

The public tooling suite passes 391 tests. Original project 015 validates and
builds. The capture launch used actual clean cleanSandbox runIde tasks; all eleven
installed JARs match the previously API-verified archives, with no ERROR or SEVERE
entries in its completed batch. The unchanged authoring plugin's prior 104-test
Jupiter result remains prior evidence, not a newly executed suite in this step.
No MARS or authoring-plugin source change, grader API change, or new highlighted
code fragment was needed. All 803 existing highlighting records are unchanged.

The final lab PDF passes automated PDF/UA-2 with zero failed checks. Pages 59–65
and grayscale tables/capture comparisons were visually reviewed. The established
fonts and native LaTeX publication authority remain in use. Original main.tex and
40 unrelated MARS edits are preserved. These checks establish technical and
publication integrity, not learner usability or assistive-technology validation.

The clipped-blit companion is now available as section 6.6; see clipped-unit.md.
The MarsBot companion is now section 4.7; see marsbot-unit.md. Native interactive
UTF-8 delivery, advanced chapters and remaining instructional-system work are open. The proposed
MARS-versus-hardware opening remains deferred. This milestone does not complete
the curriculum goal.

A later caller review corrected stack setup to mask the initial pointer down to
a multiple of sixteen, matching the book convention. Branch and stack-using
scanners pass the 15 caller and seven written cases again, plus direct entry/return
alignment checks across all four calls. Function instructions are unchanged.
The correction evidence is in the private clipped-companion-01/caller-alignment
record; the original UTF-8 milestone evidence remains intact.
