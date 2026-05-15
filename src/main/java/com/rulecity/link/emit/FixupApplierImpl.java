package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.parse.data.FixupProcessed;
import com.rulecity.parse.data.LedataChunk;

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
    public int apply(byte[] image,
                     List<OMFFile> modulesInInputOrder,
                     PieceLookup lookup,
                     GlobalSymbolTable symbols)
    {
        int unresolved = 0;
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
                        unresolved++;
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
}
