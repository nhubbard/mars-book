#!/usr/bin/env python3
"""Retain the student's reader/seeker while adding full-frame encoding regions."""
import argparse,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent
def attach(source):
 if source.count('\n.text\n')!=1 or len(re.findall(r'^main:',source,re.M))!=1 or any(source.count('\n'+n+':\n')!=1 for n in('video_seek','video_apply_record')):raise ValueError('Use one completed seek working file')
 if re.search(r'^(?:ef_\w+|encode_\w+|video_encode_full|seek_example_main):',source,re.M):raise ValueError('Encoder labels already exist')
 data,code=source.split('\n.text\n');code=re.sub(r'^main:','seek_example_main:',code,count=1,flags=re.M)
 return data+'\n'+(ROOT/'data.asm').read_text()+'\n.text\n'+(ROOT/'caller.asm').read_text()+'\n'+(ROOT/'encoder.asm').read_text()+'\n'+code
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--source',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();result=attach(a.source.read_text())
 with a.output.open('x')as out:out.write(result)
