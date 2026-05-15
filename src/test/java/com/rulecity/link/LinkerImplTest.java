package com.rulecity.link;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.LinkedLayout;
import com.rulecity.link.data.ResolvedSymbol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LinkerImplTest
{
    @Mock private SegmentCombiner combiner;
    @Mock private SegmentLayouter layouter;
    @Mock private SymbolResolver symbolResolver;
    @Mock private OMFFile moduleA;
    @InjectMocks private LinkerImpl linker;

    @Test
    public void invokesPhasesInOrder_andSeparatesPublicFromLocal()
    {
        List<OMFFile> modules = List.of(moduleA);
        List<CombinedSegment> combined = List.of();
        List<CombinedSegment> placed = List.of();
        List<ResolvedSymbol> all = List.of(
                new ResolvedSymbol("pub1", 0x10, "A", "_text", false),
                new ResolvedSymbol("loc1", 0x20, "A", "_text", true),
                new ResolvedSymbol("pub2", 0x30, "A", "_text", false));
        when(combiner.combine(modules)).thenReturn(combined);
        when(layouter.layout(combined)).thenReturn(placed);
        when(symbolResolver.resolve(modules, placed)).thenReturn(all);

        LinkedLayout layout = linker.link(modules);

        InOrder order = inOrder(combiner, layouter, symbolResolver);
        order.verify(combiner).combine(modules);
        order.verify(layouter).layout(combined);
        order.verify(symbolResolver).resolve(modules, placed);

        assertEquals(2, layout.publicSymbols().size());
        assertEquals(1, layout.localSymbols().size());
        assertEquals("pub1", layout.publicSymbols().get(0).name());
        assertEquals("pub2", layout.publicSymbols().get(1).name());
        assertEquals("loc1", layout.localSymbols().get(0).name());
    }
}
