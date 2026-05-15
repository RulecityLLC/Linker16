package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemEXTDEFImpl;
import com.rulecity.parse.data.ExternalNamesDefinition;
import com.rulecity.parse.io.ByteCursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles both EXTDEF (0x8C, {@code isLEXTDEF=false}) and LEXTDEF (0xB4, {@code isLEXTDEF=true}).
 */
public class ExtdefRecordHandler implements RecordHandler
{
    private final boolean isLEXTDEF;

    public ExtdefRecordHandler(boolean isLEXTDEF)
    {
        this.isLEXTDEF = isLEXTDEF;
    }

    @Override
    public OMFItem handle(ByteCursor cursor)
    {
        List<ExternalNamesDefinition> lstDefs = new ArrayList<>();
        StringBuilder bldr = new StringBuilder();
        int endCount = cursor.getRecordLength() - 1;

        while (cursor.getRecordCount() < endCount)
        {
            int length = cursor.getUnsignedByteAsInt();
            for (int i = 0; i < length; i++)
            {
                bldr.append((char) cursor.getSignedByte());
            }
            int typeIndex = cursor.getIndex();
            lstDefs.add(new ExternalNamesDefinition(bldr.toString(), typeIndex));
            bldr.setLength(0);
        }

        return new OMFItemEXTDEFImpl(lstDefs, isLEXTDEF);
    }
}
