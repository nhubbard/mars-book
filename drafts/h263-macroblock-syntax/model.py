"""Independent restricted-baseline macroblock syntax, separate from picture ownership."""
import importlib.util
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parent
TABLE=json.loads((ROOT/'codes.json').read_text())
spec=importlib.util.spec_from_file_location('macro_coefficients',ROOT.parent/'h263-coefficient-construction/model.py');COEFF=importlib.util.module_from_spec(spec);spec.loader.exec_module(COEFF)

def wrap(value):return (value+32)%64-32

def validate(blocks,picture,intra,motion,predictor,allow_skip):
    if any(type(v)is not bool for v in(picture,intra,allow_skip))or not(picture or intra):raise ValueError('I pictures require INTRA macroblocks')
    if len(blocks)!=6 or len(motion)!=2 or len(predictor)!=2 or any(type(v)is not int or not -32<=v<=31 for v in motion+predictor):raise ValueError('Use six blocks and signed half-sample vector components')
    if intra and any(motion+predictor):raise ValueError('INTRA vector fields must be zero')
    for block in blocks:COEFF.events(block,intra)

def presence(blocks,intra):
    return sum((1<<(5-i))for i,b in enumerate(blocks)if any(b[1 if intra else 0:]))

def encode(blocks,picture=False,intra=True,motion=(0,0),predictor=(0,0),allow_skip=True,compact=True,choose_skip=True):
    validate(blocks,picture,intra,motion,predictor,allow_skip);mask=presence(blocks,intra)
    if picture and not intra and allow_skip and choose_skip and mask==0 and motion==(0,0):return '1'
    bits='0'if picture else ''
    group=0 if not picture else 2 if intra else 1
    def code(table,index):
        value,width=table[index];return format(value,f'0{width}b')
    bits+=code(TABLE['mcbpc'],4*group+(mask&3))
    bits+=code(TABLE['cbpyIntra'],mask>>2 if intra else 15-(mask>>2))
    if not intra:
        for value,pred in zip(motion,predictor):bits+=code(TABLE['mvd'],wrap(value-pred)+32)
    for i,block in enumerate(blocks):
        if intra or mask&(1<<(5-i)):bits+=COEFF.encode(block,intra,compact)
    return bits

def decode_prefix(bits,picture=False,predictor=(0,0)):
    if type(picture)is not bool or len(predictor)!=2 or any(type(v)is not int or not -32<=v<=31 for v in predictor)or any(c not in '01'for c in bits):raise ValueError('Invalid bit sequence/predictor')
    p=0
    def take(n):
        nonlocal p
        if p+n>len(bits):raise ValueError('Truncated macroblock')
        value=int(bits[p:p+n],2);p+=n;return value
    def vlc(entries):
        prefix=0
        for width in range(1,14):
            prefix=(prefix<<1)|take(1)
            for index,(code,length)in enumerate(entries):
                if length==width and code==prefix:return index
        raise ValueError('Unsupported macroblock VLC')
    if picture and take(1):return dict(intra=False,skip=True,mask=0,motion=(0,0),blocks=[[0]*64 for _ in range(6)]),p
    if picture:
        index=vlc(TABLE['mcbpc'][4:]);intra=index>=4;cbpc=index%4
    else:intra=True;cbpc=vlc(TABLE['mcbpc'][:4])
    cbpy=vlc(TABLE['cbpyIntra']);cbpy=cbpy if intra else 15-cbpy;mask=(cbpy<<2)|cbpc
    motion=(0,0)if intra else tuple(wrap(pred+vlc(TABLE['mvd'])-32)for pred in predictor)
    blocks=[]
    for i in range(6):
        active=bool(mask&(1<<(5-i)))
        if intra or active:
            block,n=COEFF.decode_prefix(bits[p:],intra,active);p+=n
            if bool(any(block[1 if intra else 0:]))!=active:raise ValueError('Presence flag does not match payload')
        else:block=[0]*64
        blocks.append(block)
    return dict(intra=intra,skip=False,mask=mask,motion=motion,blocks=blocks),p

def decode(bits,picture=False,predictor=(0,0)):
    result,n=decode_prefix(bits,picture,predictor)
    if n!=len(bits):raise ValueError('Trailing macroblock bits')
    return result
