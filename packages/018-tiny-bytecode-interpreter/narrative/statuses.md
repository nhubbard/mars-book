# Interpret the status with its stopping point
Validation failures precede execution. Once execution begins, stack requirements
precede capacity checks for copying operations; arithmetic requires two operands,
then checks a zero divisor when relevant, then checks result range. A successful
halt may have any live depth, including zero.


See assignment.md and the companion fixtures for complete numerical tables.


Changing the branch target in the default program from sixteen to six makes it point
at the branch's own operand. That image reports status minus three, fault five and
zero retired instructions even with a zero budget. Reducing stack capacity to two
leaves the image valid, but execution reports status minus six at PC four after two
pushes. These are failures from different phases with different retained state.

For a separate arithmetic example, encode `PUSH32` of the maximum signed word,
then `PUSH8` of one, then `ADD` and `HALT`. The addition begins
at byte seven. It reports minus seven with PC and fault seven, retired count two,
and both original values still live. With budget two, execution stops before that
addition and returns status zero with the same PC and live values.
