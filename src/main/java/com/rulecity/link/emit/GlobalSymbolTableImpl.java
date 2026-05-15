package com.rulecity.link.emit;

import java.util.Map;

public class GlobalSymbolTableImpl implements GlobalSymbolTable
{
    private final Map<String, Integer> byName;

    public GlobalSymbolTableImpl(Map<String, Integer> byName)
    {
        this.byName = byName;
    }

    @Override
    public Integer lookup(String name)
    {
        return byName.get(name);
    }
}
