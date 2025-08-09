package com.rulecity.parse;

import com.rulecity.aggregation.SegmentDefProcessed;

import java.util.List;

public class OMFItemSEGDEFImpl implements OMFItem, OMFItemSEGDEF
{
    private final byte A;
    private final byte C;
    private final boolean big;
    private final boolean P;
    private final int segmentLength;
    private final int segmentNameIdx;
    private final int classNameIdx;
    private final int overlayNameIdx;

    public OMFItemSEGDEFImpl(byte a, byte c, boolean big, boolean p, int segmentLength, int segmentNameIdx, int classNameIdx, int overlayNameIdx)
    {
        this.A = a;
        this.C = c;
        this.big = big;
        this.P = p;
        this.segmentLength = segmentLength;
        this.segmentNameIdx = segmentNameIdx;
        this.classNameIdx = classNameIdx;
        this.overlayNameIdx = overlayNameIdx;
    }

    @Override
    public String getTypeString()
    {
        return "SEGDEF (98h)";
    }

    @Override
    public String getDataString()
    {
        String AStr = switch (A)
        {
            case 0 -> "Absolute";
            case 1 -> "Relocatable, byte aligned";
            case 2 -> "Relocatable, word (2-byte) aligned";
            case 3 -> "Relocatable, paragraph (16-byte) aligned";
            case 4 -> "Relocatable, page aligned";
            case 5 -> "Relocatable, double word (4-byte) aligned";  // doesn't really apply to 16-bit stuff
            default -> "Unknown A value!";
        };

        String CStr = switch (C)
        {
            case 0 -> "Private";    // Cannot be combined
            case 1, 3 -> "Reserved";

            // Can be concatenated with another segment of the same name.
            case 2, 4, 7 ->
                    "Public";
            case 5 -> "Stack";
            case 6 -> "Common";

            default -> "Unknown C value!";
        };
        return String.format("A: %s. C: %s.\nSegment len: %xh. Segment name idx: %xh. ", AStr, CStr, segmentLength, segmentNameIdx);
    }

    @Override
    public SegmentDefProcessed getProcessed(List<String> lstLNames)
    {
        Alignment alignment = switch (A)
        {
            case 0 -> Alignment.ABSOLUTE;
            case 1 -> Alignment.BYTE_ALIGNED;
            case 2 -> Alignment.WORD_ALIGNED;
            case 3 -> Alignment.PARAGRAPH_ALIGNED;
            case 4 -> Alignment.PAGE_ALIGNED;
            case 5 -> Alignment.DOUBLE_WORD_ALIGNED;
            default -> Alignment.UNKNOWN;
        };

        Combination combination = switch (C)
        {
            case 0 -> Combination.PRIVATE;
            case 2, 4, 7 -> Combination.PUBLIC;
            case 5 -> Combination.STACK;
            case 6 -> Combination.COMMON;
            default -> Combination.UNKNOWN;
        };

        int length = (!big) || (this.segmentLength != 0) ? this.segmentLength : 1 << 16;   // I didn't test the 'big==true' case exhaustively

        // the spec says that the segmentNameIdx and classNameIdx must be defined, so no need to check to see if they refer to valid strings
        return new SegmentDefProcessed(length, alignment, combination,
                lstLNames.get(this.segmentNameIdx-1),
                lstLNames.get(this.classNameIdx-1));
    }
}
