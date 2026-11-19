#!/usr/bin/env python3
"""Select collector observations without rewriting student instruction edits."""
import argparse,importlib.util,json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('png_loader_controls',ROOT.parent/'png-construction/prepare.py');loader=importlib.util.module_from_spec(spec);spec.loader.exec_module(loader)
def prepare(source,recipe):
 value=loader.unsigned(recipe['collectorCapacity']);result=loader.prepare(source,recipe);data,code=result.split('\n.text\n');signed=value if value<2**63 else value-2**64
 data,count=re.subn(r'^collector_capacity: \.dword -?[0-9]+$',lambda _:f'collector_capacity: .dword {signed}',data,flags=re.M)
 if count!=1:raise ValueError('Keep one native collector capacity control')
 return data+'\n.text\n'+code
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('recipe');p.add_argument('--source',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();recipes=json.loads((ROOT/'inputs.json').read_text())['recipes'];selected=[r for r in recipes if r['id']==a.recipe]
 if len(selected)!=1:p.error('Unknown collector recipe')
 result=prepare(a.source.read_text(),selected[0])
 with a.output.open('x')as out:out.write(result)
if __name__=='__main__':main()
