package com.rulecity.aggregation.handler;

import com.rulecity.aggregation.ItemAggregator;
import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemTHEADR;

public class TheadrItemAggregator implements ItemAggregator
{
    @Override
    public void aggregate(OMFItem item, AggregationState state)
    {
        OMFItemTHEADR t = (OMFItemTHEADR) item;
        state.setModuleName(t.getDataString());
    }
}
