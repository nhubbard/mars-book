#!/usr/bin/env python3
"""Generate complete encoded video files, including malformed and empty inputs."""
import argparse,importlib.util
from pathlib import Path
ROOT=Path(__file__).resolve().parent
s=importlib.util.spec_from_file_location('video_io_inputs',ROOT.parent/'video-index-construction/fixtures.py');index=importlib.util.module_from_spec(s);s.loader.exec_module(index)
NAMES=(*index.video.NAMES,'empty','late-crc','late-command','file-suffix')
def material(name='moving'):
 if name=='empty':return b''
 if name in index.CASES[1:]:return index.material(case=name)['file']
 return index.material(name)['file']
if __name__=='__main__':
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('fixture',choices=NAMES);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
 with a.output.open('xb')as out:out.write(material(a.fixture))
