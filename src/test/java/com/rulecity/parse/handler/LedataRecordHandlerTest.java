package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemLEDATA;
import com.rulecity.parse.io.ByteCursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LedataRecordHandlerTest
{
    @Mock private ByteCursor cursor;

    @Test
    public void readsSegmentIdxOffsetThenPayload()
    {
        // segmentIndex=2, offset=0x1234, then 3 payload bytes; recordLength = 1 (segIdx) + 2 (offset) + 3 (data) + 1 (checksum) = 7
        when(cursor.getRecordLength()).thenReturn(7);
        when(cursor.getIndex()).thenReturn(2);
        when(cursor.getWord()).thenReturn(0x1234);
        when(cursor.getRecordCount()).thenReturn(3);
        when(cursor.getSignedByte()).thenReturn((byte) 0x10, (byte) 0x20, (byte) 0x30);

        OMFItem item = new LedataRecordHandler().handle(cursor);
        OMFItemLEDATA ledata = (OMFItemLEDATA) item;

        assertEquals(2, ledata.getSegmentIdx());
        assertEquals(0x1234, ledata.getEnumeratedDataOffset());
        assertArrayEquals(new byte[]{0x10, 0x20, 0x30}, ledata.getBytes());
    }

    @Test
    public void emptyDataPayload()
    {
        when(cursor.getRecordLength()).thenReturn(4);
        when(cursor.getIndex()).thenReturn(1);
        when(cursor.getWord()).thenReturn(0);
        when(cursor.getRecordCount()).thenReturn(3);

        OMFItem item = new LedataRecordHandler().handle(cursor);
        OMFItemLEDATA ledata = (OMFItemLEDATA) item;

        assertEquals(0, ledata.getBytes().length);
    }

    @Test
    public void recordCountAccountsForVariableWidthIndex()
    {
        // 2-byte segmentIndex encoding consumes 2 bytes from recordCount; data length should derive from recordCount.
        when(cursor.getRecordLength()).thenReturn(8);
        when(cursor.getIndex()).thenReturn(0x100);
        when(cursor.getWord()).thenReturn(0);
        when(cursor.getRecordCount()).thenReturn(4); // 2-byte index + word
        when(cursor.getSignedByte()).thenReturn((byte) 0x01, (byte) 0x02, (byte) 0x03);

        OMFItem item = new LedataRecordHandler().handle(cursor);
        OMFItemLEDATA ledata = (OMFItemLEDATA) item;

        assertEquals(3, ledata.getBytes().length);
    }
}
