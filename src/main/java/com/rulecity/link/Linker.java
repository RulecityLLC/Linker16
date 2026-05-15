package com.rulecity.link;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.LinkedLayout;

import java.util.List;

/**
 * Top-level linker entry point. Given the aggregated views of every input OBJ
 * in command-line order, produces a {@link LinkedLayout} describing where each
 * segment lives in the image and where every PUBDEF/LPUBDEF resolves to.
 * <p>
 * Phase 3 deliverable: this is enough to verify against {@code DL2.MAP}.
 * Phase 4 will use the same layout to emit bytes and apply FIXUPPs.
 */
public interface Linker
{
    LinkedLayout link(List<OMFFile> modulesInInputOrder);
}
