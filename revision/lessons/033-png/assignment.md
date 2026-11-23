# Check each PNG representation

Read Chapter 24 and its written lab. This native unit follows a complete supplied
decoder before assigning the opposite row transformation independently. It retains
the original 266 public forward-filter cases. The original 100-step full-decoder
construction task remains a separate capstone, with its own harness and checks.

Begin with `worked/pipeline-boundaries.asm`. It embeds the exact public 115-byte
two-by-five trace. The supplied decoder functions and format constants are retained
unchanged from the book's complete viewer; this smaller caller uses explicit
scratch buffers and a stride of seventeen words. No file path or external input
is needed for the required observations. Record 115 file bytes, 46 compressed
bytes, 35 filtered row bytes, thirty RGB samples and forty active display bytes
as different quantities. At each stop, explain what succeeded and what the next
stage still has to prove. Public dimensions and byte count are published only
after successful packing. One unchanged gap byte is evidence about that byte,
not a substitute for the independent access policies.

Answer the predictions inline in MARS Learning and use normal Debug at the named
boundaries. A label identifies the next instruction, so inspect state before
executing it. Finish each process before the next activity. If you edit execution
state or backstep while exploring, start a fresh forward Debug session to record
the required sequence. The five other worked sources isolate bit interpretation,
self-overlapping history, predictor precision, vector inputs, packed outputs, scalar tails and full-width native
settings. They are experiments, not a complete forward-filter solution.

Implement only `png_filter_scanline` in the continuing independent source. Its
README gives the exact interface. The first five assessment groups each contain
44 cases for one predictor; then 46 cases cover boundaries, aliasing and reuse.
The final full assessment checks all 266 on the completed source. All groups
assemble native settings and your declarations in both byte orders. When edits
make earlier evidence stale, follow MARS's prompts to renew it; predictions and
hint history remain saved. Free-writing mode and hint availability follow the
instructor's release policy.

The ordinary Run activity uses the complete embedded-image worked source and
terminates silently. A normal Run of the independent source calls your function
on its Average demonstration row; silence alone does not establish correct bytes.
Pause at `filter_demo_return` and inspect v0/v1 before main publishes them. Expect
status zero, count seven and output `03 0f 14 19 12 12 12` in hexadecimal.

For the additional artifact experiment, follow the written lab to collect five
rows from your implementation as 35 binary bytes. Keep each selector, without
text addresses or hexadecimal characters. `tools/png-from-rows.py` compresses and
wraps those bytes without supplying your filter. Use width two and height five,
try stored/fixed/automatic compression, and compare every decoded sample with
an independent image reader. `tools/fixture.py` produces the original teaching
fixture if you need a fresh baseline. Both tools refuse an existing output.
The required in-IDE checks do not depend on running Python or opening host files.
The written lab’s commands use book-repository paths. The matching copies in this
package are under `tools/`; run those optional commands from this imported lesson
folder, using fresh output paths.
