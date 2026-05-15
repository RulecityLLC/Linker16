package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemPUBDEF;
import com.rulecity.parse.data.PublicNamesDefinitionProcessed;
import com.rulecity.parse.io.ByteCursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PubdefRecordHandlerTest
{
    @Mock private ByteCursor cursor;

    @Test
    public void segmentBased_noBaseFrame_singleEntry()
    {
        // baseGroupIdx=1, baseSegmentIdx=2 (non-zero -> no baseFrame), one entry "AB" off=0x100 type=0
        // First while-check at count=2 (after group+seg indices), second at count=8 (>= endCount=10? endCount=10 actually)
        // recordLength=11 -> endCount=10. Need second check >= 10.
        when(cursor.getRecordLength()).thenReturn(11);
        when(cursor.getRecordCount()).thenReturn(2, 10);
        when(cursor.getIndex()).thenReturn(1, 2, 0); // group, segment, type
        when(cursor.getUnsignedByteAsInt()).thenReturn(2);
        when(cursor.getSignedByte()).thenReturn((byte) 'A', (byte) 'B');
        when(cursor.getWord()).thenReturn(0x100);

        OMFItem item = new PubdefRecordHandler(false).handle(cursor);
        OMFItemPUBDEF pubdef = (OMFItemPUBDEF) item;
        PublicNamesDefinitionProcessed def = pubdef.getDef();

        assertEquals(0, def.baseGroupIdx()); // 1 - 1
        assertEquals(1, def.baseSegmentIdx()); // 2 - 1
        assertNull(def.baseFrame());
        assertFalse(def.isLPUBDEF());
        assertEquals(1, def.lstNamesAndOffsets().size());
        assertEquals("AB", def.lstNamesAndOffsets().get(0).publicNameString());
        assertEquals(0x100, def.lstNamesAndOffsets().get(0).publicOffset());
        verify(cursor, times(1)).getWord(); // only the publicOffset, no baseFrame
    }

    @Test
    public void zeroBaseSegment_readsBaseFrameWord()
    {
        // baseGroupIdx=0, baseSegmentIdx=0 -> baseFrame word read; recordLength=5 (2 idx + word + checksum), endCount=4
        when(cursor.getRecordLength()).thenReturn(5);
        when(cursor.getRecordCount()).thenReturn(4);
        when(cursor.getIndex()).thenReturn(0, 0);
        when(cursor.getWord()).thenReturn(0xABCD);

        OMFItem item = new PubdefRecordHandler(true).handle(cursor);
        OMFItemPUBDEF pubdef = (OMFItemPUBDEF) item;
        PublicNamesDefinitionProcessed def = pubdef.getDef();

        assertNull(def.baseGroupIdx());
        assertNull(def.baseSegmentIdx());
        assertNotNull(def.baseFrame());
        assertEquals(0xABCD, def.baseFrame());
        assertTrue(def.isLPUBDEF());
    }

    @Test
    public void nonZeroBaseSegment_doesNotReadBaseFrame()
    {
        // recordLength=3 -> endCount=2, recordCount=2 after both indices, exits loop.
        when(cursor.getRecordLength()).thenReturn(3);
        when(cursor.getRecordCount()).thenReturn(2);
        when(cursor.getIndex()).thenReturn(0, 1);

        OMFItem item = new PubdefRecordHandler(false).handle(cursor);
        PublicNamesDefinitionProcessed def = ((OMFItemPUBDEF) item).getDef();
        assertNull(def.baseFrame());
        verify(cursor, never()).getWord();
    }
}
