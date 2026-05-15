package com.rulecity.link;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.ResolvedSymbol;
import com.rulecity.link.data.SegmentPiece;
import com.rulecity.parse.data.PublicNameAndOffset;
import com.rulecity.parse.data.PublicNamesDefinitionProcessed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolResolverImpl implements SymbolResolver
{
    @Override
    public List<ResolvedSymbol> resolve(List<OMFFile> modulesInInputOrder,
                                        List<CombinedSegment> placedSegments)
    {
        // (moduleIdx, moduleSegmentIdx) -> (combined, piece) lookup.
        Map<Long, PlacedPiece> lookup = new HashMap<>();
        for (CombinedSegment cs : placedSegments)
        {
            for (SegmentPiece p : cs.pieces())
            {
                lookup.put(pieceKey(p.moduleIndex(), p.moduleSegmentIdx()), new PlacedPiece(cs, p));
            }
        }

        List<ResolvedSymbol> out = new ArrayList<>();
        for (int m = 0; m < modulesInInputOrder.size(); m++)
        {
            OMFFile module = modulesInInputOrder.get(m);
            for (PublicNamesDefinitionProcessed pdef : module.getPublicSymbols())
            {
                Integer baseSegIdx = pdef.baseSegmentIdx();
                PlacedPiece pp = (baseSegIdx != null) ? lookup.get(pieceKey(m, baseSegIdx)) : null;
                if (baseSegIdx != null && pp == null)
                {
                    throw new RuntimeException("PUBDEF references unplaced segment idx "
                            + baseSegIdx + " in module " + module.getModuleName());
                }
                for (PublicNameAndOffset n : pdef.lstNamesAndOffsets())
                {
                    int imageOffset;
                    String segName;
                    if (pp != null)
                    {
                        imageOffset = pp.combined.imageOffset() + pp.piece.offsetWithinCombined() + n.publicOffset();
                        segName = pp.combined.name();
                    }
                    else
                    {
                        // Group-relative (or absolute frame): DGROUP starts at image offset 0
                        // in the small-model layout we're targeting, so publicOffset IS the
                        // image offset. (See e.g. asmlib's __acrtused at 0x9876 in DL2.MAP.)
                        imageOffset = n.publicOffset();
                        segName = null;
                    }
                    out.add(new ResolvedSymbol(n.publicNameString(), imageOffset,
                            module.getModuleName(), segName, pdef.isLPUBDEF()));
                }
            }
        }
        return out;
    }

    private static long pieceKey(int moduleIdx, int moduleSegmentIdx)
    {
        return (((long) moduleIdx) << 32) | (moduleSegmentIdx & 0xFFFFFFFFL);
    }

    private record PlacedPiece(CombinedSegment combined, SegmentPiece piece) {}
}
