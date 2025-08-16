package com.rulecity.parse.data;

import java.util.List;

public record PublicNamesDefinitionProcessed(Integer baseGroupIdx, Integer baseSegmentIdx, Integer baseFrame,
                                             List<PublicNameAndOffset> lstNamesAndOffsets,

                                             // if isPrivate is true, this is a LPUBDEF record, not a PUBDEF record
                                             boolean isLPUBDEF)
{
}
