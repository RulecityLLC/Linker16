package com.rulecity.link;

import com.rulecity.link.data.CombinedSegment;
import com.rulecity.parse.OMFItemSEGDEF;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SegmentLayouterImplTest
{
    private static CombinedSegment unplaced(String name, int alignBytes, int totalLength)
    {
        return new CombinedSegment(name, "cls", OMFItemSEGDEF.Combination.PUBLIC,
                alignBytes, -1, totalLength, List.of());
    }

    @Test
    public void explicitOrderRespected()
    {
        SegmentLayouterImpl layouter = new SegmentLayouterImpl(List.of("_atext", "_text", "_data"));

        // Pass them in reverse order; layouter should reorder.
        List<CombinedSegment> result = layouter.layout(List.of(
                unplaced("_data", 1, 0x10),
                unplaced("_text", 1, 0x20),
                unplaced("_atext", 1, 0x30)));

        assertEquals(List.of("_atext", "_text", "_data"),
                result.stream().map(CombinedSegment::name).toList());
        assertEquals(0, result.get(0).imageOffset());
        assertEquals(0x30, result.get(1).imageOffset());
        assertEquals(0x50, result.get(2).imageOffset());
    }

    @Test
    public void unorderedSegmentsAppendedInInputOrder()
    {
        SegmentLayouterImpl layouter = new SegmentLayouterImpl(List.of("_atext"));

        List<CombinedSegment> result = layouter.layout(List.of(
                unplaced("first", 1, 0x10),
                unplaced("_atext", 1, 0x20),
                unplaced("second", 1, 0x10)));

        assertEquals(List.of("_atext", "first", "second"),
                result.stream().map(CombinedSegment::name).toList());
    }

    @Test
    public void alignmentPadsBetweenSegments()
    {
        SegmentLayouterImpl layouter = new SegmentLayouterImpl(List.of("_atext", "_text"));

        // _atext: BYTE-aligned, length 0x111 (odd). _text: WORD-aligned.
        // _text must start at 0x112, not 0x111.
        List<CombinedSegment> result = layouter.layout(List.of(
                unplaced("_atext", 1, 0x111),
                unplaced("_text", 2, 0x100)));

        assertEquals(0, result.get(0).imageOffset());
        assertEquals(0x112, result.get(1).imageOffset());
    }

    @Test
    public void emptyOrderingMeansInputOrder()
    {
        SegmentLayouterImpl layouter = new SegmentLayouterImpl(List.of());

        List<CombinedSegment> result = layouter.layout(List.of(
                unplaced("a", 1, 0x10),
                unplaced("b", 1, 0x20)));

        assertEquals(List.of("a", "b"), result.stream().map(CombinedSegment::name).toList());
    }

    @Test
    public void nonExistentExplicitNamesAreSkipped()
    {
        SegmentLayouterImpl layouter = new SegmentLayouterImpl(List.of("_ghost", "_text"));

        List<CombinedSegment> result = layouter.layout(List.of(
                unplaced("_text", 1, 0x10),
                unplaced("_data", 1, 0x20)));

        assertEquals(List.of("_text", "_data"), result.stream().map(CombinedSegment::name).toList());
    }
}
