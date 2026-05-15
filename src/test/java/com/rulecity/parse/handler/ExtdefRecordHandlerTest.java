package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemEXTDEF;
import com.rulecity.parse.data.ExternalNamesDefinition;
import com.rulecity.parse.io.ByteCursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExtdefRecordHandlerTest
{
    @Mock private ByteCursor cursor;

    @Test
    public void readsMultipleExternals_isLEXTDEFPropagated()
    {
        // Two externals: "AB" type idx 0, "X" type idx 7
        // Payload bytes: 2,'A','B',(idx0=1B), 1,'X',(idx7=1B) = 7 bytes, recordLength=8
        when(cursor.getRecordLength()).thenReturn(8);
        when(cursor.getRecordCount()).thenReturn(0, 4, 7);
        when(cursor.getUnsignedByteAsInt()).thenReturn(2, 1);
        when(cursor.getSignedByte()).thenReturn(
                (byte) 'A', (byte) 'B',
                (byte) 'X');
        when(cursor.getIndex()).thenReturn(0, 7);

        OMFItem item = new ExtdefRecordHandler(true).handle(cursor);
        OMFItemEXTDEF extdef = (OMFItemEXTDEF) item;
        List<ExternalNamesDefinition> defs = extdef.getExternalNamesDefinitions();

        assertTrue(extdef.isLEXTDEF());
        assertEquals(2, defs.size());
        assertEquals("AB", defs.get(0).externalNameString());
        assertEquals(0, defs.get(0).typeIndex());
        assertEquals("X", defs.get(1).externalNameString());
        assertEquals(7, defs.get(1).typeIndex());
    }

    @Test
    public void notLEXTDEF_propagatesFalse()
    {
        when(cursor.getRecordLength()).thenReturn(1);
        when(cursor.getRecordCount()).thenReturn(0);

        OMFItem item = new ExtdefRecordHandler(false).handle(cursor);
        assertFalse(((OMFItemEXTDEF) item).isLEXTDEF());
        assertEquals(0, ((OMFItemEXTDEF) item).getExternalNamesDefinitions().size());
    }
}
