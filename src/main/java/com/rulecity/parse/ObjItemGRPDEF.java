package com.rulecity.parse;

import java.util.List;

public class ObjItemGRPDEF implements ObjItem
{
    private final byte grpNameIdx;
    private final List<Byte> lstSegDefs;

    public ObjItemGRPDEF(byte grpNameIdx, List<Byte> lstSegDefs)
    {
        this.grpNameIdx = grpNameIdx;
        this.lstSegDefs = lstSegDefs;
    }

    @Override
    public String getTypeString()
    {
        return "GRPDEF (9Ah)";
    }

    @Override
    public String getDataString()
    {
        return "todo";
    }
}
