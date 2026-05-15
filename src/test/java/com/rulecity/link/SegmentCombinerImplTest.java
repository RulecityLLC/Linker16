package com.rulecity.link;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.SegmentPiece;
import com.rulecity.parse.OMFItemSEGDEF;
import com.rulecity.parse.data.SegmentDefProcessed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SegmentCombinerImplTest
{
    @Mock private AlignmentResolver alignmentResolver;
    @Mock private OMFFile moduleA;
    @Mock private OMFFile moduleB;
    @InjectMocks private SegmentCombinerImpl combiner;

    @Test
    public void singleModule_singleSegment_combinesToOnePiece()
    {
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getSegmentDefs()).thenReturn(List.of(
                new SegmentDefProcessed(0x100, OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                        OMFItemSEGDEF.Combination.PUBLIC, "_text", "code")));
        when(alignmentResolver.toBytes(OMFItemSEGDEF.Alignment.BYTE_ALIGNED)).thenReturn(1);

        List<CombinedSegment> result = combiner.combine(List.of(moduleA));

        assertEquals(1, result.size());
        CombinedSegment cs = result.get(0);
        assertEquals("_text", cs.name());
        assertEquals("code", cs.className());
        assertEquals(0x100, cs.totalLength());
        assertEquals(1, cs.alignmentBytes());
        assertEquals(-1, cs.imageOffset(), "imageOffset is set by SegmentLayouter, not here");
        assertEquals(1, cs.pieces().size());
        SegmentPiece p = cs.pieces().get(0);
        assertEquals(0, p.moduleIndex());
        assertEquals("A", p.moduleName());
        assertEquals(0, p.moduleSegmentIdx());
        assertEquals(0, p.offsetWithinCombined());
        assertEquals(0x100, p.length());
    }

    @Test
    public void twoModulesSameSegmentName_concatenatedInInputOrder()
    {
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getSegmentDefs()).thenReturn(List.of(
                new SegmentDefProcessed(0x100, OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                        OMFItemSEGDEF.Combination.PUBLIC, "_text", "code")));
        when(moduleB.getModuleName()).thenReturn("B");
        when(moduleB.getSegmentDefs()).thenReturn(List.of(
                new SegmentDefProcessed(0x80, OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                        OMFItemSEGDEF.Combination.PUBLIC, "_text", "code")));
        when(alignmentResolver.toBytes(OMFItemSEGDEF.Alignment.BYTE_ALIGNED)).thenReturn(1);

        List<CombinedSegment> result = combiner.combine(List.of(moduleA, moduleB));

        assertEquals(1, result.size());
        CombinedSegment cs = result.get(0);
        assertEquals(0x180, cs.totalLength());
        assertEquals(2, cs.pieces().size());
        assertEquals(0, cs.pieces().get(0).offsetWithinCombined());
        assertEquals(0x100, cs.pieces().get(1).offsetWithinCombined());
    }

    @Test
    public void pieceAlignmentPadsBetweenPieces()
    {
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getSegmentDefs()).thenReturn(List.of(
                new SegmentDefProcessed(0x111, OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                        OMFItemSEGDEF.Combination.PUBLIC, "_text", "code")));
        when(moduleB.getModuleName()).thenReturn("B");
        when(moduleB.getSegmentDefs()).thenReturn(List.of(
                new SegmentDefProcessed(0x80, OMFItemSEGDEF.Alignment.WORD_ALIGNED,
                        OMFItemSEGDEF.Combination.PUBLIC, "_text", "code")));
        when(alignmentResolver.toBytes(OMFItemSEGDEF.Alignment.BYTE_ALIGNED)).thenReturn(1);
        when(alignmentResolver.toBytes(OMFItemSEGDEF.Alignment.WORD_ALIGNED)).thenReturn(2);

        List<CombinedSegment> result = combiner.combine(List.of(moduleA, moduleB));

        CombinedSegment cs = result.get(0);
        // 0x111 (odd) → next piece is WORD-aligned, padded to 0x112.
        assertEquals(0, cs.pieces().get(0).offsetWithinCombined());
        assertEquals(0x112, cs.pieces().get(1).offsetWithinCombined());
        assertEquals(0x112 + 0x80, cs.totalLength());
        assertEquals(2, cs.alignmentBytes(), "Combined alignment = max of piece alignments");
    }

    @Test
    public void distinctSegmentNames_orderedByFirstEncounter()
    {
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getSegmentDefs()).thenReturn(List.of(
                new SegmentDefProcessed(0x10, OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                        OMFItemSEGDEF.Combination.PUBLIC, "_text", "code"),
                new SegmentDefProcessed(0x10, OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                        OMFItemSEGDEF.Combination.PUBLIC, "_data", "data")));
        when(moduleB.getModuleName()).thenReturn("B");
        when(moduleB.getSegmentDefs()).thenReturn(List.of(
                new SegmentDefProcessed(0x10, OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                        OMFItemSEGDEF.Combination.PUBLIC, "const", "const"),
                new SegmentDefProcessed(0x10, OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                        OMFItemSEGDEF.Combination.PUBLIC, "_text", "code")));
        when(alignmentResolver.toBytes(OMFItemSEGDEF.Alignment.BYTE_ALIGNED)).thenReturn(1);

        List<CombinedSegment> result = combiner.combine(List.of(moduleA, moduleB));

        assertEquals(List.of("_text", "_data", "const"),
                result.stream().map(CombinedSegment::name).toList());
    }

    @Test
    public void caseInsensitiveSegmentNameGrouping()
    {
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getSegmentDefs()).thenReturn(List.of(
                new SegmentDefProcessed(0x10, OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                        OMFItemSEGDEF.Combination.PUBLIC, "_text", "code")));
        when(moduleB.getModuleName()).thenReturn("B");
        when(moduleB.getSegmentDefs()).thenReturn(List.of(
                new SegmentDefProcessed(0x20, OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                        OMFItemSEGDEF.Combination.PUBLIC, "_TEXT", "code")));
        when(alignmentResolver.toBytes(OMFItemSEGDEF.Alignment.BYTE_ALIGNED)).thenReturn(1);

        List<CombinedSegment> result = combiner.combine(List.of(moduleA, moduleB));

        assertEquals(1, result.size(), "_text and _TEXT must combine into one segment");
        assertEquals(0x30, result.get(0).totalLength());
    }

    @Test
    public void nonPublicCombineThrows()
    {
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getSegmentDefs()).thenReturn(List.of(
                new SegmentDefProcessed(0x10, OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                        OMFItemSEGDEF.Combination.COMMON, "c_common", "bss")));

        assertThrows(UnsupportedOperationException.class, () -> combiner.combine(List.of(moduleA)));
    }
}
