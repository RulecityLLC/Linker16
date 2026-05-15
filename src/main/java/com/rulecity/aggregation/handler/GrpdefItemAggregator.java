package com.rulecity.aggregation.handler;

import com.rulecity.aggregation.ItemAggregator;
import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemGRPDEF;
import com.rulecity.parse.data.GroupDef;
import com.rulecity.parse.data.GroupDefProcessed;
import com.rulecity.parse.data.SegmentDefProcessed;

import java.util.List;

public class GrpdefItemAggregator implements ItemAggregator
{
    @Override
    public void aggregate(OMFItem item, AggregationState state)
    {
        OMFItemGRPDEF g = (OMFItemGRPDEF) item;
        GroupDef groupDef = g.getGroupDef();
        String nameGroup = state.getLnames().get(groupDef.grpNameIdx());
        List<SegmentDefProcessed> lstSegDefsProcessed = groupDef.lstSegDefIndices().stream()
                .map(state.getSegmentDefs()::get)
                .toList();
        state.getGroupDefs().add(new GroupDefProcessed(nameGroup, lstSegDefsProcessed));
    }
}
