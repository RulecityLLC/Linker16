package com.rulecity.parse.io;

/**
 * Mutable cursor over the bytes of one OBJ file. Tracks the global position,
 * the current record's running checksum, and the count of payload bytes
 * consumed during the handler's body.
 *
 * <p>Per TIS 1.1 the trailing checksum byte of a record is chosen so that the
 * unsigned sum of every byte in the record (type + length-lo + length-hi +
 * payload + checksum) is 0 mod 256. We model that as a running checksum that
 * is initialized to zero at {@link #beginRecord()}, decremented by every byte
 * read via {@link #getSignedByte()} (and friends), and compared to the
 * trailing checksum byte read via {@link #readRawByte()}.
 *
 * <p>The {@code recordCount} tracks bytes consumed AFTER the
 * {@link #markStartOfPayload(int)} call — i.e. the handler's payload only —
 * so it can be compared against {@code recordLength - 1} to detect under/over-read.
 */
public interface ByteCursor
{
    /** True iff the cursor has not consumed the entire source buffer. */
    boolean hasMore();

    /** Begin a new record: zero the running checksum and reset the payload-byte counter. */
    void beginRecord();

    /**
     * Mark the boundary between the record header (type + length) and the payload.
     * Captures the record length and zeros the payload-byte counter, but leaves
     * the running checksum intact (so the type and length bytes already contribute).
     */
    void markStartOfPayload(int recordLength);

    int getRecordLength();
    int getRecordCount();
    byte getChecksum();

    /** Reads the literal next byte WITHOUT contributing to the running checksum or recordCount. */
    byte readRawByte();

    byte getSignedByte();
    int getUnsignedByteAsInt();
    int getWord();

    /** TIS 1.1 sec 2.2 variable-width index (1- or 2-byte unsigned). */
    int getIndex();

    /** TIS 1.1 COMDEF length field (1, 3, 4, or 5 bytes depending on lead byte). */
    int getCommunalField();
}
