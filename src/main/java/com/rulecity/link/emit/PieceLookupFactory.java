package com.rulecity.link.emit;

import com.rulecity.link.data.CombinedSegment;

import java.util.List;

public interface PieceLookupFactory
{
    PieceLookup build(List<CombinedSegment> placedSegments);
}
