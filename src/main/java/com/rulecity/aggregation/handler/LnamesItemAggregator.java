package com.rulecity.aggregation.handler;

import com.rulecity.aggregation.ItemAggregator;
import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemLNAMES;

public class LnamesItemAggregator implements ItemAggregator
{
    @Override
    public void aggregate(OMFItem item, AggregationState state)
    {
        // TIS 1.1 allows multiple LNAMES; concatenate in order.
        OMFItemLNAMES n = (OMFItemLNAMES) item;
        state.getLnames().addAll(n.getNames());
    }
}
