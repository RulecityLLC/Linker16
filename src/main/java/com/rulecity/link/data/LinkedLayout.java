package com.rulecity.link.data;

import java.util.List;

/**
 * Output of the linker layer: combined segments with image offsets and every
 * PUBDEF/LPUBDEF resolved to its final image offset. (FIXUPP application and
 * image emission live in Phase 4.)
 */
public record LinkedLayout(List<CombinedSegment> combinedSegments,
                           List<ResolvedSymbol> publicSymbols,
                           List<ResolvedSymbol> localSymbols)
{
}
