#!/usr/bin/env python3
"""Generate explicit native color inputs, without implementing a video encoder."""
import argparse,json
from pathlib import Path
NAMES=('gradient','zero','white','invalid-first','invalid-last')
def words(name='gradient'):
 if name not in NAMES:raise ValueError('Unknown source frame')
 data=[((i*257)^0x123456)&0xffffff for i in range(4096)]
 if name=='zero':data=[0]*4096
 if name=='white':data=[0xffffff]*4096
 if name=='invalid-first':data[0]|=0x1000000
 if name=='invalid-last':data[-1]|=0x80000000
 return data

def declaration(name='gradient'):
 data=words(name);return 'encode_source: .word -1515870811\n'+''.join('    .word '+', '.join(str(v if v<2**31 else v-2**32)for v in data[i:i+16])+'\n'for i in range(0,4096,16))+'    .byte -91:60\n'
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('fixture',choices=NAMES);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
 with a.output.open('x')as out:out.write(json.dumps(dict(fixture=a.fixture,numericWords=words(a.fixture)),indent=2)+'\n')
