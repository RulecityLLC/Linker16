package com.rulecity.parse;

public class ObjItemTHEADR implements ObjItem
{
    private final String text;

    public ObjItemTHEADR(String string)
    {
        this.text = string;
    }

    @Override
    public String getTypeString()
    {
        return "THEADR (80h)";
    }

    @Override
    public String getDataString()
    {
        return text;
    }
}
