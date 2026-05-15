package com.rulecity.aggregation.handler;

import com.rulecity.aggregation.LedataChunkLifecycle;
import com.rulecity.aggregation.ThreadResolver;
import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemCOMDEF;
import com.rulecity.parse.OMFItemEXTDEF;
import com.rulecity.parse.OMFItemFIXUPP;
import com.rulecity.parse.OMFItemGRPDEF;
import com.rulecity.parse.OMFItemLEDATA;
import com.rulecity.parse.OMFItemLNAMES;
import com.rulecity.parse.OMFItemMODEND;
import com.rulecity.parse.OMFItemPUBDEF;
import com.rulecity.parse.OMFItemSEGDEF;
import com.rulecity.parse.OMFItemTHEADR;
import com.rulecity.parse.data.Communal;
import com.rulecity.parse.data.ExternalNamesDefinition;
import com.rulecity.parse.data.FixupOrThreadProcessed;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.GroupDef;
import com.rulecity.parse.data.GroupDefProcessed;
import com.rulecity.parse.data.PublicNamesDefinitionProcessed;
import com.rulecity.parse.data.SegmentDefProcessed;
import com.rulecity.parse.data.ThreadProcessed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AggregatorsTest
{
    @Mock private OMFItemTHEADR theadrItem;
    @Mock private OMFItemLNAMES lnamesItem;
    @Mock private OMFItemSEGDEF segdefItem;
    @Mock private OMFItemGRPDEF grpdefItem;
    @Mock private OMFItemPUBDEF pubdefItem;
    @Mock private OMFItemEXTDEF extdefItem;
    @Mock private OMFItemCOMDEF comdefItem;
    @Mock private OMFItemMODEND modendItem;
    @Mock private OMFItemFIXUPP fixuppItem;
    @Mock private OMFItemLEDATA ledataItem;
    @Mock private LedataChunkLifecycle lifecycle;
    @Mock private ThreadResolver threadResolver;

    @Test
    public void theadr_setsModuleName()
    {
        AggregationState state = new AggregationState();
        when(theadrItem.getDataString()).thenReturn("MOD.OBJ");

        new TheadrItemAggregator().aggregate(theadrItem, state);

        assertEquals("MOD.OBJ", state.getModuleName());
    }

    @Test
    public void comment_isNoOp()
    {
        AggregationState state = new AggregationState();
        OMFItem any = org.mockito.Mockito.mock(OMFItem.class);
        new CommentItemAggregator().aggregate(any, state);
        assertEquals("", state.getModuleName());
        assertEquals(0, state.getLnames().size());
        assertEquals(0, state.getExternals().size());
    }

    @Test
    public void lnames_appendsAllNames()
    {
        AggregationState state = new AggregationState();
        when(lnamesItem.getNames()).thenReturn(List.of("A", "B"));
        new LnamesItemAggregator().aggregate(lnamesItem, state);

        assertEquals(List.of("A", "B"), state.getLnames());

        // A second LNAMES record should APPEND, not replace.
        when(lnamesItem.getNames()).thenReturn(List.of("C"));
        new LnamesItemAggregator().aggregate(lnamesItem, state);
        assertEquals(List.of("A", "B", "C"), state.getLnames());
    }

    @Test
    public void segdef_appendsProcessedToState()
    {
        AggregationState state = new AggregationState();
        state.getLnames().addAll(List.of("seg", "cls"));
        SegmentDefProcessed processed = new SegmentDefProcessed(
                10, OMFItemSEGDEF.Alignment.WORD_ALIGNED,
                OMFItemSEGDEF.Combination.PUBLIC, "seg", "cls");
        when(segdefItem.getProcessed(state.getLnames())).thenReturn(processed);

        new SegdefItemAggregator().aggregate(segdefItem, state);

        assertEquals(1, state.getSegmentDefs().size());
        assertSame(processed, state.getSegmentDefs().get(0));
    }

    @Test
    public void grpdef_resolvesGroupNameAndSegments()
    {
        AggregationState state = new AggregationState();
        state.getLnames().addAll(List.of("DGROUP", "_DATA"));
        SegmentDefProcessed seg = new SegmentDefProcessed(
                100, OMFItemSEGDEF.Alignment.PARAGRAPH_ALIGNED,
                OMFItemSEGDEF.Combination.PUBLIC, "_DATA", "DATA");
        state.getSegmentDefs().add(seg);
        when(grpdefItem.getGroupDef()).thenReturn(new GroupDef(0, List.of(0)));

        new GrpdefItemAggregator().aggregate(grpdefItem, state);

        assertEquals(1, state.getGroupDefs().size());
        GroupDefProcessed gdp = state.getGroupDefs().get(0);
        assertEquals("DGROUP", gdp.name());
        assertEquals(List.of(seg), gdp.lstSegDefs());
    }

    @Test
    public void pubdef_appendsDefToPublicSymbols()
    {
        AggregationState state = new AggregationState();
        PublicNamesDefinitionProcessed proc = new PublicNamesDefinitionProcessed(
                null, 0, null, List.of(), false);
        when(pubdefItem.getDef()).thenReturn(proc);
        new PubdefItemAggregator().aggregate(pubdefItem, state);

        assertEquals(1, state.getPublicSymbols().size());
        assertSame(proc, state.getPublicSymbols().get(0));
    }

    @Test
    public void extdef_external_wrapsAsExternalSlot()
    {
        AggregationState state = new AggregationState();
        when(extdefItem.isLEXTDEF()).thenReturn(false);
        var def = new ExternalNamesDefinition("foo", 0);
        when(extdefItem.getExternalNamesDefinitions()).thenReturn(List.of(def));

        new ExtdefItemAggregator().aggregate(extdefItem, state);

        assertEquals(1, state.getExternals().size());
        assertSame(def, state.getExternals().get(0).external());
        assertNull(state.getExternals().get(0).localExternal());
        assertNull(state.getExternals().get(0).communal());
    }

    @Test
    public void extdef_lextdef_wrapsAsLocalExternalSlot()
    {
        AggregationState state = new AggregationState();
        when(extdefItem.isLEXTDEF()).thenReturn(true);
        var def = new ExternalNamesDefinition("priv", 0);
        when(extdefItem.getExternalNamesDefinitions()).thenReturn(List.of(def));

        new ExtdefItemAggregator().aggregate(extdefItem, state);

        assertEquals(1, state.getExternals().size());
        assertSame(def, state.getExternals().get(0).localExternal());
        assertNull(state.getExternals().get(0).external());
    }

    @Test
    public void comdef_wrapsCommunalsIntoExternalsList()
    {
        AggregationState state = new AggregationState();
        var c1 = new Communal("foo", 10);
        var c2 = new Communal("bar", 20);
        when(comdefItem.getCommualList()).thenReturn(List.of(c1, c2));

        new ComdefItemAggregator().aggregate(comdefItem, state);

        assertEquals(2, state.getExternals().size());
        assertSame(c1, state.getExternals().get(0).communal());
        assertNull(state.getExternals().get(0).external());
        assertNull(state.getExternals().get(0).localExternal());
        assertSame(c2, state.getExternals().get(1).communal());
    }

    @Test
    public void modend_mainModuleThrows()
    {
        when(modendItem.isAMainProgramModule()).thenReturn(true);
        assertThrows(RuntimeException.class,
                () -> new ModendItemAggregator().aggregate(modendItem, new AggregationState()));
    }

    @Test
    public void modend_startAddressThrows()
    {
        when(modendItem.isAMainProgramModule()).thenReturn(false);
        when(modendItem.moduleContainsAStartAddress()).thenReturn(true);
        assertThrows(RuntimeException.class,
                () -> new ModendItemAggregator().aggregate(modendItem, new AggregationState()));
    }

    @Test
    public void modend_neitherFlag_isNoOp()
    {
        when(modendItem.isAMainProgramModule()).thenReturn(false);
        when(modendItem.moduleContainsAStartAddress()).thenReturn(false);
        // Should not throw
        new ModendItemAggregator().aggregate(modendItem, new AggregationState());
    }

    @Test
    public void ledata_delegatesToLifecycleOpenNewChunk()
    {
        AggregationState state = new AggregationState();
        new LedataItemAggregator(lifecycle).aggregate(ledataItem, state);
        verify(lifecycle, times(1)).openNewChunk(ledataItem, state);
    }

    @Test
    public void fixupp_threadDef_storedInFrameSlotWhenMethodFrameSet()
    {
        AggregationState state = new AggregationState();
        ThreadProcessed t = new ThreadProcessed(null, OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF, 2, 5, null, null);
        when(fixuppItem.getFixupsOrThreadsProcessed()).thenReturn(List.of(
                new FixupOrThreadProcessed(t, null)));

        new FixuppItemAggregator(threadResolver).aggregate(fixuppItem, state);

        assertSame(t, state.getFrameThreads()[2]);
        assertNull(state.getTargetThreads()[2]);
    }

    @Test
    public void fixupp_threadDef_storedInTargetSlotWhenMethodFrameNull()
    {
        AggregationState state = new AggregationState();
        ThreadProcessed t = new ThreadProcessed(OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF, null, 1, 5, null, null);
        when(fixuppItem.getFixupsOrThreadsProcessed()).thenReturn(List.of(
                new FixupOrThreadProcessed(t, null)));

        new FixuppItemAggregator(threadResolver).aggregate(fixuppItem, state);

        assertSame(t, state.getTargetThreads()[1]);
        assertNull(state.getFrameThreads()[1]);
    }

    @Test
    public void fixupp_fixupWithNoOpenLedata_throws()
    {
        AggregationState state = new AggregationState();
        FixupProcessed fp = new FixupProcessed(false, OMFItemFIXUPP.Location.OFFSET_16BIT, 0,
                null, OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF,
                null, OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF,
                0, null, null, 0, null, null, null);
        when(fixuppItem.getFixupsOrThreadsProcessed()).thenReturn(List.of(
                new FixupOrThreadProcessed(null, fp)));

        assertThrows(RuntimeException.class,
                () -> new FixuppItemAggregator(threadResolver).aggregate(fixuppItem, state));
        verify(threadResolver, never()).resolve(any(), any(), any());
    }

    @Test
    public void fixupp_fixupWithOpenLedata_addsResolvedFixupToOpenChunk()
    {
        AggregationState state = new AggregationState();
        state.setCurrentLedataFixups(new ArrayList<>());
        FixupProcessed fp = new FixupProcessed(false, OMFItemFIXUPP.Location.OFFSET_16BIT, 0,
                null, OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF,
                null, OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF,
                0, null, null, 0, null, null, null);
        FixupProcessed resolved = new FixupProcessed(false, OMFItemFIXUPP.Location.SEGMENT, 0,
                null, OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF,
                null, OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF,
                0, null, null, 0, null, null, null);
        when(fixuppItem.getFixupsOrThreadsProcessed()).thenReturn(List.of(
                new FixupOrThreadProcessed(null, fp)));
        when(threadResolver.resolve(fp, state.getFrameThreads(), state.getTargetThreads()))
                .thenReturn(resolved);

        new FixuppItemAggregator(threadResolver).aggregate(fixuppItem, state);

        assertEquals(1, state.getCurrentLedataFixups().size());
        assertSame(resolved, state.getCurrentLedataFixups().get(0));
    }

    private static <T> T any() { return org.mockito.ArgumentMatchers.any(); }
}
