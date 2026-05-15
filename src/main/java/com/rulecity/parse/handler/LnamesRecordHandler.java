package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemLNAMESImpl;
import com.rulecity.parse.io.ByteCursor;

import java.util.ArrayList;
import java.util.List;

public class LnamesRecordHandler implements RecordHandler
{
    @Override
    public OMFItem handle(ByteCursor cursor)
    {
        StringBuilder bldr = new StringBuilder();
        List<String> names = new ArrayList<>();
        int endCount = cursor.getRecordLength() - 1;

        while (cursor.getRecordCount() < endCount)
        {
            int length = cursor.getSignedByte();
            for (int i = 0; i < length; i++)
            {
                bldr.append((char) cursor.getSignedByte());
            }
            names.add(bldr.toString());
            bldr.setLength(0);
        }

        return new OMFItemLNAMESImpl(names);
    }
}
