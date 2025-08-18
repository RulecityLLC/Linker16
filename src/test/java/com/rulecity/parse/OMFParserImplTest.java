package com.rulecity.parse;

import com.rulecity.parse.data.*;
import com.rulecity.parse.data.Thread;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.rulecity.parse.OMFItemFIXUPP.FixupMethodFrame.*;
import static com.rulecity.parse.OMFItemFIXUPP.FixupMethodTarget.*;
import static com.rulecity.parse.OMFItemFIXUPP.Location.*;
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
        List<FixupOrThreadProcessed> fixupsOrThreadsProcessed = itemFIXUPP.getFixupsOrThreadsProcessed();
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

        FixupOrThreadProcessed fixupOrThreadProcessed1 = fixupsOrThreadsProcessed.get(0);
        FixupProcessed fixp1 = fixupOrThreadProcessed1.fixup();
        assertEquals(8, fixp1.dataRecordOffset());
        assertEquals(FRAME_SPECIFIED_BY_TARGET, fixp1.methodFrame());
        assertNull(fixp1.idxSegmentFrame());
        assertNull(fixp1.idxGroupFrame());
        assertNull(fixp1.idxExternalFrame());
        assertEquals(OFFSET_16BIT, fixp1.location());
        assertFalse(fixp1.segmentRelativeFixups());
        assertEquals(TARGET_SPECIFIED_BY_EXTDEF, fixp1.methodTarget());
        assertNull(fixp1.idxSegmentTarget());
        assertNull(fixp1.idxGroupTarget());
        assertEquals(0, fixp1.idxExternalTarget());
        assertNull(fixp1.targetDisplacement());

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

        FixupOrThreadProcessed fixupOrThreadProcessed2 = fixupsOrThreadsProcessed.get(1);
        FixupProcessed fixp2 = fixupOrThreadProcessed2.fixup();
        assertEquals(0x10C, fixp2.dataRecordOffset());
        assertEquals(FRAME_SPECIFIED_BY_TARGET, fixp2.methodFrame());
        assertNull(fixp2.idxSegmentFrame());
        assertNull(fixp2.idxGroupFrame());
        assertNull(fixp2.idxExternalFrame());
        assertEquals(OFFSET_16BIT, fixp2.location());
        assertFalse(fixp2.segmentRelativeFixups());
        assertEquals(TARGET_SPECIFIED_BY_EXTDEF, fixp2.methodTarget());
        assertNull(fixp2.idxSegmentTarget());
        assertNull(fixp2.idxGroupTarget());
        assertEquals(0, fixp2.idxExternalTarget());
        assertNull(fixp2.targetDisplacement());
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
        List<FixupOrThreadProcessed> fixupsOrThreadsProcessed = itemFIXUPP.getFixupsOrThreadsProcessed();
        assertEquals(6, fixups.size());

        // 0
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

        FixupOrThreadProcessed fixupOrThreadProcessed = fixupsOrThreadsProcessed.get(0);
        FixupProcessed fixp = fixupOrThreadProcessed.fixup();
        assertEquals(1, fixp.dataRecordOffset());
        assertEquals(FRAME_SPECIFIED_BY_SEGDEF, fixp.methodFrame());
        assertEquals(0, fixp.idxSegmentFrame());
        assertNull(fixp.idxGroupFrame());
        assertNull(fixp.idxExternalFrame());
        assertEquals(OFFSET_16BIT, fixp.location());
        assertFalse(fixp.segmentRelativeFixups());
        assertEquals(TARGET_SPECIFIED_BY_EXTDEF, fixp.methodTarget());
        assertNull(fixp.idxSegmentTarget());
        assertNull(fixp.idxGroupTarget());
        assertEquals(1, fixp.idxExternalTarget());
        assertNull(fixp.targetDisplacement());

        // 1
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

        fixupOrThreadProcessed = fixupsOrThreadsProcessed.get(1);
        fixp = fixupOrThreadProcessed.fixup();
        assertEquals(4, fixp.dataRecordOffset());
        assertEquals(FRAME_SPECIFIED_BY_SEGDEF, fixp.methodFrame());
        assertEquals(0, fixp.idxSegmentFrame());
        assertNull(fixp.idxGroupFrame());
        assertNull(fixp.idxExternalFrame());
        assertEquals(LOW_ORDER_BYTE, fixp.location());
        assertFalse(fixp.segmentRelativeFixups());
        assertEquals(TARGET_SPECIFIED_BY_EXTDEF, fixp.methodTarget());
        assertNull(fixp.idxSegmentTarget());
        assertNull(fixp.idxGroupTarget());
        assertEquals(1, fixp.idxExternalTarget());
        assertNull(fixp.targetDisplacement());

        // 2
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

        fixupOrThreadProcessed = fixupsOrThreadsProcessed.get(2);
        fixp = fixupOrThreadProcessed.fixup();
        assertEquals(6, fixp.dataRecordOffset());
        assertEquals(FRAME_SPECIFIED_BY_SEGDEF, fixp.methodFrame());
        assertEquals(1, fixp.idxSegmentFrame());
        assertNull(fixp.idxGroupFrame());
        assertNull(fixp.idxExternalFrame());
        assertEquals(POINTER, fixp.location());
        assertTrue(fixp.segmentRelativeFixups());
        assertEquals(TARGET_SPECIFIED_BY_SEGDEF, fixp.methodTarget());
        assertEquals(1, fixp.idxSegmentTarget());
        assertNull(fixp.idxGroupTarget());
        assertNull(fixp.idxExternalTarget());
        assertNull(fixp.targetDisplacement());

        // 3
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

        fixupOrThreadProcessed = fixupsOrThreadsProcessed.get(3);
        fixp = fixupOrThreadProcessed.fixup();
        assertEquals(11, fixp.dataRecordOffset());
        assertEquals(FRAME_SPECIFIED_BY_SEGDEF, fixp.methodFrame());
        assertEquals(0, fixp.idxSegmentFrame());
        assertNull(fixp.idxGroupFrame());
        assertNull(fixp.idxExternalFrame());
        assertEquals(POINTER, fixp.location());
        assertTrue(fixp.segmentRelativeFixups());
        assertEquals(TARGET_SPECIFIED_BY_EXTDEF, fixp.methodTarget());
        assertNull(fixp.idxSegmentTarget());
        assertNull(fixp.idxGroupTarget());
        assertEquals(0, fixp.idxExternalTarget());
        assertNull(fixp.targetDisplacement());

        // 4
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

        fixupOrThreadProcessed = fixupsOrThreadsProcessed.get(4);
        fixp = fixupOrThreadProcessed.fixup();
        assertEquals(0x10, fixp.dataRecordOffset());
        assertEquals(FRAME_SPECIFIED_BY_SEGDEF, fixp.methodFrame());
        assertEquals(0, fixp.idxSegmentFrame());
        assertNull(fixp.idxGroupFrame());
        assertNull(fixp.idxExternalFrame());
        assertEquals(OFFSET_16BIT, fixp.location());
        assertTrue(fixp.segmentRelativeFixups());
        assertEquals(TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT, fixp.methodTarget());
        assertEquals(0, fixp.idxSegmentTarget());
        assertNull(fixp.idxGroupTarget());
        assertNull(fixp.idxExternalTarget());
        assertEquals(0x15, fixp.targetDisplacement());

        // 5
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

        fixupOrThreadProcessed = fixupsOrThreadsProcessed.get(5);
        fixp = fixupOrThreadProcessed.fixup();
        assertEquals(19, fixp.dataRecordOffset());
        assertEquals(FRAME_SPECIFIED_BY_SEGDEF, fixp.methodFrame());
        assertEquals(0, fixp.idxSegmentFrame());
        assertNull(fixp.idxGroupFrame());
        assertNull(fixp.idxExternalFrame());
        assertEquals(SEGMENT, fixp.location());
        assertTrue(fixp.segmentRelativeFixups());
        assertEquals(TARGET_SPECIFIED_BY_SEGDEF, fixp.methodTarget());
        assertEquals(0, fixp.idxSegmentTarget());
        assertNull(fixp.idxGroupTarget());
        assertNull(fixp.idxExternalTarget());
        assertNull(fixp.targetDisplacement());
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
        List<Thread> threads = itemFIXUPP.getThreads();
        List<FixupOrThreadProcessed> fixupsOrThreadsProcessed = itemFIXUPP.getFixupsOrThreadsProcessed();
        assertEquals(6, threads.size());

        // 0
        Thread thread = threads.get(0);
        assertFalse(thread.threadFieldSpecifiesFrame());
        assertEquals(0, thread.method());
        assertEquals(0, thread.threadNum());
        assertEquals(3, thread.index());

        FixupOrThreadProcessed fixupOrThreadProcessed = fixupsOrThreadsProcessed.get(0);
        ThreadProcessed threadp = fixupOrThreadProcessed.thread();
        assertEquals(TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT, threadp.methodTarget());
        assertNull(threadp.methodFrame());
        assertEquals(0, threadp.threadNum());
        assertEquals(2, threadp.idxSegment());
        assertNull(threadp.idxGroup());
        assertNull(threadp.idxExternal());

        // 1
        thread = threads.get(1);
        assertFalse(thread.threadFieldSpecifiesFrame());
        assertEquals(0, thread.method());
        assertEquals(1, thread.threadNum());
        assertEquals(2, thread.index());

        fixupOrThreadProcessed = fixupsOrThreadsProcessed.get(1);
        threadp = fixupOrThreadProcessed.thread();
        assertEquals(TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT, threadp.methodTarget());
        assertNull(threadp.methodFrame());
        assertEquals(1, threadp.threadNum());
        assertEquals(1, threadp.idxSegment());
        assertNull(threadp.idxGroup());
        assertNull(threadp.idxExternal());

        // 2
        thread = threads.get(2);
        assertFalse(thread.threadFieldSpecifiesFrame());
        assertEquals(0, thread.method());
        assertEquals(2, thread.threadNum());
        assertEquals(1, thread.index());

        fixupOrThreadProcessed = fixupsOrThreadsProcessed.get(2);
        threadp = fixupOrThreadProcessed.thread();
        assertEquals(TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT, threadp.methodTarget());
        assertNull(threadp.methodFrame());
        assertEquals(2, threadp.threadNum());
        assertEquals(0, threadp.idxSegment());
        assertNull(threadp.idxGroup());
        assertNull(threadp.idxExternal());

        // 3
        thread = threads.get(3);
        assertFalse(thread.threadFieldSpecifiesFrame());
        assertEquals(0, thread.method());
        assertEquals(3, thread.threadNum());
        assertEquals(4, thread.index());

        fixupOrThreadProcessed = fixupsOrThreadsProcessed.get(3);
        threadp = fixupOrThreadProcessed.thread();
        assertEquals(TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT, threadp.methodTarget());
        assertNull(threadp.methodFrame());
        assertEquals(3, threadp.threadNum());
        assertEquals(3, threadp.idxSegment());
        assertNull(threadp.idxGroup());
        assertNull(threadp.idxExternal());

        // 4
        thread = threads.get(4);
        assertTrue(thread.threadFieldSpecifiesFrame());
        assertEquals(0, thread.method());
        assertEquals(0, thread.threadNum());
        assertEquals(1, thread.index());

        fixupOrThreadProcessed = fixupsOrThreadsProcessed.get(4);
        threadp = fixupOrThreadProcessed.thread();
        assertNull(threadp.methodTarget());
        assertEquals(FRAME_SPECIFIED_BY_SEGDEF, threadp.methodFrame());
        assertEquals(0, threadp.threadNum());
        assertEquals(0, threadp.idxSegment());
        assertNull(threadp.idxGroup());
        assertNull(threadp.idxExternal());

        // 5
        thread = threads.get(5);
        assertTrue(thread.threadFieldSpecifiesFrame());
        assertEquals(1, thread.method());
        assertEquals(1, thread.threadNum());
        assertEquals(1, thread.index());

        fixupOrThreadProcessed = fixupsOrThreadsProcessed.get(5);
        threadp = fixupOrThreadProcessed.thread();
        assertNull(threadp.methodTarget());
        assertEquals(FRAME_SPECIFIED_BY_GRPDEF, threadp.methodFrame());
        assertEquals(1, threadp.threadNum());
        assertNull(threadp.idxSegment());
        assertEquals(0, threadp.idxGroup());
        assertNull(threadp.idxExternal());
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
    public void FIXUPPTest6()
    {
        // ARRANGE

        // These bytes come from an actual FIXUPP record generated by Microsoft C compiler on some Dragon's Lair 2 source code.
        // They demonstrate a parsing problem in the current code.
        byte[] arrRecord = new byte[] {
                (byte)0x9C, (byte)0xF5, (byte)0x00,
                (byte)0xC7, (byte)0xAC, (byte)0x9D,
                (byte)0xC7, (byte)0xA6, (byte)0x56, (byte)0x49,
                (byte)0xC7, (byte)0x97, (byte)0x9D,
                (byte)0xC7, (byte)0x84, (byte)0x56, (byte)0x12,
                (byte)0xC7, (byte)0x74, (byte)0x56, (byte)0x49,
                (byte)0xC7, (byte)0x5F, (byte)0x56, (byte)0x49,
                (byte)0xC7, (byte)0x53, (byte)0x56, (byte)0x21,
                (byte)0xC7, (byte)0x4A, (byte)0x56, (byte)0x49,
                (byte)0xC7, (byte)0x40, (byte)0x9D,
                (byte)0xC7, (byte)0x2E, (byte)0x56, (byte)0x21,
                (byte)0xC7, (byte)0x1D, (byte)0x9D,
                (byte)0xC7, (byte)0x13, (byte)0x56, (byte)0x21,
                (byte)0xC7, (byte)0x0C, (byte)0x56, (byte)0x49,
                (byte)0xC7, (byte)0x06, (byte)0x56, (byte)0x21,
                (byte)0xC6, (byte)0xEB, (byte)0x56, (byte)0x49,
                (byte)0xC6, (byte)0xDA, (byte)0x56, (byte)0x49,
                (byte)0xC6, (byte)0xD3, (byte)0x56, (byte)0x7F,
                (byte)0xC6, (byte)0xA7, (byte)0x56, (byte)0x21,
                (byte)0xC6, (byte)0xA0, (byte)0x56, (byte)0x1A,
                (byte)0xC6, (byte)0x97, (byte)0x56, (byte)0x5A,
                (byte)0xC6, (byte)0x8D, (byte)0x56, (byte)0x57,
                (byte)0xC6, (byte)0x86, (byte)0x56, (byte)0x49,
                (byte)0xC6, (byte)0x7F, (byte)0x56, (byte)0x21,
                (byte)0xC6, (byte)0x73, (byte)0x56, (byte)0x5A,
                (byte)0xC6, (byte)0x6D, (byte)0x56, (byte)0x57,
                (byte)0xC6, (byte)0x69, (byte)0x56, (byte)0x7F,
                (byte)0xC6, (byte)0x66, (byte)0x9D,
                (byte)0xC6, (byte)0x63, (byte)0x9D,
                (byte)0xC6, (byte)0x21, (byte)0x56, (byte)0x2D,
                (byte)0xC5, (byte)0xF9, (byte)0x56, (byte)0x2D,
                (byte)0xC5, (byte)0xEE, (byte)0x56, (byte)0x49,
                (byte)0xC5, (byte)0xDD, (byte)0x9D,
                (byte)0xC5, (byte)0xD8, (byte)0x56, (byte)0x2D,
                (byte)0xC5, (byte)0xBB, (byte)0x56, (byte)0x49,
                (byte)0xC5, (byte)0xB5, (byte)0x56, (byte)0x21,
                (byte)0xC5, (byte)0xA5, (byte)0x9D,
                (byte)0xC5, (byte)0xA1, (byte)0x9D,
                (byte)0xC5, (byte)0x9D, (byte)0x9D,
                (byte)0xC5, (byte)0x98, (byte)0x9D,
                (byte)0xC5, (byte)0x8D, (byte)0x9D,
                (byte)0xC5, (byte)0x89, (byte)0x9D,
                (byte)0xC5, (byte)0x85, (byte)0x9D,
                (byte)0xC5, (byte)0x80, (byte)0x9D,
                (byte)0xC5, (byte)0x79, (byte)0x9D,
                (byte)0x85, (byte)0x60, (byte)0x56, (byte)0x02,
                (byte)0x85, (byte)0x56, (byte)0x56, (byte)0x02,
                (byte)0xC5, (byte)0x50, (byte)0x9D,
                (byte)0xC5, (byte)0x4D, (byte)0x9D,

                // We do not currently parse the following bytes properly.
                // I can't find anything in the official spec that explains the trailing 0x9C.
                //  From what I can tell, it should not be there.
                //  So for now, I am blocked until I know how to handle it.
                (byte)0xC5, (byte)0x45, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC5, (byte)0x31, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC5, (byte)0x26, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC5, (byte)0x0B, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC5, (byte)0x06, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC4, (byte)0xDA, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC4, (byte)0xB5, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC4, (byte)0xB0, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC4, (byte)0x81, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC4, (byte)0x6C, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC4, (byte)0x55, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC4, (byte)0x2F, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC4, (byte)0x1A, (byte)0x56, (byte)0x80, (byte)0x9C,
                (byte)0xC4, (byte)0x03, (byte)0x56, (byte)0x80, (byte)0x9C,

                // checksum
                (byte)0x35
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
        assertEquals(62, fixups.size());
    }

    @Test
    public void FIXUPPTest8()
    {
        // This is another fixup record where the target datum is 0x80 and another byte trails it.
        // I haven't found any docs that explain what to do in this case.

        // ARRANGE
        byte[] arrRecord = {
                b(0x9C), b(6), 0, b(0x85), b(0x77), b(0x56), b(0x80),b(0x8E),b(0xFE)
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

        // we can add more
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
