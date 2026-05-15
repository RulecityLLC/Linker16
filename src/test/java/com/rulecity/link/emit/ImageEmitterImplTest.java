package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.LinkedLayout;
import com.rulecity.parse.OMFItemSEGDEF;
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
public class ImageEmitterImplTest
{
    @Mock private ImageSizer sizer;
    @Mock private PieceLookupFactory pieceLookupFactory;
    @Mock private LedataPlacer ledataPlacer;
    @Mock private GlobalSymbolTableFactory symbolTableFactory;
    @Mock private FixupApplier fixupApplier;
    @Mock private PieceLookup lookup;
    @Mock private GlobalSymbolTable symbols;
    @Mock private OMFFile moduleA;
    @InjectMocks private ImageEmitterImpl emitter;

    @Test
    public void sizesPlacesAndAppliesInOrder()
    {
        List<CombinedSegment> segs = List.of(new CombinedSegment(
                "_text", "code", OMFItemSEGDEF.Combination.PUBLIC, 1, 0, 0x10, List.of()));
        LinkedLayout layout = new LinkedLayout(segs, List.of(), List.of());
        List<OMFFile> modules = List.of(moduleA);

        when(sizer.size(layout)).thenReturn(0x10);
        when(pieceLookupFactory.build(segs)).thenReturn(lookup);
        when(symbolTableFactory.build(modules, layout)).thenReturn(symbols);

        byte[] image = emitter.emit(modules, layout);

        assertEquals(0x10, image.length);
        InOrder order = inOrder(sizer, pieceLookupFactory, symbolTableFactory,
                ledataPlacer, fixupApplier);
        order.verify(sizer).size(layout);
        order.verify(pieceLookupFactory).build(segs);
        order.verify(symbolTableFactory).build(modules, layout);
        order.verify(ledataPlacer).place(image, modules, lookup);
        order.verify(fixupApplier).apply(image, modules, lookup, symbols);
    }
}
