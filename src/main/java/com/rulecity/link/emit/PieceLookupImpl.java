package com.rulecity.link.emit;

import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.SegmentPiece;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieceLookupImpl implements PieceLookup
{
    private final Map<Long, Placement> byKey;

    public PieceLookupImpl(List<CombinedSegment> placedSegments)
    {
        Map<Long, Placement> map = new HashMap<>();
        for (CombinedSegment cs : placedSegments)
        {
            for (SegmentPiece p : cs.pieces())
            {
                map.put(key(p.moduleIndex(), p.moduleSegmentIdx()), new Placement(cs, p));
            }
        }
        this.byKey = map;
    }

    @Override
    public Placement find(int moduleIdx, int moduleSegmentIdx)
    {
        return byKey.get(key(moduleIdx, moduleSegmentIdx));
    }

    private static long key(int moduleIdx, int moduleSegmentIdx)
    {
        return (((long) moduleIdx) << 32) | (moduleSegmentIdx & 0xFFFFFFFFL);
    }
}
