package com.rulecity.parse;

public class ObjItemLEDATA implements ObjItem
{
    private final byte segmentIndex;
    private final int enumeratedDataOffset;
    private final byte[] arrBytes;

    public ObjItemLEDATA(byte segmentIndex, int enumeratedDataOffset, byte[] arrBytes)
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
        return "todo";
    }
}
