package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemSEGDEFImpl;
import com.rulecity.parse.io.ByteCursor;

public class SegdefRecordHandler implements RecordHandler
{
    @Override
    public OMFItem handle(ByteCursor cursor)
    {
        byte attributes = cursor.getSignedByte();
        byte A = (byte) (attributes >> 5);
        byte C = (byte) ((attributes >> 2) & 7);
        boolean Big = (attributes & 2) != 0;
        boolean P = (attributes & 1) != 0;

        if (A == 0)
        {
            throw new RuntimeException("A is 0 so the optional Frame Number word and Offset byte need to be read in.  This is currently unsupported");
        }

        int segmentLength = cursor.getWord();
        int segmentNameIdx = cursor.getIndex();
        int classNameIdx = cursor.getIndex();
        int overlayNameIdx = cursor.getIndex();

        return new OMFItemSEGDEFImpl(A, C, Big, P, segmentLength, segmentNameIdx, classNameIdx, overlayNameIdx);
    }
}
