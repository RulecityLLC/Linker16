package com.rulecity.parse;

import com.rulecity.parse.data.ExternalNamesDefinition;

import java.util.List;

public class OMFItemEXTDEFImpl implements OMFItem, OMFItemEXTDEF
{
    private final List<ExternalNamesDefinition> lstDefs;
    private final boolean isLEXTDEF;

    public OMFItemEXTDEFImpl(List<ExternalNamesDefinition> lstDefs, boolean isLEXTDEF)
    {
        this.lstDefs = lstDefs;
        this.isLEXTDEF = isLEXTDEF;
    }

    @Override
    public String getTypeString()
    {
        return isLEXTDEF ? "LEXTDEF (B4h)" : "EXTDEF (8Ch)";
    }

    @Override
    public String getDataString()
    {
        return "todo";
    }

    @Override
    public List<ExternalNamesDefinition> getExternalNamesDefinitions()
    {
        return lstDefs;
    }

    @Override
    public boolean isLEXTDEF()
    {
        return this.isLEXTDEF;
    }
}
