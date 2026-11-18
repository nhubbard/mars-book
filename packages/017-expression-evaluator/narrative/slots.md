# Local frame slots

Term and expression each reserve 48 bytes: left value at byte zero, operator at eight, operator offset at sixteen, return address at forty. Primary and unary reserve 32 bytes with return address at twenty-four. Primary keeps a grouped value at zero; unary keeps sign offset at zero and sign byte at eight. Each invocation owns separate addresses for those slots. The shared cursor and error are not rolled back on return.
