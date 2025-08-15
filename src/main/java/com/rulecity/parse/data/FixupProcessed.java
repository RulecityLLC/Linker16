package com.rulecity.parse.data;

import com.rulecity.parse.OMFItemFIXUPP;

public record FixupProcessed(boolean segmentRelativeFixups, OMFItemFIXUPP.Location location, int dataRecordOffset,
                             Integer threadFieldContainingFrameMethod,
                             OMFItemFIXUPP.FixupMethodFrame methodFrame,
                             Integer threadFieldContainingTargetMethod,
                             OMFItemFIXUPP.FixupMethodTarget methodTarget,
                             Byte frameDatum,
                             Byte targetDatum,
                             Integer targetDisplacement)
{
}
