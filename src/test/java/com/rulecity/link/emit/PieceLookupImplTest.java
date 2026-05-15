package com.rulecity.link.emit;

import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.SegmentPiece;
import com.rulecity.parse.OMFItemSEGDEF;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PieceLookupImplTest
{
    private static CombinedSegment placed(String name, int imageOffset, List<SegmentPiece> pieces, int totalLength)
    {
        return new CombinedSegment(name, "cls", OMFItemSEGDEF.Combination.PUBLIC,
                1, imageOffset, totalLength, pieces);
    }

    @Test
    public void returnsPlacementForKnownPair()
    {
        CombinedSegment text = placed("_text", 0x100, List.of(
                new SegmentPiece(0, "A", 0, 0, 0x40),
                new SegmentPiece(1, "B", 0, 0x40, 0x60)
        ), 0xA0);

        PieceLookupImpl lookup = new PieceLookupImpl(List.of(text));

        PieceLookup.Placement a = lookup.find(0, 0);
        assertNotNull(a);
        assertEquals("_text", a.combined().name());
        assertEquals("A", a.piece().moduleName());

        PieceLookup.Placement b = lookup.find(1, 0);
        assertNotNull(b);
        assertEquals("B", b.piece().moduleName());
    }

    @Test
    public void returnsNullForUnknownPair()
    {
        CombinedSegment text = placed("_text", 0, List.of(
                new SegmentPiece(0, "A", 0, 0, 0x10)
        ), 0x10);
        PieceLookupImpl lookup = new PieceLookupImpl(List.of(text));

        assertNull(lookup.find(1, 0));
        assertNull(lookup.find(0, 5));
    }
}
