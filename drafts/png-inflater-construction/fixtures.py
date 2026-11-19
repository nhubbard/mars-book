#!/usr/bin/env python3
"""Create stored-stage PNG and raw zlib observations; never supply a decoder."""
import argparse,importlib.util,struct,zlib
from pathlib import Path
ROOT=Path(__file__).resolve().parent
NAMES=('worked','bad-header','bad-length','bad-adler','extra-body','two-blocks','fixed','dynamic')
def material(name):
 spec=importlib.util.spec_from_file_location('inflater_inputs',ROOT.parent/'png-fixtures/fixture.py');f=importlib.util.module_from_spec(spec);spec.loader.exec_module(f)
 if name=='dynamic':m=f.material(512,5,'auto');stream=m['zlib'];assert(stream[2]>>1)&3==2,'Host encoder did not emit the requested dynamic observation';return dict(png=m['png'],zlib=stream,plain=m['filtered'])
 m=f.material(compression='fixed'if name=='fixed'else'stored');stream=m['zlib'];plain=m['filtered']
 if name=='bad-header':stream=bytes((stream[0],stream[1]^1))+stream[2:]
 elif name=='bad-length':stream=stream[:5]+bytes((stream[5]^1,))+stream[6:]
 elif name=='bad-adler':stream=stream[:-1]+bytes((stream[-1]^1,))
 elif name=='extra-body':stream=stream[:-4]+b'X'+stream[-4:]
 elif name=='two-blocks':
  first=plain[:7];second=plain[7:]
  def block(raw,final):return bytes((final,))+struct.pack('<HH',len(raw),65535-len(raw))+raw
  stream=stream[:2]+block(first,0)+block(second,1)+stream[-4:]
 elif name not in('worked','fixed'):raise ValueError('Unknown stored-stage fixture')
 def chunk(kind,payload):return struct.pack('>I',len(payload))+kind+payload+struct.pack('>I',zlib.crc32(kind+payload))
 png=m['png'][:33]+chunk(b'IDAT',stream[:1])+chunk(b'IDAT',stream[1:])+chunk(b'IEND',b'')
 return dict(png=png,zlib=stream,plain=plain)
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('name',choices=NAMES);p.add_argument('output',type=Path);p.add_argument('--kind',choices=('png','zlib'),default='png');a=p.parse_args();data=material(a.name)[a.kind]
 with a.output.open('xb')as out:out.write(data)
if __name__=='__main__':main()
