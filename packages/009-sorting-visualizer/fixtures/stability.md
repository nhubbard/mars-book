# Tagged equal keys reveal stability

Selection can transform [2a,2b,1] into [1,2b,2a] with its first distant exchange.
The final key values are [1,2,2], but b now precedes a. Strictly choosing the first
minimum does not prevent this reversal of equal nonminimum records.

The current word-array representation stores no identity tags. Its duplicate tests
check multiplicities and exchange states, not general record stability. A tagged
extension must move and verify complete records.
