package com.rulecity.parse;

import com.rulecity.parse.data.Fixup;

import java.util.List;

public class ObjItemFIXUPPImpl implements ObjItem, ObjItemFIXUPP
{
    private final List<Fixup> lstFixups;

    public ObjItemFIXUPPImpl(List<Fixup> lstFixups)
    {
        this.lstFixups = lstFixups;
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
}
