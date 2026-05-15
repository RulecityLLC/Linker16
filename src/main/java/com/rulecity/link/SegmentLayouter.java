package com.rulecity.link;

import com.rulecity.link.data.CombinedSegment;

import java.util.List;

/**
 * Orders combined segments and assigns each one its image offset, returning new
 * {@link CombinedSegment} records with {@code imageOffset} populated.
 * <p>
 * Ordering: segments named in the linker's {@code -order segment ...} list come
 * first, in that order; all remaining segments follow in the order they appear
 * in the input list (which {@link SegmentCombiner} populates in first-encounter
 * order across modules).
 */
public interface SegmentLayouter
{
    List<CombinedSegment> layout(List<CombinedSegment> combined);
}
