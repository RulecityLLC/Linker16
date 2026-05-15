package com.rulecity.aggregation;

import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItemFIXUPP;
import com.rulecity.parse.OMFItemLEDATA;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.LedataChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LedataChunkLifecycleImplTest
{
    @Mock private OMFItemLEDATA ledataItem;

    @Test
    public void openNewChunk_seedsCurrentChunkFromLedataAndConvertsSegmentIdxToZeroBased()
    {
        AggregationState state = new AggregationState();
        when(ledataItem.getSegmentIdx()).thenReturn(3);
        when(ledataItem.getEnumeratedDataOffset()).thenReturn(0x100);
        when(ledataItem.getBytes()).thenReturn(new byte[]{1, 2, 3});

        new LedataChunkLifecycleImpl().openNewChunk(ledataItem, state);

        assertEquals(2, state.getCurrentLedataSegmentIdx()); // 3-1
        assertEquals(0x100, state.getCurrentLedataOffset());
        assertArrayEquals(new byte[]{1, 2, 3}, state.getCurrentLedataBytes());
        assertNotNull(state.getCurrentLedataFixups());
        assertEquals(0, state.getCurrentLedataFixups().size());
        assertEquals(0, state.getLedataChunks().size()); // first openNewChunk has nothing to close
    }

    @Test
    public void openNewChunk_closesPreviouslyOpenChunkBeforeOpening()
    {
        AggregationState state = new AggregationState();
        // Seed an open chunk
        state.setCurrentLedataSegmentIdx(0);
        state.setCurrentLedataOffset(0);
        state.setCurrentLedataBytes(new byte[]{9});
        state.setCurrentLedataFixups(new java.util.ArrayList<>());

        when(ledataItem.getSegmentIdx()).thenReturn(2);
        when(ledataItem.getEnumeratedDataOffset()).thenReturn(0x40);
        when(ledataItem.getBytes()).thenReturn(new byte[]{0x55});

        new LedataChunkLifecycleImpl().openNewChunk(ledataItem, state);

        assertEquals(1, state.getLedataChunks().size());
        LedataChunk closed = state.getLedataChunks().get(0);
        assertEquals(0, closed.segmentIdx());
        assertArrayEquals(new byte[]{9}, closed.bytes());

        // The new chunk is now open
        assertEquals(1, state.getCurrentLedataSegmentIdx());
        assertEquals(0x40, state.getCurrentLedataOffset());
    }

    @Test
    public void closeOpenChunk_noOpenChunk_doesNothing()
    {
        AggregationState state = new AggregationState();
        new LedataChunkLifecycleImpl().closeOpenChunk(state);
        assertEquals(0, state.getLedataChunks().size());
        assertNull(state.getCurrentLedataBytes());
        assertEquals(-1, state.getCurrentLedataSegmentIdx());
        assertEquals(-1, state.getCurrentLedataOffset());
    }

    @Test
    public void closeOpenChunk_resetsCurrentChunkSentinels()
    {
        AggregationState state = new AggregationState();
        state.setCurrentLedataSegmentIdx(7);
        state.setCurrentLedataOffset(0xAB);
        state.setCurrentLedataBytes(new byte[]{0});
        state.setCurrentLedataFixups(new java.util.ArrayList<>());

        new LedataChunkLifecycleImpl().closeOpenChunk(state);

        assertEquals(-1, state.getCurrentLedataSegmentIdx());
        assertEquals(-1, state.getCurrentLedataOffset());
        assertNull(state.getCurrentLedataBytes());
        assertNull(state.getCurrentLedataFixups());
        assertEquals(1, state.getLedataChunks().size());
        assertEquals(7, state.getLedataChunks().get(0).segmentIdx());
        assertEquals(0xAB, state.getLedataChunks().get(0).offsetInSegment());
    }

    @Test
    public void closedChunk_fixupListIsUnmodifiable()
    {
        AggregationState state = new AggregationState();
        state.setCurrentLedataSegmentIdx(0);
        state.setCurrentLedataOffset(0);
        state.setCurrentLedataBytes(new byte[]{0});
        state.setCurrentLedataFixups(new java.util.ArrayList<>());

        new LedataChunkLifecycleImpl().closeOpenChunk(state);

        LedataChunk closed = state.getLedataChunks().get(0);
        assertThrows(UnsupportedOperationException.class,
                () -> closed.fixups().add((FixupProcessed) null));
    }

    @SuppressWarnings("unused")
    private Object unused(OMFItemFIXUPP x) { return x; } // keep import in some IDEs
}
