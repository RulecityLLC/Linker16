package com.rulecity.parse;

import com.rulecity.aggregation.SegmentDefProcessed;

import java.util.List;

public interface OMFItemSEGDEF
{
    enum Alignment
    {
        ABSOLUTE,
        BYTE_ALIGNED,
        WORD_ALIGNED,
        PARAGRAPH_ALIGNED,
        PAGE_ALIGNED,
        DOUBLE_WORD_ALIGNED,
        UNKNOWN
    }

    enum Combination
    {
        PRIVATE,
        PUBLIC,             // Can be concatenated with another segment of the same name.
        STACK,
        COMMON,
        UNKNOWN
    }

    /**
     * Returns a processed segment definition record.
     * Processed meaning the caller does not need to understand the binary data format of the OMF file.
     * @param lstLNames A list of 'LNames' extracted from the OMF file previously (in the order as they were extracted).
     * @return The processed segment definition record.
     */
    SegmentDefProcessed getProcessed(List<String> lstLNames);
}
