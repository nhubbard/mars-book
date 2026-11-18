# Convert between a cell and its address

For row $`y=1`$, column $`x=1`$, and width eight,
``` math
i=8y+x=9,\qquad y=\lfloor i/8\rfloor=1,\qquad x=i\bmod8=1.
```
The wall byte is at $`\text{walls}+9`$. The pixel word starts at $`\text{frame}+4\cdot9`$, which is $`\text{frame}+36`$. An index names an entry; the element width determines its byte displacement.

Moving east from cell seven first produces the coordinate pair $`(0,8)`$. That pair is rejected. Converting it immediately to ID eight would instead name $`(1,0)`$, which is a different geometric move.
