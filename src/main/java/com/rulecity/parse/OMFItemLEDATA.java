package com.rulecity.parse;

public interface OMFItemLEDATA extends OMFItem
{
    // 1-based OMF segment index per TIS 1.1 (variable-width: 1..32767, wider than a byte).
    int getSegmentIdx();

    int getEnumeratedDataOffset();

    byte[] getBytes();
}
