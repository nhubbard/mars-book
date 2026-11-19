#!/usr/bin/env python3
"""Reduce only unused provided example arenas; preserve all instruction text."""
import argparse,re
from pathlib import Path
CAPACITY=262144

def compact(source):
 if source.count('\n.text\n')!=1:raise ValueError('Keep one text section')
 data,code=source.split('\n.text\n')
 def shrink(pattern,amount):
  nonlocal data
  matches=list(re.finditer(pattern,data,re.M|re.S))
  if len(matches)!=1:raise ValueError('Keep the supplied arena markers')
  m=matches[0];remaining=int(m[2])-amount
  if remaining<0:raise ValueError('This input does not fit the compact example profile')
  data=data[:m.start()]+m[1]+str(remaining)+m[3]+data[m.end():]
 shrink(r'(^index_file_data:.*?\n    \.space )(\d+)(\n    \.byte -91:61\n)',7344152-CAPACITY)
 shrink(r'(^file_encode_frames:.*?\n    \.space )(\d+)(\n    \.byte -91:60\n)',4194304-CAPACITY)
 for pattern,replacement in[(r'^(file_encode_context: \.dword )7344152,',r'\g<1>262144,'),(r'^io_capacity: \.dword 7344152$','io_capacity: .dword 262144')]:
  data,n=re.subn(pattern,replacement,data,flags=re.M)
  if n!=1:raise ValueError('Keep the supplied maximum capacity controls before compaction')
 return data+'\n.text\n'+code
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--source',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();result=compact(a.source.read_text())
 with a.output.open('x')as out:out.write(result)
