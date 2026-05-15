package com.rulecity.link.data;

import com.rulecity.parse.OMFItemSEGDEF;

import java.util.List;

/**
 * Result of combining same-named SEGDEFs from one or more modules into a single
 * runtime segment, with each module's piece placed at a known offset.
 *
 * @param name             Segment name (e.g. {@code "_text"}).
 * @param className        Class name (e.g. {@code "code"}).
 * @param combination      How this segment's pieces are combined (PUBLIC concatenates;
 *                         COMMON overlays). For DL2 this is always PUBLIC.
 * @param alignmentBytes   Required alignment (in bytes) for this combined segment's
 *                         start position — equals the max alignment among its pieces,
 *                         so every piece's image-relative position is naturally aligned.
 * @param imageOffset      Final byte offset of this segment's start within the linked
 *                         image (relative to image offset 0, which equals the start of
 *                         DGROUP). {@code -1} indicates "not yet laid out".
 * @param totalLength      Total byte length of the combined segment.
 * @param pieces           Pieces in the order they were appended (== module input
 *                         order, filtered to modules that defined this segment).
 */
public record CombinedSegment(String name,
                              String className,
                              OMFItemSEGDEF.Combination combination,
                              int alignmentBytes,
                              int imageOffset,
                              int totalLength,
                              List<SegmentPiece> pieces)
{
}
