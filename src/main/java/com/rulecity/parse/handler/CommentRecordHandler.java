package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemCOMENTImpl;
import com.rulecity.parse.io.ByteCursor;

public class CommentRecordHandler implements RecordHandler
{
    @Override
    public OMFItem handle(ByteCursor cursor)
    {
        byte commentType = cursor.getSignedByte();
        byte commentClass = cursor.getSignedByte();
        int payloadLen = cursor.getRecordLength() - 3;
        byte[] arrBytes = new byte[payloadLen];
        for (int i = 0; i < payloadLen; i++)
        {
            arrBytes[i] = cursor.getSignedByte();
        }
        return new OMFItemCOMENTImpl(commentType, commentClass, arrBytes);
    }
}
