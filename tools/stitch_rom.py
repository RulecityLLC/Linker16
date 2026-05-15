#!/usr/bin/env python3
"""
Stitch the Linker16 output (DL2 portion) together with DIAG and BIOS blobs
into the full 64 KB Dragon's Lair 2 ROM image.

Layout produced:
    0x0000-0x9F4F   DL2 (from Linker16, raw binary)
    0x9F50-0x9FFF   zero pad
    0xA000-0xABB0   DIAG (from Intel HEX)
    0xABB1-0xE06F   zero pad
    0xE070-0xFFF4   BIOS (from Intel HEX, includes reset vector at 0xFFF0)
    0xFFF5-0xFFFF   zero pad / BIOS tail

Usage:
    python stitch_rom.py --dl2 dl2.bin --diag DIAG.HEX --bios BIOS.HEX -o rom.bin
"""

import argparse
import sys


def apply_ihex(image: bytearray, hex_path: str) -> None:
    """Parse an Intel HEX file and write its data records into `image`."""
    base = 0
    with open(hex_path, "r") as f:
        for lineno, raw in enumerate(f, 1):
            line = raw.strip()
            if not line.startswith(":"):
                continue
            data = bytes.fromhex(line[1:])
            count = data[0]
            addr = (data[1] << 8) | data[2]
            rectype = data[3]
            payload = data[4:4 + count]
            checksum = (-sum(data[:-1])) & 0xFF
            if checksum != data[-1]:
                sys.exit(f"{hex_path}:{lineno}: bad checksum")
            if rectype == 0x00:  # Data
                target = base + addr
                if target + len(payload) > len(image):
                    sys.exit(f"{hex_path}:{lineno}: record overruns 64 KB image "
                             f"(addr=0x{target:05X})")
                image[target:target + len(payload)] = payload
            elif rectype == 0x01:  # EOF
                return
            elif rectype == 0x02:  # Extended Segment Address
                base = ((payload[0] << 8) | payload[1]) << 4
            elif rectype == 0x04:  # Extended Linear Address
                base = ((payload[0] << 8) | payload[1]) << 16
            elif rectype == 0x03 or rectype == 0x05:
                pass  # Start address records — ignored for ROM image
            else:
                sys.exit(f"{hex_path}:{lineno}: unsupported record type 0x{rectype:02X}")


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__,
                                formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--dl2", required=True, help="Linker16 output (raw binary, 0x9F50 bytes)")
    p.add_argument("--diag", required=True, help="DIAG Intel HEX file (loads at 0xA000)")
    p.add_argument("--bios", required=True, help="BIOS Intel HEX file (loads at 0xE070)")
    p.add_argument("-o", "--output", required=True, help="Output 64 KB ROM image")
    args = p.parse_args()

    image = bytearray(0x10000)  # 64 KB, zero-initialised

    with open(args.dl2, "rb") as f:
        dl2 = f.read()
    if len(dl2) > 0xA000:
        sys.exit(f"--dl2 file is {len(dl2)} bytes; must be <= 0xA000 to fit before DIAG")
    image[0:len(dl2)] = dl2

    apply_ihex(image, args.diag)
    apply_ihex(image, args.bios)

    with open(args.output, "wb") as f:
        f.write(image)
    print(f"Wrote {len(image)} bytes to {args.output}")


if __name__ == "__main__":
    main()
