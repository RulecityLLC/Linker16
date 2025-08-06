package com.rulecity.parse;

import com.rulecity.parse.data.Communal;
import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.data.Thread;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OMFParserImplTest
{
    @Test
    public void COMDEFTest1()
    {
        // ARRANGE
        byte[] arrRecord = { b(0xB0), b(0x20), 0, 4, b(0x5F), b(0x66), b(0x6F), b(0x6F), 0,
                    b(0x62), 2, 5, b(0x5f), b(0x66), b(0x6F), b(0x6F),
                b(0x32), 0, b(0x62), b(0x81), 0, b(0x80), 5, b(0x5F),
                b(0x66), b(0x6f), b(0x6f), b(0x33), 0, b(0x61), b(0x81), b(0x90),
                1, 1, b(0x99)
        };

        // ACT
        var instance = new OMFParserImpl();
        List<OMFItem> objItems = instance.parseBinary(arrRecord);

        // ASSERT
        assertEquals(1, objItems.size());
        OMFItem item = objItems.getFirst();
        var itemCOMDEF = (OMFItemCOMDEF) item;
        List<Communal> lstCommunal = itemCOMDEF.getCommualList();
        assertEquals(3, lstCommunal.size());

        Communal entry = lstCommunal.getFirst();
        assertEquals("_foo", entry.name());
        assertEquals(2, entry.length());

        entry = lstCommunal.get(1);
        assertEquals("_foo2", entry.name());
        assertEquals(32768, entry.length());

        entry = lstCommunal.get(2);
        assertEquals("_foo3", entry.name());
        assertEquals(400, entry.length());
    }

    @Test
    public void COMENTTest()
    {
        // ARRANGE
        byte[] arrRecord = { b(0x88), 0x4, 0, 0, b(0xA2), 1, b(0xD1) };

        // ACT
        var instance = new OMFParserImpl();
        List<OMFItem> objItems = instance.parseBinary(arrRecord);

        // ASSERT
        assertEquals(1, objItems.size());
        OMFItem item = objItems.getFirst();
        OMFItemCOMENT itemCOMENT = (OMFItemCOMENT) item;
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
        var instance = new OMFParserImpl();
        List<OMFItem> objItems = instance.parseBinary(arrRecord);

        // ASSERT
        assertEquals(1, objItems.size());
        OMFItem item = objItems.getFirst();
        OMFItemFIXUPP itemFIXUPP = (OMFItemFIXUPP) item;
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
        var instance = new OMFParserImpl();
        List<OMFItem> objItems = instance.parseBinary(arrRecord);

        // ASSERT
        assertEquals(1, objItems.size());
        OMFItem item = objItems.getFirst();
        OMFItemFIXUPP itemFIXUPP = (OMFItemFIXUPP) item;
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

    @Test
    public void FIXUPPTest3_Threads()
    {
        // ARRANGE
        byte[] arrRecord = {
                b(0x9C), b(0xD), 0, 0, 3, 1, 2, 2, 1, 3, 4, b(0x40), 1, b(0x45), 1, b(0xC0)
        };

        // ACT
        var instance = new OMFParserImpl();
        List<OMFItem> objItems = instance.parseBinary(arrRecord);

        // ASSERT
        assertEquals(1, objItems.size());
        OMFItem item = objItems.getFirst();
        OMFItemFIXUPP itemFIXUPP = (OMFItemFIXUPP) item;
        List<Fixup> fixups = itemFIXUPP.getFixups();
        List<Thread> threads = itemFIXUPP.getThreads();
        assertEquals(6, threads.size());

        Thread thread = threads.get(0);
        assertFalse(thread.threadFieldSpecifiesFrame());
        assertEquals(0, thread.method());
        assertEquals(0, thread.threadNum());
        assertEquals(3, thread.index());

        thread = threads.get(1);
        assertFalse(thread.threadFieldSpecifiesFrame());
        assertEquals(0, thread.method());
        assertEquals(1, thread.threadNum());
        assertEquals(2, thread.index());

        thread = threads.get(2);
        assertFalse(thread.threadFieldSpecifiesFrame());
        assertEquals(0, thread.method());
        assertEquals(2, thread.threadNum());
        assertEquals(1, thread.index());

        thread = threads.get(3);
        assertFalse(thread.threadFieldSpecifiesFrame());
        assertEquals(0, thread.method());
        assertEquals(3, thread.threadNum());
        assertEquals(4, thread.index());

        thread = threads.get(4);
        assertTrue(thread.threadFieldSpecifiesFrame());
        assertEquals(0, thread.method());
        assertEquals(0, thread.threadNum());
        assertEquals(1, thread.index());

        thread = threads.get(5);
        assertTrue(thread.threadFieldSpecifiesFrame());
        assertEquals(1, thread.method());
        assertEquals(1, thread.threadNum());
        assertEquals(1, thread.index());
    }

    @Test
    public void FIXUPPTest4()
    {
        // ARRANGE
        byte[] arrRecord = {
                b(0x9C), b(4), 0, b(0xC4), 9, b(0x9D), b(0xF6)
        };

        // ACT
        var instance = new OMFParserImpl();
        List<OMFItem> objItems = instance.parseBinary(arrRecord);

        // ASSERT
        assertEquals(1, objItems.size());
        OMFItem item = objItems.getFirst();
        OMFItemFIXUPP itemFIXUPP = (OMFItemFIXUPP) item;
        List<Fixup> fixups = itemFIXUPP.getFixups();
        List<Thread> threads = itemFIXUPP.getThreads();
        assertEquals(0, threads.size());
        assertEquals(1, fixups.size());

        Fixup fix = fixups.getFirst();
        assertEquals(9, fix.dataRecordOffset());
        assertEquals(1, fix.frame());
        assertNull(fix.frameDatum());
        assertTrue(fix.frameSpecifiedByPreviousThreadFieldRef());
        assertEquals(1, fix.location());
        assertTrue(fix.segmentRelativeFixups());
        assertEquals(5, fix.targt());
        assertNull(fix.targetDatum());
        assertTrue(fix.targetSpecifiedByPreviousThreadFieldRef());
        assertNull(fix.targetDisplacement());
    }

    @Test
    public void FIXUPPTest5()
    {
        // ARRANGE
        byte[] arrRecord = {
                b(0x9C), b(8), 0, b(0xC4), 2, b(2), 1,1,b(0x10),0,b(0x82)
        };

        // ACT
        var instance = new OMFParserImpl();
        List<OMFItem> objItems = instance.parseBinary(arrRecord);

        // ASSERT
        assertEquals(1, objItems.size());
        OMFItem item = objItems.getFirst();
        OMFItemFIXUPP itemFIXUPP = (OMFItemFIXUPP) item;
        List<Fixup> fixups = itemFIXUPP.getFixups();
        List<Thread> threads = itemFIXUPP.getThreads();
        assertEquals(0, threads.size());
        assertEquals(1, fixups.size());

        // some of these assertion values I didn't verify so if only this test is failing, double-check these values
        Fixup fix = fixups.getFirst();
        assertEquals(2, fix.dataRecordOffset());
        assertEquals(0, fix.frame());
        assertEquals(b(1), fix.frameDatum());
        assertFalse(fix.frameSpecifiedByPreviousThreadFieldRef());
        assertEquals(1, fix.location());
        assertTrue(fix.segmentRelativeFixups());
        assertEquals(2, fix.targt());
        assertEquals(b(1), fix.targetDatum());
        assertFalse(fix.targetSpecifiedByPreviousThreadFieldRef());
        assertEquals(0x10, fix.targetDisplacement());
    }

    @Test
    public void LEDATATest1()
    {
        // ARRANGE
        byte[] arrRecord = { b(0xA0), 8, 0, 1, 0,0,b(0x8D), b(0x1E), b(0x10), 0, b(0x9C) };

        // ACT
        var instance = new OMFParserImpl();
        List<OMFItem> objItems = instance.parseBinary(arrRecord);

        // ASSERT
        assertEquals(1, objItems.size());
        OMFItem item = objItems.getFirst();
        var itemLEDATA = (OMFItemLEDATA) item;
        assertEquals(arrRecord[3], itemLEDATA.getSegmentIdx());
        assertEquals(0, itemLEDATA.getEnumeratedDataOffset());
        byte[] arrBytes = itemLEDATA.getBytes();
        assertEquals(4, arrBytes.length);
        assertEquals(arrRecord[6], arrBytes[0]);
        assertEquals(arrRecord[7], arrBytes[1]);
        assertEquals(arrRecord[8], arrBytes[2]);
        assertEquals(arrRecord[9], arrBytes[3]);
    }

    // to avoid having to cast everything to byte
    public static byte b(int i)
    {
        return (byte) i;
    }
}
