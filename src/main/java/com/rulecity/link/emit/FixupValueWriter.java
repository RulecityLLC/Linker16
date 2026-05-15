package com.rulecity.link.emit;

import com.rulecity.parse.OMFItemFIXUPP.Location;

/**
 * Writes the resolved value of a single FIXUPP into the image buffer at the
 * fixup's location, with the correct byte width and addressing semantics
 * (segment-relative vs. self-relative).
 */
public interface FixupValueWriter
{
    void write(byte[] image,
               int locationImageOffset,
               Location location,
               boolean segmentRelative,
               int framePara,
               int targetImageOffset);
}
