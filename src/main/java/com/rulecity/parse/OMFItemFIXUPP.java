package com.rulecity.parse;

import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.data.FixupOrThread;
import com.rulecity.parse.data.FixupOrThreadProcessed;
import com.rulecity.parse.data.Thread;

import java.util.List;

public interface OMFItemFIXUPP
{
    enum FixupMethodTarget
    {
        TARGET_SPECIFIED_BY_SEGDEF_WITH_DISPLACEMENT,
        TARGET_SPECIFIED_BY_GRPDEF_WITH_DISPLACEMENT,
        TARGET_SPECIFIED_BY_EXTDEF_WITH_DISPLACEMENT,
        TARGET_SPECIFIED_BY_SEGDEF,
        TARGET_SPECIFIED_BY_GRPDEF,
        TARGET_SPECIFIED_BY_EXTDEF
    }

    enum FixupMethodFrame
    {
        FRAME_SPECIFIED_BY_SEGDEF,
        FRAME_SPECIFIED_BY_GRPDEF,
        FRAME_SPECIFIED_BY_EXTDEF,
        FRAME_SPECIFIED_BY_TARGET
    }

    enum Location
    {
        LOW_ORDER_BYTE,
        OFFSET,
        SEGMENT,
        POINTER,
        HIGH_ORDER_BYTE,
        LOADER_RESOLVED_OFFSET
    }

    List<Fixup> getFixups();

    List<Thread> getThreads();

    List<FixupOrThread> getFixupsOrThreads();

    List<FixupOrThreadProcessed> getFixupsOrThreadsProcessed();
}
