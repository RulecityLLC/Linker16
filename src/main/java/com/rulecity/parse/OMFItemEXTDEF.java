package com.rulecity.parse;

import com.rulecity.parse.data.ExternalNamesDefinition;

import java.util.List;

public interface OMFItemEXTDEF
{
    List<ExternalNamesDefinition> getExternalNamesDefinitions();

    boolean isLEXTDEF();
}
