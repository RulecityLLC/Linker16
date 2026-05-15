package com.rulecity.parse.handler;

import com.rulecity.parse.data.FixupOrThread;
import com.rulecity.parse.data.FixupOrThreadProcessed;

/**
 * Converts a raw FIXUP/THREAD subrecord into its "processed" enum-rich form
 * (decoded location, frame-method, target-method, and disambiguated index slots).
 * Pure: no per-module state — that's the aggregation layer's job.
 */
public interface FixupOrThreadProcessor
{
    FixupOrThreadProcessed process(FixupOrThread raw);
}
