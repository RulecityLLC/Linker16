package com.rulecity.link.emit;

import com.rulecity.link.data.CombinedSegment;

import java.util.List;

public class PieceLookupFactoryImpl implements PieceLookupFactory
{
    @Override
    public PieceLookup build(List<CombinedSegment> placedSegments)
    {
        return new PieceLookupImpl(placedSegments);
    }
}
