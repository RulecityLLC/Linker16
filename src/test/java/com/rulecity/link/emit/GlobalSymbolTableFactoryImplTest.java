package com.rulecity.link.emit;

import com.rulecity.aggregation.OMFFile;
import com.rulecity.link.data.LinkedLayout;
import com.rulecity.link.data.ResolvedSymbol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalSymbolTableFactoryImplTest
{
    @Mock private ComdefAllocator comdefAllocator;
    @Mock private OMFFile moduleA;
    @InjectMocks private GlobalSymbolTableFactoryImpl factory;

    @Test
    public void mergesPubdefsOverCommunalsOnNameCollision()
    {
        // A communal _foo at 0xA000, but a PUBDEF _foo at 0x100 must win.
        LinkedLayout layout = new LinkedLayout(List.of(),
                List.of(new ResolvedSymbol("_foo", 0x100, "M", "_text", false)),
                List.of());
        when(comdefAllocator.allocate(any(), any())).thenReturn(Map.of("_foo", 0xA000));

        GlobalSymbolTable t = factory.build(List.of(moduleA), layout);
        assertEquals(0x100, t.lookup("_foo"));
    }

    @Test
    public void includesBothPubdefsAndCommunals()
    {
        LinkedLayout layout = new LinkedLayout(List.of(),
                List.of(new ResolvedSymbol("pub", 0x500, "M", "_text", false)),
                List.of());
        when(comdefAllocator.allocate(any(), any())).thenReturn(Map.of("com", 0xA000));

        GlobalSymbolTable t = factory.build(List.of(moduleA), layout);
        assertEquals(0x500, t.lookup("pub"));
        assertEquals(0xA000, t.lookup("com"));
        assertNull(t.lookup("missing"));
    }
}
