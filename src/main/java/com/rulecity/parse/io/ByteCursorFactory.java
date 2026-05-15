package com.rulecity.parse.io;

/**
 * Constructs a fresh {@link ByteCursor} over a source buffer. Injecting a
 * factory (rather than directly instantiating a cursor) lets the parser be
 * reused across multiple parseBinary calls and lets tests substitute a mock cursor.
 */
public interface ByteCursorFactory
{
    ByteCursor create(byte[] src);
}
