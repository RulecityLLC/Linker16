package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.parse.data.FixupProcessed;

/**
 * Resolves a FIXUPP's target to its final byte offset within the linked image.
 * The target offset already includes the target displacement (if the FIXUPP
 * has one), so the caller can use the returned value directly to compute the
 * fixup location's value.
 */
public interface TargetResolver
{
    /**
     * @return target image offset, or {@code null} if the symbol referenced is
     *         not (yet) defined anywhere (e.g. a COMDEF in a phase that doesn't
     *         allocate communals yet).
     */
    Integer imageOffset(FixupProcessed fixup,
                        int moduleIdx,
                        OMFFile module,
                        PieceLookup lookup,
                        GlobalSymbolTable symbols);
}
