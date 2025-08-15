package com.rulecity.parse;

public interface OMFItemMODEND
{
    boolean isAMainProgramModule();

    boolean moduleContainsAStartAddress();

    // not implementing anything else because I'm assuming none of the files I am dealing with will contain a start address
}
