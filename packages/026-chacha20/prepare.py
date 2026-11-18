#!/usr/bin/env python3
"""Prepare an exact ChaCha observation in a new source, preserving authored regions."""
import argparse,json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent
TARGETS=['quarter','scalar','msa','stream']
def word_bytes(x):return list(x.to_bytes(4,'little'))
def arenas(q):
 if q['target'] not in TARGETS or type(q['length'])is not int or not 0<=q['length']<2**64:raise ValueError('Choose a declared target and unsigned64-bit count')
 if len(q['state'])!=16 or len(q['quad'])!=4 or any(type(x)is not int or not 0<=x<=0xffffffff for x in q['state']+q['quad']):raise ValueError('Supply sixteen state words and four quarter words')
 if len(q['input'])>4096 or any(type(x)is not int or not 0<=x<=255 for x in q['input']):raise ValueError('Supply at most4096 input octets')
 if type(q['inPlace'])is not bool:raise ValueError('Choose a Boolean in-place flag')
 quad=[165]*96
 for i,x in enumerate(q['quad']):quad[i*20:i*20+4]=word_bytes(x)
 return dict(source=q['input']+[165]*(4160-len(q['input'])),destination=[165]*4160,state=sum([word_bytes(x) for x in q['state']],[])+[165]*64,quad=quad)
def prepare(source,q):
 for name,values in arenas(q).items():
  matches=list(re.finditer('^'+re.escape(name)+':',source,re.M));end=name+'_end:'
  if len(matches)!=1 or source.count(end)!=1:raise ValueError('Retain the supplied '+name+' arena and boundary label')
  a=matches[0].end();b=source.index(end,a);text='\n'+''.join('    .byte '+','.join(str(x if x<128 else x-256) for x in values[i:i+16])+'\n' for i in range(0,len(values),16));source=source[:a]+text+source[b:]
 for name,kind,old,value in [('mode','word',0,TARGETS.index(q['target'])),('in_place','word',0,int(q['inPlace'])),('length','dword',65,q['length'] if q['length']<2**63 else q['length']-2**64)]:
  before=f'{name}: .{kind} {old}\n'
  if source.count(before)!=1:raise ValueError('Retain the default '+name+' control')
  source=source.replace(before,f'{name}: .{kind} {value}\n',1)
 return source

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('recipe');p.add_argument('--source',type=Path,default=ROOT/'starter/main.asm');p.add_argument('--output',type=Path,required=True);a=p.parse_args();found=[q for q in json.loads((ROOT/'inputs.json').read_text())['recipes'] if q['id']==a.recipe]
 if len(found)!=1:p.error('Choose an exact recipe ID')
 if a.output.exists() or a.output.resolve()==a.source.resolve():p.error('Use a distinct new output path; existing work is never overwritten')
 try:text=prepare(a.source.read_text(),found[0])
 except (ValueError,UnicodeError)as e:p.error(str(e))
 created=False
 try:
  with a.output.open('x')as stream:created=True;stream.write(text)
 except BaseException:
  if created:a.output.unlink()
  raise
 print(a.output)
if __name__=='__main__':main()
