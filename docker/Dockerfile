# Image for compiling DL2 v3.19 .OBJ files under DOSBox.
#
# Build from the repo root so the Artifacts/ tree is in the build context:
#     docker build -t dl2build -f docker/Dockerfile .
#
# The MASM/CL command list is NOT baked in -- the user supplies it as
# BUILD.BAT in the workspace they mount at /workspace.

FROM debian:stable-slim

RUN apt-get update \
 && apt-get install -y --no-install-recommends dosbox \
 && rm -rf /var/lib/apt/lists/*

# Inside DOSBox, drive C: is /shared, with C:\C600 and C:\MASM as the
# Microsoft toolchains and C:\SOURCE as the user's workspace.  Matching the
# c:\source\ paths the SynaMax readme uses matters because the OBJ files
# embed the source pathname literally, so a different path here produces
# different bytes than the canonical reference OBJs.
COPY Artifacts/TOOLS/C600 /shared/C600
COPY Artifacts/TOOLS/MASM /shared/MASM
RUN ln -s /workspace /shared/source

COPY docker/dosbox.conf /etc/dosbox/dosbox.conf
COPY docker/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Run DOSBox without a real display or audio device.  SDL's dummy drivers
# are enough -- nothing graphical actually has to render, and DOSBox never
# makes a sound in this image anyway, so probing ALSA just produces noise.
ENV SDL_VIDEODRIVER=dummy
ENV SDL_AUDIODRIVER=dummy

VOLUME ["/workspace"]
WORKDIR /workspace

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
