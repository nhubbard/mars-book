# Importing a student lab archive

In IntelliJ, choose **Tools → Import MARS Lab…**. Browse to a student ZIP or
paste a direct HTTPS archive link, then choose **Preview lab**. Review the title,
package ID, version, target and activity count. **Import lab** opens the assignment
and source in the current project. Canceling either dialog leaves no working copy.

Use the revised native unit named by the written lab. The current editions and
archive filenames are listed in [the native unit index](../revision/native-unit-index.json).
The original project distributions remain available for their original assignments;
their old step metadata does not create a native lesson.

Each import has its own folder under `mars-labs/packages`. Work in the opened
starter and use ordinary MARS Run and Debug. Use the target stated by the assignment;
importing files does not select a different target for the project.

Importing the same archive again opens your existing working copy and preserves
its edits. A different version gets a separate folder. Different archive bytes
also get a separate folder even if the publisher reused the version number. Keep
working in the earlier copy until you choose to move to another version. The
importer does not merge source changes or migrate learning progress between them.

The content digest shown in the preview identifies the exact archive. The importer
retains that archive beside the working files, with a receipt, so reopening the
same distribution can distinguish its original content from student edits. A
missing starter or assignment is reported rather than silently reconstructed.

This action imports the existing public project distributions, including their
written assignments and companion files. Included grading scripts are copied as
files; importing does not execute them. Follow the assignment's separate grading
directions when you want to run its published checks. Use student distributions;
instructor-only archives belong in the private teaching workflow.

The built-in first unit is available through **Tools → MARS Learning**. Revised
native archives open their questions, hints, editor pointers and activities in
Learn. Answer inline choices there. Ordinary student Run and Debug launches
supply the requested observations; independent checks assemble the current editor
text and evaluate bounded public case data in separate simulator sessions.
Importing does not execute an included Kotlin grading script.

Use **Tools → Continue Downloaded MARS Lab** to return to the last selected
activity in that project and edition. **Tools → Choose Downloaded MARS Lab
Activity…** offers guided work and independent exercises permitted by the course
policy. Changing activities preserves source edits, quiz attempts, hint history
and completed assessments. A source edit requires affected execution evidence
to be earned again. Unchanged independent work can retain its completion.

Instructors can control prediction prerequisites, hint release and independent
entry through MARS course settings. Opening an exercise directly does not satisfy
its checking prerequisites. Equivalent implementations are accepted through the
stated state, output and access contracts, rather than source matching. Public
checks are learning feedback; a local plugin is not a secure examination boundary.

A publisher may host immutable student ZIPs over HTTPS; no course server is
deployed by this repository. IntelliJ's normal proxy and certificate handling
applies. HTTP-only sources, embedded URL credentials, login pages and malformed
archives are rejected. Rehosting identical bytes preserves the content edition.
Keep instructor archives, reference implementations and private checks off the
student endpoint. A hosted catalog, account sign-in, rosters and server-side
submission grading are separate services.

Build one revised archive with:

```sh
python3 tools/native_lesson.py revision/lessons/018-bytecode-machine \
  --output build/018-bytecode-machine-0.1.0-student.zip
```

The adjacent `.sha256` file identifies the exact artifact. Build the other units
from the source folders in the index. Native LaTeX continues to control the book;
lesson metadata and package Markdown do not generate the publication's prose.
