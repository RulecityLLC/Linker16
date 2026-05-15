package com.rulecity.link;

import com.rulecity.link.data.CombinedSegment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SegmentLayouterImpl implements SegmentLayouter
{
    private final List<String> orderedSegmentNames;

    /**
     * @param orderedSegmentNames Segments that must appear first in this exact
     *                            order (corresponds to LinkLoc's
     *                            {@code -order segment a,b,c} switch). Any
     *                            combined segment whose name is NOT in this
     *                            list is appended in input-list order.
     */
    public SegmentLayouterImpl(List<String> orderedSegmentNames)
    {
        this.orderedSegmentNames = List.copyOf(orderedSegmentNames);
    }

    @Override
    public List<CombinedSegment> layout(List<CombinedSegment> combined)
    {
        LinkedHashMap<String, CombinedSegment> byKey = new LinkedHashMap<>();
        for (CombinedSegment cs : combined) byKey.put(cs.name().toUpperCase(), cs);

        List<CombinedSegment> ordered = new ArrayList<>(combined.size());
        for (String name : orderedSegmentNames)
        {
            CombinedSegment cs = byKey.remove(name.toUpperCase());
            if (cs != null) ordered.add(cs);
        }
        ordered.addAll(byKey.values());

        List<CombinedSegment> placed = new ArrayList<>(ordered.size());
        int position = 0;
        for (CombinedSegment seg : ordered)
        {
            int aligned = roundUp(position, seg.alignmentBytes());
            placed.add(new CombinedSegment(seg.name(), seg.className(), seg.combination(),
                    seg.alignmentBytes(), aligned, seg.totalLength(), seg.pieces()));
            position = aligned + seg.totalLength();
        }
        return placed;
    }

    private static int roundUp(int value, int align)
    {
        return (value + align - 1) & -align;
    }
}
