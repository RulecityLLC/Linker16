package com.rulecity.parse;

public class OMFItemCOMENTImpl implements OMFItem, OMFItemCOMENT
{
    private final byte commentType;
    private final byte commentClass;
    private final byte[] arrBytes;

    public OMFItemCOMENTImpl(byte commentType, byte commentClass, byte[] arrBytes)
    {
        this.commentType = commentType;
        this.commentClass = commentClass;
        this.arrBytes = arrBytes;
    }

    @Override
    public String getTypeString()
    {
        return "COMENT (88h)";
    }

    @Override
    public String getDataString()
    {
        return "todo";
    }

    @Override
    public byte getCommentType()
    {
        return commentType;
    }

    @Override
    public byte getCommentClass()
    {
        return commentClass;
    }

    @Override
    public byte[] getBytes()
    {
        return arrBytes;
    }
}
