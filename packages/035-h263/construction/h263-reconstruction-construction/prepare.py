#!/usr/bin/env python3
"""Set ordinary block observations without replacing student instructions."""
import argparse
import re
from pathlib import Path
ROOT=Path(__file__).resolve().parent

def prepare(source,intra=0,mode=1,current=130,prediction=128,quant=8):
    values=(intra,mode,current,prediction,quant)
    if any(type(v)is not int for v in values)or intra not in(0,1)or mode not in(0,1)or not 0<=current<=255 or not 0<=prediction<=255 or not 1<=quant<=31:
        raise ValueError('Use binary selectors, byte samples and quantizer 1..31')
    if source.count('\n.text\n')!=1:raise ValueError('Keep one provided text-section boundary')
    data,code=source.split('\n.text\n')
    for label,value in [('block_current',current),('block_prediction',prediction)]:
        data,n=re.subn(r'^'+label+r': \.byte -?[0-9]{1,3}:64$',lambda _,label=label,value=value:f'{label}: .byte {value if value<128 else value-256}:64',data,flags=re.M)
        if n!=1:raise ValueError('Keep one typed constant block declaration')
    data,n=re.subn(r'^block_request: \.dword 0, 8, 0, (?:0|8), (?:[1-9]|[12][0-9]|3[01]), [01], [01], 0$',lambda _:f'block_request: .dword 0, 8, 0, {0 if intra else 8}, {quant}, {intra}, {mode}, 0',data,flags=re.M)
    if n!=1:raise ValueError('Keep the typed request declaration')
    return data+'\n.text\n'+code

if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--source',type=Path,default=ROOT/'main.asm')
    for name,value in [('intra',0),('mode',1),('current',130),('prediction',128),('quant',8)]:p.add_argument('--'+name,type=int,default=value)
    p.add_argument('--output',type=Path,required=True);a=p.parse_args()
    result=prepare(a.source.read_text(),a.intra,a.mode,a.current,a.prediction,a.quant)
    with a.output.open('x')as out:out.write(result)
