#!/bin/bash
# Run dosbox and filter out the harmless boilerplate it always prints
# (banner, MIDI/MIXER/ALSA chatter, SHELL:Redirect notices) while polling
# the workspace build.log in the background so the user sees compiler
# progress in real time.  Real errors from dosbox or the compilers still
# reach stdout, and dosbox's exit code is preserved via PIPESTATUS.

# DOSBox upper-cases 8.3 names when it writes through to the host, so on a
# case-sensitive filesystem (Linux) the log ends up as BUILD.LOG even though
# BUILD.BAT redirects to `build.log`.  Windows hosts are case-insensitive,
# so either form resolves to the same file.  Check both.
find_log() {
  for f in /workspace/build.log /workspace/BUILD.LOG; do
    [ -f "$f" ] && { echo "$f"; return; }
  done
}

# Clear any prior log so we follow only the current build.  The sample
# BUILD.BAT truncates this file on its first line anyway, so users with a
# stock setup aren't losing anything they'd want to keep.
rm -f /workspace/build.log /workspace/BUILD.LOG 2>/dev/null

# Background poller: every 300 ms, if build.log has grown since we last
# looked, print just the new bytes.  We do this by hand instead of using
# `tail -F` because DOSBox's writes to the bind-mounted log don't reliably
# trigger tail's change-detection heuristic, even though plain stat sees
# the growth fine.
(
  LAST=0
  while true; do
    LOG=$(find_log)
    if [ -n "$LOG" ]; then
      SIZE=$(stat -c %s "$LOG" 2>/dev/null || echo 0)
      if [ "$SIZE" -gt "$LAST" ]; then
        tail -c +$((LAST + 1)) "$LOG" 2>/dev/null
        LAST=$SIZE
      fi
    fi
    sleep 0.3
  done
) &
POLL_PID=$!

dosbox -conf /etc/dosbox/dosbox.conf "$@" 2>&1 \
  | stdbuf -oL grep -vE '^(DOSBox version|Copyright 2002|---$|CONFIG:|MIDI:|MIXER:|ALSA:|SHELL:Redirect)'
EXIT=${PIPESTATUS[0]}

# Give the poller a moment to pick up the final lines (Done. etc.), then
# stop it.
sleep 1
kill "$POLL_PID" 2>/dev/null
wait "$POLL_PID" 2>/dev/null

exit $EXIT
