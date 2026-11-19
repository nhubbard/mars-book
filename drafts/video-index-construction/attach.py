#!/usr/bin/env python3
"""Attach the next caller and index scaffold without replacing the student's record routine."""
import argparse,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent

def attach(source):
 if source.count('\n.text\n')!=1 or len(re.findall(r'^main:',source,re.M))!=1 or source.count('\nvideo_apply_record:\n')!=1:raise ValueError('Use one record caller, text section and record entry')
 if re.search(r'^(?:index_\w+|video_index|record_example_main):',source,re.M):raise ValueError('Index scaffold or reserved labels already present')
 data,code=source.split('\n.text\n');code=re.sub(r'^main:', 'record_example_main:',code,count=1,flags=re.M)
 return data+'\n'+(ROOT/'data.asm').read_text()+'\n.text\n'+(ROOT/'caller.asm').read_text()+'\n'+(ROOT/'index.asm').read_text()+'\n'+code

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--source',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();result=attach(a.source.read_text())
 with a.output.open('x')as out:out.write(result)
if __name__=='__main__':main()
