#!/usr/bin/env python3
"""Keep completed stored code while attaching unfinished Huffman handlers and helpers."""
import argparse
from pathlib import Path
ROOT=Path(__file__).resolve().parent
ANCHOR='fixed_block:\ndynamic_block:\ninflate_missing:\n'
def attach(source):
 if source.count(ANCHOR)!=1 or source.count('\n.text\n')!=1:raise ValueError('Keep the supplied Huffman development labels once; reconcile altered handlers by hand')
 if any(x in source for x in ('build_tree:', 'decode_symbol:', 'alphabet_order:', '# BEGIN huff_')):raise ValueError('Huffman source or conflicting labels already exist')
 source=source.replace('\n.text\n',(ROOT/'tables.asm').read_text()+'\n.text\n',1)
 return source.replace(ANCHOR,(ROOT/'handlers.asm').read_text()+'inflate_missing:\n',1)+'\n'+(ROOT/'helpers.asm').read_text()
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--source',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();result=attach(a.source.read_text())
 with a.output.open('x')as out:out.write(result)
if __name__=='__main__':main()
