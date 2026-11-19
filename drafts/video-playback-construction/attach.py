#!/usr/bin/env python3
"""Keep earlier complete-file work while adding validated timed playback."""
import argparse,re
from pathlib import Path
ROOT=Path(__file__).resolve().parent

def attach(source):
 if source.count('\n.text\n')!=1 or len(re.findall(r'^main:',source,re.M))!=1 or any(source.count('\n'+name+':\n')!=1 for name in('video_encode_file','video_store','video_index','video_seek','video_apply_record')):raise ValueError('Use one complete file-codec working file')
 if re.search(r'^(?:vp_\w+|vn_\w+|playback_\w+|video_play|video_now|file_codec_example_main):',source,re.M):raise ValueError('Playback labels already exist')
 data,code=source.split('\n.text\n');code=re.sub(r'^main:','file_codec_example_main:',code,count=1,flags=re.M)
 return data+'\n'+(ROOT/'data.asm').read_text()+'\n.text\n'+(ROOT/'caller.asm').read_text()+'\n'+(ROOT/'player.asm').read_text()+'\n'+code
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--source',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();result=attach(a.source.read_text())
 with a.output.open('x')as out:out.write(result)
