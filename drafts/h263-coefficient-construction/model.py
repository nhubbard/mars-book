"""Baseline block events; compact and escape encodings decode to the same levels."""
import json
from pathlib import Path
TABLE=json.loads((Path(__file__).resolve().parent/'codes.json').read_text())
SCAN=TABLE['scan'];CODES={(r['last'],r['run'],r['magnitude']):(r['code'],r['width'])for r in TABLE['events']}
DECODE={format(r['code']>>1,f"0{r['width']-1}b"):(r['last'],r['run'],r['magnitude'])for r in TABLE['events']}

def events(levels,intra=False):
    if type(intra)is not bool or len(levels)!=64:raise ValueError('Use 64 natural-order levels and Boolean INTRA')
    for i,l in enumerate(levels):
        if type(l)is not int or (not 1<=l<=255 or l==128 if intra and i==0 else not -127<=l<=127):raise ValueError('Invalid baseline level or INTRA DC code')
    active=[(i,levels[SCAN[i]])for i in range(1 if intra else 0,64)if levels[SCAN[i]]]
    result=[];previous=0 if intra else -1
    for n,(i,l)in enumerate(active):
        result.append((int(n==len(active)-1),i-previous-1,l));previous=i
    return result

def encode(levels,intra=False,compact=True):
    sequence=events(levels,intra);bits=format(levels[0],'08b')if intra else ''
    for last,run,level in sequence:
        found=CODES.get((last,run,abs(level)))if compact else None
        if found:
            code,width=found;bits+=format(code|int(level<0),f'0{width}b')
        else:bits+='0000011'+str(last)+format(run,'06b')+format(level&255,'08b')
    return bits

def decode(bits,intra=False):
    if type(intra)is not bool or any(c not in '01'for c in bits):raise ValueError('Use a bit string and Boolean INTRA')
    pos=0;levels=[0]*64;index=0 if intra else -1
    def take(width):
        nonlocal pos
        if pos+width>len(bits):raise ValueError('Truncated coefficient code')
        value=int(bits[pos:pos+width],2);pos+=width;return value
    if intra:
        dc=take(8)
        if dc in(0,128):raise ValueError('Forbidden INTRA DC code')
        levels[0]=dc
    last_seen=False;have_events=False
    while pos<len(bits):
        have_events=True;prefix='';event=None
        for _ in range(12):
            prefix+=str(take(1))
            if prefix=='0000011':
                last=take(1);run=take(6);l=take(8);level=l-256 if l>=128 else l
                if level in(0,-128):raise ValueError('Forbidden ordinary escape level')
                event=(last,run,level);break
            if prefix in DECODE:
                last,run,mag=DECODE[prefix];event=(last,run,-mag if take(1)else mag);break
        if event is None:raise ValueError('Unknown baseline VLC')
        last,run,level=event;index+=run+1
        if index>=64:raise ValueError('Run exceeds block')
        levels[SCAN[index]]=level
        if last:
            last_seen=True
            if pos!=len(bits):raise ValueError('Trailing bits after LAST')
            break
    if have_events and not last_seen:raise ValueError('Missing LAST')
    return levels
