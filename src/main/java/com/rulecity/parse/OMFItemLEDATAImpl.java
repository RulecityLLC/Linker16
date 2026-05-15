package com.rulecity.parse;

public class OMFItemLEDATAImpl implements OMFItem, OMFItemLEDATA
{
    private final int segmentIndex;
    private final int enumeratedDataOffset;
    private final byte[] arrBytes;

    public OMFItemLEDATAImpl(int segmentIndex, int enumeratedDataOffset, byte[] arrBytes)
    {
        this.segmentIndex = segmentIndex;
        this.enumeratedDataOffset = enumeratedDataOffset;
        this.arrBytes = arrBytes;
    }

    @Override
    public String getTypeString()
    {
        return "LEDATA (A0h)";
    }

    @Override
    public String getDataString()
    {
        return String.format("Segment idx: %x.  Enumerated data offset: %x.  Data is %x bytes in size", segmentIndex,
                enumeratedDataOffset,
                arrBytes.length);
    }

    @Override
    public int getSegmentIdx()
    {
        return segmentIndex;
    }

    @Override
    public int getEnumeratedDataOffset()
    {
        return enumeratedDataOffset;
    }

    @Override
    public byte[] getBytes()
    {
        return arrBytes;
    }
}
