package com.rulecity.parse;

import com.rulecity.parse.handler.RecordHandler;
import com.rulecity.parse.io.ByteCursor;
import com.rulecity.parse.io.ByteCursorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OMFParserImplTest
{
    @Mock private ByteCursorFactory cursorFactory;
    @Mock private ByteCursor cursor;
    @Mock private RecordHandler theadrHandler;
    @Mock private RecordHandler comentHandler;
    @Mock private OMFItem theadrItem;
    @Mock private OMFItem comentItem;

    @Test
    public void dispatchesToHandlerByRecordType()
    {
        when(cursorFactory.create(any())).thenReturn(cursor);
        // Two records, then end of stream.
        when(cursor.hasMore()).thenReturn(true, true, false);
        // Each record: getSignedByte (type) -> 0x80 / 0x88; getWord (length) -> 1
        // After payload: cursor.getRecordCount() == 0 (no payload); readRawByte == 0 (checksum).
        when(cursor.getSignedByte()).thenReturn((byte) 0x80, (byte) 0x88);
        when(cursor.getWord()).thenReturn(1, 1);
        when(cursor.getRecordCount()).thenReturn(0);
        when(cursor.getChecksum()).thenReturn((byte) 0);
        when(cursor.readRawByte()).thenReturn((byte) 0, (byte) 0);
        when(theadrHandler.handle(cursor)).thenReturn(theadrItem);
        when(comentHandler.handle(cursor)).thenReturn(comentItem);

        OMFParserImpl parser = new OMFParserImpl(cursorFactory,
                Map.of((byte) 0x80, theadrHandler, (byte) 0x88, comentHandler));

        List<OMFItem> items = parser.parseBinary(new byte[0]);

        assertEquals(2, items.size());
        assertSame(theadrItem, items.get(0));
        assertSame(comentItem, items.get(1));
        verify(theadrHandler, times(1)).handle(cursor);
        verify(comentHandler, times(1)).handle(cursor);
    }

    @Test
    public void unknownRecordType_throws()
    {
        when(cursorFactory.create(any())).thenReturn(cursor);
        when(cursor.hasMore()).thenReturn(true);
        when(cursor.getSignedByte()).thenReturn((byte) 0xFF);
        when(cursor.getWord()).thenReturn(1);

        OMFParserImpl parser = new OMFParserImpl(cursorFactory, Map.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> parser.parseBinary(new byte[0]));
        assertEquals("Unknown record type ff", ex.getMessage());
    }

    @Test
    public void wrongRecordByteCount_throws()
    {
        when(cursorFactory.create(any())).thenReturn(cursor);
        when(cursor.hasMore()).thenReturn(true);
        when(cursor.getSignedByte()).thenReturn((byte) 0x80);
        when(cursor.getWord()).thenReturn(5);
        when(cursor.getRecordCount()).thenReturn(2); // handler only consumed 2, expected 4
        when(theadrHandler.handle(cursor)).thenReturn(theadrItem);

        OMFParserImpl parser = new OMFParserImpl(cursorFactory, Map.of((byte) 0x80, theadrHandler));

        assertThrows(RuntimeException.class, () -> parser.parseBinary(new byte[0]));
        verify(cursor, never()).getChecksum();
    }

    @Test
    public void checksumMismatch_throws()
    {
        when(cursorFactory.create(any())).thenReturn(cursor);
        when(cursor.hasMore()).thenReturn(true);
        when(cursor.getSignedByte()).thenReturn((byte) 0x80);
        when(cursor.getWord()).thenReturn(1);
        when(cursor.getRecordCount()).thenReturn(0);
        when(cursor.readRawByte()).thenReturn((byte) 0x42);
        when(cursor.getChecksum()).thenReturn((byte) 0x37); // expected 0x42, got 0x37
        when(theadrHandler.handle(cursor)).thenReturn(theadrItem);

        OMFParserImpl parser = new OMFParserImpl(cursorFactory, Map.of((byte) 0x80, theadrHandler));

        assertThrows(RuntimeException.class, () -> parser.parseBinary(new byte[0]));
    }
}
