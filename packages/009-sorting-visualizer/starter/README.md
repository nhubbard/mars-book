# Sorting visualizer starter

Use MIPS64 Release 6, the Default layout, and little-endian byte order. The header
configures an 8 by 8 word Bitmap Display. The active values prefix has length count,
from zero through eight; active signed values are −4 through 3. Inactive words are
scratch-free input that must remain unchanged. Algorithm zero chooses adjacent-exchange
insertion sort; one chooses selection sort.

The provided labels and BEGIN/END regions make intermediate edits assemblable. Place
each fragment in its named region; some later steps append within an existing region.
Do not move the renderer into main's fall-through path. done exits before the function.
The starter exits without a valid published frame and intentionally fails the checks.

The public labels render, `sort_ready`, and done support function and visualization
checks. `sort_begin` and `sort_end` are reference measurement boundaries; other labels are
scaffolding for the guide. Free-writing code may change internal structure while
preserving the public function and snapshot contracts.
