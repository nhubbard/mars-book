#!/usr/bin/env python3
"""Carry a student's complete picture group into the clip working source."""
import argparse
from pathlib import Path
ROOT=Path(__file__).resolve().parent
MARKER='# Replace the picture dependency group with your own completed entry and data.'
def compose(source,picture):
    if picture.startswith('.text\n'):picture='\n'+picture
    if source.count('\n.text\n')!=1 or source.count(MARKER)!=1 or picture.count('\n.text\n')!=1 or picture.count('\nh263_encode_picture:\n')!=1:raise ValueError('Use one text boundary and retain the clip dependency marker')
    data,code=source.split('\n.text\n');pd,pc=picture.split('\n.text\n')
    if code.count('\nh263_encode_clip:\n')!=1:raise ValueError('Keep one clip entry')
    return data+'\n'+pd+'\n.text\n'+code.split(MARKER,1)[0]+'\nh263_encode_picture:\n'+pc.split('h263_encode_picture:\n',1)[1]
if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--source',type=Path,default=ROOT/'main.asm');p.add_argument('--picture',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();text=compose(a.source.read_text(),a.picture.read_text())
    with a.output.open('x')as out:out.write(text)
