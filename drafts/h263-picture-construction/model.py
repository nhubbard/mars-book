"""Inspect all syntax in this chapter's explicit-row-GOB, whole-pixel QCIF profile."""
import importlib.util
from pathlib import Path
ROOT=Path(__file__).resolve().parent
s=importlib.util.spec_from_file_location('picture_macro_syntax',ROOT.parent/'h263-macroblock-syntax/model.py');MACRO=importlib.util.module_from_spec(s);s.loader.exec_module(MACRO)
FRAME_BYTES=38016
WORKSPACE_BYTES=169088
MAX_PICTURE_BYTES=(99*8487+50+8*(29+7)+7+7)//8

def block_origin(row,col,block):
    if any(type(x)is not int for x in(row,col,block))or not 0<=row<9 or not 0<=col<11 or not 0<=block<6:raise ValueError('Use an admitted QCIF macroblock/block position')
    if block<4:return (row*16+(block//2)*8)*176+col*16+(block%2)*8,176
    return 25344+(block-4)*6336+(row*8)*88+col*8,88

def header(picture,tr,quant,gfid):
    if any(type(v)is not int for v in(picture,tr,quant,gfid))or picture not in(0,1)or not 0<=tr<=255 or not 1<=quant<=31 or not 0<=gfid<=3:raise ValueError('Invalid picture controls')
    return format(32,'022b')+format(tr,'08b')+format(4160+(picture<<4),'013b')+format(quant,'05b')+'00'

def inspect_stream(data):
    if not isinstance(data,bytes)or not data:raise ValueError('Provide a nonempty elementary stream')
    bits=''.join(format(b,'08b')for b in data);p=0;result=[]
    def take(n):
        nonlocal p
        if p+n>len(bits):raise ValueError('Truncated picture/GOB header')
        v=int(bits[p:p+n],2)if n else 0;p+=n;return v
    def align():
        if take((-p)%8):raise ValueError('Nonzero alignment stuffing')
    while p<len(bits):
        start=p//8
        if take(22)!=32:raise ValueError('Invalid picture start code')
        tr=take(8);ptype=take(13)
        if ptype not in(4160,4176):raise ValueError('Unsupported picture type/options')
        picture=(ptype>>4)&1;quant=take(5)
        if not quant or take(2):raise ValueError('Unsupported quantizer/CPM/PEI')
        if bool(result)!=bool(picture):raise ValueError('This clip profile uses one initial I-picture followed by P-pictures')
        macroblocks=[];gfid=None
        for row in range(9):
            predictor=(0,0)
            if row:
                align()
                if take(17)!=1 or take(5)!=row:raise ValueError('Missing or incorrect row GOB header')
                observed=take(2)
                if gfid is not None and observed!=gfid:raise ValueError('GFID changes inside a picture')
                gfid=observed
                if take(5)!=quant:raise ValueError('This profile retains one quantizer per picture')
            for col in range(11):
                mb,n=MACRO.decode_prefix(bits[p:],bool(picture),predictor);p+=n
                if mb['intra']==bool(picture):raise ValueError('The stated controller selects INTRA only in I-pictures')
                dx,dy=mb['motion']
                if dx%2 or dy%2 or not -4<=dx<=4 or not -4<=dy<=4 or not 0<=col*16+dx//2<=160 or not 0<=row*16+dy//2<=128:raise ValueError('Motion is outside the admitted whole-pixel search window')
                predictor=mb['motion'];macroblocks.append(mb)
        align()
        if result and ((gfid==result[-1]['gfid'])!=(picture==result[-1]['picture'])):raise ValueError('GFID does not follow a PTYPE change')
        result.append(dict(offset=start,bytes=p//8-start,picture=picture,temporalReference=tr,quantizer=quant,gfid=gfid,macroblocks=macroblocks))
    return result
