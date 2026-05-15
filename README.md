# Linker16
Linker for 16-bit Intel OMF files created with MS-DOS MASM or Microsoft C compilers.

This was created mainly to help build the source code for the laserdisc arcade game Dragon's Lair 2.  We have the source code, but the special embedded linker they used was lost.  I thought it wouldn't be too hard to write a replacement linker from scratch.  But like most projects I take on, it ended up being harder than I thought.

But now it's finished.  Using .OBJ files generated from Microsoft C Compiler (MS-DOS), this Linker will produce the exact Dragon's Lair 2 v3.19 binary.  In theory this means, that building v3.20 is now possible.

See USAGE.md for instructions.

Helpful reference:
"The MS-DOS Encyclopedia" by Ray Duncan (1988) has a lot of info about OMF and how their linker works.  Article 19 "Object Modules" and Article 20 "The Microsoft Object Linker" are especially useful.
