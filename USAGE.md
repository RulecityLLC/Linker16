# Using Linker16

This is the practical "how do I actually run it" guide.  The goal here is to take the eight Dragon's Lair 2 `.OBJ` files and end up with a working 64 KB ROM image that matches the original `dl2_319.bin` byte-for-byte.

## Linking the DL2 .OBJ files

Drop the eight `.OBJ` files in your current directory and run this from the project root:

```
./gradlew run --args="--dgroup-paragraph=0x0008 -o dl2_partial.bin ASMLIB.OBJ CLIB.OBJ DL2PROD2.OBJ SERIAL.OBJ ENTERI.OBJ EEPROTS2.OBJ DL2DATA.OBJ STATS2.OBJ"
```

If your `.OBJ` files live somewhere else, just pass full or relative paths instead of the bare filenames — Gradle runs with the project root as its working directory, so paths are resolved from there.

A few things to know about the arguments.  The order of the `.OBJ` files matters — segment combining depends on it, and the order above is the one the original LinkLoc response file (EPROM.LNK) used.  Re-order them and you'll get a different (and wrong) image.

The `--dgroup-paragraph=0x0008` flag is the runtime paragraph where DGROUP lives when the image executes.  This isn't something Linker16 can figure out on its own from the `.OBJ` inputs — it's a property of the target hardware's BIOS bootstrap, so you have to tell it.  For DL2 v3.19 the value is `0x0008`, which we figured out empirically by matching against the known-good ROM.

## What you get

The linker writes a `dl2_partial.bin` of exactly 0x9F50 bytes (40,784).  That's the linked DL2 code and data, and it's bit-for-bit identical to the first 0x9F50 bytes of the Dragon's Lair 2 v3.19 ROM.

After the DL2 portion ends at 0x9F4F, the ROM is zero-padded until 0xA000, where the DIAG (diagnostic) blob lives through 0xABB0.  Then more zero padding until 0xE070, where the BIOS blob starts and runs all the way to the end at 0xFFF4 — including the reset vector at 0xFFF0.  Linker16 doesn't produce DIAG or BIOS; those come from separate toolchains and ship as pre-built Intel HEX files in DIAG.HEX and BIOS.HEX with their absolute load addresses baked into the records.

## Building the full 64 KB ROM

To stitch your `dl2_partial.bin` together with DIAG and BIOS into the full ROM, there's a little Python script at [tools/stitch_rom.py](tools/stitch_rom.py):

```
python tools/stitch_rom.py \
    --dl2 dl2_partial.bin \
    --diag DIAG.HEX \
    --bios BIOS.HEX \
    -o dl2_319.bin
```

All it does is allocate a 64 KB buffer of zeros, copy `dl2_partial.bin` in at offset 0, then walk through each Intel HEX file and drop its data records at the addresses they specify.  The resulting `dl2_319.bin` should be byte-for-byte identical to the DL2 v3.19 ROM.

In theory, compiling the DL2 v3.20 source code and producing a new ROM binary should now be possible.  I haven't tested this; I leave this as an exercise for the reader :)
