package com.rulecity.parse;

import com.rulecity.Main;
import com.rulecity.parse.data.Communal;
import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.data.FixupOrThreadProcessed;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.Thread;
import com.rulecity.parse.data.ThreadProcessed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.rulecity.parse.OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_GRPDEF;
import static com.rulecity.parse.OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF;
import static com.rulecity.parse.OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_TARGET;
import static com.rulecity.parse.OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_EXTDEF;
import static com.rulecity.parse.OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF;
import static com.rulecity.parse.OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT;
import static com.rulecity.parse.OMFItemFIXUPP.Location.LOW_ORDER_BYTE;
import static com.rulecity.parse.OMFItemFIXUPP.Location.OFFSET_16BIT;
import static com.rulecity.parse.OMFItemFIXUPP.Location.POINTER;
import static com.rulecity.parse.OMFItemFIXUPP.Location.SEGMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end regression tests for the parser layer: real byte sequences (most
 * sourced from MS-C output that previously broke the parser) fed through the
 * full DI-assembled parser. Lives in test-integration because each test
 * exercises the parser + its injected handlers + cursor + fixup reader +
 * fixup/thread processor together — i.e. multiple production classes —
 * even though no file is read from disk.
 */
public class OMFParserPipelineIT
{
    private static OMFParser parser()
    {
        return Main.buildParser();
    }

    @Test
    public void COMDEFTest1()
    {
        byte[] arrRecord = { b(0xB0), b(0x20), 0, 4, b(0x5F), b(0x66), b(0x6F), b(0x6F), 0,
                b(0x62), 2, 5, b(0x5f), b(0x66), b(0x6F), b(0x6F),
                b(0x32), 0, b(0x62), b(0x81), 0, b(0x80), 5, b(0x5F),
                b(0x66), b(0x6f), b(0x6f), b(0x33), 0, b(0x61), b(0x81), b(0x90),
                1, 1, b(0x99) };

        List<OMFItem> objItems = parser().parseBinary(arrRecord);

        assertEquals(1, objItems.size());
        var itemCOMDEF = (OMFItemCOMDEF) objItems.getFirst();
        List<Communal> lstCommunal = itemCOMDEF.getCommualList();
        assertEquals(3, lstCommunal.size());

        assertEquals("_foo", lstCommunal.get(0).name());
        assertEquals(2, lstCommunal.get(0).length());
        assertEquals("_foo2", lstCommunal.get(1).name());
        assertEquals(32768, lstCommunal.get(1).length());
        assertEquals("_foo3", lstCommunal.get(2).name());
        assertEquals(400, lstCommunal.get(2).length());
    }

    @Test
    public void COMENTTest()
    {
        byte[] arrRecord = { b(0x88), 0x4, 0, 0, b(0xA2), 1, b(0xD1) };
        List<OMFItem> objItems = parser().parseBinary(arrRecord);
        OMFItemCOMENT itemCOMENT = (OMFItemCOMENT) objItems.getFirst();
        assertEquals(arrRecord[3], itemCOMENT.getCommentType());
        assertEquals(arrRecord[4], itemCOMENT.getCommentClass());
        assertEquals(arrRecord[5], itemCOMENT.getBytes()[0]);
    }

    @Test
    public void FIXUPPTest1()
    {
        byte[] arrRecord = { b(0x9C), 0x9, 0, b(0x84), b(8), b(0x56), 1,
                b(0x85), b(0x0C), b(0x56), 1, b(0x90) };

        List<OMFItem> objItems = parser().parseBinary(arrRecord);
        OMFItemFIXUPP itemFIXUPP = (OMFItemFIXUPP) objItems.getFirst();
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
        assertEquals(1, fix1.targetDatum());
        assertNull(fix1.targetDisplacement());
        assertFalse(fix1.targetSpecifiedByPreviousThreadFieldRef());

        FixupProcessed fixp1 = fixupsOrThreadsProcessed.get(0).fixup();
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

        FixupProcessed fixp2 = fixupsOrThreadsProcessed.get(1).fixup();
        assertEquals(0x10C, fixp2.dataRecordOffset());
        assertEquals(FRAME_SPECIFIED_BY_TARGET, fixp2.methodFrame());
        assertEquals(OFFSET_16BIT, fixp2.location());
        assertEquals(TARGET_SPECIFIED_BY_EXTDEF, fixp2.methodTarget());
        assertEquals(0, fixp2.idxExternalTarget());
    }

    @Test
    public void FIXUPPTest2()
    {
        byte[] arrRecord = { b(0x9C), b(0x21), 0,
                b(0x84), b(1), b(6), b(1), b(2),
                b(0x80), b(4), b(0x6), 1, b(2),
                b(0xCC), b(6), b(4), 2, b(2),
                b(0xCC), b(0xB), b(6), 1, b(1),
                b(0xC4), b(0x10), b(0), 1, b(1), (0x15), 0,
                b(0xC8), b(0x13), b(4), 1, b(1),
                b(0xA3) };

        List<OMFItem> objItems = parser().parseBinary(arrRecord);
        OMFItemFIXUPP itemFIXUPP = (OMFItemFIXUPP) objItems.getFirst();
        List<Fixup> fixups = itemFIXUPP.getFixups();
        List<FixupOrThreadProcessed> fixupsOrThreadsProcessed = itemFIXUPP.getFixupsOrThreadsProcessed();
        assertEquals(6, fixups.size());

        // Spot-check a few:
        FixupProcessed fixp = fixupsOrThreadsProcessed.get(0).fixup();
        assertEquals(1, fixp.dataRecordOffset());
        assertEquals(FRAME_SPECIFIED_BY_SEGDEF, fixp.methodFrame());
        assertEquals(0, fixp.idxSegmentFrame());
        assertEquals(OFFSET_16BIT, fixp.location());
        assertEquals(TARGET_SPECIFIED_BY_EXTDEF, fixp.methodTarget());
        assertEquals(1, fixp.idxExternalTarget());

        fixp = fixupsOrThreadsProcessed.get(1).fixup();
        assertEquals(LOW_ORDER_BYTE, fixp.location());

        fixp = fixupsOrThreadsProcessed.get(2).fixup();
        assertEquals(POINTER, fixp.location());
        assertEquals(TARGET_SPECIFIED_BY_SEGDEF, fixp.methodTarget());

        fixp = fixupsOrThreadsProcessed.get(4).fixup();
        assertEquals(TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT, fixp.methodTarget());
        assertEquals(0x15, fixp.targetDisplacement());

        fixp = fixupsOrThreadsProcessed.get(5).fixup();
        assertEquals(SEGMENT, fixp.location());
    }

    @Test
    public void FIXUPPTest3_Threads()
    {
        byte[] arrRecord = {
                b(0x9C), b(0xD), 0, 0, 3, 1, 2, 2, 1, 3, 4, b(0x40), 1, b(0x45), 1, b(0xC0)
        };

        List<OMFItem> objItems = parser().parseBinary(arrRecord);
        OMFItemFIXUPP itemFIXUPP = (OMFItemFIXUPP) objItems.getFirst();
        List<Thread> threads = itemFIXUPP.getThreads();
        List<FixupOrThreadProcessed> fixupsOrThreadsProcessed = itemFIXUPP.getFixupsOrThreadsProcessed();
        assertEquals(6, threads.size());

        ThreadProcessed threadp = fixupsOrThreadsProcessed.get(4).thread();
        assertEquals(FRAME_SPECIFIED_BY_SEGDEF, threadp.methodFrame());
        assertEquals(0, threadp.threadNum());
        assertEquals(0, threadp.idxSegment());

        threadp = fixupsOrThreadsProcessed.get(5).thread();
        assertEquals(FRAME_SPECIFIED_BY_GRPDEF, threadp.methodFrame());
        assertEquals(1, threadp.threadNum());
        assertEquals(0, threadp.idxGroup());
    }

    @Test
    public void FIXUPPTest4()
    {
        byte[] arrRecord = { b(0x9C), b(4), 0, b(0xC4), 9, b(0x9D), b(0xF6) };

        List<OMFItem> objItems = parser().parseBinary(arrRecord);
        OMFItemFIXUPP itemFIXUPP = (OMFItemFIXUPP) objItems.getFirst();
        List<Fixup> fixups = itemFIXUPP.getFixups();
        assertEquals(0, itemFIXUPP.getThreads().size());
        assertEquals(1, fixups.size());

        Fixup fix = fixups.getFirst();
        assertEquals(9, fix.dataRecordOffset());
        assertTrue(fix.frameSpecifiedByPreviousThreadFieldRef());
        assertTrue(fix.segmentRelativeFixups());
        assertTrue(fix.targetSpecifiedByPreviousThreadFieldRef());
    }

    @Test
    public void FIXUPPTest5()
    {
        byte[] arrRecord = { b(0x9C), b(8), 0, b(0xC4), 2, b(2), 1, 1, b(0x10), 0, b(0x82) };

        List<OMFItem> objItems = parser().parseBinary(arrRecord);
        OMFItemFIXUPP itemFIXUPP = (OMFItemFIXUPP) objItems.getFirst();
        List<Fixup> fixups = itemFIXUPP.getFixups();
        assertEquals(1, fixups.size());

        Fixup fix = fixups.getFirst();
        assertEquals(0x10, fix.targetDisplacement());
    }

    @Test
    public void FIXUPPTest6_TwoByteTargetDatum()
    {
        // Bytes captured from a real Microsoft C compiler FIXUPP record. The last 14 fixups
        // carry a 2-byte target-datum index (TIS 1.1 sec 2.2): the high-bit-set first byte
        // (0x80) means "two-byte form"; the value is ((firstByte & 0x7F) << 8) | secondByte.
        // Here every one decodes to 0x9C = 156.
        byte[] arrRecord = new byte[] {
                (byte) 0x9C, (byte) 0xF5, (byte) 0x00,
                (byte) 0xC7, (byte) 0xAC, (byte) 0x9D,
                (byte) 0xC7, (byte) 0xA6, (byte) 0x56, (byte) 0x49,
                (byte) 0xC7, (byte) 0x97, (byte) 0x9D,
                (byte) 0xC7, (byte) 0x84, (byte) 0x56, (byte) 0x12,
                (byte) 0xC7, (byte) 0x74, (byte) 0x56, (byte) 0x49,
                (byte) 0xC7, (byte) 0x5F, (byte) 0x56, (byte) 0x49,
                (byte) 0xC7, (byte) 0x53, (byte) 0x56, (byte) 0x21,
                (byte) 0xC7, (byte) 0x4A, (byte) 0x56, (byte) 0x49,
                (byte) 0xC7, (byte) 0x40, (byte) 0x9D,
                (byte) 0xC7, (byte) 0x2E, (byte) 0x56, (byte) 0x21,
                (byte) 0xC7, (byte) 0x1D, (byte) 0x9D,
                (byte) 0xC7, (byte) 0x13, (byte) 0x56, (byte) 0x21,
                (byte) 0xC7, (byte) 0x0C, (byte) 0x56, (byte) 0x49,
                (byte) 0xC7, (byte) 0x06, (byte) 0x56, (byte) 0x21,
                (byte) 0xC6, (byte) 0xEB, (byte) 0x56, (byte) 0x49,
                (byte) 0xC6, (byte) 0xDA, (byte) 0x56, (byte) 0x49,
                (byte) 0xC6, (byte) 0xD3, (byte) 0x56, (byte) 0x7F,
                (byte) 0xC6, (byte) 0xA7, (byte) 0x56, (byte) 0x21,
                (byte) 0xC6, (byte) 0xA0, (byte) 0x56, (byte) 0x1A,
                (byte) 0xC6, (byte) 0x97, (byte) 0x56, (byte) 0x5A,
                (byte) 0xC6, (byte) 0x8D, (byte) 0x56, (byte) 0x57,
                (byte) 0xC6, (byte) 0x86, (byte) 0x56, (byte) 0x49,
                (byte) 0xC6, (byte) 0x7F, (byte) 0x56, (byte) 0x21,
                (byte) 0xC6, (byte) 0x73, (byte) 0x56, (byte) 0x5A,
                (byte) 0xC6, (byte) 0x6D, (byte) 0x56, (byte) 0x57,
                (byte) 0xC6, (byte) 0x69, (byte) 0x56, (byte) 0x7F,
                (byte) 0xC6, (byte) 0x66, (byte) 0x9D,
                (byte) 0xC6, (byte) 0x63, (byte) 0x9D,
                (byte) 0xC6, (byte) 0x21, (byte) 0x56, (byte) 0x2D,
                (byte) 0xC5, (byte) 0xF9, (byte) 0x56, (byte) 0x2D,
                (byte) 0xC5, (byte) 0xEE, (byte) 0x56, (byte) 0x49,
                (byte) 0xC5, (byte) 0xDD, (byte) 0x9D,
                (byte) 0xC5, (byte) 0xD8, (byte) 0x56, (byte) 0x2D,
                (byte) 0xC5, (byte) 0xBB, (byte) 0x56, (byte) 0x49,
                (byte) 0xC5, (byte) 0xB5, (byte) 0x56, (byte) 0x21,
                (byte) 0xC5, (byte) 0xA5, (byte) 0x9D,
                (byte) 0xC5, (byte) 0xA1, (byte) 0x9D,
                (byte) 0xC5, (byte) 0x9D, (byte) 0x9D,
                (byte) 0xC5, (byte) 0x98, (byte) 0x9D,
                (byte) 0xC5, (byte) 0x8D, (byte) 0x9D,
                (byte) 0xC5, (byte) 0x89, (byte) 0x9D,
                (byte) 0xC5, (byte) 0x85, (byte) 0x9D,
                (byte) 0xC5, (byte) 0x80, (byte) 0x9D,
                (byte) 0xC5, (byte) 0x79, (byte) 0x9D,
                (byte) 0x85, (byte) 0x60, (byte) 0x56, (byte) 0x02,
                (byte) 0x85, (byte) 0x56, (byte) 0x56, (byte) 0x02,
                (byte) 0xC5, (byte) 0x50, (byte) 0x9D,
                (byte) 0xC5, (byte) 0x4D, (byte) 0x9D,
                (byte) 0xC5, (byte) 0x45, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC5, (byte) 0x31, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC5, (byte) 0x26, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC5, (byte) 0x0B, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC5, (byte) 0x06, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC4, (byte) 0xDA, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC4, (byte) 0xB5, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC4, (byte) 0xB0, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC4, (byte) 0x81, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC4, (byte) 0x6C, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC4, (byte) 0x55, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC4, (byte) 0x2F, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC4, (byte) 0x1A, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0xC4, (byte) 0x03, (byte) 0x56, (byte) 0x80, (byte) 0x9C,
                (byte) 0x35 // checksum
        };

        List<OMFItem> objItems = parser().parseBinary(arrRecord);
        OMFItemFIXUPP itemFIXUPP = (OMFItemFIXUPP) objItems.getFirst();
        List<Fixup> fixups = itemFIXUPP.getFixups();
        assertEquals(62, fixups.size());

        for (int i = 48; i < 62; i++)
        {
            assertEquals(156, fixups.get(i).targetDatum(), "fixup #" + i + " targetDatum");
        }
    }

    @Test
    public void FIXUPPTest8_SingleTwoByteTargetDatum()
    {
        // Single fixup whose target datum is encoded in the 2-byte form (0x80, 0x8E),
        // decoding to index 142 per TIS 1.1 sec 2.2.
        byte[] arrRecord = { b(0x9C), b(6), 0, b(0x85), b(0x77), b(0x56), b(0x80), b(0x8E), b(0xFE) };

        List<OMFItem> objItems = parser().parseBinary(arrRecord);
        OMFItemFIXUPP itemFIXUPP = (OMFItemFIXUPP) objItems.getFirst();
        Fixup fix = itemFIXUPP.getFixups().getFirst();
        assertEquals(142, fix.targetDatum());
        assertNull(fix.frameDatum());
        assertNull(fix.targetDisplacement());
    }

    @Test
    public void getIndex_2ByteForm_DecodesPerTIS()
    {
        // Targeted regression for the TIS 1.1 sec 2.2 variable-width "index" field.
        // tgtDat = 0xC0 0x55 -> 2-byte index = ((0xC0 & 0x7F) << 8) | 0x55 = 0x4055
        byte[] arrRecord = {
                b(0x9C), b(6), 0,
                b(0x80), b(0x00),    // locat
                b(0x56),              // fixDat (P=1 -> no displacement)
                b(0xC0), b(0x55),    // 2-byte targetDatum
                b(0x00)               // checksum (computed below)
        };

        byte sum = 0;
        for (int i = 0; i < arrRecord.length - 1; i++) sum += arrRecord[i];
        arrRecord[arrRecord.length - 1] = (byte) (-sum);

        List<OMFItem> objItems = parser().parseBinary(arrRecord);
        Fixup fix = ((OMFItemFIXUPP) objItems.getFirst()).getFixups().getFirst();
        assertEquals(0x4055, fix.targetDatum());
    }

    @Test
    public void LEDATATest1()
    {
        byte[] arrRecord = { b(0xA0), 8, 0, 1, 0, 0, b(0x8D), b(0x1E), b(0x10), 0, b(0x9C) };
        List<OMFItem> objItems = parser().parseBinary(arrRecord);
        var itemLEDATA = (OMFItemLEDATA) objItems.getFirst();
        assertEquals(arrRecord[3], itemLEDATA.getSegmentIdx());
        assertEquals(0, itemLEDATA.getEnumeratedDataOffset());
        byte[] arrBytes = itemLEDATA.getBytes();
        assertEquals(4, arrBytes.length);
        assertEquals(arrRecord[6], arrBytes[0]);
        assertEquals(arrRecord[9], arrBytes[3]);
    }

    private static byte b(int i) { return (byte) i; }
}
