#!/usr/bin/env python3
"""Export the native lossless-video composition and exercise assets, without composing prose."""
import hashlib,importlib.util,json,re,shutil
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];ID='034-lossless-video';PACKAGE=ROOT/'packages'/ID;NATIVE=ROOT/'includes/chapters'/ID
STAGES=[('video-construction','kernel',1,6,'kernel'),('video-record-construction','record',7,23,'record'),('video-index-construction','index',24,36,'index'),('video-io-construction','io',37,45,'load'),('video-seek-construction','seek',46,56,'seek'),('video-full-encode-construction','full-encode',57,68,'full'),('video-delta-encode-construction','delta-encode',69,83,'record-encode'),('video-file-encode-construction','file-encode',84,101,'file-encode'),('video-playback-construction','playback',102,122,'playback')]

def load(name,path):
 s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m

def write(path,value):path.write_text(json.dumps(value,indent=2)+'\n')

def combined(drafts):
 text=(drafts/'video-record-construction/main.asm').read_text()
 for folder in('video-index-construction','video-io-construction','video-seek-construction','video-full-encode-construction'):text=load('video_export_'+folder.replace('-','_'),drafts/folder/'attach.py').attach(text)
 kernel=(drafts/'video-construction/kernel.asm').read_text();text=load('video_export_delta',drafts/'video-delta-encode-construction/attach.py').attach(text,kernel)
 for folder in('video-file-encode-construction','video-playback-construction'):text=load('video_export_'+folder.replace('-','_'),drafts/folder/'attach.py').attach(text)
 return text

def build():
 PACKAGE.mkdir(exist_ok=True)
 for name in('starter','native','checks','captures','construction'):(PACKAGE/name).mkdir(exist_ok=True)
 for folder,_,_,_,_ in STAGES:
  source=ROOT/'drafts'/folder;target=PACKAGE/'construction'/folder;target.mkdir(exist_ok=True)
  for path in sorted(source.rglob('*')):
   if path.is_file()and'__pycache__'not in path.parts and path.suffix in('.asm','.py','.json','.md'):
    dest=target/path.relative_to(source);dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(path,dest)
 source=ROOT/'drafts/video-fixtures';target=PACKAGE/'construction/video-fixtures';target.mkdir(exist_ok=True)
 for path in source.glob('*'):
  if path.is_file()and path.suffix in('.py','.md'):shutil.copyfile(path,target/path.name)
 for name,target in(('compact.py','construction/compact.py'),('grade.py','grade.py'),('README.md','README.md')):shutil.copyfile(ROOT/'drafts/video-feedback'/name,PACKAGE/target)
 text=load('video_export_compact',ROOT/'drafts/video-feedback/compact.py').compact(combined(ROOT/'drafts'));(PACKAGE/'starter/main.asm').write_text(text)
 for path in(ROOT/'drafts/video-feedback/checks').glob('*'):
  if path.is_file():shutil.copyfile(path,PACKAGE/'checks'/path.name)
 for path in NATIVE.glob('*.tex'):
  text=path.read_text().replace('includes/chapters/'+ID,'packages/'+ID+'/native').replace('book/captures/'+ID,'packages/'+ID+'/captures');text=re.sub(r'drafts/(video-[a-z-]+)',lambda m:'packages/'+ID+'/construction/'+m[1],text);(PACKAGE/'native'/path.name).write_text(text)
 (PACKAGE/'assignment.tex').write_text('% Native assignment fragment; use the book preamble.\n\\input{packages/'+ID+'/native/assignment}\n')
 steps=[];recipes=[];nativeSteps={}
 for folder,prefix,first,last,domain in STAGES:
  raw=json.loads((ROOT/'drafts'/folder/'steps.json').read_text())['steps'];recipes.extend(dict(r,stage=folder)for r in json.loads((ROOT/'drafts'/folder/'inputs.json').read_text())['recipes'])
  for original in raw:
   s=dict(original);n=int(s['id'][1:]);assert first<=n<=last;file=prefix+'-'+s['id'];quiz=dict(s['quiz']);quiz['prompt']=quiz.pop('question');s.update(quiz=quiz,kind='edit',content='native/'+file+'.tex',stage=folder,prerequisites=[]if n==1 else[f's{n-1:02}']);s['title']=re.sub(r'^\\section\{(?:Kernel )?[Ss]tep \d+:? (.*?)\}\s*$',r'\1',(NATIVE/(file+'.tex')).read_text().splitlines()[0]);s['recipes']=[s['recipe']];s['completion']=dict(type='native-observation',checkpoint=s['checkpoint'],completedStageCheck='checks/'+('store'if n>=94 and n<=101 else domain)+'.kts',scope='Stage-local construction observation; completed-interface feedback is separate. Use the matching stage working file.');steps.append(s);nativeSteps[file]=s['id']
 assert len(steps)==122 and[s['id']for s in steps]==[f's{n:02}'for n in range(1,123)];write(PACKAGE/'steps.json',dict(steps=steps));write(PACKAGE/'inputs.json',dict(recipes=recipes))
 figures=[]
 for ident,step,caption,panes in [('video-vector-count','s05','Inspect word lanes and their nonzero count.','registers and memory'),('video-record-publication','s23','Inspect an accepted picture and its next expected sequence.','registers and bitmap'),('video-index-publication','s36','Inspect offsets and metadata after whole-file validation.','memory and registers'),('video-independent-seek','s56','Compare the requested picture and replay start.','bitmap and registers'),('video-record-selection','s83','Inspect a required full record beside its source picture.','memory and registers'),('video-playback-progress','s122','Inspect final playback counters and the last accepted picture.','bitmap and registers')]:
  selected=next(s for s in steps if s['id']==step);figures.append(dict(id=ident,step=step,recipe=selected['recipe'],stage=selected['stage'],checkpoint=selected['checkpoint'],theme='IntelliJ Light',captureStatus='planned',caption=caption,alt='Planned '+panes+' observation at '+selected['checkpoint']+'. No screenshot captured.',nativeAnnotations='Pending actual image coordinates and semantic descriptions'))
 approved=ROOT/'book/captures'/ID
 if (approved/'figures.json').exists():
  captured=json.loads((approved/'figures.json').read_text())['figures'];seen=set()
  for figure in captured:
   assert figure['id']not in seen,'Duplicate approved capture';seen.add(figure['id']);canonical=next(f for f in figures if f['id']==figure['id'])
   assert all(figure[k]==canonical[k]for k in('id','step','recipe','stage','checkpoint','theme')),'Approved figure differs from canonical recipe'
   for name in('artifact','receipt'):
    relative=Path(figure[name]);assert not relative.is_absolute()and'..'not in relative.parts,'Unsafe approved asset'
   folder=Path(figure['receipt']).parent;assert folder==Path(figure['artifact']).parent
   source=approved/folder;assert source.is_dir()and not source.is_symlink()
   for path in source.rglob('*'):assert not path.is_symlink(),'Symlink approved asset'
   dest=PACKAGE/'captures'/folder;shutil.copytree(source,dest,dirs_exist_ok=True)
   receipt=json.loads((dest/Path(figure['receipt']).name).read_text())
   native=NATIVE/Path(figure['nativeAnnotations']).name
   assert hashlib.sha256(native.read_bytes()).hexdigest()==receipt['nativeSourceSha256'],'Native composition changed since review'
   receipt['nativeExportSha256']=hashlib.sha256((PACKAGE/figure['nativeAnnotations']).read_bytes()).hexdigest();write(dest/Path(figure['receipt']).name,receipt)
   canonical.update(figure,artifact='captures/'+figure['artifact'],receipt='captures/'+figure['receipt'])
 write(PACKAGE/'captures/plan.json',dict(figures=figures));reading=[]
 for name in re.findall(r'\\input\{includes/chapters/'+ID+r'/([^}]+)\}',(NATIVE/'chapter.tex').read_text()):
  if name in nativeSteps:
   step=nativeSteps[name];reading.append(dict(id='step-'+step,kind='step',step=step));reading.extend(dict(id='figure-'+f['id'],kind='figure',figure=f['id'])for f in figures if f['step']==step)
  else:reading.append(dict(id='p-'+name,kind='prose',content='native/'+name+'.tex',origin='ai',typesetting=dict(nativeLatex='native/'+name+'.tex')))
 write(PACKAGE/'reading.json',dict(formatVersion=1,authority='Native LaTeX; index of exercise assets only',segments=reading))
 domains=('kernel','record','index','load','seek','full','record-encode','file-encode','store','playback','reuse')
 manifest=dict(formatVersion=1,id=ID,version='0.1.0',status='draft',title='Build a small lossless video codec',audience='Students with basic programming competence and prior assembly/MSA experience',environment=dict(isa='MIPS64 Release 6',syscallAbi='MARS educational',jdk='25',ide='IntelliJ IDEA 2026.3',upstreamCommit='4bbee135c4e7612ea3a2048101624df4d0429623',memoryLayout='Student-owned ordinary data in the first two MiB; reserved separate grading arenas and 32768-byte private stack',byteOrder='little-endian',graderRequirements=['callFunction','enterFunction','readDataAddress','readBytes','traceMemoryAccesses','traceMemoryStores','provideFiles','provideTiming','runUntilSyscall']),modes=['guided','free-writing'],progression=dict(default='recommended-order',instructorControls=['require-quiz','require-check','hint-availability','allow-skip','allow-mode-switch'],localImplementation='Native stage-local observations and public completed checks; remote progression is pending'),entry='starter/main.asm',assignment='assignment.tex',reading='reading.json',gradingHarness='checks/harness.asm',prerequisites=['Native MIPS64 memory and calling conventions','CRC32 and bounded record parsing','Numeric RGB words and MSA word/vector operations'],learningObjectives=['Encode and recover exact frame sequences using full/XOR records','Validate whole files before directory and picture publication','Keep failed-call state according to each interface contract','Seek independently and play sequentially with anchored deadlines','Compare correct scalar/MSA work and valid representation choices'],functionContract=dict(entries=['video_xor_words','video_xor_words_msa','video_apply_record','video_index','video_load','video_seek','video_encode_full','video_encode_record','video_encode_file','video_store','video_now','video_play'],statuses={'0':'success','1':'completed failure','4':'unfinished construction only'},profile='MLV1 64x64 RGB, one through 256 frames, period 1..64, duration 1..1000ms',maxFileBytes=7344152,maxPackedSourceBytes=4194304,publication='Interface-specific: playback failure retains actual accepted pictures; file staging/work may change, final fields publish only on success',privateStack='32768 supplied bytes, no required offsets'),steps=steps,figures='captures/plan.json',publicChecks=['checks/'+name+'.kts'for name in domains],exports=['package.json','README.md','assignment.tex','starter','native','checks','captures','reading.json','construction','inputs.json','steps.json','grade.py'])
 bare='.data\nvideo_interface_message: .asciiz "Video interfaces are still under construction.\\n"\n.text\n.globl main\nmain:\n    dla $a0, video_interface_message\n    li $v0, 4\n    syscall\n    li $v0, 10\n    syscall\n'
 for entry in manifest['functionContract']['entries']:bare+=entry+':\n    b video_interface_unfinished\n    nop\n'
 bare+='video_interface_unfinished:\n    li $v0, 4\n    move $v1, $zero\n    jr $ra\n    nop\n';(PACKAGE/'starter/interfaces.asm').write_text(bare)
 write(PACKAGE/'package.json',manifest);return manifest
if __name__=='__main__':m=build();print(m['id'],len(m['steps']),'native steps exported')
