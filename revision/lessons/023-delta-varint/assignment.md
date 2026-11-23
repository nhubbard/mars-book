# Candidate bytes and committed values

Read Chapter 17 and its written lab before continuing in Learn. Start with the
complete delta-codec.asm. Derive a wire representation from exact signed
reading differences, then use the supplied complete variations to investigate
mapping width, unused capacity, rejected candidates and error precedence.

The guided route uses inline predictions, actual Debug observations and three
ordinary Runs. A highlighted next instruction is the next requested operation;
the selected Debug frame shows the actual pause. Use one breakpoint at a time
and finish each process before changing sources. The complete source variants
are examples to investigate, not stages whose instruction spelling you must copy.

Write scan_delta independently in independent/delta-scan.asm. Its contract is
in independent/README.md. It validates the byte stream and returns the accepted
prefix without writing decoded readings. The starter deliberately stops with
break. A correct alternative algorithm and temporary-register schedule are
accepted; the worked codec's register observations constrain only the guided
examples. Hints narrow the next investigation without replacing your explanation.

Keep an exact wire derivation, a candidate/prefix invariant and one rejected-
candidate observation. Use each assessment's first mismatch to choose a new
observation. A passing finite corpus supports those arguments; it does not prove
correctness for all possible byte strings or establish a hardware speedup.
