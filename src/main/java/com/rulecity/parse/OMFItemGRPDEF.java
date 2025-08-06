package com.rulecity.parse;

import java.util.List;

public class OMFItemGRPDEF implements OMFItem
{
    private final byte grpNameIdx;
    private final List<Byte> lstSegDefs;

    public OMFItemGRPDEF(byte grpNameIdx, List<Byte> lstSegDefs)
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
