# Revised state and buffers unit

The native main chapter is `main/state-and-buffers.tex`; its written supplement is
`labs/state-and-buffers.tex`. The current proofs contain 51 main pages and 35 lab
pages. Chapter eight begins on main page 45 and lab page 27. These are continuing
revision proofs, not the completed curriculum.

The chapter uses finite eight-by-eight Life to explain source snapshots, padded
array bounds, preserved state across helper calls, complete destination construction
and pointer-role exchange. A native three-generation diagram shows why matching
initial and final images do not prove that the intervening update happened. Four
printed helper/core/driver fragments occur exactly in the complete public
`labs/files/life-buffers.asm`. The public source supplies the omitted setup,
epilogue, seed copy and familiar render loops; it is not an instructor-only solution.

| Outcome | Written activity | Technical evidence |
| --- | --- | --- |
| Keep decisions in one generation | Trace the first in-place wrong birth at (4,2) | Verified counterexample and source-protection histories |
| Derive safe padded addresses | Account for offsets 11..88 and all eight neighbors | All 512 local binary states and every finite-board position |
| Preserve live state across calls | Inspect the 48-byte frame and cursor registers | Full-width saved/caller state, including a clobbering helper |
| Recognize a complete published result | Inspect built board, pointer exchange and ready boundary | All requested generation counts 0..4 and stopped-session phase checks |
| Extend an update without changing its result | Count changed rather than live cells | Public checks and 640 private cases per equivalent implementation |

Repeat the worked-program qualification with JDK 25:

```sh
python3 tools/check_revision_life.py \
  --java /absolute/path/to/jdk-25/bin/java \
  --cli /absolute/path/to/cli-1.0-SNAPSHOT.jar \
  --output /new/report/directory
```

Per accepted implementation, it runs 670 generation fixtures, 22 direct update
calls and 576 direct neighbor calls. The literal program and reverse traversal with
a deliberately clobbering helper both pass. Seven semantic faults are rejected.
The qualification checks every local binary neighborhood but not all 2^64 complete
boards. Larger boards/colors are outside this contract; whole-board patterns are
sampled. Relevant store histories do not constitute a whole-address-space audit.

The independent starter is `labs/files/life-changes.asm`; its failing body is
intentional. Public formative checks are `labs/checks/life-changes.kts`. They require
a complete next board, correct changed-cell count, protected source/halos and
preserved saved/caller state without fixing an algorithm, frame or scratch registers.
Private checks accept counting during construction, comparing in a second pass and
relocated boards, with 640 cases each. Four faulty submissions are rejected, including
a store that leaves the source value unchanged but violates its read-only contract.
Instructor references and private cases remain private. No new grader primitive
was required.

Two actual light-theme IDE Debug runs verify the final image after one and two
updates, with all 64 logical pixels checked in tooltips, native rendering and exact
screen-center samples. The two-update input is the exact public source. The one-update
fixture changes only the generations word from 2 to 1; all instructions and other
data remain identical. Raw captures and exact plugin bindings are private technical
evidence; the book uses its native conceptual diagram instead of another IDE figure.

The four active printed fragments use actual IntelliJ highlighter exports. All 787
prior records remain unchanged; 792 records include one retained superseded combined
listing. The separate authoring plugin passes 104 Jupiter tests; public tooling passes
391 tests. Existing 008 Life and 014 ring-buffer packages still validate and build.
Both proofs pass automated PDF/UA-2 validation and visual review, including grayscale
inspection of the generation diagram. Original manuscript and unrelated MARS edits
remain preserved.

The eighth native interactive unit and human learner/assistive-technology reviews
remain open. The ring-buffer project is preserved and its revised written companion now follows
in section 8.6, on pages 30–35; see ring-unit.md for its separate qualification. The other companion revisions and the
remaining main chapters also remain open. The proposed opening MARS-versus-hardware
explanation is deferred. This milestone does not complete the curriculum goal.
