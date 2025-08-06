package com.rulecity.parse;

public class OMFItemTHEADR implements OMFItem
{
    private final String text;

    public OMFItemTHEADR(String string)
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
