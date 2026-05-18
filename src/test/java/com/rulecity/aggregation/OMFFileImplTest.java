package com.rulecity.aggregation;

import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItemSEGDEF;
import com.rulecity.parse.data.Communal;
import com.rulecity.parse.data.ExternalOrRelated;
import com.rulecity.parse.data.GroupDefProcessed;
import com.rulecity.parse.data.LedataChunk;
import com.rulecity.parse.data.PublicNamesDefinitionProcessed;
import com.rulecity.parse.data.SegmentDefProcessed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OMFFileImplTest
{
    @Test
    public void exposesEachUnderlyingStateField()
    {
        AggregationState state = new AggregationState();
        state.setModuleName("MOD.OBJ");
        state.getLnames().add("seg");
        SegmentDefProcessed seg = new SegmentDefProcessed(
                10, OMFItemSEGDEF.Alignment.WORD_ALIGNED, OMFItemSEGDEF.Combination.PUBLIC, "seg", "cls");
        state.getSegmentDefs().add(seg);
        GroupDefProcessed grp = new GroupDefProcessed("DGROUP", List.of(seg));
        state.getGroupDefs().add(grp);
        PublicNamesDefinitionProcessed pub = new PublicNamesDefinitionProcessed(
                null, 0, null, List.of(), false);
        state.getPublicSymbols().add(pub);
        ExternalOrRelated ext = new ExternalOrRelated(
                new Communal("c", 1), null, null);
        state.getExternals().add(ext);
        LedataChunk chunk = new LedataChunk(0, 0, new byte[]{1}, List.of());
        state.getLedataChunks().add(chunk);

        OMFFileImpl file = new OMFFileImpl(state, "test.obj");

        assertEquals("MOD.OBJ", file.getModuleName());
        assertEquals(List.of("seg"), file.getLnames());
        assertEquals(List.of(seg), file.getSegmentDefs());
        assertEquals(List.of(grp), file.getGroupDefs());
        assertEquals(List.of(pub), file.getPublicSymbols());
        assertEquals(List.of(ext), file.getExternals());
        assertEquals(List.of(chunk), file.getLedataChunks());
    }

    @Test
    public void allListGettersReturnUnmodifiableViews()
    {
        AggregationState state = new AggregationState();
        OMFFileImpl file = new OMFFileImpl(state, "test.obj");

        // Each list getter must reject mutation.
        assertThrows(UnsupportedOperationException.class, () -> file.getLnames().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> file.getSegmentDefs().add(null));
        assertThrows(UnsupportedOperationException.class, () -> file.getGroupDefs().add(null));
        assertThrows(UnsupportedOperationException.class, () -> file.getPublicSymbols().add(null));
        assertThrows(UnsupportedOperationException.class, () -> file.getExternals().add(null));
        assertThrows(UnsupportedOperationException.class, () -> file.getLedataChunks().add(null));
    }

    @Test
    public void emptyState_returnsEmptyListsAndEmptyName()
    {
        OMFFileImpl file = new OMFFileImpl(new AggregationState(), "test.obj");
        assertEquals("", file.getModuleName());
        assertEquals(0, file.getLnames().size());
        assertEquals(0, file.getSegmentDefs().size());
        assertEquals(0, file.getGroupDefs().size());
        assertEquals(0, file.getPublicSymbols().size());
        assertEquals(0, file.getExternals().size());
        assertEquals(0, file.getLedataChunks().size());
    }

    @Test
    public void changeToBackingStateIsVisibleThroughViews()
    {
        AggregationState state = new AggregationState();
        OMFFileImpl file = new OMFFileImpl(state, "test.obj");
        state.getLnames().add("after-construction");
        assertEquals(List.of("after-construction"), file.getLnames());
    }

    @Test
    public void delegationDoesNotCopy()
    {
        AggregationState state = new AggregationState();
        ExternalOrRelated ext = new ExternalOrRelated(new Communal("x", 1), null, null);
        state.getExternals().add(ext);
        OMFFileImpl file = new OMFFileImpl(state, "test.obj");
        assertSame(ext, file.getExternals().get(0));
    }
}
