package com.rulecity.parse.data;

import com.rulecity.parse.OMFItemSEGDEF;

/**
 *
 * @param length
 * @param alignment
 * @param combination
 * @param nameSeg   I chose to use strings here (instead of indices to an array) because across files, string comparison is required.
 * @param nameClass
 */
public record SegmentDefProcessed(int length, OMFItemSEGDEF.Alignment alignment,
                                  OMFItemSEGDEF.Combination combination,
                                  String nameSeg, String nameClass)
{
}
