package com.rulecity.parse;

public class ObjItemSEGDEF implements ObjItem
{
    private final byte A;
    private final byte C;
    private final boolean big;
    private final boolean P;
    private final int segmentLength;
    private final int segmentNameIdx;
    private final int classNameIdx;
    private final int overlayNameIdx;

    public ObjItemSEGDEF(byte a, byte c, boolean big, boolean p, int segmentLength, int segmentNameIdx, int classNameIdx, int overlayNameIdx)
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
        return "todo";
    }
}
