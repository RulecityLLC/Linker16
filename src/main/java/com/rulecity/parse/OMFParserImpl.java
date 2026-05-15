package com.rulecity.parse;

import com.rulecity.parse.handler.RecordHandler;
import com.rulecity.parse.io.ByteCursor;
import com.rulecity.parse.io.ByteCursorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OMFParserImpl implements OMFParser
{
    private final ByteCursorFactory cursorFactory;
    private final Map<Byte, RecordHandler> handlersByRecordType;

    public OMFParserImpl(ByteCursorFactory cursorFactory,
                         Map<Byte, RecordHandler> handlersByRecordType)
    {
        this.cursorFactory = cursorFactory;
        this.handlersByRecordType = handlersByRecordType;
    }

    @Override
    public List<OMFItem> parseBinary(byte[] src)
    {
        ByteCursor cursor = cursorFactory.create(src);
        List<OMFItem> result = new ArrayList<>();

        while (cursor.hasMore())
        {
            cursor.beginRecord();
            byte recordType = cursor.getSignedByte();
            int recordLength = cursor.getWord();
            cursor.markStartOfPayload(recordLength);

            RecordHandler handler = handlersByRecordType.get(recordType);
            if (handler == null)
            {
                throw new RuntimeException(String.format("Unknown record type %02x", recordType & 0xFF));
            }

            OMFItem item = handler.handle(cursor);

            if (cursor.getRecordCount() != (recordLength - 1))
            {
                throw new RuntimeException("Unexpected bytes");
            }

            byte checkSumExpected = cursor.readRawByte();
            if (cursor.getChecksum() != checkSumExpected)
            {
                throw new RuntimeException("Checksum mismatch!");
            }

            result.add(item);
        }

        return result;
    }
}
