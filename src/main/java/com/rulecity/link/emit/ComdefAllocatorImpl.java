package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.CombinedSegment;
import com.rulecity.link.data.LinkedLayout;
import com.rulecity.parse.data.Communal;
import com.rulecity.parse.data.ExternalOrRelated;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ComdefAllocatorImpl implements ComdefAllocator
{
    private static final String BSS_CLASS = "bss";

    @Override
    public Map<String, Integer> allocate(List<OMFFile> modulesInInputOrder, LinkedLayout layout)
    {
        int cCommonStart = findBssStart(layout);

        TreeMap<String, Integer> maxSize = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (OMFFile module : modulesInInputOrder)
        {
            for (ExternalOrRelated e : module.getExternals())
            {
                Communal c = e.communal();
                if (c == null) continue;
                maxSize.merge(c.name(), c.length(), Math::max);
            }
        }

        Map<String, Integer> offsets = new LinkedHashMap<>();
        int pos = cCommonStart;
        for (Map.Entry<String, Integer> e : maxSize.entrySet())
        {
            pos = (pos + 1) & ~1;  // WORD-align
            offsets.put(e.getKey(), pos);
            pos += e.getValue();
        }
        return offsets;
    }

    private static int findBssStart(LinkedLayout layout)
    {
        for (CombinedSegment cs : layout.combinedSegments())
        {
            if (cs.className().equalsIgnoreCase(BSS_CLASS))
            {
                return cs.imageOffset();
            }
        }
        throw new RuntimeException(
                "No BSS-class combined segment found; cannot anchor c_common allocation");
    }
}
