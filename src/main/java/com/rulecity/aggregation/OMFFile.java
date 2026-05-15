package com.rulecity.aggregation;

import com.rulecity.parse.data.ExternalOrRelated;
import com.rulecity.parse.data.GroupDefProcessed;
import com.rulecity.parse.data.LedataChunk;
import com.rulecity.parse.data.PublicNamesDefinitionProcessed;
import com.rulecity.parse.data.SegmentDefProcessed;

import java.util.List;

/**
 * One OBJ file's records aggregated into a module-level view suitable for linking.
 * Indices used inside this view are 0-based and refer to the lists returned here
 * (e.g. {@link #getSegmentDefs()}, {@link #getExternals()}).
 */
public interface OMFFile
{
    /** Module name from THEADR (e.g. {@code C:\SOURCE\ASMLIB.ASM}). */
    String getModuleName();

    /** LNAMES strings in OBJ order. */
    List<String> getLnames();

    /** SEGDEFs in OBJ order. */
    List<SegmentDefProcessed> getSegmentDefs();

    /** GRPDEFs in OBJ order (e.g. DGROUP membership). */
    List<GroupDefProcessed> getGroupDefs();

    /** All PUBDEF / LPUBDEF records, in OBJ order. */
    List<PublicNamesDefinitionProcessed> getPublicSymbols();

    /** EXTDEF + LEXTDEF + COMDEF, unified, in OBJ order. */
    List<ExternalOrRelated> getExternals();

    /** All LEDATA records with their bound (thread-resolved) FIXUPPs, in OBJ order. */
    List<LedataChunk> getLedataChunks();
}
