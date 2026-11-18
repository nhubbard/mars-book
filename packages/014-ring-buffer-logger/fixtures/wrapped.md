# Physical slots after operation three

The eight ring bytes, in hexadecimal physical order, are `7f 80 ff 43 02 78 79 03`.
Head is four, tail three, and used seven. Prefix `02` at four identifies the older
payload `78 79`. Prefix `03` at seven identifies the newer payload `7f 80 ff`
across indices zero through two. The `43` at free index three is stale. The native
chapter has a labelled diagram; it is not an IDE screenshot.
