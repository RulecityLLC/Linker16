package com.rulecity.aggregation;

import com.rulecity.parse.OMFItem;

import java.util.List;

/**
 * Builds the read-only {@link OMFFile} view of one OBJ file from its parsed
 * OMFItem stream. Holds the long-lived collaborators (dispatcher + LEDATA
 * lifecycle); each call constructs a fresh mutable {@link com.rulecity.aggregation.state.AggregationState}.
 */
public interface OMFFileFactory
{
    OMFFile build(List<OMFItem> items, String sourceFilename);
}
