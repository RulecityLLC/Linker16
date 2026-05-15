package com.rulecity.parse.handler;

import com.rulecity.parse.OMFItem;
import com.rulecity.parse.io.ByteCursor;

/**
 * Reads the body of a single OMF record (after the record type + length have been
 * consumed) and returns an OMFItem. Bytes consumed must equal recordLength - 1
 * (the trailing checksum byte is read by the parser, not the handler).
 */
public interface RecordHandler
{
    OMFItem handle(ByteCursor cursor);
}
