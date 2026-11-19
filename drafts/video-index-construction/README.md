# Complete-file index construction, steps 24–36

Read the native video chapter. This stage validates a complete in-memory MLV1 file
and publishes native file-relative record offsets. The later loader must supply
complete bytes from actual file I/O. Attach to a working record implementation:

```sh
python3 drafts/video-index-construction/attach.py --source work.asm --output index-work.asm
python3 drafts/video-index-construction/prepare.py s24-1 --source index-work.asm --output observe.asm
```

Attachment preserves the record function and other instruction text, renaming its
old `main` entry to `record_example_main`. Preparation only changes input controls.
Both commands require fresh outputs. The default caller establishes stack alignment
and uses the moving animation. Keep its large file arena, separate directory and
scratch windows, and native context extents when editing the program.

Thirteen editable regions have thirteen quizzes, twenty-six hints and twenty-five
recipes. Public fixtures include bad magic, a suffix, a late bad checksum and a
CRC-valid late invalid command. A completed failure must retain the full directory
and context. Active scratch contents are unspecified on both paths. The source
must remain immutable between validation, directory publication and later seeking.

Development status four is for unfinished construction, including an unfinished
record prerequisite. Once directory stores are written, an unfinished following
region may return four with a partial directory. Completed status one never permits
that. Free writing can use equivalent validation and a different private frame;
only the illustrated checkpoints prescribe the shown scratch/register state.

Whole-file validation uses the completed record interface. This stage adds no file
system calls, seek implementation, encoder or playback loop. Those follow later.
