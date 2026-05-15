package com.rulecity.aggregation;

import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItemLEDATA;
import com.rulecity.parse.data.LedataChunk;

import java.util.ArrayList;
import java.util.Collections;

public class LedataChunkLifecycleImpl implements LedataChunkLifecycle
{
    @Override
    public void openNewChunk(OMFItemLEDATA itemLEDATA, AggregationState state)
    {
        closeOpenChunk(state);
        // LEDATA's segment index is 1-based on the wire; convert to 0-based here.
        state.setCurrentLedataSegmentIdx(itemLEDATA.getSegmentIdx() - 1);
        state.setCurrentLedataOffset(itemLEDATA.getEnumeratedDataOffset());
        state.setCurrentLedataBytes(itemLEDATA.getBytes());
        state.setCurrentLedataFixups(new ArrayList<>());
    }

    @Override
    public void closeOpenChunk(AggregationState state)
    {
        if (state.getCurrentLedataBytes() == null) return;
        state.getLedataChunks().add(new LedataChunk(state.getCurrentLedataSegmentIdx(),
                state.getCurrentLedataOffset(),
                state.getCurrentLedataBytes(),
                Collections.unmodifiableList(state.getCurrentLedataFixups())));
        state.setCurrentLedataSegmentIdx(-1);
        state.setCurrentLedataOffset(-1);
        state.setCurrentLedataBytes(null);
        state.setCurrentLedataFixups(null);
    }
}
