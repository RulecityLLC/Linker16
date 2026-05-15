package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemTHEADR;
import com.rulecity.parse.io.ByteCursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TheadrRecordHandlerTest
{
    @Mock private ByteCursor cursor;

    @Test
    public void readsLengthThenAsciiCharacters()
    {
        when(cursor.getSignedByte()).thenReturn((byte) 3, (byte) 'F', (byte) 'O', (byte) 'O');

        OMFItem item = new TheadrRecordHandler().handle(cursor);

        assertEquals("FOO", ((OMFItemTHEADR) item).getDataString());
        verify(cursor, times(4)).getSignedByte();
    }

    @Test
    public void emptyName()
    {
        when(cursor.getSignedByte()).thenReturn((byte) 0);

        OMFItem item = new TheadrRecordHandler().handle(cursor);

        assertEquals("", ((OMFItemTHEADR) item).getDataString());
        verify(cursor, times(1)).getSignedByte();
    }
}
