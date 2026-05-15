package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItemFIXUPP.FixupMethodFrame;
import com.rulecity.parse.OMFItemFIXUPP.FixupMethodTarget;
import com.rulecity.parse.OMFItemFIXUPP.Location;
import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.data.FixupOrThread;
import com.rulecity.parse.data.FixupOrThreadProcessed;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.Thread;
import com.rulecity.parse.data.ThreadProcessed;

public class FixupOrThreadProcessorImpl implements FixupOrThreadProcessor
{
    @Override
    public FixupOrThreadProcessed process(FixupOrThread src)
    {
        if (src.fixup() != null)
        {
            return new FixupOrThreadProcessed(null, processFixup(src.fixup()));
        }
        return new FixupOrThreadProcessed(processThread(src.thread()), null);
    }

    private FixupProcessed processFixup(Fixup fixup)
    {
        Integer threadFieldContainingFrameMethod = null;
        FixupMethodFrame methodFrame = null;
        Integer threadFieldContainingTargetMethod = null;
        FixupMethodTarget methodTarget = null;
        int frame = fixup.frame();
        int targt = fixup.targt();

        Location location = decodeLocation(fixup.location());

        if (fixup.frameSpecifiedByPreviousThreadFieldRef())
        {
            // Per TIS 1.1 sec 4.7 (FixDat F=1): the 3-bit "Frame" field encodes a
            // 2-bit thread number (0-3); the top bit is reserved.
            threadFieldContainingFrameMethod = frame & 3;
        }
        else
        {
            methodFrame = decodeFrameMethod(frame);
        }

        if (fixup.targetSpecifiedByPreviousThreadFieldRef())
        {
            // Per TIS 1.1 sec 4.7 (FixDat T=1): the low 2 bits of the 3-bit "Targt"
            // field are the thread number (0-3); bit 2 is the P bit (already consumed
            // at parse time to decide whether targetDisplacement was read).
            threadFieldContainingTargetMethod = targt & 3;
        }
        else
        {
            methodTarget = decodeTargetMethod(targt);
        }

        Integer idxSegmentFrame = null;
        Integer idxGroupFrame = null;
        Integer idxExternalFrame = null;
        Integer idxSegmentTarget = null;
        Integer idxGroupTarget = null;
        Integer idxExternalTarget = null;

        Integer frameDatum = fixup.frameDatum();
        Integer targetDatum = fixup.targetDatum();

        if (frameDatum != null)
        {
            int idx = frameDatum - 1; // wire format is 1-based
            switch (methodFrame)
            {
                case FRAME_SPECIFIED_BY_SEGDEF -> idxSegmentFrame = idx;
                case FRAME_SPECIFIED_BY_GRPDEF -> idxGroupFrame = idx;
                case FRAME_SPECIFIED_BY_EXTDEF -> idxExternalFrame = idx;
                default -> throw new RuntimeException("Unhandled case");
            }
        }

        if (targetDatum != null)
        {
            int idx = targetDatum - 1;
            switch (methodTarget)
            {
                case TARGET_SPECIFIED_BY_SEGDEF, TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT -> idxSegmentTarget = idx;
                case TARGET_SPECIFIED_BY_GRPDEF, TARGET_SPECIFIED_BY_GRPDEF_WITH_DISPLACEMENT -> idxGroupTarget = idx;
                case TARGET_SPECIFIED_BY_EXTDEF, TARGET_SPECIFIED_BY_EXTDEF_WITH_DISPLACEMENT -> idxExternalTarget = idx;
                default -> throw new RuntimeException("Unhandled case");
            }
        }

        return new FixupProcessed(fixup.segmentRelativeFixups(),
                location,
                fixup.dataRecordOffset(),
                threadFieldContainingFrameMethod,
                methodFrame,
                threadFieldContainingTargetMethod,
                methodTarget,
                idxSegmentFrame,
                idxGroupFrame,
                idxExternalFrame,
                idxSegmentTarget,
                idxGroupTarget,
                idxExternalTarget,
                fixup.targetDisplacement());
    }

    private ThreadProcessed processThread(Thread thread)
    {
        FixupMethodFrame methodFrame = null;
        FixupMethodTarget methodTarget = null;
        int method = thread.method();

        if (thread.threadFieldSpecifiesFrame())
        {
            methodFrame = decodeFrameMethod(method);
        }
        else
        {
            methodTarget = decodeTargetMethod(method);
        }

        int idxThread = thread.index() - 1;
        Integer idxSegment = null;
        Integer idxGroup = null;
        Integer idxExternal = null;

        switch (method)
        {
            case 0 -> idxSegment = idxThread;
            case 1 -> idxGroup = idxThread;
            case 2 -> idxExternal = idxThread;
            default -> throw new RuntimeException("Not supported");
        }

        return new ThreadProcessed(methodTarget, methodFrame, thread.threadNum(),
                idxSegment, idxGroup, idxExternal);
    }

    private Location decodeLocation(byte raw)
    {
        return switch (raw)
        {
            case 0 -> Location.LOW_ORDER_BYTE;
            case 1 -> Location.OFFSET_16BIT;
            case 2 -> Location.SEGMENT;
            case 3 -> Location.POINTER;
            case 4 -> Location.HIGH_ORDER_BYTE;
            case 5 -> Location.LOADER_RESOLVED_OFFSET;
            default -> throw new RuntimeException("Not supported");
        };
    }

    private FixupMethodTarget decodeTargetMethod(int method)
    {
        return switch (method)
        {
            case 0 -> FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT;
            case 1 -> FixupMethodTarget.TARGET_SPECIFIED_BY_GRPDEF_WITH_DISPLACEMENT;
            case 2 -> FixupMethodTarget.TARGET_SPECIFIED_BY_EXTDEF_WITH_DISPLACEMENT;
            case 4 -> FixupMethodTarget.TARGET_SPECIFIED_BY_SEGDEF;
            case 5 -> FixupMethodTarget.TARGET_SPECIFIED_BY_GRPDEF;
            case 6 -> FixupMethodTarget.TARGET_SPECIFIED_BY_EXTDEF;
            default -> throw new RuntimeException("Not supported");
        };
    }

    private FixupMethodFrame decodeFrameMethod(int method)
    {
        return switch (method)
        {
            case 0 -> FixupMethodFrame.FRAME_SPECIFIED_BY_SEGDEF;
            case 1 -> FixupMethodFrame.FRAME_SPECIFIED_BY_GRPDEF;
            case 2 -> FixupMethodFrame.FRAME_SPECIFIED_BY_EXTDEF;
            case 5 -> FixupMethodFrame.FRAME_SPECIFIED_BY_TARGET;
            default -> throw new RuntimeException("Not supported");
        };
    }
}
