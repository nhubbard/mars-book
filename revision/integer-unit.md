# Revised integer unit

The second main-book chapter is directly composed in
`revision/main/integer-representation.tex`; its supplement is
`revision/labs/integer-representation.tex`. At this unit’s first publication, the combined main proof had 11 pages and the lab
proof had 9. Later chapters extend those proofs; chapter 2 still starts on page 6
and lab 2 on page 7.
The original manuscript remains preserved.

The teaching sequence goes from eight-bit place values to hexadecimal, two's
complement, widening, a complete byte inspector, debugger reasoning and arithmetic
width. The companion lab explains predictions, two different faults, test
selection, masking versus validation, and independent output variations.

The public examples `revision/labs/files/byte-inspector.asm` and
`word-boundary.asm` match the literal native listings. They use MIPS64 Release 6
and the educational console ABI. The existing 004-bit-inspector project remains
available with its historical assignment; this chapter introduces its integer
concepts before the next chapter introduces memory. Its old step sequence is not
an interactive implementation of this revised unit.

Repeat the executable check with JDK 25 and the current MARS CLI:

```sh
python3 tools/check_revision_integers.py \
  --java /absolute/path/to/jdk-25/bin/java \
  --cli /absolute/path/to/cli-1.0-SNAPSHOT.jar \
  --output /new/report/directory
```

The check compares exact output and full final registers for all 256 admitted
byte inputs, three explicitly outside-range masking observations, and the
32-/64-bit arithmetic experiment. Four assembled faults expose missing sign
extension, a wrong output argument, a mask that loses the sign bit and the wrong
arithmetic width. This verifies worked examples, not all possible independent
solutions or native IDE pause locations. No new grading primitive was needed.

New listings use actual IntelliJ highlighting exported by the separate authoring
plugin, with unchanged previous snippet spans. Native proofs retain Minion Pro,
Noto Sans Math and tagged structure. Automated PDF/UA-2 and page review results
are retained privately with exact sources, fonts, exports and execution records.
Automated accessibility checks do not replace human accessibility review.

The second native interactive unit, its quizzes and checks, and its new annotated
IDE figures remain to be implemented. The supplement explicitly supports manual
Run/Debug in the meantime. Human editorial review and the author's independent
walkthrough remain open. The proposed opening MARS-versus-hardware explanation
remains deferred.
