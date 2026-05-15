package com.rulecity.parse.io;

public class ByteCursorImpl implements ByteCursor
{
    private final byte[] src;
    private int idxSrc;
    private byte checkSum;
    private int recordLength;
    private int recordCount;

    public ByteCursorImpl(byte[] src)
    {
        this.src = src;
        this.idxSrc = 0;
    }

    @Override public boolean hasMore() { return idxSrc < src.length; }

    @Override
    public void beginRecord()
    {
        this.checkSum = 0;
        this.recordCount = 0;
    }

    @Override
    public void markStartOfPayload(int recordLength)
    {
        this.recordLength = recordLength;
        this.recordCount = 0;
    }

    @Override public int getRecordLength() { return recordLength; }
    @Override public int getRecordCount() { return recordCount; }
    @Override public byte getChecksum() { return checkSum; }

    @Override
    public byte readRawByte()
    {
        return src[idxSrc++];
    }

    @Override
    public byte getSignedByte()
    {
        byte tmp = src[idxSrc++];
        checkSum -= tmp;
        recordCount++;
        return tmp;
    }

    @Override
    public int getUnsignedByteAsInt()
    {
        return getSignedByte() & 0xFF;
    }

    @Override
    public int getWord()
    {
        return getUnsignedByteAsInt() | (getUnsignedByteAsInt() << 8);
    }

    @Override
    public int getIndex()
    {
        int firstByte = getUnsignedByteAsInt();
        if ((firstByte & 0x80) == 0)
        {
            return firstByte;
        }
        int secondByte = getUnsignedByteAsInt();
        return ((firstByte & 0x7F) << 8) | secondByte;
    }

    @Override
    public int getCommunalField()
    {
        int val = getUnsignedByteAsInt();
        if (val < 0x80) return val;
        if (val == 0x81) return getWord();
        if (val == 0x84)
        {
            return getUnsignedByteAsInt() | (getUnsignedByteAsInt() << 8) | (getUnsignedByteAsInt() << 16);
        }
        if (val == 0x88)
        {
            return getUnsignedByteAsInt() | (getUnsignedByteAsInt() << 8) | (getUnsignedByteAsInt() << 16) | (getUnsignedByteAsInt() << 24);
        }
        return 0;
    }
}
