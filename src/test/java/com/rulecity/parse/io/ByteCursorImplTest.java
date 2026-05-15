package com.rulecity.parse.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the value-object cursor. No collaborators to mock — the cursor
 * is the bottom of the parser stack.
 */
public class ByteCursorImplTest
{
    @Test
    public void getIndex_oneByteForm_returnsLowByteValue()
    {
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{0x7F});
        cursor.beginRecord();
        cursor.markStartOfPayload(1);
        assertEquals(0x7F, cursor.getIndex());
    }

    @Test
    public void getIndex_twoByteForm_decodesPerTIS()
    {
        // ((0xC0 & 0x7F) << 8) | 0x55 = 0x4055
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{(byte) 0xC0, 0x55});
        cursor.beginRecord();
        cursor.markStartOfPayload(2);
        assertEquals(0x4055, cursor.getIndex());
    }

    @Test
    public void getCommunalField_smallValueIsLiteral()
    {
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{0x7F});
        cursor.beginRecord();
        cursor.markStartOfPayload(1);
        assertEquals(0x7F, cursor.getCommunalField());
    }

    @Test
    public void getCommunalField_0x81_readsTwoByteWord()
    {
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{(byte) 0x81, 0x34, 0x12});
        cursor.beginRecord();
        cursor.markStartOfPayload(3);
        assertEquals(0x1234, cursor.getCommunalField());
    }

    @Test
    public void getCommunalField_0x84_readsThreeByteValue()
    {
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{(byte) 0x84, 0x11, 0x22, 0x33});
        cursor.beginRecord();
        cursor.markStartOfPayload(4);
        assertEquals(0x332211, cursor.getCommunalField());
    }

    @Test
    public void getCommunalField_0x88_readsFourByteValue()
    {
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{(byte) 0x88, 0x44, 0x33, 0x22, 0x11});
        cursor.beginRecord();
        cursor.markStartOfPayload(5);
        assertEquals(0x11223344, cursor.getCommunalField());
    }

    @Test
    public void getWord_isLittleEndian()
    {
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{0x34, 0x12});
        cursor.beginRecord();
        cursor.markStartOfPayload(2);
        assertEquals(0x1234, cursor.getWord());
    }

    @Test
    public void getSignedByte_updatesChecksumAndCount()
    {
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{0x05, 0x03});
        cursor.beginRecord();
        cursor.markStartOfPayload(2);
        cursor.getSignedByte();
        cursor.getSignedByte();
        // checksum = -(0x05 + 0x03) = -8 = 0xF8
        assertEquals((byte) 0xF8, cursor.getChecksum());
        assertEquals(2, cursor.getRecordCount());
    }

    @Test
    public void readRawByte_doesNotAffectChecksumOrRecordCount()
    {
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{0x05, 0x03});
        cursor.beginRecord();
        cursor.markStartOfPayload(2);
        cursor.readRawByte();
        cursor.readRawByte();
        assertEquals(0, cursor.getRecordCount());
        assertEquals((byte) 0, cursor.getChecksum());
    }

    @Test
    public void markStartOfPayload_preservesChecksumButZeroesCount()
    {
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{0x05, 0x03, 0x07});
        cursor.beginRecord();
        cursor.getSignedByte(); // simulates reading type
        cursor.getSignedByte(); // simulates reading length lo
        // running checksum so far = -(0x05+0x03) = 0xF8
        byte checksumBeforeMark = cursor.getChecksum();
        cursor.markStartOfPayload(5);
        assertEquals(checksumBeforeMark, cursor.getChecksum(), "checksum preserved across markStartOfPayload");
        assertEquals(0, cursor.getRecordCount(), "recordCount zeroed at start of payload");
        assertEquals(5, cursor.getRecordLength());
    }

    @Test
    public void beginRecord_zeroesChecksumAndCount()
    {
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{0x05, 0x03});
        cursor.beginRecord();
        cursor.markStartOfPayload(2);
        cursor.getSignedByte();
        cursor.beginRecord();
        assertEquals((byte) 0, cursor.getChecksum());
        assertEquals(0, cursor.getRecordCount());
    }

    @Test
    public void hasMore_tracksGlobalPosition()
    {
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{0x01, 0x02});
        cursor.beginRecord();
        cursor.markStartOfPayload(2);
        assertTrue(cursor.hasMore());
        cursor.getSignedByte();
        assertTrue(cursor.hasMore());
        cursor.getSignedByte();
        assertFalse(cursor.hasMore());
    }

    @Test
    public void readRawByte_returnsExactByteValue()
    {
        // Pin both bytes so a "return 0" mutation is detectable.
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{0x42, (byte) 0xA7});
        assertEquals((byte) 0x42, cursor.readRawByte());
        assertEquals((byte) 0xA7, cursor.readRawByte());
    }

    @Test
    public void getCommunalField_0x80_isUnknownLeadByte_returnsZero()
    {
        // The boundary in `if (val < 0x80) return val;` distinguishes 0x7F from 0x80.
        // 0x80 isn't a recognised lead byte (0x81/0x84/0x88), so it falls through
        // every branch and returns 0.
        ByteCursorImpl cursor = new ByteCursorImpl(new byte[]{(byte) 0x80});
        cursor.beginRecord();
        cursor.markStartOfPayload(1);
        assertEquals(0, cursor.getCommunalField());
    }
}
