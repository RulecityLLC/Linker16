package com.rulecity.aggregation;

import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;

/**
 * Folds one OMFItem of a known concrete type into the aggregation state.
 * Implementations are paired with the OMFItem interface they handle via the
 * dispatcher's type-to-aggregator map.
 */
public interface ItemAggregator
{
    void aggregate(OMFItem item, AggregationState state);
}
