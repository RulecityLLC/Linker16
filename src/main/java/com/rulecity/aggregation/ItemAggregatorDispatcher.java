package com.rulecity.aggregation;

import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;

/**
 * Routes a single OMFItem to the aggregator registered for its concrete (or interface) type.
 */
public interface ItemAggregatorDispatcher
{
    void dispatch(OMFItem item, AggregationState state);
}
