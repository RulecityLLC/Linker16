package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.LinkedLayout;

import java.util.List;

/**
 * Phase-4 entry point: given the aggregated modules and the linker's layout,
 * returns the linked binary image with all LEDATA placed and all FIXUPPs
 * resolved.
 */
public interface ImageEmitter
{
    byte[] emit(List<OMFFile> modulesInInputOrder, LinkedLayout layout);
}
