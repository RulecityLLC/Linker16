package com.rulecity.parse.io;

public class ByteCursorFactoryImpl implements ByteCursorFactory
{
    @Override
    public ByteCursor create(byte[] src)
    {
        return new ByteCursorImpl(src);
    }
}
