package com.rulecity.parse.data;

public record Fixup(boolean segmentRelativeFixups, byte location, int dataRecordOffset,
                    boolean frameSpecifiedByPreviousThreadFieldRef,
                    int frame,
                    boolean targetSpecifiedByPreviousThreadFieldRef,
                    int targt,
                    Byte frameDatum,
                    Byte targetDatum,
                    Integer targetDisplacement)
{
}
