package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;

import java.util.List;

/**
 * Applies every FIXUPP in every LEDATA chunk in every module to the image
 * buffer. Fixups whose external target isn't present in {@link GlobalSymbolTable}
 * are skipped; the returned list names each missing symbol and the module that
 * referenced it so the caller can produce an actionable error.
 */
public interface FixupApplier
{
    record Unresolved(String symbolName, String referencingModule) {}

    List<Unresolved> apply(byte[] image,
                           List<OMFFile> modulesInInputOrder,
                           PieceLookup lookup,
                           GlobalSymbolTable symbols);
}
