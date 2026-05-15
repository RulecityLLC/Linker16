package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.SegmentPiece;
import com.rulecity.parse.OMFItemFIXUPP.FixupMethodTarget;
import com.rulecity.parse.OMFItemFIXUPP.Location;
import com.rulecity.parse.OMFItemSEGDEF;
import com.rulecity.parse.data.Communal;
import com.rulecity.parse.data.ExternalNamesDefinition;
import com.rulecity.parse.data.ExternalOrRelated;
import com.rulecity.parse.data.FixupProcessed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TargetResolverImplTest
{
    @Mock private OMFFile module;
    @Mock private PieceLookup lookup;
    @Mock private GlobalSymbolTable symbols;

    private final TargetResolverImpl resolver = new TargetResolverImpl();

    private static FixupProcessed segdefTarget(int segIdx, Integer disp, boolean withDisp)
    {
        return new FixupProcessed(true, Location.OFFSET_16BIT, 0,
                null,
                null,
                null,
                withDisp ? FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT
                         : FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF,
                null, null, null,
                segIdx, null, null,
                disp);
    }

    private static FixupProcessed grpdefTarget(Integer disp, boolean withDisp)
    {
        return new FixupProcessed(true, Location.OFFSET_16BIT, 0,
                null, null, null,
                withDisp ? FixupMethodTarget.TARGET_SPECIFIED_BY_GRPDEF_WITH_DISPLACEMENT
                         : FixupMethodTarget.TARGET_SPECIFIED_BY_GRPDEF,
                null, null, null,
                null, 0, null,
                disp);
    }

    private static FixupProcessed extdefTarget(int extIdx, Integer disp, boolean withDisp)
    {
        return new FixupProcessed(true, Location.OFFSET_16BIT, 0,
                null, null, null,
                withDisp ? FixupMethodTarget.TARGET_SPECIFIED_BY_EXTDEF_WITH_DISPLACEMENT
                         : FixupMethodTarget.TARGET_SPECIFIED_BY_EXTDEF,
                null, null, null,
                null, null, extIdx,
                disp);
    }

    @Test
    public void segdefTargetUsesCombinedPlusPieceOffsetPlusDisplacement()
    {
        // Combined _data at 0x55B2; module's piece at piece-offset 0x0E (after clib).
        // Target SEGDEF idx 0 with displacement 0x4C → 0x55B2 + 0x0E + 0x4C = 0x560C.
        CombinedSegment data = new CombinedSegment("_data", "data",
                OMFItemSEGDEF.Combination.PUBLIC, 1, 0x55B2, 0x499D, List.of());
        SegmentPiece piece = new SegmentPiece(2, "dl2prod2", 0, 0x0E, 0x3A3);
        when(lookup.find(2, 0)).thenReturn(new PieceLookup.Placement(data, piece));

        Integer r = resolver.imageOffset(segdefTarget(0, 0x4C, true), 2, module, lookup, symbols);
        assertEquals(0x55B2 + 0x0E + 0x4C, r);
    }

    @Test
    public void segdefTargetWithoutDisplacement_treatsDisplacementAsZero()
    {
        CombinedSegment text = new CombinedSegment("_text", "code",
                OMFItemSEGDEF.Combination.PUBLIC, 1, 0x112, 0x100, List.of());
        SegmentPiece piece = new SegmentPiece(0, "A", 0, 0, 0x100);
        when(lookup.find(0, 0)).thenReturn(new PieceLookup.Placement(text, piece));

        Integer r = resolver.imageOffset(segdefTarget(0, null, false), 0, module, lookup, symbols);
        assertEquals(0x112, r);
    }

    @Test
    public void grpdefTargetReturnsDisplacementOnly_dgroupStartsAtImageZero()
    {
        // GRPDEF target = DGROUP-relative offset. DGROUP starts at image 0.
        Integer r = resolver.imageOffset(grpdefTarget(0x9876, true), 0, module, lookup, symbols);
        assertEquals(0x9876, r);
    }

    @Test
    public void extdefTargetResolvesViaGlobalSymbolTable()
    {
        when(module.getExternals()).thenReturn(List.of(
                new ExternalOrRelated(null, new ExternalNamesDefinition("foo", 0), null)));
        when(symbols.lookup("foo")).thenReturn(0x2000);

        Integer r = resolver.imageOffset(extdefTarget(0, 0x10, true), 0, module, lookup, symbols);
        assertEquals(0x2010, r);
    }

    @Test
    public void extdefTargetReturnsNullWhenSymbolNotInTable()
    {
        when(module.getExternals()).thenReturn(List.of(
                new ExternalOrRelated(null, new ExternalNamesDefinition("ghost", 0), null)));
        when(symbols.lookup("ghost")).thenReturn(null);

        Integer r = resolver.imageOffset(extdefTarget(0, 0, false), 0, module, lookup, symbols);
        assertNull(r);
    }

    @Test
    public void extdefTargetUsesCommunalNameWhenEntryIsCommunal()
    {
        when(module.getExternals()).thenReturn(List.of(
                new ExternalOrRelated(new Communal("_bigbuf", 0x100), null, null)));
        when(symbols.lookup("_bigbuf")).thenReturn(0xA050);

        Integer r = resolver.imageOffset(extdefTarget(0, 0, false), 0, module, lookup, symbols);
        assertEquals(0xA050, r);
    }

    @Test
    public void segdefTargetWithUnplacedSegment_throws()
    {
        when(module.getModuleName()).thenReturn("M");
        when(lookup.find(0, 7)).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> resolver.imageOffset(segdefTarget(7, 0, true), 0, module, lookup, symbols));
    }
}
