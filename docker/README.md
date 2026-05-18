# Building the DL2 .OBJ files with Docker

The DL2 source code was originally compiled with Microsoft C 6.0a and MASM 6.0, both MS-DOS-era tools.

This `docker/` folder gives you a way to skip running DOSBox and/or installing those tools.

You build a Docker image once, drop your source files into a folder, and run a single `docker run` to build all of the .OBJ files.

## Building the docker image

From the repo root (not from inside `docker/` -- the Dockerfile needs the `Artifacts/` tree in its build context):

```
docker build -t dl2build -f docker/Dockerfile .
```

That gives you an image called `dl2build`, around 200 MB once Debian and DOSBox are layered in.  You only need to do this once.

## Building the DL2 source code

Put a copy of [build.bat](build.bat) into your DL2 source folder.

Then `cd` into that folder and run:

```
# PowerShell (Windows)
docker run --rm -v ${PWD}:/workspace dl2build

# bash / zsh (Linux, macOS, Git-Bash on Windows)
docker run --rm -v "$PWD:/workspace" dl2build
```

If you're on cmd rather than PowerShell in Windows, use `%cd%` instead of `${PWD}`.

## Caveats

On Linux hosts, the `.OBJ` files may end up owned by root because that's who Docker runs as by default.  If that's annoying, pass `--user $(id -u):$(id -g)` to `docker run`.
