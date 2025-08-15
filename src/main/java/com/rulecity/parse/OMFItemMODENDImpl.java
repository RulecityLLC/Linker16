package com.rulecity.parse;

public class OMFItemMODENDImpl implements OMFItem, OMFItemMODEND
{
    private final boolean isAMainProgramModule;
    private final boolean moduleContainsAStartAddress;
    private final Byte endData;
    private final Byte frameDatum;
    private final Byte targetDatum;
    private final Integer targetDisplacement;

    public OMFItemMODENDImpl(boolean isAMainProgramModule, boolean moduleContainsAStartAddress, Byte endData, Byte frameDatum, Byte targetDatum, Integer targetDisplacement)
    {
        this.isAMainProgramModule = isAMainProgramModule;
        this.moduleContainsAStartAddress = moduleContainsAStartAddress;
        this.endData = endData;
        this.frameDatum = frameDatum;
        this.targetDatum = targetDatum;
        this.targetDisplacement = targetDisplacement;
    }

    @Override
    public String getTypeString()
    {
        return "MODEND (8Ah)";
    }

    @Override
    public String getDataString()
    {
        return "todo";
    }

    @Override
    public boolean isAMainProgramModule()
    {
        return isAMainProgramModule;
    }

    @Override
    public boolean moduleContainsAStartAddress()
    {
        return moduleContainsAStartAddress;
    }
}
