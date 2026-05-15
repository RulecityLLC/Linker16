package com.rulecity.aggregation.state;

import com.rulecity.parse.OMFItemFIXUPP;
import com.rulecity.parse.data.ExternalNamesDefinition;
import com.rulecity.parse.data.ExternalOrRelated;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.GroupDefProcessed;
import com.rulecity.parse.data.LedataChunk;
import com.rulecity.parse.data.PublicNamesDefinitionProcessed;
import com.rulecity.parse.data.ThreadProcessed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AggregationStateTest
{
    @Test
    public void initialState_hasEmptyDefaults()
    {
        AggregationState s = new AggregationState();
        assertEquals("", s.getModuleName());
        assertEquals(0, s.getLnames().size());
        assertEquals(0, s.getSegmentDefs().size());
        assertEquals(0, s.getGroupDefs().size());
        assertEquals(0, s.getPublicSymbols().size());
        assertEquals(0, s.getExternals().size());
        assertEquals(0, s.getLedataChunks().size());
        assertEquals(4, s.getFrameThreads().length);
        assertEquals(4, s.getTargetThreads().length);
        for (int i = 0; i < 4; i++)
        {
            assertNull(s.getFrameThreads()[i]);
            assertNull(s.getTargetThreads()[i]);
        }
        assertEquals(-1, s.getCurrentLedataSegmentIdx());
        assertEquals(-1, s.getCurrentLedataOffset());
        assertNull(s.getCurrentLedataBytes());
        assertNull(s.getCurrentLedataFixups());
    }

    @Test
    public void moduleNameSetter_roundTrip()
    {
        AggregationState s = new AggregationState();
        s.setModuleName("foo.obj");
        assertEquals("foo.obj", s.getModuleName());
    }

    @Test
    public void currentLedataSetters_roundTrip()
    {
        AggregationState s = new AggregationState();
        byte[] bytes = {1, 2, 3};
        java.util.List<FixupProcessed> fixups = new java.util.ArrayList<>();

        s.setCurrentLedataSegmentIdx(7);
        s.setCurrentLedataOffset(0x42);
        s.setCurrentLedataBytes(bytes);
        s.setCurrentLedataFixups(fixups);

        assertEquals(7, s.getCurrentLedataSegmentIdx());
        assertEquals(0x42, s.getCurrentLedataOffset());
        assertArrayEquals(bytes, s.getCurrentLedataBytes());
        assertSame(fixups, s.getCurrentLedataFixups());
    }

    @Test
    public void mutableLists_actuallyMutable()
    {
        AggregationState s = new AggregationState();
        s.getLnames().add("foo");
        s.getGroupDefs().add(new GroupDefProcessed("g", List.of()));
        s.getPublicSymbols().add(new PublicNamesDefinitionProcessed(null, 0, null, List.of(), false));
        s.getExternals().add(new ExternalOrRelated(null,
                new ExternalNamesDefinition("n", 0), null));
        s.getLedataChunks().add(new LedataChunk(0, 0, new byte[]{0}, List.of()));

        assertEquals(List.of("foo"), s.getLnames());
        assertEquals(1, s.getGroupDefs().size());
        assertEquals(1, s.getPublicSymbols().size());
        assertEquals(1, s.getExternals().size());
        assertEquals(1, s.getLedataChunks().size());
    }

    @Test
    public void threadSlots_canBeAssignedAndRead()
    {
        AggregationState s = new AggregationState();
        ThreadProcessed t = new ThreadProcessed(null,
                OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF, 2, 5, null, null);
        s.getFrameThreads()[2] = t;
        assertNotNull(s.getFrameThreads()[2]);
        assertSame(t, s.getFrameThreads()[2]);

        s.getTargetThreads()[0] = t;
        assertSame(t, s.getTargetThreads()[0]);
    }
}
