package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.LinkedLayout;

import java.util.List;
import java.util.Map;

/**
 * Allocates space within c_common (BSS region) for every Communal symbol
 * declared by any module, and returns each communal's final image offset.
 * <p>
 * c_common starts at the first BSS-class combined segment in the layout.
 * Communals are ordered by name (case-insensitive ASCII) and each is
 * WORD-aligned; odd-sized communals leave a one-byte pad before the next
 * one. Size is the maximum declared across modules.
 */
public interface ComdefAllocator
{
    /** @return name → image offset for every communal across all modules. */
    Map<String, Integer> allocate(List<OMFFile> modulesInInputOrder, LinkedLayout layout);
}
