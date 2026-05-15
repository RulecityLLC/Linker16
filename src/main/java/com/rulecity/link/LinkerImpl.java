package com.rulecity.link;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.LinkedLayout;
import com.rulecity.link.data.ResolvedSymbol;

import java.util.ArrayList;
import java.util.List;

public class LinkerImpl implements Linker
{
    private final SegmentCombiner combiner;
    private final SegmentLayouter layouter;
    private final SymbolResolver symbolResolver;

    public LinkerImpl(SegmentCombiner combiner,
                      SegmentLayouter layouter,
                      SymbolResolver symbolResolver)
    {
        this.combiner = combiner;
        this.layouter = layouter;
        this.symbolResolver = symbolResolver;
    }

    @Override
    public LinkedLayout link(List<OMFFile> modulesInInputOrder)
    {
        List<CombinedSegment> combined = combiner.combine(modulesInInputOrder);
        List<CombinedSegment> placed = layouter.layout(combined);
        List<ResolvedSymbol> all = symbolResolver.resolve(modulesInInputOrder, placed);

        List<ResolvedSymbol> publics = new ArrayList<>();
        List<ResolvedSymbol> locals = new ArrayList<>();
        for (ResolvedSymbol s : all)
        {
            if (s.isLocal()) locals.add(s); else publics.add(s);
        }
        return new LinkedLayout(placed, List.copyOf(publics), List.copyOf(locals));
    }
}
