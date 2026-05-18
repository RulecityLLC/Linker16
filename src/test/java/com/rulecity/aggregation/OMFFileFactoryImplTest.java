package com.rulecity.aggregation;

import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class OMFFileFactoryImplTest
{
    @Mock private ItemAggregatorDispatcher dispatcher;
    @Mock private LedataChunkLifecycle ledataChunkLifecycle;
    @Mock private OMFItem item1;
    @Mock private OMFItem item2;
    @InjectMocks private OMFFileFactoryImpl factory;

    @Test
    public void dispatchesEachItem_thenClosesOpenChunk()
    {
        OMFFile result = factory.build(List.of(item1, item2), "test.obj");

        assertNotNull(result);
        InOrder order = inOrder(dispatcher, ledataChunkLifecycle);
        order.verify(dispatcher, times(2)).dispatch(any(OMFItem.class), any(AggregationState.class));
        order.verify(ledataChunkLifecycle, times(1)).closeOpenChunk(any(AggregationState.class));
    }

    @Test
    public void emptyItemList_stillFinalizesLedataLifecycle()
    {
        factory.build(List.of(), "test.obj");

        // No dispatch calls but closeOpenChunk still happens.
        org.mockito.Mockito.verifyNoInteractions(dispatcher);
        org.mockito.Mockito.verify(ledataChunkLifecycle, times(1)).closeOpenChunk(any(AggregationState.class));
    }
}
