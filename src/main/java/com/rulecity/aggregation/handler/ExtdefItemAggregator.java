package com.rulecity.aggregation.handler;

import com.rulecity.aggregation.ItemAggregator;
import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemEXTDEF;
import com.rulecity.parse.data.ExternalOrRelated;

public class ExtdefItemAggregator implements ItemAggregator
{
    @Override
    public void aggregate(OMFItem item, AggregationState state)
    {
        OMFItemEXTDEF e = (OMFItemEXTDEF) item;
        if (e.isLEXTDEF())
        {
            state.getExternals().addAll(e.getExternalNamesDefinitions().stream()
                    .map(x -> new ExternalOrRelated(null, null, x)).toList());
        }
        else
        {
            state.getExternals().addAll(e.getExternalNamesDefinitions().stream()
                    .map(x -> new ExternalOrRelated(null, x, null)).toList());
        }
    }
}
