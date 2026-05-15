package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemFIXUPP;
import com.rulecity.parse.OMFItemFIXUPPImpl;
import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.data.FixupOrThread;
import com.rulecity.parse.data.FixupOrThreadProcessed;
import com.rulecity.parse.io.ByteCursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FixuppRecordHandlerTest
{
    @Mock private FixupReader fixupReader;
    @Mock private FixupOrThreadProcessor fixupOrThreadProcessor;
    @Mock private ByteCursor cursor;
    @Mock private Fixup mockFixup;
    @Mock private FixupOrThreadProcessed mockProcessed;
    @InjectMocks private FixuppRecordHandler handler;

    @Test
    public void singleFixup_delegatesToFixupReaderAndProcessor()
    {
        // firstByte 0x84 -> high bit set => FIXUP, segmentRelative=0, location=1
        // locat second byte 0x10 => dataRecordOffset = ((0x84<<8)|0x10) & 1023 = 0x010
        when(cursor.getRecordLength()).thenReturn(5);
        when(cursor.getRecordCount()).thenReturn(0, 5);
        when(cursor.getUnsignedByteAsInt()).thenReturn(0x84, 0x10);
        when(fixupReader.readFixup(eq(cursor), anyBoolean(), anyByte(), anyInt())).thenReturn(mockFixup);
        when(fixupOrThreadProcessor.process(any(FixupOrThread.class))).thenReturn(mockProcessed);

        OMFItem result = handler.handle(cursor);

        verify(fixupReader, times(1)).readFixup(eq(cursor), eq(false), eq((byte) 1), eq(0x10));
        verify(fixupOrThreadProcessor, times(1)).process(any(FixupOrThread.class));
        OMFItemFIXUPP fixupp = (OMFItemFIXUPP) result;
        assertEquals(1, fixupp.getFixups().size());
        assertSame(mockFixup, fixupp.getFixups().get(0));
        assertEquals(1, fixupp.getFixupsOrThreadsProcessed().size());
        assertSame(mockProcessed, fixupp.getFixupsOrThreadsProcessed().get(0));
    }

    @Test
    public void singleThread_doesNotCallFixupReader()
    {
        // firstByte 0x40 -> high bit clear => THREAD; threadFieldSpecifiesFrame=1, method=0, threadNum=0
        when(cursor.getRecordLength()).thenReturn(3);
        when(cursor.getRecordCount()).thenReturn(0, 3);
        when(cursor.getUnsignedByteAsInt()).thenReturn(0x40);
        when(cursor.getIndex()).thenReturn(5);
        when(fixupOrThreadProcessor.process(any(FixupOrThread.class))).thenReturn(mockProcessed);

        OMFItem result = handler.handle(cursor);

        verify(fixupReader, never()).readFixup(any(), anyBoolean(), anyByte(), anyInt());
        OMFItemFIXUPP fixupp = (OMFItemFIXUPP) result;
        assertEquals(1, fixupp.getThreads().size());
        assertEquals(0, fixupp.getFixups().size());
        assertNotNull(fixupp.getThreads().get(0));
        assertTrue(fixupp.getThreads().get(0).threadFieldSpecifiesFrame());
        assertEquals(5, fixupp.getThreads().get(0).index());
    }

    @Test
    public void emptyRecord_returnsEmptyImpl()
    {
        when(cursor.getRecordLength()).thenReturn(1);
        when(cursor.getRecordCount()).thenReturn(0);

        OMFItem result = handler.handle(cursor);
        OMFItemFIXUPPImpl fixupp = (OMFItemFIXUPPImpl) result;
        assertEquals(0, fixupp.getFixupsOrThreads().size());
        assertEquals(0, fixupp.getFixupsOrThreadsProcessed().size());
    }

    @Test
    public void verifiesFixupOrThreadIsPairedWithRawEntry()
    {
        when(cursor.getRecordLength()).thenReturn(3);
        when(cursor.getRecordCount()).thenReturn(0, 3);
        when(cursor.getUnsignedByteAsInt()).thenReturn(0x40);
        when(cursor.getIndex()).thenReturn(7);
        when(fixupOrThreadProcessor.process(any(FixupOrThread.class))).thenReturn(mockProcessed);

        OMFItem result = handler.handle(cursor);

        OMFItemFIXUPPImpl fixupp = (OMFItemFIXUPPImpl) result;
        FixupOrThread raw = fixupp.getFixupsOrThreads().get(0);
        assertNull(raw.fixup());
        assertNotNull(raw.thread());
        assertEquals(7, raw.thread().index());
    }
}
