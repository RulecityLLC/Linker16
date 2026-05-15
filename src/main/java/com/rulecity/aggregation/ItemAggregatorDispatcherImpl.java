package com.rulecity.aggregation;

import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;

import java.util.Map;

public class ItemAggregatorDispatcherImpl implements ItemAggregatorDispatcher
{
    private final Map<Class<?>, ItemAggregator> aggregators;

    /**
     * @param aggregators map from OMFItem interface class to the aggregator that handles it.
     *                    First matching key (by {@code Class.isInstance}) wins.
     */
    public ItemAggregatorDispatcherImpl(Map<Class<?>, ItemAggregator> aggregators)
    {
        this.aggregators = aggregators;
    }

    @Override
    public void dispatch(OMFItem item, AggregationState state)
    {
        for (Map.Entry<Class<?>, ItemAggregator> e : aggregators.entrySet())
        {
            if (e.getKey().isInstance(item))
            {
                e.getValue().aggregate(item, state);
                return;
            }
        }
        throw new RuntimeException("Unknown item type: " + item.getClass().getName());
    }
}
