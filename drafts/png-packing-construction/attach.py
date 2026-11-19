#!/usr/bin/env python3
"""Attach unfinished pixel packing and result publication to student row-stage work."""
import argparse
from pathlib import Path
ROOT=Path(__file__).resolve().parent

def attach(source):
 old=(ROOT.parent/'png-row-construction/wrapper.asm').read_text()
 if source.count(old)!=1 or source.count('\n.text\n')!=1:raise ValueError('Keep the supplied row wrapper once; reconcile altered wrappers by hand')
 if source.count('\npng_unfilter:\n')!=1 or source.count('\npng_unfilter_msa:\n')!=1:raise ValueError('Attach after row reconstruction')
 if any(s in source for s in ('png_blit:', 'rgb_shuffle:', '# BEGIN pack_', 'publication_entry:')):raise ValueError('Packing source or conflicting labels already exist')
 source=source.replace('\n.text\n','\n'+(ROOT/'masks.asm').read_text()+'\n.text\n',1)
 return source.replace(old,(ROOT/'wrapper.asm').read_text(),1)+'\n'+(ROOT/'packing.asm').read_text()

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--source',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();result=attach(a.source.read_text())
 with a.output.open('x')as out:out.write(result)
if __name__=='__main__':main()
