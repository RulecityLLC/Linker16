package com.rulecity.aggregation.state;

import com.rulecity.parse.data.ExternalOrRelated;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.GroupDefProcessed;
import com.rulecity.parse.data.LedataChunk;
import com.rulecity.parse.data.PublicNamesDefinitionProcessed;
import com.rulecity.parse.data.SegmentDefProcessed;
import com.rulecity.parse.data.ThreadProcessed;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable accumulator that grows as each OMFItem from a single OBJ file is
 * folded in by the per-type aggregators. After all items are processed it
 * becomes the read-only data for the {@code OMFFile} view.
 */
public class AggregationState
{
    private String moduleName = "";
    private final List<String> lstNames = new ArrayList<>();
    private final List<SegmentDefProcessed> lstSegDef = new ArrayList<>();
    private final List<GroupDefProcessed> lstGrpDef = new ArrayList<>();
    private final List<PublicNamesDefinitionProcessed> lstPubNames = new ArrayList<>();
    private final List<ExternalOrRelated> lstExternal = new ArrayList<>();
    private final List<LedataChunk> lstLedataChunks = new ArrayList<>();

    // OMF reserves 4 frame threads and 4 target threads (TIS 1.1 sec 4.7),
    // carried across FIXUPP records within a single module.
    private final ThreadProcessed[] arrFrameThreads = new ThreadProcessed[4];
    private final ThreadProcessed[] arrTargetThreads = new ThreadProcessed[4];

    // Open-LEDATA accumulators: any FIXUPP record we see next binds to this chunk.
    private int currentLedataSegmentIdx = -1;
    private int currentLedataOffset = -1;
    private byte[] currentLedataBytes = null;
    private List<FixupProcessed> currentLedataFixups = null;

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public List<String> getLnames() { return lstNames; }
    public List<SegmentDefProcessed> getSegmentDefs() { return lstSegDef; }
    public List<GroupDefProcessed> getGroupDefs() { return lstGrpDef; }
    public List<PublicNamesDefinitionProcessed> getPublicSymbols() { return lstPubNames; }
    public List<ExternalOrRelated> getExternals() { return lstExternal; }
    public List<LedataChunk> getLedataChunks() { return lstLedataChunks; }

    public ThreadProcessed[] getFrameThreads() { return arrFrameThreads; }
    public ThreadProcessed[] getTargetThreads() { return arrTargetThreads; }

    public int getCurrentLedataSegmentIdx() { return currentLedataSegmentIdx; }
    public void setCurrentLedataSegmentIdx(int v) { this.currentLedataSegmentIdx = v; }

    public int getCurrentLedataOffset() { return currentLedataOffset; }
    public void setCurrentLedataOffset(int v) { this.currentLedataOffset = v; }

    public byte[] getCurrentLedataBytes() { return currentLedataBytes; }
    public void setCurrentLedataBytes(byte[] v) { this.currentLedataBytes = v; }

    public List<FixupProcessed> getCurrentLedataFixups() { return currentLedataFixups; }
    public void setCurrentLedataFixups(List<FixupProcessed> v) { this.currentLedataFixups = v; }
}
