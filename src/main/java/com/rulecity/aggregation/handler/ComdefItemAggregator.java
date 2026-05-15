package com.rulecity.aggregation.handler;

import com.rulecity.aggregation.ItemAggregator;
import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemCOMDEF;
import com.rulecity.parse.data.ExternalOrRelated;

public class ComdefItemAggregator implements ItemAggregator
{
    @Override
    public void aggregate(OMFItem item, AggregationState state)
    {
        OMFItemCOMDEF c = (OMFItemCOMDEF) item;
        state.getExternals().addAll(c.getCommualList().stream()
                .map(x -> new ExternalOrRelated(x, null, null)).toList());
    }
}
