package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;

import java.util.List;

/**
 * Applies every FIXUPP in every LEDATA chunk in every module to the image
 * buffer. Unresolvable fixups (target symbol not yet in {@link GlobalSymbolTable})
 * are skipped silently — they'll be picked up by a later phase that augments
 * the symbol table.
 *
 * @return the number of fixups that could not be resolved
 */
public interface FixupApplier
{
    int apply(byte[] image,
              List<OMFFile> modulesInInputOrder,
              PieceLookup lookup,
              GlobalSymbolTable symbols);
}
