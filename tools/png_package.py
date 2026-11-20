#!/usr/bin/env python3
"""Export native chapter33 and exercise assets; never compose prose from metadata."""
import importlib.util,json,re,shutil
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];ID='033-png';PACKAGE=ROOT/'packages'/ID;NATIVE=ROOT/'includes/chapters'/ID
STAGES=[('png-construction','load',1,10,'loader'),('png-collector-construction','collect',11,27,'collect'),('png-inflater-construction','inflate',28,43,'inflate'),('png-huffman-construction','huff',44,70,'inflate'),('png-row-construction','row',71,83,'rows'),('png-packing-construction','pack',84,100,'pack')]

def load(name,path):
 s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m

def write(path,data):path.write_text(json.dumps(data,indent=2)+'\n')

def build():
 PACKAGE.mkdir(exist_ok=True)
 for name in('starter','native','checks','captures','construction'):(PACKAGE/name).mkdir(exist_ok=True)
 for folder,_,_,_,_ in STAGES:
  source=ROOT/'drafts'/folder;target=PACKAGE/'construction'/folder;target.mkdir(exist_ok=True)
  for path in sorted(source.rglob('*')):
   if path.is_file()and'__pycache__'not in path.parts and path.suffix in('.py','.asm','.json','.md'):
    dest=target/path.relative_to(source);dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(path,dest)
 for folder in('png-fixtures',):
  source=ROOT/'drafts'/folder;target=PACKAGE/'construction'/folder;target.mkdir(exist_ok=True)
  for path in source.glob('*.py'):shutil.copyfile(path,target/path.name)
 text=(ROOT/'drafts/png-construction/main.asm').read_text()
 for folder,_,_,_,_ in STAGES[1:]:text=load('png_attach_'+folder.replace('-','_'),ROOT/'drafts'/folder/'attach.py').attach(text)
 (PACKAGE/'starter/main.asm').write_text(text)
 for path in (ROOT/'drafts/png-feedback/checks').glob('*'):
  if path.is_file():shutil.copyfile(path,PACKAGE/'checks'/path.name)
 shutil.copyfile(ROOT/'drafts/png-feedback/grade.py',PACKAGE/'grade.py');shutil.copyfile(ROOT/'drafts/png-feedback/README.md',PACKAGE/'README.md')
 for path in NATIVE.glob('*.tex'):
  text=path.read_text().replace('includes/chapters/'+ID,'packages/'+ID+'/native')
  text=re.sub(r'drafts/(png-[a-z-]+)',lambda m:'packages/'+ID+'/construction/'+m[1],text)
  (PACKAGE/'native'/path.name).write_text(text)
 (PACKAGE/'assignment.tex').write_text('% Native assignment fragment; compose with the book preamble.\n\\input{packages/'+ID+'/native/assignment}\n')
 steps=[];native_steps={};inputs=[]
 for folder,prefix,first,last,domain in STAGES:
  raw=json.loads((ROOT/'drafts'/folder/'steps.json').read_text())['steps'];recipes=json.loads((ROOT/'drafts'/folder/'inputs.json').read_text())['recipes']
  inputs.extend(dict(r,stage=folder)for r in recipes)
  for original in raw:
   s=dict(original);n=int(s['id'][1:]);assert first<=n<=last;s.pop('prose',None);s.pop('expectedObservation',None);quiz=dict(s['quiz']);quiz['prompt']=quiz.pop('question',quiz.get('prompt'));quiz['choices']=[dict(c,id=c['id'].upper())for c in quiz['choices']];s['quiz']=quiz;s['kind']=s.get('kind','edit');s['content']='native/'+prefix+'-'+s['id']+'.tex';s['prerequisites']=[]if n==1 else[f's{n-1:02}'];s['stage']=folder;s['recipes']=s.get('recipes',[s['recipe']]if'recipe'in s else[]);s['completion']=dict(type='native-observation',checkpoint=s['checkpoint'],completedStageCheck='checks/'+('decode'if n>=98 else domain)+'.kts',scope='Compare the native Observe checkpoint during construction; run the completed stage after its regions are filled. Instructor cumulative checks are separate.');steps.append(s);native_steps[prefix+'-'+s['id']]=s['id']
 assert len(steps)==100 and[s['id']for s in steps]==[f's{n:02}'for n in range(1,101)]
 write(PACKAGE/'steps.json',dict(steps=steps));write(PACKAGE/'inputs.json',dict(recipes=inputs))
 plans=[]
 for ident,step,recipe,checkpoint,caption,panes in[
  ('png-file-eof','s10','s10-empty','loader_return','Inspect the empty-file failure at the loader caller boundary.',['editor','registers','memory']),
  ('png-container-fields','s27','s27-1','collect_publish_ready','Inspect accepted dimensions and joined/inflated counts before decompression.',['editor','registers','memory']),
  ('png-row-reconstruction','s83','s83-1','row_up_tail_ready','Compare reconstructed row bytes and the retained filter selectors.',['editor','registers','memory']),
  ('png-vector-packing','s97','s97-1','pack_vector_advanced','Inspect sixteen packed pixels and the scalar remainder.',['editor','registers','memory']),
  ('png-final-publication','s100','s100-1','publication_results_ready','Show final width, height and active bytes beside the displayed image.',['editor','registers','bitmap'])]:
  # A normal IDE launch follows the supplied caller, not a direct-grading sentinel.
  selected=[r for r in inputs if r['id']==recipe]
  assert len(selected)==1 and recipe in next(s for s in steps if s['id']==step)['recipes']
  assert checkpoint==selected[0].get('callerCheckpoint',selected[0]['checkpoint'])
  plans.append(dict(id=ident,step=step,recipe=recipe,checkpoint=checkpoint,theme='IntelliJ Light',captureStatus='planned',caption=caption,alt='Planned '+', '.join(panes)+' view at '+checkpoint+'. No screenshot has been captured.',nativeAnnotations='Pending actual capture coordinates and semantic descriptions'))
 write(PACKAGE/'captures/plan.json',dict(figures=plans))
 reading=[]
 for name in re.findall(r'\\input\{includes/chapters/'+ID+r'/([^}]+)\}',(NATIVE/'chapter.tex').read_text()):
  if name in native_steps:
   step=native_steps[name];reading.append(dict(id='step-'+step,kind='step',step=step));reading.extend(dict(id='figure-'+p['id'],kind='figure',figure=p['id'])for p in plans if p['step']==step)
  else:reading.append(dict(id='p-'+name,kind='prose',content='native/'+name+'.tex',origin='ai',typesetting=dict(nativeLatex='native/'+name+'.tex')))
 write(PACKAGE/'reading.json',dict(formatVersion=1,authority='Native LaTeX; exercise/asset index only',segments=reading))
 manifest=dict(formatVersion=1,id=ID,version='0.1.0',status='draft',title='Decode PNG images',audience='Students with basic programming and prior assembly/MSA experience',environment=dict(isa='MIPS64 Release 6',syscallAbi='MARS educational',jdk='25',ide='IntelliJ IDEA 2026.3',upstreamCommit='118b85dc831c6caced3f94e67228cb4e7fe809f5',memoryLayout='Default classroom heap with separate grading storage',byteOrder='little-endian',graderRequirements=['callFunction','readDataAddress','readBytes','traceMemoryAccesses','provideFiles','startAt','runUntil']),modes=['guided','free-writing'],progression=dict(default='recommended-order',instructorControls=['require-quiz','require-check','hint-availability','allow-skip','allow-mode-switch'],localImplementation='Native per-step observations and public completed-stage feedback; automatic remote progression is pending'),entry='starter/main.asm',assignment='assignment.tex',reading='reading.json',gradingHarness='checks/harness.asm',prerequisites=['Full-width memory windows and nested calls','CRC32, bounded variable-length parsing and bit operations','Native-word RGB packing and MSA loads/shuffles'],learningObjectives=['Validate PNG chunks before publishing metadata','Inflate stored/fixed/dynamic DEFLATE blocks into exact bounded output','Reconstruct all five PNG filters after complete selector validation','Pack numeric colors with exact stride/capacity bounds','Preserve visible outputs across rejected calls and compare equivalent scalar/vector results'],functionContract=dict(entries=['png_load','png_collect','png_inflate','png_unfilter','png_unfilter_msa','png_blit','png_blit_msa','png_decode','png_decode_msa'],statuses={'0':'success','1':'completed failure','4':'unfinished construction only'},dimensions=[1,512],maxFileBytes=1048576,maxInflatedBytes=786944,profile='Static noninterlaced RGB8 PNG, exact container and zlib completion',publication='Complete failures retain framebuffer/context; scratch failure bytes may change within capacity',privateStack='Valid supplied private storage; no required internal offsets'),steps=steps,figures='captures/plan.json',publicChecks=['checks/'+name+'.kts'for name in('loader','collect','inflate','rows','pack','decode','reuse','caller')],exports=['package.json','README.md','assignment.tex','starter','native','checks','captures','reading.json','construction','inputs.json','steps.json','grade.py'])
 write(PACKAGE/'package.json',manifest);return manifest
if __name__=='__main__':m=build();print(m['id'],len(m['steps']),'native steps exported')
