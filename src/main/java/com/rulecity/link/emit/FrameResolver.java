package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.parse.data.FixupProcessed;

/**
 * Resolves a FIXUPP's frame to a 16-bit paragraph value — the value that
 * would be loaded into a real-mode segment register at runtime.
 */
public interface FrameResolver
{
    int paragraph(FixupProcessed fixup,
                  int moduleIdx,
                  OMFFile module,
                  PieceLookup lookup,
                  GlobalSymbolTable symbols,
                  int targetImageOffset);
}
