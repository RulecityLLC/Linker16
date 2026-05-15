package com.rulecity.parse.data;

public record Fixup(boolean segmentRelativeFixups, byte location, int dataRecordOffset,
                    boolean frameSpecifiedByPreviousThreadFieldRef,
                    int frame,
                    boolean targetSpecifiedByPreviousThreadFieldRef,
                    int targt,
                    // 1-based OMF index (per TIS 1.1): 1..32767. Wider than a byte because
                    // OMF index fields use a variable-width encoding (high-bit-set first byte
                    // means a 2-byte form). null when omitted (frame method 3+ or thread-ref).
                    Integer frameDatum,
                    Integer targetDatum,
                    Integer targetDisplacement)
{
}
