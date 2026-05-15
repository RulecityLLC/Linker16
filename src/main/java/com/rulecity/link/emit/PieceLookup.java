package com.rulecity.link.emit;

import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.SegmentPiece;

/**
 * Locates which {@link CombinedSegment} and {@link SegmentPiece} a given
 * (moduleIdx, moduleSegmentIdx) pair was placed into during layout.
 */
public interface PieceLookup
{
    /**
     * @param moduleIdx         0-based module index (input order).
     * @param moduleSegmentIdx  0-based index into that module's SEGDEF list.
     * @return the placement, or {@code null} if the segment wasn't placed
     *         (e.g. an absolute or unused SEGDEF).
     */
    Placement find(int moduleIdx, int moduleSegmentIdx);

    record Placement(CombinedSegment combined, SegmentPiece piece) {}
}
