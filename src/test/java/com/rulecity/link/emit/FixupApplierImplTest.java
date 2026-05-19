package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.SegmentPiece;
import com.rulecity.parse.OMFItemFIXUPP.FixupMethodFrame;
import com.rulecity.parse.OMFItemFIXUPP.FixupMethodTarget;
import com.rulecity.parse.OMFItemFIXUPP.Location;
import com.rulecity.parse.OMFItemSEGDEF;
import com.rulecity.parse.data.Communal;
import com.rulecity.parse.data.ExternalNamesDefinition;
import com.rulecity.parse.data.ExternalOrRelated;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.LedataChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FixupApplierImplTest
{
    @Mock private FrameResolver frameResolver;
    @Mock private TargetResolver targetResolver;
    @Mock private FixupValueWriter valueWriter;
    @Mock private OMFFile moduleA;
    @Mock private PieceLookup lookup;
    @Mock private GlobalSymbolTable symbols;
    @InjectMocks private FixupApplierImpl applier;

    private static FixupProcessed grpdefFixup(int dataRecordOffset)
    {
        return new FixupProcessed(true, Location.OFFSET_16BIT, dataRecordOffset,
                null, FixupMethodFrame.FRAME_SPECIFIED_BY_GRPDEF,
                null, FixupMethodTarget.TARGET_SPECIFIED_BY_GRPDEF_WITH_DISPLACEMENT,
                null, 0, null, null, 0, null, 0x1234);
    }

    private static FixupProcessed extdefFixup(int dataRecordOffset, Integer extIdx)
    {
        return new FixupProcessed(true, Location.OFFSET_16BIT, dataRecordOffset,
                null, FixupMethodFrame.FRAME_SPECIFIED_BY_EXTDEF,
                null, FixupMethodTarget.TARGET_SPECIFIED_BY_EXTDEF,
                null, null, extIdx, null, null, extIdx, 0);
    }

    private void wireUnresolvedFixup(ExternalOrRelated external, Integer extIdx)
    {
        CombinedSegment text = new CombinedSegment("_text", "code",
                OMFItemSEGDEF.Combination.PUBLIC, 1, 0, 0x10, List.of());
        SegmentPiece piece = new SegmentPiece(0, "A", 0, 0, 0x10);
        when(moduleA.getLedataChunks()).thenReturn(List.of(
                new LedataChunk(0, 0, new byte[16], List.of(extdefFixup(0, extIdx)))));
        when(moduleA.getExternals()).thenReturn(List.of(external));
        when(lookup.find(0, 0)).thenReturn(new PieceLookup.Placement(text, piece));
        when(targetResolver.imageOffset(any(), anyInt(), any(), any(), any())).thenReturn(null);
    }

    @Test
    public void resolvedFixupTriggersValueWrite()
    {
        byte[] image = new byte[0x100];

        CombinedSegment text = new CombinedSegment("_text", "code",
                OMFItemSEGDEF.Combination.PUBLIC, 1, 0x10, 0x80, List.of());
        SegmentPiece piece = new SegmentPiece(0, "A", 0, 0x20, 0x40);
        when(moduleA.getLedataChunks()).thenReturn(List.of(
                new LedataChunk(0, 0x04, new byte[16], List.of(grpdefFixup(0x06)))));
        when(lookup.find(0, 0)).thenReturn(new PieceLookup.Placement(text, piece));
        when(targetResolver.imageOffset(any(), eq(0), eq(moduleA), eq(lookup), eq(symbols)))
                .thenReturn(0x1234);
        when(frameResolver.paragraph(any(), eq(0), eq(moduleA), eq(lookup), eq(symbols), eq(0x1234)))
                .thenReturn(0x0008);

        List<FixupApplier.Unresolved> unresolved =
                applier.apply(image, List.of(moduleA), lookup, symbols);

        assertEquals(List.of(), unresolved);
        // Expected location = 0x10 (combined) + 0x20 (piece) + 0x04 (chunk) + 0x06 (fixup) = 0x3A
        verify(valueWriter).write(image, 0x3A, Location.OFFSET_16BIT, true, 0x0008, 0x1234);
    }

    @Test
    public void unresolvedExtdefTargetReportsSymbolNameAndModule()
    {
        byte[] image = new byte[0x100];
        when(moduleA.getSourceFilename()).thenReturn("ASMLIB.OBJ");
        wireUnresolvedFixup(
                new ExternalOrRelated(null, new ExternalNamesDefinition("_printf", 0), null), 0);

        List<FixupApplier.Unresolved> unresolved =
                applier.apply(image, List.of(moduleA), lookup, symbols);

        assertEquals(List.of(new FixupApplier.Unresolved("_printf", "ASMLIB.OBJ")), unresolved);
        verifyNoInteractions(valueWriter);
    }

    @Test
    public void unresolvedLextdefTarget_reportsLocalExternalName()
    {
        byte[] image = new byte[0x100];
        when(moduleA.getSourceFilename()).thenReturn("MOD.OBJ");
        wireUnresolvedFixup(
                new ExternalOrRelated(null, null, new ExternalNamesDefinition("_local", 0)), 0);

        List<FixupApplier.Unresolved> unresolved =
                applier.apply(image, List.of(moduleA), lookup, symbols);

        assertEquals(List.of(new FixupApplier.Unresolved("_local", "MOD.OBJ")), unresolved);
    }

    @Test
    public void unresolvedComdefTarget_reportsCommunalName()
    {
        byte[] image = new byte[0x100];
        when(moduleA.getSourceFilename()).thenReturn("MOD.OBJ");
        wireUnresolvedFixup(
                new ExternalOrRelated(new Communal("_shared", 4), null, null), 0);

        List<FixupApplier.Unresolved> unresolved =
                applier.apply(image, List.of(moduleA), lookup, symbols);

        assertEquals(List.of(new FixupApplier.Unresolved("_shared", "MOD.OBJ")), unresolved);
    }

    @Test
    public void unresolvedTarget_withNullExternalIndex_reportsUnknown()
    {
        byte[] image = new byte[0x100];
        // Fixup carries no idxExternalTarget — externalName short-circuits on the
        // null check and never consults the externals list.
        CombinedSegment text = new CombinedSegment("_text", "code",
                OMFItemSEGDEF.Combination.PUBLIC, 1, 0, 0x10, List.of());
        SegmentPiece piece = new SegmentPiece(0, "A", 0, 0, 0x10);
        when(moduleA.getLedataChunks()).thenReturn(List.of(
                new LedataChunk(0, 0, new byte[16], List.of(extdefFixup(0, null)))));
        when(moduleA.getSourceFilename()).thenReturn("MOD.OBJ");
        when(lookup.find(0, 0)).thenReturn(new PieceLookup.Placement(text, piece));
        when(targetResolver.imageOffset(any(), anyInt(), any(), any(), any())).thenReturn(null);

        List<FixupApplier.Unresolved> unresolved =
                applier.apply(image, List.of(moduleA), lookup, symbols);

        assertEquals(List.of(new FixupApplier.Unresolved("<unknown>", "MOD.OBJ")), unresolved);
    }

    @Test
    public void unresolvedTarget_withAllExternalSlotsNull_reportsUnknown()
    {
        byte[] image = new byte[0x100];
        when(moduleA.getSourceFilename()).thenReturn("MOD.OBJ");
        // Pathological ExternalOrRelated where none of the three sub-records is populated —
        // externalName falls through every branch and returns the placeholder.
        wireUnresolvedFixup(new ExternalOrRelated(null, null, null), 0);

        List<FixupApplier.Unresolved> unresolved =
                applier.apply(image, List.of(moduleA), lookup, symbols);

        assertEquals(List.of(new FixupApplier.Unresolved("<unknown>", "MOD.OBJ")), unresolved);
    }

    @Test
    public void chunkWithNoFixups_makesNoResolverCalls()
    {
        byte[] image = new byte[0x100];

        CombinedSegment text = new CombinedSegment("_text", "code",
                OMFItemSEGDEF.Combination.PUBLIC, 1, 0, 0x10, List.of());
        SegmentPiece piece = new SegmentPiece(0, "A", 0, 0, 0x10);
        when(moduleA.getLedataChunks()).thenReturn(List.of(
                new LedataChunk(0, 0, new byte[16], List.of())));
        when(lookup.find(0, 0)).thenReturn(new PieceLookup.Placement(text, piece));

        List<FixupApplier.Unresolved> unresolved =
                applier.apply(image, List.of(moduleA), lookup, symbols);

        assertEquals(List.of(), unresolved);
        verifyNoInteractions(targetResolver, frameResolver, valueWriter);
    }
}
