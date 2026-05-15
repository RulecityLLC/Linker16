package com.rulecity.link.emit;

import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.LinkedLayout;

/**
 * Image length = start of the first BSS-class combined segment. For DL2 that
 * is c_common at 0x9F50; everything past it (c_common itself, _fill) is BSS
 * and is reserved at runtime but not written.
 * <p>
 * If no BSS-class segment exists, the image extends to the end of the last
 * placed segment.
 */
public class ImageSizerImpl implements ImageSizer
{
    private static final String BSS_CLASS = "bss";

    @Override
    public int size(LinkedLayout layout)
    {
        int end = 0;
        for (CombinedSegment cs : layout.combinedSegments())
        {
            if (cs.className().equalsIgnoreCase(BSS_CLASS))
            {
                return cs.imageOffset();
            }
            int segEnd = cs.imageOffset() + cs.totalLength();
            if (segEnd > end) end = segEnd;
        }
        return end;
    }
}
