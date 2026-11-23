# Draw four sides from coordinate feedback

The supplied program starts at the origin, facing north, stopped with its pen
up. `side` is a word from one through 63. `side_guard` follows it in memory.
Keep those declarations and the exit code; replace the failing `break` with
your implementation. This task follows the one-leg experiment but leaves the
four-leg program for you to design.

Starting at (0,0), visit (side,0), (side,side), (0,side) and (0,0), in that order
with the pen down. The actual trail must consist of exactly those four sides,
with no missing side or extra stroke. Finish at the origin, facing north,
stopped with the pen up. The requested domain is every side 1–63 at every integer
movement rate 8–128 completed simulator instructions per tick.

Coordinates increase rightward and downward. Use heading 90 for east, 180 for
south, 270 for west and zero for north. The device commands are the same as in
the one-leg program. Obtain progress through fresh coordinate loads. Stores to
x/y feedback words are forbidden, even if later restored. Preserve both input
words without writing them; preserving only their final values is insufficient.
The program must exit normally and produce no console text.

The public profile uses three distinguishing requests: side/rate 16/64, 1/64
and 63/17, each limited to 100,000 executed instructions. Each check loads a fresh
machine and actual MarsBot device. It initializes the side and a varying guard,
then checks the complete ordered trail, all final flags, unchanged input and
execution-origin writes to input and feedback. A RAM list of intended corners
cannot substitute for the device's actual trail. Passing these samples does
not replace your argument for the complete documented domain; instructor checks
may add requests.

The check fixes the supplied data interface, not your instruction sequence,
register allocation, helper labels or exact instruction count. A command table
is optional after you know arrays. A direct sequence of feedback-controlled legs
is sufficient with the current prerequisites. Keep a written invariant for each
leg and explain the transition to the next heading and the final pen command.

In the native lesson, choose **Check this program**. For optional command-line
checking, set CLI to your MARS CLI JAR and run the following from the imported
package root, the directory containing `package.json`:

```sh
java -jar "$CLI" grade independent/square.kts independent/square.asm
```

The adjacent `.cases.json` is the public contract; `.kts` is its generated
command-line equivalent. The native plugin reads retained immutable case data,
not an edited extracted checker. Leave the worked sources intact while writing
this separate program, so their observations remain useful for comparison.
