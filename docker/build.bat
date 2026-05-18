@echo off
REM Sample BUILD.BAT for DL2 source code.  Intended to be run by a docker image, not run directly from a native Windows/DOS host!
REM
REM Make sure this BUILD.BAT lives in the same folder as the DL2 source code. (it can replace the existing BUILD.BAT which is no longer needed)

echo Building DL2 .OBJ files > build.log

masm /Ml c:\source\asmlib.asm >> build.log
cl /J /G0 /c /Zl /AS /Ox c:\source\clib.c >> build.log
cl /J /G0 /c /Zl /AS /Ox /Fcdl2prod /DOUR_PCB /Di88 /DDOMESTIC /DPRODUCTION /DSONY /DENGLISH /DEPROM c:\source\dl2prod2.c >> build.log
cl /J /G0 /c /Zl /AS /Ox /Di88 /DDOMESTIC /DSONY /DOUR_PCB /DPRODUCTION /DEPROM /DMAKE_OBJ c:\source\serial.c >> build.log
cl /J /G0 /c /Zl /AS /Ox /DOUR_PCB /Di88 /DDOMESTIC /DPRODUCTION /DEPROM /DSONY /DENGLISH c:\source\enteri.c >> build.log
cl /J /G0 /c /Zl /AS /Ox /DOUR_PCB /Di88 /DDOMESTIC /DPRODUCTION /DEPROM /DSONY c:\source\eeprots2.c >> build.log
cl /J /G0 /c /Zl /AS /Ox c:\source\dl2data.c >> build.log
cl /J /G0 /c /Zl /AS /Ox /DCOIN /Di88 /DDOMESTIC /DSONY /DENGLISH /DOUR_PCB /DPRODUCTION /DEPROM c:\source\stats2.c >> build.log

echo Done. >> build.log
