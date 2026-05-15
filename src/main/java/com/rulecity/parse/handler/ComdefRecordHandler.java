package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemCOMDEFImpl;
import com.rulecity.parse.data.Communal;
import com.rulecity.parse.io.ByteCursor;

import java.util.ArrayList;
import java.util.List;

public class ComdefRecordHandler implements RecordHandler
{
    @Override
    public OMFItem handle(ByteCursor cursor)
    {
        List<Communal> lstCommunal = new ArrayList<>();
        StringBuilder bldr = new StringBuilder();
        int endCount = cursor.getRecordLength() - 1;

        while (cursor.getRecordCount() < endCount)
        {
            byte len = cursor.getSignedByte();
            for (int i = 0; i < len; i++)
            {
                bldr.append((char) cursor.getSignedByte());
            }
            cursor.getIndex(); // typeIdx, discarded
            byte dataSegmentType = cursor.getSignedByte();
            int communalLength = switch (dataSegmentType)
            {
                case 0x62 -> cursor.getCommunalField(); // NEAR
                case 0x61 -> cursor.getCommunalField() * cursor.getCommunalField(); // FAR
                default -> throw new RuntimeException("Unexpected value for dataSegmentType");
            };
            lstCommunal.add(new Communal(bldr.toString(), communalLength));
            bldr.setLength(0);
        }

        return new OMFItemCOMDEFImpl(lstCommunal);
    }
}
