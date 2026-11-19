#!/usr/bin/env python3
"""Encode known RGB row observations; no student reconstruction algorithm is supplied."""
import argparse,importlib.util,struct,zlib
from pathlib import Path
ROOT=Path(__file__).resolve().parent
s=importlib.util.spec_from_file_location('png_format',ROOT.parent/'png-fixtures/fixture.py');format=importlib.util.module_from_spec(s);s.loader.exec_module(format)
NAMES=('worked','bad-selector','up1','up5','up6','up17')
def material(name):
 if name not in NAMES:raise ValueError('Unknown row fixture')
 if name in ('worked','bad-selector'):
  width,height=2,5;raw=bytearray(format.TRACE_FILTERED);rgb=format.TRACE_RGB
  if name=='bad-selector':raw[28]=5
 else:
  width=int(name[2:]);height=2;raw=bytearray([2]+[250]*(3*width)+[2]+[10]*(3*width));rgb=bytes([250]*(3*width)+[4]*(3*width))
 raw=bytes(raw);stream=zlib.compress(raw,0);header=struct.pack('>IIBBBBB',width,height,8,2,0,0,0)
 png=format.SIGNATURE+format.chunk(b'IHDR',header)+format.chunk(b'IDAT',stream[:1])+format.chunk(b'IDAT',stream[1:])+format.chunk(b'IEND',b'')
 return dict(png=png,filtered=raw,rgb=rgb,width=width,height=height)
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('fixture',choices=NAMES);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
 with a.output.open('xb')as out:out.write(material(a.fixture)['png'])
if __name__=='__main__':main()
