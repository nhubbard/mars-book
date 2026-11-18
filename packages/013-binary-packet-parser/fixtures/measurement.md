# Observed native parser work

The verified moving-pointer and indexed implementations executed respectively
111/70 instructions for an empty payload, 132/94 for three bytes, and 559/582 for
64 bytes. Input reads were 17/9, 20/12, and 81/73. These are successful independently
called function counts in the default layout, excluding setup, assembly, main,
and exit. Both implementations use byte input loads. The indexed version saves
fixed header work but adds one instruction per payload byte in these particular
loops. Their measured paths are 111+7L and 70+8L; equality occurs at L=41.

This is not a host-time benchmark or a grading instruction-count requirement.
Check safe access widths, error priority, output preservation and calling
conventions after changing an implementation. Fewer loads alone do not establish
a faster host execution time or lower target cycle count.
