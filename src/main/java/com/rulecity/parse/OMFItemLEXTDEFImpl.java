package com.rulecity.parse;

import com.rulecity.parse.data.ExternalNamesDefinition;

import java.util.List;

public class OMFItemLEXTDEFImpl implements OMFItem, OMFItemLEXTDEF
{
    private final List<ExternalNamesDefinition> lstDefs;

    public OMFItemLEXTDEFImpl(List<ExternalNamesDefinition> lstDefs)
    {
        this.lstDefs = lstDefs;
    }

    @Override
    public String getTypeString()
    {
        return "LEXTDEF (B4h)";
    }

    @Override
    public String getDataString()
    {
        return "todo";
    }

    @Override
    public List<ExternalNamesDefinition> getLocalExternalNamesDefinitions()
    {
        return lstDefs;
    }
}
