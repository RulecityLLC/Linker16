package com.rulecity.link.emit;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PieceLookupFactoryImplTest
{
    @Test
    public void build_returnsNonNullLookup()
    {
        // Factory delegates to a concrete PieceLookupImpl constructor — behavior
        // of the lookup itself is owned by PieceLookupImplTest.
        PieceLookup result = new PieceLookupFactoryImpl().build(List.of());
        assertNotNull(result);
    }
}
