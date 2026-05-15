package com.rulecity.aggregation.handler;

import com.rulecity.aggregation.ItemAggregator;
import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemSEGDEF;

public class SegdefItemAggregator implements ItemAggregator
{
    @Override
    public void aggregate(OMFItem item, AggregationState state)
    {
        OMFItemSEGDEF s = (OMFItemSEGDEF) item;
        state.getSegmentDefs().add(s.getProcessed(state.getLnames()));
    }
}
