package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemMODEND;
import com.rulecity.parse.io.ByteCursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ModendRecordHandlerTest
{
    @Mock private ByteCursor cursor;

    @Test
    public void mainModuleNoStartAddress_setsMainTrueStartFalse()
    {
        // 0x80 -> main bit set, start bit clear
        when(cursor.getSignedByte()).thenReturn((byte) 0x80);

        OMFItem item = new ModendRecordHandler().handle(cursor);
        OMFItemMODEND modend = (OMFItemMODEND) item;

        assertTrue(modend.isAMainProgramModule());
        assertFalse(modend.moduleContainsAStartAddress());
        verify(cursor, times(1)).getSignedByte();
        verify(cursor, never()).getWord();
    }

    @Test
    public void notMainNoStart_bothFalse()
    {
        when(cursor.getSignedByte()).thenReturn((byte) 0x00);

        OMFItem item = new ModendRecordHandler().handle(cursor);
        OMFItemMODEND modend = (OMFItemMODEND) item;

        assertFalse(modend.isAMainProgramModule());
        assertFalse(modend.moduleContainsAStartAddress());
    }

    @Test
    public void startAddress_consumesEndDataAndFrameTargetDispl()
    {
        // 0x40 -> start address present
        when(cursor.getSignedByte()).thenReturn((byte) 0x40, (byte) 0x01, (byte) 0x02, (byte) 0x03);
        when(cursor.getWord()).thenReturn(0x4567);

        OMFItem item = new ModendRecordHandler().handle(cursor);
        OMFItemMODEND modend = (OMFItemMODEND) item;

        assertFalse(modend.isAMainProgramModule());
        assertTrue(modend.moduleContainsAStartAddress());
        verify(cursor, times(4)).getSignedByte();
        verify(cursor, times(1)).getWord();
    }

    @Test
    public void mainAndStart_bothFlagsSet()
    {
        when(cursor.getSignedByte()).thenReturn((byte) 0xC0, (byte) 0, (byte) 0, (byte) 0);
        when(cursor.getWord()).thenReturn(0);

        OMFItem item = new ModendRecordHandler().handle(cursor);
        OMFItemMODEND modend = (OMFItemMODEND) item;

        assertTrue(modend.isAMainProgramModule());
        assertTrue(modend.moduleContainsAStartAddress());
    }
}
