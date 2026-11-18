#!/usr/bin/env python3
"""Prepare PPM observation data without changing student-authored instructions."""
import argparse,json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent
TARGETS=['parse','decode','encode','load','save','pipeline']
def integer(value,bits=64):
 if not isinstance(value,str)or not re.fullmatch(r'0|[1-9][0-9]*',value)or int(value)>=1<<bits:raise ValueError('Use a canonical unsigned decimal string')
 return int(value)
def octets(value,limit):
 if not isinstance(value,str)or not re.fullmatch(r'(?:[0-9a-f]{2})*',value):raise ValueError('Use lowercase hexadecimal octets')
 result=bytes.fromhex(value)
 if len(result)>limit:raise ValueError('Fixture exceeds its declared arena')
 return result
def values(q):
 if q['target']not in TARGETS:raise ValueError('Choose a declared target')
 file=octets(q['fileHex'],12544);pixels=octets(q['pixelHex'],16384);n=integer(q['count']);cap=integer(q['capacity']);w=integer(q['width'],32);h=integer(q['height'],32)
 if len(pixels)%4:raise ValueError('Pixel fixture must contain complete words')
 if q['target']=='encode'and 1<=w<=64 and 1<=h<=64 and len(pixels)!=4*w*h:raise ValueError('Provide exactly one word per encoder pixel')
 octets(q['inputFileHex'],12545);octets(q['outputFileHex'],12544)
 for name in ['inputExists','outputWritable']:
  if type(q[name])is not bool:raise ValueError('File permissions must be Boolean')
 for name in ['readChunk','writeChunk']:
  if type(q[name])is not int or q[name]<=0:raise ValueError('File chunks must be positive integers')
 for name in ['failReadAfter','failWriteAfter']:
  if q[name]is not None and(type(q[name])is not int or q[name]<0):raise ValueError('Failure positions must be nonnegative integers')
 if type(q['firstDescriptor'])is not int or not 3<=q['firstDescriptor']<=31:raise ValueError('Descriptor must be between three and thirty-one')
 return file,pixels,n,cap,(h<<32)|w
def prepare(source,q):
 file,pixels,n,cap,dims=values(q)
 for name,size,data in [('source',12576,file),('framebuffer',16448,pixels)]:
  body=data+bytes([165])*(size-len(data));replacement=name+':\n'+''.join('    .byte '+','.join(str(v if v<128 else v-256)for v in body[i:i+16])+'\n'for i in range(0,len(body),16))
  source,matches=re.subn(r'^'+name+r':.*?(?=^'+name+'_end:)',lambda _:replacement,source,flags=re.M|re.S)
  if matches!=1:raise ValueError('Retain the unique '+name+' arena and end label')
 controls=[('mode','word',0,TARGETS.index(q['target'])),('byte_count','dword',23,n),('pixel_capacity','dword',4096,cap if q['target']=='decode'else 4096),('dimensions','dword',8589934594,dims),('byte_capacity','dword',12544,cap if q['target']in ['encode','load','pipeline']else 12544)]
 for name,kind,old,new in controls:
  token=f'{name}: .{kind} {old}\n'
  if source.count(token)!=1:raise ValueError('Retain the default '+name+' control')
  source=source.replace(token,f'{name}: .{kind} {new if new<2**63 else new-2**64}\n',1)
 return source
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('recipe');p.add_argument('--source',type=Path,default=ROOT/'main.asm');p.add_argument('--output',type=Path,required=True);a=p.parse_args();rows=[q for q in json.loads((ROOT/'inputs.json').read_text())['recipes']if q['id']==a.recipe]
 if len(rows)!=1:p.error('Choose an exact recipe ID')
 if a.output.exists()or a.source.resolve()==a.output.resolve():p.error('Choose a distinct new output; existing work is never overwritten')
 try:result=prepare(a.source.read_text(),rows[0])
 except(ValueError,UnicodeError)as error:p.error(str(error))
 with a.output.open('x')as stream:stream.write(result)
 print(a.output)
if __name__=='__main__':main()
