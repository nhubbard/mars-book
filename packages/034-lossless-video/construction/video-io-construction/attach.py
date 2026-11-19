#!/usr/bin/env python3
"""Attach file loading without replacing the student's index or record functions."""
import argparse,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent
def attach(source):
 if source.count('\n.text\n')!=1 or len(re.findall(r'^main:',source,re.M))!=1 or source.count('\nvideo_index:\n')!=1 or source.count('\nvideo_apply_record:\n')!=1 or source.count('\nindex_file_data:')!=1:raise ValueError('Use one completed index caller and its input arena')
 if re.search(r'^(?:io_\w+|video_load|index_example_main):',source,re.M):raise ValueError('File-loader labels already exist')
 data,code=source.split('\n.text\n');code=re.sub(r'^main:','index_example_main:',code,count=1,flags=re.M)
 return data+'\n'+(ROOT/'data.asm').read_text()+'\n.text\n'+(ROOT/'caller.asm').read_text()+'\n'+(ROOT/'loader.asm').read_text()+'\n'+code
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--source',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();result=attach(a.source.read_text())
 with a.output.open('x')as out:out.write(result)
