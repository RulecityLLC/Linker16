package com.rulecity.parse.handler;

import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.io.ByteCursor;

public class FixupReaderImpl implements FixupReader
{
    @Override
    public Fixup readFixup(ByteCursor cursor, boolean segmentRelativeFixups, byte location, int dataRecordOffset)
    {
        byte fixDat = cursor.getSignedByte();
        boolean frameSpecifiedByPreviousThreadFieldRef = (fixDat & 0x80) != 0;
        int frame = (fixDat >> 4) & 7;
        boolean targetSpecifiedByPreviousThreadFieldRef = (fixDat & 8) != 0;
        int targt = fixDat & 7;
        boolean P = (targt & 4) != 0;

        if ((targt & 3) == 3)
        {
            throw new RuntimeException("I don't know how to handle this type of targt!");
        }

        Integer frameDatum = null;
        if (!frameSpecifiedByPreviousThreadFieldRef)
        {
            if (frame <= 2)
            {
                frameDatum = cursor.getIndex();
            }
        }

        Integer targetDatum = null;
        if (!targetSpecifiedByPreviousThreadFieldRef)
        {
            targetDatum = cursor.getIndex();
        }

        Integer targetDisplacement = null;
        if (!P)
        {
            targetDisplacement = cursor.getWord();
        }
        return new Fixup(segmentRelativeFixups, location, dataRecordOffset, frameSpecifiedByPreviousThreadFieldRef,
                frame, targetSpecifiedByPreviousThreadFieldRef, targt, frameDatum, targetDatum, targetDisplacement);
    }
}
