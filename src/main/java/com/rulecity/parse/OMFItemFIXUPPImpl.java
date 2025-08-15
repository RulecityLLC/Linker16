package com.rulecity.parse;

import com.rulecity.parse.data.*;
import com.rulecity.parse.data.Thread;

import java.util.List;
import java.util.Objects;

public class OMFItemFIXUPPImpl implements OMFItem, OMFItemFIXUPP
{
    private final List<FixupOrThread> lstFixupsOrThreads;

    public OMFItemFIXUPPImpl(List<FixupOrThread> lstFixupsOrThreads)
    {
        this.lstFixupsOrThreads = lstFixupsOrThreads;
    }

    @Override
    public String getTypeString()
    {
        return "FIXUPP (9Ch)";
    }

    @Override
    public String getDataString()
    {
        return "todo";
    }

    @Override
    public List<Fixup> getFixups()
    {
        return lstFixupsOrThreads.stream()
                .map(FixupOrThread::fixup)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<Thread> getThreads()
    {
        return lstFixupsOrThreads.stream()
                .map(FixupOrThread::thread)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<FixupOrThread> getFixupsOrThreads()
    {
        return lstFixupsOrThreads;
    }

    @Override
    public List<FixupOrThreadProcessed> getFixupsOrThreadsProcessed()
    {
        return lstFixupsOrThreads.stream()
                .map(this::convert)
                .toList();
    }

    private FixupOrThreadProcessed convert(FixupOrThread src)
    {
        FixupOrThreadProcessed result;

        // if this is a fixup record
        if (src.fixup() != null)
        {
            Fixup fixup = src.fixup();
            Integer threadFieldContainingFrameMethod = null;
            OMFItemFIXUPP.FixupMethodFrame methodFrame = null;
            Integer threadFieldContainingTargetMethod = null;
            OMFItemFIXUPP.FixupMethodTarget methodTarget = null;
            int frame = fixup.frame();
            int targt = fixup.targt();

            Location location = switch (fixup.location())
            {
                case 0 -> Location.LOW_ORDER_BYTE;
                case 1 -> Location.OFFSET;
                case 2 -> Location.SEGMENT;
                case 3 -> Location.POINTER;
                case 4 -> Location.HIGH_ORDER_BYTE;
                case 5 -> Location.LOADER_RESOLVED_OFFSET;
                default -> throw new RuntimeException("Not supported");
            };

            if (fixup.frameSpecifiedByPreviousThreadFieldRef())
            {
                threadFieldContainingFrameMethod = frame;
            }
            else
            {
                methodFrame = getFixupMethodFrame(frame);
            }

            if (fixup.targetSpecifiedByPreviousThreadFieldRef())
            {
                threadFieldContainingTargetMethod = targt;
            }
            else
            {
                methodTarget = getFixupMethodTarget(targt);
            }

            var fixupProcessed = new FixupProcessed(fixup.segmentRelativeFixups(),
                    location,
                    fixup.dataRecordOffset(),
                    threadFieldContainingFrameMethod,
                    methodFrame,
                    threadFieldContainingTargetMethod,
                    methodTarget,
                    fixup.frameDatum(),
                    fixup.targetDatum(),
                    fixup.targetDisplacement());

            result = new FixupOrThreadProcessed(null, fixupProcessed);
        }
        // else it's a thread
        else
        {
            Thread thread = src.thread();
            FixupMethodFrame methodFrame = null;
            FixupMethodTarget methodTarget = null;
            int method = thread.method();

            if (thread.threadFieldSpecifiesFrame())
            {
                methodFrame = getFixupMethodFrame(method);
            }
            else
            {
                methodTarget = getFixupMethodTarget(method);
            }

            int idxThread = thread.index() - 1; // -1 so that index 0 is the first entry
            Integer idxSegment = null;
            Integer idxGroup = null;
            Integer idxExternal = null;

            switch (method)
            {
            case 0:
                idxSegment = idxThread;
                break;
            case 1:
                idxGroup = idxThread;
                break;
            case 2:
                idxExternal = idxThread;
                break;
            default:
                throw new RuntimeException("Not supported");
            }

            result = new FixupOrThreadProcessed(new ThreadProcessed(methodTarget, methodFrame, thread.threadNum(),
                    idxSegment, idxGroup, idxExternal), null);
        }

        return result;
    }

    private FixupMethodTarget getFixupMethodTarget(int method)
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

    private FixupMethodFrame getFixupMethodFrame(int method)
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
