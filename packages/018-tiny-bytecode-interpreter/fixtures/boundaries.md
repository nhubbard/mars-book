# Instruction boundaries in the default image

Decimal bytes: 1,0,1,3,32,49,16,34,35,16,34,1,1,17,48,4,33,0.
Starts: 0,2,4,5,7,8,9,10,11,13,14,16,17. Payload offsets: 1,3,6,12,15.
The JZ at five names sixteen; changing its payload at six to six makes a range-valid but boundary-invalid target. Validation reports the branch start, five. A target of 64 must fail the range check before a variable shift could alias bit zero.
