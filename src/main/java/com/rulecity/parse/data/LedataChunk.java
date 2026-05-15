package com.rulecity.parse.data;

import java.util.List;

/**
 * One LEDATA record's bytes plus any FIXUPPs that apply to it.
 *
 * @param segmentIdx       0-based index into the module's SEGDEF list.
 * @param offsetInSegment  Byte offset within the segment where {@code bytes} begin.
 * @param bytes            Raw initialized data from the OBJ.
 * @param fixups           FIXUPP entries that target this LEDATA, with thread refs
 *                         already resolved (so {@link FixupProcessed#methodFrame()} and
 *                         {@link FixupProcessed#methodTarget()} plus the idx fields are
 *                         always populated; the threadFieldContaining... fields are null).
 */
public record LedataChunk(int segmentIdx,
                          int offsetInSegment,
                          byte[] bytes,
                          List<FixupProcessed> fixups)
{
}
