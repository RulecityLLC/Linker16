package com.rulecity;

import com.rulecity.aggregation.ItemAggregator;
import com.rulecity.aggregation.ItemAggregatorDispatcher;
import com.rulecity.aggregation.ItemAggregatorDispatcherImpl;
import com.rulecity.aggregation.LedataChunkLifecycle;
import com.rulecity.aggregation.LedataChunkLifecycleImpl;
import com.rulecity.aggregation.OMFFileFactory;
import com.rulecity.aggregation.OMFFileFactoryImpl;
import com.rulecity.aggregation.ThreadResolver;
import com.rulecity.aggregation.ThreadResolverImpl;
import com.rulecity.aggregation.handler.ComdefItemAggregator;
import com.rulecity.aggregation.handler.CommentItemAggregator;
import com.rulecity.aggregation.handler.ExtdefItemAggregator;
import com.rulecity.aggregation.handler.FixuppItemAggregator;
import com.rulecity.aggregation.handler.GrpdefItemAggregator;
import com.rulecity.aggregation.handler.LedataItemAggregator;
import com.rulecity.aggregation.handler.LnamesItemAggregator;
import com.rulecity.aggregation.handler.ModendItemAggregator;
import com.rulecity.aggregation.handler.PubdefItemAggregator;
import com.rulecity.aggregation.handler.SegdefItemAggregator;
import com.rulecity.aggregation.handler.TheadrItemAggregator;
import com.rulecity.link.AlignmentResolver;
import com.rulecity.link.AlignmentResolverImpl;
import com.rulecity.link.Linker;
import com.rulecity.link.LinkerImpl;
import com.rulecity.link.SegmentCombiner;
import com.rulecity.link.SegmentCombinerImpl;
import com.rulecity.link.SegmentLayouter;
import com.rulecity.link.SegmentLayouterImpl;
import com.rulecity.link.SymbolResolver;
import com.rulecity.link.SymbolResolverImpl;
import com.rulecity.link.emit.ComdefAllocator;
import com.rulecity.link.emit.ComdefAllocatorImpl;
import com.rulecity.link.emit.FixupApplier;
import com.rulecity.link.emit.FixupApplierImpl;
import com.rulecity.link.emit.FixupValueWriter;
import com.rulecity.link.emit.FixupValueWriterImpl;
import com.rulecity.link.emit.FrameResolver;
import com.rulecity.link.emit.FrameResolverImpl;
import com.rulecity.link.emit.GlobalSymbolTableFactory;
import com.rulecity.link.emit.GlobalSymbolTableFactoryImpl;
import com.rulecity.link.emit.ImageEmitter;
import com.rulecity.link.emit.ImageEmitterImpl;
import com.rulecity.link.emit.ImageSizer;
import com.rulecity.link.emit.UnresolvedExternalsException;
import com.rulecity.link.emit.ImageSizerImpl;
import com.rulecity.link.emit.LedataPlacer;
import com.rulecity.link.emit.LedataPlacerImpl;
import com.rulecity.link.emit.PieceLookupFactory;
import com.rulecity.link.emit.PieceLookupFactoryImpl;
import com.rulecity.link.emit.TargetResolver;
import com.rulecity.link.emit.TargetResolverImpl;
import com.rulecity.parse.OMFItem;
import com.rulecity.parse.OMFItemCOMDEF;
import com.rulecity.parse.OMFItemCOMENT;
import com.rulecity.parse.OMFItemEXTDEF;
import com.rulecity.parse.OMFItemFIXUPP;
import com.rulecity.parse.OMFItemGRPDEF;
import com.rulecity.parse.OMFItemLEDATA;
import com.rulecity.parse.OMFItemLNAMES;
import com.rulecity.parse.OMFItemMODEND;
import com.rulecity.parse.OMFItemPUBDEF;
import com.rulecity.parse.OMFItemSEGDEF;
import com.rulecity.parse.OMFItemTHEADR;
import com.rulecity.parse.OMFParser;
import com.rulecity.parse.OMFParserImpl;
import com.rulecity.parse.handler.ComdefRecordHandler;
import com.rulecity.parse.handler.CommentRecordHandler;
import com.rulecity.parse.handler.ExtdefRecordHandler;
import com.rulecity.parse.handler.FixupOrThreadProcessor;
import com.rulecity.parse.handler.FixupOrThreadProcessorImpl;
import com.rulecity.parse.handler.FixupReader;
import com.rulecity.parse.handler.FixupReaderImpl;
import com.rulecity.parse.handler.FixuppRecordHandler;
import com.rulecity.parse.handler.GrpdefRecordHandler;
import com.rulecity.parse.handler.LedataRecordHandler;
import com.rulecity.parse.handler.LnamesRecordHandler;
import com.rulecity.parse.handler.ModendRecordHandler;
import com.rulecity.parse.handler.PubdefRecordHandler;
import com.rulecity.parse.handler.RecordHandler;
import com.rulecity.parse.handler.SegdefRecordHandler;
import com.rulecity.parse.handler.TheadrRecordHandler;
import com.rulecity.parse.io.ByteCursorFactory;
import com.rulecity.parse.io.ByteCursorFactoryImpl;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.LinkedLayout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main
{
    private static final String USAGE =
            "Usage: Linker16 --dgroup-paragraph=<hex> -o <output> <obj1> <obj2> ...\n"
                    + "  --dgroup-paragraph=<hex>  Runtime paragraph (segment value) at which DGROUP\n"
                    + "                            lives when the linked image executes. For DL2 v3.19\n"
                    + "                            this is 0x0008. The value is not derivable from the\n"
                    + "                            OBJ inputs — supply it explicitly based on the\n"
                    + "                            target BIOS/bootstrap.\n"
                    + "  -o <output>               Output binary path.\n"
                    + "  <objN>                    Input OBJ files in EPROM.LNK order.";

    public static void main(String[] args) throws IOException
    {
        Integer dgroupParagraph = null;
        Path output = null;
        List<Path> objs = new ArrayList<>();
        for (int i = 0; i < args.length; i++)
        {
            String a = args[i];
            if (a.startsWith("--dgroup-paragraph="))
            {
                dgroupParagraph = Integer.decode(a.substring("--dgroup-paragraph=".length()));
            }
            else if (a.equals("-o") && i + 1 < args.length)
            {
                output = Paths.get(args[++i]);
            }
            else
            {
                objs.add(Paths.get(a));
            }
        }

        if (dgroupParagraph == null || output == null || objs.isEmpty())
        {
            System.out.println(USAGE);
            System.exit(1);
        }

        OMFParser parser = buildParser();
        OMFFileFactory fileFactory = buildOMFFileFactory();
        Linker linker = buildLinker();
        ImageEmitter emitter = buildImageEmitter(dgroupParagraph);

        List<OMFFile> modules = new ArrayList<>();
        for (Path p : objs)
        {
            List<OMFItem> items = parser.parseBinary(Files.readAllBytes(p));
            modules.add(fileFactory.build(items, p.toString()));
        }
        LinkedLayout layout = linker.link(modules);
        byte[] image;
        try
        {
            image = emitter.emit(modules, layout);
        }
        catch (UnresolvedExternalsException ex)
        {
            System.err.println(ex.getMessage());
            System.exit(1);
            return;
        }
        Files.write(output, image);
        System.out.printf("Wrote %d bytes to %s (DGROUP paragraph=0x%04X)%n",
                image.length, output, dgroupParagraph);
    }

    /** Composition root for the parser layer. */
    public static OMFParser buildParser()
    {
        ByteCursorFactory cursorFactory = new ByteCursorFactoryImpl();
        FixupReader fixupReader = new FixupReaderImpl();
        FixupOrThreadProcessor fixupOrThreadProcessor = new FixupOrThreadProcessorImpl();

        Map<Byte, RecordHandler> handlers = new LinkedHashMap<>();
        handlers.put((byte) 0x80, new TheadrRecordHandler());
        handlers.put((byte) 0x88, new CommentRecordHandler());
        handlers.put((byte) 0x8A, new ModendRecordHandler());
        handlers.put((byte) 0x8C, new ExtdefRecordHandler(false));
        handlers.put((byte) 0x90, new PubdefRecordHandler(false));
        handlers.put((byte) 0x96, new LnamesRecordHandler());
        handlers.put((byte) 0x98, new SegdefRecordHandler());
        handlers.put((byte) 0x9A, new GrpdefRecordHandler());
        handlers.put((byte) 0x9C, new FixuppRecordHandler(fixupReader, fixupOrThreadProcessor));
        handlers.put((byte) 0xA0, new LedataRecordHandler());
        handlers.put((byte) 0xB0, new ComdefRecordHandler());
        handlers.put((byte) 0xB4, new ExtdefRecordHandler(true));
        handlers.put((byte) 0xB6, new PubdefRecordHandler(true));

        return new OMFParserImpl(cursorFactory, handlers);
    }

    /** Composition root for the aggregation layer. */
    public static OMFFileFactory buildOMFFileFactory()
    {
        ThreadResolver threadResolver = new ThreadResolverImpl();
        LedataChunkLifecycle ledataChunkLifecycle = new LedataChunkLifecycleImpl();

        // Map keyed by item INTERFACE class so concrete subclasses dispatch via isInstance.
        // Use LinkedHashMap so the lookup order is stable and predictable.
        Map<Class<?>, ItemAggregator> aggregators = new LinkedHashMap<>();
        aggregators.put(OMFItemTHEADR.class, new TheadrItemAggregator());
        aggregators.put(OMFItemCOMENT.class, new CommentItemAggregator());
        aggregators.put(OMFItemMODEND.class, new ModendItemAggregator());
        aggregators.put(OMFItemEXTDEF.class, new ExtdefItemAggregator());
        aggregators.put(OMFItemPUBDEF.class, new PubdefItemAggregator());
        aggregators.put(OMFItemLNAMES.class, new LnamesItemAggregator());
        aggregators.put(OMFItemSEGDEF.class, new SegdefItemAggregator());
        aggregators.put(OMFItemGRPDEF.class, new GrpdefItemAggregator());
        aggregators.put(OMFItemFIXUPP.class, new FixuppItemAggregator(threadResolver));
        aggregators.put(OMFItemLEDATA.class, new LedataItemAggregator(ledataChunkLifecycle));
        aggregators.put(OMFItemCOMDEF.class, new ComdefItemAggregator());

        ItemAggregatorDispatcher dispatcher = new ItemAggregatorDispatcherImpl(aggregators);
        return new OMFFileFactoryImpl(dispatcher, ledataChunkLifecycle);
    }

    /**
     * Composition root for the linker layer. Segment ordering comes from
     * {@code Artifacts/SOURCE/EPROM.LNK}'s {@code -order segment _atext,_text,_data}.
     */
    public static Linker buildLinker()
    {
        AlignmentResolver alignmentResolver = new AlignmentResolverImpl();
        SegmentCombiner combiner = new SegmentCombinerImpl(alignmentResolver);
        SegmentLayouter layouter = new SegmentLayouterImpl(List.of("_atext", "_text", "_data"));
        SymbolResolver symbolResolver = new SymbolResolverImpl();
        return new LinkerImpl(combiner, layouter, symbolResolver);
    }

    /**
     * Composition root for the image-emit layer (Phase 4).
     *
     * @param dgroupParagraph runtime paragraph value for DGROUP. See
     *                        {@link FrameResolverImpl} for why this must be
     *                        supplied externally.
     */
    public static ImageEmitter buildImageEmitter(int dgroupParagraph)
    {
        ImageSizer sizer = new ImageSizerImpl();
        PieceLookupFactory pieceLookupFactory = new PieceLookupFactoryImpl();
        LedataPlacer ledataPlacer = new LedataPlacerImpl();
        ComdefAllocator comdefAllocator = new ComdefAllocatorImpl();
        GlobalSymbolTableFactory symbolTableFactory = new GlobalSymbolTableFactoryImpl(comdefAllocator);
        FrameResolver frameResolver = new FrameResolverImpl(dgroupParagraph);
        TargetResolver targetResolver = new TargetResolverImpl();
        FixupValueWriter valueWriter = new FixupValueWriterImpl(dgroupParagraph);
        FixupApplier fixupApplier = new FixupApplierImpl(frameResolver, targetResolver, valueWriter);
        return new ImageEmitterImpl(sizer, pieceLookupFactory, ledataPlacer,
                symbolTableFactory, fixupApplier);
    }
}
