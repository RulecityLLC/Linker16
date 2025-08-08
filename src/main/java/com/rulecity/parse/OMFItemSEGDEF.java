package com.rulecity.parse;

public interface OMFItemSEGDEF
{
    public enum Relocatable
    {
        ABSOLUTE,
        BYTE_ALIGNED,
        WORD_ALIGNED,
        PARAGRAPH_ALIGNED,
        PAGE_ALIGNED,
        DOUBLE_WORD_ALIGNED
    }

    public enum Scope
    {
        PRIVATE,
        PUBLIC,
        STACK,
        COMMON
    }
}
