package com.rulecity.aggregation.handler;

import com.rulecity.aggregation.ItemAggregator;
import com.rulecity.aggregation.LedataChunkLifecycle;
import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemLEDATA;

public class LedataItemAggregator implements ItemAggregator
{
    private final LedataChunkLifecycle lifecycle;

    public LedataItemAggregator(LedataChunkLifecycle lifecycle)
    {
        this.lifecycle = lifecycle;
    }

    @Override
    public void aggregate(OMFItem item, AggregationState state)
    {
        lifecycle.openNewChunk((OMFItemLEDATA) item, state);
    }
}
