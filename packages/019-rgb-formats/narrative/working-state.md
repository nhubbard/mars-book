# Working registers and partial returns

Arguments become full working pointers and remaining count; v0 retains the original count. Scalar pack uses t0,t1,t2 for components. Expansion retains the packed value in t0 and expanded components in t1,t2,t3. These private roles are construction fixtures, not free-writing grading requirements.

Replace each placeholder branch and nop. Initial vector gates choose the scalar tails; other unfinished regions return. A partial function can return the right count before its output is complete. The author checker pauses at the supplied return continuation before caller instructions overwrite temporary registers or vector lanes. Interpret each register according to the layout established by its preceding instruction. Preserve the explicit delayed-transfer nop slots and full pointer width.
