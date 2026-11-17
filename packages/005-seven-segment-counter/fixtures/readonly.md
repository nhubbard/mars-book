# Restoring the source does not remove a write

Consider an implementation that temporarily edits a glyph, uses it, then restores
the original byte. A final expectBytes check could see the initial table again.
A native store trace over the table still contains the execution writes.

For this task, the source objects must receive no stores. The instructor can use
traceMemoryStores("glyphs", 16) and expectNone() to check that property at the
label's actual address. This is distinct from merely comparing final contents.
