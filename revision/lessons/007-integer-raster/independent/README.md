# Draw alternating visits without changing the raster

Replace draw_dashed's break body. a0 points to four words x0,y0,x1,y1, with each
coordinate from zero through seven. a1 points to an eight-by-eight frame of color
words; its row stride is 32 bytes. a2 supplies a color from zero through 0x00ffffff.
Order endpoints by x, breaking equal-x ties by y, and use the chapter's halfway
rule toward the ordered endpoint B. Follow that canonical raster in order, paint
visit zero, preserve visit one, paint two and continue alternating. Return the
number actually painted in v0. A point paints once and returns one.

Preserve skipped and unrelated frame words, the endpoint record, color word, all
four guards and the caller-owned line_count word. Preserve s0–s7, fp, gp and sp at
full width, leave the 32 caller stack bytes unchanged at return, and return through
the incoming continuation. Produce no console or device output other than the
ordinary framebuffer stores. Private working labels, scratch registers, frame
layouts and equivalent raster algorithms are allowed. The input record may be
ordered in working registers; it must receive no execution stores, even if the
original values would later be restored. The color word and guards have the same
no-store protection. Frame and caller-stack byte comparisons are separate
observations at return, not a whole-memory write history.

The supplied main calls the function twice, reloading its arguments and using
line_complete and line_repeat as distinct continuations. It stores each returned
count after the callee returns. Native checks instead enter the function directly
and stop before each continuation. They do not execute main's count stores or exit
selector. line_count is caller-owned memory in these direct tests.

Sixteen public cases cover endpoint reversal, horizontal and vertical lines,
steep and falling lines, points and halfway decisions. Extra halfway cases place
the tied choice at visit two, where the dashed routine must paint: a tie only at
skipped visit one can hide a wrong rounding choice. Each case prefills every frame
word with a distinct nonzero value and varies the arguments, guards, full-width
saved-register seeds and stack location. Its first selected color is black, so
skipping zero stores cannot pass on an already-black frame.

The direct checker passes a2 independently of the caller's color word; that word
contains a different value and must remain unchanged. The ordinary main still
loads a2 from color. The function interface is the passed argument, not a request
to reload a global variable. Data pointers use actual assembled public-label
addresses, so allocations may move. Keep the public fixture labels; private
implementation labels are your choice.

Every case calls the function twice in one machine, through both continuations,
and compares the returned count and full frame words after each return. It also
checks the inputs, guards, line_count, preserved registers, sp, caller stack bytes
and silent execution. Execution-store observers watch the six protected input
regions throughout both calls. Setup writes occur before tracing. Each case owns
a fresh machine and a cumulative budget of 10000 instructions. Sixteen passing
cases represent 32 returned calls for those sampled inputs, not all 4096 endpoint
tuples or every color and prior frame.

The adjacent version-3 function profile states the exact public data. Byte strings
encode the book's little-endian words. The generated Kotlin script expresses the
same cases through the existing CLI grader. With JDK 25 and a MARS CLI supporting
label-relative byte setup, run from this directory:

java -jar /path/to/cli.jar grade dashed-line.kts dashed-line.asm

Read failure details to distinguish a wrong selected word, count, protected store
or preserved value. Keep your own address calculation and state trace alongside a
passing report. Instructor policies can control attempts, prerequisites and hint
release; private instructor variants remain in a separate distribution.
