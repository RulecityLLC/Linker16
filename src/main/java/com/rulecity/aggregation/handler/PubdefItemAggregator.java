package com.rulecity.aggregation.handler;

import com.rulecity.aggregation.ItemAggregator;
import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemPUBDEF;

public class PubdefItemAggregator implements ItemAggregator
{
    @Override
    public void aggregate(OMFItem item, AggregationState state)
    {
        OMFItemPUBDEF p = (OMFItemPUBDEF) item;
        state.getPublicSymbols().add(p.getDef());
    }
}
