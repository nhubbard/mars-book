#!/usr/bin/env python3
"""Encode known RGB8 packing observations using the existing public fixture writers."""
import argparse,importlib.util
from pathlib import Path
ROOT=Path(__file__).resolve().parent

def load(name,path):
 s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
rows=load('packing_row_inputs',ROOT.parent/'png-row-construction/fixtures.py');format=load('packing_png_inputs',ROOT.parent/'png-fixtures/fixture.py')
NAMES=('worked','width15','width16','width17','bad-selector')
def material(name):
 if name not in NAMES:raise ValueError('Unknown packing fixture')
 if name in('worked','bad-selector'):return rows.material(name)
 width=int(name[5:]);m=format.material(width=width,height=5);return dict(png=m['png'],filtered=m['filtered'],rgb=m['rgb'],width=width,height=5)
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('fixture',choices=NAMES);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
 with a.output.open('xb')as out:out.write(material(a.fixture)['png'])
if __name__=='__main__':main()
