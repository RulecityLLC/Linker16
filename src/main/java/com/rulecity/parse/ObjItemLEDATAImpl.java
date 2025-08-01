package com.rulecity.parse;

public class ObjItemLEDATAImpl implements ObjItem, ObjItemLEDATA
{
    private final byte segmentIndex;
    private final int enumeratedDataOffset;
    private final byte[] arrBytes;

    public ObjItemLEDATAImpl(byte segmentIndex, int enumeratedDataOffset, byte[] arrBytes)
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
        return String.format("Segment idx is %x.  Data is %x bytes in size", segmentIndex, arrBytes.length);
    }

    @Override
    public byte getSegmentIdx()
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
