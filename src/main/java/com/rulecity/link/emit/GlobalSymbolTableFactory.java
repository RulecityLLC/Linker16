package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.LinkedLayout;

import java.util.List;

public interface GlobalSymbolTableFactory
{
    GlobalSymbolTable build(List<OMFFile> modulesInInputOrder, LinkedLayout layout);
}
