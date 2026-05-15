package com.rulecity.link.emit;

import com.rulecity.parse.OMFItemFIXUPP.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FixupValueWriterImplTest
{
    private final FixupValueWriterImpl writer = new FixupValueWriterImpl(0x0008);

    @Test
    public void offset16BitDgroupFrame_writesTargetImageOffsetAsAddend()
    {
        // DGROUP frame ⇒ frameStart = 0 ⇒ value written = targetImageOffset
        // added to existing bytes.
        byte[] image = new byte[8];
        image[2] = 0x0C;  // existing addend (low byte of 0x000C)
        writer.write(image, 2, Location.OFFSET_16BIT, true, 0x0008, 0x55B2);
        // 0x000C + 0x55B2 = 0x55BE
        assertEquals((byte) 0xBE, image[2]);
        assertEquals((byte) 0x55, image[3]);
    }

    @Test
    public void segmentLocation_writesFrameParagraph_overExistingZero()
    {
        byte[] image = new byte[4];
        writer.write(image, 0, Location.SEGMENT, true, 0x0008, 0x1234);
        assertEquals((byte) 0x08, image[0]);
        assertEquals((byte) 0x00, image[1]);
    }

    @Test
    public void pointerLocation_writesOffsetThenSegment()
    {
        byte[] image = new byte[6];
        // DGROUP frame ⇒ offsetWithinFrame = targetImageOffset = 0x0042
        writer.write(image, 0, Location.POINTER, true, 0x0008, 0x0042);
        assertArrayEquals(new byte[] { 0x42, 0x00, 0x08, 0x00, 0x00, 0x00 }, image);
    }

    @Test
    public void selfRelativeOffset16Bit_subtractsLocationPlusTwo()
    {
        // CALL near at image offset 0x100, target at 0x150. operand at 0x101.
        // value = 0x150 - (0x101 + 2) = 0x4D
        byte[] image = new byte[0x200];
        writer.write(image, 0x101, Location.OFFSET_16BIT, false, 0x0008, 0x150);
        assertEquals((byte) 0x4D, image[0x101]);
        assertEquals((byte) 0x00, image[0x102]);
    }

    @Test
    public void lowOrderByte_segmentRelative_addsLowByteOfDelta()
    {
        byte[] image = new byte[4];
        image[0] = 0x10;  // existing addend (low byte)
        writer.write(image, 0, Location.LOW_ORDER_BYTE, true, 0x0008, 0x0030);
        // delta = 0x30, plus existing 0x10 = 0x40
        assertEquals((byte) 0x40, image[0]);
    }

    @Test
    public void highOrderByte_segmentRelative_addsHighByteOfDelta()
    {
        byte[] image = new byte[4];
        writer.write(image, 0, Location.HIGH_ORDER_BYTE, true, 0x0008, 0x0234);
        // delta = 0x0234, high byte = 0x02
        assertEquals((byte) 0x02, image[0]);
    }

    @Test
    public void segmentFrameNonDgroup_subtractsFrameOffsetFromTarget()
    {
        // Hypothetical: frame paragraph = 0x0018 ⇒ frameStart = (0x18 - 0x08) << 4 = 0x100
        // Target image offset = 0x123 ⇒ offsetWithinFrame = 0x023
        byte[] image = new byte[4];
        writer.write(image, 0, Location.OFFSET_16BIT, true, 0x0018, 0x123);
        assertEquals((byte) 0x23, image[0]);
        assertEquals((byte) 0x00, image[1]);
    }
}
