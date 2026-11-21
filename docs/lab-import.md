# Importing a student lab archive

In IntelliJ, choose **Tools → Import MARS Lab…**. Select a student ZIP archive with
Browse, or paste its full path, then choose **Preview lab**. Review the title,
package ID, version and required target. **Import lab** opens the assignment and
starter in the current project. Cancelling either dialog leaves no working copy.

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

The bundled first unit remains available through **Tools → MARS Learning** with
its native questions, Run/Debug observations and independent assessment. Importing
a version 1 student archive does not turn its historical step metadata into native
trainer tasks. A remote catalog and general native lesson-content delivery remain
in development. Native LaTeX continues to control the revised book composition.
