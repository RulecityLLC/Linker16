package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemGRPDEFImpl;
import com.rulecity.parse.io.ByteCursor;

import java.util.ArrayList;
import java.util.List;

public class GrpdefRecordHandler implements RecordHandler
{
    @Override
    public OMFItem handle(ByteCursor cursor)
    {
        List<Integer> lstSegDefs = new ArrayList<>();
        int grpNameIdx = cursor.getIndex();
        int endCount = cursor.getRecordLength() - 1;

        while (cursor.getRecordCount() < endCount)
        {
            // Per TIS 1.1 sec 4.4: each "Segment Definition Component" begins with a literal
            // 0xFF anchor byte (NOT an index field), followed by the segment-def index field.
            byte anchor = cursor.getSignedByte();
            if (anchor != (byte) 0xFF) throw new RuntimeException("GRPDEF segment-def anchor was not FFh.");
            lstSegDefs.add(cursor.getIndex());
        }

        return new OMFItemGRPDEFImpl(grpNameIdx, lstSegDefs);
    }
}
