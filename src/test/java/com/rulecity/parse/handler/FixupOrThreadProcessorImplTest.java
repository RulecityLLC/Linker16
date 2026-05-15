package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItemFIXUPP;
import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.data.FixupOrThread;
import com.rulecity.parse.data.FixupOrThreadProcessed;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.Thread;
import com.rulecity.parse.data.ThreadProcessed;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FixupOrThreadProcessorImplTest
{
    private final FixupOrThreadProcessorImpl proc = new FixupOrThreadProcessorImpl();

    private Fixup fixup(byte location, boolean fByThread, int frame,
                        boolean tByThread, int targt, Integer frameDatum, Integer targetDatum,
                        Integer targetDispl)
    {
        return new Fixup(false, location, 0x100, fByThread, frame, tByThread, targt,
                frameDatum, targetDatum, targetDispl);
    }

    @Test
    public void processFixup_locationDecoding_allValues()
    {
        OMFItemFIXUPP.Location[] expected = {
                OMFItemFIXUPP.Location.LOW_ORDER_BYTE,
                OMFItemFIXUPP.Location.OFFSET_16BIT,
                OMFItemFIXUPP.Location.SEGMENT,
                OMFItemFIXUPP.Location.POINTER,
                OMFItemFIXUPP.Location.HIGH_ORDER_BYTE,
                OMFItemFIXUPP.Location.LOADER_RESOLVED_OFFSET};
        for (byte loc = 0; loc <= 5; loc++)
        {
            Fixup f = fixup(loc, false, 0, false, 4, 1, 1, null);
            FixupOrThreadProcessed out = proc.process(new FixupOrThread(f, null));
            assertEquals(expected[loc], out.fixup().location());
        }
    }

    @Test
    public void processFixup_invalidLocationThrows()
    {
        Fixup f = fixup((byte) 6, false, 0, false, 4, 1, 1, null);
        assertThrows(RuntimeException.class, () -> proc.process(new FixupOrThread(f, null)));
    }

    @Test
    public void processFixup_frameAndTargetMethodDecoding_perDatumKind()
    {
        // method 0 (segdef + displ) — Target with displacement
        FixupProcessed fp = proc.process(new FixupOrThread(
                fixup((byte) 0, false, 0, false, 0, 5, 7, 0xAB), null)).fixup();
        assertEquals(OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF, fp.methodFrame());
        assertEquals(OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT, fp.methodTarget());
        assertEquals(4, fp.idxSegmentFrame()); // 5-1
        assertEquals(6, fp.idxSegmentTarget()); // 7-1
        assertNull(fp.idxGroupFrame());
        assertNull(fp.idxExternalFrame());
        assertEquals(0xAB, fp.targetDisplacement());

        // method 1 (grpdef)
        fp = proc.process(new FixupOrThread(
                fixup((byte) 0, false, 1, false, 1, 3, 4, null), null)).fixup();
        assertEquals(OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_GRPDEF, fp.methodFrame());
        assertEquals(OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_GRPDEF_WITH_DISPLACEMENT, fp.methodTarget());
        assertEquals(2, fp.idxGroupFrame());
        assertEquals(3, fp.idxGroupTarget());
        assertNull(fp.idxSegmentFrame());

        // method 2 (extdef)
        fp = proc.process(new FixupOrThread(
                fixup((byte) 0, false, 2, false, 2, 8, 9, null), null)).fixup();
        assertEquals(OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_EXTDEF, fp.methodFrame());
        assertEquals(OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_EXTDEF_WITH_DISPLACEMENT, fp.methodTarget());
        assertEquals(7, fp.idxExternalFrame());
        assertEquals(8, fp.idxExternalTarget());
    }

    @Test
    public void processFixup_targetMethodWithoutDisplacementBranches()
    {
        // method 4 (segdef no displ)
        FixupProcessed fp = proc.process(new FixupOrThread(
                fixup((byte) 0, false, 0, false, 4, 1, 5, null), null)).fixup();
        assertEquals(OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF, fp.methodTarget());
        assertEquals(4, fp.idxSegmentTarget());

        // method 5 (grpdef no displ)
        fp = proc.process(new FixupOrThread(
                fixup((byte) 0, false, 0, false, 5, 1, 5, null), null)).fixup();
        assertEquals(OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_GRPDEF, fp.methodTarget());
        assertEquals(4, fp.idxGroupTarget());

        // method 6 (extdef no displ)
        fp = proc.process(new FixupOrThread(
                fixup((byte) 0, false, 0, false, 6, 1, 5, null), null)).fixup();
        assertEquals(OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_EXTDEF, fp.methodTarget());
        assertEquals(4, fp.idxExternalTarget());
    }

    @Test
    public void processFixup_unsupportedFrameMethodThrows()
    {
        // method 3 isn't mapped; method 5 maps to FRAME_SPECIFIED_BY_TARGET (no datum). 4 throws.
        Fixup f = fixup((byte) 0, false, 4, false, 0, 1, 1, null);
        assertThrows(RuntimeException.class, () -> proc.process(new FixupOrThread(f, null)));
    }

    @Test
    public void processFixup_unsupportedTargetMethodThrows()
    {
        Fixup f = fixup((byte) 0, false, 0, false, 7, 1, 1, null);
        assertThrows(RuntimeException.class, () -> proc.process(new FixupOrThread(f, null)));
    }

    @Test
    public void processFixup_threadFieldRef_storesThreadNumberLowBits()
    {
        // Frame thread ref: top bit reserved -> only low 2 bits matter
        Fixup f = fixup((byte) 0, true, 0b111, true, 0b110, null, null, null);
        FixupProcessed fp = proc.process(new FixupOrThread(f, null)).fixup();
        assertEquals(3, fp.threadFieldContainingFrameMethod()); // 0b111 & 3
        assertEquals(2, fp.threadFieldContainingTargetMethod()); // 0b110 & 3
        assertNull(fp.methodFrame());
        assertNull(fp.methodTarget());
    }

    @Test
    public void processFixup_frameMethodSpecifiedByTarget_keepsTargetUntouched()
    {
        // method 5 -> FRAME_SPECIFIED_BY_TARGET; frameDatum is null so no idx switch is hit.
        Fixup f = fixup((byte) 0, false, 5, false, 0, null, 1, null);
        FixupProcessed fp = proc.process(new FixupOrThread(f, null)).fixup();
        assertEquals(OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_TARGET, fp.methodFrame());
        assertNull(fp.idxSegmentFrame());
        assertNull(fp.idxGroupFrame());
        assertNull(fp.idxExternalFrame());
    }

    @Test
    public void processFixup_dataRecordOffsetAndSegmentRelativeFlagPropagated()
    {
        Fixup f = new Fixup(true, (byte) 1, 0x42, false, 0, false, 0, 1, 1, null);
        FixupProcessed fp = proc.process(new FixupOrThread(f, null)).fixup();
        assertEquals(true, fp.segmentRelativeFixups());
        assertEquals(0x42, fp.dataRecordOffset());
    }

    @Test
    public void processThread_method0_segdef()
    {
        Thread t = new Thread(true, 0, 1, 5);
        ThreadProcessed tp = proc.process(new FixupOrThread(null, t)).thread();
        assertEquals(OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF, tp.methodFrame());
        assertNull(tp.methodTarget());
        assertEquals(4, tp.idxSegment()); // 5-1
        assertNull(tp.idxGroup());
        assertNull(tp.idxExternal());
        assertEquals(1, tp.threadNum());
    }

    @Test
    public void processThread_method1_grpdef()
    {
        Thread t = new Thread(true, 1, 0, 7);
        ThreadProcessed tp = proc.process(new FixupOrThread(null, t)).thread();
        assertEquals(OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_GRPDEF, tp.methodFrame());
        assertEquals(6, tp.idxGroup());
        assertNull(tp.idxSegment());
    }

    @Test
    public void processThread_method2_extdef()
    {
        Thread t = new Thread(false, 2, 3, 4);
        ThreadProcessed tp = proc.process(new FixupOrThread(null, t)).thread();
        assertNull(tp.methodFrame());
        assertEquals(OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_EXTDEF_WITH_DISPLACEMENT, tp.methodTarget());
        assertEquals(3, tp.idxExternal());
    }

    @Test
    public void processThread_unsupportedMethodThrows()
    {
        // method 3 is NOT supported in processThread's switch
        Thread t = new Thread(true, 3, 0, 1);
        assertThrows(RuntimeException.class, () -> proc.process(new FixupOrThread(null, t)));
    }

    @Test
    public void process_returnsFixupOrThreadProcessedShape()
    {
        // When fixup is non-null, thread slot is null and vice versa.
        Fixup f = fixup((byte) 0, false, 0, false, 0, 1, 1, 0);
        FixupOrThreadProcessed result = proc.process(new FixupOrThread(f, null));
        assertNotNull(result.fixup());
        assertNull(result.thread());

        Thread t = new Thread(true, 0, 0, 1);
        FixupOrThreadProcessed result2 = proc.process(new FixupOrThread(null, t));
        assertNull(result2.fixup());
        assertNotNull(result2.thread());
    }
}
