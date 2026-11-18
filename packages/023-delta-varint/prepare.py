#!/usr/bin/env python3
"""Prepare exact codec observation inputs without overwriting cumulative student source."""
import argparse
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parent

def directives(kind,values):
    return ''.join('    .'+kind+' '+','.join(map(str,values[i:i+8]))+'\n' for i in range(0,len(values),8))

def prepare(source,recipe):
    decode=recipe['decode'];values=recipe['input'];capacity=recipe['capacity']
    if not isinstance(decode,bool) or not isinstance(capacity,int) or not 0<=capacity<=(32 if decode else 160):
        raise ValueError('Use the declared direction and capacity domain')
    if len(values)>(160 if decode else 32) or not all(isinstance(v,int) and (0<=v<=255 if decode else -2147483648<=v<=2147483647) for v in values):
        raise ValueError('Use signed word readings or unsigned input octets in the declared extent')
    words=[0x6a6b6c6d]*40;stream=[165]*192
    if decode:stream[:len(values)]=values
    else:words[:len(values)]=values
    for label,kind,data in [('words','word',words),('stream','byte',[v if v<128 else v-256 for v in stream])]:
        begin,end=label+':',label+'_end:'
        if source.count(begin)!=1 or source.count(end)!=1:raise ValueError('Retain the complete supplied '+label+' arena')
        a=source.index(begin)+len(begin);b=source.index(end,a);source=source[:a]+'\n'+directives(kind,data)+source[b:]
    changes={'word_count':5,'byte_capacity':160,'byte_count':8,'word_capacity':32,'method':0}
    wanted=dict(changes);wanted['method']=1 if decode else 0;wanted['byte_count' if decode else 'word_count']=len(values);wanted['word_capacity' if decode else 'byte_capacity']=capacity
    for name,old in changes.items():
        line=name+': .word '+str(old)+'\n'
        if source.count(line)!=1:raise ValueError('Prepare from cumulative source retaining default '+name)
        source=source.replace(line,name+': .word '+str(wanted[name])+'\n',1)
    return source

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('recipe');parser.add_argument('--source',type=Path,default=ROOT/'starter/main.asm');parser.add_argument('--output',type=Path,required=True);args=parser.parse_args()
    selected=[q for q in json.loads((ROOT/'inputs.json').read_text())['recipes'] if q['id']==args.recipe]
    if len(selected)!=1:parser.error('Choose an exact recipe ID from inputs.json')
    if args.output.resolve()==args.source.resolve():parser.error('Write a separate observation file')
    text=prepare(args.source.read_text(),selected[0])
    with args.output.open('x') as output:output.write(text)
    print(args.output)
if __name__=='__main__':main()
