#!/usr/bin/env python3
"""Author finite public video feedback from the published profile and numeric fixtures."""
import importlib.util,json,struct
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];DEST=ROOT/'drafts/video-feedback'
DOMAINS=('kernel','record','index','load','seek','full','record-encode','file-encode','store','playback','reuse')

def load(path):
 s=importlib.util.spec_from_file_location('public_video_feedback_fixtures',path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m

def build():
 DEST.mkdir(exist_ok=True);(DEST/'checks').mkdir(exist_ok=True);f=load(ROOT/'drafts/video-fixtures/fixture.py');decl=[];vectors={};raws={};mats={};groups={n:[]for n in DOMAINS}
 def words(values):
  key=tuple(values)
  if key not in vectors:
   name='colors'+str(len(vectors));vectors[key]=name;decl.append('val '+name+'=words('+json.dumps(''.join(f'{v:08x}'for v in values))+')')
  return vectors[key]
 def raw(data):
  key=bytes(data)
  if key not in raws:
   name='data'+str(len(raws));raws[key]=name;h=key.hex();decl.append('val '+name+'=octets(listOf('+','.join(json.dumps(h[i:i+16000])for i in range(0,len(h),16000))+').joinToString(""))'if h else'val '+name+'=octets("")')
  return raws[key]
 for name in f.NAMES:
  m=f.material(name);frames=[[int.from_bytes(rgb[i:i+3],'big')for i in range(0,len(rgb),3)]for rgb in m['frames']];mats[name]=(m,frames,raw(m['file']),'listOf('+','.join(words(frame)for frame in frames)+')')
 def add(domain,name,body,little):groups[domain].append('case('+json.dumps(name+f' little={little}')+',instructionLimit=200000000,byteOrder=ByteOrder.'+('LITTLE_ENDIAN'if little else'BIG_ENDIAN')+'){'+body+'}')
 for little in(False,True):
  b=str(little).lower()
  for msa in(False,True):
   v=str(msa).lower()
   for n in(1,3,4,5,17,4096):
    a=[(i*0x10203)&0xffffffff for i in range(n)];other=[value^(0x80000000 if i%3==0 else 0)for i,value in enumerate(a)]
    for alias in(0,1,2):add('kernel',f'{n} words alias={alias} msa={msa}',f'kernel({words(a)},{words(other)},{alias},{v},{b})',little)
   for count,status in((0,0),(4097,1),(2**32+5,1),(5,1)):add('kernel',f'argument admission {count} msa={msa}',f'kernelAdmission({count}L,{v},{status})',little)
   add('reuse','involution and rejected retry msa='+str(msa),f'kernelPersistent({v},{b})',little)
  for name,(m,frames,data,refs)in mats.items():
   for i,d in enumerate(m['directory']):
    if i not in(0,1,len(frames)-1):continue
    record=m['file'][d['offset']:d['offset']+d['record_bytes']];previous=frames[max(0,i-1)];add('record',name+' frame'+str(i),f'apply({raw(record)},{words(previous)},{words(frames[i])},{i},0,{b})',little)
   offsets='listOf('+','.join(str(d['offset'])+'L'for d in m['directory'])+')';add('index',name,f'index({data},{offsets},{len(frames)},0,{b})',little)
   for target in(0,len(frames)-1,len(frames),2**32):add('seek',name+' target'+str(target),f'seek({data},{offsets},{refs},{target}L,{0 if target<len(frames)else 1},{b})',little)
  m,frames,data,refs=mats['moving'];first=m['directory'][0];record=m['file'][24:24+first['record_bytes']];bad=record[:-1]+bytes([record[-1]^1]);add('record','bad CRC',f'apply({raw(bad)},{words(frames[0])},{words(frames[0])},0,1,{b})',little)
  malformed=f.frame_record(1,1,bytes(4)+struct.pack('>HH',4096,0));add('record','nonprogress with valid CRC',f'apply({raw(malformed)},{words(frames[0])},{words(frames[0])},1,1,{b})',little)
  for expression,label in((data+'.copyOf().also{it[it.size-1]=it.last() xor 1}','late CRC'),(data+'+intArrayOf(0)','file suffix'),(raw(m['file'][:23]),'short header')):add('index',label,f'index({expression},emptyList(),0,1,{b})',little)
  add('index','short directory',f'index({data},emptyList(),0,1,{b},capacity=63)',little)
  for n,capacity,status in((0,1,1),(1,1,0),(4097,4097,0),(4097,4096,1),(4097,65536,0)):
   payload=raw(bytes((37*i+11)&255 for i in range(n)));add('load',f'{n} bytes capacity={capacity}',f'load({payload},{capacity}L,{status},{b})',little)
  for name,extra in(('EOF read error','fail=4097'),('missing input','missing=true')):add('load',name,f'load({raw(bytes(4097))},4097L,1,{b},{extra})',little)
  add('load','zero capacity',f'load({raw(b"x")},0L,1,{b})',little)
  for full in(False,True):
   domain='full'if full else'record-encode';v=str(full).lower()
   for sequence in(0,1,255):
    for current,name in((frames[1],'changed'),(frames[0],'unchanged')):add(domain,name+' sequence'+str(sequence),f'encode({words(current)},{words(frames[0])},{sequence}L,{v},0,{b})',little)
   invalid=list(frames[1]);invalid[-1]|=1<<24;add(domain,'late high byte',f'encode({words(invalid)},{words(frames[0])},1L,{v},1,{b})',little);add(domain,'high source count',f'encode({words(frames[1])},{words(frames[0])},1L,{v},1,{b},count=4294983680L)',little)
   if not full:add(domain,'optional compact capacity',f'encode({words(frames[0])},{words(frames[0])},1L,false,-1,{b},capacity=20L)',little)
  for subset,name in((frames[:1],'single'),(frames[:2],'pair'),(frames,'eight')):add('file-encode',name,f'encodeFile(listOf('+','.join(words(x)for x in subset)+f'),0,{b})',little)
  invalid=list(frames[-1]);invalid[-1]|=1<<24;badFrames='listOf('+','.join(words(x)for x in frames[:-1])+','+words(invalid)+')';add('file-encode','late invalid input',f'encodeFile({badFrames},1,{b})',little);add('file-encode','mismatched source count',f'encodeFile({refs},1,{b},count=131076L)',little)
  for n in(1,7,4097):add('store',str(n)+' transferred bytes',f'store({raw(bytes((i*23+3)&255 for i in range(n)))},0,{b})',little)
  for name,extra in(('partial error','fail=13'),('denied open','allowed=false'),('zero count','count=0L'),('high count','count=4294967313L')):add('store',name,f'store({raw(bytes(97))},1,{b},{extra})',little)
  for profile,start in(('exact',0),('wait',0),('early',0),('catchup',0),('backward',0),('overflow',0),('carry',6),('exact',8)):
   anchor=0xfffffffffffffff0 if profile=='overflow'else 0x12345678fffffff0 if profile=='carry'else 1000;events=[];pictures=[];times=[];presented=0;late=0;maximum=0;status=0
   def event(op,value,picture):events.append('Event(Operation.'+op+','+str(value if value<2**63 else value-2**64)+'L)');pictures.append(picture);times.append(value)if op=='TIME'else None
   if start>=len(frames):status=1
   else:
    event('TIME',anchor,-1)
    for i in range(start,len(frames)):
     deadline=anchor+(i-start+1)*40
     if deadline>=2**64:status=1;break
     presented+=1
     if profile=='backward':event('TIME',anchor-1,i);status=1;break
     if profile=='wait':event('TIME',deadline-30,i);event('SLEEP',30,i)
     if profile=='early':event('TIME',deadline-30,i);event('SLEEP',30,i);event('TIME',deadline-5,i);event('SLEEP',5,i)
     now=anchor+110 if profile=='catchup'and i-start<2 else deadline;event('TIME',now,i)
     if now>deadline:late+=1;maximum=max(maximum,now-deadline)
   args=[data,refs,str(start)+'L','listOf('+','.join(str(t if t<2**63 else t-2**64)+'L'for t in times)+')'if times else'emptyList()','listOf('+','.join(events)+')'if events else'emptyList()','listOf('+','.join(str(p)for p in pictures)+')'if pictures else'emptyList()',str(status),str(presented),str(late),str(maximum)+'L',str((len(frames)-start)*40 if status==0 else 0)+'L',b];add('playback',profile+' start='+str(start),'play('+','.join(args)+')',little)
  add('playback','bad reloaded CRC',f'play({data}.copyOf().also{{it[it.size-1]=it.last() xor 1}},{refs},0L,emptyList(),emptyList(),emptyList(),1,0,0,0L,0L,{b})',little)
 common=(ROOT/'tools/video-feedback/common.kts').read_text();shared=common+'\n'.join(decl)+'\n'
 for name,lines in groups.items():(DEST/'checks'/(name+'.kts')).write_text(shared+'\n'.join(lines)+'\n')
 text='# Independent public grading storage; no video implementation or keys.\n.data 0x100000000\n'
 for name,n in(('a',262208),('b',262208),('out',262208),('previous',16448),('scratch',16448),('directory',2176),('context',160),('stack',32768)):text+='.align 4\nvideoCheck_'+name+': .space '+str(n)+'\n'
 text+='videoCheck_path: .asciiz "animation.mlv"\n    .space 114\n.text\nvideoCheck_return:\n    nop\n';(DEST/'checks/harness.asm').write_text(text);counts={k:len(v)for k,v in groups.items()};(DEST/'counts.json').write_text(json.dumps(counts,indent=2)+'\n');return counts
if __name__=='__main__':print(json.dumps(build(),indent=2))
