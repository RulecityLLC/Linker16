package com.rulecity.parse;

public interface ObjItemCOMENT
{
    /*
        private final byte commentType;
    private final byte commentClass;
    private final byte[] arrBytes;
     */

    byte getCommentType();

    byte getCommentClass();

    byte[] getBytes();
}
