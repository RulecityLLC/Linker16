package com.rulecity.aggregation;

import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;

import java.util.List;

public class OMFFileFactoryImpl implements OMFFileFactory
{
    private final ItemAggregatorDispatcher dispatcher;
    private final LedataChunkLifecycle ledataChunkLifecycle;

    public OMFFileFactoryImpl(ItemAggregatorDispatcher dispatcher,
                              LedataChunkLifecycle ledataChunkLifecycle)
    {
        this.dispatcher = dispatcher;
        this.ledataChunkLifecycle = ledataChunkLifecycle;
    }

    @Override
    public OMFFile build(List<OMFItem> items)
    {
        AggregationState state = new AggregationState();
        for (OMFItem item : items)
        {
            dispatcher.dispatch(item, state);
        }
        ledataChunkLifecycle.closeOpenChunk(state);
        return new OMFFileImpl(state);
    }
}
