package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemLEDATAImpl;
import com.rulecity.parse.io.ByteCursor;

public class LedataRecordHandler implements RecordHandler
{
    @Override
    public OMFItem handle(ByteCursor cursor)
    {
        // The segment-index field is variable-width (1 or 2 bytes), so the data-byte payload
        // length depends on how the index was encoded — derive it from recordCount, not recordLength.
        int segmentIndex = cursor.getIndex();
        int enumeratedDataOffset = cursor.getWord();
        int dataByteCount = (cursor.getRecordLength() - 1) - cursor.getRecordCount();
        byte[] arrBytes = new byte[dataByteCount];
        for (int i = 0; i < dataByteCount; i++)
        {
            arrBytes[i] = cursor.getSignedByte();
        }

        return new OMFItemLEDATAImpl(segmentIndex, enumeratedDataOffset, arrBytes);
    }
}
