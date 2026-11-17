# Direction and boundary cases to predict

| Endpoints before ordering | Case to inspect | Expected feature |
| --- | --- | --- |
| (0,2) to (7,2) | Horizontal | y stays two |
| (3,7) to (3,0) | Vertical and reversed | Canonical y runs zero through seven |
| (1,1) to (3,7) | Steep increasing y | Some updates change only y |
| (1,6) to (6,2) | Negative slope | Successful y steps subtract one |
| (0,0) to (2,1) | Exact tie | Middle selected cell is (1,1) |
| (2,1) to (0,0) | Reversed exact tie | Same image as the previous row |
| (4,4) to (4,4) | Point | One selected cell |
| (0,7) to (7,0) | Diagonal at boundaries | Both axes change at each update |

These cases are not separate algorithms. Normalization, independent decisions, and
the post-store endpoint test handle them within one loop.
