package com.rulecity.parse.data;

public record PublicNamesDefinition(String publicNameString,
        int publicOffset,
        // 1-based OMF index per TIS 1.1 (variable-width: 1..32767, wider than a byte).
        int typeIndex
)
{
}
