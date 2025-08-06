package com.rulecity.parse;

import com.rulecity.parse.data.ExternalNamesDefinition;

import java.util.List;

public class OMFItemEXTDEFImpl implements OMFItem, OMFItemEXTDEF
{
    private final List<ExternalNamesDefinition> lstDefs;

    public OMFItemEXTDEFImpl(List<ExternalNamesDefinition> lstDefs)
    {
        this.lstDefs = lstDefs;
    }

    @Override
    public String getTypeString()
    {
        return "EXTDEF (8Ch)";
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
}
