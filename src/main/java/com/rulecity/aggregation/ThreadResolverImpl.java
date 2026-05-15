package com.rulecity.aggregation;

import com.rulecity.parse.OMFItemFIXUPP;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.ThreadProcessed;

public class ThreadResolverImpl implements ThreadResolver
{
    @Override
    public FixupProcessed resolve(FixupProcessed fp,
                                  ThreadProcessed[] frameThreads,
                                  ThreadProcessed[] targetThreads)
    {
        OMFItemFIXUPP.FixupMethodFrame methodFrame = fp.methodFrame();
        Integer idxSegmentFrame = fp.idxSegmentFrame();
        Integer idxGroupFrame = fp.idxGroupFrame();
        Integer idxExternalFrame = fp.idxExternalFrame();

        if (fp.threadFieldContainingFrameMethod() != null)
        {
            ThreadProcessed t = frameThreads[fp.threadFieldContainingFrameMethod()];
            if (t == null) throw new RuntimeException("Frame thread referenced before defined");
            methodFrame = t.methodFrame();
            idxSegmentFrame = t.idxSegment();
            idxGroupFrame = t.idxGroup();
            idxExternalFrame = t.idxExternal();
        }

        OMFItemFIXUPP.FixupMethodTarget methodTarget = fp.methodTarget();
        Integer idxSegmentTarget = fp.idxSegmentTarget();
        Integer idxGroupTarget = fp.idxGroupTarget();
        Integer idxExternalTarget = fp.idxExternalTarget();

        if (fp.threadFieldContainingTargetMethod() != null)
        {
            ThreadProcessed t = targetThreads[fp.threadFieldContainingTargetMethod()];
            if (t == null) throw new RuntimeException("Target thread referenced before defined");
            methodTarget = t.methodTarget();
            idxSegmentTarget = t.idxSegment();
            idxGroupTarget = t.idxGroup();
            idxExternalTarget = t.idxExternal();
        }

        return new FixupProcessed(fp.segmentRelativeFixups(),
                fp.location(),
                fp.dataRecordOffset(),
                null,
                methodFrame,
                null,
                methodTarget,
                idxSegmentFrame,
                idxGroupFrame,
                idxExternalFrame,
                idxSegmentTarget,
                idxGroupTarget,
                idxExternalTarget,
                fp.targetDisplacement());
    }
}
