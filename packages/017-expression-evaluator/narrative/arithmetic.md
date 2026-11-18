# Use wide arithmetic to check a narrow result
The function's result domain is signed 32-bit integers, while the machine's general
registers hold 64 bits. Use that extra width to calculate a candidate result before
deciding whether it belongs to the required domain. Two signed 32-bit operands have
an exact sum and difference that fit in a signed doubleword. Their exact product
also fits: even multiplying $-2^{31}$ by itself produces only $2^{62}$.
The doubleword multiply therefore retains the complete product needed for this check.

After a candidate is computed, take its low word and sign extend that word back to
a full register. If the result equals the original candidate, the candidate was
representable as a signed word. If it differs, the operation overflowed the exercise's
range. This is a representability test, not a decision to accept a wrapped word.
For $2{,}147{,}483{,}647+1$, the candidate is $2{,}147{,}483{,}648$; sign-extending
its low word produces $-2{,}147{,}483{,}648$, so equality fails.

Reject a zero denominator before executing division. For a nonzero denominator,
use signed doubleword division and then the same result-range check. Dividing the
minimum signed word by minus one produces positive $2^{31}$, which fits in a
signed doubleword but not in the required signed word. This call reports overflow.
A negative quotient truncates toward zero: minus seven divided by three is minus
two, not minus three. Floating-point conversion is unnecessary and could obscure
the exact integer and error rules.

The literal accumulator is also wide. Before appending a digit, its accepted value
is at most 2,147,483,647. Multiplying that prefix by ten and adding a digit can produce
at most 21,474,836,479, well within a doubleword. Test against the literal maximum
before consuming the offending digit. Leading zeros are allowed; their count is
bounded by input length rather than by the number of digits normally needed to print
a signed word.
