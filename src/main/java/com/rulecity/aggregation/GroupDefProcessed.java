package com.rulecity.aggregation;

import java.util.List;

public record GroupDefProcessed(String name, List<SegmentDefProcessed> lstSegDefs)
{
}
