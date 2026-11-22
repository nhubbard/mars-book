# Decisions-and-repetition unit preparation

The independent range-counter contract in `independent/` is qualified through the
actual CLI grader. It supplies ten public cases and a deliberately incomplete
starter. The generated check accepts both a correct remaining-count implementation
and a correct end-pointer implementation; the exercise asks you to explain and
write the latter. Three empty cases pass even for the starter, while its seven
nonempty cases fail. Passing those empty cases is not completion of the program.

This directory is not yet an importable native lesson. Its manifest, guided
content and real IDE walkthrough remain pending. The main learning book and
written lab remain the authority for the explanation. Existing chapter-four text
still identifies the native unit as unfinished.

The native walkthrough must compare the complete range, an empty range and the
tail ending at sixteen. These need separately source-bound Debug observations:
changing one file's data cannot keep execution credit for its previous revision.
The lesson host currently binds Run/Debug observations to its single entry file.
Extend the versioned format and native file-opening path before claiming these
three source variants as checked activities; keep the existing source invalidation
rules and instructor prerequisites intact.

All screenshot/controller work stays in the separate authoring plugin. Complete
programs and the three 153-pair instructor case sets remain private. The public
`.cases.json` and generated `.kts` describe observable transaction and read/write
boundaries, not a source-pattern or exact instruction-count requirement.
