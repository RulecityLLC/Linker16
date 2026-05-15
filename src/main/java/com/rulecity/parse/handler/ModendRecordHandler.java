package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemMODENDImpl;
import com.rulecity.parse.io.ByteCursor;

public class ModendRecordHandler implements RecordHandler
{
    @Override
    public OMFItem handle(ByteCursor cursor)
    {
        byte moduleType = cursor.getSignedByte();
        Byte endData = null, frameDatum = null, targetDatum = null;
        Integer targetDisplacement = null;

        boolean isAMainProgramModule = (moduleType & 0x80) != 0;
        boolean moduleContainsAStartAddress = (moduleType & 0x40) != 0;

        if (moduleContainsAStartAddress)
        {
            endData = cursor.getSignedByte();
            frameDatum = cursor.getSignedByte();
            targetDatum = cursor.getSignedByte();
            targetDisplacement = cursor.getWord();
        }

        return new OMFItemMODENDImpl(isAMainProgramModule, moduleContainsAStartAddress,
                endData, frameDatum, targetDatum, targetDisplacement);
    }
}
