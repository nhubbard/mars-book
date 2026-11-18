# Test logical intervals and call lifetime

Mapped padding does not extend a selected input or output slice. Restoring an input after writing it still violates ownership. A partial-sum read needs initialization during the current call. Repeated calls with different shapes expose stale state.

The full directly edited composition is in `native/ownership.tex`.
