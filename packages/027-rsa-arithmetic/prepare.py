#!/usr/bin/env python3
"""Prepare an exact RSA observation in a new file, preserving all authored code."""
import argparse,json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent
TARGETS=['gcd64','pow32','mul128','addmod128','reduce128','mulmod128','pow128']
def arenas(q):
 if q['target']not in TARGETS:raise ValueError('Choose a declared arithmetic entry')
 result={}
 for name in ['left','right','modulus','exponent']:
  value=q[name]
  if not isinstance(value,str)or not re.fullmatch(r'0|[1-9][0-9]*',value)or not 0<=int(value)<2**128:raise ValueError('Supply canonical unsigned128-bit decimal values')
  result[name]=list(int(value).to_bytes(16,'little'))+[165]*112
 if q['target']=='gcd64'and any(int(q[n])>=2**64 for n in ['left','right']):raise ValueError('Gcd inputs must fit unsigned64 bits')
 if q['target']=='pow32'and any(int(q[n])>=2**32 for n in ['left','right','modulus']):raise ValueError('Scalar inputs must fit unsigned32 bits')
 if q['target']=='addmod128'and int(q['modulus'])and any(int(q[n])>=int(q['modulus'])for n in ['left','right']):raise ValueError('Modular addition requires residue operands')
 result['output']=[165]*256
 return result
def prepare(source,q):
 for name,values in arenas(q).items():
  pattern=re.compile(r'^'+name+r':.*?(?=^'+name+r'_end:)',re.M|re.S)
  # Each owned arena has an explicit boundary; only bytes between those labels change.
  replacement=name+':\n'+''.join('    .byte '+','.join(str(v if v<128 else v-256)for v in values[i:i+16])+'\n'for i in range(0,len(values),16))
  source,count=pattern.subn(lambda _:replacement,source)
  if count!=1:raise ValueError('Retain the unique '+name+' arena and its boundary')
 before='mode: .word 0\n'
 if source.count(before)!=1:raise ValueError('Retain the default mode control')
 return source.replace(before,'mode: .word '+str(TARGETS.index(q['target']))+'\n',1)
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('recipe');p.add_argument('--source',type=Path,default=ROOT/'starter/main.asm');p.add_argument('--output',type=Path,required=True);a=p.parse_args();found=[q for q in json.loads((ROOT/'inputs.json').read_text())['recipes']if q['id']==a.recipe]
 if len(found)!=1:p.error('Choose an exact recipe ID')
 if a.output.exists()or a.output.resolve()==a.source.resolve():p.error('Use a distinct new output path; existing work is never overwritten')
 try:text=prepare(a.source.read_text(),found[0])
 except(ValueError,UnicodeError)as e:p.error(str(e))
 created=False
 try:
  with a.output.open('x')as f:created=True;f.write(text)
 except BaseException:
  if created:a.output.unlink()
  raise
 print(a.output)
if __name__=='__main__':main()
