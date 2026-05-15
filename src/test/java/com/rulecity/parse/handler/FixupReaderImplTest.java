package com.rulecity.parse.handler;

import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.io.ByteCursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FixupReaderImplTest
{
    @Mock private ByteCursor cursor;
    @InjectMocks private FixupReaderImpl fixupReader;

    @Test
    public void typicalSegmentTargetFixup_readsAllFields()
    {
        // fixDat = 0x06 -> F=0, frame=0 (SEGDEF), T=0, targt=6 (EXTDEF, P=1 -> no displacement)
        when(cursor.getSignedByte()).thenReturn((byte) 0x06);
        when(cursor.getIndex()).thenReturn(2, 7); // frameDatum, targetDatum

        Fixup fix = fixupReader.readFixup(cursor, false, (byte) 1, 0x100);

        assertEquals(0x100, fix.dataRecordOffset());
        assertEquals((byte) 1, fix.location());
        assertFalse(fix.segmentRelativeFixups());
        assertFalse(fix.frameSpecifiedByPreviousThreadFieldRef());
        assertFalse(fix.targetSpecifiedByPreviousThreadFieldRef());
        assertEquals(0, fix.frame());
        assertEquals(6, fix.targt());
        assertEquals(2, fix.frameDatum());
        assertEquals(7, fix.targetDatum());
        assertNull(fix.targetDisplacement(), "P=1 means no displacement");
    }

    @Test
    public void frameThreadReference_skipsFrameDatum()
    {
        // fixDat = 0x86 -> F=1 (frame from thread), frame thread=0, T=0, targt=6 (P=1)
        when(cursor.getSignedByte()).thenReturn((byte) 0x86);
        when(cursor.getIndex()).thenReturn(7); // only targetDatum, no frameDatum

        Fixup fix = fixupReader.readFixup(cursor, false, (byte) 1, 0);

        assertTrue(fix.frameSpecifiedByPreviousThreadFieldRef());
        assertNull(fix.frameDatum());
        assertEquals(7, fix.targetDatum());
    }

    @Test
    public void targetWithoutP_readsDisplacement()
    {
        // fixDat = 0x02 -> F=0, frame=0 (SEGDEF), T=0, targt=2 (EXTDEF_WITH_DISPLACEMENT, P=0)
        when(cursor.getSignedByte()).thenReturn((byte) 0x02);
        when(cursor.getIndex()).thenReturn(1, 1);
        when(cursor.getWord()).thenReturn(0x1234);

        Fixup fix = fixupReader.readFixup(cursor, true, (byte) 0, 0);

        assertEquals(0x1234, fix.targetDisplacement());
    }

    @Test
    public void targt3_isRejected()
    {
        // fixDat low nibble 0x03 -> (targt & 3) == 3, currently unsupported.
        when(cursor.getSignedByte()).thenReturn((byte) 0x03);

        assertThrows(RuntimeException.class, () -> fixupReader.readFixup(cursor, false, (byte) 0, 0));
    }

    @Test
    public void frame_greaterThan2_skipsFrameDatumRead()
    {
        // fixDat = 0x56 -> F=0, frame=5 (FRAME_BY_TARGET, no frameDatum), T=0, targt=6 (P=1)
        when(cursor.getSignedByte()).thenReturn((byte) 0x56);
        when(cursor.getIndex()).thenReturn(7); // only targetDatum

        Fixup fix = fixupReader.readFixup(cursor, false, (byte) 1, 0);

        assertEquals(5, fix.frame());
        assertNull(fix.frameDatum());
        assertEquals(7, fix.targetDatum());
    }
}
