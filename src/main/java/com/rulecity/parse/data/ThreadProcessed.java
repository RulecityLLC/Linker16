package com.rulecity.parse.data;

import com.rulecity.parse.OMFItemFIXUPP;

public record ThreadProcessed(OMFItemFIXUPP.FixupMethodTarget methodTarget, OMFItemFIXUPP.FixupMethodFrame methodFrame, int threadNum,
                              Integer idxSegment,
                              Integer idxGroup,
                              Integer idxExternal)
{
}
