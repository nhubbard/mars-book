# Two programs at the same endpoint

| Program behavior | Final position | Pen trail | Meets the drawing contract? |
| --- | --- | --- | --- |
| Exit immediately | (0, 0) | Empty | No |
| Draw east, then return west | (0, 0) | Two collinear opposing strokes | No |
| Draw the four requested sides | (0, 0) | Four ordered square sides | Yes, if final controls and protected data agree |
| Write a RAM list of corners, then exit | (0, 0) | Empty | No |

A final position is necessary but insufficient. The history assertion uses the actual
device strokes, not a list maintained by the submitted program.
