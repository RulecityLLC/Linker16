package com.rulecity.link;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;

import java.util.List;

/**
 * Combines same-named SEGDEFs from the input modules into a list of
 * {@link CombinedSegment} records. Each combined segment's pieces are
 * positioned relative to the start of the combined segment (i.e. the
 * piece offsets are correct, but {@code imageOffset == -1} — actual
 * placement in the image is the next stage's job).
 */
public interface SegmentCombiner
{
    /**
     * @param modulesInInputOrder Modules in the order they appeared on the
     *                            linker command line.
     * @return One {@link CombinedSegment} per unique segment name, returned in
     *         the order each name was first encountered while walking modules.
     */
    List<CombinedSegment> combine(List<OMFFile> modulesInInputOrder);
}
