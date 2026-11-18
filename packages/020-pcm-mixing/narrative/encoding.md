# Encoding

The low byte plus 256 times the high byte forms an unsigned halfword. If bit fifteen is set, its signed value is that pattern minus 65536. Bytes 18 fc are 64536 as unsigned and -1000 as signed. Byte loads avoid an alignment requirement; seh supplies the signed interpretation before addition.
