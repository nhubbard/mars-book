#!/usr/bin/env python3
"""Generate the repeated-row 512 by 512 RGB8 teaching image at a fresh path."""
import argparse
from pathlib import Path
import struct
import zlib


def material():
    samples=bytes((37*x+97*channel)%256 for x in range(512) for channel in range(3))
    filtered=bytearray([0]);filtered.extend(samples)
    for _ in range(511):filtered.append(2);filtered.extend(bytes(1536))
    def chunk(name,payload):
        checked=name+payload
        return struct.pack('>I',len(payload))+checked+struct.pack('>I',zlib.crc32(checked))
    header=struct.pack('>IIBBBBB',512,512,8,2,0,0,0)
    png=bytes.fromhex('89504e470d0a1a0a')+chunk(b'IHDR',header)
    png+=chunk(b'IDAT',zlib.compress(bytes(filtered)))+chunk(b'IEND',b'')
    return {'png':png,'filtered':bytes(filtered),'rgb':samples*512}


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('output',type=Path)
    parser.add_argument('--part',choices=('png','filtered','rgb'),default='png')
    args=parser.parse_args()
    try:
        with args.output.open('xb') as output:output.write(material()[args.part])
    except FileExistsError:parser.exit(2,'Existing destination refused\n')


if __name__=='__main__':main()
