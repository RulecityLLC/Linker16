package com.rulecity.link;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.SegmentPiece;
import com.rulecity.parse.OMFItemSEGDEF;
import com.rulecity.parse.data.SegmentDefProcessed;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SegmentCombinerImpl implements SegmentCombiner
{
    private final AlignmentResolver alignmentResolver;

    public SegmentCombinerImpl(AlignmentResolver alignmentResolver)
    {
        this.alignmentResolver = alignmentResolver;
    }

    @Override
    public List<CombinedSegment> combine(List<OMFFile> modulesInInputOrder)
    {
        // LinkedHashMap preserves first-encountered order, used by SegmentLayouter
        // to decide where un-explicitly-ordered segments end up.
        LinkedHashMap<String, Accumulator> byKey = new LinkedHashMap<>();

        for (int moduleIdx = 0; moduleIdx < modulesInInputOrder.size(); moduleIdx++)
        {
            OMFFile module = modulesInInputOrder.get(moduleIdx);
            List<SegmentDefProcessed> segs = module.getSegmentDefs();
            for (int segIdx = 0; segIdx < segs.size(); segIdx++)
            {
                SegmentDefProcessed seg = segs.get(segIdx);
                String key = seg.nameSeg().toUpperCase();
                Accumulator acc = byKey.get(key);
                if (acc == null)
                {
                    acc = new Accumulator(seg.nameSeg(), seg.nameClass(), seg.combination());
                    byKey.put(key, acc);
                }
                if (seg.combination() != OMFItemSEGDEF.Combination.PUBLIC)
                {
                    throw new UnsupportedOperationException(
                            "Phase 3 supports only PUBLIC combine; got " + seg.combination()
                                    + " for segment '" + seg.nameSeg() + "' in module "
                                    + module.getModuleName());
                }
                int pieceAlign = alignmentResolver.toBytes(seg.alignment());
                int pieceOffset = roundUp(acc.totalLength, pieceAlign);
                acc.pieces.add(new SegmentPiece(moduleIdx, module.getModuleName(),
                        segIdx, pieceOffset, seg.length()));
                acc.totalLength = pieceOffset + seg.length();
                if (pieceAlign > acc.alignmentBytes) acc.alignmentBytes = pieceAlign;
            }
        }

        List<CombinedSegment> result = new ArrayList<>();
        for (Accumulator a : byKey.values())
        {
            result.add(new CombinedSegment(a.name, a.className, a.combination,
                    a.alignmentBytes, -1, a.totalLength, List.copyOf(a.pieces)));
        }
        return result;
    }

    /** Rounds {@code value} up to the next multiple of {@code align} (power of 2). */
    private static int roundUp(int value, int align)
    {
        return (value + align - 1) & -align;
    }

    private static final class Accumulator
    {
        final String name;
        final String className;
        final OMFItemSEGDEF.Combination combination;
        final List<SegmentPiece> pieces = new ArrayList<>();
        int totalLength = 0;
        int alignmentBytes = 1;

        Accumulator(String name, String className, OMFItemSEGDEF.Combination combination)
        {
            this.name = name;
            this.className = className;
            this.combination = combination;
        }
    }
}
