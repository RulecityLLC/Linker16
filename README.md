# Linker16
Linker for 16-bit Intel OMF files created with MS-DOS MASM or Microsoft C compilers.

This was created mainly to help build the source code for the laserdisc arcade game Dragon's Lair 2.  We have the source code, but the special embedded linker they used was lost.  I thought it wouldn't be too hard to write a replacement linker from scratch.  But like most projects I take on, it ended up being harder than I thought.  I got blocked due to some stuff Microsoft C compiler v6.0 was generating that was not in any OMF documentation I could find.  I've created a unit test that will fail due to these issues.  So just run the unit tests and look at the test that is failing to see where work needs to be done to move forward.

For now, the main method will just attempt to parse an .OBJ file.  It succeeds on many of them that I tried but not all.

"The MS-DOS Encyclopedia" by Ray Duncan (1988) has a lot of info about OMF and how their linker works.  Article 19 "Object Modules" and Article 20 "The Microsoft Object Linker" are especially useful.  Unfortunately, it appears that Microsoft extended the OMF standard after this book was published and that's partly why I'm blocked.
