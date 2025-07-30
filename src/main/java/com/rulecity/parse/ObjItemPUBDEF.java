package com.rulecity.parse;

import com.rulecity.parse.data.PublicNamesDefinition;

import java.util.List;

public class ObjItemPUBDEF implements ObjItem
{
    private final byte baseGroupIdx;
    private final byte baseSegmentIdx;
    private final Integer baseFrame;
    private final List<PublicNamesDefinition> lstDefs;

    public ObjItemPUBDEF(byte baseGroupIdx, byte baseSegmentIdx, Integer baseFrame, List<PublicNamesDefinition> lstDefs)
    {
        this.baseGroupIdx = baseGroupIdx;
        this.baseSegmentIdx = baseSegmentIdx;
        this.baseFrame = baseFrame;
        this.lstDefs = lstDefs;
    }

    @Override
    public String getTypeString()
    {
        return "PUBDEF (90h)";
    }

    @Override
    public String getDataString()
    {
        var bldr = new StringBuilder();
        for (PublicNamesDefinition def : lstDefs)
        {
            bldr.append(def.publicNameString);
            bldr.append(" ");
        }
        return bldr.toString();
    }
}
