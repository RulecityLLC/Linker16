package com.rulecity.parse;

import com.rulecity.parse.data.PublicNameAndOffset;
import com.rulecity.parse.data.PublicNamesDefinitionProcessed;
import com.rulecity.parse.data.PublicNamesDefinition;

import java.util.List;

public class OMFItemPUBDEFImpl implements OMFItem, OMFItemPUBDEF
{
    private final byte baseGroupIdx;
    private final byte baseSegmentIdx;
    private final Integer baseFrame;
    private final List<PublicNamesDefinition> lstDefs;
    private final boolean isLPUBDEF;

    public OMFItemPUBDEFImpl(byte baseGroupIdx, byte baseSegmentIdx, Integer baseFrame, List<PublicNamesDefinition> lstDefs, boolean isLPUBDEF)
    {
        this.baseGroupIdx = baseGroupIdx;
        this.baseSegmentIdx = baseSegmentIdx;
        this.baseFrame = baseFrame;
        this.lstDefs = lstDefs;
        this.isLPUBDEF = isLPUBDEF;
    }

    @Override
    public String getTypeString()
    {
        return isLPUBDEF ? "LPUBDEF (B6h)" : "PUBDEF (90h)";
    }

    @Override
    public String getDataString()
    {
        var bldr = new StringBuilder();
        for (PublicNamesDefinition def : lstDefs)
        {
            bldr.append(def.publicNameString());
            bldr.append(" ");
        }
        return bldr.toString();
    }

    @Override
    public PublicNamesDefinitionProcessed getDef()
    {
        List<PublicNameAndOffset> lstNamesAndOffsets = lstDefs.stream()
                .map(i -> new PublicNameAndOffset(i.publicNameString(), i.publicOffset()))
                .toList();
        return new PublicNamesDefinitionProcessed(
                baseGroupIdx != 0 ? baseGroupIdx - 1 : null,
                baseSegmentIdx != 0 ? baseSegmentIdx - 1 : null,
                baseFrame,
                lstNamesAndOffsets,
                isLPUBDEF);
    }
}
