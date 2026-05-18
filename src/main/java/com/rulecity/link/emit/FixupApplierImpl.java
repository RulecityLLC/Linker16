package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.parse.data.Communal;
import com.rulecity.parse.data.ExternalNamesDefinition;
import com.rulecity.parse.data.ExternalOrRelated;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.LedataChunk;

import java.util.ArrayList;
import java.util.List;

public class FixupApplierImpl implements FixupApplier
{
    private final FrameResolver frameResolver;
    private final TargetResolver targetResolver;
    private final FixupValueWriter valueWriter;

    public FixupApplierImpl(FrameResolver frameResolver,
                            TargetResolver targetResolver,
                            FixupValueWriter valueWriter)
    {
        this.frameResolver = frameResolver;
        this.targetResolver = targetResolver;
        this.valueWriter = valueWriter;
    }

    @Override
    public List<Unresolved> apply(byte[] image,
                                  List<OMFFile> modulesInInputOrder,
                                  PieceLookup lookup,
                                  GlobalSymbolTable symbols)
    {
        List<Unresolved> unresolved = new ArrayList<>();
        for (int m = 0; m < modulesInInputOrder.size(); m++)
        {
            OMFFile module = modulesInInputOrder.get(m);
            for (LedataChunk chunk : module.getLedataChunks())
            {
                PieceLookup.Placement pp = lookup.find(m, chunk.segmentIdx());
                int chunkImageOffset = pp.combined().imageOffset()
                        + pp.piece().offsetWithinCombined()
                        + chunk.offsetInSegment();
                for (FixupProcessed fixup : chunk.fixups())
                {
                    Integer targetOffset = targetResolver.imageOffset(
                            fixup, m, module, lookup, symbols);
                    if (targetOffset == null)
                    {
                        // Only EXTDEF targets can resolve to null (SEGDEF/GRPDEF
                        // throw on missing data inside TargetResolver), so the
                        // missing name lives in the module's external table.
                        unresolved.add(new Unresolved(
                                externalName(module, fixup.idxExternalTarget()),
                                module.getModuleName()));
                        continue;
                    }
                    int framePara = frameResolver.paragraph(
                            fixup, m, module, lookup, symbols, targetOffset);
                    int locInImage = chunkImageOffset + fixup.dataRecordOffset();
                    valueWriter.write(image, locInImage, fixup.location(),
                            fixup.segmentRelativeFixups(), framePara, targetOffset);
                }
            }
        }
        return unresolved;
    }

    private static String externalName(OMFFile module, Integer extIdx)
    {
        if (extIdx == null) return "<unknown>";
        ExternalOrRelated e = module.getExternals().get(extIdx);
        ExternalNamesDefinition ext = e.external();
        if (ext != null) return ext.externalNameString();
        ExternalNamesDefinition lext = e.localExternal();
        if (lext != null) return lext.externalNameString();
        Communal c = e.communal();
        if (c != null) return c.name();
        return "<unknown>";
    }
}
