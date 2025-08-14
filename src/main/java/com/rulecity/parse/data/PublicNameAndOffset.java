package com.rulecity.parse.data;

/**
 * This is 'processed' because it strips out the type index that isn't used
 * @param publicNameString
 * @param publicOffset
 */
public record PublicNameAndOffset(String publicNameString,
                                  int publicOffset)
{
}
