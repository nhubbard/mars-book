#!/usr/bin/env python3
"""Install coefficient observations while preserving tables and student instructions."""
import argparse
import re
from pathlib import Path
ROOT=Path(__file__).resolve().parent

def levels(intra,pattern):
    if type(intra)is not int or intra not in(0,1)or pattern not in('worked','zero','tail','dense'):raise ValueError('Choose binary INTRA and a named pattern')
    result=[0]*64
    if pattern=='dense':result=[1]*64
    if pattern=='worked':result[1]=1;result[8]=-1
    if pattern=='tail':result[63]=-127
    result[0]=255 if intra else 1 if pattern in('worked','dense')else 0
    return result

def prepare(source,intra=1,pattern='worked',capacity=192,used=3):
    values=levels(intra,pattern)
    if type(capacity)is not int or not 0<=capacity<=192 or type(used)is not int or not 0<=used<=capacity*8:raise ValueError('Use capacity 0..192 and used count within its bits')
    if source.count('\n.text\n')!=1:raise ValueError('Keep one text-section boundary')
    data,code=source.split('\n.text\n')
    replacement='coeff_input:\n'+'\n'.join('    .half '+','.join(map(str,values[i:i+8]))for i in range(0,64,8))+'\ncoeff_input_end:'
    data,n=re.subn(r'^coeff_input:.*?^coeff_input_end:',lambda _:replacement,data,flags=re.M|re.S)
    if n!=1:raise ValueError('Keep unique input boundaries')
    for label,before,after in [('context',r'coeff_context: \.dword 0, [0-9]+, [0-9]+',f'coeff_context: .dword 0, {capacity}, {used}'),('request',r'coeff_request: \.dword 0, 0, [01], 0',f'coeff_request: .dword 0, 0, {intra}, 0')]:
        data,n=re.subn('^'+before+'$',lambda _,after=after:after,data,flags=re.M)
        if n!=1:raise ValueError('Keep typed '+label+' declaration')
    return data+'\n.text\n'+code

if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--source',type=Path,default=ROOT/'main.asm');p.add_argument('--intra',type=int,choices=(0,1),default=1);p.add_argument('--pattern',choices=('worked','zero','tail','dense'),default='worked');p.add_argument('--capacity',type=int,default=192);p.add_argument('--used',type=int,default=3);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
    result=prepare(a.source.read_text(),a.intra,a.pattern,a.capacity,a.used)
    with a.output.open('x')as out:out.write(result)
