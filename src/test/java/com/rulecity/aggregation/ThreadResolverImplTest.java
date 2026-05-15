package com.rulecity.aggregation;

import com.rulecity.parse.OMFItemFIXUPP;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.ThreadProcessed;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ThreadResolverImplTest
{
    private final ThreadResolverImpl resolver = new ThreadResolverImpl();

    private FixupProcessed fp(Integer frameThread, Integer targetThread,
                              OMFItemFIXUPP.FixupMethodFrame mf,
                              OMFItemFIXUPP.FixupMethodTarget mt)
    {
        return new FixupProcessed(true, OMFItemFIXUPP.Location.OFFSET_16BIT, 0x1234,
                frameThread, mf,
                targetThread, mt,
                10, null, null,
                20, null, null,
                42);
    }

    @Test
    public void noThreadRefs_passesAllFieldsThroughExceptZeroesThreadFields()
    {
        FixupProcessed src = fp(null, null,
                OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_GRPDEF,
                OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_EXTDEF);
        FixupProcessed out = resolver.resolve(src,
                new ThreadProcessed[4], new ThreadProcessed[4]);

        assertSame(src.location(), out.location());
        assertEquals(0x1234, out.dataRecordOffset());
        assertEquals(true, out.segmentRelativeFixups());
        assertEquals(42, out.targetDisplacement());
        assertEquals(OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_GRPDEF, out.methodFrame());
        assertEquals(OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_EXTDEF, out.methodTarget());
        assertNull(out.threadFieldContainingFrameMethod());
        assertNull(out.threadFieldContainingTargetMethod());
        assertEquals(10, out.idxSegmentFrame());
        assertEquals(20, out.idxSegmentTarget());
    }

    @Test
    public void frameThreadRef_overridesFrameMethodAndIndices()
    {
        ThreadProcessed[] frame = new ThreadProcessed[4];
        frame[2] = new ThreadProcessed(null, OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_EXTDEF,
                2, null, null, 99);

        FixupProcessed src = fp(2, null,
                OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF,
                OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF);
        FixupProcessed out = resolver.resolve(src, frame, new ThreadProcessed[4]);

        assertEquals(OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_EXTDEF, out.methodFrame());
        assertNull(out.idxSegmentFrame());
        assertNull(out.idxGroupFrame());
        assertEquals(99, out.idxExternalFrame());
        // target untouched
        assertEquals(20, out.idxSegmentTarget());
    }

    @Test
    public void targetThreadRef_overridesTargetMethodAndIndices()
    {
        ThreadProcessed[] target = new ThreadProcessed[4];
        target[3] = new ThreadProcessed(OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_GRPDEF,
                null, 3, null, 77, null);

        FixupProcessed src = fp(null, 3,
                OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF,
                OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF);
        FixupProcessed out = resolver.resolve(src, new ThreadProcessed[4], target);

        assertEquals(OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_GRPDEF, out.methodTarget());
        assertNull(out.idxSegmentTarget());
        assertEquals(77, out.idxGroupTarget());
        assertNull(out.idxExternalTarget());
        // frame untouched
        assertEquals(10, out.idxSegmentFrame());
    }

    @Test
    public void undefinedFrameThread_throws()
    {
        FixupProcessed src = fp(1, null,
                OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF,
                OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF);
        assertThrows(RuntimeException.class,
                () -> resolver.resolve(src, new ThreadProcessed[4], new ThreadProcessed[4]));
    }

    @Test
    public void undefinedTargetThread_throws()
    {
        FixupProcessed src = fp(null, 0,
                OMFItemFIXUPP.FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF,
                OMFItemFIXUPP.FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF);
        assertThrows(RuntimeException.class,
                () -> resolver.resolve(src, new ThreadProcessed[4], new ThreadProcessed[4]));
    }
}
