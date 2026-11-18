# Shared state and temporary returns

The private helpers share s0=input base, s1=cursor, s2=length, s3=error status and s4=error offset. Base and length remain fixed. The public wrapper preserves the caller values before using those roles. Helpers return a value in v0 when s3 is zero; the wrapper later translates that protocol into public status/value-or-offset results.

Replace each named region's temporary return with its listed instructions. Do not append after its unconditional branch. A partial routine may stop early or return an unnegated operand; only its stated observation applies. Final free-writing checks require the complete function contract, not these private helper states.
