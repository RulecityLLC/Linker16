package com.rulecity.link;

import com.rulecity.parse.OMFItemSEGDEF;

import java.util.Map;

public class AlignmentResolverImpl implements AlignmentResolver
{
    private static final Map<OMFItemSEGDEF.Alignment, Integer> BYTES = Map.of(
            OMFItemSEGDEF.Alignment.BYTE_ALIGNED, 1,
            OMFItemSEGDEF.Alignment.WORD_ALIGNED, 2,
            OMFItemSEGDEF.Alignment.DOUBLE_WORD_ALIGNED, 4,
            OMFItemSEGDEF.Alignment.PARAGRAPH_ALIGNED, 16,
            OMFItemSEGDEF.Alignment.PAGE_ALIGNED, 256
    );

    @Override
    public int toBytes(OMFItemSEGDEF.Alignment alignment)
    {
        Integer v = BYTES.get(alignment);
        if (v == null) throw new IllegalArgumentException("Unsupported SEGDEF alignment: " + alignment);
        return v;
    }
}
