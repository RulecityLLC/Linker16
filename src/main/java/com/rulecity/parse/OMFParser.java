package com.rulecity.parse;

import java.util.List;

public interface OMFParser
{
    List<OMFItem> parseBinary(byte[] src);
}
