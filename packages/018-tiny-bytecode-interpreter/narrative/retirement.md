# Retire an instruction only after it succeeds
Execution checks the remaining budget first, then checks whether PC identifies an
available opcode. A valid image and its validated branches guarantee that an executed
PC is an instruction beginning or the image end. Sequential execution selects the
byte immediately after the current record. A branch can instead select its validated
target. Every successfully completed instruction increments retired count and consumes
one unit of budget, including halt and a branch back to its own address.

A failing fetch or instruction consumes no budget unit and does not increment the
retired count. Underflow keeps the original depth and live values. A failed push
keeps the original full live prefix. Arithmetic overflow or division by zero retains
both operands and the current PC. The guided handlers check requirements before their
logical stack update and join one retirement path after successful work.

A budget of two on the default program retires its two pushes and stops at PC four.
With capacity two, that is a valid budget-exhausted result. A budget of three tries
the following duplicate, finds no room, and reports stack overflow at four with the
same two live values and retired count two. The reason for stopping changes even
though this active prefix and PC match.
