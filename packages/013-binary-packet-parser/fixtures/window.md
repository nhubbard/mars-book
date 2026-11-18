# A complete load must fit

For eleven available bytes, legal offsets are zero through ten. A four-byte read
starting at offset eight includes offset eleven and crosses the boundary. That
byte can be mapped padding without belonging to this input. The native chapter
includes a twelve-cell diagram with the available end and crossing width marked.
Its cell numbers are offsets, not stored values.
