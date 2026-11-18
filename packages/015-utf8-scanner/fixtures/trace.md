# Default state checkpoints

Counts and offsets are decimal; next-byte bounds are hexadecimal.

|Call|New bytes|Total|Count|Need|Next|Start|Status|Error|
|---:|---:|---:|---:|---:|---|---:|---:|---:|
|0|2|2|1|2|80..bf|1|0|-1|
|1|3|5|2|3|90..bf|4|0|-1|
|2|2|7|2|1|80..bf|4|0|-1|
|3|4|11|5|0|80..bf|-1|1|-1|

If byte five becomes 8f, call two reports malformed input with zero newly accepted
bytes, start four and detecting offset five. Call three rejects reuse with -3/0.
If the fourth chunk is instead empty final input, it reports truncation with zero
newly accepted bytes, start four and detecting end seven.
