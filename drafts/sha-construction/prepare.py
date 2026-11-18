#!/usr/bin/env python3
"""Prepare a separate SHA observation source while retaining the cumulative program."""
import argparse,json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent
TARGETS=['memory','file','schedule','compress','init','update','final']
def bytes_words(values):
 return ''.join('    .byte '+','.join(str(v if v<128 else v-256) for v in values[i:i+16])+'\n' for i in range(0,len(values),16))
def word_bytes(word):return [(word>>(8*i))&255 for i in range(4)]
def arenas(q,path=None):
 if q['target'] not in TARGETS or len(q['input'])>4096 or any(not isinstance(x,int) or not 0<=x<=255 for x in q['input']+q['contextTail']):raise ValueError('Use a declared target and at most 4096 input octets')
 if not 0<=q['contextTotal']<2**61 or len(q['contextTail'])!=q['contextTotal']%64:raise ValueError('Context tail must match the valid original byte count')
 if any(len(q[name])!=8 or any(not isinstance(x,int) or not 0<=x<=0xffffffff for x in q[name]) for name in ['initialState','contextState']):raise ValueError('Supply eight unsigned state words')
 if not 1<=q['capacity']<=256:raise ValueError('Capacity must be one through 256')
 context=sum([word_bytes(w) for w in q['contextState']],[])+list(q['contextTotal'].to_bytes(8,'little'))+word_bytes(len(q['contextTail']))+[165]*4+q['contextTail']+[165]*(64-len(q['contextTail']))+[165]*256+[165]*464
 pathname=str(path.resolve()).encode('ascii') if path is not None else b'fixture.bin'
 if len(pathname)>127 or b'\0' in pathname:raise ValueError('Use a terminated ASCII path of at most 127 bytes')
 return dict(data=q['input']+[165]*(4160-len(q['input'])),state=sum([word_bytes(w) for w in q['initialState']],[])+[165]*64,schedule=[165]*320,context=context,digest=[165]*96,path=list(pathname)+[0]+[165]*(127-len(pathname)),scratch=[165]*320)
def prepare(source,q,fixture_path=None):
 for name,values in arenas(q,fixture_path).items():
  start=name+':';end=name+'_end:'
  matches=list(re.finditer(r'^'+re.escape(start),source,re.M))
  if len(matches)!=1 or source.count(end)!=1:raise ValueError('Retain the complete supplied '+name+' arena')
  a=matches[0].end();b=source.index(end,a);source=source[:a]+'\n'+bytes_words(values)+source[b:]
 for name,default,value in [('length',3,len(q['input'])),('mode',2,TARGETS.index(q['target'])),('buffer_capacity',7,q['capacity'])]:
  old=f'{name}: .word {default}\n'
  if source.count(old)!=1:raise ValueError('Retain the default '+name+' control in the cumulative source')
  source=source.replace(old,f'{name}: .word {value}\n',1)
 return source

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('recipe');p.add_argument('--source',type=Path,default=ROOT/'main.asm');p.add_argument('--output',type=Path,required=True);p.add_argument('--fixture-output',type=Path);a=p.parse_args();found=[q for q in json.loads((ROOT/'inputs.json').read_text())['recipes'] if q['id']==a.recipe]
 if len(found)!=1:p.error('Choose an exact recipe ID')
 q=found[0];targets=[a.output]+([a.fixture_output] if a.fixture_output else [])
 if a.fixture_output and q['target']!='file':p.error('Host file fixtures apply only to file observations')
 if len({p.resolve() for p in targets})!=len(targets) or any(p.resolve()==a.source.resolve() or p.exists() for p in targets):p.error('Source and new output paths must be distinct; outputs must not already exist')
 try:text=prepare(a.source.read_text(),q,a.fixture_output)
 except (ValueError,UnicodeError) as e:p.error(str(e))
 created=[]
 try:
  with a.output.open('x') as stream:created.append(a.output);stream.write(text)
  if a.fixture_output:
   with a.fixture_output.open('xb') as stream:created.append(a.fixture_output);stream.write(bytes(q['input']))
 except BaseException:
  for path in created:path.unlink()
  raise
 print(a.output)
 if a.fixture_output:print(a.fixture_output)
 if q['target']=='file':print('Short reads, missing-file cases and injected errors are CLI fixture controls; ordinary host files only contain the input bytes.')
if __name__=='__main__':main()
