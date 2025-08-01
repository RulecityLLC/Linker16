package com.rulecity.parse;

import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.data.Thread;

import java.util.List;

public class ObjItemFIXUPPImpl implements ObjItem, ObjItemFIXUPP
{
    private final List<Fixup> lstFixups;
    private final List<Thread> lstThreads;

    public ObjItemFIXUPPImpl(List<Fixup> lstFixups, List<Thread> lstThreads)
    {
        this.lstFixups = lstFixups;
        this.lstThreads = lstThreads;
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
        return lstFixups;
    }

    @Override
    public List<Thread> getThreads()
    {
        return lstThreads;
    }
}
