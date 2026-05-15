package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemTHEADR;
import com.rulecity.parse.io.ByteCursor;

public class TheadrRecordHandler implements RecordHandler
{
    @Override
    public OMFItem handle(ByteCursor cursor)
    {
        StringBuilder bldr = new StringBuilder();
        int length = cursor.getSignedByte();
        for (int i = 0; i < length; i++)
        {
            bldr.append((char) cursor.getSignedByte());
        }
        return new OMFItemTHEADR(bldr.toString());
    }
}
