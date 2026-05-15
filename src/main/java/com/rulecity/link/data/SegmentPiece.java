package com.rulecity.link.data;

/**
 * One module's contribution to a combined segment.
 *
 * @param moduleIndex         0-based index of the module in input order (i.e. the
 *                            order modules appeared on the linker command line).
 * @param moduleName          The contributing module's THEADR-derived name.
 * @param moduleSegmentIdx    0-based index into the module's {@code getSegmentDefs()}.
 * @param offsetWithinCombined Byte offset of this piece relative to the start of
 *                            the enclosing {@link CombinedSegment}.
 * @param length              Length of this piece (= SEGDEF length from the OBJ).
 */
public record SegmentPiece(int moduleIndex,
                           String moduleName,
                           int moduleSegmentIdx,
                           int offsetWithinCombined,
                           int length)
{
}
