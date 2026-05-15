package com.rulecity.link;

import com.rulecity.parse.OMFItemSEGDEF;

/**
 * Maps a SEGDEF alignment enum to its byte-boundary equivalent.
 */
public interface AlignmentResolver
{
    /** Returns the alignment in bytes (a positive power of two). */
    int toBytes(OMFItemSEGDEF.Alignment alignment);
}
