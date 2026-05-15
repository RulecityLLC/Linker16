package com.rulecity.parse;

import com.rulecity.parse.data.ExternalNamesDefinition;

import java.util.List;

public interface OMFItemEXTDEF extends OMFItem
{
    List<ExternalNamesDefinition> getExternalNamesDefinitions();

    boolean isLEXTDEF();
}
