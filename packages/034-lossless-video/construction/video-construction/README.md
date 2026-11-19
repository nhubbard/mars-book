# Lossless-video kernel construction

The native chapter is `drafts/lossless-video.tex`. It defines the MLV1 teaching
format, full frames, XOR skip/change records, validation before publication,
indexing, seeking, presentation deadlines, encoder choices and a first scalar/MSA
kernel. The full file-codec assembly construction and chapter distributions remain
unfinished. The kernel caller is runnable and does not yet play a video file.

Copy `kernel.asm` and fill its six named regions cumulatively. Select an exact
observation without replacing any student instruction text:

```sh
python3 drafts/video-construction/prepare.py s01-1 --source my-kernel.asm --output observation.asm
```

The worked caller supplies five native words and prints `3` after completion.
Stop before the recipe's checkpoint in Debug. Unfinished regions return status 4
and a development message; invalid arguments return status 1. Count zero succeeds
without pointer use. Positive counts from one through 4096 require word alignment.
No sixteen-byte address-alignment precondition is added for the MSA entry.

The output may be separate or exactly alias either input. Partial overlaps are
outside the callable contract. The helper returns the number of nonzero XOR output
words. With previous/current images, that is the changed-pixel count. With a previous
image and a delta, it instead counts nonzero reconstructed words, which the player
need not use. The helper's numeric tests include all 32 bits, even though valid MLV1
image words have zero high bytes.

Six quizzes, twelve progressive hints and twenty-six public recipes accompany the
native passages. Completed algorithms, answers and private checks remain in the
instructor repository. Ordinary Run/Debug screenshots still require actual automated
light-theme capture; no fixture preview is labeled as an IDE observation.
