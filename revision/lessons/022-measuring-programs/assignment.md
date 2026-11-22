# Measuring a complete kernel

Read Chapter 13 and its written lab. Derive the default matrix product before
running it. Follow a dot product from register accumulation to its doubleword
store, then distinguish function return from the caller’s completed store.
Use the separate wide-sum source to explain positive 2^31 without mistaking its
low word for the whole signed result. Follow the blocked source’s partial output
and clipped final reduction interval.

Compare the two deterministic cache sources at the same return boundary. Record
workload, geometry, initial cache state and interval with every count. Use the
public measurement tool for cold, retained-cache warm and reset-replay evidence.
Treat a stopped GUI cache reset as a separately recorded experiment, rather than
mixing its counts with the default launch. Fewer modeled misses alone do not
establish lower host time or hardware cycles.

Write matvec independently under its own interface. Keep the original public
script, native function/caller checks and normal-exit check distinct. Explain a
row invariant, a zero-width reduction and a signed-width counterexample. On a
failure, identify the first incorrect sum, forbidden access or missing write
before changing the returned count. Measure your own correct implementation over
a stated interval; do not imitate the worked schedule’s exact counters to pass.
