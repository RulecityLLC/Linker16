package com.rulecity.aggregation.handler;

import com.rulecity.aggregation.ItemAggregator;
import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemMODEND;

public class ModendItemAggregator implements ItemAggregator
{
    @Override
    public void aggregate(OMFItem item, AggregationState state)
    {
        OMFItemMODEND m = (OMFItemMODEND) item;
        if (m.isAMainProgramModule()) throw new RuntimeException("Unsupported: main-program MODEND");
        if (m.moduleContainsAStartAddress()) throw new RuntimeException("Unsupported: MODEND with start address");
    }
}
