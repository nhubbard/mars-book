#!/usr/bin/env python3
"""Generate previous/current numeric frames, not an encoded delta stream."""
import argparse,json
from pathlib import Path
NAMES=('few','still','all-change','alternating','tail','tie','invalid-source-last','invalid-previous-last')
def words(name='few'):
 if name not in NAMES:raise ValueError('Unknown frame pair')
 previous=[((i*257)^0x123456)&0xffffff for i in range(4096)];current=list(previous)
 changes={'few':{0:1,325:0x102040,326:0x102040,4095:0x008000},'tail':{4095:0x112233},'still':{}}.get(name)
 if changes is None:changes={i:0x010203 for i in range(4096)if(name!='alternating'or i%2==0)and(name!='tie'or i not in(1364,1365,2730,2731))}
 for i,v in changes.items():current[i]^=v
 if name=='invalid-source-last':current[-1]|=0x1000000
 if name=='invalid-previous-last':previous[-1]|=0x80000000
 return previous,current

def declaration(label,data):return label+': .word -1515870811\n'+''.join('    .word '+', '.join(str(v if v<2**31 else v-2**32)for v in data[i:i+16])+'\n'for i in range(0,4096,16))+'    .byte -91:60\n'
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('fixture',choices=NAMES);p.add_argument('--output',type=Path,required=True);a=p.parse_args();previous,current=words(a.fixture)
 with a.output.open('x')as out:json.dump(dict(fixture=a.fixture,previous=previous,current=current),out,indent=2);out.write('\n')
