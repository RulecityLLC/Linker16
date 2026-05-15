package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemCOMDEF;
import com.rulecity.parse.data.Communal;
import com.rulecity.parse.io.ByteCursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComdefRecordHandlerTest
{
    @Mock private ByteCursor cursor;

    @Test
    public void nearCommunal_lengthFromCommunalField()
    {
        // One name "AB", typeIdx=0, dataSegmentType=0x62 (NEAR), length=10
        when(cursor.getRecordLength()).thenReturn(7);
        when(cursor.getRecordCount()).thenReturn(0, 6);
        when(cursor.getSignedByte()).thenReturn((byte) 2, (byte) 'A', (byte) 'B', (byte) 0x62);
        when(cursor.getIndex()).thenReturn(0);
        when(cursor.getCommunalField()).thenReturn(10);

        OMFItem item = new ComdefRecordHandler().handle(cursor);
        List<Communal> list = ((OMFItemCOMDEF) item).getCommualList();

        assertEquals(1, list.size());
        assertEquals("AB", list.get(0).name());
        assertEquals(10, list.get(0).length());
    }

    @Test
    public void farCommunal_lengthIsCommunalFieldSquared()
    {
        // dataSegmentType=0x61 (FAR), getCommunalField called twice and multiplied
        when(cursor.getRecordLength()).thenReturn(6);
        when(cursor.getRecordCount()).thenReturn(0, 5);
        when(cursor.getSignedByte()).thenReturn((byte) 1, (byte) 'X', (byte) 0x61);
        when(cursor.getIndex()).thenReturn(0);
        when(cursor.getCommunalField()).thenReturn(3, 4);

        OMFItem item = new ComdefRecordHandler().handle(cursor);
        List<Communal> list = ((OMFItemCOMDEF) item).getCommualList();

        assertEquals(12, list.get(0).length());
    }

    @Test
    public void unknownDataSegmentType_throws()
    {
        when(cursor.getRecordLength()).thenReturn(5);
        when(cursor.getRecordCount()).thenReturn(0);
        when(cursor.getSignedByte()).thenReturn((byte) 1, (byte) 'A', (byte) 0x77);
        when(cursor.getIndex()).thenReturn(0);

        assertThrows(RuntimeException.class, () -> new ComdefRecordHandler().handle(cursor));
    }

    @Test
    public void multipleEntries()
    {
        when(cursor.getRecordLength()).thenReturn(11);
        when(cursor.getRecordCount()).thenReturn(0, 5, 10);
        when(cursor.getSignedByte()).thenReturn(
                (byte) 1, (byte) 'A', (byte) 0x62,
                (byte) 1, (byte) 'B', (byte) 0x62);
        when(cursor.getIndex()).thenReturn(0, 0);
        when(cursor.getCommunalField()).thenReturn(5, 7);

        OMFItem item = new ComdefRecordHandler().handle(cursor);
        List<Communal> list = ((OMFItemCOMDEF) item).getCommualList();

        assertEquals(2, list.size());
        assertEquals("A", list.get(0).name());
        assertEquals(5, list.get(0).length());
        assertEquals("B", list.get(1).name());
        assertEquals(7, list.get(1).length());
    }
}
