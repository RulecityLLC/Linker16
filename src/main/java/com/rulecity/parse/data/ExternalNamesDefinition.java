package com.rulecity.parse.data;

// typeIndex: 1-based OMF index per TIS 1.1 (variable-width: 1..32767, wider than a byte).
public record ExternalNamesDefinition(String externalNameString, int typeIndex)
{
}
