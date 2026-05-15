package com.rulecity.link;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.ResolvedSymbol;

import java.util.List;

/**
 * Resolves PUBDEF/LPUBDEF records from each module to their final byte offset
 * within the linked image, using the placed combined segments to look up where
 * each module's contribution starts.
 */
public interface SymbolResolver
{
    /**
     * @param modulesInInputOrder Same module list passed to the
     *                            {@link SegmentCombiner}.
     * @param placedSegments      Output of {@link SegmentLayouter#layout}.
     * @return One {@link ResolvedSymbol} per (PublicNamesDefinition × name);
     *         PUBDEFs and LPUBDEFs are both included and distinguished by
     *         {@link ResolvedSymbol#isLocal()}.
     */
    List<ResolvedSymbol> resolve(List<OMFFile> modulesInInputOrder,
                                 List<CombinedSegment> placedSegments);
}
