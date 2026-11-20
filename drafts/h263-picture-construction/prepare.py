#!/usr/bin/env python3
"""Change constant-plane observations and picture controls while preserving instructions."""
import argparse
import re
from pathlib import Path
ROOT=Path(__file__).resolve().parent

def prepare(source,picture=0,current=128,reference=128,quant=8,mode=1,tr=0,gfid=0):
    if any(type(v)is not int for v in(picture,current,reference,quant,mode,tr,gfid))or picture not in(0,1)or mode not in(0,1)or not 0<=current<=255 or not 0<=reference<=255 or not 1<=quant<=31 or not 0<=tr<=255 or not 0<=gfid<=3:raise ValueError('Choose admitted sample bytes and picture controls')
    if source.count('\n.text\n')!=1:raise ValueError('Keep one text boundary')
    data,code=source.split('\n.text\n')
    for name,value in [('current',current),('reference',reference)]:
        data,n=re.subn(r'^picture_'+name+r': \.byte -?[0-9]+:38016$',f'picture_{name}: .byte {value if value<128 else value-256}:38016',data,flags=re.M)
        if n!=1:raise ValueError('Keep the supplied constant-plane declarations')
    data,n=re.subn(r'^picture_request: \.dword 0,0,0,0,0,169088,[01],[0-9]+,[0-9]+,[01],[0-3],0$',f'picture_request: .dword 0,0,0,0,0,169088,{picture},{tr},{quant},{mode},{gfid},0',data,flags=re.M)
    if n!=1:raise ValueError('Keep the typed picture request')
    return data+'\n.text\n'+code
if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--source',type=Path,default=ROOT/'main.asm')
    for name,value in [('picture',0),('current',128),('reference',128),('quant',8),('mode',1),('tr',0),('gfid',0)]:p.add_argument('--'+name,type=int,default=value)
    p.add_argument('--output',type=Path,required=True);a=p.parse_args();text=prepare(a.source.read_text(),a.picture,a.current,a.reference,a.quant,a.mode,a.tr,a.gfid)
    with a.output.open('x')as out:out.write(text)
