package com.rulecity.parse;

import com.rulecity.parse.data.GroupDef;

import java.util.List;
import java.util.stream.Collectors;

public class OMFItemGRPDEFImpl implements OMFItem, OMFItemGRPDEF
{
    private final byte grpNameIdx;
    private final List<Byte> lstSegDefs;

    public OMFItemGRPDEFImpl(byte grpNameIdx, List<Byte> lstSegDefs)
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

    @Override
    public GroupDef getGroupDef()
    {
        // -1 to adjust indices to be zero-based instead of one-based
        return new GroupDef(grpNameIdx-1, lstSegDefs.stream()
                .map(n -> n - 1)
                .toList());
    }
}
