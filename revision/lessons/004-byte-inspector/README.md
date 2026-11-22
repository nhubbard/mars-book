# Revised byte-inspector guided segment

This development package accompanies Chapter 2 and its written lab. Native LaTeX
remains authoritative for the publication. Its runtime definition interleaves
four formative predictions with five observations of ordinary Run. Private
instructor solutions and summative checks are not included.

The assembly files are exact copies of the revised lab sources. A Run check
observes the integer consumed by service 5, exact console output, normal
termination and the source that actually executed. An edit invalidates Run
credit; prediction and hint history remain. This segment does not award Debug
or independent-writing credit.

Build the student ZIP from the public repository root:

```sh
python3 tools/native_lesson.py revision/lessons/004-byte-inspector \
  --output build/004-byte-inspector-0.2.0-student.zip
```

The builder exports only the manifest's entry, assignment, native lesson and
explicit `supportingFiles`, preserving their literal bytes. MARS validates the
native definition before import. Serve the ZIP as an immutable course asset;
its complete content digest identifies the edition. Do not overwrite an archive
already distributed to students.

The development walkthrough checks HTTPS import, inline predictions, hints,
resumption, real Run feedback, misleading output, mid-run edits and source
rechecking. Human learner review is still required before release. The written
lab's debugger, diagnosis and independent work remain part of the unit.
