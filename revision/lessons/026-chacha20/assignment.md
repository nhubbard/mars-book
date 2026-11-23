# Transform words while preserving their interfaces

Read the main chapter before this laboratory. The four-entry program gives you three levels to reason about: word operations inside a quarter-round, selections inside a block, and byte intervals inside the wrapper. Errors at one level often look plausible at another. Predict the next boundary before using the debugger to inspect it.

## Read the word operation before its register display

Begin in `chacha20.asm`. The four inputs live at quad offsets 0, 20, 40 and 60. The gaps make the address interface visible: these are four independently supplied words. The first addition produces hexadecimal 12131415. The XOR consumes this new value, and the rotation consumes that XOR result. Continue through the other three groups using the latest values each time.

The scalar register view contains 64 bits. Word instructions may sign-extend their result into that view. Compare the low word with the 32-bit calculation, then explain the high half from the instruction semantics. In `worked/word-wrap.asm`, ffffffff plus one becomes zero before the XOR. A 64-bit carry would change every later dependency.

## Separate a block’s working state from its input

`worked/scalar.asm` selects the scalar block. Its first copy creates a working output while retaining the input for feed-forward. The first quarter-round updates positions 0, 4, 8 and 12. The eighth completes one column/diagonal double-round; ten such double-rounds precede the final addition of corresponding original words.

At the pre-feed boundary the transformed words are not yet the output block. After feed-forward, the first output word is e4e7f110, stored as bytes 10 f1 e7 e4. Read all sixteen words and all 64 bytes consistently. The raw-state checks also supply arbitrary words rather than only the familiar key and nonce example.

## Follow lane provenance through the MSA round

`worked/msa.asm` holds working rows in w0–w3 and originals in w4–w7. In Debug’s Threads & Variables tab, expand MSA vector registers, expand each register, and read its 32-bit integer lanes. Before a column half, each lane already connects one column’s four values. The four columns are independent, but the additions, XORs and rotations within each lane still depend on one another.

After the column half, the row selections align diagonals. Immediate 57 selects old row-one lanes 1,2,3,0; its inverse is 147. Row two’s 78 is self-inverse. Write a table of source positions separately from the changing word contents. After a diagonal half, undo the selections before the next column half. Compare canonical rows at the end of ten double-rounds, then compare the feed-forward result with both the scalar block and its independent expected bytes.

## Give the wrapper its own loop argument

`worked/stream-65.asm` supplies a patterned 65-byte message. The first generated block serves 64 bytes, the second only one. At the first after_stream_bytes visit, s2 is still 65 while s3 and s4 are 64; the subtraction follows. At the second visit all three are one. Resume execution between those two observations of the same label.

`worked/in-place-65.asm` uses the same original message and state with identical source and destination. Load each old source byte before storing its replacement. The transformed source must equal the disjoint example’s destination. The input state remains unchanged; repeating a call with the same state starts at that same stream position.

## Decide admission before writing output

At counter ffffffff, 64 bytes need only the last available block and succeed. A 65-byte request needs another counter and fails before any output is written. Inspect both `worked/last-counter-64.asm` and `worked/last-counter-65.asm`. A status-two return after publishing the first block would violate this contract.

`worked/large-count.asm` requests 0000000100000000 bytes. Its low word is zero, but its full unsigned count is excessive, so it returns status one before using supplied pointers. `worked/empty.asm` takes the admitted zero path and succeeds without state or message access. The independent data checks also use unusable pointers on these early paths.

## Build the interfaces from your own notes

Close the complete worked sources and use `independent/chacha20.asm`. Read its README and complete the quarter-round, scalar block, MSA block and wrapper in that order. The six native groups retain the 73 original cases, with changed arguments, arbitrary raw states, exact in-place buffers and repeated calls. They additionally follow the real caller through termination and stored status. Incorrect intermediate state in your own implementation can use different scratch registers; grade its public results and ownership contract, and use your own earliest-boundary trace for diagnosis.

Keep an explanation of why each selected interval is legal, where the originals survive until feed-forward, and why each independent call uses its own inputs. These arguments let you investigate a new failure beyond the finite supplied inputs.
