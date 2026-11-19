#!/usr/bin/env python3
"""Create a named loader input at a fresh path; no decoder is supplied."""
import argparse,importlib.util
from pathlib import Path
ROOT=Path(__file__).resolve().parent


def material(name):
    spec=importlib.util.spec_from_file_location('png_teaching_inputs',ROOT.parent/'png-fixtures/fixture.py')
    f=importlib.util.module_from_spec(spec);spec.loader.exec_module(f)
    worked=f.material()['png']
    if name=='worked':return worked
    if name=='partial':return worked[:17]
    if name=='empty':return b''
    if name=='long':return worked+b'X'
    raise ValueError('Unknown loader input')


def main():
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('name',choices=('worked','partial','empty','long'));p.add_argument('output',type=Path);a=p.parse_args()
    data=material(a.name)
    with a.output.open('xb')as output:output.write(data)

if __name__=='__main__':main()
