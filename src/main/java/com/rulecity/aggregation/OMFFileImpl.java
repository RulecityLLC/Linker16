package com.rulecity.aggregation;

import com.rulecity.aggregation.state.AggregationState;
import com.rulecity.parse.data.ExternalOrRelated;
import com.rulecity.parse.data.GroupDefProcessed;
import com.rulecity.parse.data.LedataChunk;
import com.rulecity.parse.data.PublicNamesDefinitionProcessed;
import com.rulecity.parse.data.SegmentDefProcessed;

import java.util.Collections;
import java.util.List;

/**
 * Read-only view over a single OBJ file's aggregated state. The aggregation
 * itself is performed by {@link OMFFileFactoryImpl}; this class is a thin
 * adapter that exposes the finalized {@link AggregationState}.
 */
public class OMFFileImpl implements OMFFile
{
    private final AggregationState state;
    private final String sourceFilename;

    public OMFFileImpl(AggregationState state, String sourceFilename)
    {
        this.state = state;
        this.sourceFilename = sourceFilename;
    }

    @Override public String getSourceFilename() { return sourceFilename; }
    @Override public String getModuleName() { return state.getModuleName(); }
    @Override public List<String> getLnames() { return Collections.unmodifiableList(state.getLnames()); }
    @Override public List<SegmentDefProcessed> getSegmentDefs() { return Collections.unmodifiableList(state.getSegmentDefs()); }
    @Override public List<GroupDefProcessed> getGroupDefs() { return Collections.unmodifiableList(state.getGroupDefs()); }
    @Override public List<PublicNamesDefinitionProcessed> getPublicSymbols() { return Collections.unmodifiableList(state.getPublicSymbols()); }
    @Override public List<ExternalOrRelated> getExternals() { return Collections.unmodifiableList(state.getExternals()); }
    @Override public List<LedataChunk> getLedataChunks() { return Collections.unmodifiableList(state.getLedataChunks()); }
}
