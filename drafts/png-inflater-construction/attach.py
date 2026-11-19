#!/usr/bin/env python3
"""Keep the student's loader and collector while extending the temporary decoder."""
import argparse
from pathlib import Path
ROOT=Path(__file__).resolve().parent

def attach(source):
 old=(ROOT.parent/'png-collector-construction/wrapper.asm').read_text()
 if source.count(old)!=1 or source.count('\n.text\n')!=1:raise ValueError('Keep the previous supplied wrapper once; reconcile changed wrappers by hand')
 if any(token in source for token in ('png_inflate:', 'inflate_call:', '# BEGIN inflate_')):raise ValueError('Inflater scaffold already exists or labels conflict')
 return source.replace(old,(ROOT/'wrapper.asm').read_text(),1)+'\n'+(ROOT/'inflate.asm').read_text()

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--source',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();text=attach(a.source.read_text())
 with a.output.open('x')as out:out.write(text)
if __name__=='__main__':main()
