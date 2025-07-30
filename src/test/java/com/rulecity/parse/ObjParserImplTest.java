package com.rulecity.parse;

import com.rulecity.parse.data.Fixup;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ObjParserImplTest
{
    @Test
    public void COMENTTest()
    {
        // ARRANGE
        byte[] arrRecord = { b(0x88), 0x4, 0, 0, b(0xA2), 1, b(0xD1) };

        // ACT
        var instance = new ObjParserImpl();
        List<ObjItem> objItems = instance.parseBinary(arrRecord);

        // ASSERT
        assertEquals(1, objItems.size());
        ObjItem item = objItems.getFirst();
        ObjItemCOMENT itemCOMENT = (ObjItemCOMENT) item;
        assertEquals(arrRecord[3], itemCOMENT.getCommentType());
        assertEquals(arrRecord[4], itemCOMENT.getCommentClass());
        byte[] bytes = itemCOMENT.getBytes();
        assertEquals(arrRecord[5], bytes[0]);
    }

    @Test
    public void FIXUPPTest1()
    {
        // ARRANGE
        byte[] arrRecord = { b(0x9C), 0x9, 0, b(0x84), b(8), b(0x56),1,
        b(0x85),b(0x0C),b(0x56),1,b(0x90) };

        // ACT
        var instance = new ObjParserImpl();
        List<ObjItem> objItems = instance.parseBinary(arrRecord);

        // ASSERT
        assertEquals(1, objItems.size());
        ObjItem item = objItems.getFirst();
        ObjItemFIXUPP itemFIXUPP = (ObjItemFIXUPP) item;
        List<Fixup> fixups = itemFIXUPP.getFixups();
        assertEquals(2, fixups.size());

        Fixup fix1 = fixups.get(0);
        assertEquals(8, fix1.dataRecordOffset());
        assertEquals(5, fix1.frame());
        assertNull(fix1.frameDatum());
        assertFalse(fix1.frameSpecifiedByPreviousThreadFieldRef());
        assertEquals(1, fix1.location());
        assertFalse(fix1.segmentRelativeFixups());
        assertEquals(6, fix1.targt());
        assertEquals(b(1), fix1.targetDatum());
        assertNull(fix1.targetDisplacement());
        assertFalse(fix1.targetSpecifiedByPreviousThreadFieldRef());

        Fixup fix2 = fixups.get(1);
        assertEquals(0x10C, fix2.dataRecordOffset());
        assertEquals(5, fix2.frame());
        assertNull(fix2.frameDatum());
        assertFalse(fix2.frameSpecifiedByPreviousThreadFieldRef());
        assertEquals(1, fix2.location());
        assertFalse(fix2.segmentRelativeFixups());
        assertEquals(6, fix2.targt());
        assertEquals(b(1), fix2.targetDatum());
        assertNull(fix2.targetDisplacement());
        assertFalse(fix2.targetSpecifiedByPreviousThreadFieldRef());
    }

    @Test
    public void FIXUPPTest2()
    {
        // ARRANGE
        byte[] arrRecord = { b(0x9C), b(0x21), 0,
                b(0x84), b(1), b(6),b(1), b(2),
                b(0x80),b(4),b(0x6),1,b(2),
                b(0xCC),b(6),b(4),2,b(2),
                b(0xCC),b(0xB),b(6),1,b(1),
                b(0xC4),b(0x10),b(0),1,b(1),(0x15),0,
                b(0xC8),b(0x13),b(4),1,b(1),
                b(0xA3)
        };

        // ACT
        var instance = new ObjParserImpl();
        List<ObjItem> objItems = instance.parseBinary(arrRecord);

        // ASSERT
        assertEquals(1, objItems.size());
        ObjItem item = objItems.getFirst();
        ObjItemFIXUPP itemFIXUPP = (ObjItemFIXUPP) item;
        List<Fixup> fixups = itemFIXUPP.getFixups();
        assertEquals(6, fixups.size());

        Fixup fix = fixups.get(0);
        assertEquals(1, fix.dataRecordOffset());
        assertEquals(0, fix.frame());
        assertEquals(b(1), fix.frameDatum());
        assertFalse(fix.frameSpecifiedByPreviousThreadFieldRef());
        assertEquals(1, fix.location());
        assertFalse(fix.segmentRelativeFixups());
        assertEquals(6, fix.targt());
        assertEquals(b(2), fix.targetDatum());
        assertFalse(fix.targetSpecifiedByPreviousThreadFieldRef());
        assertNull(fix.targetDisplacement());

        fix = fixups.get(1);
        assertEquals(4, fix.dataRecordOffset());
        assertEquals(0, fix.frame());
        assertEquals(b(1), fix.frameDatum());
        assertFalse(fix.frameSpecifiedByPreviousThreadFieldRef());
        assertEquals(0, fix.location());
        assertFalse(fix.segmentRelativeFixups());
        assertEquals(6, fix.targt());
        assertEquals(b(2), fix.targetDatum());
        assertFalse(fix.targetSpecifiedByPreviousThreadFieldRef());
        assertNull(fix.targetDisplacement());

        fix = fixups.get(2);
        assertEquals(6, fix.dataRecordOffset());
        assertEquals(0, fix.frame());
        assertEquals(b(2), fix.frameDatum());
        assertFalse(fix.frameSpecifiedByPreviousThreadFieldRef());
        assertEquals(3, fix.location());
        assertTrue(fix.segmentRelativeFixups());
        assertEquals(4, fix.targt());
        assertEquals(b(2), fix.targetDatum());
        assertFalse(fix.targetSpecifiedByPreviousThreadFieldRef());
        assertNull(fix.targetDisplacement());

        fix = fixups.get(3);
        assertEquals(11, fix.dataRecordOffset());
        assertEquals(0, fix.frame());
        assertEquals(b(1), fix.frameDatum());
        assertFalse(fix.frameSpecifiedByPreviousThreadFieldRef());
        assertEquals(3, fix.location());
        assertTrue(fix.segmentRelativeFixups());
        assertEquals(6, fix.targt());
        assertEquals(b(1), fix.targetDatum());
        assertFalse(fix.targetSpecifiedByPreviousThreadFieldRef());
        assertNull(fix.targetDisplacement());

        fix = fixups.get(4);
        assertEquals(b(0x10), fix.dataRecordOffset());
        assertEquals(0, fix.frame());
        assertEquals(b(1), fix.frameDatum());
        assertFalse(fix.frameSpecifiedByPreviousThreadFieldRef());
        assertEquals(1, fix.location());
        assertTrue(fix.segmentRelativeFixups());
        assertEquals(0, fix.targt());
        assertEquals(b(1), fix.targetDatum());
        assertFalse(fix.targetSpecifiedByPreviousThreadFieldRef());
        assertEquals(0x15, fix.targetDisplacement());

        fix = fixups.get(5);
        assertEquals(b(19), fix.dataRecordOffset());
        assertEquals(0, fix.frame());
        assertEquals(b(1), fix.frameDatum());
        assertFalse(fix.frameSpecifiedByPreviousThreadFieldRef());
        assertEquals(2, fix.location());
        assertTrue(fix.segmentRelativeFixups());
        assertEquals(4, fix.targt());
        assertEquals(b(1), fix.targetDatum());
        assertFalse(fix.targetSpecifiedByPreviousThreadFieldRef());
        assertNull(fix.targetDisplacement());
    }

    // to avoid having to cast everything to byte
    public static byte b(int i)
    {
        return (byte) i;
    }
}
