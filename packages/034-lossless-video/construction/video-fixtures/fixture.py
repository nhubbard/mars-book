#!/usr/bin/env python3
"""Encode explicitly planned MLV1 teaching frames; no video decoder is supplied."""
import argparse,json,struct,zlib
from pathlib import Path
WIDTH=HEIGHT=64
PIXELS=WIDTH*HEIGHT
NAMES=('moving','still','all-change','extra-full')

def frame_record(kind,index,payload):
 head=struct.pack('>B3xII',kind,index,len(payload))
 return head+struct.pack('>I',zlib.crc32(head+payload))+payload

def commands(items):
 out=bytearray()
 for skip,changes in items:
  out.extend(struct.pack('>HH',skip,len(changes)))
  for red,green,blue in changes:out.extend((red,green,blue))
 return bytes(out)

def material(name='moving'):
 if name not in NAMES:raise ValueError('Unknown video fixture')
 base=bytes((37*x+53*y+97*c)&255 for y in range(HEIGHT)for x in range(WIDTH)for c in range(3));frames=[base];payloads=[(0,base)]
 def next_frame(items):
  raw=bytearray(frames[-1]);cursor=0
  for skip,changes in items:
   cursor+=skip
   for delta in changes:
    for c,v in enumerate(delta):raw[3*cursor+c]^=v
    cursor+=1
  assert cursor==PIXELS
  frames.append(bytes(raw));payloads.append((1,commands(items)))
 if name=='still':
  for _ in range(3):next_frame([(4096,[])])
 elif name=='all-change':next_frame([(0,[(1,0,0)]*4096)])
 else:
  next_frame([(0,[(1,0,0)]),(4095,[])])
  next_frame([(325,[(16,32,64)]*2),(62,[(16,32,64)]*2),(3705,[])])
  next_frame([(4096,[])])
  raw=bytearray(frames[-1])
  for y in range(64):raw[3*(64*y+32)]^=128
  frames.append(bytes(raw));payloads.append((0,bytes(raw)))
  next_frame([(3,[(0,0,255)]*3),(4090,[])])
  next_frame([(4095,[(0,128,0)])])
  next_frame([(4096,[])])
  if name=='extra-full':payloads[5]=(0,frames[5])
 head=struct.pack('>4sHHIHHII',b'MLV1',64,64,len(frames),4,0,40,0);file=bytearray(head);directory=[]
 for index,(kind,payload)in enumerate(payloads):
  record=frame_record(kind,index,payload);directory.append(dict(index=index,kind=kind,offset=len(file),payload_bytes=len(payload),record_bytes=len(record),crc32=f'{int.from_bytes(record[12:16],"big"):08x}'));file.extend(record)
 return dict(file=bytes(file),frames=frames,directory=directory,width=64,height=64,period=4,duration_ms=40)

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('fixture',choices=NAMES);p.add_argument('--output',type=Path,required=True);a=p.parse_args();m=material(a.fixture);a.output.mkdir()
 (a.output/'animation.mlv').write_bytes(m['file'])
 for n,rgb in enumerate(m['frames']):(a.output/f'frame-{n:03}.ppm').write_bytes(b'P6\n64 64\n255\n'+rgb)
 (a.output/'observations.json').write_text(json.dumps(dict(fixture=a.fixture,file_bytes=len(m['file']),frame_count=len(m['frames']),duration_ms=m['duration_ms'],directory=m['directory']),indent=2)+'\n')
if __name__=='__main__':main()
