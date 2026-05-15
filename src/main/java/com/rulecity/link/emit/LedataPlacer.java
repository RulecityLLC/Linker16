package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;

import java.util.List;

/**
 * Copies the bytes of every {@code LedataChunk} from every module into the
 * image buffer at the chunk's computed image position. FIXUPPs are NOT
 * applied here — that's {@code FixupApplier}'s job.
 */
public interface LedataPlacer
{
    void place(byte[] image, List<OMFFile> modulesInInputOrder, PieceLookup lookup);
}
