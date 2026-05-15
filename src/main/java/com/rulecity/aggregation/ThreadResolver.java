package com.rulecity.aggregation;

import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.ThreadProcessed;

/**
 * Replaces any thread-field references in a {@link FixupProcessed} with the
 * concrete frame/target method + index from the supplied thread state. The
 * returned FixupProcessed has its {@code threadFieldContaining...} fields null
 * and its {@code methodFrame}/{@code methodTarget} plus index slots populated.
 */
public interface ThreadResolver
{
    FixupProcessed resolve(FixupProcessed fp,
                           ThreadProcessed[] frameThreads,
                           ThreadProcessed[] targetThreads);
}
