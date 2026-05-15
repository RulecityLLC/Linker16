package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.parse.data.LedataChunk;

import java.util.List;

public class LedataPlacerImpl implements LedataPlacer
{
    @Override
    public void place(byte[] image, List<OMFFile> modulesInInputOrder, PieceLookup lookup)
    {
        for (int m = 0; m < modulesInInputOrder.size(); m++)
        {
            OMFFile module = modulesInInputOrder.get(m);
            for (LedataChunk chunk : module.getLedataChunks())
            {
                PieceLookup.Placement pp = lookup.find(m, chunk.segmentIdx());
                if (pp == null)
                {
                    throw new RuntimeException("LEDATA references unplaced segment idx "
                            + chunk.segmentIdx() + " in module " + module.getModuleName());
                }
                int dest = pp.combined().imageOffset()
                        + pp.piece().offsetWithinCombined()
                        + chunk.offsetInSegment();
                System.arraycopy(chunk.bytes(), 0, image, dest, chunk.bytes().length);
            }
        }
    }
}
