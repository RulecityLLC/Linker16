package com.rulecity.parse.handler;

import com.rulecity.parse.data.Fixup;
import com.rulecity.parse.io.ByteCursor;

/**
 * Reads a single FIXUP subrecord (the Fix Data + frame/target datum + optional displacement)
 * from the cursor after the caller has already consumed the locat word.
 */
public interface FixupReader
{
    Fixup readFixup(ByteCursor cursor, boolean segmentRelativeFixups, byte location, int dataRecordOffset);
}
