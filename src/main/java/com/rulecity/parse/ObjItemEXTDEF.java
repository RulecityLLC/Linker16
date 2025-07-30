package com.rulecity.parse;

import com.rulecity.parse.data.ExternalNamesDefinition;

import java.util.List;

public class ObjItemEXTDEF implements ObjItem
{
    private final List<ExternalNamesDefinition> lstDefs;

    public ObjItemEXTDEF(List<ExternalNamesDefinition> lstDefs)
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
