package com.rulecity.parse;

import com.rulecity.parse.data.Communal;

import java.util.List;

public class ObjItemCOMDEFImpl implements ObjItem, ObjItemCOMDEF
{
    private final List<Communal> lstCommunal;

    public ObjItemCOMDEFImpl(List<Communal> lstCommunal)
    {
        this.lstCommunal = lstCommunal;
    }

    @Override
    public String getTypeString()
    {
        return "COMDEF (B0h)";
    }

    @Override
    public String getDataString()
    {
        StringBuilder bdr = new StringBuilder();
        for (Communal entry : lstCommunal)
        {
            bdr.append(entry.name());
            bdr.append(": ");
            bdr.append(entry.length());
            bdr.append('\n');
        }
        return bdr.toString();
    }

    @Override
    public List<Communal> getCommualList()
    {
        return this.lstCommunal;
    }
}
