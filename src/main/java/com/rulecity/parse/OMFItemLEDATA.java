package com.rulecity.parse;

public interface OMFItemLEDATA
{
    byte getSegmentIdx();

    int getEnumeratedDataOffset();

    byte[] getBytes();
}
