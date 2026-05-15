package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.LinkedLayout;

import java.util.List;

public class ImageEmitterImpl implements ImageEmitter
{
    private final ImageSizer sizer;
    private final PieceLookupFactory pieceLookupFactory;
    private final LedataPlacer ledataPlacer;
    private final GlobalSymbolTableFactory symbolTableFactory;
    private final FixupApplier fixupApplier;

    public ImageEmitterImpl(ImageSizer sizer,
                            PieceLookupFactory pieceLookupFactory,
                            LedataPlacer ledataPlacer,
                            GlobalSymbolTableFactory symbolTableFactory,
                            FixupApplier fixupApplier)
    {
        this.sizer = sizer;
        this.pieceLookupFactory = pieceLookupFactory;
        this.ledataPlacer = ledataPlacer;
        this.symbolTableFactory = symbolTableFactory;
        this.fixupApplier = fixupApplier;
    }

    @Override
    public byte[] emit(List<OMFFile> modulesInInputOrder, LinkedLayout layout)
    {
        byte[] image = new byte[sizer.size(layout)];
        PieceLookup lookup = pieceLookupFactory.build(layout.combinedSegments());
        GlobalSymbolTable symbols = symbolTableFactory.build(modulesInInputOrder, layout);
        ledataPlacer.place(image, modulesInInputOrder, lookup);
        fixupApplier.apply(image, modulesInInputOrder, lookup, symbols);
        return image;
    }
}
