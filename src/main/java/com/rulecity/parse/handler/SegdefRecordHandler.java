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
        // attributes bit 0 (P) is consumed by the wire format but ignored — the
        // current downstream pipeline doesn't model the segment's privilege flag.

        if (A == 0)
        {
            throw new RuntimeException("A is 0 so the optional Frame Number word and Offset byte need to be read in.  This is currently unsupported");
        }

        int segmentLength = cursor.getWord();
        int segmentNameIdx = cursor.getIndex();
        int classNameIdx = cursor.getIndex();
        int overlayNameIdx = cursor.getIndex();

        return new OMFItemSEGDEFImpl(A, C, Big, segmentLength, segmentNameIdx, classNameIdx, overlayNameIdx);
    }
}
