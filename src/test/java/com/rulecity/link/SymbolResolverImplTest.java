package com.rulecity.link;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.ResolvedSymbol;
import com.rulecity.link.data.SegmentPiece;
import com.rulecity.parse.OMFItemSEGDEF;
import com.rulecity.parse.data.PublicNameAndOffset;
import com.rulecity.parse.data.PublicNamesDefinitionProcessed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SymbolResolverImplTest
{
    @Mock private OMFFile moduleA;
    @Mock private OMFFile moduleB;

    private final SymbolResolverImpl resolver = new SymbolResolverImpl();

    private static CombinedSegment placed(String name, int imageOffset, List<SegmentPiece> pieces, int totalLength)
    {
        return new CombinedSegment(name, "cls", OMFItemSEGDEF.Combination.PUBLIC,
                1, imageOffset, totalLength, pieces);
    }

    @Test
    public void publicInFirstModulesPiece_resolvesToImageOffsetPlusPublicOffset()
    {
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getPublicSymbols()).thenReturn(List.of(
                new PublicNamesDefinitionProcessed(0, 0, null,
                        List.of(new PublicNameAndOffset("foo", 0x42)), false)));

        CombinedSegment text = placed("_text", 0x112,
                List.of(new SegmentPiece(0, "A", 0, 0, 0x100)), 0x100);

        List<ResolvedSymbol> r = resolver.resolve(List.of(moduleA), List.of(text));
        assertEquals(1, r.size());
        assertEquals("foo", r.get(0).name());
        assertEquals(0x112 + 0x42, r.get(0).imageOffset());
        assertEquals("_text", r.get(0).segmentName());
        assertEquals(false, r.get(0).isLocal());
    }

    @Test
    public void publicInSecondModulesPiece_addsBothCombinedAndPieceOffset()
    {
        when(moduleA.getPublicSymbols()).thenReturn(List.of());
        when(moduleB.getModuleName()).thenReturn("B");
        when(moduleB.getPublicSymbols()).thenReturn(List.of(
                new PublicNamesDefinitionProcessed(0, 0, null,
                        List.of(new PublicNameAndOffset("bar", 0x10)), false)));

        CombinedSegment text = placed("_text", 0x112, List.of(
                new SegmentPiece(0, "A", 0, 0, 0x100),
                new SegmentPiece(1, "B", 0, 0x100, 0x50)), 0x150);

        List<ResolvedSymbol> r = resolver.resolve(List.of(moduleA, moduleB), List.of(text));
        assertEquals(1, r.size());
        assertEquals(0x112 + 0x100 + 0x10, r.get(0).imageOffset());
        assertEquals("B", r.get(0).moduleName());
    }

    @Test
    public void lpubdefIsMarkedLocal()
    {
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getPublicSymbols()).thenReturn(List.of(
                new PublicNamesDefinitionProcessed(0, 0, null,
                        List.of(new PublicNameAndOffset("baz", 0)), true)));

        CombinedSegment text = placed("_text", 0,
                List.of(new SegmentPiece(0, "A", 0, 0, 0x10)), 0x10);

        List<ResolvedSymbol> r = resolver.resolve(List.of(moduleA), List.of(text));
        assertEquals(1, r.size());
        assertTrue(r.get(0).isLocal());
    }

    @Test
    public void groupRelativePublicWithNoSegment_resolvesToPublicOffsetAsImageOffset()
    {
        // E.g. asmlib's __acrtused at 0x9876 (no segment column in DL2.MAP).
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getPublicSymbols()).thenReturn(List.of(
                new PublicNamesDefinitionProcessed(0, null, null,
                        List.of(new PublicNameAndOffset("__acrtused", 0x9876)), false)));

        List<ResolvedSymbol> r = resolver.resolve(List.of(moduleA), List.of());
        assertEquals(1, r.size());
        assertEquals(0x9876, r.get(0).imageOffset());
        assertNull(r.get(0).segmentName());
    }

    @Test
    public void multipleNamesPerPubdef_allResolvedSeparately()
    {
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getPublicSymbols()).thenReturn(List.of(
                new PublicNamesDefinitionProcessed(0, 0, null,
                        List.of(new PublicNameAndOffset("alpha", 0x10),
                                new PublicNameAndOffset("beta", 0x20)),
                        false)));

        CombinedSegment text = placed("_text", 0x100,
                List.of(new SegmentPiece(0, "A", 0, 0, 0x100)), 0x100);

        List<ResolvedSymbol> r = resolver.resolve(List.of(moduleA), List.of(text));
        assertEquals(2, r.size());
        assertEquals(0x110, r.get(0).imageOffset());
        assertEquals(0x120, r.get(1).imageOffset());
    }

    @Test
    public void missingPlacedPieceForSegmentIdx_throws()
    {
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getPublicSymbols()).thenReturn(List.of(
                new PublicNamesDefinitionProcessed(0, 7, null,
                        List.of(new PublicNameAndOffset("foo", 0)), false)));

        assertThrows(RuntimeException.class,
                () -> resolver.resolve(List.of(moduleA), List.of()));
    }

    @Test
    public void pieceKey_distinguishesByModuleAndBySegment()
    {
        // Two modules, each contributing two SEGDEFs. The (moduleIdx, segmentIdx)
        // key must keep all four pieces distinct — if any pair collides, two of
        // these PUBDEFs resolve to the wrong piece's offset.
        when(moduleA.getModuleName()).thenReturn("A");
        when(moduleA.getPublicSymbols()).thenReturn(List.of(
                new PublicNamesDefinitionProcessed(0, 0, null,
                        List.of(new PublicNameAndOffset("a_text", 0)), false),
                new PublicNamesDefinitionProcessed(0, 1, null,
                        List.of(new PublicNameAndOffset("a_data", 0)), false)));
        when(moduleB.getModuleName()).thenReturn("B");
        when(moduleB.getPublicSymbols()).thenReturn(List.of(
                new PublicNamesDefinitionProcessed(0, 0, null,
                        List.of(new PublicNameAndOffset("b_text", 0)), false),
                new PublicNamesDefinitionProcessed(0, 1, null,
                        List.of(new PublicNameAndOffset("b_data", 0)), false)));

        // _text combined: module 0 piece at offset 0, module 1 piece at offset 0x100.
        CombinedSegment text = placed("_text", 0x1000, List.of(
                new SegmentPiece(0, "A", 0, 0,      0x100),
                new SegmentPiece(1, "B", 0, 0x100,  0x80)),
                0x180);
        // _data combined: module 0 piece at offset 0, module 1 piece at offset 0x40.
        CombinedSegment data = placed("_data", 0x2000, List.of(
                new SegmentPiece(0, "A", 1, 0,      0x40),
                new SegmentPiece(1, "B", 1, 0x40,   0x20)),
                0x60);

        List<ResolvedSymbol> r = resolver.resolve(List.of(moduleA, moduleB), List.of(text, data));
        java.util.Map<String, Integer> byName = new java.util.HashMap<>();
        for (ResolvedSymbol rs : r) byName.put(rs.name(), rs.imageOffset());

        // Each PUBDEF must end up at its OWN piece's slot — collision in the key
        // function would point to whichever piece was inserted last into the lookup.
        assertEquals(0x1000,         byName.get("a_text")); // mod 0, seg 0
        assertEquals(0x2000,         byName.get("a_data")); // mod 0, seg 1
        assertEquals(0x1000 + 0x100, byName.get("b_text")); // mod 1, seg 0
        assertEquals(0x2000 + 0x40,  byName.get("b_data")); // mod 1, seg 1
    }
}
