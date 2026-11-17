# A leaf call and its continuation

![Call timeline: `jal` at P records P+8, its P+4 `nop` executes, the renderer writes the full frame and reaches `sort_ready`, then `jr` `ra` and its `nop` return to P+8 with saved registers preserved.](assets/call.svg)

The actual sequence is: caller prepares `a0`/`a1`; `jal` writes `ra`=P+8; the delay-slot `nop`
executes; render uses temporary registers; `sort_ready` exposes the frame; `jr` `ra` and
its `nop` complete the return; main continues at P+8. Register preservation comes
from the function body and contract, not automatic restoration by a jump.
