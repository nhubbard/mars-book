# A wrong dependency can change a pixel

At the second sample, (2,2), err is 4 and the saved e2 is 8. The x step changes
err to 1. Testing the y condition with the original 8 gives false because 8 > 5;
the next point is (3,2).

If the program recomputes twice the changed error, it gets 2 and takes a y step,
selecting (3,3) instead. Every arithmetic instruction can be individually correct
while the algorithm is wrong because it used a value from the wrong boundary.
