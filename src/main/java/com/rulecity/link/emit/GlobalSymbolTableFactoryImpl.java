package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.LinkedLayout;
import com.rulecity.link.data.ResolvedSymbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Composes the global symbol table from two sources: PUBDEFs already resolved
 * by the linker (their image offsets are known from layout) and Communals
 * allocated by the {@link ComdefAllocator}. PUBDEFs win on name collisions.
 */
public class GlobalSymbolTableFactoryImpl implements GlobalSymbolTableFactory
{
    private final ComdefAllocator comdefAllocator;

    public GlobalSymbolTableFactoryImpl(ComdefAllocator comdefAllocator)
    {
        this.comdefAllocator = comdefAllocator;
    }

    @Override
    public GlobalSymbolTable build(List<OMFFile> modulesInInputOrder, LinkedLayout layout)
    {
        Map<String, Integer> byName = new HashMap<>();
        for (Map.Entry<String, Integer> e : comdefAllocator.allocate(modulesInInputOrder, layout).entrySet())
        {
            byName.put(e.getKey(), e.getValue());
        }
        for (ResolvedSymbol s : layout.publicSymbols())
        {
            byName.put(s.name(), s.imageOffset());  // PUBDEFs override Communals.
        }
        return new GlobalSymbolTableImpl(byName);
    }
}
