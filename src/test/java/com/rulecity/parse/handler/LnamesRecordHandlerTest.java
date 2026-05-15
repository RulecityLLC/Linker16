package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemLNAMES;
import com.rulecity.parse.io.ByteCursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LnamesRecordHandlerTest
{
    @Mock private ByteCursor cursor;

    @Test
    public void readsMultiplePrefixedNames()
    {
        // Two names: "AB" and "C" -> payload bytes 2,'A','B',1,'C' (5 bytes), recordLength = 6 (5 + checksum)
        when(cursor.getRecordLength()).thenReturn(6);
        when(cursor.getRecordCount()).thenReturn(0, 3, 5);
        when(cursor.getSignedByte()).thenReturn(
                (byte) 2, (byte) 'A', (byte) 'B',
                (byte) 1, (byte) 'C');

        OMFItem item = new LnamesRecordHandler().handle(cursor);
        List<String> names = ((OMFItemLNAMES) item).getNames();

        assertEquals(List.of("AB", "C"), names);
    }

    @Test
    public void emptyRecord_returnsEmptyList()
    {
        when(cursor.getRecordLength()).thenReturn(1);
        when(cursor.getRecordCount()).thenReturn(0);

        OMFItem item = new LnamesRecordHandler().handle(cursor);
        assertEquals(List.of(), ((OMFItemLNAMES) item).getNames());
    }

    @Test
    public void singleZeroLengthName()
    {
        when(cursor.getRecordLength()).thenReturn(2);
        when(cursor.getRecordCount()).thenReturn(0, 1);
        when(cursor.getSignedByte()).thenReturn((byte) 0);

        OMFItem item = new LnamesRecordHandler().handle(cursor);
        assertEquals(List.of(""), ((OMFItemLNAMES) item).getNames());
    }
}
