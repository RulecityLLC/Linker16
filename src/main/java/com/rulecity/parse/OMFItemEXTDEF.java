package com.rulecity.parse;

import com.rulecity.parse.data.ExternalNamesDefinition;

import java.util.List;

public class OMFItemEXTDEF implements OMFItem
{
    private final List<ExternalNamesDefinition> lstDefs;

    public OMFItemEXTDEF(List<ExternalNamesDefinition> lstDefs)
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
}
