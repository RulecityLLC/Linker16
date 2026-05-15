package com.rulecity.aggregation;

import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItemLEDATA;

/**
 * Manages the "currently open" LEDATA chunk in the aggregation state.
 * <p>
 * The OMF wire format binds a FIXUPP record to the immediately preceding
 * LEDATA/LIDATA. We model that by keeping one open chunk in the state at a
 * time; a new LEDATA closes the previous one and opens a new one, and a final
 * {@link #closeOpenChunk(AggregationState)} call flushes the last chunk at
 * end-of-stream.
 */
public interface LedataChunkLifecycle
{
    void openNewChunk(OMFItemLEDATA itemLEDATA, AggregationState state);
    void closeOpenChunk(AggregationState state);
}
