#!/usr/bin/env python3
"""Independently decode a complete clip and report source quality and local-reference drift."""
import argparse
import hashlib
import importlib.util
import json
import math
import shutil
import subprocess
import tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location("picture_stream_model",ROOT/"model.py");MODEL=importlib.util.module_from_spec(spec);spec.loader.exec_module(MODEL)
inspect_stream=MODEL.inspect_stream

def check(stream,raw,reconstruction,frames=8,tick_step=6):
    s=importlib.util.spec_from_file_location('picture_fixture',ROOT.parent/'h263-fixtures/fixture.py');f=importlib.util.module_from_spec(s);s.loader.exec_module(f);f.validate(frames,tick_step)
    encoded=stream.read_bytes();source=raw.read_bytes();local=reconstruction.read_bytes();pictures=inspect_stream(encoded)
    if len(pictures)!=frames or len(source)!=frames*38016 or len(local)!=len(source):raise ValueError('Frame counts and complete plane extents must agree')
    if [p['temporalReference']for p in pictures]!=[i*tick_step%256 for i in range(frames)]:raise ValueError('Temporal references disagree with the chosen clock step')
    tool=shutil.which('ffmpeg')
    if not tool:raise ValueError('Install FFmpeg for the independent interoperability check')
    with tempfile.TemporaryDirectory(prefix='mars-h263-check-')as temp:
        decoded=Path(temp)/'decoded.yuv';args=[tool,'-hide_banner','-v','error','-xerror','-threads','1','-idct','int','-f','h263','-i',str(stream.resolve()),'-map','0:v:0','-fps_mode','passthrough','-pix_fmt','yuv420p','-f','rawvideo',str(decoded)]
        r=subprocess.run(args,capture_output=True,text=True)
        if r.returncode or r.stderr:raise ValueError('Independent decoding failed: '+r.stderr)
        pixels=decoded.read_bytes()
    if len(pixels)!=len(source):raise ValueError('Independent decoder returned a different picture count/size')
    quality=[]
    for index in range(frames):
        for name,offset,size in [('Y',0,25344),('Cb',25344,6336),('Cr',31680,6336)]:
            at=index*38016+offset;a=source[at:at+size];b=pixels[at:at+size];c=local[at:at+size]
            squared=sum((x-y)**2 for x,y in zip(a,b));psnr=None if not squared else 10*math.log10(65025*size/squared)
            drift=max(abs(x-y)for x,y in zip(b,c))
            if (psnr is not None and psnr<35)or drift>16:raise ValueError('Clip exceeds the documented quality/drift acceptance limits')
            quality.append(dict(picture=index,plane=name,samples=size,squaredError=squared,psnrDb=psnr,maximumLocalDecoderDifference=drift))
    sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
    return dict(scope='Complete restricted-profile stream syntax, independent decoding, source quality and local reconstruction comparison; individual kernel gates remain required',frames=frames,encodedBytes=len(encoded),pictures=[{k:v for k,v in p.items()if k!='macroblocks'}|dict(skipped=sum(m['skip']for m in p['macroblocks']),nonzeroMotion=sum(m['motion']!=(0,0)for m in p['macroblocks']))for p in pictures],quality=quality,limits=dict(minimumPlanePsnrDb=35,maximumLocalDecoderDifference=16,explanation='Accuracy tolerances accept different legal transform rounding; these clip gates supplement the standalone inverse accuracy gate'),sha256=dict(stream=sha(stream),input=sha(raw),localReconstruction=sha(reconstruction),ffmpeg=sha(Path(tool).resolve())),ffmpegVersion=subprocess.check_output([tool,'-version'],text=True),decoderCommand=args)

if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--stream',type=Path,required=True);p.add_argument('--input',type=Path,required=True);p.add_argument('--reconstruction',type=Path,required=True);p.add_argument('--frames',type=int,default=8);p.add_argument('--tick-step',type=int,default=6);p.add_argument('--report',type=Path,required=True);a=p.parse_args()
    if a.report.exists():p.error('Existing report refused')
    try:result=check(a.stream,a.input,a.reconstruction,a.frames,a.tick_step)
    except(ValueError,FileNotFoundError)as e:p.error(str(e))
    with a.report.open('x')as out:json.dump(result,out,indent=2,allow_nan=False);out.write('\n')
    print('Decoded',result['frames'],'complete QCIF pictures from',result['encodedBytes'],'bytes')
