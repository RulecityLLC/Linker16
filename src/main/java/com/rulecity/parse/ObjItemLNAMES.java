package com.rulecity.parse;

import java.util.List;

public class ObjItemLNAMES implements ObjItem
{
    private final List<String> names;

    public ObjItemLNAMES(List<String> names)
    {
        this.names = names;
    }

    @Override
    public String getTypeString()
    {
        return "LNAMES (96h)";
    }

    @Override
    public String getDataString()
    {
        StringBuilder result = new StringBuilder();

        for (String name : names)
        {
            result.append(name);
            result.append(" ");
        }
        return result.toString();
    }
}
