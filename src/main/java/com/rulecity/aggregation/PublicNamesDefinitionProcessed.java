package com.rulecity.aggregation;

import java.util.List;

public record PublicNamesDefinitionProcessed(Integer baseGroupIdx, Integer baseSegmentIdx, Integer baseFrame,
                                             List<PublicNameAndOffset> lstNamesAndOffsets)
{
}
