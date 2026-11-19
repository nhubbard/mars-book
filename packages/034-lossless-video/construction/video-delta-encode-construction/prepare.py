#!/usr/bin/env python3
"""Change frame-pair and context controls while retaining student instructions."""
import argparse,importlib.util,json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('delta_frames',ROOT/'fixtures.py');fixtures=importlib.util.module_from_spec(spec);spec.loader.exec_module(fixtures)
def prepare(source,recipe):
 if source.count('\n.text\n')!=1 or source.count('\nvideo_encode_record:\n')!=1:raise ValueError('Keep one record encoder and text section')
 values=[recipe.get(k,d)for k,d in(('sourceCount',16384),('capacity',12304),('sequence',1),('period',4),('previousCapacity',16384),('scratchCapacity',16384))]
 if any(type(v)is not int or not 0<=v<2**64 for v in values):raise ValueError('Use unsigned 64-bit controls')
 count,cap,seq,period,pcap,scap=[v if v<2**63 else v-2**64 for v in values];previous,current=fixtures.words(recipe.get('fixture','few'));data,code=source.split('\n.text\n')
 for label,words in(('delta_source',current),('delta_previous',previous)):
  data,n=re.subn(r'^'+label+r':.*?(?=^'+label+r'_end:)',lambda _:fixtures.declaration(label,words),data,flags=re.M|re.S)
  if n!=1:raise ValueError('Keep frame arena markers')
 data,n=re.subn(r'^delta_source_count: \.dword -?[0-9]+$',lambda _:f'delta_source_count: .dword {count}',data,flags=re.M)
 if n!=1:raise ValueError('Keep the source count')
 data,n=re.subn(r'^delta_context: \.dword [^\n]+$',lambda _:f'delta_context: .dword {cap}, {seq}, {period}, 0, {pcap}',data,flags=re.M)
 if n!=1:raise ValueError('Keep the record context')
 data,n=re.subn(r'^delta_scratch_fields: \.dword [^\n]+$',lambda _:f'delta_scratch_fields: .dword 0, {scap}',data,flags=re.M)
 if n!=1:raise ValueError('Keep scratch input fields')
 return data+'\n.text\n'+code
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('recipe');p.add_argument('--source',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();matches=[r for r in json.loads((ROOT/'inputs.json').read_text())['recipes']if r['id']==a.recipe]
 if len(matches)!=1:p.error('Unknown delta recipe')
 result=prepare(a.source.read_text(),matches[0])
 with a.output.open('x')as out:out.write(result)
