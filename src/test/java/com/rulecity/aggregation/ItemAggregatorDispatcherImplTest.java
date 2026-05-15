package com.rulecity.aggregation;

import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemCOMENT;
import com.rulecity.parse.OMFItemLNAMES;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ItemAggregatorDispatcherImplTest
{
    @Mock private ItemAggregator lnamesAggregator;
    @Mock private ItemAggregator commentAggregator;
    @Mock private OMFItemLNAMES lnamesItem;
    @Mock private OMFItemCOMENT commentItem;
    @Mock private OMFItem unknownItem;
    @Mock private AggregationState state;

    @Test
    public void dispatchesByInterfaceType()
    {
        Map<Class<?>, ItemAggregator> map = new LinkedHashMap<>();
        map.put(OMFItemLNAMES.class, lnamesAggregator);
        map.put(OMFItemCOMENT.class, commentAggregator);
        ItemAggregatorDispatcherImpl dispatcher = new ItemAggregatorDispatcherImpl(map);

        dispatcher.dispatch(lnamesItem, state);

        verify(lnamesAggregator, times(1)).aggregate(lnamesItem, state);
        verify(commentAggregator, never()).aggregate(lnamesItem, state);
    }

    @Test
    public void unknownType_throws()
    {
        Map<Class<?>, ItemAggregator> map = new LinkedHashMap<>();
        map.put(OMFItemLNAMES.class, lnamesAggregator);
        ItemAggregatorDispatcherImpl dispatcher = new ItemAggregatorDispatcherImpl(map);

        assertThrows(RuntimeException.class, () -> dispatcher.dispatch(unknownItem, state));
        verify(lnamesAggregator, never()).aggregate(unknownItem, state);
    }
}
