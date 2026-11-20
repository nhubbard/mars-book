#!/usr/bin/env python3
"""Select final caller observations without rewriting student instructions."""
import argparse,importlib.util,json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent
s=importlib.util.spec_from_file_location('collector_controls',ROOT.parent/'png-collector-construction/prepare.py');previous=importlib.util.module_from_spec(s);s.loader.exec_module(previous)
def prepare(source,recipe):
 if source.count('\npng_blit:\n')!=1 or source.count('\npublication_entry:\n')!=1:raise ValueError('Use the attached packing source')
 result=previous.prepare(source,recipe)
 if 'frameStride' in recipe:
  stride=previous.loader.unsigned(recipe['frameStride'])
  if not 1<=stride<=512:raise ValueError('Frame stride must be between one and 512 pixels')
  # This provided constant allocates no storage and leaves student instructions intact.
  data,code=result.split('\n.text\n')
  data,n=re.subn(r'^\.eqv FRAME_STRIDE [0-9]+$',lambda _:'.eqv FRAME_STRIDE '+str(stride),data,flags=re.M)
  if n!=1:raise ValueError('Keep the unique provided FRAME_STRIDE constant')
  result=data+'\n.text\n'+code
 return result
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('recipe');p.add_argument('--source',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();found=[r for r in json.loads((ROOT/'inputs.json').read_text())['recipes']if r['id']==a.recipe]
 if len(found)!=1:p.error('Unknown packing recipe')
 result=prepare(a.source.read_text(),found[0])
 with a.output.open('x')as out:out.write(result)
if __name__=='__main__':main()
