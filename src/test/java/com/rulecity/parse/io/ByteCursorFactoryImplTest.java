package com.rulecity.parse.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ByteCursorFactoryImplTest
{
    @Test
    public void create_returnsNonNullCursor()
    {
        // Factory delegates to a concrete ByteCursorImpl constructor — no
        // collaborators to mock. The contract is just "give me back a non-null
        // cursor seeded with the source bytes". Behavior of the cursor itself
        // is owned by ByteCursorImplTest.
        ByteCursor result = new ByteCursorFactoryImpl().create(new byte[]{1, 2, 3});
        assertNotNull(result);
    }
}
