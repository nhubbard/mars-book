#!/usr/bin/env python3
"""Render verified simulator-produced PCM as deterministic mono WAV listening fixtures."""
import argparse,hashlib,json,struct,subprocess,tempfile,wave
from pathlib import Path

def triangle(position,amplitude):
 u=position%64;level=u if u<16 else 32-u if u<48 else u-64
 return amplitude*level//16

def render(material,cli,java,source,output):
 sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
 with tempfile.TemporaryDirectory() as folder:
  script=Path(folder)/'listening.kts';script.write_text((material/'checks/model.kts').read_text()+(material/'checks/listening-cases.kts').read_text())
  r=subprocess.run([java,'-jar',str(cli),'grade',str(script),str(source)],capture_output=True,text=True)
  if r.returncode or sum(x.startswith('PASS listening ') for x in r.stdout.splitlines())!=3 or any(x.startswith('FAIL ') for x in r.stdout.splitlines()):raise RuntimeError(r.stdout+r.stderr)
  checksSha=sha(script)
 data={};ends={}
 for line in r.stdout.splitlines():
  if line.startswith('PCM,'):
   _,scene,track,start,encoded=line.split(',');start=int(start);key=(scene,track);stream=data.setdefault(key,bytearray());assert start==len(stream)//2;raw=bytes.fromhex(encoded);assert len(raw)==128;stream.extend(raw)
  elif line.startswith('PCM-END,'):
   _,scene,n,rate,clipped=line.split(',');assert scene not in ends;ends[scene]={'frames':int(n),'sampleRate':int(rate),'clipped':int(clipped)}
 scenes={'quiet':(6000,0),'clipping':(24000,0),'phase-offset':(24000,8)}
 assert set(ends)==set(scenes) and set(data)=={(s,t) for s in scenes for t in ['a','b','saturated','wrapped']}
 output.mkdir(parents=True,exist_ok=True);files={}
 for scene,(amplitude,phase) in scenes.items():
  a=[triangle(i,amplitude) for i in range(32000)];b=[triangle(i+phase,amplitude) for i in range(32000)];sums=[x+y for x,y in zip(a,b)];sat=[min(32767,max(-32768,x)) for x in sums];wrap=[(x+32768)%65536-32768 for x in sums]
  assert ends[scene]=={'frames':32000,'sampleRate':16000,'clipped':sum(x!=y for x,y in zip(sums,sat))}
  if scene=='quiet':assert sat==wrap and ends[scene]['clipped']==0
  else:assert sat!=wrap and ends[scene]['clipped']>0
  for track,values in [('a',a),('b',b),('saturated',sat),('wrapped',wrap)]:
   raw=bytes(data[(scene,track)]);assert raw==struct.pack('<'+'h'*len(values),*values)
   path=output/f'{scene}-{track}.wav'
   with wave.open(str(path),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(16000);w.writeframes(raw)
   with wave.open(str(path),'rb') as w:assert w.getnchannels()==1 and w.getsampwidth()==2 and w.getframerate()==16000 and w.getnframes()==32000 and w.readframes(32000)==raw
   files[path.name]={'sha256':sha(path),'pcmSha256':hashlib.sha256(raw).hexdigest(),'frames':32000,'sampleRate':16000,'channels':1,'sampleBits':16,'seconds':2}
 receipt={'status':'Verified simulator-derived listening fixtures; no live audio-device test claimed','rendererSha256':sha(Path(__file__)),'sourceSha256':sha(source),'cliSha256':sha(cli),'checksSha256':checksSha,'javaVersion':subprocess.run([java,'-version'],capture_output=True,text=True,check=True).stderr.strip(),'scenes':ends,'files':files,'schema':'signed 16-bit little-endian mono PCM inside standard WAV; original kernel amplitudes retained'}
 (output/'receipt.json').write_text(json.dumps(receipt,indent=2)+'\n');print('Rendered',len(files),'verified WAV files;', {s:v['clipped'] for s,v in ends.items()},flush=True);return receipt

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--material',type=Path,required=True);p.add_argument('--cli',type=Path,required=True);p.add_argument('--java',default='java');p.add_argument('--source',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();render(a.material.resolve(),a.cli.resolve(),a.java,a.source.resolve(),a.output.resolve())
if __name__=='__main__':main()
