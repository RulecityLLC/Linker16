package com.rulecity.parse;

import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.data.FixupOrThread;
import com.rulecity.parse.data.FixupOrThreadProcessed;
import com.rulecity.parse.data.Thread;

import java.util.List;
import java.util.Objects;

public class OMFItemFIXUPPImpl implements OMFItem, OMFItemFIXUPP
{
    private final List<FixupOrThread> lstFixupsOrThreads;
    private final List<FixupOrThreadProcessed> lstProcessed;

    public OMFItemFIXUPPImpl(List<FixupOrThread> lstFixupsOrThreads,
                             List<FixupOrThreadProcessed> lstProcessed)
    {
        this.lstFixupsOrThreads = lstFixupsOrThreads;
        this.lstProcessed = lstProcessed;
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
        return lstProcessed;
    }
}
