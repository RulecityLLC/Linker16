package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemCOMENT;
import com.rulecity.parse.io.ByteCursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentRecordHandlerTest
{
    @Mock private ByteCursor cursor;

    @Test
    public void readsTypeClassThenPayload()
    {
        // recordLength = 6 -> payloadLen = 6 - 3 = 3 bytes
        when(cursor.getRecordLength()).thenReturn(6);
        when(cursor.getSignedByte()).thenReturn((byte) 0x80, (byte) 0x9F,
                (byte) 0x01, (byte) 0x02, (byte) 0x03);

        OMFItem item = new CommentRecordHandler().handle(cursor);
        OMFItemCOMENT coment = (OMFItemCOMENT) item;

        assertEquals((byte) 0x80, coment.getCommentType());
        assertEquals((byte) 0x9F, coment.getCommentClass());
        assertArrayEquals(new byte[]{1, 2, 3}, coment.getBytes());
    }

    @Test
    public void emptyPayload_recordLength3()
    {
        when(cursor.getRecordLength()).thenReturn(3);
        when(cursor.getSignedByte()).thenReturn((byte) 0x00, (byte) 0xA0);

        OMFItem item = new CommentRecordHandler().handle(cursor);
        OMFItemCOMENT coment = (OMFItemCOMENT) item;

        assertEquals(0, coment.getBytes().length);
    }
}
