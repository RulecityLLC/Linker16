package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemPUBDEFImpl;
import com.rulecity.parse.data.PublicNamesDefinition;
import com.rulecity.parse.io.ByteCursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles both PUBDEF (0x90, {@code isLPUBDEF=false}) and LPUBDEF (0xB6, {@code isLPUBDEF=true}).
 */
public class PubdefRecordHandler implements RecordHandler
{
    private final boolean isLPUBDEF;

    public PubdefRecordHandler(boolean isLPUBDEF)
    {
        this.isLPUBDEF = isLPUBDEF;
    }

    @Override
    public OMFItem handle(ByteCursor cursor)
    {
        List<PublicNamesDefinition> lstDefs = new ArrayList<>();
        StringBuilder bldr = new StringBuilder();
        int endCount = cursor.getRecordLength() - 1;

        int baseGroupIdx = cursor.getIndex();
        int baseSegmentIdx = cursor.getIndex();
        Integer baseFrame = null;

        if (baseSegmentIdx == 0)
        {
            baseFrame = cursor.getWord();
        }

        while (cursor.getRecordCount() < endCount)
        {
            int length = cursor.getUnsignedByteAsInt();
            for (int i = 0; i < length; i++)
            {
                bldr.append((char) cursor.getSignedByte());
            }

            int publicOffset = cursor.getWord();
            int typeIndex = cursor.getIndex();

            lstDefs.add(new PublicNamesDefinition(bldr.toString(), publicOffset, typeIndex));
            bldr.setLength(0);
        }

        return new OMFItemPUBDEFImpl(baseGroupIdx, baseSegmentIdx, baseFrame, lstDefs, isLPUBDEF);
    }
}
