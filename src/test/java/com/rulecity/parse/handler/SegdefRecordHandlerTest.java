package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemSEGDEF;
import com.rulecity.parse.data.SegmentDefProcessed;
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
public class SegdefRecordHandlerTest
{
    @Mock private ByteCursor cursor;

    @Test
    public void parsesAttributesLengthAndIndices()
    {
        // attributes: A=2 (word-aligned, 010_xxxxx), C=2 (Public, xxx_010_xx), Big=0, P=0
        // 010_010_00 = 0x48
        when(cursor.getSignedByte()).thenReturn((byte) 0x48);
        when(cursor.getWord()).thenReturn(0x100);
        when(cursor.getIndex()).thenReturn(1, 2, 3);

        OMFItem item = new SegdefRecordHandler().handle(cursor);
        SegmentDefProcessed processed = ((OMFItemSEGDEF) item).getProcessed(List.of("_text", "CODE"));

        assertEquals(0x100, processed.length());
        assertEquals(OMFItemSEGDEF.Alignment.WORD_ALIGNED, processed.alignment());
        assertEquals(OMFItemSEGDEF.Combination.PUBLIC, processed.combination());
        assertEquals("_text", processed.nameSeg());
        assertEquals("CODE", processed.nameClass());
    }

    @Test
    public void absoluteAttributeThrows()
    {
        when(cursor.getSignedByte()).thenReturn((byte) 0x00); // A=0

        assertThrows(RuntimeException.class, () -> new SegdefRecordHandler().handle(cursor));
    }

    @Test
    public void bigAttribute_segmentLengthZero_processedSizeIs64K()
    {
        // attributes: A=1 (byte-aligned, 001_xxxxx), C=0 (private), Big=1
        // 001_000_10 = 0x22
        when(cursor.getSignedByte()).thenReturn((byte) 0x22);
        when(cursor.getWord()).thenReturn(0); // segmentLength=0 + Big=1 -> 64K
        when(cursor.getIndex()).thenReturn(1, 2, 0);

        OMFItem item = new SegdefRecordHandler().handle(cursor);
        SegmentDefProcessed processed = ((OMFItemSEGDEF) item).getProcessed(List.of("seg", "cls"));

        assertEquals(1 << 16, processed.length());
        assertEquals(OMFItemSEGDEF.Alignment.BYTE_ALIGNED, processed.alignment());
        assertEquals(OMFItemSEGDEF.Combination.PRIVATE, processed.combination());
    }

    @Test
    public void bigAttributeClear_segmentLengthZero_processedSizeIsZero()
    {
        // attributes: A=1, C=0, Big=0 -> 001_000_00 = 0x20.
        // The handler decodes Big from `(attributes & 2) != 0`. With AND→OR
        // mutation, the bit would be forced true and length would jump to 64K.
        when(cursor.getSignedByte()).thenReturn((byte) 0x20);
        when(cursor.getWord()).thenReturn(0);
        when(cursor.getIndex()).thenReturn(1, 2, 0);

        OMFItem item = new SegdefRecordHandler().handle(cursor);
        SegmentDefProcessed processed = ((OMFItemSEGDEF) item).getProcessed(List.of("seg", "cls"));

        assertEquals(0, processed.length());
    }

    @Test
    public void aValues1To3MapToCorrectAlignment()
    {
        // The handler uses an arithmetic right shift so A>=4 sign-extends; only A=1..3
        // round-trip cleanly through the handler. A=4..5 are exercised by OMFItemImplsTest
        // by constructing the impl directly.
        OMFItemSEGDEF.Alignment[] expected = {null,
                OMFItemSEGDEF.Alignment.BYTE_ALIGNED,
                OMFItemSEGDEF.Alignment.WORD_ALIGNED,
                OMFItemSEGDEF.Alignment.PARAGRAPH_ALIGNED};
        for (int aRaw = 1; aRaw <= 3; aRaw++)
        {
            ByteCursor c = org.mockito.Mockito.mock(ByteCursor.class);
            byte attrs = (byte) (aRaw << 5);
            when(c.getSignedByte()).thenReturn(attrs);
            when(c.getWord()).thenReturn(1);
            when(c.getIndex()).thenReturn(1, 2, 0);

            OMFItem item = new SegdefRecordHandler().handle(c);
            SegmentDefProcessed processed = ((OMFItemSEGDEF) item).getProcessed(List.of("a", "b"));
            assertEquals(expected[aRaw], processed.alignment());
        }
    }
}
