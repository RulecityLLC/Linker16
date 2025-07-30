package com.rulecity.parse;

import java.util.List;

public interface ObjParser
{
    List<ObjItem> parseBinary(byte[] src);
}
